package com.example.nick.fantraffic;

import android.app.ProgressDialog;
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
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RVAdapter adapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Traffic> traffic;

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

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST);
        mRecyclerView.addItemDecoration(itemDecoration);

        adapter = new RVAdapter(traffic);
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
        initializeData();
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
        String strHttpJsonResponse;

        strJsonData = "http://traffic.cit.api.here.com/traffic/6.0/incidents.json" +
                "?app_id=wa1d1f5iSh0BQqHHtLI5" +
                "&app_code=br5kbaTStGduNL9LYzPD8w" +
                "&quadkey=0313113200";
        //strJsonData = "http://api.androidhive.info/volley/person_array.json";

        newRequestQueue(strJsonData);
    }

    public void newRequestQueue(String url){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        // Request a string response from the provided URL.

        JsonArrayRequest JsonRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
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

    public void populateTrafficData(JSONArray json)
    {
        try {
            //traffic = new ArrayList<>();
            for(int i = 0; i < 2; i++) {
                traffic.add(new Traffic(json.getJSONObject(i).getString("name"), json.getJSONObject(i).getString("email")));
            }
        }catch (JSONException jex)
        {}
        adapter.addItemsToList(traffic);
        adapter.notifyDataSetChanged();
    }
}
