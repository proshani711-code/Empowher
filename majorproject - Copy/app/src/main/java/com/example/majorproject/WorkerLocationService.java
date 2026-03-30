package com.example.majorproject;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class WorkerLocationService extends Service {

    private static final String TAG = "WorkerLocationService";
    private static final String CHANNEL_ID = "LocationUpdatesChannel";

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private String workerId;
    private LocationCallback locationCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();
        workerId = FirebaseAuth.getInstance().getUid();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1, getNotification());
        if (hasLocationPermission()) {
            startLocationUpdates();
        } else {
            Log.e(TAG, "Location permissions not granted.");
        }
        return START_STICKY;
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result != null && result.getLastLocation() != null) {
                    Location location = result.getLastLocation();
                    updateWorkerLocation(location.getLatitude(), location.getLongitude());
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void updateWorkerLocation(double latitude, double longitude) {
        db.collection("Orders")
                .whereEqualTo("workerId", workerId)
                .whereEqualTo("status", "Accepted")
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        for (QueryDocumentSnapshot doc : query) {
                            String orderId = doc.getId();
                            db.collection("Orders").document(orderId)
                                    .update("workerLat", latitude, "workerLng", longitude)
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Updated location for order: " + orderId))
                                    .addOnFailureListener(e -> Log.e(TAG, "Update failed for order: " + orderId, e));
                        }
                    } else {
                        Log.d(TAG, "No active accepted orders for worker.");
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Failed to fetch active orders", e));
    }

    private Notification getNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Tracking Your Location")
                .setContentText("Your location is being updated.")
                .setSmallIcon(R.drawable.ic_location)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Location Updates";
            String description = "Channel for worker location tracking";
            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        Log.d(TAG, "Location updates stopped.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
