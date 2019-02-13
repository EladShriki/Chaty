package com.example.eladshriki.chaty;

public class msgNotification
{
    private String sender;
    private String msg;

    public msgNotification(String sender,String msg)
    {
        this.sender = sender;
        this.msg = msg;
    }

    public String getSender() {
        return sender;
    }

    public String getMsg() {
        return msg;
    }
}
