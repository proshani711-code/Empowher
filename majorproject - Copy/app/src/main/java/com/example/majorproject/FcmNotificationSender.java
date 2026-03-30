package com.example.majorproject;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class FcmNotificationSender {
    private static final String SERVER_KEY = "uHWifAAP6G12JWwNCJMkwPeQP_8IHGxEFkhWTVRMTRQ";
    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";

    public static void sendNotification(String userToken, String title, String message) {
        new SendNotificationTask(userToken, title, message).execute();
    }

    private static class SendNotificationTask extends AsyncTask<Void, Void, Void> {
        private final String userToken, title, message;

        SendNotificationTask(String userToken, String title, String message) {
            this.userToken = userToken;
            this.title = title;
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(FCM_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "key=" + SERVER_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);


                JSONObject json = new JSONObject();
                json.put("to", userToken);

                JSONObject notification = new JSONObject();
                notification.put("title", title);
                notification.put("body", message);
                json.put("notification", notification);


                JSONObject data = new JSONObject();
                data.put("key1", "value1"); // Example data
                json.put("data", data);


                OutputStream os = conn.getOutputStream();
                os.write(json.toString().getBytes("UTF-8"));
                os.close();


                Scanner inStream = new Scanner(conn.getInputStream());
                while (inStream.hasNextLine()) {
                    Log.d("FCM Response", inStream.nextLine());
                }
            } catch (Exception e) {
                Log.e("FCM Error", "Error sending notification", e);
            }
            return null;
        }
    }
}