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

import java.io.InputStream;
import java.util.ArrayList;

public class FlickrFeedAdapter extends ArrayAdapter<FlickrFeed> {

    ArrayList<FlickrFeed> flickrFeedList;
    LayoutInflater vi;
    int Resource;
    ViewHolder holder;

    public FlickrFeedAdapter(Context context, int resource, ArrayList<FlickrFeed> objects) {
        super(context, resource, objects);
        vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resource = resource;
        flickrFeedList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            holder = new ViewHolder();
            v = vi.inflate(Resource, null);
            holder.media = (ImageView) v.findViewById(R.id.media);
            holder.title = (TextView) v.findViewById(R.id.title);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }
        holder.media.setImageResource(R.drawable.ic_launcher);
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
            String urldisplay = strings[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }

    }
}