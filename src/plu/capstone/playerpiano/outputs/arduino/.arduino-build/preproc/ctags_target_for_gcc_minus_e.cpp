# 1 "C:\\Users\\eric\\Documents\\PLU\\Year 5\\Capstone\\Piano Project\\PianoController\\src\\plu\\capstone\\playerpiano\\plugins\\impl\\PluginArduinoPiano\\Arduino\\Arduino.ino"
// Note: When you recompile this, you need to delete: "C:\Users\eric\AppData\Local\Temp\arduino\cores\arduino_avr_uno_f742622285952b9ea3aafa09dbdb4e60" folder for some reason

//----------------SETTINGS------------------------------
// Algorithm: ALG_NAIVE, ALG_BRESENHAM

// #define ALG_BRESENHAM

// Print debugging over serial port
// #define DEBUG_SERIAL

// Amount of boards we have hooked up


// How often we check for note updates


// Num milliseconds we PWM on before 255


//-----------------------------------------------

# 23 "C:\\Users\\eric\\Documents\\PLU\\Year 5\\Capstone\\Piano Project\\PianoController\\src\\plu\\capstone\\playerpiano\\plugins\\impl\\PluginArduinoPiano\\Arduino\\Arduino.ino" 2

// ESP32 specific settings






// Arduino UNO specific settings






# 39 "C:\\Users\\eric\\Documents\\PLU\\Year 5\\Capstone\\Piano Project\\PianoController\\src\\plu\\capstone\\playerpiano\\plugins\\impl\\PluginArduinoPiano\\Arduino\\Arduino.ino" 2

const int TOTAL_PINS = 11 * 8;
ShiftRegisterPWM sr(11, 64); // Run out of memory?

// time the key went down
long pwmStartTime[TOTAL_PINS];

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
# 57 "C:\\Users\\eric\\Documents\\PLU\\Year 5\\Capstone\\Piano Project\\PianoController\\src\\plu\\capstone\\playerpiano\\plugins\\impl\\PluginArduinoPiano\\Arduino\\Arduino.ino"
    sr.set(TOTAL_PINS - pin - 1, value);
}

long lastVelocityTime = 0;
void updatePinVelocity()
{
    long currentTime = millis();
    if (currentTime - lastVelocityTime > 25 && currentTime > 500)
    {
        lastVelocityTime = currentTime;
        long cutoffTime = currentTime - 500;
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
# 93 "C:\\Users\\eric\\Documents\\PLU\\Year 5\\Capstone\\Piano Project\\PianoController\\src\\plu\\capstone\\playerpiano\\plugins\\impl\\PluginArduinoPiano\\Arduino\\Arduino.ino"
void parseNPacket()
{
    byte howManyNotes = Serial.read();

    // each note is 2 bytes. Note number and velocity
    // isOn if velocity is not 0
    int howManyBytes = howManyNotes * 2;

    // read the note data
    byte noteData[howManyBytes];
    int read = Serial.readBytes(noteData, howManyBytes);

    if (read != howManyBytes)
    {
# 117 "C:\\Users\\eric\\Documents\\PLU\\Year 5\\Capstone\\Piano Project\\PianoController\\src\\plu\\capstone\\playerpiano\\plugins\\impl\\PluginArduinoPiano\\Arduino\\Arduino.ino"
        return;
    }







    for (int i = 0; i < howManyNotes; i++)
    {
        byte note = noteData[i * 2];
        byte velocity = noteData[i * 2 + 1];

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
    byte numOfBatches = Serial.read();
    int howManyBytes = numOfBatches * 3;

    // read the note data
    byte noteData[howManyBytes];
    int read = Serial.readBytes(noteData, howManyBytes);

    if (read != howManyBytes)
    {
# 165 "C:\\Users\\eric\\Documents\\PLU\\Year 5\\Capstone\\Piano Project\\PianoController\\src\\plu\\capstone\\playerpiano\\plugins\\impl\\PluginArduinoPiano\\Arduino\\Arduino.ino"
        return;
    }







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

void parseMPacket()
{
    byte numberOfNotes = Serial.read();
    byte velocity = Serial.read();

    int howManyBytes = numberOfNotes;

    // read the note data
    byte noteData[howManyBytes];

    int read = Serial.readBytes(noteData, howManyBytes);

    if (read != howManyBytes)
    {
# 227 "C:\\Users\\eric\\Documents\\PLU\\Year 5\\Capstone\\Piano Project\\PianoController\\src\\plu\\capstone\\playerpiano\\plugins\\impl\\PluginArduinoPiano\\Arduino\\Arduino.ino"
        return;
    }







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
    }
}

void parseFSPPacket()
{
    // turn all the lights off
    for (int i = 0; i < TOTAL_PINS; i++)
    {
        setPin(i, 0);
        pwmStartTime[i] = 0;
    }
}

void processIncomingSerial()
{
    // Read the midi data from the serial port
    while (Serial.available() >= 2)
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

        /*

            Turn off all the keys

            S == Start of song

            F == Finished playing song

            P == Pause

        */
# 293 "C:\\Users\\eric\\Documents\\PLU\\Year 5\\Capstone\\Piano Project\\PianoController\\src\\plu\\capstone\\playerpiano\\plugins\\impl\\PluginArduinoPiano\\Arduino\\Arduino.ino"
        else if (command == 'F' || command == 'S' || command == 'P')
        {
            parseFSPPacket();
        }
        updatePinVelocity();
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
# 332 "C:\\Users\\eric\\Documents\\PLU\\Year 5\\Capstone\\Piano Project\\PianoController\\src\\plu\\capstone\\playerpiano\\plugins\\impl\\PluginArduinoPiano\\Arduino\\Arduino.ino"
void setup()
{
    Serial.begin(115200); // 115200

    // Setup the shift register
    pinMode(2, 0x1); // sr data pin
    pinMode(3, 0x1); // sr clock pin
    pinMode(4, 0x1); // sr latch pin
# 351 "C:\\Users\\eric\\Documents\\PLU\\Year 5\\Capstone\\Piano Project\\PianoController\\src\\plu\\capstone\\playerpiano\\plugins\\impl\\PluginArduinoPiano\\Arduino\\Arduino.ino"
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

    Serial.println((reinterpret_cast<const __FlashStringHelper *>(
# 365 "C:\\Users\\eric\\Documents\\PLU\\Year 5\\Capstone\\Piano Project\\PianoController\\src\\plu\\capstone\\playerpiano\\plugins\\impl\\PluginArduinoPiano\\Arduino\\Arduino.ino" 3
                  (__extension__({static const char __c[] __attribute__((__progmem__)) = (
# 365 "C:\\Users\\eric\\Documents\\PLU\\Year 5\\Capstone\\Piano Project\\PianoController\\src\\plu\\capstone\\playerpiano\\plugins\\impl\\PluginArduinoPiano\\Arduino\\Arduino.ino"
                  "Hello from Arduino!"
# 365 "C:\\Users\\eric\\Documents\\PLU\\Year 5\\Capstone\\Piano Project\\PianoController\\src\\plu\\capstone\\playerpiano\\plugins\\impl\\PluginArduinoPiano\\Arduino\\Arduino.ino" 3
                  ); &__c[0];}))
# 365 "C:\\Users\\eric\\Documents\\PLU\\Year 5\\Capstone\\Piano Project\\PianoController\\src\\plu\\capstone\\playerpiano\\plugins\\impl\\PluginArduinoPiano\\Arduino\\Arduino.ino"
                  )));
}

void loop()
{
    // Serial code that breaks with solinoid
    processIncomingSerial();
    updatePinVelocity();

    //    long start = micros();
    //    for (int i = 0; i < 1000; ++i) {
    //      sr.update();
    //    }
    //    long end = micros();
    //
    //    Serial.print("Time to update (microseconds): ");
    //    Serial.println((end - start) / 1000);

    // Debugging Pins
    // debugAllPins();
}
