// Note: When you recompile this, you need to delete: "C:\Users\eric\AppData\Local\Temp\arduino\cores\arduino_avr_uno_f742622285952b9ea3aafa09dbdb4e60" folder for some reason

#include <Arduino.h>
#include "ShiftRegisterPWM.h"

#define DATA_PIN 2
#define CLOCK_PIN 3
#define LATCH_PIN 4

const int SHIFT_REGISTER_COUNT = 2;
const int TOTAL_PINS = SHIFT_REGISTER_COUNT * 8;
ShiftRegisterPWM sr(SHIFT_REGISTER_COUNT, 128);

// Print debugging over serial port
// #define DEBUG_SERIAL

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

        // If we start or stop a song for now, we turn off all the lights
        //  This may change in the future
        else if (command == 'F' || command == 'S')
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
        delay(100);
    }

    for (int i = 0; i < TOTAL_PINS; i++)
    {
        setPin(i, 0);
        delay(100);
    }
}

void setup()
{
    Serial.begin(115200); // 115200

    // Setup the shift register
    pinMode(DATA_PIN, OUTPUT);  // sr data pin
    pinMode(CLOCK_PIN, OUTPUT); // sr clock pin
    pinMode(LATCH_PIN, OUTPUT); // sr latch pin

    sr.interrupt(ShiftRegisterPWM::UpdateFrequency::Medium);

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

    // Debugging Pins
    //debugAllPins();
}
