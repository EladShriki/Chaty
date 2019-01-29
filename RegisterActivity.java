package com.example.eladshriki.chaty;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    String host = MainActivity.host;

    EditText etRUser,etRPass,etREmail;
    Button btnSub, btnName,btnRLogin;
    TextView tvNameCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        etRUser = (EditText)findViewById(R.id.etRUser);
        etRPass = (EditText)findViewById(R.id.etRPass);
        etREmail = (EditText)findViewById(R.id.etREmail);
        tvNameCheck = (TextView) findViewById(R.id.tvNameCheck);

        btnSub = (Button)findViewById(R.id.btnSub);
        btnName = (Button)findViewById(R.id.btnName);
        btnRLogin = (Button)findViewById(R.id.btnRLogin);

        btnSub.setOnClickListener(this);
        btnName.setOnClickListener(this);
        btnRLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {

        if(view==btnSub)
        {
            nameCheck();
            if(tvNameCheck.getText().toString().equals("Username is free!")) {
                String username = etRUser.getText().toString();
                String password = etRPass.getText().toString();
                String eMail = etREmail.getText().toString();

                if(goodPassword(password)) {
                    if (realEmail(eMail)) {
                        String urlParameters = "username=" + username + "&password=" + password + "&Email=" + eMail + "&nameCheck=" + 1;
                        try {
                            String url = host + "/TestServer/Register";
                            HttpsURLConnection conn = CustomCAHttpProvider.getConnection(this, url);

                            conn.setDoOutput(true);

                            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

                            writer.write(urlParameters);
                            writer.flush();

                            String line;
                            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                            if ((line = reader.readLine()) != null) {

                            }
                            writer.close();
                            reader.close();
                            Toast.makeText(this, "User Created!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(this,MainActivity.class));
                        } catch (Exception e) {
                        }
                    } else
                        Toast.makeText(this, "Use Gmail mail please!", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(this, "Invalid password!", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(this, "Check Your name First!", Toast.LENGTH_LONG).show();

        }
        if(view==btnName)
        {
            nameCheck();
        }
        if(view==btnRLogin)
        {
            startActivity(new Intent(this,MainActivity.class));
        }
    }

    public boolean goodPassword(String password)
    {
        if (password != null)
        {
            Pattern p = Pattern.compile("^[a-z0-9](\\.?[a-z0-9]){5,}$");
            Matcher m = p.matcher(password);
            return m.find();
        }
        return false;
    }

    public boolean realEmail(String email)
    {
        if (email != null)
        {
            Pattern p = Pattern.compile("^[a-z0-9](\\.?[a-z0-9]){5,}@gmail\\.com$");
            Matcher m = p.matcher(email);
            return m.find();
        }
        return false;
    }

    public boolean goodName(String name)
    {
        if (name != null)
        {
            Pattern p = Pattern.compile("^[a-z0-9](\\.?[a-z0-9]){5,}$");
            Matcher m = p.matcher(name);
            return m.find();
        }
        return false;
    }

    public void nameCheck()
    {
        String name = etRUser.getText().toString();
        if(goodName(name))
        {
            String urlParameters = "username=" + name + "&nameCheck=" + 0;
            try {
                String url = host + "/TestServer/Register";
                HttpsURLConnection conn = CustomCAHttpProvider.getConnection(this, url);

                conn.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

                writer.write(urlParameters);
                writer.flush();

                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                tvNameCheck.setText(line = reader.readLine());

                writer.close();
                reader.close();
            } catch (Exception e) {

            }
        }
        else
            tvNameCheck.setText("The Name is Invalid!");
    }
}
