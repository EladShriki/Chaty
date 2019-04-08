package com.example.eladshriki.chaty;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    String host = MainActivity.host;

    EditText etRUser,etRPass,etREmail;
    Button btnSub;
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

        btnSub.setOnClickListener(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.registerToolBar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(this,MainActivity.class));
    }

    @Override
    public void onClick(View view) {

        if(view==btnSub)
        {
            if(nameCheck())
            {
                tvNameCheck.setText("");
                String username = etRUser.getText().toString();
                String password = etRPass.getText().toString();
                String eMail = etREmail.getText().toString();

                if (goodPassword(password)) {
                    if (realEmail(eMail)) {
                        try {
                            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                            messageDigest.update(password.getBytes());
                            String hashPass = bytesToHex(messageDigest.digest());

                            String urlParameters = "username=" + username + "&password=" + hashPass + "&Email=" + eMail + "&nameCheck=" + 1;
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
                            Toast.makeText(this, "User Created!", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(this, MainActivity.class));
                        } catch (Exception e) {
                        }
                    } else
                        Toast.makeText(this, "Use Gmail mail please!", Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(this, "Invalid password!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
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

    public boolean nameCheck()
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

                line = reader.readLine();


                writer.close();
                reader.close();

                if(line.equals("Username in use!"))
                    return false;
                else
                    return true;
            } catch (Exception e) {

            }
        }
        else
            tvNameCheck.setText("The Name is Invalid!");
        return false;
    }
}
