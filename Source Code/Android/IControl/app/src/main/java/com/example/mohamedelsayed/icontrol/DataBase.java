package com.example.mohamedelsayed.icontrol;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by Mohamed Elsayed on 10/02/2017.
 */
public class DataBase extends SQLiteOpenHelper {

    public static final String DBName = "IControl.db";

    public static final String TBUsers = "Users";
    public static final String TBUNAutho = "UnAuthorized";

    public static final String ID = "ID";
    public static final String Name = "Name";
    public static final String Image = "Image";
    public static final String Date = "AccessDate";

    public DataBase(Context context) {
        super(context, DBName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TBUsers + " (" + ID + " INTEGER," + Name + " TEXT)");
        db.execSQL("CREATE TABLE " + TBUNAutho + " (" + ID + " INTEGER, " + Date + " DATETIME, " + Image + " BLOB)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TBUsers);
        db.execSQL("DROP TABLE IF EXISTS " + TBUNAutho);
        onCreate(db);
    }

    // Add all authorized users in the system: (ID, Name)

    public boolean AddUser(int ID, String Name){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(this.ID, ID);
        contentValues.put(this.Name, Name);
        return (db.insert(TBUsers, null, contentValues) != -1 ? true: false);
    }

    // Add user who is not recognized by the system: (ID, Image, AccessDate)

    public boolean AddUnauthorizedUser(int ID, Bitmap Image, String AccessDate){
        byte[] Img = getBitmapAsByteArray(Image);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(this.ID, ID);
        contentValues.put(this.Date, AccessDate);
        contentValues.put(this.Image, Img);
        return (db.insert(TBUNAutho, null, contentValues) != -1 ? true : false);
    }

    // Convert Image to array for storing in the database

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }


    // Get all users in the system from database to show then in listView later: (ID, Name)

    public ArrayList<Person> GetAllUsers(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT * FROM " + TBUsers, null);
        if(res.getCount() == 0)
            return  null;
        else
        {
            ArrayList<Person> Users = new ArrayList<>();
            while (res.moveToNext()){
                Person P = new Person();
                P.ID = res.getInt(0);
                P.Name = res.getString(1);
                Users.add(P);
            }
            return Users;
        }
    }

    // get all users who are not recognized by the system: (ID, Image, AccessDate)

    public ArrayList<UnPerson> GetUnAuthorizedUsers(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("SELECT * FROM " + TBUNAutho, null);

        if(result.getCount() == 0)
            return null;
        else {
            ArrayList<UnPerson> UnUsers = new ArrayList<>();
            while (result.moveToNext()){
                UnPerson P = new UnPerson();
                P.ID = result.getInt(0);
                P.AccessTime = result.getString(1);
                byte[] ImgByte = result.getBlob(2);

                P.Image = BitmapFactory.decodeByteArray(ImgByte, 0 , ImgByte.length);
                UnUsers.add(P);
            }

            return UnUsers;
        }
    }

    // Delete unauthorized user after check his/ her Image: ID

    public int DeleteUnUser(int ID){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TBUNAutho, this.ID + "=?", new String[]{String.valueOf(ID)});
    }

}
