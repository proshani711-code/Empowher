package com.example.majorproject;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ManageServicesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewServices;
    private ServiceAdapter serviceAdapter;
    private List<ServiceCategory> serviceList;
    private ProgressBar progressBarServices;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private EditText etServiceName;
    private Button btnAddService;
    private Spinner spinnerIcons;
    private int[] serviceIcons = {
            R.drawable.painter,
            R.drawable.electrician,
            R.drawable.plumber,
            R.drawable.carpenter,
            R.drawable.cleaner
    };
    private String[] serviceIconNames = {
            "Painter", "Electrician", "Plumber", "Carpenter", "Cleaner"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_services);

        initializeViews();
        setupToolbar();

        setupRecyclerView();
        setupFirebase();
        loadServices();
        setupButtonListeners();
    }

    private void initializeViews() {
        recyclerViewServices = findViewById(R.id.recyclerViewServices);
        progressBarServices = findViewById(R.id.progressBarServices);
        etServiceName = findViewById(R.id.etServiceName);
        btnAddService = findViewById(R.id.btnAddService);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Manage Services");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupRecyclerView() {
        recyclerViewServices.setLayoutManager(new LinearLayoutManager(this));
        serviceList = new ArrayList<>();
        serviceAdapter = new ServiceAdapter(serviceList, this::editService, this::deleteService);
        recyclerViewServices.setAdapter(serviceAdapter);
    }

    private void setupFirebase() {
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void setupButtonListeners() {
        btnAddService.setOnClickListener(v -> addService());
    }

    private void loadServices() {
        progressBarServices.setVisibility(View.VISIBLE);
        db.collection("Services")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    progressBarServices.setVisibility(View.GONE);
                    if (task.isSuccessful() && task.getResult() != null) {
                        serviceList.clear();
                        for (DocumentSnapshot doc : task.getResult()) {
                            ServiceCategory service = doc.toObject(ServiceCategory.class);
                            if (service != null) {
                                service.setId(doc.getId());
                                serviceList.add(service);
                            }
                        }
                        serviceAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load services", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void addService() {
        String serviceName = etServiceName.getText().toString().trim();
        if (serviceName.isEmpty()) {
            etServiceName.setError("Service name required");
            return;
        }

        int selectedIconPosition = spinnerIcons.getSelectedItemPosition();
        int selectedIcon = serviceIcons[selectedIconPosition];

        String randomId = UUID.randomUUID().toString();
        ServiceCategory newService = new ServiceCategory(randomId, serviceName, selectedIcon);

        db.collection("Services").document(randomId)
                .set(newService)
                .addOnSuccessListener(aVoid -> {
                    serviceList.add(newService);
                    serviceAdapter.notifyDataSetChanged();
                    etServiceName.setText("");
                    spinnerIcons.setSelection(0);
                    Toast.makeText(this, "Service Added", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to add service: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void editService(String serviceId, String oldServiceName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Service");

        final EditText input = new EditText(this);
        input.setText(oldServiceName);
        builder.setView(input);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newServiceName = input.getText().toString().trim();
            if (newServiceName.isEmpty()) {
                input.setError("Service name required");
                return;
            }

            db.collection("Services").document(serviceId)
                    .update("name", newServiceName)
                    .addOnSuccessListener(aVoid -> {
                        updateServiceInList(serviceId, newServiceName);
                        Toast.makeText(this, "Service Updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void toggleServiceStatus(String serviceId, boolean currentStatus) {
        db.collection("Services").document(serviceId)
                .update("isActive", !currentStatus)
                .addOnSuccessListener(aVoid -> {
                    updateServiceStatusInList(serviceId, !currentStatus);
                    String status = currentStatus ? "disabled" : "enabled";
                    Toast.makeText(this, "Service " + status, Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to update status", Toast.LENGTH_SHORT).show());
    }

    private void updateServiceInList(String serviceId, String newName) {
        for (ServiceCategory service : serviceList) {
            if (service.getId().equals(serviceId)) {
                service.setName(newName);
                break;
            }
        }
        serviceAdapter.notifyDataSetChanged();
    }

    private void updateServiceStatusInList(String serviceId, boolean newStatus) {
        for (ServiceCategory service : serviceList) {
            if (service.getId().equals(serviceId)) {
                service.setActive(newStatus);
                break;
            }
        }
        serviceAdapter.notifyDataSetChanged();
    }

    private void deleteService(String serviceId) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this service?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("Services").document(serviceId)
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                serviceList.removeIf(service -> service.getId().equals(serviceId));
                                serviceAdapter.notifyDataSetChanged();
                                Toast.makeText(this, "Service Deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


}