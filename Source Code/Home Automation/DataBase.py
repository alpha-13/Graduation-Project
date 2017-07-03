# -*- coding: utf-8 -*-
"""
Created on Sat Feb 04 06:58:17 2017

@author: Mohamed Elsayed
"""

import sqlite3 as sql, cv2

"""
    This Class uses for:-
        1. Insert new user to the system and return his/ her ID for using it to create a folder which will contains user Images
        2. Return all users from the database
"""

class DB:
    
    def InsertInDB(self, Name):
        self.Name = Name
        conn = sql.connect('IControl.db')
        cmd = "INSERT INTO person (Name) VALUES ('"+ Name + "')"
        conn.execute(cmd)
        conn.commit()
        
        cmd = "SELECT ID FROM person ORDER BY ID DESC LIMIT 1"
        cursor = conn.execute(cmd)
            
        ID = cursor.fetchone()[0]
        conn.close()
        return ID

    
    def GetAllClients(self):
        conn = sql.connect('IControl.db')
        cmd = "SELECT ID, Name FROM person"
        cursor = conn.execute(cmd)
        
        Info = cursor.fetchall()
        
        return Info

"""
    This class used for:-
        1. Add unauthorized user to database
        2. Delete unauthorized user from database
"""

class UnAuthorizedDB:   
    
    def AddPerson(self, Img, Face):
        self.Img = Img
        Un_dir = 'users'
        
        conn = sql.connect('IControl.db')
        
        cmd = '''INSERT INTO UnAuthorizedUsers (Image) VALUES (?)'''
        params = [sql.Binary(Img)]
        conn.execute(cmd, params)
        conn.commit()
        
        cmd = "SELECT ID FROM UnAuthorizedUsers ORDER BY ID DESC LIMIT 1"
        cursor = conn.execute(cmd)
            
        ID = cursor.fetchone()[0]
        conn.close()
        
        cv2.imwrite('%s/%s.png' % (Un_dir,ID), Face)
    
    def DeletePerson(self, ID):
        self.ID = ID
