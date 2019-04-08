package com.example.eladshriki.chaty;

import android.graphics.Bitmap;

public class User extends Chat
{
    private String username;
    private String email;
    private String status;
    private Bitmap img;

    public User(String username, String email,String status,Bitmap img)
    {
        super(username,img);
        this.username = username;
        this.email = email;
        this.status = status;
        this.img = img;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public Bitmap getImg() {
        return img;
    }

    public String getStatus() {
        return status;
    }
}
