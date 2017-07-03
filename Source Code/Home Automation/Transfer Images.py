# -*- coding: utf-8 -*-
"""
Created on Sun Feb 05 08:08:11 2017

@author: Mohamed Elsayed
"""

import socket, sys, thread, os, time, DataBase, json

port = 8000
connectionList = []

UN_Dir = 'users'
New_Dir = 'Sended Images'

# Setup Connection
def setupServer():
    s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    print("Socket created !")
    try:
        s.bind(('', port))
        print("Socket bind complete !")
        s.listen(10)
    except socket.error as msg:
        print(msg)

    return s

def sendData():
        while True:
                if(len(os.listdir(UN_Dir)) > 0 and len(connectionList) > 0):
                        time.sleep(.5)
                        print("\n\n ========== \n " + str(connectionList) + "\n ======== \n\n")
                        try:
                                Paths = []
                                for f in os.listdir(UN_Dir):                                                    # Get All Images for transfer
                                        if os.path.splitext(f)[1].lower() in ('.jpg', '.jpeg', '.png'):
                                                p = os.path.join(UN_Dir, f)
                                                print p
                                                Paths.append(p)

                                                
                                for path in Paths:                                                              # Loop for each path in Paths
                                        sended = False
                                        for Socket in connectionList:                                           # Send Image for all users
                                                connection = Socket[0]
                                                FileName = str(os.path.splitext(path)[0].split('/')[-1])
                                                #print("File Name: " + str(FileName))

                                                ct = os.path.getctime(path)                                     # Get Create time of this image (AccessDate)
                                                dateTime = time.ctime(ct)
                                                header = str(FileName) + "::" + str(dateTime) + "&&"            # Set Header
                                                try:
                                                        connection.send(str.encode(header))
                                                        print("File Name: " + str(FileName))
                                                        pic = open(path, 'rb')
                                                        chunk = pic.read(1024)

                                                        while chunk:                                            # Send Image
                                                                try:
                                                                        connection.send(chunk)
                                                                        chunk = pic.read(1024)
                                                                        sended = True
                                                                except socket.error:
                                                                        sended = False
                                                                        Socket[0].close()
                                                                        connectionList.remove(Socket)
                                                                        print connectionList
                                                                        break
                                                        pic.close()
                                                        time.sleep(1)
                                                except socket.error:
                                                        sended = False
                                                        connection.close()
                                                        time.sleep(1)
                                                        connectionList.remove(Socket)
                                                        break

                                        if(sended):                                                             # Check if Image was transfered for any user or not!
                                                fil = path.split('/')[-1]
                                                print("File: " + fil)
                                                os.rename(path, New_Dir + '/' + fil.split('.')[-2] + '.' +fil.split('.')[-1])
                        except thread.error:
                                pass
                time.sleep(1)

# Accept new sockets
def acceptSockets():
    Socket = sock.accept()
    
    sendUsers(Socket[0])
    connectionList.append(Socket)

# Send All users to client
def sendUsers(connection):
    DB = DataBase.DB()
    Info = DB.GetAllClients()
    print("Sending All users")
    
    buff = json.dumps(Info)
    connection.send(buff)
    print("Done")

sock = setupServer()

thread.start_new_thread(sendData, ( ))

while True:
    try:
        acceptSockets()
    except Exception:
        continue
