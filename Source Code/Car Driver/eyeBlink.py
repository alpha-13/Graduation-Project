#!/usr/bin/env python

# Created by Mohamed Elsayed

import time, thread
import dlib
import cv2

from RPi import GPIO
from scipy.spatial import distance as dist
from imutils import face_utils


PIN = 32

def eye_aspect_ratio(eye):                                              # Calculate Eye Aspect Ratio
    A = dist.euclidean(eye[1], eye[5])
    B = dist.euclidean(eye[2], eye[4])
    
    C = dist.euclidean(eye[0], eye[3])

    ear = (A + B) / (2.0 * C)

    return ear

def Alarm(repeat, delay):                                               # Alarm user when face not detected or eye closed
        GPIO.setmode(GPIO.BOARD)
        GPIO.setwarnings(False)
        GPIO.setup(PIN, GPIO.OUT)

        try:
            
                for _ in xrange(repeat):
                    for value in [True, False]:
                        GPIO.output(PIN, value)
                        time.sleep(delay)
        except:
            pass


faceCounter = 0
faceThreshold = 5

eyeThreshold = 0.23
closedFrames = 5

COUNTERl = 0
COUNTERr = 0
TOTAL = 0

scaleSize = 4

print("Loading landmark predictor...")
detector = dlib.get_frontal_face_detector()
predictor = dlib.shape_predictor('shape_predictor_68_face_landmarks.dat')

(lStart, lEnd) = face_utils.FACIAL_LANDMARKS_IDXS["left_eye"]                                   # Load left eye landmarks points (From -> To)
(rStart, rEnd) = face_utils.FACIAL_LANDMARKS_IDXS["right_eye"]                                  # Load right eye landmarks points (From -> To)


camera = cv2.VideoCapture(0)
camera.release()
camera = cv2.VideoCapture(0)
classifier = cv2.CascadeClassifier('haarcascade_frontalface_default.xml')

while True:
    _, frame = camera.read()
    
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)

    mini = cv2.resize(gray, (gray.shape[1] / scaleSize, gray.shape[0] / scaleSize))             # Resize image for fast detection

    rects = classifier.detectMultiScale(mini)                                                   # Detect face in image

    if(len(rects) < 1):
        #frame = cv2.flip(frame, 1, 0)
        if(faceCounter >= faceThreshold):
            print("Face !!!")
           # thread.start_new_thread(Alarm, (2, 0.5))
            faceCounter = 0
        else:
            faceCounter += 1
    
    for rect in rects:

        x = rect[0]
        y = rect[1]
        w = rect[2]
        h = rect[3]
        
        rect = dlib.rectangle(x*scaleSize, y*scaleSize, (x+w)*scaleSize, (y+h)*scaleSize)

        shape = predictor(gray, rect)
        shape = face_utils.shape_to_np(shape)

        leftEye = shape[lStart:lEnd]                                                            # Left eye landmarks
        rightEye = shape[rStart:rEnd]                                                           # Right eye landmarks
        leftEAR = eye_aspect_ratio(leftEye)
        rightEAR = eye_aspect_ratio(rightEye)

        leftEyeHull = cv2.convexHull(leftEye)
        rightEyeHull = cv2.convexHull(rightEye)
        cv2.drawContours(frame, [leftEyeHull], -1, (0, 255, 0), 1)
        cv2.drawContours(frame, [rightEyeHull], -1, (0, 255, 0), 1)

        #frame = cv2.flip(frame, 1, 0)

        Alrm = False

        # Check Left Eye
        
        if(leftEAR < eyeThreshold + 0.01):
            COUNTERl += 1
        else:
            COUNTERl = 0

        if COUNTERl >= closedFrames:
            TOTAL += 1
            #thread.start_new_thread(Alarm, (6, 0.05))
            print("Left Closed")
            Alrm = True
            COUNTERl = 0

        # Check Right Eye
        
        if(rightEAR < eyeThreshold):
            COUNTERr += 1
        else:
            COUNTERr = 0

        if COUNTERr >= closedFrames:
            TOTAL += 1
           # if(not Alrm):
             #   thread.start_new_thread(Alarm, (6, 0.05))
            print("Right Closed")
            COUNTERr = 0
        
        cv2.putText(frame, "Blinks: {}".format(TOTAL), (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
        
        cv2.putText(frame, "L: {:.2f}  R: {:.2f}".format(leftEAR, rightEAR), (250, 30), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2)
    
    cv2.imshow("Eye Blink", frame)

    if (cv2.waitKey(5) & 0xFF == 27):
        break

cv2.destroyAllWindows()
camera.release()
GPIO.cleanup()
