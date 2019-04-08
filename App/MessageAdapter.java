package com.example.eladshriki.chaty;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message>
{
    Context context;
    List<Message> objects;
    final int imgMaxSize = 550;

    public MessageAdapter(Context context, int resource, List<Message> objects)
    {
        super(context, resource, objects);
        this.objects = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {

        Message temp = objects.get(position);

        if(temp.getImgBytes()!=null)
        {
            Bitmap img = temp.getBitmap();

            LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.img_row,parent,false);

            if(temp.getSender().equals(MainActivity.loginSystem.getUsername()))
            {
                ImageView imgMsgL = (ImageView) view.findViewById(R.id.imgMsgL);
                view.findViewById(R.id.imgLayoutR).setAlpha(0);
                imgMsgL.setImageBitmap(getResizedBitmap(img,imgMaxSize));
            }
            else
            {
                ImageView imgMsgR = (ImageView)view.findViewById(R.id.imgMsgR);
                view.findViewById(R.id.imgLayoutL).setAlpha(0);
                imgMsgR.setImageBitmap(getResizedBitmap(img,imgMaxSize));
            }
            return view;
        }

        LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.chat_row,parent,false);
        TextView tvMsgL = (TextView)view.findViewById(R.id.tvMsgL);
        TextView tvMsgR = (TextView)view.findViewById(R.id.tvMsgR);


        if(temp.getSender().equals(MainActivity.loginSystem.getUsername())) {
            tvMsgL.setText(temp.getText());
            tvMsgR.setAlpha(0);
        }
        else
        {
            tvMsgR.setText(temp.getText());
            tvMsgL.setAlpha(0);
        }
        return view;
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
}
