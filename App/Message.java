package com.example.eladshriki.chaty;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;
import java.util.Date;

public class Message implements Parcelable
{

    private String chatName;
    private String username;
    private String sender;
    private String text;
    private String  date;
    private String img;
    private byte[] imgBytes;

    public Message(String chatName, String username, String sender, String text, String date, byte[] img) {
        this.chatName = chatName;
        this.username = username;
        this.sender = sender;
        this.text = text;
        this.date = date;
        this.img =  Arrays.toString(img);
        this.imgBytes = img;
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
        this.sender = sender;
        this.text = null;
        this.img = Arrays.toString(img);
        this.chatName = chatName;
        this.username = username;
        this.imgBytes = img;
        this.date = new Date().toString();
    }

    public Message(String sender, byte[] img,String chatName,String username,String date) {
        this.sender = sender;
        this.text = null;
        this.img = Arrays.toString(img);
        this.chatName = chatName;
        this.username = username;
        this.date = date;
        this.imgBytes = img;
    }

    public Message(String sender, String text,String chatName,String username,String date) {
        this.sender = sender;
        this.text = text;
        this.chatName = chatName;
        this.username = username;
        this.img = null;
        this.date = date;
        this.imgBytes = null;
    }

    protected Message(Parcel in) {
        chatName = in.readString();
        sender = in.readString();
        text = in.readString();
        username = in.readString();
        date = in.readString();
        img = in.readString();

        String[] byteValues = img.substring(1, img.length() - 1).split(",");
        imgBytes = new byte[byteValues.length];

        for (int i=0, len=imgBytes.length; i<len; i++)
            imgBytes[i] = Byte.parseByte(byteValues[i].trim());

    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDate() {
        return date;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getText() {
        return text;
    }

    public byte[] getImgBytes() {
        return imgBytes;
    }

    public void setText(String text) {
        this.text = text;
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
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(chatName);
        parcel.writeString(sender);
        parcel.writeString(text);
        parcel.writeString(username);
        parcel.writeString(date);
        parcel.writeString(img);
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
