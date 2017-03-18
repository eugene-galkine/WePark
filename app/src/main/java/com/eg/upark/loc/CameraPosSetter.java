package com.eg.upark.loc;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by Eugene Galkine on 10/11/2016.
 */

public class CameraPosSetter
{
    private LatLngBounds.Builder builder;

    public CameraPosSetter (LatLng pos)
    {
        builder = new LatLngBounds.Builder();
        builder.include(pos);
    }

    public CameraUpdate AddLatLng (LatLng pos)
    {
        builder.include(pos);
        LatLngBounds bounds = builder.build();

        return CameraUpdateFactory.newLatLngBounds(bounds, 300);
        //TODO: calulate padding
    }
}
