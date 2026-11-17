package com.example.sportsdiary.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.sportsdiary.R;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText profileNameInput;
    private MaterialAutoCompleteTextView preferredSportInput;
    private RadioGroup themeRadioGroup;

    private SharedPreferences preferences;

    private final String[] sports = {
            "Бягане",
            "Фитнес",
            "Колоездене",
            "Плуване",
            "Йога",
            "Друг спорт"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        preferences = getSharedPreferences(
                "sports_profile",
                MODE_PRIVATE
        );

        profileNameInput = findViewById(R.id.profileNameInput);
        preferredSportInput = findViewById(R.id.preferredSportInput);
        themeRadioGroup = findViewById(R.id.themeRadioGroup);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                sports
        );

        preferredSportInput.setAdapter(adapter);

        preferredSportInput.setOnClickListener(v ->
                preferredSportInput.showDropDown()
        );

        loadProfile();

        findViewById(R.id.saveProfileButton).setOnClickListener(v ->
                saveProfile()
        );
    }

    private void loadProfile() {

        String name = preferences.getString("name", "");
        String sport = preferences.getString("sport", "Бягане");

        profileNameInput.setText(name);
        preferredSportInput.setText(sport, false);

        int theme = preferences.getInt(
                "theme",
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        );

        if (theme == AppCompatDelegate.MODE_NIGHT_NO) {
            themeRadioGroup.check(R.id.themeLight);

        } else if (theme == AppCompatDelegate.MODE_NIGHT_YES) {
            themeRadioGroup.check(R.id.themeDark);

        } else {
            themeRadioGroup.check(R.id.themeSystem);
        }
    }

    private void saveProfile() {

        String name = profileNameInput.getText() == null
                ? ""
                : profileNameInput.getText().toString().trim();

        String sport = preferredSportInput.getText().toString().trim();

        int selectedTheme;

        int checkedId = themeRadioGroup.getCheckedRadioButtonId();

        if (checkedId == R.id.themeLight) {
            selectedTheme = AppCompatDelegate.MODE_NIGHT_NO;

        } else if (checkedId == R.id.themeDark) {
            selectedTheme = AppCompatDelegate.MODE_NIGHT_YES;

        } else {
            selectedTheme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }

        preferences.edit()
                .putString("name", name)
                .putString("sport", sport)
                .putInt("theme", selectedTheme)
                .apply();

        AppCompatDelegate.setDefaultNightMode(selectedTheme);

        Toast.makeText(
                this,
                "Профилът е запазен!",
                Toast.LENGTH_SHORT
        ).show();

        finish();
    }
}