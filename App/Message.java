package com.example.eladshriki.chaty;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.Date;

public class Message
{
    private int id;
    private String chatName;
    private String username;
    private String sender;
    private String text;
    private String  date;
    private String img;
    private byte[] imgBytes;
    private Bitmap bitmap;

    public Message(int id,String chatName, String username, String sender, String text, String date, byte[] img) {
        this.id = id;
        this.chatName = chatName;
        this.username = username;
        this.sender = sender;
        this.text = text;
        this.date = date;
        this.img =  Arrays.toString(img);
        this.imgBytes = img;
        if(imgBytes!=null)
            toBitmap();
    }

    public Message(String sender, String text, String chatName, String username) {
        this.sender = sender;
        this.text = text;
        this.chatName = chatName;
        this.username = username;
        this.img = null;
        this.date = new Date().toString();
    }

    public Message(String sender, byte[] img,String chatName,String username)
    {
        this.id = -1;
        this.sender = sender;
        this.text = null;
        this.img = Arrays.toString(img);
        this.chatName = chatName;
        this.username = username;
        this.imgBytes = img;
        this.date = new Date().toString();
        toBitmap();
    }

    public Message(int id,String sender, byte[] img,String chatName,String username,String date) {
        this.id = id;
        this.sender = sender;
        this.text = null;
        this.img = Arrays.toString(img);
        this.chatName = chatName;
        this.username = username;
        this.date = date;
        this.imgBytes = img;
        toBitmap();
    }

    public Message(int id,String sender, String text,String chatName,String username,String date) {
        this.id = id;
        this.sender = sender;
        this.text = text;
        this.chatName = chatName;
        this.username = username;
        this.img = null;
        this.date = date;
        this.imgBytes = null;
    }

    public void toBitmap()
    {
        this.bitmap = BitmapFactory.decodeByteArray(imgBytes, 0, imgBytes.length);
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public String getDate() {
        return date;
    }

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    public byte[] getImgBytes() {
        return imgBytes;
    }

    public String getChatName()
    {
        return this.chatName;
    }

    public String getStringImg()
    {
        return  this.img;
    }

    @Override
    public String toString() {
        return "Message{" +
                "chatName='" + chatName + '\'' +
                ", username='" + username + '\'' +
                ", sender='" + sender + '\'' +
                ", text='" + text + '\'' +
                ", date='" + date + '\'' +
                ", img=" + img +
                '}';
    }
}
