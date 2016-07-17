package com.cherry.alok.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by alok on 28/6/16.
 * http://www.tutorialspoint.com/android/android_sqlite_database.htm
 */
public class DataBaseHelper extends SQLiteOpenHelper {

    //The Android's default system path of your application database.
    private static String DB_PATH = "/data/data/com.cherry.alok.myapplication/databases/";

    private static String DB_NAME = "carlane_db.sqlite3";

    private SQLiteDatabase myDataBase;

    private final Context myContext;

    /**
     * Constructor
     * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
     * @param context
     */
    public DataBaseHelper(Context context) {

        super(context, DB_NAME, null, 1);
        this.myContext = context;
    }

    /**
     * Creates a empty database on the system and rewrites it with your own database.
     * */
    public void createDataBase() throws IOException {

        boolean dbExist = checkDataBase();

        if(dbExist){
            //do nothing - database already exist
        }else{

            //By calling this method and empty database will be created into the default system path
            //of your application so we are gonna be able to overwrite that database with our database.
            this.getReadableDatabase();

            try {

                copyDataBase();

            } catch (IOException e) {

                throw new Error("Error copying database");

            }
        }

    }

    /**
     * Check if the database already exist to avoid re-copying the file each time you open the application.
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase(){

        SQLiteDatabase checkDB = null;

        try{
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

        }catch(SQLiteException e){

            //database does't exist yet.

        }

        if(checkDB != null){

            checkDB.close();

        }

        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() throws IOException{

        //Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        //transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer))>0){
            myOutput.write(buffer, 0, length);
        }

        //Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    public void openDataBase() throws SQLException {

        //Open the database
        String myPath = DB_PATH + DB_NAME;
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READONLY);

    }
    public Cursor getStatus(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor res =  db.rawQuery( "select * from userstatus", null );
        res.moveToFirst();
        while (res.isAfterLast() == false) {
            String nam = res.getString(res.getColumnIndex("status"));

            res.moveToNext();
        }
        return res;
    }

    @Override
    public synchronized void close() {

        if(myDataBase != null)
            myDataBase.close();

        super.close();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean InsertUser  (String name, String phone, String email , String token)
    {
        if(CheckUser(name))return true;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("mobile", phone);
        contentValues.put("email", email);
        contentValues.put("googletoken",token);
        contentValues.put("status",1);
        contentValues.put("bkEndId",0);
        db.insert("user", null, contentValues);
        int number = numberOfRows();
        return true;



    }

    public void DeleteAllUser()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        int num = db.delete("user",null,null);
    }

    public int numberOfRows(){
        SQLiteDatabase db = this.getReadableDatabase();
        int numRows = (int) DatabaseUtils.queryNumEntries(db, "user");
        return numRows;
    }

    public boolean CheckUser  (String firstname)
    {

        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor res =  db.rawQuery( "select * from user where name ='"+firstname+"'", null );// ,

            res.moveToFirst();
            while (res.isAfterLast() == false) {
                String nam = res.getString(res.getColumnIndex("name"));
                if(nam.equals(firstname))
                {
                    return true;
                }
                res.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    public int GetUserId()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        int bkEndId = 0;
        try {
            Cursor res =  db.rawQuery( "select * from user", null );

            res.moveToFirst();
            while (res.isAfterLast() == false) {
                bkEndId = res.getInt(res.getColumnIndex("bkEndId"));
                res.moveToNext();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            String message  = e.getMessage();
        }
        return bkEndId ;
    }

    public HashMap<String , String> FetchUser()
    {
        SQLiteDatabase db = this.getReadableDatabase();
        HashMap<String, String> userdetailsmap = new HashMap<String,String>();
        try {
            Cursor res =  db.rawQuery( "select * from user", null );

            res.moveToFirst();
            while (res.isAfterLast() == false) {
                String nam = res.getString(res.getColumnIndex("name"));
                String mobile = res.getString(res.getColumnIndex("mobile"));
                String googleToken = res.getString(res.getColumnIndex("googletoken"));
                int status = res.getInt(res.getColumnIndex("status"));
                String email = res.getString(res.getColumnIndex("email"));
                int bkEndId = res.getInt(res.getColumnIndex("bkEndId"));

                userdetailsmap.put("name",nam);
                userdetailsmap.put("mobile",mobile);
                userdetailsmap.put("googletoken",googleToken);
                userdetailsmap.put("status",Integer.toString(status));
                userdetailsmap.put("email",email);
                userdetailsmap.put("bkEndId",Integer.toString(bkEndId));


                res.moveToNext();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            String message  = e.getMessage();
        }
        return userdetailsmap ;
    }

    // Add your public helper methods to access and get content from the database.
    // You could return cursors by doing "return myDataBase.query(....)" so it'd be easy
    // to you to create adapters for your views.

    public boolean InsertCar  (String model, String brand, String registration, String yof)
    {
        if(CheckCar(registration))return true;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("model", model);
        contentValues.put("brand", brand);
        contentValues.put("regno", registration);
        contentValues.put("status",1);
        db.insert("usercar", null, contentValues);
        int number = numberOfRows();
        //as soon as you insert a car user status should be updated to CarProfile
        UpdateUserStatus(2);
        return true;

    }

    public boolean CheckCar  (String registration)
    {

        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor res =  db.rawQuery( "select * from usercar where regno ='"+registration+"'", null );// ,

            res.moveToFirst();
            while (res.isAfterLast() == false) {
                String nam = res.getString(res.getColumnIndex("regno"));
                if(nam.equals(registration))
                {
                    return true;
                }
                res.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    public void DeleteAllUserCars()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        int num = db.delete("usercar",null,null);
    }

    public boolean UpdateUserPhone (String phone)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            Cursor res =  db.rawQuery( "select * from user", null );

            res.moveToFirst();
            while (res.isAfterLast() == false) {
                String nam = res.getString(res.getColumnIndex("name"));
                Integer id = res.getInt(res.getColumnIndex("_id"));
                ContentValues contentValues = new ContentValues();
                contentValues.put("mobile", phone);
                db.update("user", contentValues, "_id = ? ", new String[] {Integer.toString(id)} );
                CheckUser(nam);
                res.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean UpdateUserStatus(int status)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            Cursor res =  db.rawQuery( "select * from user", null );

            res.moveToFirst();
            while (res.isAfterLast() == false) {
                String nam = res.getString(res.getColumnIndex("name"));
                Integer id = res.getInt(res.getColumnIndex("_id"));
                ContentValues contentValues = new ContentValues();
                contentValues.put("status", status);
                db.update("user", contentValues, "_id = ? ", new String[] {Integer.toString(id)} );
                CheckUser(nam);
                res.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    public boolean UpdateUserId(int userid)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            Cursor res =  db.rawQuery( "select * from user", null );

            res.moveToFirst();
            while (res.isAfterLast() == false) {
                String nam = res.getString(res.getColumnIndex("name"));
                Integer id = res.getInt(res.getColumnIndex("_id"));
                ContentValues contentValues = new ContentValues();
                contentValues.put("bkEndId", userid);
                db.update("user", contentValues, "_id = ? ", new String[] {Integer.toString(id)} );
                CheckUser(nam);
                res.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }


    public HashMap<String,String> GetUserCarDetails()
    {

        SQLiteDatabase db = this.getReadableDatabase();
        HashMap<String, String> cardetailsmap = new HashMap<String,String>();

        try {
            Cursor res =  db.rawQuery( "select * from usercar", null );// ,
            res.moveToFirst();
            while (res.isAfterLast() == false) {
                String car_reg_no = res.getString(res.getColumnIndex("regno"));
                String car_model = res.getString(res.getColumnIndex("brand"));
                String car_brand = res.getString(res.getColumnIndex("model"));
                cardetailsmap.put("brand",car_brand);
                cardetailsmap.put("model",car_model);
                cardetailsmap.put("regno",car_reg_no);
                res.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cardetailsmap;

    }



}