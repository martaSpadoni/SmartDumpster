#ifndef __SMARTCONTROLLER__
#define __SMARTCONTROLLER__

#include "servo_motor_impl.h"
#include "Led.h"
#include "MsgServiceBT.h"
#include "SoftwareSerial.h"
#include "Config.h"

class SmartController {

public : 

  SmartController();
  void tick();

private :

  enum { OPENING, IN_DEPOSIT, CLOSING, WAITING } state;

  bool requestOfTime();
  int  requestOfDeposit();

  MsgServiceBT *msgService;
  Led* leds[3];
  ServoMotorImpl*servo;
  int timer;
  int typeOfJunk; //represents the 3 possible type and leds
  
 
};

#endif
