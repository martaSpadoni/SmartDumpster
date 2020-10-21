#include "Dumpster.h"

HTTPClient http;

Dumpster::Dumpster(String address){
  this->address = address;
  greenLed = new Led(LED_GREEN);
  greenLed->switchOn();
  redLed = new Led(LED_RED);
  pot = new Pot(POT);
  lastWeight = 0;
  nDeposit = 0;
  state = "AVAILABLE";
  connectionEstablished = false;
}
int sendData(String address, float value, float totalWeight, float availability, int nDeposit){ 
   http.addHeader("Content-Type", "application/json");  
   String msg = 
    String("{ \"value\": ") + String(value) + 
    ", \"totalWeight\": " + String(totalWeight) +
    ", \"availability\": " + String(availability) +
    ", \"nDeposit\": " + String(nDeposit) +" }";  
   int retCode = http.POST(msg);             
   return retCode;
}

void Dumpster::manageState(){
   int retCode; 
   String payload;
   int retBeg;
   if(!connectionEstablished){
    retBeg = http.begin(address + "/api/esp");
    connectionEstablished = true;
   }    
   retCode = http.GET();   
   payload = http.getString();  
   Serial.println("payload = " + payload + " get:" + retCode);
   if(payload != state){
      state = payload;
     if(state == "DEPOSIT"){
        handleDeposit();
      }else if(state == "AVAILABLE"){
        handleAvailable();
      }else{
        handleNotAvailable(); //also in case of error when payload is empty
      }
   }
   
}

void Dumpster::handleAvailable(){
  greenLed->switchOn();
  redLed->switchOff();
}


void Dumpster::handleNotAvailable(){
  redLed->switchOn();
  greenLed->switchOff();
  lastWeight =0;
  nDeposit =0;
}

void Dumpster::handleDeposit(){
    int code;
    nDeposit++;
    float value = pot->getValue();
    if(value> MAX_WEIGHT)value = MAX_WEIGHT;
    code = sendData(address, value-lastWeight, value, MAX_WEIGHT-value, nDeposit);
    Serial.println("sent "+String(value-lastWeight)+ "... code:" + code ); 
    lastWeight = value;
}
