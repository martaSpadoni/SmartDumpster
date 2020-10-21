#ifndef __DUMPSTER__
#define __DUMPSTER__

#include "config.h"
#include "Led.h"
#include "Pot.h"
#include "Arduino.h"
#include "SoftwareSerial.h"

class Dumpster{
  
  public:
  
      Dumpster(String address);
      void manageState();

      
  private:
  
      void handleAvailable();
      void handleNotAvailable();
      void handleDeposit();

      Led* greenLed;
      Led* redLed;
      Pot* pot;
      String address;
      String state;
      float lastWeight;
      bool depositFlag;
      bool connectionEstablished;
      int nDeposit;
        
};

#endif;
