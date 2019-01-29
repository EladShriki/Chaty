package com.example.eladshriki.chaty;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener {

    String host = MainActivity.host;

    EditText etChat;
    Button btnSend, btnPic;
    ArrayList<Message> messages;
    String sender;
    String reciver;
    Activity context;
    ListView lvMsg;
    MessageAdapter messageAdapter;
    Sleep thread;
    LoginSystem loginSystem = MainActivity.loginSystem;
    ChatsDB chatsDB;
    Uri imgURI;
    Boolean intentCamera = false;
    Chat chat;

    private static final String IMAGE_DIRECTORY = "/Chaty";
    private int GALLERY = 1, CAMERA = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatsDB = new ChatsDB(this);

        sender = loginSystem.getUsername();
        reciver = getIntent().getExtras().getString("Username");
        int index = getIntent().getExtras().getInt("Index");

        if(index!=-1)
            chat = (Chat) MainPageActivity.chats.get(index);

        if(chat!=null)
            messages = chat.getMessages();
        else
            messages = new ArrayList<Message>();

        MessageAdapter messageAdapter = new MessageAdapter(this,0,messages);

        context = (Activity)this;

        etChat = (EditText)findViewById(R.id.etChat);

        btnSend = (Button)findViewById(R.id.btnSend);
        btnPic = (Button)findViewById(R.id.btnPic);
        btnPic.setOnClickListener(this);
        btnSend.setOnClickListener(this);

        lvMsg = (ListView) findViewById(R.id.lvMsg);
        lvMsg.setAdapter(messageAdapter);

        thread = new Sleep();
        thread.start();

    }

    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(this,MainPageActivity.class));
    }

    @Override
    protected  void onStop()
    {
        super.onStop();
        if(!intentCamera)
            thread.kill();
        intentCamera = false;
    }

    @Override
    public void onClick(View view) {
        if(view==btnSend)
        {
            if(!etChat.getText().toString().isEmpty())
            {
                String msg = etChat.getText().toString();
                sendMessage(msg);
                etChat.setText("");
            }
            else
                Toast.makeText(context, "Write Something!", Toast.LENGTH_SHORT).show();
        }
        if(view==btnPic)
        {
            showPictureDialog();
        }
    }

    private void showPictureDialog(){
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select picture options:");
        String[] pictureDialogItems = {
                "Gallery",
                "Camera" };
        pictureDialog.setItems(pictureDialogItems,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                choosePhotoFromGallery();
                                break;
                            case 1:
                                takePhotoFromCamera();
                                break;
                        }
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intentCamera = true;
        startActivityForResult(galleryIntent, GALLERY);
    }

    public void takePhotoFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
            intentCamera = true;
            File photo = null;
            try {
                photo = this.createTemporaryFile("pic",".jpg");
                photo.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
            imgURI = Uri.fromFile(photo);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,imgURI);
            startActivityForResult(takePictureIntent, CAMERA);
        }
    }

    private File createTemporaryFile(String part, String ext) throws Exception
    {
        File tempDir= Environment.getExternalStorageDirectory();
        tempDir=new File(tempDir.getAbsolutePath()+"/.temp/");
        if(!tempDir.exists())
        {
            tempDir.mkdirs();
        }
        return File.createTempFile(part, ext, tempDir);
    }

    public Bitmap grabImage()
    {
        //this.getContentResolver().notifyChange(imgURI, null);
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap;
        try
        {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, imgURI);
            return bitmap;
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
            Log.d("ERROR", "Failed to load", e);
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();

                Bitmap bitmap = null;
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                byte[] bytes = getBitmapAsByteArray(bitmap);
                sendImg(bytes);

            }

        } else if (requestCode == CAMERA)
        {
            Bitmap bitmap = grabImage();
            String path;

            if(isStoragePermissionGranted()) {
                path = saveImage(bitmap);

                Bitmap img = BitmapFactory.decodeFile(path);

                byte[] bytes = getBitmapAsByteArray(img);
                sendImg(bytes);
            }
            else
                Toast.makeText(context, "No Storage!", Toast.LENGTH_SHORT).show();
        }
    }

    public String saveImage(Bitmap myBitmap)
    {
        boolean sideWay = false;
        if(myBitmap.getHeight() < myBitmap.getWidth())
            sideWay = true;
        else
            sideWay = false;

        if (sideWay) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            myBitmap = Bitmap.createBitmap(myBitmap, 0, 0, myBitmap.getWidth(),
                    myBitmap.getHeight(), matrix, true);
        }

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        String fname = new Date() +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            Log.e("Error",e.getMessage());
        }
        return file.getAbsolutePath();
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("Storage","Permission is granted");
                return true;
            } else {

                Log.v("Storage","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("Storage","Permission is granted");
            return true;
        }
    }

    public static byte[] getBitmapAsByteArray(Bitmap bitmap)
    {
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50 /*ignored for PNG*/, blob);
        byte[] bitmapdata = blob.toByteArray();
        return bitmapdata;
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, outputStream);
//        byte[] temp = outputStream.toByteArray();
//        return temp;
    }

    public void sendImg(byte[] bytes)
    {
        Message temp = new Message(sender,bytes,reciver,loginSystem.getUsername());
        String urlParameters = "sender=" + temp.getSender() + "&reciver=" + reciver + "&msg=" + temp.getText() + "&chatName=" + temp.getChatName() +
                "&date=" + temp.getDate() + "&Send=" + 1 + "&Img=" + 1+ "&imgByte=" + temp.getStringImg();
        try {
            String url = host+"/TestServer/Chat";
            HttpsURLConnection conn = CustomCAHttpProvider.getConnection(this,url);

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

            messages.add(temp);
            chatsDB.open();
            chatsDB.createMessage(temp);
            chatsDB.close();

            messageAdapter = new MessageAdapter(context, 0, messages);


            lvMsg.post(new Runnable() {
                @Override
                public void run() {
                    lvMsg.setAdapter(messageAdapter);
                    lvMsg.setSelection(messages.size());
                }
            });

        } catch (Exception e) {
            Log.e("Chaty",e.getMessage());
        }
    }

    public void sendMessage(String msg)
    {
        Message temp = new Message(sender,msg,reciver,loginSystem.getUsername());
        String urlParameters = "sender=" + temp.getSender() + "&reciver=" + reciver + "&msg=" + temp.getText() + "&chatName=" + temp.getChatName() +
                "&date=" + temp.getDate() +"&Send=" + 1 + "&Img=" + 0;
        try {
            String url = host+"/TestServer/Chat";
            HttpsURLConnection conn = CustomCAHttpProvider.getConnection(this,url);

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

            messages.add(temp);
            chatsDB.open();
            chatsDB.createMessage(temp);
            chatsDB.close();

            messageAdapter = new MessageAdapter(context, 0, messages);


            lvMsg.post(new Runnable() {
                @Override
                public void run() {
                    lvMsg.setAdapter(messageAdapter);
                    lvMsg.setSelection(messages.size());
                }
            });

        } catch (Exception e) {
            Log.e("Chaty",e.getMessage());
        }
    }

    public void getMessage() {
        boolean getMessage = false;
        String urlParameters = "sender="+sender+"&reciver="+reciver + "&Send=" + 0 + "&Img="+0;
        try {
            String url = host+"/TestServer/Chat";
            HttpsURLConnection conn = CustomCAHttpProvider.getConnection(this,url);

            conn.setDoOutput(true);

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream());

            writer.write(urlParameters);
            writer.flush();

            String line;
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String username=null,msg=null;


            while ((line = reader.readLine()) != null)
            {
                getMessage = true;

                username = line.substring(0,line.indexOf(","));
                line = line.substring(line.indexOf(",")+1);
                msg = line.substring(0,line.indexOf(","));
                line = line.substring(line.indexOf(",")+1);
                String date = line.substring(0,line.indexOf(","));
                line = line.substring(line.indexOf(",")+1);
                String img = line;
                if(!img.equals("null")) {
                    String[] byteValues = img.substring(1, img.length() - 1).split(",");
                    byte[] imgByte = new byte[byteValues.length];

                    for (int i=0, len=imgByte.length; i<len; i++)
                        imgByte[i] = Byte.parseByte(byteValues[i].trim());


                    Message temp = new Message(username, imgByte, reciver, loginSystem.getUsername(), date);
                    messages.add(temp);
                    chatsDB.open();
                    chatsDB.createMessage(temp);
                    chatsDB.close();
                }
                else {
                    Message temp = new Message(username, msg, reciver, loginSystem.getUsername(), date);
                    messages.add(temp);
                    chatsDB.open();
                    chatsDB.createMessage(temp);
                    chatsDB.close();
                }
            }
            writer.close();
            reader.close();

            if(getMessage) {
                messageAdapter = new MessageAdapter(context, 0, messages);

                lvMsg.post(new Runnable() {
                    @Override
                    public void run() {
                        lvMsg.setAdapter(messageAdapter);
                        lvMsg.setSelection(messages.size());
                    }
                });
            }
        } catch (Exception e) {
            Log.i("Error",e.getMessage().toString());
        }
    }

    class Sleep extends Thread
    {
        private boolean alive = true;

        public void kill()
        {
            alive = false;
        }


        @Override
        public void run()
        {
            while (alive) {
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