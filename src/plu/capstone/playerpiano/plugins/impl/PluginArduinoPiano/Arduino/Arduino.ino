// Note: When you recompile this, you need to delete: "C:\Users\eric\AppData\Local\Temp\arduino\cores\arduino_avr_uno_f742622285952b9ea3aafa09dbdb4e60" folder for some reason

//----------------SETTINGS------------------------------
//Algorithm: ALG_NAIVE, ALG_BRESENHAM
//#define ALG_NAIVE
#define ALG_BRESENHAM

// Print debugging over serial port
// #define DEBUG_SERIAL

//Amount of boards we have hooked up
#define SHIFT_REGISTER_COUNT 11

//-----------------------------------------------

#include <Arduino.h>


//ESP32 specific settings
#ifdef ESP32
  #define DATA_PIN 26
  #define CLOCK_PIN 14
  #define LATCH_PIN 27
  #define SERIAL_SPEED 500000
#else
//Arduino UNO specific settings
  #define DATA_PIN 2
  #define CLOCK_PIN 3
  #define LATCH_PIN 4
  #define SERIAL_SPEED 115200
#endif


#include "ShiftRegisterPWM.h"

const int TOTAL_PINS = SHIFT_REGISTER_COUNT * 8;
ShiftRegisterPWM sr(SHIFT_REGISTER_COUNT, 128);


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

// Read the serial port and process the incoming data from the PianoController software.
/*
Protocol:
    (char) 'N' - Note data
        (byte) <number of notes>
        (byte array) <note data>
            Note data:
                (byte) <note>
                (byte) <isOn>
                (byte) <velocity>

    (char) 'F' - Finished playing song
    (char) 'S' - Start of song
*/

void processIncomingSerial()
{
    // Read the midi data from the serial port
    while (Serial.available() >= 2)
    {

        // Read the first character as the type of command
        byte command = Serial.read();

        if (command == 'N')
        {

            byte howManyNotes = Serial.read();

            // each note is 3 bytes
            int howManyBytes = howManyNotes * 3;

            // read the note data
            byte noteData[howManyBytes];
            int read = Serial.readBytes(noteData, howManyBytes);

            if (read != howManyBytes)
            {
#ifdef DEBUG_SERIAL
                Serial.print(F("Error reading note data. Expected "));
                Serial.print(howManyBytes);
                Serial.print(F(" bytes but got "));
                Serial.print(read);
                Serial.print(F(". Tried to read "));
                Serial.print(howManyNotes);
                Serial.println(F(" notes."));
#endif

                continue;
            }

#ifdef DEBUG_SERIAL
            Serial.print(F("Recieved "));
            Serial.print(howManyNotes);
            Serial.println(F(" notes."));
#endif

            for (int i = 0; i < howManyNotes; i++)
            {
                byte note = noteData[i * 3];
                bool isOn = noteData[i * 3 + 1] == 1;
                byte velocity = noteData[i * 3 + 2];

                setPin(note, isOn ? velocity : 0);
            }
        }

        /*
            Turn off all the keys
            S == Start of song
            F == Finished playing song
            P == Pause
        */
        else if (command == 'F' || command == 'S' || command == 'P')
        {
            // turn all the lights off
            for (int i = 0; i < TOTAL_PINS; i++)
            {
                setPin(i, 0);
            }
        }
    }
}


void debugAllPins()
{
    for (int i = 0; i < TOTAL_PINS; i++)
    {
        setPin(i, 255);
        //sr.update();
        delay(100);
    }

    for (int i = 0; i < TOTAL_PINS; i++)
    {
        setPin(i, 0);
        //sr.update();
        delay(100);
    }
}

#ifdef ESP32

  TaskHandle_t Task1;
  
  void Task1code( void * parameter) {
    while(true)
    {
      sr.update();
    }
  }

#endif

void setup()
{
    Serial.begin(SERIAL_SPEED); // 115200

    // Setup the shift register
    pinMode(DATA_PIN, OUTPUT);  // sr data pin
    pinMode(CLOCK_PIN, OUTPUT); // sr clock pin
    pinMode(LATCH_PIN, OUTPUT); // sr latch pin

#ifdef ESP32
    xTaskCreatePinnedToCore(
      Task1code, /* Function to implement the task */
      "Task1", /* Name of the task */
      10000,  /* Stack size in words */
      NULL,  /* Task input parameter */
      0,  /* Priority of the task */
      &Task1,  /* Task handle. */
      0); /* Core where the task should run */
#else
    sr.interrupt(ShiftRegisterPWM::UpdateFrequency::VerySlow);
#endif

    // Wait for the serial port to connect
    while (!Serial)
    {
        ;
    }

    Serial.println(F("Hello from Arduino!"));
}

void loop()
{
    //Serial code that breaks with solinoid
    processIncomingSerial();

//    long start = micros();
//    for (int i = 0; i < 1000; ++i) {
//      sr.update();
//    }
//    long end = micros();
//
//    Serial.print("Time to update (microseconds): ");
//    Serial.println((end - start) / 1000);

    // Debugging Pins
    //debugAllPins();
}
