package com.example.eladshriki.chaty;

public class User
{
    private String username;
    private String email;
    private String status;
    private byte[] imgBytes;

    public User(String username, String email,String status,byte[] img)
    {
        this.username = username;
        this.email = email;
        this.status = status;
        this.imgBytes = img;
    }

    public User(String username, String email,String status)
    {
        this.username = username;
        this.email = email;
        this.status = status;
        this.imgBytes = null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getImgBytes() {
        return imgBytes;
    }

    public String getStatus() {
        return status;
    }
}
