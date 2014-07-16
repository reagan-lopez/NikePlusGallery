/**
     Copyright © 2014 Reagan Lopez
     [This program is licensed under the "MIT License"]
     Please see the file LICENSE in the source
     distribution of this software for license terms
*/

package com.nike.plusgps.nikeplusgallery;

/**
 * Container for the images and titles with its getter and setter methods.
 */
public class FlickrFeed {

    private String media;
    private String title;

    public FlickrFeed() {

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