/*Authors: Matteo Ragazzini, Marta Spadoni*/
#include "Dumpster.h"

#include <ESP8266HTTPClient.h>
#include <ESP8266WiFi.h>

/* wifi network name */
char* ssidName = "Matteo";
/* WPA2 PSK password */
char* pwd = "ciaociao";
/* service IP address */ 
String address = "http://68c04524.ngrok.io";

Dumpster* dumpster = new Dumpster(address);

void setup() { 
  Serial.begin(115200);                                
  WiFi.begin(ssidName, pwd);
  Serial.print("Connecting...");
  while (WiFi.status() != WL_CONNECTED) {  
    delay(500);
    Serial.print(".");
  } 
  Serial.println("Connected");
  
}

   
void loop() { 
   if (WiFi.status()== WL_CONNECTED){   

      dumpster->manageState(); 
      
   } else { 
     Serial.println("Error in WiFi connection");   
   } 
   delay(2000);
}
