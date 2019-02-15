package com.example.eladshriki.chaty;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class msgGroup
{
    private int id;
    private String name;
    private Bitmap img;
    private ArrayList<msgNotification> messages;

    public msgGroup(int id,String name, msgNotification message,Bitmap img)
    {
        this.id = id;
        this.name = name;
        this.messages = new ArrayList<>();
        this.messages.add(message);
        this.img = img;
    }

    public Bitmap getImg() {
        return img;
    }

    public void deleteAllMsgs()
    {
        this.messages = new ArrayList<msgNotification>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<msgNotification> getMessages() {
        return messages;
    }

    public String getAllmsgs()
    {
        String s = "";
        for(int i=0;i<this.messages.size();i++)
        {
            s+=messages.get(i).getMsg()+" \n";
        }
        return s.substring(0,s.length()-2);
    }
}
