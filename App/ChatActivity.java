package com.example.eladshriki.chaty;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

    RefreshListReciver refreshListReciver = null;
    Boolean myReceiverIsRegistered = false;

    int pos;
    EditText etChat;
    ImageButton btnSend, btnPic;
    ArrayList<Message> messages;
    String sender;
    String reciver;
    Activity context;
    ListView lvMsg;
    MessageAdapter messageAdapter;
    LoginSystem loginSystem = MainActivity.loginSystem;
    int notificationID;
    ChatsDB chatsDB;
    Uri imgURI;
    Boolean intentCamera = false;
    private int GALLERY = 1, CAMERA = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        refreshListReciver = new RefreshListReciver();

        chatsDB = new ChatsDB(this);


        sender = loginSystem.getUsername();

        notificationID = getIntent().getIntExtra("int_id",-1);

        reciver = getIntent().getStringExtra("Username");

        String type = null;
        try {
            type = getIntent().getStringExtra("type");
        }
        catch (Exception e)
        {

        }

        pos = getIntent().getIntExtra("pos",-1);
        if(pos==-1)
        {
            for(int i=0;i<MainPageActivity.chats.size();i++)
                if(MainPageActivity.chats.get(i).getChatName().equals(reciver)) {
                    pos = i;
                    break;
                }
        }

        cancelNotifications(notificationID);

        //int index = getIntent().getExtras().getInt("Index");

//        if(index!=-1)
//          chat = (Chat) MainPageActivity.chats.get(index);
//
//        if(chat!=null) {
//            chatsDB.open();
//            messages = chatsDB.getAllMessagesByName(reciver);
//            chatsDB.close();
//        }
//        else
//            messages = new ArrayList<Message>();

        messages = new ArrayList<Message>();

        messageAdapter = new MessageAdapter(this,0,messages);

        context = (Activity)this;

        etChat = (EditText)findViewById(R.id.etChat);

        btnSend = (ImageButton)findViewById(R.id.btnSend);
        btnPic = (ImageButton) findViewById(R.id.btnPic);
        btnPic.setOnClickListener(this);
        btnSend.setOnClickListener(this);

        lvMsg = (ListView) findViewById(R.id.lvMsg);
        lvMsg.setEmptyView(findViewById(R.id.tvLoadMsg));
        lvMsg.setAdapter(messageAdapter);
        lvMsg.setSelection(messages.size());

        new Thread()
        {
            @Override
            public void run() {
                refreshList();
            }
        }.start();

        Toolbar toolbar = (Toolbar) findViewById(R.id.ChatToolBar);
        setSupportActionBar(toolbar);

        Bitmap img=null;

        if(type!=null) {
            if (type.equals("user")) {
                img = Bitmap.createScaledBitmap(((Chat) MainPageActivity.userAdapter.getItem(pos)).getProfileImg(), 125, 125, false);
                img = getCircleBitmap(img);
            }
        }
        else {
            ProfileImgService profileImgService = new ProfileImgService();
            profileImgService.getProfileImg(reciver, pos);

            img = Bitmap.createScaledBitmap(MainPageActivity.chats.get(pos).getProfileImg(), 125, 125, false);
            img = getCircleBitmap(img);
        }
        if(img!=null) {
            Drawable profile = new BitmapDrawable(getResources(), img);

            getSupportActionBar().setLogo(profile);
        }
        getSupportActionBar().setTitle("  "+reciver);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

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

        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }

        return output;
    }

    public void cancelNotifications(int id)
    {
        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
        for(int i=0;i<MessageService.msgGroups.size();i++)
            if(MessageService.msgGroups.get(i).getId()==id)
                MessageService.msgGroups.get(i).deleteAllMsgs();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!myReceiverIsRegistered) {
            registerReceiver(refreshListReciver, new IntentFilter("com.example.eladshriki.chaty"));
            myReceiverIsRegistered = true;
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (myReceiverIsRegistered) {
            unregisterReceiver(refreshListReciver);
            myReceiverIsRegistered = false;
        }
    }

    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(this,MainPageActivity.class));
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

    public void refreshList()
    {
        ChatsDB db = new ChatsDB(context);
        db.open();
        messages = db.getAllMessagesByName(reciver);
        db.close();
        final MessageAdapter messageAdapter = new MessageAdapter(context,0,messages);
        lvMsg.post(new Runnable() {
            @Override
            public void run()
            {
                lvMsg.setAdapter(messageAdapter);
                lvMsg.setSelection(messages.size());
            }
        });
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

        try {
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                intentCamera = true;
                File photo = null;
                try {
                    photo = this.createTemporaryFile("pic", ".jpg");
                    photo.delete();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                imgURI = Uri.fromFile(photo);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imgURI);
                startActivityForResult(takePictureIntent, CAMERA);
            }
        }
        catch (Exception e)
        {
            Log.e("Error",e.getMessage());
        }
    }

    private File createTemporaryFile(String part, String ext) throws Exception
    {
        if(isStoragePermissionGranted()) {
            File tempDir = Environment.getExternalStorageDirectory();
            tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            return File.createTempFile(part, ext, tempDir);
        }
        return null;
    }

//    public  boolean isStoragePermissionGranted() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    == PackageManager.PERMISSION_GRANTED) {
//                Log.e("Error","Permission is granted");
//                return true;
//            } else {
//
//                Log.e("Error","Permission is revoked");
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//                return false;
//            }
//        }
//        else { //permission is automatically granted on sdk<23 upon installation
//            Log.e("Error","Permission is granted");
//            return true;
//        }
//    }

    public Bitmap grabImage()
    {
        //this.getContentResolver().notifyChange(imgURI, null);
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap;
        try
        {
            bitmap = android.provider.MediaStore.Images.Media.getBitmap(cr, imgURI);
            return rotateImageIfRequired(this,bitmap,imgURI);
        }
        catch (Exception e)
        {
            Toast.makeText(this, "Failed to load", Toast.LENGTH_SHORT).show();
            Log.d("ERROR", "Failed to load", e);
        }
        return null;
    }

    private Bitmap rotateImageIfRequired(Context context,Bitmap img, Uri selectedImage) {

        // Detect rotation
        int rotation = getRotation(context, selectedImage);
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
            img.recycle();
            return rotatedImg;
        }
        else{
            return img;
        }
    }

    private int getRotation(Context context,Uri selectedImage) {

        int rotation = 0;
        ContentResolver content = context.getContentResolver();

        Cursor mediaCursor = content.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { "orientation", "date_added" },
                null, null, "date_added desc");

        if (mediaCursor != null && mediaCursor.getCount() != 0) {
            while(mediaCursor.moveToNext()){
                rotation = mediaCursor.getInt(0);
                break;
            }
        }
        mediaCursor.close();
        return rotation;
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
        File myDir = new File(root + "/Chaty/saved_images");
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
            int id=-1;
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            if ((line = reader.readLine()) != null) {
                id = Integer.parseInt(line);
            }

            writer.close();
            reader.close();

            if(id!=-1)
                temp.setId(id);
            else
                Toast.makeText(context, "BUGG!!", Toast.LENGTH_SHORT).show();
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
            int id = -1;
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            if ((line = reader.readLine()) != null) {
                id = Integer.parseInt(line);
            }

            writer.close();
            reader.close();

            if(id!=-1)
                temp.setId(id);
            else
                Toast.makeText(context, "BUGG!!", Toast.LENGTH_SHORT).show();
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

    class RefreshListReciver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            int id = intent.getIntExtra("ID",-1);
            if(id==notificationID) {
                try {
                    refreshList();
                    NotificationManager mNotificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(intent.getIntExtra("ID", -1));
                } catch (Exception e) {
                    Log.i("Chaty", e.getMessage());
                }
            }
        }
    }
}
