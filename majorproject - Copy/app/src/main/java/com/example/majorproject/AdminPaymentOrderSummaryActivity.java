package com.example.majorproject;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminPaymentOrderSummaryActivity extends AppCompatActivity {

    private TextView tvTotalOrdersValue, tvTotalRevenueValue,
            tvMonthlyOrdersValue, tvMonthlyRevenueValue,
            tvCompletedOrdersValue, tvPendingOrdersValue;

    private ProgressBar progressBar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_payment_order_summary);

        initViews();
        db = FirebaseFirestore.getInstance();
        loadOrderSummary();
    }

    private void initViews() {
        progressBar = findViewById(R.id.progressBar);
        tvTotalOrdersValue = findViewById(R.id.tvTotalOrdersValue);
        tvTotalRevenueValue = findViewById(R.id.tvTotalRevenueValue);
        tvMonthlyOrdersValue = findViewById(R.id.tvMonthlyOrdersValue);
        tvMonthlyRevenueValue = findViewById(R.id.tvMonthlyRevenueValue);
        tvCompletedOrdersValue = findViewById(R.id.tvCompletedOrdersValue);
        tvPendingOrdersValue = findViewById(R.id.tvPendingOrdersValue);
    }

    private void loadOrderSummary() {
        progressBar.setVisibility(View.VISIBLE);
        CollectionReference ordersRef = db.collection("Orders");

        ordersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int totalOrders = 0;
                double totalRevenue = 0;
                int monthlyOrders = 0;
                double monthlyRevenue = 0;
                int completedOrders = 0;
                int pendingOrders = 0;

                String currentMonth = new SimpleDateFormat("MM-yyyy").format(new Date());

                for (QueryDocumentSnapshot doc : task.getResult()) {
                    totalOrders++;

                    double price = 0.0;
                    Object priceObj = doc.get("price");
                    if (priceObj instanceof Number) {
                        price = ((Number) priceObj).doubleValue();
                    } else if (priceObj instanceof String) {
                        try {
                            price = Double.parseDouble((String) priceObj);
                        } catch (NumberFormatException e) {
                            price = 0.0;
                        }
                    }

                    totalRevenue += price;

                    String status = doc.getString("status");
                    if ("Completed".equalsIgnoreCase(status)) completedOrders++;
                    else pendingOrders++;

                    Date orderDate = doc.getDate("date");
                    if (orderDate != null) {
                        String orderMonth = new SimpleDateFormat("MM-yyyy").format(orderDate);
                        if (orderMonth.equals(currentMonth)) {
                            monthlyOrders++;
                            monthlyRevenue += price;
                        }
                    }
                }

                tvTotalOrdersValue.setText(String.valueOf(totalOrders));
                tvTotalRevenueValue.setText("₹" + String.format("%.2f", totalRevenue));
                tvMonthlyOrdersValue.setText(String.valueOf(monthlyOrders));
                tvMonthlyRevenueValue.setText("₹" + String.format("%.2f", monthlyRevenue));
                tvCompletedOrdersValue.setText(String.valueOf(completedOrders));
                tvPendingOrdersValue.setText(String.valueOf(pendingOrders));
            }

            progressBar.setVisibility(View.GONE);
        });
    }
}
