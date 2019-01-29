package com.example.eladshriki.chaty;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class Chat implements Parcelable
{
    private String chatName;
    private ArrayList<Message> messages;

    public Chat(String chatName)
    {
        this.messages = new ArrayList<Message>();
        this.chatName = chatName;
    }

    public Chat(String chatName,Message message)
    {
        this.messages = new ArrayList<Message>();
        messages.add(message);
        this.chatName = chatName;
    }

    public Chat(Parcel in) {
        this.chatName = in.readString();
        this.messages = new ArrayList<Message>();
        in.readTypedList(messages,Message.CREATOR);
    }

    public static final Creator<Chat> CREATOR = new Creator<Chat>() {
        @Override
        public Chat createFromParcel(Parcel in) {
            return new Chat(in);
        }

        @Override
        public Chat[] newArray(int size) {
            return new Chat[size];
        }
    };

    public void addMessages(Message message)
    {
        this.messages.add(message);
    }

    public String getChatName() {
        return chatName;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(chatName);
        parcel.writeTypedList(messages);
    }
}
