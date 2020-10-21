#include "Pot.h"
#include "Arduino.h"

Pot::Pot(int pin){
  this->pin = pin;
} 
  
float Pot::getValue(){
  float val = analogRead(pin);
  return map(val, 0, 1023,0, 500); /*(ipotizziamo che il dumpster supporti 500kg) */
}
