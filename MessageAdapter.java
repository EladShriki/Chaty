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
import android.widget.TextView;

import java.util.List;

public class MessageAdapter extends ArrayAdapter<Message>
{
    Context context;
    List<Message> objects;

    public MessageAdapter(Context context, int resource, List<Message> objects)
    {
        super(context, resource, objects);
        this.objects = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Message temp = objects.get(position);

        if(temp.getImgBytes()!=null)
        {
            byte[] imgBytes = temp.getImgBytes();
            Bitmap img = BitmapFactory.decodeByteArray(imgBytes,0,imgBytes.length);


            LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.img_row,parent,false);
            TextView tvImgSend = (TextView)view.findViewById(R.id.tvImgSend);
            ImageView imgMsg = (ImageView)view.findViewById(R.id.imgMsg);
            imgMsg.setImageBitmap(Bitmap.createScaledBitmap(img,600,900,false));
            tvImgSend.setText(temp.getSender());
            return view;
        }

        LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.chat_row,parent,false);
        TextView tvSender = (TextView)view.findViewById(R.id.tvSender);
        TextView tvMsg = (TextView)view.findViewById(R.id.tvMsg);


        tvSender.setText(temp.getSender());
        tvMsg.setText(temp.getText());
        return view;
    }
}
