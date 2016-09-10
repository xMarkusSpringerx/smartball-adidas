package com.adidas.sb.hackathon;

import android.Manifest;

import android.os.StrictMode;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Movie;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.adidas.sensors.api.Sensor;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class JuggleLeaderboardActivity extends AppCompatActivity {

    private ListView theListView;
    private Sensor sensor;
    private LinearLayoutManager lLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_juggle_leaderboard_2);

        Intent in = getIntent();
        sensor = in.getParcelableExtra(getString(R.string.INTENT_SELECTED_SENSOR));

        // check location permission
        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }

        List<ItemObject> rowListItem = getAllItemList();

        lLayout = new LinearLayoutManager(JuggleLeaderboardActivity.this);

        RecyclerView rView = (RecyclerView)findViewById(R.id.recycler_view);
        rView.setLayoutManager(lLayout);

        RecyclerViewAdapter rcAdapter = new RecyclerViewAdapter(JuggleLeaderboardActivity.this, rowListItem);
        rView.setAdapter(rcAdapter);

        theListView = (ListView) findViewById(R.id.leaderboard_listview);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    /*    // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if(id == R.id.action_refresh){
            Toast.makeText(JuggleLeaderboardActivity.this, "Refresh App", Toast.LENGTH_LONG).show();
        }
        if(id == R.id.action_new){
            Toast.makeText(JuggleLeaderboardActivity.this, "Create Text", Toast.LENGTH_LONG).show();
        }
        return super.onOptionsItemSelected(item);
    */
        return true;
    }

    private List<ItemObject> getAllItemList() {

<<<<<<< 5b55f42c7d439df48a723d98c8015bc858a73ac7
        List<ItemObject> allItems = new ArrayList<ItemObject>();

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        String sURL = "http://10.25.28.202:1337/highscores"; //just a string

        try {
            // Connect to the URL using java's native library
            URL url = new URL(sURL);
            HttpURLConnection request = (HttpURLConnection) url.openConnection();
            request.setRequestProperty( "Content-Type", "application/json" );
            request.setRequestProperty("Accept", "application/json");

            request.connect();
            // Convert to a JSON object to print data
            JsonParser jp = new JsonParser(); //from gson

            JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent())); //Convert the input stream to a json element

            JsonArray t = root.getAsJsonArray();

            for(int i  = 0; i < t.size(); i++) {
                JsonElement e = t.get(i);
                JsonObject o = e.getAsJsonObject();
                String name = o.get("name").toString();
                String score = o.get("score").toString();

                allItems.add(new ItemObject(name, score, R.drawable.face));
            }

            return allItems;

        } catch (Exception e) {
            e.printStackTrace();
        }
=======
        List<ItemObject> allItems = new ArrayList<>();
        allItems.add(new ItemObject("Lionel Messi", "55", R.drawable.leo));
        allItems.add(new ItemObject("Lukas", "51", R.drawable.lukas));
        allItems.add(new ItemObject("Markus", "21", R.drawable.markus));
<<<<<<< 6e3f112bcfae218529deafe71f7b5e3575dc2515
        allItems.add(new ItemObject("Chidi Johnson", "20", R.drawable.face));
        allItems.add(new ItemObject("DeGordio Puritio", "17", R.drawable.face));
        allItems.add(new ItemObject("Gary Cook", "15", R.drawable.face));
        allItems.add(new ItemObject("Edith Helen", "12", R.drawable.face));
        allItems.add(new ItemObject("Kingston Dude", "10", R.drawable.face));
        allItems.add(new ItemObject("Edwin Bent", "8", R.drawable.face));
        allItems.add(new ItemObject("Grace Praise", "1", R.drawable.face));
>>>>>>> remove checkbox kickdetected as it is obsolete
=======
        allItems.add(new ItemObject("Chidi Johnson", "20", R.drawable.unknown));
        allItems.add(new ItemObject("DeGordio Puritio", "17", R.drawable.unknown));
        allItems.add(new ItemObject("Gary Cook", "15", R.drawable.unknown));
        allItems.add(new ItemObject("Edith Helen", "12", R.drawable.unknown));
        allItems.add(new ItemObject("Kingston Dude", "10", R.drawable.unknown));
        allItems.add(new ItemObject("Edwin Bent", "8", R.drawable.unknown));
        allItems.add(new ItemObject("Grace Praise", "1", R.drawable.unknown));
>>>>>>> add unknown user avatars to example data

        return allItems;
    }

    public void btnRecordJuggleClick(View view) {
        Intent iActivityJuggle = new Intent(this, JuggleActivity.class);
        iActivityJuggle.putExtra(getString(R.string.INTENT_SELECTED_SENSOR), sensor);
        startActivity(iActivityJuggle);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //start scanning when the activity is in foreground
    }

    @Override
    protected void onPause() {
        super.onPause();
        //stop scanning when this activity is in background
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            Toast.makeText(this, "App will not work without location permission. Please restart the app and grant the permission.", Toast.LENGTH_LONG).show();
        }
    }

}
