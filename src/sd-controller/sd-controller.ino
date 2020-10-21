/*Authors: Matteo Ragazzini, Marta Spadoni*/
#include "SmartController.h"
#include "Timer.h"

SmartController* controller = new SmartController();
Timer timer;
void setup() { 
  Serial.begin(9600);
  while (!Serial){}
  Serial.println("ready to go."); 
  timer.setupPeriod(500);
}

void loop() {
  timer.waitForNextTick();
  controller->tick();
}
