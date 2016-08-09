package com.procasy.dubarah_nocker;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.procasy.dubarah_nocker.API.APIinterface;
import com.procasy.dubarah_nocker.API.ApiClass;
import com.procasy.dubarah_nocker.Activity.Nocker.MyProfileActivity;
import com.procasy.dubarah_nocker.Fragments.FragmentDrawerNocker;
import com.procasy.dubarah_nocker.Fragments.FragmentDrawerUser;
import com.procasy.dubarah_nocker.Fragments.MainFragment;
import com.procasy.dubarah_nocker.Helper.SessionManager;
import com.procasy.dubarah_nocker.Model.Responses.InfoNockerResponse;
import com.procasy.dubarah_nocker.Services.LocationService;

import cc.cloudist.acplibrary.ACProgressConstant;
import cc.cloudist.acplibrary.ACProgressFlower;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements LocationListener, CommuncationChannel {

    DrawerLayout mDrawerLayout;
    private Toolbar mtoolbar;
    private FragmentDrawerNocker drawerFragment;
    private FragmentDrawerUser drawerUser;
    private Context mContext;
    private ImageView message, drawer, notification;
    // flag for GPS status
    boolean isGPSEnabled = false;
    // flag for network status
    boolean isNetworkEnabled = false;
    // flag for GPS status
    boolean canGetLocation = false;
    Location location; // location
    double latitude; // latitude
    double longitude; // longitude
    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    // Declaring a Location Manager
    protected LocationManager locationManager;
    APIinterface apiService;
    SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mtoolbar = (Toolbar) findViewById(R.id.toolbar);

        message = (ImageView) mtoolbar.findViewById(R.id.message);
        notification = (ImageView) mtoolbar.findViewById(R.id.notification);
        drawer = (ImageView) mtoolbar.findViewById(R.id.drawer_btn);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        sessionManager = new SessionManager(this);


        final ACProgressFlower dialog = new ACProgressFlower.Builder(MainActivity.this)
                .direction(ACProgressConstant.DIRECT_CLOCKWISE)
                .themeColor(Color.WHITE)
                .text("Getting Info..")
                .fadeColor(Color.DKGRAY).build();
        dialog.show();
        APIinterface apiService = ApiClass.getClient().create(APIinterface.class);
        Call<InfoNockerResponse> call = apiService.GetInfoNocker(sessionManager.getEmail(),sessionManager.getUDID());
        call.enqueue(new Callback<InfoNockerResponse>() {
            @Override
            public void onResponse(Call<InfoNockerResponse> call, Response<InfoNockerResponse> response) {
           //     System.out.println(response.body().getUser().toString());

                sessionManager.setEmail(response.body().getUser().getUser_email());
                sessionManager.setFName(response.body().getUser().getUser_fname());
                sessionManager.setLName(response.body().getUser().getUser_lname());
                sessionManager.setPP(response.body().getUser().getUser_img());
                sessionManager.setAVG(response.body().getAvg_charge());
                sessionManager.setKeyIsNocker(response.body().getUser().is_nocker());
                Log.d("nocker_data", response.body().toString() + "");


                Log.e("user_img",sessionManager.getPP()+" ff");

                if (dialog.isShowing())
                    dialog.dismiss();
            }

            @Override
            public void onFailure(Call<InfoNockerResponse> call, Throwable t) {
                System.out.println("here 2" + t.toString());
                if (dialog.isShowing())
                    dialog.dismiss();
            }

        });


        setSupportActionBar(mtoolbar);
        if (sessionManager.getKeyNocker() == 1) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_navigation_drawer, new FragmentDrawerNocker()).commit();
        } else {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_navigation_drawer, new FragmentDrawerUser()).commit();
        }
        setTitle("");
        mContext = getApplicationContext();
        marshmallowGPSPremissionCheck();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container_body, new MainFragment()).commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 5 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getLocation();
            startService(new Intent(this, LocationService.class));
        }
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);
            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return location;
    }

    private void marshmallowGPSPremissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && getApplicationContext().checkSelfPermission(
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && getApplicationContext().checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION},
                    5);
        } else {

       //     Log.e("Loction", getLocation().toString());
            startService(new Intent(this, LocationService.class));
        }
    }

    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }
        // return latitude
        return latitude;
    }

    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }
        // return longitude
        return longitude;
    }

    public boolean canGetLocation() {
        return this.canGetLocation;
    }
    
    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);
        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");
        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });
        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location currentLocation) {
// TODO Auto-generated method stub
        this.location = currentLocation;
        getLatitude();
        getLongitude();

    }

    @Override
    public void onProviderDisabled(String provider) {
// TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
// TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
// TODO Auto-generated method stub
    }

    @Override
    public void setCommunication(String msg) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        switch (msg) {
            case "activateSub":
                fragmentManager.beginTransaction().replace(R.id.container_body, new MainFragment()).commit();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case "myShop":
                startActivity(new Intent(getApplicationContext(), MyProfileActivity.class));
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case "promotion":
                fragmentManager.beginTransaction().replace(R.id.container_body, new MainFragment()).commit();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;
            case "help":
                fragmentManager.beginTransaction().replace(R.id.container_body, new MainFragment()).commit();
                drawerLayout.closeDrawer(GravityCompat.START);
                sessionManager.setEmail("");
                sessionManager.setPassword("");
                sessionManager.setLogin(false);
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                break;
            case "settings":
                fragmentManager.beginTransaction().replace(R.id.container_body, new MainFragment()).commit();
                drawerLayout.closeDrawer(GravityCompat.START);

                break;
            case "editProfile":
                fragmentManager.beginTransaction().replace(R.id.container_body, new MainFragment()).commit();
                drawerLayout.closeDrawer(GravityCompat.START);

                break;
        }
    }
}
