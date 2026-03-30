package com.example.majorproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class WorkerBudgetDecisionActivity extends AppCompatActivity {

    private EditText etProposedBudget, etWorkerComments;
    private Button btnSubmitBudget, btnCancel;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private TextView tvUserName, tvProblemDescription, tvAddress, tvIncludeMaterials;
    private String negotiationId, userId, workerId, serviceName, description, workerName, workerPhone;
    private boolean includesMaterials;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_budget_decision);

        // Initialize views
        tvUserName = findViewById(R.id.tvUserName);
        tvProblemDescription = findViewById(R.id.tvProblemDescription);
        tvAddress = findViewById(R.id.tvAddress);
        tvIncludeMaterials = findViewById(R.id.tvIncludeMaterials);
        etProposedBudget = findViewById(R.id.etProposedBudget);
        etWorkerComments = findViewById(R.id.etWorkerComments);
        btnSubmitBudget = findViewById(R.id.btnSubmitBudget);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);
        db = FirebaseFirestore.getInstance();


        Intent intent = getIntent();
        negotiationId = intent.getStringExtra("negotiationId");
        workerId = intent.getStringExtra("workerId");
        serviceName = intent.getStringExtra("serviceName");
        description = intent.getStringExtra("description");
        userId = intent.getStringExtra("userId");
        includesMaterials = intent.getBooleanExtra("includesMaterials", false);
        String userName = intent.getStringExtra("userName");
        String problemDescription = intent.getStringExtra("problemDescription");
        String address = intent.getStringExtra("address");


        tvProblemDescription.setText("Problem Description: " + (problemDescription != null ? problemDescription : "N/A"));
        tvAddress.setText("Address: " + (address != null ? address : "Not Available"));
        tvIncludeMaterials.setVisibility(includesMaterials ? View.VISIBLE : View.GONE);

        // Load user name if not provided in the intent
        if (userName != null && !userName.isEmpty()) {
            tvUserName.setText("User: " + userName);
        } else {
            loadUserName(userId);
        }


        loadWorkerDetails();


        btnSubmitBudget.setOnClickListener(v -> submitBudget());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void submitBudget() {
        String budgetStr = etProposedBudget.getText().toString().trim();
        String workerComments = etWorkerComments.getText().toString().trim();


        if (TextUtils.isEmpty(budgetStr)) {
            Toast.makeText(this, "Enter proposed budget", Toast.LENGTH_SHORT).show();
            return;
        }


        progressBar.setVisibility(View.VISIBLE);


        Map<String, Object> updateData = new HashMap<>();
        updateData.put("status", "Worker Proposed");
        updateData.put("proposedBudget", budgetStr);
        updateData.put("workerComments", workerComments);
        updateData.put("workerName", workerName);
        updateData.put("workerPhone", workerPhone);


        DocumentReference negotiationRef = db.collection("Negotiations").document(negotiationId);
        negotiationRef.update(updateData)
                .addOnSuccessListener(aVoid -> {

                    sendNotificationToUser();
                    Toast.makeText(this, "Budget Submitted Successfully!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to submit budget!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    Log.e("WorkerBudgetDecision", "Error submitting budget", e);
                });
    }

    private void sendNotificationToUser() {

        Map<String, Object> notificationData = new HashMap<>();
        notificationData.put("userId", userId);
        notificationData.put("workerId", workerId);
        notificationData.put("workerName", workerName);
        notificationData.put("workerPhone", workerPhone);
        notificationData.put("serviceName", serviceName);
        notificationData.put("description", description);
        notificationData.put("proposedBudget", etProposedBudget.getText().toString());
        notificationData.put("workerComments", etWorkerComments.getText().toString());
        notificationData.put("negotiationId", negotiationId);
        notificationData.put("message", "A worker has proposed a budget for your request.");
        notificationData.put("timestamp", System.currentTimeMillis());


        db.collection("Notifications").add(notificationData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("WorkerBudgetDecision", "Notification sent to user");
                })
                .addOnFailureListener(e -> {
                    Log.e("WorkerBudgetDecision", "Failed to send notification", e);
                });
    }

    private void loadWorkerDetails() {

        db.collection("Users").document(workerId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        workerName = documentSnapshot.getString("name");
                        workerPhone = documentSnapshot.getString("phone");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("WorkerBudgetDecision", "Error fetching worker details", e);
                });
    }

    private void loadUserName(String userId) {

        db.collection("Users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.getString("name") != null) {
                        tvUserName.setText("User: " + documentSnapshot.getString("name"));
                    } else {
                        tvUserName.setText("User: Not Found");
                    }
                })
                .addOnFailureListener(e -> {
                    tvUserName.setText("User: Error Loading");
                    Log.e("WorkerBudgetDecision", "Error fetching user name", e);
                });
    }
}