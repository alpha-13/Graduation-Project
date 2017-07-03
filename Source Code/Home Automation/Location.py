#!/usr/bin/env python
# -*- coding: utf-8 -*-

# Created by Mohamed Elsayed

import requests, json, urllib2, RPi.GPIO as GPIO

from time import sleep
from math import sin, cos, sqrt, atan2, radians

GPIO.setmode(GPIO.BOARD)
GPIO.setwarnings(False)

Motor1A = 31
Motor1B = 33
Motor1E = 35


print "Start Location Module"
        
def Garage(Value):
    GPIO.setup(Motor1A, GPIO.OUT)
    GPIO.setup(Motor1B, GPIO.OUT)
    GPIO.setup(Motor1E, GPIO.OUT)

    print(Value)
    if(Value):
        GPIO.output(Motor1A, GPIO.HIGH)
        GPIO.output(Motor1B, GPIO.LOW)
    else:
        GPIO.output(Motor1A, GPIO.LOW)
        GPIO.output(Motor1B, GPIO.HIGH)
        
    GPIO.output(Motor1E, GPIO.HIGH)
    sleep(0.2)

    GPIO.output(Motor1E, GPIO.LOW)
    

def main():
    try:
        response = requests.get('https://maps.googleapis.com/maps/api/geocode/json?address=Supply+Bureau+-+Mahlat+El+Qasab,+كفر+الشيخ,+وسط+البلد،+At+Tarabeyah,+Kafr+El-Shaikh,+Kafr+El+Sheikh+Governorate')

        RespJson = response.json()
        Homelat = RespJson['results'][0]['geometry']['location']['lat']
        Homelon = RespJson['results'][0]['geometry']['location']['lng']

        #print("Home Lat: " + str(Homelat) + "\nHome Lon: " + str(Homelon))
        

        READ_API_KEY='5WBREV15CZX3PGHB'
        CHANNEL_ID=267930

        LastRead = 0
        CurrentRead = 0

        ValueBetween = 0

        IsOpen = False

        while True:
            try:
                conn = urllib2.urlopen("http://api.thingspeak.com/channels/%s/feeds/last.json?api_key=%s" % (CHANNEL_ID,READ_API_KEY))

                response = conn.read()
                data=json.loads(response)
                CurrentLat = data['field1']
                CurrentLon = data['field2']
                conn.close()

                #print("\nCar Lat: " + str(CurrentLat) + "\nCar Lon: " + str(CurrentLon))


                # approximate radius of earth in km
                R = 6373.0

                lat1 = radians(float(Homelat))
                lon1 = radians(float(Homelon))
                lat2 = radians(float(CurrentLat))
                lon2 = radians(float(CurrentLon))

                dlon = lon2 - lon1
                dlat = lat2 - lat1

                a = sin(dlat / 2)**2 + cos(lat1) * cos(lat2) * sin(dlon / 2)**2
                c = 2 * atan2(sqrt(a), sqrt(1 - a))

                distance = R * c

                LastRead = CurrentRead
                CurrentRead = distance*100

                print("Curr: " + str((CurrentRead - LastRead)))
                ValueBetween += (CurrentRead - LastRead)

                print("Value Between: " + str(ValueBetween))

                print("\nResult: " + str(distance * 100) + " m\n")

                
                if(distance <= 100 and ValueBetween < 0.0 and IsOpen == False):
                    print("Open Garage")
                    Garage(True)
                    IsOpen = True
                elif(distance <= 100 and ValueBetween > 20 and IsOpen == True):
                    print("Close Garage")
                    Garage(False)
                    IsOpen = False
                else:
                    print("Unknown")


                """
                if(IsOpen == False):
                    Garage(False)
                    IsOpen = True
                else:
                    Garage(True)
                    IsOpen = False
                """
                print('\n')
                sleep(5)
            except:
                pass

        GPIO.cleanup()
    except:
        print("No Internet Connection")

main()
