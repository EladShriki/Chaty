package com.example.eladshriki.chaty;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

public class Chat
{
    private String chatName;
    private Bitmap profileImg;

    public Chat(String chatName,Bitmap img)
    {
        this.chatName = chatName;
        this.profileImg = img;
    }

    public String getChatName() {
        return chatName;
    }

    public Bitmap getProfileImg() {
        return profileImg;
    }

    public void setProfileImg(Bitmap profileImg) {
        this.profileImg = profileImg;
    }

}
