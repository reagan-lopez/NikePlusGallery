package com.nike.plusgps.nikeplusgallery;

public class FlickrFeed {

    private String media;
    private String title;


    public FlickrFeed() {
        // TODO Auto-generated constructor stub
    }

    public FlickrFeed(String media, String title) {
        super();
        this.media = media;
        this.title = title;
    }


    public String getMedia() {
        return media;
    }

    public void setMedia(String media) {
        this.media = media;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}