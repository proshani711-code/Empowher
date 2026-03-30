package com.example.majorproject;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class FeedbackFormActivity extends AppCompatActivity {

    private RatingBar ratingBar;
    private EditText etReview;
    private Button btnSubmitFeedback;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private String orderId, workerId, userId, serviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_form);

        ratingBar = findViewById(R.id.ratingBar);
        etReview = findViewById(R.id.etReview);
        btnSubmitFeedback = findViewById(R.id.btnSubmitFeedback);
        progressBar = findViewById(R.id.progressBar);
        db = FirebaseFirestore.getInstance();

        orderId = getIntent().getStringExtra("orderId");
        workerId = getIntent().getStringExtra("workerId");
        userId = getIntent().getStringExtra("userId");
        serviceName = getIntent().getStringExtra("serviceName");

        if (workerId == null || userId == null) {
            Toast.makeText(this, "Invalid worker or user ID!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        btnSubmitFeedback.setOnClickListener(v -> submitFeedback());
    }

    private void submitFeedback() {
        float rating = ratingBar.getRating();
        String review = etReview.getText().toString().trim();

        if (rating == 0) {
            Toast.makeText(this, "Please provide a rating!", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> feedbackData = new HashMap<>();
        feedbackData.put("orderId", orderId);
        feedbackData.put("workerId", workerId);
        feedbackData.put("userId", userId);
        feedbackData.put("rating", rating);
        feedbackData.put("review", review);
        feedbackData.put("serviceName", serviceName);
        feedbackData.put("timestamp", System.currentTimeMillis());

        db.collection("Feedbacks").add(feedbackData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(FeedbackFormActivity.this, "Feedback submitted successfully!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    redirectToUserDashboard();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FeedbackFormActivity.this, "Failed to submit feedback!", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    Log.e("FeedbackFormActivity", "Error submitting feedback", e);
                });
    }
    private void redirectToUserDashboard() {
        Intent intent = new Intent(FeedbackFormActivity.this, UserDashboardActivity.class);
        startActivity(intent);
        finish();
    }
}
