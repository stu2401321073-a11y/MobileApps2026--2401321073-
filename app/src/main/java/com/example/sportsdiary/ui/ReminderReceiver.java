package com.example.sportsdiary.ui;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.sportsdiary.R;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "workout_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {

        createNotificationChannel(context);

        if (Build.VERSION.SDK_INT < 33 ||
                ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED) {

            Intent openIntent = new Intent(context, MainActivity.class);

            PendingIntent contentIntent = PendingIntent.getActivity(
                    context,
                    2001,
                    openIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT |
                            PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("Време е за тренировка!")
                            .setContentText("Отдели малко време за спорт днес.")
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setAutoCancel(true)
                            .setContentIntent(contentIntent);

            NotificationManagerCompat.from(context)
                    .notify(2001, builder.build());
        }

        ReminderScheduler.scheduleNext(context);
    }

    private void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Напомняния за тренировки",
                    NotificationManager.IMPORTANCE_DEFAULT
            );

            channel.setDescription("Известия за планирани тренировки");

            NotificationManager manager =
                    context.getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}