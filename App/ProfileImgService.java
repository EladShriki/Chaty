package com.example.eladshriki.chaty;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.Properties;

import javax.net.ssl.HttpsURLConnection;

public class ProfileImgService extends Service
{
    Context context = this;

    public ProfileImgService() {
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        for(int i=0;i<MainPageActivity.chats.size();i++)
            getProfileImg(MainPageActivity.chats.get(i).getChatName(),i);

        sendOutBoradcast();
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    public void updateImgDate(String name,long imgDate)
    {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Chaty/profile_Img");
        myDir.mkdirs();
        String fname = "imgDates.date";
        File file = new File (myDir, fname);
        if(file.exists()) {
            try {
                FileInputStream in = new FileInputStream(file);
                Properties properties = new Properties();
                properties.load(in);
                in.close();
                properties.setProperty(name,Long.toString(imgDate));
                FileOutputStream out = new FileOutputStream(file);
                properties.store(out,null);
                out.close();
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
        }
        else
        {
            try {
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);
                Properties properties = new Properties();
                properties.setProperty(name,Long.toString(imgDate));
                properties.store(out,null);
                out.close();
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
        }
    }

    public long getImgDate(String name)
    {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Chaty/profile_Img");
        myDir.mkdirs();
        String fname = "imgDates.date";
        File file = new File (myDir, fname);
        if(file.exists()) {
            try {
                FileInputStream in = new FileInputStream(file);
                Properties properties = new Properties();
                properties.load(in);
                in.close();
                return Long.parseLong(properties.getProperty(name,Long.toString(new Date().getTime())));
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
            }
        }
        return new Date().getTime();
    }

    public void getProfileImg(String name,int pos)
    {
        String status=null,image=null;
        long imgDate = getImgDate(name);
        String urlParameters = "username="+name+"&changeStatus=0&changeImage=0"+"&dateImg="+imgDate;
        try {
            String url = MainActivity.host+"/TestServer/Profile";
            HttpsURLConnection conn = CustomCAHttpProvider.getConnection(this,url);

            conn.setDoOutput(true);

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

            writer.write(urlParameters);
            writer.flush();

            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            if ((line = reader.readLine()) != null)
            {
                status = line.substring(0,line.indexOf(','));
                line = line.substring(line.indexOf(',')+1);
                imgDate = Long.parseLong(line.substring(0,line.indexOf(',')));
                image = line.substring(line.indexOf(',')+1);
            }

            writer.close();
            reader.close();
        } catch (Exception e) {
            Log.i("Chaty",e.getMessage());
        }
        if(image!=null)
        {
            if (!image.equals("null")) {
                String[] byteValues = image.substring(1, image.length() - 1).split(",");
                byte[] imgByte = new byte[byteValues.length];

                for (int i = 0, len = imgByte.length; i < len; i++)
                    imgByte[i] = Byte.parseByte(byteValues[i].trim());

                updateImgDate(name,imgDate);

                Bitmap img = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);

                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/Chaty/profile_Img");
                myDir.mkdirs();
                String fname = name+".jpg";
                File file = new File (myDir, fname);
                if (file.exists ()) file.delete ();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    img.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();

                } catch (Exception e) {
                    Log.e("Error",e.getMessage());
                }
                MainPageActivity.chats.get(pos).setProfileImg(img);
            }
        }
    }

    public void sendOutBoradcast()
    {
        Intent i = new Intent();
        i.setAction("newImg");
        i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(i);
    }
}
