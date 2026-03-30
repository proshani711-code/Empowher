package com.example.majorproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class UserDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private List<Category> categoryList;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("User Dashboard");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        categoryList = new ArrayList<>();
        adapter = new CategoryAdapter(this, categoryList, category -> {
            Intent intent = new Intent(UserDashboardActivity.this, WorkerListActivity.class);
            intent.putExtra("category", category.getCategoryName());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        loadStaticCategories();
        loadCategoriesFromFirestore();
        checkWorkerResponses();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_my_requests) {
            startActivity(new Intent(UserDashboardActivity.this, UserRequestActivity.class));
            return true;
        }
        if (item.getItemId() == R.id.action_logOut) {
            auth.signOut();
            startActivity(new Intent(UserDashboardActivity.this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadStaticCategories() {
        categoryList.add(new Category("Painter", R.drawable.painter));
        categoryList.add(new Category("Electrician", R.drawable.electrician));
        categoryList.add(new Category("Plumber", R.drawable.plumber));
        categoryList.add(new Category("Carpenter", R.drawable.carpenter));
        categoryList.add(new Category("House Cleaner", R.drawable.cleaner));
        adapter.notifyDataSetChanged();
    }

    private void loadCategoriesFromFirestore() {
        db.collection("Services")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                        String serviceName = document.getString("name");
                        Long imageResId = document.getLong("imageResId");

                        if (serviceName != null && imageResId != null) {
                            categoryList.add(new Category(serviceName, imageResId.intValue()));
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load services!", Toast.LENGTH_SHORT).show());
    }

    private void checkWorkerResponses() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("UserRequests")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "Worker Responded")
                .addSnapshotListener((snapshots, error) -> {
                    if (error == null && snapshots != null && !snapshots.isEmpty()) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            sendWorkerResponseNotification(doc.getString("workerResponse"));
                        }
                    }
                });
    }

    private void sendWorkerResponseNotification(String message) {
        NotificationHelper.sendNotification(this, "Worker Response", message);
    }
}
