#include "SmartController.h"

SmartController::SmartController(){
  servo = new ServoMotorImpl(SERVO_PIN);
  state = WAITING;
  timer = 0;
  leds[0] = new Led(LA_PIN );
  leds[1] = new Led(LB_PIN );
  leds[2] = new Led(LC_PIN );
  msgService = new MsgServiceBT(TXD_PIN,RXD_PIN);
  msgService->init(); 
}


void SmartController::tick(){
  switch(state){
    case OPENING :
        Serial.println("opening");
        leds[typeOfJunk]->switchOn();
        servo->on();
        servo->setPosition(180);
        state = IN_DEPOSIT;
        break;
        
    case IN_DEPOSIT :
        Serial.println("deposit");
        timer++;
        if(timer >= TIME_DELIVER) state = CLOSING;
        if(requestOfTime())timer = 0;
        break;
  
    case CLOSING:
        Serial.println("closing");
        timer = 0;
        leds[typeOfJunk]->switchOff();
        servo->setPosition(0);
        // notify the app the end of deposit
        msgService->sendMsg(Msg("ok"));
        state = WAITING;
        break;
  
    case WAITING :
        servo->off();
        Serial.println("waiting");
        int d = requestOfDeposit();
        if(d>=0){
           typeOfJunk = d;
           state = OPENING;
        }
        break;
  }
}

bool SmartController::requestOfTime(){
   if (msgService->isMsgAvailable()) {
       Msg* msg = msgService->receiveMsg();
        String c = msg->getContent();
        return c=="TIME";
   }
   return false;
}

//returns the index of the led to switch on or -1.
int SmartController::requestOfDeposit(){
    if (msgService->isMsgAvailable()) {
        Msg* msg = msgService->receiveMsg();
        String c = msg->getContent();
        return c.toInt();
    }
    return -1;       
} 
