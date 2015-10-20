package com.example.nick.fantraffic;

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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private List<Traffic> traffic;
    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initializeData();

        mRecyclerView = (RecyclerView)findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(llm);

        RVAdapter adapter = new RVAdapter(traffic);
        mRecyclerView.setAdapter(adapter);

        view = this.view;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "refreshing data", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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

    private void initializeData(){
        String strJsonData = "http://traffic.cit.api.here.com/traffic/6.0/incidents.json" +
                "?app_id=wa1d1f5iSh0BQqHHtLI5" +
                "&app_code=br5kbaTStGduNL9LYzPD8w" +
                "&quadkey=12020330";

        newRequestQueue(strJsonData);

        try {
            InputStream in = new InputStream() {
                @Override
                public int read() throws IOException {
                    return 0;
                }
            };

            in.read(strJsonData.getBytes());

            traffic = readJsonStream(in);
        }catch(IOException ioex)
        {}
        /*
        traffic.add(new Traffic("Very Good", "10"));
        traffic.add(new Traffic("Terrible", "60"));
        traffic.add(new Traffic("Bad", "50"));
        traffic.add(new Traffic("Good", "20"));*/
    }

    public List readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readMessagesArray(reader);
        }catch(Exception ex){}

        finally{
            reader.close();
            return Collections.emptyList();
        }
    }

    public List readMessagesArray(JsonReader reader) throws IOException {
        List messages = new ArrayList();

        reader.beginArray();
        while (reader.hasNext()) {
            messages.add(readTraffic(reader));
        }
        reader.endArray();
        return messages;
    }

    public Traffic readTraffic(JsonReader reader) throws IOException {
        long id = -1;
        Traffic traffic = null;
        String flow = null;
        String duration = null;

        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("id")) {
                id = reader.nextLong();
            } else if (name.equals("text")) {
                flow = reader.nextString();
            } else if (name.equals("text")) {
                duration = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        return new Traffic(flow, duration);
    }

    public void newRequestQueue(String url){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Snackbar.make(view, "Response is: "+ response.substring(0,500), Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Snackbar.make(view, "That didn't work!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }
}
