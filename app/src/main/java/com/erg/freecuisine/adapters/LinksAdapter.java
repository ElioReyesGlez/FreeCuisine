package com.erg.freecuisine.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.erg.freecuisine.R;
import com.erg.freecuisine.models.LinkModel;

import java.util.ArrayList;

public class LinksAdapter extends ArrayAdapter<LinkModel> {

    private final ArrayList<LinkModel> links;
    private final Context context;

    public LinksAdapter(@NonNull Context context, int resource,
                        @NonNull ArrayList<LinkModel> links) {
        super(context, resource, links);
        this.context = context;
        this.links = links;
    }

    @Override
    public int getCount() {
        return links == null ? 0 : links.size();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        ViewHolder viewHolder;
        LinkModel link = getItem(position);

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.item_step_link, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (link != null) {
            viewHolder.tag.setText(link.getTag());
            viewHolder.url.setText(link.getUrl());
        }

        return convertView;
    }

    private static class ViewHolder{
        public final TextView tag;
        public final TextView url;

        public ViewHolder(View convertView) {
            this.tag = convertView.findViewById(R.id.tag_link);
            this.url = convertView.findViewById(R.id.url_link);
        }
    }
}
