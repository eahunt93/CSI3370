package com.example.elija.sms;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by elija on 11/1/2017.
 */

public class MyDBhandler extends SQLiteOpenHelper {
    private  static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "SecretKeys.dp";
    public static final String Table_Keys = "keys";
    public static final String Column_ID = "id";
    public static final String Column_NAME ="name";
    public static final String Column_Address= "Number";
    public static final String Column_SecretKey = "SecretKey";

    public MyDBhandler(Context context, String name, SQLiteDatabase.CursorFactory factory,int version ){
        super(context, DATABASE_NAME, factory,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "Create Table " + Table_Keys+ "("+
                Column_ID+ " INTEGER PRIMARY KEY AUTOINCREMENT, "+
                Column_NAME+ " TEXT," +
                Column_Address+ " TEXT unique,"+
                Column_SecretKey+ " TEXT"+
                ");";
        db.execSQL(query);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS" + Table_Keys);
        onCreate(db);
    }
    //adds a new row to the database
    public void addContact(ContactObject contactObject){
        ContentValues values = new ContentValues();
        values.put(Column_NAME, contactObject.getContact());
        values.put(Column_Address, contactObject.getNumber());
        values.put(Column_SecretKey, contactObject.getSecretkey());
        SQLiteDatabase db = getWritableDatabase();
        db.insert(Table_Keys, null, values);
        db.close();
    }
    //returns the toString of the database
    public String databaseToString() {
        String dbString = "";
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + Table_Keys + " WHERE 1";

        Cursor c = db.rawQuery(query, null);
        c.moveToFirst();
        while (!c.isAfterLast()) {
            if(c.getString(c.getColumnIndex("id")) != null &&
                    c.getString(c.getColumnIndex("name"))!= null&&
                    c.getString(c.getColumnIndex("Number")) !=null
                    && c.getString(c.getColumnIndex("SecretKey"))!=null){
                dbString+=c.getString(c.getColumnIndex("name")) + " " + c.getString(c.getColumnIndex("Number")) + " " + c.getString(c.getColumnIndex("SecretKey"))+ "\n";
            }
            c.moveToNext();
        }
        db.close();
        return dbString;
    }
    //used to update the users secret key
    public void upDateRow(ContactObject contactObject){
        ContentValues values = new ContentValues();
        values.put(Column_NAME, contactObject.getContact());
        values.put(Column_Address, contactObject.getNumber());
        values.put(Column_SecretKey, contactObject.getSecretkey());
        SQLiteDatabase db = getWritableDatabase();
        db.update(Table_Keys, values, "name = ?",new String[]{contactObject.getContact()});
        db.close();
    }
    //check if the table is empty
    public boolean checkIfTableIsEmpty(){
        SQLiteDatabase db = getWritableDatabase();
        Cursor mCursor = db.rawQuery("SELECT * FROM " + Table_Keys, null);
        Boolean rowExists;

        if(mCursor.moveToFirst()){
            rowExists = true;
        }else{
            rowExists = false;
        }
        return rowExists;
    }
    //gets the secret key based on the name that is passed through
    public String getSecretKey(String name){
        String secretkey = "";
      SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(Table_Keys, new String[]{Column_ID, Column_NAME, Column_Address, Column_SecretKey}, Column_NAME  +
                "=?",new String[]{name}, null, null, null);

        secretkey = null;
        if(c.moveToFirst()){
           secretkey =c.getString(3);
        }
        c.close();
        return secretkey;
    }
}
