package com.example.eladshriki.chaty;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class UserAdapter extends ArrayAdapter<User>
{
    Context context;
    List<User> objects;

    public UserAdapter(Context context, int resource, List<User> objects)
    {
        super(context, resource, objects);
        this.objects = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.one_row,parent,false);
        TextView tvStatus = (TextView)view.findViewById(R.id.tvStatus);
        ImageView imgSrc = (ImageView)view.findViewById(R.id.imgSrc);
        TextView tvName = (TextView)view.findViewById(R.id.tvName);
        TextView tvEmail = (TextView)view.findViewById(R.id.tvEmail);
        User temp = objects.get(position);
        tvName.setText(temp.getUsername());
        tvEmail.setText(temp.getEmail());
        tvStatus.setText(temp.getStatus());
        placeImg(temp,imgSrc);
        return view;
    }

    private void placeImg(User temp,ImageView imgSrc)
    {
        if(temp.getImgBytes()!=null)
        {
            try {
                byte[] bytes = temp.getImgBytes();
                Bitmap img = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                imgSrc.setImageBitmap(Bitmap.createScaledBitmap(img, 400, 400, false));
            }
            catch (Exception e)
            {
                Log.i("Error",e.getMessage());
            }
        }
        else
        {
            Bitmap img = BitmapFactory.decodeResource(context.getResources(),
                    R.drawable.user);
            imgSrc.setImageBitmap(Bitmap.createScaledBitmap(img,400,400,false));
        }
    }
}
