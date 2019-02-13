package com.example.eladshriki.chaty;

import java.util.ArrayList;

public class msgGroup
{
    private int id;
    private String name;
    private ArrayList<msgNotification> messages;

    public msgGroup(int id,String name, msgNotification message)
    {
        this.id = id;
        this.name = name;
        this.messages = new ArrayList<>();
        this.messages.add(message);
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
