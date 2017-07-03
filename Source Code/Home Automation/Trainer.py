# -*- coding: utf-8 -*-
"""
Created on Sat Feb 04 01:47:51 2017

@author: Mohamed Elsayed
"""

import cv2, os, DataBase, time

class Train:
    
    def Training(self, Name):
        self.Name = Name
        try:
            scaleSize = 4
            cascade = 'haarcascade_frontalface_default.xml'
            trainningDir = 'Trainning Data'
            
            DB = DataBase.DB()

            
###########################################################            
            
            
            ID = 1 #DB.InsertInDB(Name)           

            print('Done')
#########################################################

            print("ID" + str(ID))
            path = os.path.join(trainningDir, str(ID))
            if not os.path.isdir(path):
                os.mkdir(path)
            (im_width, im_height) = (200, 200)
            classifier = cv2.CascadeClassifier(cascade)
            webcam = cv2.VideoCapture(0)
            
            count = 0
            while count < 2:
                (_, im) = webcam.read()
                im = cv2.flip(im, 1, 0)
                gray = cv2.cvtColor(im, cv2.COLOR_BGR2GRAY)
                mini = cv2.resize(gray, (gray.shape[1] / scaleSize, gray.shape[0] / scaleSize))
                
                faces = classifier.detectMultiScale(mini)
                
                if(len(faces) > 0):
                    (x, y, w, h) = [v * scaleSize for v in faces[0]]
                    face = gray[y:y + h, x:x + w]
                    face_resize = cv2.resize(face, (im_width, im_height))
                    pin=sorted([int(n[:n.find('.')]) for n in os.listdir(path) if n[0]!='.' ]+[0])[-1] + 1
                    cv2.imwrite('%s/%s.png' % (path, pin), face_resize)
                    
                    cv2.rectangle(im, (x, y), (x + w, y + h), (0, 255, 0), 3)
                    cv2.putText(im, Name, (x - 10, y - 10), cv2.FONT_HERSHEY_PLAIN, 1,(0, 255, 0))
                    count += 1
                cv2.imshow('Train', im)
                time.sleep(2)
                key = cv2.waitKey(5)
                if key == 27:
                    break
            
            webcam.release()
            cv2.destroyAllWindows()
            
        except Exception:
            print("Exception")
            webcam.release()
            cv2.destroyAllWindows()

Tr = Train()
name = 'Alaa Essam'#raw_input ('Please Enter Your Name: ')
Tr.Training(name);
