// NOTE TO FUTURE:
/*
Serial.read() DOES NOT WAIT FOR DATA TO BE AVAILABLE. It will return 255 if there is no data available.
Serial.readBytes() WILL WAIT FOR DATA TO BE AVAILABLE. It will return the number of bytes read.
*/

//----------------SETTINGS------------------------------
// Algorithm: ALG_NAIVE, ALG_BRESENHAM
#define ALG_NAIVE
// #define ALG_BRESENHAM

// Print debugging over serial port
// #define DEBUG_SERIAL

// Amount of boards we have hooked up
#define SHIFT_REGISTER_COUNT 11

// How often we check for note updates
#define VELOCITY_UPDATE_INTERVAL 25

// Num milliseconds we PWM on before 255
#define MAX_PWM_ON_TIME 150

// Time to report errors to the console in MS
#define ERROR_REPORT_TIME 5000

// Speed of serial
#define SERIAL_SPEED 115200

//-----------------------------------------------

#include <Arduino.h>
#include "ServoTimer2.h"
#include "ShiftRegisterPWM.h"
#include <avr/wdt.h>

#define DATA_PIN 2
#define CLOCK_PIN 3
#define LATCH_PIN 4
#define RELAY_PIN 5
#define SERVO_PIN 6
#define CHECKBIT_PIN 7

ServoTimer2 sustainServo;
// Pos 0
#define SERVO_MIN 600

// Pos 180
#define SERVO_MAX 2300

const int TOTAL_PINS = SHIFT_REGISTER_COUNT * 8;
ShiftRegisterPWM sr(SHIFT_REGISTER_COUNT, 64, CHECKBIT_PIN); // Run out of memory?

// time the key went down
long pwmStartTime[TOTAL_PINS];

long lastError = 0;

void setPin(int pin, int value)
{
  if (pin < 0 || pin >= TOTAL_PINS)
  {
    return;
  }

  /*
  Currently the shift register code, 0 is the last pin on the last shift register
  We want 0 to be the first pin on the first shift register.
  */
  sr.set(TOTAL_PINS - pin - 1, value);
}

long lastVelocityTime = 0;
void updatePinVelocity()
{
  long currentTime = millis();
  if (currentTime - lastVelocityTime > VELOCITY_UPDATE_INTERVAL && currentTime > MAX_PWM_ON_TIME)
  {
    lastVelocityTime = currentTime;
    long cutoffTime = currentTime - MAX_PWM_ON_TIME;
    for (int i = 0; i < TOTAL_PINS; i++)
    {
      if (pwmStartTime[i] != 0 && pwmStartTime[i] < cutoffTime)
      {
        pwmStartTime[i] = 0;
        setPin(i, 255);
      }
    }
  }
}

// Read the serial port and process the incoming data from the PianoController software.
/*
Protocol:
    (char) 'N' - Note data
        (byte) <number of notes>
        (byte array) <note data>
            Note data:
                (byte) <note>
                (byte) <velocity>

    (char) 'F' - Finished playing song
    (char) 'S' - Start of song
*/

void parseNPacket()
{
  byte howManyNotes;
  Serial.readBytes(&howManyNotes, 1);

  // SANITY CHECK
  if (howManyNotes > 88)
  {
    return;
  }

  // each note is 2 bytes. Note number and velocity
  // isOn if velocity is not 0
  int howManyBytes = howManyNotes * 2;

  // read the note data
  byte noteData[howManyBytes];
  int read = Serial.readBytes(noteData, howManyBytes);

  if (read != howManyBytes)
  {
    // #ifdef DEBUG_SERIAL
    //     Serial.print(F("Error reading NPacket note data. Expected "));
    //     Serial.print(howManyBytes);
    //     Serial.print(F(" bytes but got "));
    //     Serial.print(read);
    //     Serial.print(F(". Tried to read "));
    //     Serial.print(howManyNotes);
    //     Serial.println(F(" notes."));
    // #endif

    return;
  }

  // #ifdef DEBUG_SERIAL
  //   Serial.print(F("Recieved "));
  //   Serial.print(howManyNotes);
  //   Serial.println(F(" notes."));
  // #endif

  for (int i = 0; i < howManyNotes; i++)
  {
    byte note = noteData[i * 2];
    byte velocity = noteData[i * 2 + 1];

    // SANITY CHECK
    if (note > 87)
    {
      return;
    }

    if (velocity != 0 && velocity != 255)
    {
      pwmStartTime[note] = millis();
    }
    else
    {
      pwmStartTime[note] = 0;
    }

    setPin(note, velocity);
  }
}

void parseBPacket()
{
  byte numOfBatches;
  Serial.readBytes(&numOfBatches, 1);
  int howManyBytes = numOfBatches * 3;

  // read the note data
  byte noteData[howManyBytes];
  int read = Serial.readBytes(noteData, howManyBytes);

  if (read != howManyBytes)
  {
#ifdef DEBUG_SERIAL
    Serial.print(F("Error reading BPacket note data. Expected "));
    Serial.print(howManyBytes);
    Serial.print(F(" bytes but got "));
    Serial.print(read);
    Serial.print(F(". Tried to read "));
    Serial.print(numOfBatches);
    Serial.println(F(" batches."));
#endif

    return;
  }

#ifdef DEBUG_SERIAL
  Serial.print(F("Recieved "));
  Serial.print(numOfBatches);
  Serial.println(F(" batches."));
#endif

  for (int i = 0; i < numOfBatches; i++)
  {
    byte contiguous = noteData[i * 3];
    byte startingNote = noteData[i * 3 + 1];
    byte velocity = noteData[i * 3 + 2];

    for (int j = 0; j < contiguous; j++)
    {
      byte note = startingNote + j;

      if (note < 0 || note >= TOTAL_PINS)
      {
        continue;
      }

      if (velocity != 0 && velocity != 255)
      {
        pwmStartTime[note] = millis();
      }
      else
      {
        pwmStartTime[note] = 0;
      }

      setPin(note, velocity);
    }
  }
}

/**
 * M Packet
 * Number of notes
 * Velocity
 * Array:
 *  Note number
 *
 */
void parseMPacket()
{
  byte numberOfNotes;
  Serial.readBytes(&numberOfNotes, 1);

  if (numberOfNotes > 88)
  {
    return;
  }

  byte velocity;
  Serial.readBytes(&velocity, 1);

  int howManyBytes = numberOfNotes;

  // read the note data
  byte noteData[howManyBytes];

  int read = Serial.readBytes(noteData, howManyBytes);

  if (read != howManyBytes)
  {
#ifdef DEBUG_SERIAL
    Serial.print(F("Error reading MPacket note data. Expected "));
    Serial.print(howManyBytes);
    Serial.print(F(" bytes but got "));
    Serial.print(read);
    Serial.println(F("."));
#endif

    return;
  }

#ifdef DEBUG_SERIAL
  Serial.print(F("Recieved "));
  Serial.print(numberOfNotes);
  Serial.println(F(" notes."));
#endif

  for (int i = 0; i < numberOfNotes; i++)
  {
    byte note = noteData[i];

    if (note < 0 || note >= TOTAL_PINS)
    {
      continue;
    }

    if (velocity != 0 && velocity != 255)
    {
      pwmStartTime[note] = millis();
    }
    else
    {
      pwmStartTime[note] = 0;
    }

    setPin(note, velocity);
#ifdef DEBUG_SERIAL
    Serial.print(F("N: "));
    Serial.print(note);
    Serial.print(F(" V: "));
    Serial.println(velocity);
#endif
  }
}

void parseOPacket()
{
  // turn all the lights off
  for (int i = 0; i < TOTAL_PINS; i++)
  {
    setPin(i, 0);
    pwmStartTime[i] = 0;
  }
}

void parseSPacket()
{
  byte servoPos;
  Serial.readBytes(&servoPos, 1);
  int val = map(servoPos, 0, 255, SERVO_MIN, SERVO_MAX);
  sustainServo.write(val);
}

void reset5VPower(int del)
{
  // Turn off the 5v for a sec
  digitalWrite(RELAY_PIN, HIGH);
  delay(del);
  digitalWrite(RELAY_PIN, LOW);
}

bool ignoreErrorChecker = false;
void parsePPacket()
{
  ignoreErrorChecker = true;
  reset5VPower(500);
}

void processIncomingSerial()
{
  // Read the midi data from the serial port
  while (Serial.available() >= 1)
  {

    // Read the first character as the type of command
    byte command = Serial.read();

    if (command == 'N')
    {
      parseNPacket();
    }
    else if (command == 'B')
    {
      parseBPacket();
    }
    else if (command == 'M')
    {
      parseMPacket();
    }
    else if (command == 'O')
    {
      parseOPacket();
    }
    else if (command == 'S')
    {
      parseSPacket();
    }
    else if (command == 'P')
    {
      parsePPacket();
    }
    updatePinVelocity();
  }
}

void checkErrorState()
{

  // If we are ignoring the error due to Java resetting the 5v power, don't check it this time.
  if (ignoreErrorChecker)
  {
    ignoreErrorChecker = false;
    sr.checkBitError = false; // reset the physical error bit
    return;
  }

  // We have an error
  if (sr.checkBitError)
  {
    if (lastError + ERROR_REPORT_TIME < millis())
    {
      lastError = millis();

      Serial.println(F("Error bit detected!"));

      // Turn off and on the relay
      reset5VPower(1000);
    }

    sr.checkBitError = false; // reset the error
  }
}

void debugAllPins()
{
  for (int i = 0; i < TOTAL_PINS; i++)
  {
    setPin(i, 255);
    // sr.update();
    delay(100);
  }

  for (int i = 0; i < TOTAL_PINS; i++)
  {
    setPin(i, 0);
    // sr.update();
    delay(100);
  }
}

void setup()
{
  Serial.begin(SERIAL_SPEED); // 115200

  // Setup the shift register
  pinMode(DATA_PIN, OUTPUT);  // sr data pin
  pinMode(CLOCK_PIN, OUTPUT); // sr clock pin
  pinMode(LATCH_PIN, OUTPUT); // sr latch pin

  pinMode(CHECKBIT_PIN, INPUT); // input for reading check bit.

  sustainServo.attach(SERVO_PIN);
  sustainServo.write(SERVO_MIN);

  pinMode(RELAY_PIN, OUTPUT);
  digitalWrite(RELAY_PIN, LOW);

  sr.interrupt(ShiftRegisterPWM::UpdateFrequency::VerySlow);

  for (int i = 0; i < TOTAL_PINS; i++)
  {
    pwmStartTime[i] = 0;
  }

  // Wait for the serial port to connect
  while (!Serial)
  {
    ;
  }

  Serial.println(F("Hello from Arduino!"));
}

void loop()
{
  // Serial code that breaks with solinoid
  processIncomingSerial();
  updatePinVelocity();
  checkErrorState();

  // Debugging Pins
  // debugAllPins();
}
