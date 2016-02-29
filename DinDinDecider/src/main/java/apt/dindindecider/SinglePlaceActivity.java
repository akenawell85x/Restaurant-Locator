package apt.dindindecider;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;



public class SinglePlaceActivity extends FragmentActivity {
    Boolean isInternetPresent = false;
    ImageButton btnGetNav;
    ImageButton btnMakeCall;
    ConnectionDetector cd;
    AlertDialogManager alert = new AlertDialogManager();
    GooglePlaces googlePlaces;
    public GoogleMap map;
    LatLng USER;
    public LatLng LOCATION;
    PlaceDetails placeDetails;
    ProgressDialog pDialog;
    public static String KEY_REFERENCE = "reference";
    public int userIcon, locationIcon;
    public String dstAdd, dstPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_place);
        userIcon = R.drawable.mark_red;
        locationIcon = R.drawable.mark_blue;

        Intent i = getIntent();

        final String user_latitude = i.getStringExtra("user_latitude");
        final String user_longitude = i.getStringExtra("user_longitude");
        String user_location = (user_latitude + ", " + user_longitude);
        final String reference = i.getStringExtra(KEY_REFERENCE);

        new LoadSinglePlaceDetails().execute(reference);

        btnGetNav = (ImageButton) findViewById(R.id.btn_get_nav);
        btnMakeCall = (ImageButton) findViewById(R.id.btn_make_call);

        btnGetNav.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String geoUriString = "http://maps.google.com/maps?" +
                        "saddr=" + user_latitude + "," + user_longitude +
                        "&daddr=" + dstAdd;

                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUriString));
                startActivity(i);
            }
        });

        btnMakeCall.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String phnUriString = "tel:" + dstPhone;

                Intent i = new Intent(Intent.ACTION_CALL, Uri.parse(phnUriString));
                startActivity(i);
            }
        });

        map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapSingleView))
                .getMap();

        USER = new LatLng((double) (Double.parseDouble(user_latitude)),
                (double) (Double.parseDouble(user_longitude)));

        Marker user = map.addMarker(new MarkerOptions().position(USER)
                .title("This is you.")
                .snippet(user_location)
                .icon(BitmapDescriptorFactory
                        .fromResource(userIcon)));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(USER, 15));

        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

    }

    class LoadSinglePlaceDetails extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SinglePlaceActivity.this);
            pDialog.setMessage("Loading profile ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }

        protected String doInBackground(String... args) {
            String reference = args[0];

            googlePlaces = new GooglePlaces();

            try {
                placeDetails = googlePlaces.getPlaceDetails(reference);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onPostExecute(String file_url) {
            pDialog.dismiss();
            runOnUiThread(new Runnable() {
                public void run() {
                    if(placeDetails != null){
                        String status = placeDetails.status;
                        if(status.equals("OK")){
                            if (placeDetails.result != null) {
                                String name = placeDetails.result.name;
                                String address = placeDetails.result.formatted_address;
                                String phone = placeDetails.result.formatted_phone_number;
                                String latitude = Double.toString(placeDetails.result.geometry.location.lat);
                                String longitude = Double.toString(placeDetails.result.geometry.location.lng);

                                Log.d("Place ", name + address + phone + latitude + longitude);

                                TextView lbl_name = (TextView) findViewById(R.id.name);
                                TextView lbl_address = (TextView) findViewById(R.id.address);
                                TextView lbl_phone = (TextView) findViewById(R.id.phone);

                                name = name == null ? "Not present" : name;
                                address = address == null ? "Not present" : address;
                                phone = phone == null ? "Not present" : phone;

                                lbl_name.setText(name);
                                lbl_address.setText(address);
                                lbl_phone.setText(Html.fromHtml("<b>Phone:</b> " + phone));

                                LOCATION = new LatLng(placeDetails.result.geometry.location.lat,
                                        placeDetails.result.geometry.location.lng);

                                Marker location = map.addMarker(new MarkerOptions().position(LOCATION)
                                        .title(name)
                                        .snippet(address)
                                        .icon(BitmapDescriptorFactory
                                                .fromResource(locationIcon)));

                                dstAdd = placeDetails.result.formatted_address;
                                dstPhone = placeDetails.result.formatted_phone_number;
                            }
                        }
                        else if(status.equals("ZERO_RESULTS")){
                            alert.showAlertDialog(SinglePlaceActivity.this, "Near Places",
                                    "Sorry no place found.",
                                    false);
                        }
                        else if(status.equals("UNKNOWN_ERROR"))
                        {
                            alert.showAlertDialog(SinglePlaceActivity.this, "Places Error",
                                    "Sorry unknown error occured.",
                                    false);
                        }
                        else if(status.equals("OVER_QUERY_LIMIT"))
                        {
                            alert.showAlertDialog(SinglePlaceActivity.this, "Places Error",
                                    "Sorry query limit to google places is reached",
                                    false);
                        }
                        else if(status.equals("REQUEST_DENIED"))
                        {
                            alert.showAlertDialog(SinglePlaceActivity.this, "Places Error",
                                    "Sorry error occured. Request is denied",
                                    false);
                        }
                        else if(status.equals("INVALID_REQUEST"))
                        {
                            alert.showAlertDialog(SinglePlaceActivity.this, "Places Error",
                                    "Sorry error occured. Invalid Request",
                                    false);
                        }
                        else
                        {
                            alert.showAlertDialog(SinglePlaceActivity.this, "Places Error",
                                    "Sorry error occured.",
                                    false);
                        }
                    }
                    else{
                        alert.showAlertDialog(SinglePlaceActivity.this, "Places Error",
                                "Sorry error occured.",
                                false);
                    }


                }
            });

        }

    }

}

