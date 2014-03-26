
#include "SoftwareSerial.h"

int ENA = 2;
int IN1 = 3;
int IN2 = 4;
int ENB = 5;
int IN3 = 6;
int IN4 = 7;

int blue_flag = 0;

// bluetooth
SoftwareSerial blue( 10 , 11 ); //pin5 Rx , pin4 Tx
int pot=0;

void setup(){
  Serial.begin( 9600 );
  // bluetooth
  blue.begin( 9600 );
  blue.println( "Conectado" );
  pinMode(9,OUTPUT);
  pinMode(13,OUTPUT);
  pinMode(ENA,OUTPUT);
  pinMode(ENB,OUTPUT);
  pinMode(IN1,OUTPUT);
  pinMode(IN2,OUTPUT);
  pinMode(IN3,OUTPUT);
  pinMode(IN4,OUTPUT);
  digitalWrite(ENA,HIGH);//enablae motorA
  digitalWrite(ENB,HIGH);//enable motorB
}

void movimiento( String resultado ){
  
  if( resultado == "61" ){
    //acel
    digitalWrite( 9 , 1);
    acel();
  }
  if( resultado == "62" ){
    //atras
    digitalWrite( 9 , 1);
    retroceso();
  }
  if( resultado == "63" ){
    //atras
    digitalWrite( 9 , 1);
    izquierda();
  }
  if( resultado == "64" ){
    //atras
    digitalWrite( 9 , 1);
    derecha();
  }
  if( resultado == "65" ){
    //stop
    digitalWrite( IN1 , 0);
    digitalWrite( IN2 , 0);
    digitalWrite( IN3 , 0);
    digitalWrite( IN4 , 0);
    digitalWrite( 9 , 0);
  }
}

void acel(){
  digitalWrite( IN1, HIGH );
  digitalWrite( IN2, LOW );
  digitalWrite( IN3, HIGH );
  digitalWrite( IN4, LOW );
  delay(10);
}

void retroceso(){
  digitalWrite(IN1,LOW);
  digitalWrite(IN2,HIGH);
  digitalWrite(IN3,LOW);
  digitalWrite(IN4,HIGH);
  delay(10);
}


void izquierda(){
  digitalWrite(IN1,HIGH);
  digitalWrite(IN2,LOW);
  digitalWrite(IN3,LOW);
  digitalWrite(IN4,HIGH);
  delay(10);  
}

void derecha(){ 
  digitalWrite(IN1,LOW);
  digitalWrite(IN2,HIGH);
  digitalWrite(IN3,HIGH);
  digitalWrite(IN4,LOW);
  delay(10);  
}

void loop() {
  //digitalWrite( 9 , 1);
  String codigo_blue = "";
  while( blue.available() ) {
    byte C = blue.read();
    codigo_blue += String( C , HEX );    
    blue_flag = 1;
  }

  if( blue_flag > 0 ) {
    movimiento( codigo_blue );
    Serial.println( codigo_blue );
    blue_flag = 0;
  }
  
}

