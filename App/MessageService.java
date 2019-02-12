package com.example.eladshriki.chaty;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import javax.net.ssl.HttpsURLConnection;

public class MessageService extends Service {

    ChatsDB chatsDB = new ChatsDB(this);
    private NotificationManager mNotificationManager;
    boolean alive = false;

    public MessageService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Chaty", "Start Getting Messeges!");
        alive = true;
        Thread thread = new Sleep();;
        thread.start();

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        alive = false;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void getMessage() {
        String urlParameters = "username=" + MainActivity.loginSystem.getUsername() + "&Send=" + 0 + "&Img=" + 0;
        try {
            String url = MainActivity.host + "/TestServer/Chat";
            HttpsURLConnection conn = CustomCAHttpProvider.getConnection(this, url);

            conn.setDoOutput(true);

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

            writer.write(urlParameters);
            writer.flush();

            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String username = null, msg = null;


            while ((line = reader.readLine()) != null) {

                username = line.substring(0, line.indexOf(","));
                line = line.substring(line.indexOf(",") + 1);
                msg = line.substring(0, line.indexOf(","));
                line = line.substring(line.indexOf(",") + 1);
                String date = line.substring(0, line.indexOf(","));
                line = line.substring(line.indexOf(",") + 1);
                String img = line;
                if (!img.equals("null"))
                {
                    String[] byteValues = img.substring(1, img.length() - 1).split(",");
                    byte[] imgByte = new byte[byteValues.length];

                    for (int i = 0, len = imgByte.length; i < len; i++)
                        imgByte[i] = Byte.parseByte(byteValues[i].trim());


                    Message temp = new Message(username, imgByte, username, MainActivity.loginSystem.getUsername(), date);
                    chatsDB.open();
                    chatsDB.createMessage(temp);
                    chatsDB.close();
                }
                else
                {
                    Message temp = new Message(username, msg, username, MainActivity.loginSystem.getUsername(), date);
                    chatsDB.open();
                    chatsDB.createMessage(temp);
                    chatsDB.close();
                }
                createNotification(username);
                sendOutBoradcast();
            }
            writer.close();
            reader.close();



        } catch (Exception e) {
            Log.i("Error", e.getMessage().toString());
        }
    }

    public void sendOutBoradcast()
    {
        Intent i = new Intent();
        i.setAction("com.example.eladshriki.chaty");
        i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(i);
    }

    public void createNotification(String sender)
    {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.getApplicationContext(), "notify_001");
        mBuilder.setAutoCancel(true);
        Intent ii = new Intent(this.getApplicationContext(), MainPageActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, ii, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText("New Message from "+ sender);
        bigText.setBigContentTitle("Chaty");
        //bigText.setSummaryText("Text in detail");

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setContentTitle("Chaty");
        mBuilder.setContentText("New Message from "+ sender);
        mBuilder.setPriority(Notification.PRIORITY_MAX);
//        mBuilder.setStyle(bigText);

        mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "YOUR_CHANNEL_ID";
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        mNotificationManager.notify(0, mBuilder.build());
    }

    class Sleep extends Thread
    {
        @Override
        public void run()
        {
            while (alive)
            {
                if(MainActivity.loginSystem.getUsername()!=null)
                    getMessage();
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
