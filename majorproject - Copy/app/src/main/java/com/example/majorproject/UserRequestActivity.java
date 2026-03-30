package com.example.majorproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class UserRequestActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RequestAdapter adapter;
    private List<UserRequest> requestList;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_request);
        initializeUI();
        loadRequests();
    }

    private void initializeUI() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        requestList = new ArrayList<>();

        adapter = new RequestAdapter(this, requestList, request -> {
            Log.d("UserRequestActivity", "Worker Name: " + request.getWorkerName());
            Intent intent = new Intent(UserRequestActivity.this, UserNegotiationActivity.class);
            populateIntentWithRequestData(intent, request);
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }

    private void populateIntentWithRequestData(Intent intent, UserRequest request) {
        intent.putExtra("negotiationId", request.getNegotiationId());
        intent.putExtra("userId", request.getUserId());
        intent.putExtra("workerId", request.getWorkerId());
        intent.putExtra("workerName", request.getWorkerName());
        intent.putExtra("workerPhone", request.getWorkerPhone());
        intent.putExtra("userPhone", request.getUserPhone());
        intent.putExtra("proposedBudget", request.getProposedBudget());
        intent.putExtra("workerComments", request.getWorkerComments());
        intent.putExtra("serviceName", request.getServiceName());
        intent.putExtra("description", request.getDescription());
        intent.putExtra("status", request.getStatus());
    }

    private void loadRequests() {
        progressBar.setVisibility(View.VISIBLE);
        String userId = auth.getCurrentUser().getUid();

        db.collection("Negotiations")
                .whereEqualTo("userId", userId)
                .whereIn("status", List.of("Accepted", "Pending", "Worker Proposed"))
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        requestList.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            UserRequest userRequest = doc.toObject(UserRequest.class);
                            if (userRequest != null) {
                                userRequest.setNegotiationId(doc.getId());
                                userRequest.setDescription(doc.getString("problemDescription"));

                                if (userRequest.getWorkerName() == null || userRequest.getWorkerName().isEmpty() ||
                                        userRequest.getWorkerPhone() == null || userRequest.getWorkerPhone().isEmpty() ||
                                        userRequest.getServiceName() == null) {
                                    fetchWorkerDetails(userRequest);
                                } else {
                                    requestList.add(userRequest);
                                    adapter.notifyDataSetChanged();
                                }
                            }
                        }
                    } else {
                        Log.e("UserRequestActivity", "Failed to load requests", task.getException());
                        Toast.makeText(this, "Failed to load requests", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchWorkerDetails(UserRequest request) {
        db.collection("Users").document(request.getWorkerId())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        request.setWorkerName(documentSnapshot.getString("name"));
                        request.setWorkerPhone(documentSnapshot.getString("phone"));
                        request.setServiceName(documentSnapshot.getString("category"));
                    }
                    requestList.add(request);
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRequestActivity", "Failed to fetch worker details", e);
                    Toast.makeText(this, "Failed to fetch worker details", Toast.LENGTH_SHORT).show();
                });
    }
    private void markRequestAsCompleted(String negotiationId) {
        db.collection("Negotiations").document(negotiationId)
                .update("status", "Completed")
                .addOnSuccessListener(aVoid -> {
                    removeRequestFromList(negotiationId);
                    Toast.makeText(UserRequestActivity.this, "Order marked as completed", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e("UserRequestActivity", "Failed to mark request as completed", e);
                    Toast.makeText(UserRequestActivity.this, "Failed to mark order as completed", Toast.LENGTH_SHORT).show();
                });
    }
    private void removeRequestFromList(String negotiationId) {
        for (int i = 0; i < requestList.size(); i++) {
            if (requestList.get(i).getNegotiationId().equals(negotiationId)) {
                requestList.remove(i);
                adapter.notifyItemRemoved(i);
                break;
            }
        }
    }
}