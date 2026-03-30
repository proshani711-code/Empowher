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

public class AdminOrderTrackingActivity extends AppCompatActivity
{
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private TextView tvOrderStatus, tvWorkerName, tvPrice, tvPaymentStatus;
    private Button btnProceedToPayment, btnCallWorker, btnChatWorker;
    private ProgressBar progressBar;
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
                .addOnSuccessListener(a -> redirectToFeedbackForm())
                .addOnFailureListener(e -> {
                    Log.e("OrderTracking", "Error deleting order", e);
                    redirectToFeedbackForm();
                });
    }

    private void redirectToFeedbackForm() {
        Intent intent = new Intent(AdminOrderTrackingActivity.this, FeedbackFormActivity.class);
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


}