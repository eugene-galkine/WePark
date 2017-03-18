package com.eg.upark.loc;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Eugene Galkine on 8/4/16.
 */
public class MyLocationListener implements LocationListener
{
    private Location LastLoc;

    public MyLocationListener()
    {
        LastLoc = null;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        LastLoc = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

    @Override
    public void onProviderEnabled(String s) {}

    @Override
    public void onProviderDisabled(String s) {}

    public double getLatitude ()
    {
        if (LastLoc == null)
            return 0;

        return LastLoc.getLatitude();
    }

    public double getLongitude ()
    {
        if (LastLoc == null)
            return 0;

        return LastLoc.getLongitude();
    }

    public LatLng getLatLng()
    {
        return new LatLng(getLatitude(), getLongitude());
    }

    public double distanceTo(double Lat, double Long)
    {
        return Math.sqrt(Lat - LastLoc.getLatitude() + Long - LastLoc.getLongitude());
    }
}
