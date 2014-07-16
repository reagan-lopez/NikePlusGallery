/**
     Copyright © 2014 Reagan Lopez
     [This program is licensed under the "MIT License"]
     Please see the file LICENSE in the source
     distribution of this software for license terms
*/
package com.nike.plusgps.nikeplusgallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Adapter that downloads the images and attaches the contents to the views.
 */
public class FlickrFeedAdapter extends ArrayAdapter<FlickrFeed> {

    LayoutInflater vi;
    int singleElem;
    ArrayList<FlickrFeed> flickrFeedList;
    DBHelper dbHelper;
    ViewHolder holder;
    LruCache<String, Bitmap> mMemoryCache;

    static class ViewHolder { // Holder class for the image and title.
        public ImageView media;
        public TextView title;
    }

    public FlickrFeedAdapter(Context context, int singleElem,
                             ArrayList<FlickrFeed> flickrFeedList, DBHelper dbHelper, LruCache<String, Bitmap> mMemoryCache) {
        super(context, singleElem, flickrFeedList);
        this.vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.singleElem = singleElem;
        this.flickrFeedList = flickrFeedList;
        this.dbHelper = dbHelper;
        this.mMemoryCache = mMemoryCache;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Toast.makeText(parent.getContext(), String.valueOf(position), Toast.LENGTH_LONG).show();
        View v = convertView;
        // This is done to reduce the expensive findViewById operation.
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(singleElem, null);
            holder.media = (ImageView) v.findViewById(R.id.media);
            holder.title = (TextView) v.findViewById(R.id.title);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.media.setImageResource(R.drawable.placeholder);
        new DownloadImageTask(holder.media).execute(flickrFeedList.get(position).getMedia());
        holder.title.setText(flickrFeedList.get(position).getTitle());
        return v;
    }

    /**
     * Async task to download the images.
     */
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        @Override
        protected Bitmap doInBackground(String... strings) {
            String imgURL = strings[0];
            Bitmap bm = null;
            bm = getBitmapFromMemCache(imgURL); // Gets bitmap from memory cache
            try {
                if (bm == null) { // Generates bitmap from the server
                    InputStream in = new java.net.URL(imgURL).openStream();
                    bm = BitmapFactory.decodeStream(in);
                }
                addBitmapToMemoryCache(imgURL, bm); // Adds bitmap to memory cache.

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bm;
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }

    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return (Bitmap) mMemoryCache.get(key);
    }
}