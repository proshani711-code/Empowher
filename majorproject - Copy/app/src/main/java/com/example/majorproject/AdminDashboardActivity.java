package com.example.majorproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    private Button btnManageUsers, btnManageWorkers, btnManageServices, btnViewOrders;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        btnManageUsers = findViewById(R.id.btnManageUsers);
        btnManageWorkers = findViewById(R.id.btnManageWorkers);
        btnManageServices = findViewById(R.id.btnManageServices);
        btnViewOrders = findViewById(R.id.btnViewOrders);

        progressBar = findViewById(R.id.progressBar);


        if (btnManageUsers != null) {
            btnManageUsers.setOnClickListener(v -> navigateToActivity(ManageUsersActivity.class));
        }

        if (btnManageWorkers != null) {
            btnManageWorkers.setOnClickListener(v -> navigateToActivity(ManageWorkersActivity.class));
        }

        if (btnManageServices != null) {
            btnManageServices.setOnClickListener(v -> navigateToActivity(ManageServicesActivity.class));
        }
        if (btnViewOrders != null) {
            btnViewOrders.setOnClickListener(v -> navigateToActivity(ManageOrdersActivity.class));
        }
    }
    public void viewSummary(View v)
    {
        Intent i=new Intent(this, AdminPaymentOrderSummaryActivity.class);
        startActivity(i);
    }
    private void navigateToActivity(Class<?> targetActivity) {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
        startActivity(new Intent(AdminDashboardActivity.this, targetActivity));
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }
}
