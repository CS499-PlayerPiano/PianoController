/**
 * Library for PWM control of the 74HC595 shift register.
 * Created by Timo Denk (www.timodenk.com), 2017.
 * Additional information is available at https://timodenk.com/blog/shiftregister-pwm-library/
 * Released into the public domain.
 * https://github.com/Simsso/ShiftRegister-PWM-Library
 */

#ifndef ShiftRegisterPWM_h
#define ShiftRegisterPWM_h

#include <stdlib.h>

//If Arduino Uno, define interupt
#ifndef ESP32
  #include <avr/interrupt.h>


/**
 * Default pinning configuration
 * Can be changed from the .ino with #define ...
 * See "CustomPins" example sketch.
 */
  #ifndef ShiftRegisterPWM_DATA_PORT
    #define ShiftRegisterPWM_DATA_PORT PORTD
  #endif
  #ifndef ShiftRegisterPWM_DATA_MASK
    #define ShiftRegisterPWM_DATA_MASK 0B00000100
  #endif
  
  #ifndef ShiftRegisterPWM_CLOCK_PORT
    #define ShiftRegisterPWM_CLOCK_PORT PORTD
  #endif
  #ifndef ShiftRegisterPWM_CLOCK_MASK
    #define ShiftRegisterPWM_CLOCK_MASK 0B00001000
  #endif
  
  #ifndef ShiftRegisterPWM_LATCH_PORT
    #define ShiftRegisterPWM_LATCH_PORT PORTD
  #endif
  #ifndef ShiftRegisterPWM_LATCH_MASK
    #define ShiftRegisterPWM_LATCH_MASK 0B00010000
  #endif
  
  #define ShiftRegisterPWM_setDataPin() ShiftRegisterPWM_DATA_PORT |= ShiftRegisterPWM_DATA_MASK;
  #define ShiftRegisterPWM_clearDataPin() ShiftRegisterPWM_DATA_PORT &= ~ShiftRegisterPWM_DATA_MASK;
  #define ShiftRegisterPWM_toggleClockPinTwice()                  \
      ShiftRegisterPWM_CLOCK_PORT ^= ShiftRegisterPWM_CLOCK_MASK; \
      ShiftRegisterPWM_CLOCK_PORT ^= ShiftRegisterPWM_CLOCK_MASK
  #define ShiftRegisterPWM_toggleLatchPinTwice()                  \
      ShiftRegisterPWM_LATCH_PORT ^= ShiftRegisterPWM_LATCH_MASK; \
      ShiftRegisterPWM_LATCH_PORT ^= ShiftRegisterPWM_LATCH_MASK

#else
//If ESP32 is defined
  #define ShiftRegisterPWM_setDataPin() GPIO.out_w1ts = 1UL << DATA_PIN;
  #define ShiftRegisterPWM_clearDataPin() GPIO.out_w1tc = 1UL << DATA_PIN;
  #define ShiftRegisterPWM_toggleClockPinTwice()                  \
      GPIO.out_w1ts = 1UL << CLOCK_PIN; \
      GPIO.out_w1tc = 1UL << CLOCK_PIN
  #define ShiftRegisterPWM_toggleLatchPinTwice()                  \
      GPIO.out_w1ts = 1UL << LATCH_PIN; \
      GPIO.out_w1tc = 1UL << LATCH_PIN

void IRAM_ATTR Timer0_ISR();
#endif




int map2(unsigned x, unsigned in_max, unsigned out_max)
{
    return (unsigned) ((x * out_max + (in_max / 2)) / in_max);
}

class ShiftRegisterPWM
{
public:
    enum UpdateFrequency
    {
        VerySlow, // 6,400 Hz interrupt
        Slow,     // 12,800 Hz interrupt
        Medium,   // 25,600 Hz interrupt
        Fast,     // 35,714 Hz interrupt
        SuperFast // 51,281 Hz interrupt
    };

    /**
     * Constructor for a new ShiftRegisterPWM object.
     * An object is equivalent to one shift register or multiple, serially connected shift registers.
     * @param shiftRegisterCount Number of serially connected shift registers. For a single one just 1. Maximum is 8. However, performance is likely to start causing trouble with lower values.
     * @param resolution PWM resolution, that is the number of possible PWM value. The value has to be between 2 and 255 (due to performance limitations).
     */
    ShiftRegisterPWM(uint8_t shiftRegisterCount, uint8_t resolution)
    {
        // set attributes
        this->shiftRegisterCount = shiftRegisterCount;
        this->resolution = resolution;

        // init data
        // internally a two-dimensional array: first dimension time, second dimension shift register bytes
        // data[t + sr * resolution]
        this->data = (uint8_t *)malloc(resolution * shiftRegisterCount * sizeof(uint8_t));
        for (int t = 0; t < resolution; ++t)
        {
            for (int i = 0; i < this->shiftRegisterCount; ++i)
            {
                this->data[t + i * resolution] = 0;
            }
        }

        ShiftRegisterPWM::singleton = this; // make this object accessible for timer interrupts

        // the boolean will be used to increase the performance in other functions
        singleShiftRegister = (shiftRegisterCount == 1);
    };

    /**
     * Set a pin of the shift register to a given PWM value.
     * @param pin The index of the pin (starting at 0). If multiple shift registers are chained, the first pin of the second shift register would be addressed with pin = 8.
     * @param value The PWM value (between 0 and 255 as it will be scaled to the resolution that was passed to the constructor).
     */
    void set(uint8_t pin, uint8_t value)
    {
#ifdef ALG_BRESENHAM
      value = map2(value, 255, resolution);
      int dx = resolution;
      int dy = (int) value;
      int D = (int) (2 * dy - dx);
      uint8_t shiftRegister = pin / 8;
      for (int t = 0; t < resolution; ++t)
      {
          int on = 0;

          if(D > 0)
          {
            on = 1;
            D -= (2 * dx);
          }
          else
          {
            on = 0;
          }
          D += (2 * dy);
          this->data[t + shiftRegister * resolution] ^= (-on ^ this->data[t + shiftRegister * resolution]) & (1 << (pin % 8));
      }
#endif
#ifdef ALG_NAIVE        
      
        value = (uint8_t)(value / 255.0 * resolution + .5); // round
        uint8_t shiftRegister = pin / 8;
        for (int t = 0; t < resolution; ++t)
        {
            // set (pin % 8)th bit to (value > t)
            this->data[t + shiftRegister * resolution] ^= (-(value > t) ^ this->data[t + shiftRegister * resolution]) & (1 << (pin % 8));
        }
#endif
        
    };

    /**
     * Updates the shift register outputs. This function should be called as frequently as possible, usually within an ISR to guarantee a fixed update frequency.
     * For manual operation it is important to ensure that the function is called with a constant frequency that is suitable for the application. Commonly that is around 50 Hz * resolution for LEDs.
     */
    void update()
    {
        // higher performance for single shift register mode
        if (singleShiftRegister)
        {
            shiftOut(this->data[time]);
        }
        else
        {
            for (int i = this->shiftRegisterCount - 1; i >= 0; i--)
            {
                shiftOut(this->data[time + i * resolution]);
            }
        }
        ShiftRegisterPWM_toggleLatchPinTwice();

        if (++time == resolution)
        {
            time = 0;
        }
    };

    /**
     * Calles void ShiftRegisterPWM::interrupt(UpdateFrequency updateFrequency) with the UpdateFrequency Medium
     * Have a look at the called function for more details.
     */
    void interrupt() const
    {
        this->interrupt(ShiftRegisterPWM::UpdateFrequency::Medium);
    };

    /**
     * Initializes and starts the timer interrupt with a given update frequency.
     * The used timer is the Arduino UNO's timer 1.
     * The function can be called multiple times with different update frequencies in order to change the update frequency at any time.
     * @param updateFrequency The update frequencies are either VerySlow @ 6,400 Hz, Slow @ 12,800 Hz, Fast @ 35,714 Hz, SuperFast @ 51,281.5 Hz, and Medium @ 25,600 Hz.
     * The actual PWM cycle length in seconds can be calculated by (resolution / frequency).
     */
    void interrupt(UpdateFrequency updateFrequency) const
    {
//If its a arduino UNO board
#ifndef ESP32      
        cli(); // disable interrupts

        // reset
        TCCR1A = 0; // set TCCR1A register to 0
        TCCR1B = 0; // set TCCR1B register to 0
        TCNT1 = 0;  // reset counter value

        switch (updateFrequency)
        {
        case VerySlow:             // exactly 6,400 Hz interrupt frequency
            OCR1A = 2499;          // compare match register
            TCCR1B |= (1 << CS10); // prescaler 1
            break;

        case Slow:                 // exactly 12,800 Hz interrupt frequency
            OCR1A = 1249;          // compare match register
            TCCR1B |= (1 << CS10); // prescaler 1
            break;

        case Fast:                 // aprox. 35,714 Hz interrupt frequency
            OCR1A = 55;            // compare match register
            TCCR1B |= (1 << CS11); // prescaler 8
            break;

        case SuperFast:            // approx. 51,281.5 Hz interrupt frequency
            OCR1A = 311;           // compare match register
            TCCR1B |= (1 << CS10); // prescaler 1
            break;

        case Medium: // exactly 25,600 Hz interrupt frequency
        default:
            OCR1A = 624;           // compare match register
            TCCR1B |= (1 << CS10); // prescaler 1
            break;
        }

        TCCR1B |= (1 << WGM12);  // turn on CTC mode
        TIMSK1 |= (1 << OCIE1A); // enable timer compare interrupt

        sei(); // allow interrupts
//ESP32
#else
        uint16_t timerCount; 
        
        switch (updateFrequency)
        {
        case VerySlow:             // exactly 6,400 Hz interrupt frequency
            timerCount = 2500;
            break;

        case Slow:                 // exactly 12,800 Hz interrupt frequency
            timerCount = 1250;
            break;

        case Fast:                 // aprox. 35,714 Hz interrupt frequency
            timerCount = 448;
            break;

        case SuperFast:            // approx. 51,281.5 Hz interrupt frequency
             timerCount = 312;
            break;

        case Medium: // exactly 25,600 Hz interrupt frequency
        default:
            timerCount = 625;
            break;
        }

        hw_timer_t * timer0 = timerBegin(0, 5, true); //Set prescaler to 5
        timerAttachInterrupt(timer0, &Timer0_ISR, true);
        timerAlarmWrite(timer0, timerCount, true);
        timerAlarmEnable(timer0);

#endif
    }

    static ShiftRegisterPWM *singleton; // used inside the ISR

private:
    uint8_t shiftRegisterCount = 1; // number of chained shift registers
    uint8_t resolution = 255;
    uint8_t *data;             // data matrix [t + sr * resolution]
    volatile uint8_t time = 0; // time resolution of resolution steps

    /**
     * Shift out function. Performance optimized of Arduino's default shiftOut.
     * See https://timodenk.com/blog/port-manipulation-and-arduino-digitalwrite-performance/ for timing measurements.
     */
    inline void shiftOut(uint8_t data) const
    {
        // unrolled for loop
        // bit 0 (LSB)
        if (data & 0B00000001)
        {
            ShiftRegisterPWM_setDataPin();
        }
        else
        {
            ShiftRegisterPWM_clearDataPin();
        }
        ShiftRegisterPWM_toggleClockPinTwice();

        // bit 1
        if (data & 0B00000010)
        {
            ShiftRegisterPWM_setDataPin();
        }
        else
        {
            ShiftRegisterPWM_clearDataPin();
        }
        ShiftRegisterPWM_toggleClockPinTwice();

        // bit 2
        if (data & 0B00000100)
        {
            ShiftRegisterPWM_setDataPin();
        }
        else
        {
            ShiftRegisterPWM_clearDataPin();
        }
        ShiftRegisterPWM_toggleClockPinTwice();

        // bit 3
        if (data & 0B00001000)
        {
            ShiftRegisterPWM_setDataPin();
        }
        else
        {
            ShiftRegisterPWM_clearDataPin();
        }
        ShiftRegisterPWM_toggleClockPinTwice();

        // bit 4
        if (data & 0B00010000)
        {
            ShiftRegisterPWM_setDataPin();
        }
        else
        {
            ShiftRegisterPWM_clearDataPin();
        }
        ShiftRegisterPWM_toggleClockPinTwice();

        // bit 5
        if (data & 0B00100000)
        {
            ShiftRegisterPWM_setDataPin();
        }
        else
        {
            ShiftRegisterPWM_clearDataPin();
        }
        ShiftRegisterPWM_toggleClockPinTwice();

        // bit 6
        if (data & 0B01000000)
        {
            ShiftRegisterPWM_setDataPin();
        }
        else
        {
            ShiftRegisterPWM_clearDataPin();
        }
        ShiftRegisterPWM_toggleClockPinTwice();

        // bit 7
        if (data & 0B10000000)
        {
            ShiftRegisterPWM_setDataPin();
        }
        else
        {
            ShiftRegisterPWM_clearDataPin();
        }
        ShiftRegisterPWM_toggleClockPinTwice();
    };

    bool singleShiftRegister; // true if (shiftRegisterCount == 1)
};

// One static reference to the ShiftRegisterPWM that was lastly created. Used for access through timer interrupts.
ShiftRegisterPWM *ShiftRegisterPWM::singleton = NULL;

/**
 * The ISR is being used unless the CUSTOM_INTERRUPT macro is defined.
 * That way this library can be used in combination with libraries that rely on Timer 1.
 * See CustomTimerInterrupt example sketch.
 */

//Arduino UNO
#ifndef ESP32
  #ifndef ShiftRegisterPWM_CUSTOM_INTERRUPT
  // Timer 1 interrupt service routine (ISR)
  ISR(TIMER1_COMPA_vect)
  {          // function which will be called when an interrupt occurs at timer 1
      cli(); // disable interrupts (in case update method takes too long)
      ShiftRegisterPWM::singleton->update();
      sei(); // re-enable
  };
  #endif

//ESP32 Boards
#else
  void IRAM_ATTR Timer0_ISR()
  {
    ShiftRegisterPWM::singleton->update();
  }
#endif

//END OF FILE
#endif
