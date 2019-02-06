package com.example.eladshriki.chaty;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

;

public class MainPageActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    LoginSystem loginSystem = MainActivity.loginSystem;
    static ArrayList<Chat> chats;
    ArrayList<Message> messages;
    ListView lvChats;
    ChatsDB chatsDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        chats = new ArrayList<Chat>();

        chatsDB = new ChatsDB(this);

        chatsDB.open();

        messages = chatsDB.getAllMessages();

        chatsDB.close();

        lvChats = (ListView) findViewById(R.id.lvChats);
        lvChats.setOnItemClickListener(this);

        sortMessages(messages);

        Toast.makeText(this, loginSystem.getUsername(), Toast.LENGTH_SHORT).show();

        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        if (!isMyServiceRunning(MessageService.class))
        {
            Intent i = new Intent(this, MessageService.class);
            startService(i);
        }
    }

    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are u want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                loginSystem.logout();
                startActivity(new Intent(getBaseContext(),MainActivity.class));
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //startActivity(new Intent(loginSystem.getContext(),MainPageActivity.class));
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_Profile:
                startActivity(new Intent(this,ProfileActivity.class));
                return true;
            case R.id.action_Search:
                startActivity(new Intent(this,SearchActivity.class));
                return true;
            case R.id.action_Logout:
                Intent intent = loginSystem.logout();
                if(intent!=null)
                    startActivity(intent);
                return true;
            case R.id.action_DelHistory:
                chatsDB.open();
                chatsDB.deleteTable();
                chatsDB.onCreate(chatsDB.database);
                messages = chatsDB.getAllMessages();
                chatsDB.close();
                sortMessages(messages);
                return true;
            case R.id.action_History:
                syncHistory(loginSystem.getUsername());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void sortMessages(ArrayList<Message> messages)
    {
        boolean found = false;
        chats = new ArrayList<Chat>();
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getUsername().toLowerCase().equals(loginSystem.getUsername().toLowerCase())) {
                found = false;
                for (int j = 0; j < chats.size(); j++) {
                    if (chats.get(j).getChatName().toLowerCase().equals(messages.get(i).getChatName().toLowerCase())) {
                        found = true;
                        chats.get(j).addMessages(messages.get(i));
                        break;
                    }
                }
                if (!found) {
                    chats.add(new Chat(messages.get(i).getChatName(), messages.get(i)));
                }
            }
        }
        final ChatAdapter chatAdapter = new ChatAdapter(this, 0, chats);
        lvChats.post(new Runnable() {
            @Override
            public void run() {
                lvChats.setAdapter(chatAdapter);
            }
        });
    }

    public void syncHistory(String username)
    {
        Message temp;
        String urlParameters = "username=" + username;
        try {
            String url = MainActivity.host + "/TestServer/History";
            HttpsURLConnection conn = CustomCAHttpProvider.getConnection(this,url);

            conn.setDoOutput(true);

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

            writer.write(urlParameters);
            writer.flush();

            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            ChatsDB chatsDB = new ChatsDB(this);
            chatsDB.open();

            while ((line = reader.readLine()) != null)
            {
                String chatName = line.substring(0,line.indexOf(","));
                line = line.substring(line.indexOf(",")+1);
                String tempUser = line.substring(0,line.indexOf(","));
                line = line.substring(line.indexOf(",")+1);
                String sender = line.substring(0,line.indexOf(","));
                line = line.substring(line.indexOf(",")+1);
                String text = line.substring(0,line.indexOf(","));
                line = line.substring(line.indexOf(",")+1);
                String date = line.substring(0,line.indexOf(","));
                line = line.substring(line.indexOf(",")+1);
                String img = line;

                if(!img.equals("null")) {
                    String[] byteValues = img.substring(1, img.length() - 1).split(",");
                    byte[] imgBytes = new byte[byteValues.length];

                    for (int i = 0, len = imgBytes.length; i < len; i++)
                        imgBytes[i] = Byte.parseByte(byteValues[i].trim());
                    temp = new Message(chatName,tempUser,sender,text,date,imgBytes);
                }
                else
                    temp = new Message(sender,text,chatName,username,date);


                if(!chatsDB.isMsgExsist(temp))
                    chatsDB.createMessage(temp);
            }

            chatsDB.close();
            writer.close();
            reader.close();

        } catch (Exception e) {
            Log.e("Chaty",e.getMessage());
        }
        chatsDB.open();
        messages = chatsDB.getAllMessages();
        chatsDB.close();
        sortMessages(messages);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(this,ChatActivity.class);
        intent.putExtra("Username",chats.get(i).getChatName());
        intent.putExtra("Index",i);
        startActivity(intent);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(this.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
