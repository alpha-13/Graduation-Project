#!/usr/bin/env python

# Created by Mohamed Elsayed

import socket, os, time, json, thread, RPi.GPIO as GPIO
import ControlHome, alsaaudio

from pygame import mixer

port = 9000

trainningDir = 'Trainning Data'
oldPath = "Sended Images"
Music_Dir = "/home/pi/Desktop/Music"

volume = alsaaudio.Mixer('PCM')

MusicPaths = []
PlayIndex = -1


# Setup Server to accept any IP address
def setupServer():
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    print("Socket created !")
    try:
        s.bind(('', port))
        print("Socket bind complete !")
    except socket.error as msg:
        print(msg)
        
    return s

# Setup Connection and accept connection
def setupConnection():
    s.listen(10)
    conn, address = s.accept()
    # print("Connected to IP: " + address[0] + "  and  Socket: " + str(address[1]))
    return conn

# Move Image from directory to another
def moveImage(NewPath, UnUserID):
    newPicName = sorted([int(n[:n.find('.')]) for n in os.listdir(NewPath)]+[0])[-1] + 1

    for f in os.listdir(oldPath):                                                               # Search about old image
        extention = os.path.splitext(f)[1].lower()
        if (extention in ('.jpg', '.jpeg', '.png')) and os.path.splitext(f)[0].lower() == UnUserID:
            print("Move: " + str(os.path.splitext(f)[0].lower()))
            os.rename(os.path.join(oldPath, f), NewPath + '/' + str(newPicName) + extention)    # Move From oldpath (Sended Imaged) to newPath (1...) or (Unauthorized)
            break

# Get All music paths from Directry
def getAllMusicFiles(Dir):
    global MusicPaths
    MusicNames = ""
    MusicPaths = []
    for f in os.listdir(Music_Dir):
        if os.path.splitext(f)[1].lower() in ('.mp3'):
            p = os.path.join(Music_Dir, f)
            MusicNames += os.path.splitext(f)[0] + ":"
            MusicPaths.append(p)

    return MusicNames[:-1]

# Music player (Stop - Play - Pause - Resume)
def musicPlayer(Command, PlayIndex = 0):
    try:
            if(Command == "play"):
                print "Play"
                mixer.init()
                mixer.music.load(MusicPaths[PlayIndex])
                mixer.music.play()
            elif(Command == "pause"):
                print "Pause"
                mixer.music.pause()
            elif(Command == "resume"):
                print "Resume"
                mixer.music.unpause()
            elif(Command == "stop"):
                print "Stop"
                mixer.music.stop()
            
            Command = ""
    except:
            pass


# Receive message from user and convert it to command
def receiveData(conn):
    msg = ""
    while True:
        data = conn.recv(1024)
        data = data.decode('utf-8')
        msg += data
        
        if not data:
            break

        print("Message: " + str(msg))

	if('Get' in msg):                                                                       # GET All data -->
		data = ControlHome.DataFromArduino                                              # 1. Data From Arduino
		data += ':Sound:' + str(volume.getvolume())[1:-2]                               # 2. Volume value
		data += ':Alarm:' + str(ControlHome.alarmSet)                                   # 3. Alarm state (On/Off)
		data += ':BR:' + str(ControlHome.BRPercentage)                                  # 4. Bed room light percentage
		data += ':LR:' + str(ControlHome.LRPercentage)                                  # 5. Living room light percentage
		data += ':BW:' + str(ControlHome.BRWindowOpen)                                  # 6. Bed room window state
		data += ':KF:' + str(ControlHome.KFanEnable) + str(ControlHome.KUserEnable)     # 7. Kitchen state
		data += ':Air:' + str(ControlHome.LRAirPercentage)                              # 8. Air Condition speed
		print("Response: " + data)
		buff = json.dumps(data)
		conn.sendall(buff)
	elif('Music' in msg):                                               # Music Player Commands
            if('All' in msg):                                                   # 1. Get All music list
                data = getAllMusicFiles(Music_Dir)
                buff = json.dumps(data)
                conn.sendall(buff)
            else:                                                               # 2. Playing music
                _, Cmd, Index = msg.split(":")

                print("Command: " + str(Cmd) + " Index: " + str(Index))
                musicPlayer(Cmd, int(Index))
        elif('Sound' in msg):                                               # Control Volume
            #print(str(int((msg.split(':'))[1])))
            volume.setvolume(int((msg.split(':'))[1]))
            
	else:                                                               # Just Receive Instructions
		conn.close()
		if("NotAccept" in msg):                                         # 1. Unauthorized user
			print("Not Accepted")
			msg = msg.replace("NotAccepted::", "")
			Data = msg.split(":")
			UnUserID = Data[1]
			print(Data[1])
			NewPath = "unauthorized"                                # Unauthorized directory name !
			moveImage(NewPath, UnUserID)
		elif("Accept" in msg):                                          # 2. Authorized User
			print("Accepted")
			msg = msg.replace("Accept::", "")
			Data = msg.split(":")
			UnUserID = Data[1]          # User ID (get his/ her file name and move it)
			PredectedUser = Data[3]     # Predected UserName (Directory Name)
			
			NewPath = os.path.join(trainningDir, PredectedUser)
			moveImage(NewPath, UnUserID)
		else:
		    ControlHome.main(msg)                                       # 3. Control Home

 
getAllMusicFiles(Music_Dir)
s = setupServer()

while True:
    try:
        conn = setupConnection()
        receiveData(conn)
    except:
        pass

GPIO.cleanup()
