package com.example.sportsdiary.ui;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.sportsdiary.R;
import androidx.appcompat.widget.SwitchCompat;

import java.util.Calendar;
import java.util.Locale;

public class ReminderActivity extends AppCompatActivity {

    private SwitchCompat reminderSwitch;
    private TextView reminderTimeText;
    private CheckBox monday, tuesday, wednesday,
            thursday, friday, saturday, sunday;

    private int hour = 18;
    private int minute = 0;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        prefs = getSharedPreferences(
                ReminderScheduler.PREFS,
                MODE_PRIVATE
        );

        reminderSwitch = findViewById(R.id.reminderSwitch);
        reminderTimeText = findViewById(R.id.reminderTimeText);

        monday = findViewById(R.id.mondayCheck);
        tuesday = findViewById(R.id.tuesdayCheck);
        wednesday = findViewById(R.id.wednesdayCheck);
        thursday = findViewById(R.id.thursdayCheck);
        friday = findViewById(R.id.fridayCheck);
        saturday = findViewById(R.id.saturdayCheck);
        sunday = findViewById(R.id.sundayCheck);

        Button timeButton = findViewById(R.id.chooseTimeButton);
        Button saveButton = findViewById(R.id.saveReminderButton);

        loadSettings();

        timeButton.setOnClickListener(v -> showTimePicker());

        saveButton.setOnClickListener(v -> saveSettings());
    }

    private void loadSettings() {
        reminderSwitch.setChecked(
                prefs.getBoolean(ReminderScheduler.KEY_ENABLED, false)
        );

        hour = prefs.getInt(ReminderScheduler.KEY_HOUR, 18);
        minute = prefs.getInt(ReminderScheduler.KEY_MINUTE, 0);

        monday.setChecked(prefs.getBoolean("day_2", true));
        tuesday.setChecked(prefs.getBoolean("day_3", true));
        wednesday.setChecked(prefs.getBoolean("day_4", true));
        thursday.setChecked(prefs.getBoolean("day_5", true));
        friday.setChecked(prefs.getBoolean("day_6", true));
        saturday.setChecked(prefs.getBoolean("day_7", false));
        sunday.setChecked(prefs.getBoolean("day_1", false));

        updateTimeText();
    }

    private void showTimePicker() {
        new TimePickerDialog(
                this,
                (view, selectedHour, selectedMinute) -> {
                    hour = selectedHour;
                    minute = selectedMinute;
                    updateTimeText();
                },
                hour,
                minute,
                true
        ).show();
    }

    private void updateTimeText() {
        reminderTimeText.setText(
                String.format(Locale.getDefault(), "%02d:%02d", hour, minute)
        );
    }

    private void saveSettings() {
        boolean enabled = reminderSwitch.isChecked();

        prefs.edit()
                .putBoolean(ReminderScheduler.KEY_ENABLED, enabled)
                .putInt(ReminderScheduler.KEY_HOUR, hour)
                .putInt(ReminderScheduler.KEY_MINUTE, minute)
                .putBoolean("day_2", monday.isChecked())
                .putBoolean("day_3", tuesday.isChecked())
                .putBoolean("day_4", wednesday.isChecked())
                .putBoolean("day_5", thursday.isChecked())
                .putBoolean("day_6", friday.isChecked())
                .putBoolean("day_7", saturday.isChecked())
                .putBoolean("day_1", sunday.isChecked())
                .apply();

        if (enabled) {
            if (!monday.isChecked() && !tuesday.isChecked()
                    && !wednesday.isChecked() && !thursday.isChecked()
                    && !friday.isChecked() && !saturday.isChecked()
                    && !sunday.isChecked()) {

                Toast.makeText(this,
                        "Избери поне един ден.",
                        Toast.LENGTH_LONG).show();
                reminderSwitch.setChecked(false);
                prefs.edit().putBoolean(
                        ReminderScheduler.KEY_ENABLED, false
                ).apply();
                ReminderScheduler.cancel(this);
                return;
            }

            requestNotificationPermissionIfNeeded();
            ReminderScheduler.scheduleNext(this);

            Toast.makeText(this,
                    "Напомнянията са запазени.",
                    Toast.LENGTH_SHORT).show();

        } else {
            ReminderScheduler.cancel(this);

            Toast.makeText(this,
                    "Напомнянията са изключени.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
                ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    101
            );
        }
    }
}