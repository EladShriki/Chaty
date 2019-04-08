package com.example.eladshriki.chaty;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

;import static com.example.eladshriki.chaty.MainActivity.host;

public class MainPageActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    imgRefresh imfRefreshReceiver = null;
    Boolean imgReceiverIsRegistered = false;

    TextView tvProg,tvLoading,tvEmpty;
    ProgressBar progressBar;
    android.support.v7.app.AlertDialog dialog;

    Toolbar toolbar;

    static UserAdapter userAdapter;

    static Context context;

    LoginSystem loginSystem = MainActivity.loginSystem;
    static ArrayList<Chat> chats;
    ArrayList<Message> messages;

    ArrayList<String> chatsName;

    ChatAdapter chatAdapter;
    static ListView lvChats;
    ChatsDB chatsDB;
    Intent service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        context = this;

        imfRefreshReceiver = new imgRefresh();

        chats = new ArrayList<Chat>();

        chatsDB = new ChatsDB(this);

        tvEmpty = (TextView)findViewById(R.id.tvEmpty);
        tvLoading = (TextView)findViewById(R.id.tvLoading);

        lvChats = (ListView) findViewById(R.id.lvChats);
        lvChats.setEmptyView(findViewById(R.id.tvLoading));
        lvChats.setOnItemClickListener(this);

//        new Thread()
//        {
//            public void run()
//            {
//                chatsDB.open();
//
//                messages = chatsDB.getAllMessages();
//
//                chatsDB.close();
//
//                sortMessages(messages);
//
//                lvChats.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        lvChats.setEmptyView(findViewById(R.id.tvEmpty));
//                    }
//                });
//                tvLoading.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        tvLoading.setAlpha(0);
//                    }
//                });
//                tvEmpty.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        tvEmpty.setAlpha(1);
//                    }
//                });
//            }
//        }.start();

        new Thread()
        {
            public void run()
            {
                chatsDB.open();

                chatsName = chatsDB.getAllChats(MainActivity.loginSystem.getUsername());

                chatsDB.close();

                makeChats(chatsName);

                lvChats.post(new Runnable() {
                    @Override
                    public void run() {
                        lvChats.setEmptyView(findViewById(R.id.tvEmpty));
                    }
                });
                tvLoading.post(new Runnable() {
                    @Override
                    public void run() {
                        tvLoading.setAlpha(0);
                    }
                });
                tvEmpty.post(new Runnable() {
                    @Override
                    public void run() {
                        tvEmpty.setAlpha(1);
                    }
                });
            }
        }.start();

        Toast.makeText(this, loginSystem.getUsername(), Toast.LENGTH_SHORT).show();

        toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        Intent imgs = new Intent(this,ProfileImgService.class);
        startService(imgs);

        service = new Intent(this, MessageService.class);
        startService(service);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!imgReceiverIsRegistered) {
            registerReceiver(imfRefreshReceiver, new IntentFilter("newImg"));
            imgReceiverIsRegistered = true;
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (imgReceiverIsRegistered) {
            unregisterReceiver(imfRefreshReceiver);
            imgReceiverIsRegistered = false;
        }
    }

    @Override
    public void onBackPressed()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are u want to logout?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                stopService(service);
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

        menu.findItem(R.id.action_Chats).setVisible(false);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s)
            {
                final String newText = s;
                lvChats.post(new Runnable() {
                    @Override
                    public void run() {
                        chatAdapter.getFilter().filter(newText);
                    }
                });
                return true;
            }
        });

        searchView.setQueryHint("Search");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_Profile:
                startActivity(new Intent(this,ProfileActivity.class));
                return true;
            case R.id.action_Findpeople:
                findPeople();
                return true;
            case R.id.action_Chats:
                backToChats();
                return true;
            case R.id.action_Logout:
                stopService(service);
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
                ArrayList<Integer> ids = historyUpdate();
                if(ids.size()!=0)
                    historyProgress(ids);
                else
                    Toast.makeText(context, "History is already updated!", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void backToChats()
    {
        Menu menu = toolbar.getMenu();
        menu.findItem(R.id.action_Findpeople).setVisible(true);
        menu.findItem(R.id.action_Chats).setVisible(false);

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }

        lvChats.post(new Runnable() {
            @Override
            public void run() {
                lvChats.setAdapter(chatAdapter);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s)
            {
                final String newText = s;
                lvChats.post(new Runnable() {
                    @Override
                    public void run() {
                        chatAdapter.getFilter().filter(newText);
                    }
                });
                return true;
            }
        });

        searchView.setQueryHint("Search");
    }

    public void findPeople()
    {
        Menu menu = toolbar.getMenu();
        menu.findItem(R.id.action_Findpeople).setVisible(false);
        menu.findItem(R.id.action_Chats).setVisible(true);

        new Thread()
        {
            @Override
            public void run() {
                search4People();
            }
        }.start();

        lvChats.post(new Runnable() {
            @Override
            public void run() {
                lvChats.setAdapter(userAdapter);
            }
        });

        MenuItem searchItem = menu.findItem(R.id.action_search);

        SearchView searchView = null;
        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s)
            {

                final String newText = s;
//                new Thread(){
//                    @Override
//                    public void run() {
//                        search4People(newText);
//                    }
//                }.start();
                lvChats.post(new Runnable() {
                    @Override
                    public void run() {
                        userAdapter.getFilter().filter(newText);
                    }
                });

                return true;
            }
        });

        searchView.setQueryHint("Search");
    }

    public void search4People()
    {
        ArrayList<User> users = new ArrayList<>();
        String urlParameters = "para="+MainActivity.loginSystem.getUsername();
        try {
            String url = host+"/TestServer/Search";
            HttpsURLConnection conn = CustomCAHttpProvider.getConnection(this,url);

            conn.setDoOutput(true);

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

            writer.write(urlParameters);
            writer.flush();

            String line,chatUser;
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((line = reader.readLine()) != null)
            {
                chatUser = line.substring(0,line.indexOf(","));
                line = line.substring(line.indexOf(",")+1);
                String email = line.substring(0,line.indexOf(","));
                line = line.substring(line.indexOf(",")+1);
                String status = line.substring(0,line.indexOf(","));
                line = line.substring(line.indexOf(",")+1);
                String img = line;


                if(img.equals("null")) {
                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.user);
                    users.add(new User(chatUser, email, status,bitmap));
                }
                else {
                    String[] byteValues = img.substring(1, img.length() - 1).split(",");
                    byte[] imgByte = new byte[byteValues.length];

                    for (int i = 0, len = imgByte.length; i < len; i++)
                        imgByte[i] = Byte.parseByte(byteValues[i].trim());
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
                    users.add(new User(chatUser, email, status, bitmap));
                }
                userAdapter = new UserAdapter(context,0,users);
                lvChats.post(new Runnable() {
                    @Override
                    public void run() {
                        lvChats.setAdapter(userAdapter);
                    }
                });
            }
//            else
//            {
//                users.clear();
//                userAdapter = new UserAdapter(context,0,users);
//                lvChats.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        lvChats.setAdapter(userAdapter);
//                    }
//                });
//            }
            writer.close();
            reader.close();
        } catch (Exception e) {

        }
    }

    public ArrayList<Integer> historyUpdate()
    {
        ArrayList<Integer> idList = new ArrayList<Integer>();
        String urlParameters = "msgNum=" + 3+"&username=" + MainActivity.loginSystem.getUsername();
        try {
            String url = host + "/TestServer/History";
            HttpsURLConnection conn = CustomCAHttpProvider.getConnection(this,url);

            conn.setDoOutput(true);

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

            writer.write(urlParameters);
            writer.flush();

            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            while ((line = reader.readLine()) != null)
            {
                idList.add(Integer.parseInt(line));
            }

            ChatsDB chatsDB = new ChatsDB(this);
            chatsDB.open();
            ArrayList<Integer> tempList = chatsDB.getAllIdByName(loginSystem.getUsername());
            chatsDB.close();
            writer.close();
            reader.close();

            idList.removeAll(tempList);

        } catch (Exception e) {
            Log.e("Chaty",e.getMessage());
        }
        return idList;
    }

    public void historyProgress(ArrayList<Integer> idList)
    {
        android.support.v7.app.AlertDialog.Builder mBuilder = new android.support.v7.app.AlertDialog.Builder(this);
        View view1 = getLayoutInflater().inflate(R.layout.history_dialog,null);
        tvProg = (TextView) view1.findViewById(R.id.tvProgress);
        progressBar = (ProgressBar)view1.findViewById(R.id.progressBar);

        Button btnCancel = (Button)view1.findViewById(R.id.btnCancel);

        mBuilder.setView(view1);
        dialog = mBuilder.create();

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                dialog.dismiss();
            }
        });

        dialog.show();

        Thread thread = new historyThread(this,loginSystem.getUsername(),idList);
        thread.start();
    }

    public void makeChats(ArrayList<String> chatsName)
    {
        for(int i=0;i<chatsName.size();i++)
            chats.add(new Chat(chatsName.get(i), getResizedBitmap(getSavedImg(chatsName.get(i)),350)));
        chatAdapter = new ChatAdapter(this, 0, chats);
        lvChats.post(new Runnable() {
            @Override
            public void run() {
                lvChats.setAdapter(chatAdapter);
            }
        });
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
                        break;
                    }
                }
                if (!found) {
                    chats.add(new Chat(messages.get(i).getChatName(), getResizedBitmap(getSavedImg(messages.get(i).getChatName()),350)));
                }
            }
        }
        chatAdapter = new ChatAdapter(this, 0, chats);
        lvChats.post(new Runnable() {
            @Override
            public void run() {
                lvChats.setAdapter(chatAdapter);
            }
        });
    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    public Bitmap getSavedImg(String name) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Chaty/profile_Img");
        myDir.mkdirs();
        String fname = name + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) {
            Bitmap img = BitmapFactory.decodeFile(file.getAbsolutePath());
            return img;
        }
        Bitmap img = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.user);
        return img;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Intent intent = new Intent(this,ChatActivity.class);
        if(lvChats.getAdapter().equals(chatAdapter)) {
            intent.putExtra("Username", chats.get(i).getChatName());
            intent.putExtra("pos", i);
        }
        else
        {
            intent.putExtra("Username", ((Chat)userAdapter.getItem(i)).getChatName());
            intent.putExtra("type","user");
            intent.putExtra("pos", i);
        }
        startActivity(intent);
    }

    class imgRefresh extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, Intent intent)
        {
            lvChats.post(new Runnable() {
                @Override
                public void run() {
                    chatAdapter =new ChatAdapter(context,0,chats);
                    lvChats.setAdapter(chatAdapter);
                }
            });
        }
    }

    class historyThread extends Thread
    {
        private ArrayList<Integer> idList;
        private int allMsg;
        private int msgCnt;
        private Context context;
        private String username;

        public historyThread(Context context, String username,ArrayList<Integer> idList)
        {
            this.idList = idList;
            this.context = context;
            this.username = username;
            this.msgCnt = 0;
            this.allMsg = idList.size();
        }

        @Override
        public void run()
        {
            progressBar.post(new Runnable() {
                @Override
                public void run() {
                    progressBar.setMax(idList.size());
                }
            });
            //getAllmsgNum();
            getHistory();
            chatsDB.open();
            messages = chatsDB.getAllMessages();
            chatsDB.close();
            sortMessages(messages);
            dialog.dismiss();
        }

        public String listToString()
        {
            String ids = "";
            for(int i : this.idList)
            {
                ids += i+",";
            }
            ids = ids.substring(0,ids.lastIndexOf(","));
            return ids;
        }

        public void getHistory()
        {
            Message msg;
            String urlParameters = "msgNum=" + 0+"&username="+username+"&idList="+listToString();
            try {
                String url = host + "/TestServer/History";
                HttpsURLConnection conn = CustomCAHttpProvider.getConnection(context, url);

                conn.setDoOutput(true);

                OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

                writer.write(urlParameters);
                writer.flush();

                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                ChatsDB chatsDB = new ChatsDB(context);
                chatsDB.open();

                while ((line = reader.readLine()) != null)
                {
                    int id = Integer.parseInt(line.substring(0, line.indexOf(",")));
                    line = line.substring(line.indexOf(",") + 1);
                    String chatName = line.substring(0, line.indexOf(","));
                    line = line.substring(line.indexOf(",") + 1);
                    String tempUser = line.substring(0, line.indexOf(","));
                    line = line.substring(line.indexOf(",") + 1);
                    String sender = line.substring(0, line.indexOf(","));
                    line = line.substring(line.indexOf(",") + 1);
                    String text = line.substring(0, line.indexOf(","));
                    line = line.substring(line.indexOf(",") + 1);
                    String date = line.substring(0, line.indexOf(","));
                    line = line.substring(line.indexOf(",") + 1);
                    String img = line;

                    if (!img.equals("null")) {
                        String[] byteValues = img.substring(1, img.length() - 1).split(",");
                        byte[] imgBytes = new byte[byteValues.length];

                        for (int i = 0, len = imgBytes.length; i < len; i++)
                            imgBytes[i] = Byte.parseByte(byteValues[i].trim());
                        msg = new Message(id,chatName, tempUser, sender, text, date, imgBytes);
                    } else
                        msg = new Message(id,sender, text, chatName, username, date);


                    chatsDB.createMessage(msg);
                    msgCnt++;

                    progressBar.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(msgCnt);
                        }
                    });
                    tvProg.post(new Runnable() {
                        @Override
                        public void run()
                        {
                            double progress = (double) msgCnt/allMsg*100;
                            tvProg.setText((int)progress+"%");
                        }
                    });
                }

                chatsDB.close();
                writer.close();
                reader.close();

            } catch (Exception e) {
                Log.e("Chaty", e.getMessage());
            }
        }
    }
}
