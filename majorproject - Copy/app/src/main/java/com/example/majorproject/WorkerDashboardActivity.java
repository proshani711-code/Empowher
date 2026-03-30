package com.example.majorproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class WorkerDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NegotiationAdapter adapter;
    private List<Negotiation> negotiationList;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String workerId, workerName, workerPhone;
    private ListenerRegistration listenerRegistration;
    private Switch availabilitySwitch;
    private Button btnStartTracking, btnStopTracking, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_dashboard);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Worker Dashboard");

        recyclerView = findViewById(R.id.ordersRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        availabilitySwitch = findViewById(R.id.availabilitySwitch);
        btnStartTracking = findViewById(R.id.btnStartTracking);
        btnStopTracking = findViewById(R.id.btnStopTracking);
        btnLogout = findViewById(R.id.btnLogout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        negotiationList = new ArrayList<>();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            workerId = user.getUid();
            fetchWorkerDetails();
        } else {
            Toast.makeText(this, "Worker not authenticated!", Toast.LENGTH_SHORT).show();
            finish();
        }

        adapter = new NegotiationAdapter(this, negotiationList, workerId, negotiation -> {
            Intent intent = new Intent(WorkerDashboardActivity.this, WorkerBudgetDecisionActivity.class);
            intent.putExtra("negotiationId", negotiation.getNegotiationId());
            intent.putExtra("userId", negotiation.getUserId());
            intent.putExtra("workerId", workerId);
            intent.putExtra("workerName", workerName);
            intent.putExtra("workerPhone", workerPhone);
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        loadNegotiations();

        availabilitySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> updateAvailabilityStatus(isChecked));
        btnStartTracking.setOnClickListener(v -> startTracking());
        btnStopTracking.setOnClickListener(v -> stopTracking());
        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(WorkerDashboardActivity.this, LoginActivity.class));
            finish();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_worker, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_my_orders) {
            startActivity(new Intent(WorkerDashboardActivity.this, WorkerOrderTrackingActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fetchWorkerDetails() {
        db.collection("Workers").document(workerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        workerName = documentSnapshot.getString("name");
                        workerPhone = documentSnapshot.getString("phone");
                        boolean isAvailable = documentSnapshot.getBoolean("available") != null &&
                                documentSnapshot.getBoolean("available");
                        availabilitySwitch.setChecked(isAvailable);
                    }
                })
                .addOnFailureListener(e -> Log.e("WorkerDashboard", "Failed to fetch worker details", e));
    }
    private void loadNegotiations() {
        progressBar.setVisibility(ProgressBar.VISIBLE);

        listenerRegistration = db.collection("Negotiations")
                .whereEqualTo("workerId", workerId)
                .whereEqualTo("status", "Pending")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    progressBar.setVisibility(ProgressBar.GONE);

                    if (e != null) {
                        Log.e("Firestore", "Error loading negotiations: " + e.getMessage());
                        Toast.makeText(WorkerDashboardActivity.this, "Failed to load orders!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        negotiationList.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            Negotiation negotiation = doc.toObject(Negotiation.class);
                            if (negotiation != null) {
                                negotiation.setNegotiationId(doc.getId());
                                negotiationList.add(negotiation);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        negotiationList.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(WorkerDashboardActivity.this, "No orders found!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateAvailabilityStatus(boolean isAvailable) {
        db.collection("Users").document(workerId)
                .update("available", isAvailable)
                .addOnSuccessListener(aVoid -> {
                    String status = isAvailable ? "Available for Work" : "Not Available";
                    Toast.makeText(this, "Status Updated: " + status, Toast.LENGTH_SHORT).show();
                    if (isAvailable) {
                        startService(new Intent(WorkerDashboardActivity.this, WorkerLocationService.class));
                    } else {
                        stopService(new Intent(WorkerDashboardActivity.this, WorkerLocationService.class));
                    }
                    Toast.makeText(this, "Successfully to update status!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update status!", Toast.LENGTH_SHORT).show());
    }

    private void startTracking() {
        Intent trackingIntent = new Intent(WorkerDashboardActivity.this, WorkerLocationService.class);
        startService(trackingIntent);
        Toast.makeText(this, "Tracking Started!", Toast.LENGTH_SHORT).show();
    }

    private void stopTracking() {
        Intent trackingIntent = new Intent(WorkerDashboardActivity.this, WorkerLocationService.class);
        stopService(trackingIntent);
        Toast.makeText(this, "Tracking Stopped!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }
}