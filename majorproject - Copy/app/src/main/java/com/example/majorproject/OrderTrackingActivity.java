package com.example.majorproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.util.Objects;

public class OrderTrackingActivity extends AppCompatActivity implements OnMapReadyCallback, PaymentResultListener {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private TextView tvOrderStatus, tvWorkerName, tvPrice, tvPaymentStatus;
    private Button btnProceedToPayment, btnCallWorker, btnChatWorker;
    private ProgressBar progressBar;
    private GoogleMap mMap;
    private FirebaseFirestore db;
    private String orderId, workerId, workerPhone = "", price, userId, workerName, serviceName;
    private boolean isMapReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_tracking);

        Checkout.preload(getApplicationContext());

        initializeViews();

        db = FirebaseFirestore.getInstance();
        orderId = getIntent().getStringExtra("orderId");

        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Error: Order ID is missing!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        loadOrderDetails();

        btnProceedToPayment.setOnClickListener(v -> {
            if (price != null && !price.isEmpty()) {
                startRazorpayPayment(price, orderId);
            } else {
                Toast.makeText(this, "Price information not available", Toast.LENGTH_SHORT).show();
            }
        });

        listenForPaymentCompletion();
        initializeMap();
    }

    private void initializeViews() {
        tvOrderStatus = findViewById(R.id.tvOrderStatus);
        tvWorkerName = findViewById(R.id.tvWorkerName);
        tvPrice = findViewById(R.id.tvPrice);
        tvPaymentStatus = findViewById(R.id.tvPaymentStatus);
        btnProceedToPayment = findViewById(R.id.btnCompleteOrder);
        btnCallWorker = findViewById(R.id.btnCallWorker);
        btnChatWorker = findViewById(R.id.btnChatWorker);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void loadOrderDetails() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("Orders").document(orderId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        workerId = documentSnapshot.getString("workerId");
                        userId = documentSnapshot.getString("userId");
                        price = Objects.requireNonNullElse(documentSnapshot.getString("price"), "0");
                        serviceName = documentSnapshot.getString("serviceName");

                        tvOrderStatus.setText("Status: " + documentSnapshot.getString("status"));
                        tvPaymentStatus.setText("Payment: " + documentSnapshot.getString("paymentStatus"));
                        tvPrice.setText("Price: ₹" + price);

                        fetchWorkerDetails(workerId);
                        checkAndRequestLocationPermission();
                    } else {
                        Toast.makeText(this, "Order not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("OrderTracking", "Error loading order details", e);
                    Toast.makeText(this, "Failed to load order details", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchWorkerDetails(String workerId) {
        db.collection("Users").document(workerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        workerName = documentSnapshot.getString("name");
                        workerPhone = Objects.requireNonNullElse(documentSnapshot.getString("contact"), "N/A");

                        tvWorkerName.setText("Worker: " + (workerName != null ? workerName : "Unknown"));
                        btnCallWorker.setOnClickListener(v -> makeCall(workerPhone));
                        btnChatWorker.setOnClickListener(v -> openWhatsAppChat(workerPhone));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("OrderTracking", "Error fetching worker details", e);
                    Toast.makeText(this, "Failed to load worker details", Toast.LENGTH_SHORT).show();
                });
    }

    private void checkAndRequestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startRazorpayPayment(String amount, String orderId) {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_WXWJeQ8botny4I");

        try {
            JSONObject options = new JSONObject();
            options.put("name", getString(R.string.app_name));
            options.put("description", "Payment for " + serviceName);
            options.put("currency", "INR");
            options.put("amount", Integer.parseInt(amount) * 100);
            JSONObject prefill = new JSONObject();
            prefill.put("email", "testuser@example.com");
            prefill.put("contact", "9876543210");

            JSONObject upi = new JSONObject();
            upi.put("flow", "collect");
            upi.put("vpa", "placewell@ibl");

            options.put("prefill", prefill);
            options.put("upi", upi);
            options.put("theme.color", "#3399cc");

            checkout.open(this, options);
        } catch (Exception e) {
            Toast.makeText(this, "Payment error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("Razorpay", "Payment Error", e);
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        Toast.makeText(this, "Payment Successful: " + razorpayPaymentID, Toast.LENGTH_SHORT).show();
        updatePaymentStatus("Completed");
    }

    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(this, "Payment Failed: " + response, Toast.LENGTH_SHORT).show();
        updatePaymentStatus("Failed");
    }

    private void updatePaymentStatus(String status) {
        db.collection("Orders").document(orderId)
                .update("paymentStatus", status)
                .addOnSuccessListener(aVoid -> {
                    if (status.equals("Completed")) {
                        removeOrderAndOpenFeedback();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update payment status", Toast.LENGTH_SHORT).show();
                });
    }

    private void listenForPaymentCompletion() {
        db.collection("Orders").document(orderId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e("OrderTracking", "Error listening to payment status", e);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        String paymentStatus = snapshot.getString("paymentStatus");
                        if ("Completed".equals(paymentStatus)) {
                            removeOrderAndOpenFeedback();
                        }
                    }
                });
    }

    private void removeOrderAndOpenFeedback() {
        db.collection("Orders").document(orderId)
                .delete()
                .addOnSuccessListener(aVoid -> redirectToFeedbackForm())
                .addOnFailureListener(e -> {
                    Log.e("OrderTracking", "Error deleting order", e);
                    redirectToFeedbackForm();
                });
    }

    private void redirectToFeedbackForm() {
        Intent intent = new Intent(OrderTrackingActivity.this, FeedbackFormActivity.class);
        intent.putExtra("orderId", orderId);
        intent.putExtra("workerId", workerId);
        intent.putExtra("workerName", workerName);
        intent.putExtra("serviceName", serviceName);
        startActivity(intent);
        finish();
    }

    private void makeCall(String phoneNumber) {
        if (!phoneNumber.equals("N/A")) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
            startActivity(intent);
        } else {
            Toast.makeText(this, "Phone number not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openWhatsAppChat(String phoneNumber) {
        if (!phoneNumber.equals("N/A")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://wa.me/" + phoneNumber + "?text=Hello!"));
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
        mMap.getUiSettings().setCompassEnabled(true);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        listenForWorkerLocationUpdates();
    }

    private void listenForWorkerLocationUpdates() {
        db.collection("Orders").document(orderId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        Log.e("OrderTracking", "Error listening to worker location", e);
                        Toast.makeText(this, "Error tracking worker location", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshot != null && snapshot.exists()) {
                        Double workerLat = snapshot.getDouble("workerLat");
                        Double workerLng = snapshot.getDouble("workerLng");

                        if (workerLat != null && workerLng != null) {
                            updateMapWithWorkerLocation(workerLat, workerLng);
                        } else {
                            Log.d("OrderTracking", "Worker location not available yet");
                            LatLng defaultLocation = new LatLng(28.6139, 77.2090);
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
                            Toast.makeText(this, "Waiting for worker location updates...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateMapWithWorkerLocation(double workerLat, double workerLng) {
        if (isMapReady && mMap != null) {
            mMap.clear();
            LatLng workerLocation = new LatLng(workerLat, workerLng);

            mMap.addMarker(new MarkerOptions()
                    .position(workerLocation)
                    .title("Worker Location")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(workerLocation, 15));
        }
    }
}