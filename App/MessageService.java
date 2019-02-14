package com.example.eladshriki.chaty;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Stack;

import javax.net.ssl.HttpsURLConnection;

import static com.example.eladshriki.chaty.App.CHANNEL_ID;

public class MessageService extends Service {

    ChatsDB chatsDB = new ChatsDB(this);
    private NotificationManager mNotificationManager;
    private int notificationID;
    private ArrayList<msgGroup> msgGroups;
    boolean alive = false;

    public MessageService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationID = 2;
        alive = true;
        msgGroups = new ArrayList<msgGroup>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Chaty", "Start Getting Messeges!");

        startServiceForeground();

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

    public void startServiceForeground()
    {
        Intent intent = new Intent(this,MainPageActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);

        Notification notification = new NotificationCompat.Builder(this,CHANNEL_ID)
                .setContentTitle("Chaty")
                .setContentText("Chaty is running")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setDefaults(0)
                .build();

        startForeground(1,notification);
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
                    addToList(new msgNotification(username,"Image"));
                }
                else
                {
                    Message temp = new Message(username, msg, username, MainActivity.loginSystem.getUsername(), date);
                    chatsDB.open();
                    chatsDB.createMessage(temp);
                    chatsDB.close();
                    addToList(new msgNotification(username,msg));
                }
                if (!img.equals("null")) {
                    createNotification(username);
                }
                else {
                    createNotification(username);
                }
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

    public void createSummary()
    {
        Notification summaryNotification =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle("Chaty")
                        .setContentText("You have "+msgGroups.size()+" new Messages")
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setStyle(new NotificationCompat.InboxStyle()
                                .setBigContentTitle(msgGroups.size()+ " new messages")
                                .setSummaryText("You have "+msgGroups.size()+" new Messages"))
                        .setGroup("Messages")
                        .setGroupSummary(true)
                        .build();
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = CHANNEL_ID;
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Messages",
                    NotificationManager.IMPORTANCE_LOW);
            notificationManager.createNotificationChannel(channel);
        }
        if(msgGroups.size()!=0)
            notificationManager.notify(0, summaryNotification);
    }

    public void createNotification(String sender)
    {
        msgGroup msg = getMsgGroup(sender);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.getApplicationContext(), "Messeges");

        Intent ii = new Intent(this.getApplicationContext(), ChatActivity.class);
        ii.putExtra("Username",sender);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, ii, 0);

        mBuilder.setAutoCancel(true);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.mipmap.ic_launcher_round);
        mBuilder.setLargeIcon(getCircleBitmap(getUserImg(sender)));
        mBuilder.setContentTitle(msg.getName());

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

        for(int i=0;i<msg.getMessages().size();i++)
            inboxStyle.addLine(msg.getMessages().get(i).getMsg());

        mBuilder.setStyle(inboxStyle);
        mBuilder.setGroup("Messages");

        mBuilder.setPriority(Notification.PRIORITY_MAX);

        mNotificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "Messeges";
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Messeges",
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        mNotificationManager.notify(msg.getId(), mBuilder.build());
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {

        Bitmap output;
        Rect srcRect, dstRect;
        float r;
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        if (width > height){
            output = Bitmap.createBitmap(height, height, Bitmap.Config.ARGB_8888);
            int left = (width - height) / 2;
            int right = left + height;
            srcRect = new Rect(left, 0, right, height);
            dstRect = new Rect(0, 0, height, height);
            r = height / 2;
        }else{
            output = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);
            int top = (height - width)/2;
            int bottom = top + width;
            srcRect = new Rect(0, top, width, bottom);
            dstRect = new Rect(0, 0, width, width);
            r = width / 2;
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, dstRect, paint);

        bitmap.recycle();

        return output;
    }


    public Bitmap getUserImg(String user)
    {
        Bitmap img=null;
        for(int i=0;i<MainPageActivity.chats.size();i++)
            if(MainPageActivity.chats.get(i).getChatName().toLowerCase().equals(user.toLowerCase()))
                img = MainPageActivity.chats.get(i).getProfileImg();

        Bitmap.createScaledBitmap(img,5,5,false);
        return img;
    }

    public msgGroup getMsgGroup(String sender)
    {
        for(int i=0;i<msgGroups.size();i++)
            if(msgGroups.get(i).getName().equals(sender))
                return msgGroups.get(i);
        return null;
    }

    public void addToList(msgNotification msg)
    {
        boolean added = false;
        for(int i=0;i<msgGroups.size();i++)
        {
            if(msgGroups.get(i).getName().equals(msg.getSender())) {
                msgGroups.get(i).getMessages().add(msg);
                added = true;
            }
        }
        if(!added)
        {
            msgGroups.add(new msgGroup(notificationID,msg.getSender(),msg));
            notificationID++;
        }
    }

    class Sleep extends Thread
    {
        @Override
        public void run()
        {
            while (alive)
            {
                if(MainActivity.loginSystem.getUsername()!=null) {
                    getMessage();
                    createSummary();
                }
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
