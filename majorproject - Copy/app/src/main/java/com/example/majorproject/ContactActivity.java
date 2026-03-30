package com.example.majorproject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ContactActivity extends AppCompatActivity {

    private TextView tvUserName, tvUserPhone, tvWorkerName, tvWorkerPhone;
    private Button btnCallUser, btnChatUser, btnCallWorker, btnChatWorker;
    private String userPhone = "+919876543210";
    private String workerPhone = "+919876543211";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        tvUserName = findViewById(R.id.tvUserName);
        tvUserPhone = findViewById(R.id.tvUserPhone);
        tvWorkerName = findViewById(R.id.tvWorkerName);
        tvWorkerPhone = findViewById(R.id.tvWorkerPhone);
        btnCallUser = findViewById(R.id.btnCallUser);
        btnChatUser = findViewById(R.id.btnChatUser);
        btnCallWorker = findViewById(R.id.btnCallWorker);
        btnChatWorker = findViewById(R.id.btnChatWorker);
        Intent intent = getIntent();
        if (intent != null) {
            String userName = intent.getStringExtra("user_name");
            String workerName = intent.getStringExtra("worker_name");
            userPhone = intent.getStringExtra("user_phone");
            workerPhone = intent.getStringExtra("worker_phone");

            tvUserName.setText("User: " + userName);
            tvUserPhone.setText("Phone: " + userPhone);
            tvWorkerName.setText("Worker: " + workerName);
            tvWorkerPhone.setText("Phone: " + workerPhone);
        }
        btnCallUser.setOnClickListener(v -> makeCall(userPhone));

        btnChatUser.setOnClickListener(v -> openWhatsAppChat(userPhone));

        btnCallWorker.setOnClickListener(v -> makeCall(workerPhone));

        btnChatWorker.setOnClickListener(v -> openWhatsAppChat(workerPhone));
    }

    private void makeCall(String phoneNumber) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        startActivity(intent);
    }

    private void openWhatsAppChat(String phoneNumber) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("https://wa.me/" + phoneNumber));
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
