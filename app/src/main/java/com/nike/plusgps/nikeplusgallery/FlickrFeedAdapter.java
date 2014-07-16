package com.nike.plusgps.nikeplusgallery;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class FlickrFeedAdapter extends ArrayAdapter<FlickrFeed> {


    LayoutInflater vi;
    int singleElem;
    ArrayList<FlickrFeed> flickrFeedList;
    DBHelper dbHelper;
    ViewHolder holder;

    public FlickrFeedAdapter(Context context, int singleElem, ArrayList<FlickrFeed> flickrFeedList, DBHelper dbHelper) {
        super(context, singleElem, flickrFeedList);
        this.vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.singleElem = singleElem;
        this.flickrFeedList = flickrFeedList;
        this.dbHelper = dbHelper;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
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

    static class ViewHolder {
        public ImageView media;
        public TextView title;
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... strings) {
            String imgURL = strings[0];
            Bitmap bm = null;
            try {
                InputStream in = new java.net.URL(imgURL).openStream();
                bm = BitmapFactory.decodeStream(in);

                // convert bitmap to byte array
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.PNG, 100, bos);
                byte[] bArray = bos.toByteArray();
                dbHelper.insertMedia(bos.toByteArray()); // Insert response into SQLite
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bm;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }

    }
}