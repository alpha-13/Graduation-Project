# -*- coding: utf-8 -*-
"""
Created on Sat Feb 04 00:37:00 2017

@author: Mohamed Elsayed
"""

import cv2, numpy, os, datetime, DataBase, thread, time, RPi.GPIO as GPIO
from random import shuffle


GPIO.setmode(GPIO.BOARD)
GPIO.setwarnings(False)

GREENPIN = 19
REDPIN = 21

scale_size = 4
(im_width, im_height) = (200, 200)
(images, labels, names, id) = ([], [], {}, 0)
(valImgs, valLabels) = ([], [])
validationImages = []
NClasses = 0
valPer = 0.2 # Validation Percentage
    
Images_dir = 'Trainning Data'
Unauthorized_dir = 'users'

classifier = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')

model = cv2.createFisherFaceRecognizer()

# Training Data
def train():
    global images, labels, names, id, model
    global valImgs, valLabels
    global NClasses
    print("Please wait for trainning data!")


    
    for (_, dirs, _) in os.walk(Images_dir):
        for subdir in dirs:
            names[id] = subdir
            subjectpath = os.path.join(Images_dir, subdir)
            length = len(os.listdir(subjectpath))
            valSize = int(float(len(os.listdir(subjectpath)))*valPer) # Valudation size = length * percentage
            
            #print("Length: " + str(length) + " Validation Size: " + str(valSize))
            i = 0
            paths = os.listdir(subjectpath)
            shuffle(paths)
            
            for filename in paths:
                path = subjectpath + '/' + filename
                label = id
                if(i < length - valSize):
                    images.append(cv2.imread(path, 0))
                    labels.append(int(label))
                else:
                    valImgs.append(cv2.imread(path, 0))
                    valLabels.append(int(label))
                i+= 1
            #print("Val: " + str(len(valImgs)) + " " + str(len(valLabels)))
            #print(valLabels)
            id += 1
            NClasses += 1
    #print(len(images))

    (images, labels) = [numpy.array(lis) for lis in [images, labels]]
    (valImgs, valLabels) = [numpy.array(lis) for lis in [valImgs, valLabels]]


    #model.train(images, labels)


train()
#model.save('me.xml')

model.load('me.xml')

total = 0.0
TClass = 0.0
lastID = 0
nImages = 0

print(valLabels)
for i in range(len(valImgs)):
    prediction = model.predict(valImgs[i])
    if(lastID != valLabels[i]):
        lastID = valLabels[i]
        print("Class Threshold " + str(valLabels[i-1]) + " : " + str(TClass))
        totalThresholdFotThisImgs = (TClass / nImages)
        print("T for these images: " + str(TClass / nImages))
        totalThresholdFotRemaining = ((1 - valPer) / valPer) * (TClass / nImages)

        print("T for each images class: " + str(totalThresholdFotRemaining))
        total += (totalThresholdFotThisImgs + totalThresholdFotRemaining)
        print("Total: " + str(total))
        TClass = 0.0
        nImages = 0
    else:
        TClass += prediction[1]                                                 # Calculate total distance for each class
        nImages += 1
    #print("Valid: " + str(valLabels[i]) + " Test: " + str(prediction))
    lastID = valLabels[i]
    
    #total+=prediction[1]                                                        # Calculate total distance for all classes


#print(total / float(len(valImgs)))

threshold = total #/ float(len(valImgs))
print("Threshold: " + str(threshold))
print("NClasses:" + str(NClasses))

#threshold *= NClasses

print("Threshold * NClasses: " + str(threshold))


TrustThreshold = 10
UnTrustThreshold = 15

webcam = cv2.VideoCapture(0)
webcam.release()
webcam = cv2.VideoCapture(0)

DB = DataBase.DB()

Clients = DB.GetAllClients()

Trusted = 0
UnTrusted = 0

timeNow = 0
timeAfter = datetime.datetime.now()                                                 # Setting threashold for unauthorized users


def LED(PIN):
        GPIO.setup(PIN, GPIO.OUT)
        GPIO.output(PIN, GPIO.HIGH)
        for _ in range(2):
                GPIO.output(PIN, GPIO.HIGH)
                time.sleep(1)
                GPIO.output(PIN, GPIO.LOW)
                time.sleep(1)
                
while True:
    _, Img = webcam.read()
    Img = cv2.flip(Img, 1, 0)
    
    gray = cv2.cvtColor(Img, cv2.COLOR_BGR2GRAY)
    
    mini = cv2.resize(gray, (gray.shape[1] / scale_size, gray.shape[0] / scale_size))    
    
    faces = classifier.detectMultiScale(mini)

    
    #for i in range(len(faces)):
    i = 0
    if(len(faces) > 0):                                                             # Just one face !
        (x, y, w, h) = [v * scale_size for v in faces[i]]

        face = gray[y:y + h, x:x + w]                                               # Get face  
        face_resize = cv2.resize(face, (im_width, im_height))
        
        #colorface = Img[y:y + h, x:x + w]        
        #colorface = cv2.resize(colorface, (im_width, im_height))
        
        prediction = model.predict(face_resize)
        print(str(prediction))

        
        if prediction[1] < threshold:
            predectedID = names[prediction[0]]
            
            cv2.rectangle(Img, (x, y), (x + w, y + h), (0,255,0), thickness=2)
            for C in Clients:
                if(int(C[0]) == int(predectedID)):
                    Name = C[1]
                    break
            Trusted += 1
            cv2.putText(Img, '%s [ %.0f ]' % (Name ,prediction[1]), (x-10, y-10), cv2.FONT_HERSHEY_PLAIN,1,(0, 255, 0))
            print(Name)
            if(Trusted > TrustThreshold):
                print("Open The Door!")
                thread.start_new_thread(LED, (GREENPIN, ))
                Trusted = 0
            UnTrusted = 0
        else:
            Trusted = 0
            UnTrusted += 1
            cv2.rectangle(Img, (x, y), (x + w, y + h), (0,0,255), thickness=2)
            cv2.putText(Img,'Unknown', (x-10, y-10), cv2.FONT_HERSHEY_PLAIN,1,(0, 0, 255))
            
            if(datetime.datetime.now() > timeAfter and UnTrusted > UnTrustThreshold):
                DB = DataBase.UnAuthorizedDB()
                
                DB.AddPerson(Img, face_resize)                
                
                print("Untrusted User!")
                
                thread.start_new_thread(LED, (REDPIN, ))
                UnTrusted = 0
                timeNow = datetime.datetime.now()
                timeAfter = timeNow + datetime.timedelta(seconds = 10)
                
            
    cv2.imshow("Video", Img)
    
    if(cv2.waitKey(5) & 0xFF == 27):
        break
    
webcam.release()
cv2.destroyAllWindows()

GPIO.cleanup()
