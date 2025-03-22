package com.example.RQWL001;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "account_daily.db";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table account(_id integer primary key autoincrement," +//主键
                "Title varchar(20)," +//Title
                "Remark varchar(20)," +//Remark
                "Date varchar(20)," +//Date
                "Money varchar(20)," +//Money
                "InOut vaechar(20))";//in  or out
        String sqlPerson = "create table accountPerson(_id integer primary key autoincrement," +//主键
                "Title varchar(20)," +//Title
                "Remark varchar(20)," +//Remark
                "Date varchar(20)," +//Date
                "Money varchar(20)," +//Money
                "InOut vaechar(20))";//in  or out
        String sqlThing = "create table accountThing(_id integer primary key autoincrement," +//主键
                "Title varchar(20)," +//Title
                "Remark varchar(20)," +//Remark
                "Date varchar(20)," +//Date
                "Money varchar(20)," +//Money
                "InOut vaechar(20))";//in  or out
        db.execSQL(sql);
        db.execSQL(sqlPerson);
        db.execSQL(sqlThing);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
