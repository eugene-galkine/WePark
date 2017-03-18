package com.eg.upark;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.eg.upark.loc.CameraPosSetter;
import com.eg.upark.loc.MyLocationListener;
import com.eg.upark.net.Client;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback
{
    public static MainActivity getInstance() {
        return instance;
    }

    //memory leak is lie because MainActivity stays alive forever
    private static MainActivity instance;

    private GoogleMap mMap;
    private android.location.LocationManager LocationManager;
    private MyLocationListener LocationListener;
    private LatLng parkLoc;
    private Button mainButton;
    private Button secondButton;
    private ViewFlipper vf;
    private CameraPosSetter parkingList;
    private LoginHandler loginHandler;

    private int loadProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView loadingImg = (ImageView) findViewById(R.id.imageView);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        instance = this;
        loadProgress = 0;
        //get button
        vf = (ViewFlipper) findViewById(R.id.viewFlipper);
        mainButton = (Button) findViewById(R.id.bParkCar);
        secondButton = (Button) findViewById(R.id.bFind);

        //init location detection
        LocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener = new MyLocationListener();
        enableLocationTracking();

        loginHandler = new LoginHandler(getPreferences(MODE_PRIVATE), getContentResolver());

        Log.i("WePark", "wtf is this shit");
    }


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
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        //request permission to get location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && (checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    2);//i have no idea what this number does but it's needed
        }
        //enable location tracking on the map
        mMap.setMyLocationEnabled(true);

        //init the map
        MapsInitializer.initialize(this);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        System.out.println("resume" +
                "");
        //resume client
        Client.instance.resume();
        //login
        //loginHandler.logout();

        //resume location updates
        enableLocationTracking();
    }


    @Override
    protected void onPause()
    {
        super.onPause();
        System.out.println("pause");
        //pause client
        Client.instance.close();

        //stop location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            LocationManager.removeUpdates(LocationListener);
    }

    /* On screen button click functions */
    public void MainButtonClick(View v)
    {
        mMap.clear();
        if (mainButton.getText().toString().startsWith("Leave"))
            //leave parking
            Client.instance.clearParking();
        else
            //park here
            Client.instance.parkCar(LocationListener.getLatitude(), LocationListener.getLongitude());
    }

    public void QueryParkingClick(View v)
    {
        mMap.clear();
        if (secondButton.getText().toString().endsWith("Parking"))
        {
            //find parking
            Client.instance.getAvailableParking(LocationListener.getLatitude(), LocationListener.getLongitude());
            parkingList = new CameraPosSetter(LocationListener.getLatLng());
        }
        else
            //find car
            Client.instance.findCar();
    }

    public void SettingsClick(View v)
    {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    /* class functions */

    private void enableLocationTracking()
    {
        //get permission to use gps
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && (checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED))
        {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    2);

            enableLocationTracking();
        } else
        {
            //listen to location updates
            LocationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 1000, 0, LocationListener);
            LocationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 1000, 0, LocationListener);
        }
    }

    private void load()
    {
        loadProgress++;

        if (loadProgress >= 1)
        {
            while (LocationListener.getLatitude() == 0 && LocationListener.getLongitude() == 0) ;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    vf.setDisplayedChild(0);
                    vf.showNext();
                    //zoom map in to user pos
                    if (mMap != null)
                        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition(LocationListener.getLatLng(), 15, 0, 0)));
                }
            });
        }
    }

    /* Public functions */

    public void logout()
    {
        vf.showPrevious();

        Client.instance.close();
        //client = null;

        loginHandler.logout();
    }

    private void setClient (String user, String pass)
    {
        //client = new Client(user, pass);
        //new Thread(client).start();
    }

    public void setClient (String id)
    {
        //client = new Client(id);
        //new Thread(client).start();
        Client.instance.connect(id);
    }

    /* UI functions */

    public void ConnectionError(final String type)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                loginHandler.dismissDialog();

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                // Add the button
                builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        loginHandler = new LoginHandler(getPreferences(MODE_PRIVATE), getContentResolver());
                    }
                });
                builder.setTitle("Error");
                builder.setMessage(type);

                builder.create().show();
            }
        });
    }

    public void ConnectionDenied()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                loginHandler.loginFailed();
                Toast.makeText(MainActivity.this, "Invalid Username or Password", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void newMessage(final String in)
    {
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                if (in.length() > 1)
                Toast.makeText(MainActivity.this, "New message: " + in + " in.substring: " + in.substring(1), Toast.LENGTH_LONG).show();

            }
        });
    }

    public void ConnectionEstablished()
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_LONG).show();
                loginHandler.loginSuccess();
            }
        });

        load();
    }

    public void SetIsParked(final boolean isParked)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mMap != null)
                    mMap.clear();
                Toast.makeText(MainActivity.this, "setting is parked to " + isParked, Toast.LENGTH_LONG).show();
                mainButton.setText(isParked ? "Leave Parking" : "Park Here");
                secondButton.setText(isParked ? "Find Car" : "Find Parking");
            }
        });
    }

    public void SetParkLocation(final String data)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Double latitude = Double.valueOf(data.substring(0, data.indexOf(',')));
                Double longitude = Double.valueOf(data.substring(data.indexOf(',') + 1, data.length()));
                parkLoc = new LatLng(latitude, longitude);
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(parkLoc).title("Car Location")).setIcon(BitmapDescriptorFactory.fromResource(R.drawable.my_car_img));

                parkingList = new CameraPosSetter(LocationListener.getLatLng());
                mMap.animateCamera(parkingList.AddLatLng(parkLoc));
            }
        });
    }

    public void NewParkingFound(final String data)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                double lat = Double.valueOf(data.substring(0, data.indexOf(",")));
                String data1 = data.substring(data.indexOf(",") + 1);
                double lng = Double.valueOf(data1.substring(0, data1.indexOf(",")));
                data1 = data1.substring(data1.indexOf(",") + 1);

                //TODO: server decide what parking spots to send user based on distance
                if (LocationListener.distanceTo(lat, lng) > 0.08)//only show parking in a ~10 mile radius (longitude is shorter than latitude)
                    return;

                int minutesAgo = (int) Double.parseDouble(data1);
                int color = 0x000000FF;//blue

                //calculate the color
                if (minutesAgo <= 20)
                {
                    int red = (int)(12f*minutesAgo);
                    int green = 0xCF - red;

                    color = (red << 16) | (green << 8);
                }

                //set the color of the icon
                Paint p = new Paint();
                ColorFilter filter = new LightingColorFilter(0, color);
                p.setColorFilter(filter);
                Bitmap mutableBitMap = BitmapFactory.decodeResource(getResources(), R.drawable.open_parking_img).copy(Bitmap.Config.ARGB_8888, true);
                Canvas canvas = new Canvas(mutableBitMap);
                canvas.drawBitmap(mutableBitMap, 0, 0, p);

                parkLoc = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().position(parkLoc).title("Open Parking").icon(BitmapDescriptorFactory.fromBitmap(mutableBitMap))).setSnippet("Opened " + minutesAgo + " minutes ago");

                mMap.animateCamera(parkingList.AddLatLng(parkLoc));
            }
        });
    }
}

//TODO: fix warning in manifest
//TODO: conform this to object oriented design
//TODO: fix warnings about permission request
//TODO: acknowledge icons8.com
//<a href="https://icons8.com/web-app/5627/Car">Car icon credits</a>
//<a href="https://icons8.com/web-app/5590/Settings">Settings icon credits</a>
//<a href="https://icons8.com/web-app/34614/parking">Parking Sign icon credits</a>
//TODO: icon for app
