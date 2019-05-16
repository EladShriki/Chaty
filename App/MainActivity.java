package com.example.eladshriki.chaty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Properties;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    static String host = "https://10.100.102.13:8443";
    static boolean checkLogin = true;

    EditText etUsername, etPassword;
    Button btnLogin, btnReg;
    static LoginSystem loginSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            readHostConfig();
        } catch (IOException e) {
            e.printStackTrace();
        }

//        ChatsDB chatsDB = new ChatsDB(this);
//        chatsDB.open();
//        chatsDB.deleteTable();
//        chatsDB.onCreate(chatsDB.database);
//        chatsDB.close();

        loggedIn();

        etUsername = (EditText)findViewById(R.id.etUserName);
        etPassword = (EditText)findViewById(R.id.etPassword);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnReg = (Button)findViewById(R.id.btnReg);

        btnLogin.setOnClickListener(this);
        btnReg.setOnClickListener(this);

    }

    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);
    }


    @Override
    public void onClick(View view)
    {
        if(view==btnLogin)
        {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            etPassword.setText("");
            try {
                login(username,password);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        if(btnReg==view)
        {
            startActivity(new Intent(this,RegisterActivity.class));
        }
    }

    public void login(String username,String password) throws NoSuchAlgorithmException {
        loginSystem = new LoginSystem(this,host,username,password);
        Intent intent = loginSystem.login(MainPageActivity.class);
        if(intent!=null)
        {
            saveLoginInfo(username,password);
            startActivity(intent);
        }
    }

    public void saveLoginInfo(String username,String password)
    {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("LoginInfo",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("username",username);
        editor.putString("password",password);
        editor.putBoolean("checkLogin",false);
        editor.commit();
    }

    public void loggedIn()
    {
        SharedPreferences preferences = getApplicationContext().getSharedPreferences("LoginInfo",MODE_PRIVATE);
        String username = preferences.getString("username",null);
        String password = preferences.getString("password",null);
        checkLogin = preferences.getBoolean("checkLogin",true);
        if(!checkLogin)
        {
            loginSystem = new LoginSystem(this, host, username, password);
            Intent i = new Intent(this,MainPageActivity.class) ;
            startActivity(i);
        }
    }

    public void changeHostConfig(String newHost)
    {
        String root = Environment.getExternalStorageDirectory().toString();
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(root + "/Chaty/host.txt");
            outputStream = new FileOutputStream(root + "/Chaty/host.txt");
        } catch (FileNotFoundException e) {
            Log.i("Chaty",e.getMessage());
        }
        Properties properties = new Properties();
        try {
            properties.load(inputStream);
            properties.setProperty("host",newHost);
            properties.store(outputStream,null);
            inputStream.close();
            outputStream.close();
        } catch (IOException e) {
            Log.i("Chaty",e.getMessage());
        }
    }

    public void readHostConfig() throws IOException
    {
        boolean fileFound = false;
        String root = Environment.getExternalStorageDirectory().toString();
        File dir = new File(root + "/Chaty/");
        dir.mkdir();
        File file = new File(root + "/Chaty/host.txt");
        Properties properties = new Properties();

        try
        {
            loadProperties(properties, file);
            fileFound = true;
        }
        catch (Exception e)
        {
            Log.i("Chaty",e.getMessage());
        }

        if(!fileFound)
        {
            properties.setProperty("host",host);
            file.createNewFile();
            saveProperties(properties,file);
        }
        else
        {
            host = properties.getProperty("host");
        }
    }

    public void saveProperties(Properties p,File file) throws IOException
    {
        FileOutputStream fr = new FileOutputStream(file);
        p.store(fr, "Properties");
        fr.close();
    }

    public void loadProperties(Properties p, File file)throws IOException
    {
        FileInputStream fi=new FileInputStream(file);
        p.load(fi);
        fi.close();
    }
}
