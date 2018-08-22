package com.example.jeko.championsportnews;

public class ChampionNews {

    private String mSection;
    private String mTitle;
    private String mUrl;
    private String mAuthor;
    private String mDate;

    public ChampionNews(String section, String title, String url, String author, String date) {
        mSection = section;
        mTitle = title;
        mUrl = url;
        mAuthor = author;
        mDate = date;
    }

    public String getSection() {
        return mSection;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getDate() {
        return mDate;
    }
}