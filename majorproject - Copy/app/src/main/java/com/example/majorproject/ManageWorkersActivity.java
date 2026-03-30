package com.example.majorproject;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;
import java.util.*;

public class ManageWorkersActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPassword, etCategory, etCity, etExperience, etContact, etRating, etProfileUrl;
    private Button btnAddWorker;
    private RecyclerView recyclerView;
    private WorkerAdapter workerAdapter;
    private List<Worker> workerList;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_workers);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        initializeViews();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        workerList = new ArrayList<>();
        workerAdapter = new WorkerAdapter(this, workerList, worker -> {
        });
        recyclerView.setAdapter(workerAdapter);

        btnAddWorker.setOnClickListener(v -> registerWorker());

        loadWorkers();
    }

    private void initializeViews() {
        etName = findViewById(R.id.etWorkerName);
        etEmail = findViewById(R.id.etWorkerEmail);
        etPassword = findViewById(R.id.etWorkerPassword);
        etCategory = findViewById(R.id.etWorkerCategory);
        etCity = findViewById(R.id.etWorkerCity);
        etExperience = findViewById(R.id.etWorkerExperience);
        etContact = findViewById(R.id.etWorkerContact);
        etRating = findViewById(R.id.etWorkerRating);
        etProfileUrl = findViewById(R.id.etWorkerProfileUrl);
        btnAddWorker = findViewById(R.id.btnAddWorker);
        recyclerView = findViewById(R.id.recyclerViewWorkers);
    }

    private void registerWorker() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String category = etCategory.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String experience = etExperience.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String rating = etRating.getText().toString().trim();
        String profileUrl = etProfileUrl.getText().toString().trim();

        if (!validateInputs(name, email, password)) return;

        String role = "Worker";

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && auth.getCurrentUser() != null) {
                        String workerId = auth.getCurrentUser().getUid();

                        Map<String, Object> workerData = new HashMap<>();
                        workerData.put("workerId", workerId);
                        workerData.put("name", name);
                        workerData.put("email", email);
                        workerData.put("category", category);
                        workerData.put("city", city);
                        workerData.put("experience", experience);
                        workerData.put("contact", contact);
                        workerData.put("rating", rating);
                        workerData.put("profileUrl", profileUrl);
                        workerData.put("role", role);

                        db.collection("Users").document(workerId)
                                .set(workerData)
                                .addOnSuccessListener(aVoid -> {
                                    Worker worker = new Worker(workerId, name, email, category, city, experience, contact, rating, profileUrl, role);
                                    workerList.add(worker);
                                    workerAdapter.notifyItemInserted(workerList.size() - 1);
                                    Toast.makeText(this, "Worker Registered Successfully", Toast.LENGTH_SHORT).show();
                                    clearFields();
                                })
                                .addOnFailureListener(e -> Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validateInputs(String name, String email, String password) {
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Name, Email, and Password are required!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid Email Format!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void loadWorkers() {
        db.collection("Users")
                .whereEqualTo("role", "Worker")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    workerList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        Worker worker = snapshot.toObject(Worker.class);
                        workerList.add(worker);
                    }
                    workerAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void clearFields() {
        etName.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etCategory.setText("");
        etCity.setText("");
        etExperience.setText("");
        etContact.setText("");
        etRating.setText("");
        etProfileUrl.setText("");
    }
}
