package com.example.majorproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class ManageOrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerViewOrders;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private ProgressBar progressBarOrders;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_orders);

        recyclerViewOrders = findViewById(R.id.recyclerViewOrders);
        progressBarOrders = findViewById(R.id.progressBarOrders);
        recyclerViewOrders.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(this, orderList, order -> {
            Toast.makeText(this, "Order Selected: " + order.getOrderId(), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, AdminOrderTrackingActivity.class);
            intent.putExtra("orderId", order.getOrderId());
            startActivity(intent);
        });
        recyclerViewOrders.setAdapter(orderAdapter);

        loadOrders();
    }

    private void loadOrders() {
        progressBarOrders.setVisibility(View.VISIBLE);
        db.collection("Orders").get().addOnCompleteListener(task -> {
            progressBarOrders.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() != null) {
                orderList.clear();
                for (DocumentSnapshot doc : task.getResult()) {
                    Order order = doc.toObject(Order.class);
                    if (order != null) {
                        order.setOrderId(doc.getId());
                        orderList.add(order);
                    }
                }
                orderAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Failed to load orders", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
