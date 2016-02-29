package apt.dindindecider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;


public class MainActivity extends Activity {
    Boolean isInternetPresent = false;
    ConnectionDetector cd;
    AlertDialogManager alert = new AlertDialogManager();
    GooglePlaces googlePlaces;
    PlacesList nearPlaces;
    GPSTracker gps;
    Button btnShowOnMap;
    Button btnGetRand;
    ProgressDialog pDialog;
    ListView lv;
    ArrayList<HashMap<String, String>> placesListItems = new ArrayList<HashMap<String,String>>();


    public static String KEY_REFERENCE = "reference";
    public static String KEY_NAME = "name";
    public static String KEY_VICINITY = "vicinity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cd = new ConnectionDetector(getApplicationContext());

        isInternetPresent = cd.isConnectingToInternet();
        if (!isInternetPresent) {
            alert.showAlertDialog(MainActivity.this, "Internet Connection Error",
                    "Please connect to working Internet connection", false);
            return;
        }

        gps = new GPSTracker(this);

        if (gps.canGetLocation()) {
            Log.d("Your Location", "latitude:" + gps.getLatitude() + ", longitude: " + gps.getLongitude());
        }
        else {
            alert.showAlertDialog(MainActivity.this, "GPS Status",
                    "Couldn't get location information. Please enable GPS",
                    false);
            return;
        }

        lv = (ListView) findViewById(R.id.list);

        btnShowOnMap = (Button) findViewById(R.id.btn_show_map);

        new LoadPlaces().execute();

        btnShowOnMap.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent i = new Intent(getApplicationContext(),
                        PlacesMapActivity.class);
                i.putExtra("user_latitude", Double.toString(gps.getLatitude()));
                i.putExtra("user_longitude", Double.toString(gps.getLongitude()));
                i.putExtra("near_places", nearPlaces);

                startActivity(i);
            }
        });

        btnGetRand = (Button) findViewById(R.id.btn_get_rand);

        btnGetRand.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                Random randomGenerator = new Random();
                int count = placesListItems.size();
                int r = randomGenerator.nextInt(count);
                String reference = nearPlaces.results.get(r).reference;

                Intent i = new Intent(getApplicationContext(),
                        SinglePlaceActivity.class);
                i.putExtra(KEY_REFERENCE, reference);
                i.putExtra("user_latitude", Double.toString(gps.getLatitude()));
                i.putExtra("user_longitude", Double.toString(gps.getLongitude()));
                startActivity(i);
            }
        });

        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String reference = ((TextView) view.findViewById(R.id.reference)).getText().toString();

                Intent i = new Intent(getApplicationContext(),
                        SinglePlaceActivity.class);
                i.putExtra(KEY_REFERENCE, reference);
                i.putExtra("user_latitude", Double.toString(gps.getLatitude()));
                i.putExtra("user_longitude", Double.toString(gps.getLongitude()));
                startActivity(i);
            }
        });
    }

    class LoadPlaces extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage(Html.fromHtml("<b>Search</b><br/>Loading Places..."));
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            googlePlaces = new GooglePlaces();

            try {
                String types = "restaurant|cafe|bar|meal_takeaway|meal_delivery";

                double radius = 15000;

                nearPlaces = googlePlaces.search(gps.getLatitude(),
                        gps.getLongitude(), radius, types);


            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();

            runOnUiThread(new Runnable() {
                public void run() {
                    String status = nearPlaces.status;
                    if(status.equals("OK")){

                        if (nearPlaces.results != null) {

                            for (Place p : nearPlaces.results) {
                                HashMap<String, String> map = new HashMap<String, String>();

                                map.put(KEY_REFERENCE, p.reference);

                                map.put(KEY_NAME, p.name);

                                map.put(KEY_VICINITY, p.vicinity);

                                placesListItems.add(map);
                            }

                            ListAdapter adapter = new SimpleAdapter(MainActivity.this, placesListItems,
                                    R.layout.list_item,
                                    new String[] { KEY_REFERENCE, KEY_NAME, KEY_VICINITY}, new int[] {
                                    R.id.reference, R.id.name, R.id.address });

                            lv.setAdapter(adapter);
                        }
                    }
                    else if(status.equals("ZERO_RESULTS")){
                        alert.showAlertDialog(MainActivity.this, "Near Places",
                                "Sorry no places found. Try to change the types of places",
                                false);
                    }
                    else if(status.equals("UNKNOWN_ERROR"))
                    {
                        alert.showAlertDialog(MainActivity.this, "Places Error",
                                "Sorry unknown error occured.",
                                false);
                    }
                    else if(status.equals("OVER_QUERY_LIMIT"))
                    {
                        alert.showAlertDialog(MainActivity.this, "Places Error",
                                "Sorry query limit to google places is reached",
                                false);
                    }
                    else if(status.equals("REQUEST_DENIED"))
                    {
                        alert.showAlertDialog(MainActivity.this, "Places Error",
                                "Sorry error occured. Request is denied",
                                false);
                    }
                    else if(status.equals("INVALID_REQUEST"))
                    {
                        alert.showAlertDialog(MainActivity.this, "Places Error",
                                "Sorry error occured. Invalid Request",
                                false);
                    }
                    else
                    {
                        alert.showAlertDialog(MainActivity.this, "Places Error",
                                "Sorry error occured.",
                                false);
                    }
                }
            });

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }



}

