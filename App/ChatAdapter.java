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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends ArrayAdapter<Chat> implements Filterable
{

    Context context;
    List<Chat> mOriginalValues=null;
    List<Chat> objects;

    public ChatAdapter(Context context, int resource, List<Chat> objects)
    {
        super(context, resource, objects);
        this.objects = objects;
        this.mOriginalValues = objects;
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if(objects.size()!=0) {
            LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
            View view = layoutInflater.inflate(R.layout.main_chats, parent, false);
            TextView tvChatName = (TextView) view.findViewById(R.id.tvChatName);
            ImageView img = (ImageView) view.findViewById(R.id.imgMainPage);
            Chat temp = objects.get(position);
            tvChatName.setText(temp.getChatName());
            //img.setImageBitmap(Bitmap.createScaledBitmap(temp.getProfileImg(),400,500,false));
            img.setImageBitmap(temp.getProfileImg());
            return view;
        }
        LayoutInflater layoutInflater = ((Activity) context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.empty_list, parent, false);
        return view;
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
                List<Chat> filteredChats = new ArrayList<Chat>();

                if (mOriginalValues == null) {
                    mOriginalValues = new ArrayList<Chat>(objects);
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
                        String data = mOriginalValues.get(i).getChatName();
                        if(data.toLowerCase().startsWith(charSequence.toString()))
                            filteredChats.add(mOriginalValues.get(i));
                    }
                    results.count = filteredChats.size();
                    results.values = filteredChats;
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                objects = (List<Chat>) filterResults.values;
                notifyDataSetChanged();
            }
        };
        return filter;
    }
}
