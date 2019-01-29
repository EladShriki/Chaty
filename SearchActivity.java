package com.example.eladshriki.chaty;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class SearchActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    String host = MainActivity.host;

    ListView lvSrch;
    EditText etSrch;
    ArrayList<User> lu;
    Button btnSearch,btnMain;
    String username,chatUser;
    LoginSystem loginSystem = MainActivity.loginSystem;
    int place;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        lu = new ArrayList<User>();

        username = loginSystem.getUsername();
        Toast.makeText(this, username, Toast.LENGTH_SHORT).show();

        etSrch = (EditText)findViewById(R.id.etSrch);
        lvSrch = (ListView)findViewById(R.id.lvSrch);

        lvSrch.setOnItemClickListener(this);

        btnMain = (Button)findViewById(R.id.btnMain);
        btnMain.setOnClickListener(this);

        btnSearch = (Button)findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(this);

    }

    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(this,MainPageActivity.class));
    }

    @Override
    public void onClick(View view) {
        if(view==btnSearch)
        {
            String temp = etSrch.getText().toString();
            String urlParameters = "para="+temp;
            try {
                String url = host+"/TestServer/Search";
                HttpsURLConnection conn = CustomCAHttpProvider.getConnection(this,url);

                conn.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

                writer.write(urlParameters);
                writer.flush();

                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                if ((line = reader.readLine()) != null)
                {
                    chatUser = line.substring(0,line.indexOf(","));
                    line = line.substring(line.indexOf(",")+1);
                    String email = line.substring(0,line.indexOf(","));
                    line = line.substring(line.indexOf(",")+1);
                    String status = line.substring(0,line.indexOf(","));
                    line = line.substring(line.indexOf(",")+1);
                    String img = line;

                    if(chatUser.toLowerCase().equals(this.username.toLowerCase())) {
                        lu.clear();
                        UserAdapter userAdapter = new UserAdapter(this, 0, lu);
                        lvSrch.setAdapter(userAdapter);
                        Toast.makeText(this, "You can't search for yourself!", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        lu.clear();
                        if(img.equals("null"))
                            lu.add(new User(chatUser, email,status));
                        else {
                            String[] byteValues = img.substring(1, img.length() - 1).split(",");
                            byte[] imgByte = new byte[byteValues.length];

                            for (int i = 0, len = imgByte.length; i < len; i++)
                                imgByte[i] = Byte.parseByte(byteValues[i].trim());
                            lu.add(new User(chatUser, email, status, imgByte));
                        }
                        UserAdapter userAdapter = new UserAdapter(this, 0, lu);
                        lvSrch.setAdapter(userAdapter);
                        Toast.makeText(this, "Username:" + chatUser + " Email:" + email, Toast.LENGTH_SHORT).show();
                    }
                }
                else
                    Toast.makeText(this, "User not Found!", Toast.LENGTH_LONG).show();
                writer.close();
                reader.close();
            } catch (Exception e) {

            }
        }
        if(view==btnMain)
        {
            Intent intt = new Intent(this,MainPageActivity.class);
            startActivity(intt);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        place = i;
        Intent intent = new Intent(this,ChatActivity.class);
        int index = chatHistory(loginSystem.getUsername(),chatUser);

        intent.putExtra("Index",index);
        intent.putExtra("Username",chatUser);
        startActivity(intent);
    }

    public int chatHistory(String username,String chatUser)
    {
        Chat history = new Chat(chatUser);
        ChatsDB chatsDB = new ChatsDB(this);
        chatsDB.open();
        ArrayList<Message> messages = chatsDB.getAllMessages();
        chatsDB.close();

        for(int i=0;i<messages.size();i++)
        {
            if(messages.get(i).getUsername().toLowerCase().equals(username.toLowerCase()) && messages.get(i).getChatName().toLowerCase().equals(chatUser.toLowerCase()))
                history.addMessages(messages.get(i));
        }

        if(MainPageActivity.chats.contains(history))
            return MainPageActivity.chats.indexOf(history);

        return -1;
    }
}
