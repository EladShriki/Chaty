package com.example.eladshriki.chaty;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;

import static android.content.Context.MODE_PRIVATE;

public class LoginSystem
{
    private Context context;
    private String host;
    private String username;
    private String password;

    public LoginSystem(Context context ,String host,String username,String password) {
        this.context = context;
        this.host = host;
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Context getContext() {
        return context;
    }

    public Intent login(Class aClass) throws NoSuchAlgorithmException {
        if(MainActivity.checkLogin)
        {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(password.getBytes());
            String hashPass = new String(messageDigest.digest());
            String urlParameters = "username=" + username + "&password=" + hashPass;
            try {
                String url = host + "/TestServer/login";
                HttpsURLConnection conn = CustomCAHttpProvider.getConnection(context,url);

                conn.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

                writer.write(urlParameters);
                writer.flush();

                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                line = reader.readLine();
                if (line != null) {
                    if (line.equals("User is already Connected!")) {
                        Toast.makeText(context, line, Toast.LENGTH_SHORT).show();
                    } else {
                        Intent intt = new Intent(context, aClass);
                        return intt;
                    }
                } else {
                    Toast.makeText(context, "Username or password is wrong!", Toast.LENGTH_LONG).show();
                }
                writer.close();
                reader.close();
            } catch (Exception e) {
                Log.e("Chaty",e.getMessage());
            }
        }
        return null;
    }


    public Intent logout()
    {
        String urlParameters = "username=" + this.username;
        SharedPreferences preferences = context.getSharedPreferences("LoginInfo",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
        MainActivity.checkLogin = true;
        try {
            String url = host+"/TestServer/Logout";
            HttpsURLConnection conn = CustomCAHttpProvider.getConnection(context,url);

            conn.setDoOutput(true);

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

            writer.write(urlParameters);
            writer.flush();

            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            line = reader.readLine();
            if (line != null)
            {

            }
            writer.close();
            reader.close();

            return new Intent(context,MainActivity.class);

        } catch (Exception e) {

        }
        return null;
    }
}
