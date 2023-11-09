package com.example.lab6;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class MyLocationListener implements LocationListener {

    private Context mContext;

    public MyLocationListener(Context context) {
        mContext = context;
    }

    @Override
    public void onLocationChanged(Location location) {
        double longitude = location.getLongitude();
        double latitude = location.getLatitude();

        // Broadcast this data
        Intent intent = new Intent("location-update");
        intent.putExtra("longitude", longitude);
        intent.putExtra("latitude", latitude);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    // Implement other methods of LocationListener if needed
}
