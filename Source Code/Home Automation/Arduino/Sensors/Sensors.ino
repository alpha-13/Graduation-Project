#include<stdlib.h>
#include <dht.h>

const int LDR1 = A0, LM = A1, LDR2 = A2;
int LDR1Value = 0, LDR2Value = 0;
float Temperature = 0.0;

#define DHTPIN 2
#define DHTTYPE DHT11

dht DHT;

void setup(){
  pinMode(LDR1, INPUT);
  pinMode(LDR2, INPUT);
  pinMode(LM, INPUT);
  
  Serial.begin(9600);
}

void loop(){
  LDR1Value = ReadLDR(LDR1);
  LDR2Value = ReadLDR(LDR2);
  Temperature = (ReadTemp(LM) * 500) /1024;
  DHT.read11(DHTPIN);
  String DHTVal = "";
  DHTVal += ":Hum:"  + floatToString(DHT.humidity);
  DHTVal += ":Temp:" + floatToString(DHT.temperature);
  
  String Temp = floatToString(Temperature);
  
  SendData( "LM:" + String(Temp) + ":LDR:" + String(LDR1Value/10) + String(DHTVal) + ":Light:" + String(LDR2Value/10));
  delay(1000);
}

int ReadLDR(int Pin){
  return analogRead(Pin);
}

float ReadTemp(int Pin){
  return analogRead(Pin);
}


void SendData(String Data){
  Serial.println(Data);
}

String floatToString(float Num){
  char buff[10];
  
  dtostrf(Num, 4, 4, buff);
 
 String Val = "";
 for(int i=0; i<sizeof(buff); i++){
   Val +=buff[i];
 }
 
 return Val.substring(0,5);
}
