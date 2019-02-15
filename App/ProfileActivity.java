package com.example.eladshriki.chaty;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Date;

import javax.net.ssl.HttpsURLConnection;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    static String status;
    Button btnEditImg,btnStatus,btnHome;
    TextView tvUsername;
    ImageView imgProfile;
    TextView tvStatus;
    Uri imgURI;
    private int GALLERY = 1, CAMERA = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvUsername = (TextView)findViewById(R.id.tvUsername);
        tvUsername.setText(MainActivity.loginSystem.getUsername());

        tvStatus = (TextView)findViewById(R.id.tvProStatus);

        imgProfile = (ImageView)findViewById(R.id.imgProfile);

        btnEditImg = (Button)findViewById(R.id.btnEditImg);
        btnEditImg.setOnClickListener(this);

        btnStatus = (Button)findViewById(R.id.btnStatus);
        btnStatus.setOnClickListener(this);

        btnHome = (Button)findViewById(R.id.btnHome);
        btnHome.setOnClickListener(this);

        getProfile();
    }

    @Override
    public void onBackPressed()
    {
        startActivity(new Intent(this,MainPageActivity.class));
    }

    @Override
    public void onClick(View view) {
        if(view==btnEditImg)
        {
            showPictureDialog();
        }
        if (view==btnStatus)
        {
            getNewStatus(status);
            tvStatus.setText(status);
        }
        if(btnHome==view)
            startActivity(new Intent(this,MainPageActivity.class));
    }

    public void getNewStatus(final String status)
    {
        final android.support.v7.app.AlertDialog.Builder mBuilder = new android.support.v7.app.AlertDialog.Builder(this);
        View view1 = getLayoutInflater().inflate(R.layout.status_change,null);
        final EditText etStatus = (EditText)view1.findViewById(R.id.etStatusCng);
        etStatus.setText(status);
        Button btnYes = (Button)view1.findViewById(R.id.btnYes);
        Button btnNo = (Button)view1.findViewById(R.id.btnNo);

        mBuilder.setView(view1);
        final android.support.v7.app.AlertDialog dialog = mBuilder.create();

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                ProfileActivity.status = etStatus.getText().toString();
                changeStatus();
                dialog.dismiss();

            }
        });
        btnNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProfileActivity.status = status;
                dialog.dismiss();
            }
        });

        dialog.show();
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
                Toast.makeText(this, "No Storage!", Toast.LENGTH_SHORT).show();
        }
    }

    public void sendImg(byte[] bytes)
    {
        String urlParameters = "username="+MainActivity.loginSystem.getUsername()+"&changeImage=1&changeStatus=0&img="+ Arrays.toString(bytes);;
        try {
            String url = MainActivity.host+"/TestServer/Profile";
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


        } catch (Exception e) {
            Log.e("Chaty",e.getMessage());
        }
        Bitmap img = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        imgProfile.setImageBitmap(Bitmap.createScaledBitmap(img,800,900,false));
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

    public Bitmap grabImage()
    {
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

    public void takePhotoFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
        {
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

    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50 /*ignored for PNG*/, blob);
        byte[] bitmapdata = blob.toByteArray();
        return bitmapdata;
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
        startActivityForResult(galleryIntent, GALLERY);
    }

    public void changeStatus()
    {
        String urlParameters = "username="+tvUsername.getText()+"&status="+ProfileActivity.status+"&changeStatus=1&changeImage=0";
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

            }

            writer.close();
            reader.close();
        } catch (Exception e) {

        }
        tvStatus.setText(ProfileActivity.status);
        Toast.makeText(this, "Status Updated!", Toast.LENGTH_SHORT).show();
    }

    public void getProfile()
    {
        String status=null,image=null;
        String urlParameters = "username="+tvUsername.getText()+"&changeStatus=0&changeImage=0";
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
                image = line.substring(line.indexOf(',')+1);
            }

            writer.close();
            reader.close();
        } catch (Exception e) {

        }
        ProfileActivity.status = status;
        tvStatus.setText(status);

        if(!image.equals("null")) {
            String[] byteValues = image.substring(1, image.length() - 1).split(",");
            byte[] imgByte = new byte[byteValues.length];

            for (int i = 0, len = imgByte.length; i < len; i++)
                imgByte[i] = Byte.parseByte(byteValues[i].trim());

            Bitmap img = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            imgProfile.setImageBitmap(img);
        }
        else
        {
            Bitmap img = BitmapFactory.decodeResource(this.getResources(),
                    R.drawable.user);
            imgProfile.setImageBitmap(img);
        }
    }
}
