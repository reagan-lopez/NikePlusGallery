package com.nike.plusgps.nikeplusgallery;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
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
import java.util.ArrayList;


public class MainActivity extends Activity {
    ArrayList<FlickrFeed> flickrFeedList;
    FlickrFeedAdapter adapter;
    private static final float INITIAL_ITEMS_COUNT = 2.5F; // number of items visible when the carousel is shown
    private LinearLayout carouselElement; // Carousel container layout
    private DBHelper dbHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DBHelper(this);
        dbHelper.deleteAllResponse();
        dbHelper.deleteAllMedia();

        setContentView(R.layout.activity_main_port);
        GridView view = (GridView)findViewById(R.id.gridView);
        flickrFeedList = new ArrayList<FlickrFeed>();
        new JSONParser().execute("http://api.flickr.com/services/feeds/photos_public.gne?tags=nike&format=json");
        adapter = new FlickrFeedAdapter(getApplicationContext(), R.layout.single_elem_port, flickrFeedList, dbHelper);
        view.setAdapter(adapter);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // In Portrait mode the grid view is displayed
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

            Cursor res = dbHelper.getResponse(1); // Fetch the only row in the table
            res.moveToFirst();
            String url = res.getString(res.getColumnIndex(DBHelper.RESPONSE_COLUMN_TXT));
            //String count = String.valueOf(dbHelper.countMedia());
            //Toast.makeText(getApplicationContext(), count, Toast.LENGTH_LONG).show();
            //Toast.makeText(getApplicationContext(), url, Toast.LENGTH_LONG).show();

            setContentView(R.layout.activity_main_port);
            GridView view = (GridView)findViewById(R.id.gridView);
            flickrFeedList = new ArrayList<FlickrFeed>();
            new SQLiteParser().execute(url); // fetching the JSON response from SQLite cache
            adapter = new FlickrFeedAdapter(getApplicationContext(), R.layout.single_elem_port, flickrFeedList, dbHelper);
            view.setAdapter(adapter);

        } else { // In Landscape mode the carousel view is displayed
            //String count = String.valueOf(dbHelper.countMedia());
            //Toast.makeText(getApplicationContext(), count, Toast.LENGTH_LONG).show();

            setContentView(R.layout.activity_main_land);
            carouselElement = (LinearLayout) findViewById(R.id.carousel);
            // Compute width of a carousel item
            final DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            final int imageWidth = (int) (displayMetrics.widthPixels / INITIAL_ITEMS_COUNT);
            ArrayList imageArray = new ArrayList();
            imageArray = dbHelper.getAllMedia();
            ImageView imageItem;
            for (int i = 0 ; i < imageArray.size() ; ++i) {
                imageItem = new ImageView(this);
                //imageItem.setBackgroundResource(R.drawable.shadow); // Set the shadow background
                //imageItem.setImageResource(puppyResourcesTypedArray.getResourceId(i, -1));
                byte[] imgBlob = (byte[]) imageArray.get(i);
                Bitmap bm = BitmapFactory.decodeByteArray(imgBlob, 0 ,imgBlob.length);
                imageItem.setImageBitmap(bm);
                imageItem.setLayoutParams(new LinearLayout.LayoutParams(imageWidth, imageWidth));
                carouselElement.addView(imageItem);
            }
        }
    }

    class SQLiteParser extends AsyncTask<String, Void, Boolean> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setMessage("Loading, please wait");
            dialog.setTitle("Connecting SQLite");
            dialog.show();
            dialog.setCancelable(false);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            try {
                JSONObject jobj = new JSONObject(strings[0]);
                JSONArray jarray = jobj.getJSONArray("items");

                for (int i = 0; i < jarray.length(); i++) {
                    JSONObject object = jarray.getJSONObject(i);
                    FlickrFeed feed = new FlickrFeed();
                    feed.setMedia((new JSONObject(object.getString("media"))).getString("m"));
                    feed.setTitle(object.getString("title"));
                    flickrFeedList.add(feed);
                }
                return true;

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

                    for (int i = 0; i < jarray.length(); i++) {
                        JSONObject object = jarray.getJSONObject(i);
                        FlickrFeed feed = new FlickrFeed();
                        feed.setMedia((new JSONObject(object.getString("media"))).getString("m"));
                        feed.setTitle(object.getString("title"));
                        flickrFeedList.add(feed);
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

        protected void onPostExecute(Boolean result) {
            dialog.cancel();
            adapter.notifyDataSetChanged();
            if(result == false)
                Toast.makeText(getApplicationContext(), "Unable to fetch data from server", Toast.LENGTH_LONG).show();
        }
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
