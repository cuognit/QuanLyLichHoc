package com.example.quanlylichhoc;

import android.app.Application;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        
        android.content.SharedPreferences prefs = getSharedPreferences("AppPrefs", android.content.Context.MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        if (isDarkMode) {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
        }
        
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    "quanlylichhoc_notifications",
                    "Thông báo nhắc nhở",
                    android.app.NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Kênh thông báo cho lịch học, nhiệm vụ và kỳ thi");
            
            android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
