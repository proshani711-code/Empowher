package com.example.majorproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

public class WorkerOrderTrackingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private TextView tvOrderStatus, tvUserName, tvUserPhone, tvPaymentStatus, tvPrice;
    private Button btnCompleteOrder, btnCallUser, btnChatUser, btnNavigate;
    private ProgressBar progressBar;
    private GoogleMap mMap;
    private FirebaseFirestore db;
    private String workerId, userId, userPhone = "N/A", orderId;
    private boolean isMapReady = false;
    private double userLat = 0.0, userLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_order_tracking);

        initializeViews();
        db = FirebaseFirestore.getInstance();
        workerId = FirebaseAuth.getInstance().getUid();

        if (workerId == null || workerId.isEmpty()) {
            Toast.makeText(this, "Worker ID is missing!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadOrderDetails();
        setupButtonListeners();
        initializeMap();
    }

    private void initializeViews() {
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        tvPrice = findViewById(R.id.tvPrice);
        btnCompleteOrder = findViewById(R.id.btnCompleteOrder);
        btnCallUser = findViewById(R.id.btnCallUser);
        btnChatUser = findViewById(R.id.btnChatUser);
        btnNavigate = findViewById(R.id.btnNavigate);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupButtonListeners() {
        btnCompleteOrder.setOnClickListener(v -> confirmOrderCompletion());
        btnNavigate.setOnClickListener(v -> launchNavigation());
        btnCallUser.setOnClickListener(v -> makeCall(userPhone));
        btnChatUser.setOnClickListener(v -> openWhatsAppChat(userPhone));
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    private void loadOrderDetails() {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("Orders")
                .whereEqualTo("workerId", workerId)
                .whereEqualTo("status", "Accepted")
                .get()
                .addOnSuccessListener(snapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (!snapshot.isEmpty()) {
                        DocumentSnapshot doc = snapshot.getDocuments().get(0);
                        orderId = doc.getId();
                        userId = doc.getString("userId");
                        updateUI(doc);
                        fetchUserDetails();
                        fetchUserLocation();
                    } else {
                        Toast.makeText(this, "No active orders found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("WorkerOrderTracking", "Error fetching orders", e);
                });
    }

    private void updateUI(DocumentSnapshot doc) {
        tvOrderStatus.setText("Status: " + doc.getString("status"));
        tvPaymentStatus.setText("Payment: " + doc.getString("paymentStatus"));
        tvPrice.setText("Price: ₹" + doc.getString("price"));
    }

    private void fetchUserDetails() {
        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        userPhone = doc.getString("mobile");
                        tvUserName.setText("User: " + (name != null ? name : "Unknown"));
                        tvUserPhone.setText("Phone: " + (userPhone != null ? userPhone : "N/A"));
                    }
                })
                .addOnFailureListener(e -> Log.e("WorkerOrderTracking", "Error fetching user details", e));
    }

    private void fetchUserLocation() {
        db.collection("Negotiations").document(orderId)
                .addSnapshotListener((snap, e) -> {
                    if (e == null && snap != null && snap.exists()) {
                        Double lat = snap.getDouble("userLat"), lng = snap.getDouble("userLng");
                        if (lat != null && lng != null) {
                            userLat = lat; userLng = lng;
                            updateMapWithUserLocation();
                            Toast.makeText(this, "User location updated on map", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Failed to fetch user location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateMapWithUserLocation() {
        if (isMapReady && mMap != null && userLat != 0.0 && userLng != 0.0) {
            mMap.clear();
            LatLng userLocation = new LatLng(userLat, userLng);
            mMap.addMarker(new MarkerOptions()
                    .position(userLocation)
                    .title("User Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
        }
    }

    private void launchNavigation() {
        if (userLat != 0.0 && userLng != 0.0) {
            try {
                Uri uri = Uri.parse("google.navigation:q=" + userLat + "," + userLng + "&mode=d");
                startActivity(new Intent(Intent.ACTION_VIEW, uri).setPackage("com.google.android.apps.maps"));
            } catch (Exception e) {
                Uri fallback = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + userLat + "," + userLng);
                startActivity(new Intent(Intent.ACTION_VIEW, fallback));
            }
        } else {
            Toast.makeText(this, "User location not available yet", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmOrderCompletion() {
        db.collection("Orders").document(orderId)
                .update("status", "Completed", "paymentStatus", "Completed")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Order marked as completed", Toast.LENGTH_SHORT).show();
                    removeOrderFromFirestore();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to complete order", Toast.LENGTH_SHORT).show());
    }

    private void removeOrderFromFirestore() {
        db.collection("Orders").document(orderId)
                .delete()
                .addOnSuccessListener(aVoid -> finish())
                .addOnFailureListener(e -> Log.e("WorkerOrderTracking", "Error deleting order", e));
    }

    private void makeCall(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.equals("N/A")) {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber)));
        } else {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openWhatsAppChat(String phoneNumber) {
        if (phoneNumber != null && !phoneNumber.equals("N/A")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://wa.me/" + phoneNumber + "?text=Hello! Regarding our service order"));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        isMapReady = true;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        if (userLat != 0.0 && userLng != 0.0) updateMapWithUserLocation();
    }
}
