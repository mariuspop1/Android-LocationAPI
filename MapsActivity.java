package fr.eurecom.android.locationservices3;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    static final LatLng NICE= new LatLng(43.7031,7.02661);
    static final LatLng EURECOM = new LatLng(43.614376,7.070450);
    protected LocationManager locationManager = null;
    private String provider;
    Location location;
    TextView latitudeField;
    TextView longitudeField;
    public static final int MY_PERMISSIONS_LOCATION = 0;
    Marker currentMarker = null;
    PendingIntent pendingIntent;
    public SharedPreferences sharedPreferences;
    private static final String PROX_ALERT_INTENT =
            "fr.eurecom.locationservices.android.lbs.ProximityAlert";
    private static final String POINT_LATITUDE_KEY = "POINT_LATITUDE_KEY";
    private static final String POINT_LONGITUDE_KEY = "POINT_LONGITUDE_KEY";
    private int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

        Criteria criteria = new Criteria();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(criteria, false);
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this.getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.i("Permission: ", "To be checked");
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    MY_PERMISSIONS_LOCATION);
            return;
        } else
            Log.i("Permission: ", "GRANTED");
        latitudeField = (TextView) findViewById(R.id.TextView02);
        longitudeField = (TextView) findViewById(R.id.TextView04);
        location = locationManager.getLastKnownLocation(provider);
        if (locationManager== null)
            locationManager =
                    (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(criteria, false);
        location = locationManager.getLastKnownLocation(provider);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 1, locationListenerGPS);
        sharedPreferences = getSharedPreferences("location",0);


    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_LOCATION: {
// If request is cancelled, the result arrays are empty.

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i("Access:","Now permissions are granted");
                    // permission was granted, yay!
                } else {
                    Log.i("Access:"," permissions are denied");
                    //disable the functionality that depends on this permission.
                }
                break;
            }
// other 'case' lines to check for other permissions this app might request
        }
    }
    public void showLocation(View view){
        Log.i("showLocation", "Entered");
        switch (view.getId()){
            case R.id.button01:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.button02:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;


        }

    }
    public void updateLocationView(){
        if (location != null){
            double lat = location.getLatitude();
            double lng = location.getLongitude();
            //latitudeField.setText(String.valueOf(lat));
            //longitudeField.setText(String.valueOf(lng));

            //String msg="New Latitude: "+lat + "             New Longitude: "+lng;
            //Toast.makeText(getBaseContext(),msg,Toast.LENGTH_LONG).show();
            Log.i("Location","LOCATION CHANGED!!!");
        } else{
            Log.i("showLocation","NULL");
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListenerGPS);

    }



    LocationListener locationListenerGPS=new LocationListener() {
        @Override
        public void onLocationChanged(android.location.Location location) {
           // Log.i("Location","LOCATION CHANGED!!!");
            double latitude=location.getLatitude();
            double longitude=location.getLongitude();
            //String msg="New Latitude: "+latitude + "          New Longitude: "+longitude;
           // Toast.makeText(getBaseContext(),msg,Toast.LENGTH_LONG).show();
            latitudeField.setText(String.valueOf(latitude));
            longitudeField.setText(String.valueOf(longitude));

            String msg= "Location Changed !!";
            Toast.makeText(getBaseContext(),msg,Toast.LENGTH_LONG).show();
            updateLocationView();

            if (currentMarker!=null) {
                currentMarker.remove();
                currentMarker=null;
            }
            if (currentMarker==null) {
                LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
                MarkerOptions markerOptions= new MarkerOptions().position(latLng)
                        .title("I am Here").snippet("Home sweet Home").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,5));
                currentMarker= mMap.addMarker(markerOptions);

            }




        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText(getBaseContext(), "Enabled new provider " + provider,
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText(getBaseContext(), "Disabled provider " + provider,
                    Toast.LENGTH_SHORT).show();


        }
    };


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */



    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap=googleMap;
        /*
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(EURECOM)
                .zoom(17)
                .bearing(90)
                .tilt(30)
                .build();

         */
        googleMap.addMarker(new MarkerOptions()
                .position(NICE)
                .title("Nice")
                .snippet("Enjoy French Riviera"));
        googleMap.addMarker(new MarkerOptions()
                .position(EURECOM)
                .title("EURECOM")
                .snippet("ENJOY STUDY!"));

        LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
        MarkerOptions markerOptions= new MarkerOptions().position(latLng)
            .title("I am Here").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,5));
        currentMarker= mMap.addMarker(markerOptions);
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng arg0) {
                googleMap.addMarker(new MarkerOptions()
                        .position(arg0)
                        .title("proximity point")
                        );
                // TODO Auto-generated method stub
                //String msg="YOU CLICKED ON THE MAP : "+arg0;
               // Toast.makeText(getBaseContext(),msg,Toast.LENGTH_LONG).show();
                Intent intent = new Intent(PROX_ALERT_INTENT);
                PendingIntent proximityIntent =
                        PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);
                if (ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(getApplicationContext(),
                                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    Log.i("Permission: ", "To be checked");
                    return;
                } else {
                    Log.i("Permission: ", "GRANTED");
                }
                saveCoordinatesInPreferences((float) arg0.latitude,
                        (float) arg0.longitude);
                locationManager.addProximityAlert(arg0.latitude, arg0.longitude,
                        100000, -1, proximityIntent);
                IntentFilter filter = new IntentFilter(PROX_ALERT_INTENT);
                registerReceiver(new eurecom.fr.locationservices.ProximityIntentReceiver(), filter);
                Log.i("Registred", "proximity");
                String msg="Added a proximity Alert : "+arg0;
                Toast.makeText(getBaseContext(), msg,
                        Toast.LENGTH_LONG).show();
                ++count;
            }
            private void saveCoordinatesInPreferences(float latitude, float longitude) {
                SharedPreferences prefs =
                        getBaseContext().getSharedPreferences(getClass().getSimpleName(),
                                Context.MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = prefs.edit();
                prefsEditor.putFloat(POINT_LATITUDE_KEY, latitude);
                prefsEditor.putFloat(POINT_LONGITUDE_KEY, longitude);
                prefsEditor.commit();

            }

        });

    }
    @Override protected void onStart(){
        super.onStart();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!gpsEnabled){
            Log.i("GPS", "not enabled");
            enableLocationSettings();
        }else{
            Log.i("GPS","enabled");
        }
    }
    private void enableLocationSettings(){
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

}
