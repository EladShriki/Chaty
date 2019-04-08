package com.example.eladshriki.chaty;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ChatsDB extends SQLiteOpenHelper
{
    public static final String DATABASENAME="chats.db";
    public static final String TABLE_MSG="Messages";
    public static final int DATABASEVERSION=1;


    public static final String COLUMN_ID="messageID";
    public static final String COLUMN_SENDER="sender";
    public static final String COLUMN_TEXT="text";
    public static final String COLUMN_CHAT_NAME="chatName";
    public static final String COLUMN_USERNAME="username";
    public static final String COLUMN_DATE="date";
    public static final String COLUMN_IMG="img";

    private static final String CREATE_TABLE_MSG="CREATE TABLE IF NOT EXISTS " +
            TABLE_MSG + "(" + COLUMN_ID +  " INTEGER PRIMARY KEY," + COLUMN_SENDER + " VARCHAR," + COLUMN_TEXT + " VARCHAR," + COLUMN_CHAT_NAME + " VARCHAR,"+ COLUMN_USERNAME +" VARCHAR,"+
            COLUMN_IMG +" BLOB,"+ COLUMN_DATE +" VARCHAR" + ");";

    String []allColumns={ChatsDB.COLUMN_ID, ChatsDB.COLUMN_USERNAME,ChatsDB.COLUMN_TEXT,ChatsDB.COLUMN_CHAT_NAME,ChatsDB.COLUMN_SENDER,ChatsDB.COLUMN_DATE,ChatsDB.COLUMN_IMG};

    SQLiteDatabase database;

    public ChatsDB(Context context)
    {
        super(context,DATABASENAME,null,DATABASEVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_MSG);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MSG);
        onCreate(db);
    }
    public void open()
    {
        database=this.getWritableDatabase();
        Log.i("data", "Database connection open");
    }

    public Message createMessage(Message temp)
    {
        ContentValues values=new ContentValues();
        values.put(ChatsDB.COLUMN_ID,temp.getId());
        values.put(ChatsDB.COLUMN_USERNAME, temp.getUsername());
        values.put(ChatsDB.COLUMN_TEXT, temp.getText());
        values.put(ChatsDB.COLUMN_CHAT_NAME, temp.getChatName());
        values.put(ChatsDB.COLUMN_SENDER,temp.getSender());
        values.put(ChatsDB.COLUMN_DATE,temp.getDate());
        values.put(ChatsDB.COLUMN_IMG,temp.getImgBytes());

        long insertId=database.insert(ChatsDB.TABLE_MSG, null, values);
        Log.i("data", "New Message Added!");
        return temp;

//        SQLiteStatement p = database.compileStatement("insert into "+ TABLE_MSG +"("+COLUMN_USERNAME+","+COLUMN_TEXT+","+COLUMN_CHAT_NAME+","+COLUMN_SENDER+","+COLUMN_DATE+","+COLUMN_IMG+") values(?,?,?,?,?,?)");
//        p.bindString(1,temp.getUsername());
//        p.bindString(2,temp.getText());
//        p.bindString(3,temp.getChatName());
//        p.bindString(4,temp.getSender());
//        p.bindString(5,temp.getDate());
//        p.bindBlob(6,temp.getImgBytes());
//        p.execute();
//
//        return temp;
    }

    public ArrayList<Message> getAllMessagesByName(String name) {

        ArrayList<Message> l = new ArrayList<Message>();
        String colums = Arrays.toString(allColumns);

        String sql = "SELECT "+colums.substring(1,colums.length()-1)+" FROM "+TABLE_MSG+" WHERE LOWER("+COLUMN_CHAT_NAME+") = ?";

        String[] args = {name};
        Cursor cursor= database.rawQuery(sql,args);

        if(cursor.getCount()>0)
        {
            while(cursor.moveToNext())
            {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String username=cursor.getString(cursor.getColumnIndex(ChatsDB.COLUMN_USERNAME));
                String text =cursor.getString(cursor.getColumnIndex(ChatsDB.COLUMN_TEXT));
                String chatName = cursor.getString(cursor.getColumnIndex(ChatsDB.COLUMN_CHAT_NAME));
                String sender = cursor.getString(cursor.getColumnIndex(ChatsDB.COLUMN_SENDER));
                String date = cursor.getString(cursor.getColumnIndex(ChatsDB.COLUMN_DATE));
                byte[] img = cursor.getBlob(cursor.getColumnIndex(ChatsDB.COLUMN_IMG));
                Message c=new Message(id,chatName,username,sender,text,date,img);
                l.add(c);
            }
        }
        return l;
    }

    public ArrayList<Message> getAllMessages() {

        ArrayList<Message> l = new ArrayList<Message>();
        Cursor cursor=database.query(ChatsDB.TABLE_MSG, allColumns, null, null, null, null, null);

        if(cursor.getCount()>0)
        {
            while(cursor.moveToNext())
            {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String username=cursor.getString(cursor.getColumnIndex(ChatsDB.COLUMN_USERNAME));
                String text =cursor.getString(cursor.getColumnIndex(ChatsDB.COLUMN_TEXT));
                String chatName = cursor.getString(cursor.getColumnIndex(ChatsDB.COLUMN_CHAT_NAME));
                String sender = cursor.getString(cursor.getColumnIndex(ChatsDB.COLUMN_SENDER));
                String date = cursor.getString(cursor.getColumnIndex(ChatsDB.COLUMN_DATE));
                byte[] img = cursor.getBlob(cursor.getColumnIndex(ChatsDB.COLUMN_IMG));
                Message c=new Message(id,chatName,username,sender,text,date,img);
                l.add(c);
            }
        }
        return l;
    }

    public ArrayList<String> getAllChats(String username)
    {
        ArrayList<String> chats = new ArrayList<>();
        String[] args = {username};

        String sql = "SELECT  DISTINCT "+COLUMN_CHAT_NAME+" FROM "+ TABLE_MSG +" WHERE "+COLUMN_USERNAME + " = ?";
        Cursor cursor = database.rawQuery(sql,args);

        if(cursor.getCount()>0)
        {
            while(cursor.moveToNext())
            {
                String chatName = cursor.getString(cursor.getColumnIndex(COLUMN_CHAT_NAME));
                chats.add(chatName.toLowerCase());
            }
        }
        Set<String> set = new HashSet<>(chats);
        chats.clear();
        chats.addAll(set);
        return chats;
    }

    public ArrayList<Integer> getAllIdByName(String username)
    {
        ArrayList<Integer> list = new ArrayList<Integer>();
        String selection = COLUMN_USERNAME +" =?";
        String[] args = {username};
        String[] column = {COLUMN_ID};
        Cursor cursor=database.query(ChatsDB.TABLE_MSG, column, selection, args, null, null, null);

        if(cursor.getCount()>0)
        {
            while(cursor.moveToNext())
            {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                list.add(id);
            }
        }
        return list;
    }

    public boolean isMsgExsist(Message message)
    {
        Cursor cursor=database.query(ChatsDB.TABLE_MSG, allColumns, null, null, null, null, null);
        if(cursor.getCount()>0)
        {
            while(cursor.moveToNext())
            {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID));
                String username=cursor.getString(cursor.getColumnIndex(ChatsDB.COLUMN_USERNAME));
                String text =cursor.getString(cursor.getColumnIndex(ChatsDB.COLUMN_TEXT));
                String chatName = cursor.getString(cursor.getColumnIndex(ChatsDB.COLUMN_CHAT_NAME));
                String sender = cursor.getString(cursor.getColumnIndex(ChatsDB.COLUMN_SENDER));
                String date = cursor.getString(cursor.getColumnIndex(ChatsDB.COLUMN_DATE));
                byte[] img = cursor.getBlob(cursor.getColumnIndex(ChatsDB.COLUMN_IMG));
                Message temp=new Message(id,chatName,username,sender,text,date,img);
                if(temp.toString().equals(message.toString()))
                    return true;
            }
        }
        return false;
    }

    public void deleteTable()
    {
        database.execSQL("DROP TABLE "+TABLE_MSG);
    }

    public void deleteHistory(String username,String sender)
    {
        database.execSQL("delete from messages where sender= "+ sender + " and reciver= "+ username);
    }
}
