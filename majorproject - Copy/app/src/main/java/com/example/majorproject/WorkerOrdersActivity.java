package com.example.majorproject;

import android.content.Intent;
import android.os.Bundle;
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

public class WorkerOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WorkerOrderAdapter adapter;
    private List<Order> orderList;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String workerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_worker_orders);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        workerId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        orderList = new ArrayList<>();

        adapter = new WorkerOrderAdapter(this, orderList, order -> {
            Intent intent = new Intent(WorkerOrdersActivity.this, WorkerOrderTrackingActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        loadWorkerOrders();
    }

    private void loadWorkerOrders() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("Orders")
                .whereEqualTo("workerId", workerId)
                .whereEqualTo("status", "Accepted")
                .get()
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        orderList.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            Order order = doc.toObject(Order.class);
                            order.setOrderId(doc.getId());
                            orderList.add(order);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load orders!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
