package com.example.majorproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class WorkerListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WorkerAdapter adapter;
    private List<Worker> workerList;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String selectedCategory;
    private TextView noWorkersText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_list);
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        noWorkersText = findViewById(R.id.noWorkersText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();
        workerList = new ArrayList<>();

        selectedCategory = getIntent().getStringExtra("category");
        if (selectedCategory == null || selectedCategory.isEmpty()) {
            Toast.makeText(this, "Invalid category", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d("WorkerListActivity", "Selected Category: " + selectedCategory);
        adapter = new WorkerAdapter(this, workerList, worker -> {
            Intent intent = new Intent(WorkerListActivity.this, NegotiationActivity.class);
            intent.putExtra("workerId", worker.getWorkerId());
            intent.putExtra("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
            intent.putExtra("workerName", worker.getName());
            intent.putExtra("workerCity", worker.getCity());
            intent.putExtra("workerImage", worker.getProfileUrl());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        loadWorkers();
    }

    private void loadWorkers() {
        progressBar.setVisibility(View.VISIBLE);
        noWorkersText.setVisibility(View.GONE);

        db.collection("Users")
                .whereEqualTo("category", selectedCategory)
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        workerList.clear();
                        QuerySnapshot snapshot = task.getResult();

                        if (snapshot.isEmpty()) {
                            noWorkersText.setVisibility(View.VISIBLE);
                        } else {
                            for (DocumentSnapshot doc : snapshot) {
                                Worker worker = doc.toObject(Worker.class);
                                if (worker != null) {
                                    worker.setWorkerId(doc.getId());
                                    workerList.add(worker);
                                    Log.d("WorkerListActivity", "Worker: " + worker.getName());
                                }
                            }
                            adapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(this, "Failed to load workers", Toast.LENGTH_SHORT).show();
                        Log.e("WorkerListActivity", "Firestore Error: ", task.getException());
                    }
                });
    }
}
