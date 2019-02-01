package com.example.eladshriki.chaty;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import java.security.NoSuchAlgorithmException;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final static String host = "https://10.100.102.4:8443";
    static boolean checkLogin = true;

    EditText etUsername, etPassword;
    Button btnLogin, btnReg, btnPassRest;
    CheckBox cbRemember;
    static LoginSystem loginSystem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

//        ChatsDB chatsDB = new ChatsDB(this);
//        chatsDB.open();
//        chatsDB.deleteTable();
//        chatsDB.onCreate(chatsDB.database);
//        chatsDB.close();

        loggedIn();

        cbRemember = findViewById(R.id.cbRemember);

        etUsername = (EditText)findViewById(R.id.etUserName);
        etPassword = (EditText)findViewById(R.id.etPassword);
        btnLogin = (Button)findViewById(R.id.btnLogin);
        btnReg = (Button)findViewById(R.id.btnReg);
        btnPassRest = (Button)findViewById(R.id.btnPassRest);

        btnLogin.setOnClickListener(this);
        btnReg.setOnClickListener(this);
        btnPassRest.setOnClickListener(this);

    }


    @Override
    public void onClick(View view)
    {
        if(view==btnLogin)
        {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();
            try {
                login(username,password);
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        if(btnPassRest==view)
        {
            String username = etUsername.getText().toString();

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
            startActivity(new Intent(this,MainPageActivity.class));
        }
    }
}
