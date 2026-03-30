package com.example.majorproject;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NegotiationActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore firestore;
    private ProgressDialog progressDialog;
    Boolean isLocationFetched;
    private TextView tvWorkerName, tvWorkerCity;
    private EditText etProblemDescription, etAddress;
    private CheckBox cbIncludeMaterials;
    private Button btnSubmitNegotiation;
    private ImageView ivWorkerProfile;

    private String negotiationId, orderId, workerId, userId, workerName, workerCity, workerImageUrl, service;
    private double userLat = 0.0, userLng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_negotiation);

        firestore = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        tvWorkerName = findViewById(R.id.tvWorkerName);
        tvWorkerCity = findViewById(R.id.tvWorkerCity);
        ivWorkerProfile = findViewById(R.id.ivWorkerProfile);
        etProblemDescription = findViewById(R.id.etProblemDescription);
        etAddress = findViewById(R.id.etAddress);
        cbIncludeMaterials = findViewById(R.id.cbIncludeMaterials);
        btnSubmitNegotiation = findViewById(R.id.btnSubmitNegotiation);

        workerId = getIntent().getStringExtra("workerId");
        userId = getIntent().getStringExtra("userId");
        workerName = getIntent().getStringExtra("workerName");
        workerCity = getIntent().getStringExtra("workerCity");
        workerImageUrl = getIntent().getStringExtra("workerImage");
        service = getIntent().getStringExtra("service");

        negotiationId = firestore.collection("Negotiations").document().getId();
        orderId = firestore.collection("Orders").document().getId();
        if (workerName == null || workerName.isEmpty()) {
            workerName = "Unknown Worker";
        }
        tvWorkerName.setText(workerName);
        tvWorkerCity.setText("City: " + workerCity);
        if (workerImageUrl != null && !workerImageUrl.isEmpty()) {
            Glide.with(this).load(workerImageUrl).into(ivWorkerProfile);
        } else {
            ivWorkerProfile.setImageResource(R.drawable.ic_placeholder);
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Submitting...");

        requestLocationPermission();
        fetchUserLocation();
        btnSubmitNegotiation.setOnClickListener(v -> submitNegotiation());
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchUserLocation();
        }
    }

    private void fetchUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        userLat = location.getLatitude();
                        userLng = location.getLongitude();
                        convertLocationToAddress(location);
                        isLocationFetched = true;
                    } else {
                        Toast.makeText(this, "Unable to fetch location. Try again!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

    }
    private void convertLocationToAddress(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                etAddress.setText(addresses.get(0).getAddressLine(0));
            } else {
                etAddress.setText("Address not found");
            }
        } catch (IOException e) {
            etAddress.setText("Error fetching address");
        }
    }

    private void submitNegotiation() {
        String problemDescription = etProblemDescription.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        boolean includeMaterials = cbIncludeMaterials.isChecked();

        if (TextUtils.isEmpty(problemDescription) || TextUtils.isEmpty(address)) {
            Toast.makeText(this, "Please enter all details!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isLocationFetched) {
            Toast.makeText(this, "Fetching location... Please wait!", Toast.LENGTH_SHORT).show();
            return;
        }


        progressDialog.show();
        Map<String, Object> negotiationData = new HashMap<>();
        negotiationData.put("workerId", workerId);
        negotiationData.put("workerName", workerName);
        negotiationData.put("userId", userId);
        negotiationData.put("negotiationId", negotiationId);
        negotiationData.put("orderId", orderId);
        negotiationData.put("problemDescription", problemDescription);
        negotiationData.put("address", address);
        negotiationData.put("includeMaterials", includeMaterials);
        negotiationData.put("userLat", userLat);
        negotiationData.put("userLng", userLng);
        negotiationData.put("service", service);
        negotiationData.put("status", "Pending");
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("orderId", orderId);
        orderData.put("workerId", workerId);
        orderData.put("workerName", workerName);
        orderData.put("service", service);
        orderData.put("userId", userId);
        orderData.put("userLat", userLat);
        orderData.put("userLng", userLng);
        orderData.put("status", "Pending");
        orderData.put("price", "N/A");
        orderData.put("workerPhone", "");
        orderData.put("userPhone", "");
        orderData.put("paymentStatus", "Unpaid");
        orderData.put("paymentMethod", "None");
        firestore.collection("Negotiations").document(negotiationId)
                .set(negotiationData)
                .addOnSuccessListener(aVoid -> firestore.collection("Orders").document(orderId)
                        .set(orderData)
                        .addOnSuccessListener(orderVoid -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Negotiation Submitted!", Toast.LENGTH_SHORT).show();
                            notifyWorker(workerId, problemDescription, userId);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            Toast.makeText(this, "Failed to create order", Toast.LENGTH_SHORT).show();
                        }))
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Failed to submit negotiation", Toast.LENGTH_SHORT).show();
                });
    }

    private void notifyWorker(String workerId, String problemDescription, String userId) {
        firestore.collection("workers").document(workerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("fcmToken")) {
                        String workerToken = documentSnapshot.getString("fcmToken");
                        if (workerToken != null) {
                            sendFCMNotification(workerToken, "New Negotiation Request", problemDescription, userId);
                        }
                    }
                });
    }

    private void sendFCMNotification(String workerToken, String title, String message, String userId) {
        String FCM_API_URL = "https://fcm.googleapis.com/fcm/send";
        try {
            JSONObject notification = new JSONObject();
            JSONObject notificationBody = new JSONObject();
            notificationBody.put("title", title);
            notificationBody.put("message", message);
            notificationBody.put("userId", userId);
            notification.put("to", workerToken);
            notification.put("data", notificationBody);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, FCM_API_URL, notification,
                    response -> Log.d("FCM_SUCCESS", "Notification sent: " + response.toString()),
                    error -> Log.e("FCM_ERROR", "Error sending FCM: " + error.getMessage())
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Authorization", "key=uHWifAAP6G12JWwNCJMkwPeQP_8IHGxEFkhWTVRMTRQ");
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            Volley.newRequestQueue(this).add(jsonObjectRequest);

        } catch (JSONException e) {
            Log.e("FCM_JSON_ERROR", "JSON Exception: " + e.getMessage());
        }
    }

}
