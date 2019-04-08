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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends ArrayAdapter<User> implements Filterable
{
    Context context;
    List<User> mOriginalValues=null;
    List<User> objects;

    public UserAdapter(Context context, int resource, List<User> objects)
    {
        super(context, resource, objects);
        this.objects = objects;
        this.mOriginalValues = objects;
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
        User temp = objects.get(position);
        tvName.setText(temp.getUsername());
        tvStatus.setText(temp.getStatus());
        placeImg(temp,imgSrc);
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

    @Override
    public int getCount()
    {
        if(objects!=null)
            return objects.size();
        return 0;
    }

    @Override
    public Filter getFilter()
    {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();
                List<User> filteredUsers = new ArrayList<User>();

                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<User>(objects);
                }

                if (charSequence == null || charSequence.length() == 0)
                {
                    results.count = mOriginalValues.size();
                    results.values = mOriginalValues;
                }
                else
                {
                    charSequence = charSequence.toString().toLowerCase();
                    for(int i=0;i<mOriginalValues.size();i++)
                    {
                        String data = mOriginalValues.get(i).getUsername();
                        if(data.toLowerCase().startsWith(charSequence.toString()))
                            filteredUsers.add(mOriginalValues.get(i));
                    }
                    results.count = filteredUsers.size();
                    results.values = filteredUsers;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                objects = (List<User>) filterResults.values;
                notifyDataSetChanged();
            }
        };
        return filter;
    }

    public void setObjects(ArrayList<User> users)
    {
        this.objects = users;
        this.mOriginalValues = users;
    }

    private void placeImg(User temp,ImageView imgSrc)
    {
        try {
            Bitmap img = temp.getImg();
            imgSrc.setImageBitmap(getResizedBitmap(img,400));
        }
        catch (Exception e)
        {
            Log.i("Error",e.getMessage());
        }
    }
}
