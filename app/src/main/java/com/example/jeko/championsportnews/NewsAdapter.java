package com.example.jeko.championsportnews;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class NewsAdapter extends ArrayAdapter<ChampionNews> {
    public NewsAdapter(Activity context, List<ChampionNews> championNews) {
        super(context, 0, championNews);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_news, parent, false);
        }

        ChampionNews currentChampionNews = getItem(position);
        // Set section
        TextView section = (TextView) listItemView.findViewById(R.id.section);
        section.setText(getContext().getString(R.string.section) + currentChampionNews.getSection());
        // Set Title
        TextView title = (TextView) listItemView.findViewById(R.id.title);
        title.setText(currentChampionNews.getTitle());
        // Set author
        TextView author = (TextView) listItemView.findViewById(R.id.author);
        author.setText(currentChampionNews.getAuthor());
        // Set date
        TextView date = (TextView) listItemView.findViewById(R.id.date);
        date.setText(currentChampionNews.getDate());

        return listItemView;
    }
}
