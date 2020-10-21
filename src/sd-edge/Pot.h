#ifndef __POT__
#define __POT__

class Pot {
 
public: 
  Pot(int pin);
  
  float getValue();

private:
  int pin;

};

#endif
