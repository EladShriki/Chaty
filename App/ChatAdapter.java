package com.example.eladshriki.chaty;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ChatAdapter extends ArrayAdapter<Chat>
{

    Context context;
    List<Chat> objects;

    public ChatAdapter(Context context, int resource, List<Chat> objects)
    {
        super(context, resource, objects);
        this.objects = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.main_chats,parent,false);
        TextView tvChatName = (TextView)view.findViewById(R.id.tvChatName);
        ImageView img = (ImageView)view.findViewById(R.id.imgMainPage);
        Chat temp = objects.get(position);
        tvChatName.setText(temp.getChatName());
        //img.setImageBitmap(Bitmap.createScaledBitmap(temp.getProfileImg(),400,500,false));
        img.setImageBitmap(temp.getProfileImg());
        return view;
    }

}
