#include <Arduino.h>
#include "ShiftRegisterPWM.h"

const int shiftRegisterCount = 2;
const int amountOfPins = shiftRegisterCount * 8;
ShiftRegisterPWM sr(shiftRegisterCount, 8);

// Print debugging over serial port
// #define DEBUG_SERIAL

// Play the note on the physical piano
void playNote(int note, bool isOn, int velocity)
{

    // TODO: Remove this code when we have the physical piano.
    //      This is just for testing with LEDS
    if (note >= 42 && note < 58)
    {
        sr.set(note - 42, isOn ? velocity : 0);
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

                playNote(note, isOn, velocity);
            }
        }

        // If we start or stop a song for now, we turn off all the lights
        //  This may change in the future
        else if (command == 'F' || command == 'S')
        {
            // turn all the lights off
            for (int i = 0; i < amountOfPins; i++)
            {
                sr.set(i, 0);
            }
        }
    }
}

void setup()
{
    Serial.begin(115200); // 115200

    // Setup the shift register
    pinMode(2, OUTPUT); // sr data pin
    pinMode(3, OUTPUT); // sr clock pin
    pinMode(4, OUTPUT); // sr latch pin

    sr.interrupt(ShiftRegisterPWM::UpdateFrequency::VerySlow);

    pinMode(13, OUTPUT);
    digitalWrite(13, LOW);

    // Wait for the serial port to connect
    while (!Serial)
    {
        ;
    }

    Serial.println(F("Hello World!."));
}

void loop()
{
    // Serial code that breaks with solinoid
    processIncomingSerial();
}