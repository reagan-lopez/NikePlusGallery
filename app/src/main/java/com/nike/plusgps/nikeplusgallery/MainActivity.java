/**
     Copyright © 2014 Reagan Lopez
     [This program is licensed under the "MIT License"]
     Please see the file LICENSE in the source
     distribution of this software for license terms
*/
package com.nike.plusgps.nikeplusgallery;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Main Activity has Async tasks to parse a JSON response and load it into SQLite.
 * In portrait mode, images and its titles are displayed in a grid view.
 * In landscape mode, images are displayed in a carousel.
 */
public class MainActivity extends Activity {
    ArrayList<FlickrFeed> flickrFeedList; // Container to hold the JSON images and titles
    FlickrFeedAdapter adapter; // Adapter to download the images and titles
    private static final float INITIAL_ITEMS_COUNT = 2.5F; // Number of items visible when the carousel is shown
    private LinearLayout carouselElement; // Carousel container layout
    private DBHelper dbHelper; // SQLite database instance
    private LruCache<String, Bitmap> mMemoryCache;
    private final Object mDiskCacheLock = new Object();
    private boolean mDiskCacheStarting = true;
    private static final int DISK_CACHE_SIZE = 1024 * 1024 * 10; // 10MB
    private static final String DISK_CACHE_SUBDIR = "thumbnails";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DBHelper(this); // Creates the database
        dbHelper.deleteAllResponse(); // Clears the JSON response cache
        dbHelper.deleteAllMedia(); // Clears the JSON images cache

        // Get memory class of this device, exceeding this amount will throw an
        // OutOfMemory exception.
        final int memClass
                = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE))
                .getMemoryClass();
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = 1024 * 1024 * memClass / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in bytes rather than number of items.
                return bitmap.getByteCount();
            }
        };



        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            setContentView(R.layout.activity_main_port);
            GridView view = (GridView) findViewById(R.id.gridView);
            flickrFeedList = new ArrayList<FlickrFeed>();
            new JSONParser().execute("http://api.flickr.com/services/feeds/photos_public.gne?tags=nike&format=json");
            adapter = new FlickrFeedAdapter(getApplicationContext(), R.layout.single_elem_port,
                    flickrFeedList, dbHelper, mMemoryCache);
            view.setAdapter(adapter);

        } else {

            setContentView(R.layout.activity_main_land);
            carouselElement = (LinearLayout) findViewById(R.id.carousel);
            // Compute width of a carousel item based on screen width and initial item count
            final DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            final int imageWidth = (int) (displayMetrics.widthPixels / INITIAL_ITEMS_COUNT);

            // Fetches the data from image cache in SQLite
            ArrayList imageArray = new ArrayList();
            imageArray = dbHelper.getAllMedia();
            ImageView imageItem;
            String imgURL;
            for (int i = 0 ; i < imageArray.size() ; ++i) {
                imageItem = new ImageView(this);
                imgURL = (String) imageArray.get(i);
                new DownloadImageTask(imageItem).execute(imgURL);
                imageItem.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageWidth));
                carouselElement.addView(imageItem);
            }
        }
    }

    /**
     * Handles the screen layouts based on the orientation.
     * The activity is not restarted when the orientation changes.
     */
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // In Portrait mode the grid view is displayed
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            setContentView(R.layout.activity_main_port);
            GridView view = (GridView)findViewById(R.id.gridView);
            adapter = new FlickrFeedAdapter(getApplicationContext(), R.layout.single_elem_port,
                    flickrFeedList, dbHelper, mMemoryCache);
            view.setAdapter(adapter);

        } else { // In Landscape mode the carousel view is displayed

            setContentView(R.layout.activity_main_land);
            carouselElement = (LinearLayout) findViewById(R.id.carousel);
            // Compute width of a carousel item based on screen width and initial item count
            final DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            final int imageWidth = (int) (displayMetrics.widthPixels / INITIAL_ITEMS_COUNT);

            // Fetches image url from SQLite cache
            ArrayList imageArray = new ArrayList();
            imageArray = dbHelper.getAllMedia();
            ImageView imageItem;
            String imgURL;
            for (int i = 0 ; i < imageArray.size() ; ++i) {
                imageItem = new ImageView(this);
                imgURL = (String) imageArray.get(i);
                //Bitmap bm;
                new DownloadImageTask(imageItem).execute(imgURL);
                //imageItem.setImageBitmap(bm);
                imageItem.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageWidth));
                carouselElement.addView(imageItem);
            }
        }
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

    /**
     * Async task to read the JSON response from server.
     */
    class JSONParser extends AsyncTask<String, Void, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Loading, please wait");
            dialog.setTitle("Connecting server");
            dialog.show();
            dialog.setCancelable(false);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                HttpGet httppost = new HttpGet(strings[0]);
                HttpClient httpclient = new DefaultHttpClient();
                HttpResponse response = httpclient.execute(httppost);

                int status = response.getStatusLine().getStatusCode();

                if (status == 200) {
                    HttpEntity entity = response.getEntity();
                    String strEntity = EntityUtils.toString(entity);
                    String data = strEntity.substring(strEntity.indexOf("{"), strEntity.lastIndexOf(")"));
                    dbHelper.insertResponse(data); // Insert response into SQLite cache

                    JSONObject jobj = new JSONObject(data);
                    JSONArray jarray = jobj.getJSONArray("items");
                    String img_url;
                    String img_title;
                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject object = jarray.getJSONObject(i);
                        FlickrFeed feed = new FlickrFeed();
                        img_url = (new JSONObject(object.getString("media"))).getString("m");
                        img_title = object.getString("title");
                        feed.setMedia(img_url);
                        feed.setTitle(img_title);
                        flickrFeedList.add(feed);
                        dbHelper.insertMedia(img_url,img_title); // Insert image path and title into SQLite Cache
                        //Toast.makeText(getApplicationContext(), String.valueOf(dbHelper.countMedia()), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            dialog.cancel();
            adapter.notifyDataSetChanged();
            if(result == false)
                Toast.makeText(getApplicationContext(), "Unable to fetch data from server", Toast.LENGTH_LONG).show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
