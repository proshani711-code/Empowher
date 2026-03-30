package com.example.majorproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    private static final String STATIC_ADMIN_EMAIL = "adminr@gmail.com";
    private static final String STATIC_ADMIN_PASSWORD = "admin1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> loginUser());
    }
    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (!validateInputs(email, password)) return;
        if (email.equals(STATIC_ADMIN_EMAIL) && password.equals(STATIC_ADMIN_PASSWORD)) {
            navigateToAdminDashboard();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (!task.isSuccessful()) {
                        Toast.makeText(this, "Login Failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null) checkUserRole(user.getUid());
                });
    }

    private void checkUserRole(String userId) {
        db.collection("Users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);

                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "User role not found!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String role = documentSnapshot.getString("role");
                    if (TextUtils.isEmpty(role)) {
                        Toast.makeText(this, "Role not assigned. Contact support.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    navigateToDashboard(role.toLowerCase());
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToAdminDashboard() {
        startActivity(new Intent(this, AdminDashboardActivity.class));
        showToast("Admin Login Successful!");
        finish();
    }

    private void navigateToDashboard(String role) {
        Intent intent;
        switch (role) {
            case "admin":
                intent = new Intent(this, AdminDashboardActivity.class);
                showToast("Admin Login Successful!");
                break;
            case "worker":
                intent = new Intent(this, WorkerDashboardActivity.class);
                showToast("Worker Login Successful!");
                break;
            case "user":
                intent = new Intent(this, UserDashboardActivity.class);
                showToast("User Login Successful!");
                break;
            default:
                showToast("Invalid role! Contact support.");
                return;
        }
        startActivity(intent);
        finish();
    }

    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            showToast("Email and Password are required!");
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Invalid Email Format!");
            return false;
        }
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void goToRegister(View v) {
        startActivity(new Intent(this, RegisterActivity.class));
    }
}
