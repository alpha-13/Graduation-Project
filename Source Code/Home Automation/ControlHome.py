#!/usr/bin/env python

# Created by Mohamed Elsayed

import time, thread, serial, RPi.GPIO as GPIO

from datetime import datetime, timedelta

# Arduino
serro = serial.Serial('/dev/ttyACM0', 9600)
DataFromArduino = ""

# Bed Room LED
BRPin = 8
BRPercentage = 0

# Livong Room LED
LRPin = 10
LRPercentage = 0

# Windows Queue State
WindowsQueue = []

# Bed Room Window
BRWindowA = 16
BRWindowB = 18
BRWindowE = 22
BRWindowOpen = False # check if window open or not
BRWindowEnable = True # If window currently in use

# Kitchen Fan
KFanA = 11
KFanB = 13
KFanE = 15
KUserEnable = False # if user enable fan
KFanEnable = False # if fan is running now
KFanForceClose = False # Force close kitchen fan

# Kitchen Temp
TimeNow = datetime.now()
KTemp = 0 # Kitchen temperature
Sure = 0 # Make sure that temperature is high
MinTemp = 37 # Minimun temperature that can make fan run

# Living Air Condition
LRAirA = 36
LRAirB = 38
LRAirE = 40
LRState = False # Check if living room air condition run or not
LRAirPercentage = 0 # Percentage of speed


# Alarm
buzz = 7
cafePin = 37
(D, H, M, C, W) = (0, 0, 0, 0, 0)
alarmSet = False # check if alarm is enabled or not
timeNowForAlarm = datetime.now()
cafeStart = 1 # Start Cafe Machine before alarm (1 minute)

# oudoor LED
outdoorPin = 24
lightValue = 100

# Init
GPIO.setwarnings(False)
GPIO.setmode(GPIO.BOARD)

GPIO.setup(BRPin, GPIO.OUT)
GPIO.setup(LRPin, GPIO.OUT)

GPIO.setup(BRWindowA, GPIO.OUT)
GPIO.setup(BRWindowB, GPIO.OUT)
GPIO.setup(BRWindowE, GPIO.OUT)

GPIO.setup(KFanA, GPIO.OUT)
GPIO.setup(KFanB, GPIO.OUT)
GPIO.setup(KFanE, GPIO.OUT)

GPIO.setup(LRAirA, GPIO.OUT)
GPIO.setup(LRAirB, GPIO.OUT)
GPIO.setup(LRAirE, GPIO.OUT)

GPIO.setup(buzz, GPIO.OUT)
GPIO.setup(outdoorPin, GPIO.OUT)


# Bed Room Light with Percentage
def BRPWM(PinNumber, Percentage):
    p = GPIO.PWM(PinNumber, 100)
    p.start(0)
	
    try:
        while(True):
            p.ChangeDutyCycle(BRPercentage)
            time.sleep(.001)
    except Exception:
        pass

    p.stop()


# Living Room Light with Percentage
def LRPWM(PinNumber, Percentage):
    p = GPIO.PWM(PinNumber, 100)
    p.start(0)
	
    try:
        while(True):
            p.ChangeDutyCycle(LRPercentage)
            time.sleep(.001)
    except Exception:
        pass

    p.stop()


# Open/ Close Bed Room Window
def Window(MA, MB, MC, State):
	p = GPIO.PWM(MC, 50)
	p.start(0)

	p.ChangeDutyCycle(0)
	time.sleep(1)
	if(State == 1):
		GPIO.output(MA, GPIO.HIGH)
		GPIO.output(MB, GPIO.LOW)
	else:
		GPIO.output(MA, GPIO.LOW)
		GPIO.output(MB, GPIO.HIGH)

	p.ChangeDutyCycle(20)
	time.sleep(1)
	p.stop()


# Open/ Close Fan by user or if temperature high
def Fan(PinA, PinB, PinE):
	p = GPIO.PWM(PinE, 50)
	p.start(0)

	GPIO.output(PinA, GPIO.HIGH)
	GPIO.output(PinB, GPIO.LOW)
	global KFanEnable
	global KFanForceClose
	global TimeNow
	global KUserEnable

	while True:
            if(KTemp > MinTemp and KFanForceClose == False and Sure >= 3):
                KFanEnable = True
                p.ChangeDutyCycle(KTemp - 10)
            else:
                if KUserEnable:
                    p.ChangeDutyCycle(15)
                else:
                    p.ChangeDutyCycle(0)

                if(KTemp > MinTemp and datetime.now() > (TimeNow + timedelta(minutes = 1))  and KFanForceClose == True):
                    KFanForceClose = False

            time.sleep(0.5)

        p.stop()


# Open/ Close Living Room Air Condition 
def AirCondition(AirA, AirB, AirE):
	p = GPIO.PWM(AirE, 50)
	p.start(0)
	GPIO.output(AirA, GPIO.HIGH)
	GPIO.output(AirB, GPIO.LOW)

	while True:
            p.ChangeDutyCycle(LRAirPercentage)                
            time.sleep(0.5)

	p.stop()


# Receive Data From Arduino
def ReceiveDataFromArduino():
    while True:
        global DataFromArduino
        DataFromArduino = serro.readline()[:-2]
        print DataFromArduino
        if("LM" in DataFromArduino):
            try:
                global KTemp
                global Sure
                global KFanEnable
                global KFanForceClose
                
                KTemp = int(float(DataFromArduino.split(":")[1]))
                if(KTemp > MinTemp):
                    Sure +=1
                else:
                    Sure = 0
                    if KFanEnable:
                        KFanEnable = False
            except:
                pass
        if("LDR" in DataFromArduino):
            try:
                global lightValue
                value = int(DataFromArduino.split(":")[3])
                lightValue = value
                #print("Light:: " + str(lightValue))
            except:
                pass


# Start Alarm
def startBuzzer():
    Buzzer_Repetitions = 30
    Buzzer_Delay = 0.06
    Pause_Time = 0.3

    try:
        for _ in xrange(Buzzer_Repetitions):
            for value in [True, False]:
                GPIO.output(buzz, value)
                time.sleep(Buzzer_Delay)
    except:
        pass
    GPIO.output(buzz, GPIO.LOW)


# Start Cafe machine before alarm
def startCafe():
    GPIO.output(buzz, True)
    time.sleep(0.05)
    GPIO.output(buzz, False)
    
    GPIO.setup(cafePin, GPIO.OUT)
    GPIO.output(cafePin, GPIO.HIGH)
    time.sleep(20)
    GPIO.output(cafePin, GPIO.LOW)


# Set Alarm, cafe machine and Bed Room Window
def setAlarm():
    global alarmSet
    global cafeStart
    global C, W
    
    Deff = 0
    if(int(C) == 1):
        Deff = int(M) - int(cafeStart) # Minutes between Actual minutes and Start cafe minutes
        
    while True:
        if(int(C) == 1 and datetime.now() > timeNowForAlarm + timedelta(days = int(D), hours = int(H), minutes = int(Deff))): # Start Cafe Machine before Alarm
            print "Start Cafe"
            startCafe()
            C = 0
        elif(datetime.now() > (timeNowForAlarm + timedelta(days = int(D), hours = int(H), minutes = int(M)))): # Start Alram and Window
            print "Start Alarm"
            startBuzzer()
            if(int(W) == 1):
                print("Open Window")
                global BRWindowEnable, State, BRWindowOpen
                State = 1
                if(BRWindowEnable and (State != BRWindowOpen)):
                    BRWindowEnable = False
                    Window(BRWindowA, BRWindowB, BRWindowE, State)
                    BRWindowEnable = True
                    BRWindowOpen = State
            break
            
        time.sleep(1)

    alarmSet = False

def AdjustLight():
    global lightValue

    p = GPIO.PWM(outdoorPin, 100)
    p.start(0)
    
    while(True):
        if(lightValue > 75):
            p.ChangeDutyCycle(0)
        elif(lightValue < 10):
            p.ChangeDutyCycle(100)
        else:
            p.ChangeDutyCycle((100 - lightValue))

        time.sleep(5)
    
        
	    
thread.start_new_thread(BRPWM, (BRPin, BRPercentage))               # Start Bed Room Light Thread
thread.start_new_thread(LRPWM, (LRPin, LRPercentage))               # Start Living Room Light Thread

thread.start_new_thread(ReceiveDataFromArduino, ())                 # Thread for Receiving Data From Arduino
thread.start_new_thread(Fan, (KFanA, KFanB, KFanE))                 # Start Kitchen Fan Thread
thread.start_new_thread(AirCondition, (LRAirA, LRAirB, LRAirE))     # Start Air Condition Thread
thread.start_new_thread(AdjustLight, ())                            # Adjust outside door light

def main(msg):
	if('LED' in msg):
		LED, Per = msg.split(':')

		global BRPercentage
		global LRPercentage

		try:
                    Per = int(Per)
                    
                    if('BR' in LED):                                # Bed Room LED
                        LED = BRPin
                        BRPercentage = Per
                    elif('LR' in LED):                              # Living Room LED
                        LED = LRPin
                        LRPercentage = Per
                    
		except:
			pass
	elif('Win' in msg):
		Win, State = msg.split(':')
		#print (Win, State)
		if('BR' in Win):                                    # Bed Room Window
			print(State)
			global BRWindowOpen
                        global BRWindowEnable
                        
                        
			try:
				if('Open' in State):
					State = 1
				else:
					State = 0
			
				if(BRWindowEnable and (State != BRWindowOpen)):
                                        BRWindowEnable = False
					Window(BRWindowA, BRWindowB, BRWindowE, State)
					BRWindowEnable = True
					BRWindowOpen = State
					
			except:
				pass
	elif('Fan' in msg):                                             # Kitchen Fan
            Fan, State = msg.split(':')
            print State
            global KFanEnable
            global KFanForceClose
            global KUserEnable

            try:
                if('Open' in State):
                    if KFanEnable:
                        KFanForceClose = False
                    
                    KUserEnable = True    
                    KFanEnable = True
                else:
                    KUserEnable = False
                    KFanEnable = False
                    global KTemp
                    global MinTemp
                    if(KTemp > MinTemp):
                        global TimeNow
                        TimeNow = datetime.now()
                        KFanForceClose = True
            except:
                pass
            
        elif('Air' in msg):                                                 # Air Conditional
            try:
                _, Perc = msg.split(':')

                global LRAirPercentage

                LRAirPercentage = int(Perc)
            except:
                pass
        elif('Alarm' in msg):                                               # Alarm
            try:
                global D, H, M, C, W
                global alarmSet
                global timeNowForAlarm
                _, D, _, H, _, M, _, C, _, W = msg.split(':')
                timeNowForAlarm = datetime.now()
                
                if(alarmSet == False):
                    print ("Set Alarm")
                    thread.start_new_thread(setAlarm, ())
                    alarmSet = True
            except:
                pass
