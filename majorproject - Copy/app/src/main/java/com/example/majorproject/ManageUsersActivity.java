package com.example.majorproject;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.*;
import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {

    private RecyclerView recyclerViewUsers;
    private UserAdapter userAdapter;
    private List<User> userList;
    private ProgressBar progressBarUsers;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_users);

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        progressBarUsers = findViewById(R.id.progressBarUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        db = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();

        userAdapter = new UserAdapter(userList, this::editUser, this::deleteUser);
        recyclerViewUsers.setAdapter(userAdapter);

        loadUsers();
    }

    private void loadUsers() {
        progressBarUsers.setVisibility(View.VISIBLE);

        db.collection("Users").get().addOnCompleteListener(task -> {
            progressBarUsers.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() != null) {
                userList.clear();
                for (DocumentSnapshot doc : task.getResult()) {
                    User user = doc.toObject(User.class);
                    user.setUserId(doc.getId());
                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void editUser(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit User");


        View view = getLayoutInflater().inflate(R.layout.dialog_edit_user, null);
        EditText etName = view.findViewById(R.id.etUserName);
        EditText etEmail = view.findViewById(R.id.etUserEmail);
        EditText etRole = view.findViewById(R.id.etUserRole);

        etName.setText(user.getName());
        etEmail.setText(user.getEmail());
        etRole.setText(user.getRole());

        builder.setView(view);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String newName = etName.getText().toString().trim();
            String newEmail = etEmail.getText().toString().trim();
            String newRole = etRole.getText().toString().trim();

            if (newName.isEmpty() || newEmail.isEmpty() || newRole.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("Users").document(user.getUserId())
                    .update("name", newName, "email", newEmail, "role", newRole)
                    .addOnSuccessListener(aVoid -> {
                        user.setName(newName);
                        user.setEmail(newEmail);
                        user.setRole(newRole);
                        userAdapter.notifyDataSetChanged();
                        Toast.makeText(this, "User updated", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void deleteUser(User user) {
        db.collection("Users").document(user.getUserId()).delete().addOnSuccessListener(aVoid -> {
            userList.remove(user);
            userAdapter.notifyDataSetChanged();
            Toast.makeText(this, "User Deleted", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to delete user", Toast.LENGTH_SHORT).show());
    }
}
