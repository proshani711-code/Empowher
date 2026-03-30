package com.example.majorproject;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class UserNegotiationActivity extends AppCompatActivity {

    private TextView tvService, tvDescription, tvWorkerName, tvWorkerPrice, tvWorkerComments, tvStatus;
    private Button btnAccept, btnReject;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String negotiationId, workerId, userId, workerName, price, serviceName, description;
    private String orderId;
    private boolean isAccepted = false;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_negotiation);

        db = FirebaseFirestore.getInstance();
        negotiationId = getIntent().getStringExtra("negotiationId");
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        initializeUI();
        loadNegotiationDetails();
    }

    private void initializeUI() {
        tvService = findViewById(R.id.tvService);
        tvDescription = findViewById(R.id.tvDescription);
        tvWorkerName = findViewById(R.id.tvWorkerName);
        tvWorkerPrice = findViewById(R.id.tvWorkerPrice);
        tvWorkerComments = findViewById(R.id.tvWorkerComments);
        tvStatus = findViewById(R.id.tvStatus);
        btnAccept = findViewById(R.id.btnAccept);
        btnReject = findViewById(R.id.btnReject);
        progressBar = findViewById(R.id.progressBar);

        btnAccept.setOnClickListener(v -> {
            if (!isAccepted) {
                isAccepted = true;
                acceptWorkerPrice();
            }
        });

        btnReject.setOnClickListener(v -> rejectNegotiation());
    }

    private void loadNegotiationDetails() {
        if (negotiationId == null || negotiationId.isEmpty()) {
            Toast.makeText(this, "Error: Negotiation ID is missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        DocumentReference negotiationRef = db.collection("Negotiations").document(negotiationId);

        negotiationRef.get().addOnSuccessListener(documentSnapshot -> {
            progressBar.setVisibility(View.GONE);
            if (documentSnapshot.exists()) {
                serviceName = documentSnapshot.getString("serviceName");
                description = documentSnapshot.getString("description");
                workerName = documentSnapshot.getString("workerName");
                price = documentSnapshot.getString("proposedBudget");

                tvService.setText(serviceName);
                tvDescription.setText(description);
                tvWorkerName.setText("Worker: " + workerName);
                tvWorkerPrice.setText("Proposed Price: ₹" + price);
                tvWorkerComments.setText("Worker Comments: " + documentSnapshot.getString("workerComments"));

                userId = documentSnapshot.getString("userId");
                workerId = documentSnapshot.getString("workerId");

                if (userId == null || workerId == null) {
                    Toast.makeText(this, "Invalid data: User or Worker ID missing!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }).addOnFailureListener(e -> {
            progressBar.setVisibility(View.GONE);
            Toast.makeText(this, "Failed to load details!", Toast.LENGTH_SHORT).show();
            Log.e("UserNegotiationActivity", "Error fetching details: " + e.getMessage());
        });
    }

    private void acceptWorkerPrice() {
        if (userId == null || workerId == null) {
            Toast.makeText(this, "User or Worker ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        orderId = db.collection("Orders").document().getId();

        fetchUserLocation((latitude, longitude) -> {
            Map<String, Object> orderData = new HashMap<>();
            orderData.put("orderId", orderId);
            orderData.put("userId", userId);
            orderData.put("workerId", workerId);
            orderData.put("workerName", workerName);
            orderData.put("price", price);
            orderData.put("serviceName", serviceName);
            orderData.put("description", description);
            orderData.put("status", "Accepted");
            orderData.put("userLat", latitude);
            orderData.put("userLng", longitude);

            db.collection("Orders").document(orderId)
                    .set(orderData)
                    .addOnSuccessListener(aVoid -> {
                        updateNegotiationStatus("Accepted");
                        sendNotificationToWorker();

                        Intent intent = new Intent(UserNegotiationActivity.this, AdminOrderTrackingActivity.class);
                        intent.putExtra("orderId", orderId);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        isAccepted = false;
                        Toast.makeText(this, "Failed to accept price!", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void fetchUserLocation(LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            callback.onLocationFetched(location.getLatitude(), location.getLongitude());
                        } else {
                            Toast.makeText(this, "Unable to fetch location. Try again!", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        } else {
            Toast.makeText(this, "Location permission not granted!", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNegotiationStatus(String status) {
        if (negotiationId == null) {
            Toast.makeText(this, "Negotiation ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Negotiations").document(negotiationId)
                .update("status", status)
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    tvStatus.setText("Status: " + status);
                    btnAccept.setEnabled(false);
                    btnReject.setEnabled(false);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("UserNegotiationActivity", "Failed to update negotiation status: " + e.getMessage());
                    Toast.makeText(this, "Failed to update status!", Toast.LENGTH_SHORT).show();
                });
    }

    private void rejectNegotiation() {
        if (negotiationId == null) {
            Toast.makeText(this, "Negotiation ID is missing!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        db.collection("Negotiations").document(negotiationId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    notifyWorker();
                    Toast.makeText(this, "Deal rejected successfully!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("UserNegotiationActivity", "Failed to reject deal: " + e.getMessage());
                    Toast.makeText(this, "Failed to reject deal!", Toast.LENGTH_SHORT).show();
                });
    }

    private void sendNotificationToWorker() {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("workerId", workerId);
        notificationData.put("orderId", orderId);
        notificationData.put("message", "Your negotiation has been accepted. Check current orders.");
        notificationData.put("timestamp", System.currentTimeMillis());
        db.collection("Notifications").add(notificationData);
    }

    private void notifyWorker() {
        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("workerId", workerId);
        notificationData.put("message", "The user has rejected your offer.");
        notificationData.put("timestamp", System.currentTimeMillis());

        db.collection("Notifications").add(notificationData);
    }

    interface LocationCallback {
        void onLocationFetched(double latitude, double longitude);
    }
}