package apt.dindindecider;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.SupportMapFragment;

public class PlacesMapActivity extends FragmentActivity {
    double latitude;
    double longitude;
    LatLng USER=null;
    LatLng LOCATION=null;
    PlacesList nearPlaces;
    private GoogleMap map;
    private int userIcon, locationIcon;
    String address;
    String name;

    @SuppressWarnings("unused")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_places);

        userIcon = R.drawable.mark_red;
        locationIcon = R.drawable.mark_blue;

        Intent i = getIntent();

        String user_latitude = i.getStringExtra("user_latitude");
        String user_longitude = i.getStringExtra("user_longitude");
        String user_location = (user_latitude + ", " + user_longitude);

        SupportMapFragment fm = (SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.mapView);
        map = fm.getMap();

        USER = new LatLng((double) (Double.parseDouble(user_latitude)),
                (double) (Double.parseDouble(user_longitude)));

        Marker user = map.addMarker(new MarkerOptions().position(USER)
                .title("This is you.")
                .snippet("You are here!")
                .icon(BitmapDescriptorFactory
                        .fromResource(userIcon)));

        nearPlaces = (PlacesList) i.getSerializableExtra("near_places");
        if(nearPlaces != null) {
            for(Place place : nearPlaces.results) {
                latitude = place.geometry.location.lat;
                longitude = place.geometry.location.lng;
                name = place.name;
                address= place.vicinity;
            }

            final LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for(int c = 0; c < nearPlaces.results.size(); c++){
                final LatLng pos = new LatLng(nearPlaces.results.get(c).geometry.location.lat,
                        nearPlaces.results.get(c).geometry.location.lng);
                builder.include(pos);
                map.addMarker(new MarkerOptions().position(pos)
                        .title(nearPlaces.results.get(c).name)
                        .snippet(nearPlaces.results.get(c).vicinity)
                        .icon(BitmapDescriptorFactory
                                .fromResource(locationIcon)));
            }
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(USER, 16));

        map.animateCamera(CameraUpdateFactory.zoomTo(11), 2000, null);
    }
}

