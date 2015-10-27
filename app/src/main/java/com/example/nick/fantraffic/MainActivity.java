package com.example.nick.fantraffic;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.CardView;
import android.util.JsonReader;
import android.util.JsonToken;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RVAdapter adapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Traffic> traffic;
    private TrafficReader.FeedEntry feeder;
    private TrafficReaderDbHelper mDbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initializeDataOnStart();

        mRecyclerView = (RecyclerView)findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);

        //RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        //mRecyclerView.addItemDecoration(itemDecoration);

        adapter = new RVAdapter(traffic, this);
        mRecyclerView.setAdapter(adapter);

        mRecyclerView.setLayoutManager(llm);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "refreshing data", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mDbHelper = new TrafficReaderDbHelper(this);

        initializeData();
    }

    public void sendMessage(View v)
    {
        Intent inent = new Intent(this, DisplayTrafficInfoActivity.class);
        int id = v.getId();

        inent.putExtra("flow", traffic.get(id).flow);
        inent.putExtra("duration", traffic.get(id).duration);

        startActivity(inent);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            initializeData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initializeDataOnStart()
    {
        traffic = new ArrayList<>();
    }

    private void initializeData(){
        String strJsonData;

        strJsonData = "http://traffic.cit.api.here.com/traffic/6.0/incidents.json" +
                "?app_id=wa1d1f5iSh0BQqHHtLI5" +
                "&app_code=br5kbaTStGduNL9LYzPD8w" +
                "&quadkey=0313113200";

        newRequestQueue(strJsonData);
    }

    public void newRequestQueue(String url){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.

        JsonObjectRequest JsonRequest = new JsonObjectRequest(Request.Method.GET,url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        // Display the first 500 characters of the response string.
                        //strHttpResponse[0] = response.substring(0,500);
                        Snackbar.make(mRecyclerView, "Data returned", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        populateTrafficData(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make(mRecyclerView, "That didn't work!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(JsonRequest);
    }


    public void parseJsonResponseDbInsert(JSONObject json)
    {
        String id,
        title,
        content,
        startDate,
        endDate,
        location,
        status;
        boolean roadClosed;

        try {
            //traffic = new ArrayList<>();
            JSONArray array = json.getJSONObject("TRAFFICITEMS").getJSONArray("TRAFFICITEM");

            for(int i = 0; i < array.length(); i++) {
                id = array.getJSONObject(i).getString("ORIGINALTRAFFICITEMID");
                title = array.getJSONObject(i).getString("TRAFFICITEMTYPEDESC");
                content = array.getJSONObject(i).getJSONArray("TRAFFICITEMDESCRIPTION").getJSONObject(1).getString("content").isEmpty() ? "no comment" : array.getJSONObject(i).getJSONArray("TRAFFICITEMDESCRIPTION").getJSONObject(1).getString("content");
                startDate = array.getJSONObject(i).getString("STARTTIME");
                endDate = array.getJSONObject(i).getString("ENDTIME");
                location = array.getJSONObject(i).getJSONObject("LOCATION").getJSONObject("DEFINED").getJSONObject("ORIGIN").getJSONObject("POINT").getJSONArray("DESCRIPTION").getJSONObject(0).getString("content");
                roadClosed = array.getJSONObject(i).getJSONObject("TRAFFICITEMDETAIL").getBoolean("ROADCLOSED");
                status = array.getJSONObject(i).getString("TRAFFICITEMSTATUSSHORTDESC");

                insertTrafficReaderData(id, title, content, startDate, endDate, location, roadClosed, status);
            }
        }catch (JSONException jex)
        {}
    }

    public void populateTrafficData(JSONObject json)
    {
        try {
            //traffic = new ArrayList<>();
            JSONArray array = json.getJSONObject("TRAFFICITEMS").getJSONArray("TRAFFICITEM");

            for(int i = 0; i < array.length(); i++) {
                if(checkTrafficReaderRow(array.getJSONObject(i).getString("ORIGINALTRAFFICITEMID")))
                    traffic.add(new Traffic(getTrafficReaderData(array.getJSONObject(i).getString("ORIGINALTRAFFICITEMID"), feeder.COLUMN_NAME_TITLE), getTrafficReaderData(array.getJSONObject(i).getString("ORIGINALTRAFFICITEMID"), feeder.COLUMN_NAME_CONTENT)));
                else {
                    parseJsonResponseDbInsert(json);
                    traffic.add(new Traffic(getTrafficReaderData(array.getJSONObject(i).getString("ORIGINALTRAFFICITEMID"), feeder.COLUMN_NAME_TITLE), getTrafficReaderData(array.getJSONObject(i).getString("ORIGINALTRAFFICITEMID"), feeder.COLUMN_NAME_CONTENT)));
                }
            }
        }catch (JSONException jex)
        {}
        adapter.addItemsToList(traffic);
        adapter.notifyDataSetChanged();
    }

    public void insertTrafficReaderData(String id, String title, String content, String startDate, String endDate, String location, boolean roadClosed, String status)
    {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(feeder.COLUMN_NAME_ENTRY_ID, id);
        values.put(feeder.COLUMN_NAME_TITLE, title);
        values.put(feeder.COLUMN_NAME_CONTENT, content);
        values.put(feeder.COLUMN_NAME_STARTDATE, startDate);
        values.put(feeder.COLUMN_NAME_ENDDATE, endDate);
        values.put(feeder.COLUMN_NAME_LOCATION, location);
        values.put(feeder.COLUMN_NAME_ROADCLOSED, roadClosed);
        values.put(feeder.COLUMN_NAME_STATUS, status);

        // Insert the new row, returning the primary key value of the new row
        long newRowId;
        newRowId = db.insert(
                feeder.TABLE_NAME,
                feeder.COLUMN_NAME_NULLABLE,
                values);

        if(newRowId == -1)
        {
            Snackbar.make(mRecyclerView, "Error on insert", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }

        //getTrafficReaderData(title);
    }

    public boolean checkTrafficReaderRow(String entryId)
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c;
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                feeder._ID,
                feeder.COLUMN_NAME_TITLE,
                feeder.COLUMN_NAME_CONTENT,
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                feeder.COLUMN_NAME_ENTRY_ID;

        String selection = feeder.COLUMN_NAME_ENTRY_ID + " = ? ";
        String[] rows = {
                entryId
        };

        c = db.query(
                feeder.TABLE_NAME,                  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                rows,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        if( c.moveToFirst() ) {
            return true;
        }
        else
            return false;
    }

    public String getTrafficReaderData(String entryId, String row)
    {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Cursor c;
        String column = null;
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projection = {
                feeder._ID,
                feeder.COLUMN_NAME_TITLE,
                feeder.COLUMN_NAME_CONTENT,
        };

        // How you want the results sorted in the resulting Cursor
        String sortOrder =
                feeder.COLUMN_NAME_ENTRY_ID;

        String selection = feeder.COLUMN_NAME_ENTRY_ID + " = ? ";
        String[] rows = {
                entryId
        };

        c = db.query(
                feeder.TABLE_NAME,                  // The table to query
                projection,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                rows,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                sortOrder                                 // The sort order
        );

        c.moveToFirst();

        if(c.getLong(c.getColumnIndexOrThrow(feeder._ID)) != -1) {

            //c.moveToPosition(c.getColumnIndexOrThrow(row));

            column = c.getString(c.getColumnIndexOrThrow(row));
        }

        return column;
    }
}
