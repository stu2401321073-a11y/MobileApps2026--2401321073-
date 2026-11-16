package com.example.sportsdiary.ui;

import com.example.sportsdiary.R;
import com.example.sportsdiary.SportsDiaryApplication;
import com.example.sportsdiary.data.Workout;
import com.example.sportsdiary.data.WorkoutRepository;
import com.example.sportsdiary.viewmodel.WorkoutViewModel;
import com.example.sportsdiary.viewmodel.WorkoutViewModelFactory;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.List;

public class EditWorkoutActivity extends AppCompatActivity {

    private EditText sportEditText;
    private EditText dateEditText;
    private EditText durationEditText;
    private EditText caloriesEditText;
    private EditText noteEditText;
    private Workout originalWorkout;
    private WorkoutViewModel workoutViewModel;
    private int workoutId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_workout);

        sportEditText = findViewById(R.id.sportEditText);
        dateEditText = findViewById(R.id.dateEditText);
        durationEditText = findViewById(R.id.durationEditText);
        caloriesEditText = findViewById(R.id.caloriesEditText);
        noteEditText = findViewById(R.id.noteEditText);

        Button updateButton = findViewById(R.id.updateWorkoutButton);
        Button deleteButton = findViewById(R.id.deleteWorkoutButton);
        Button shareButton = findViewById(R.id.shareWorkoutButton);

        SportsDiaryApplication app =
                (SportsDiaryApplication) getApplication();

        WorkoutViewModelFactory factory =
                new WorkoutViewModelFactory(
                        app.getAppContainer().workoutRepository
                );

        workoutViewModel = new ViewModelProvider(this, factory)
                .get(WorkoutViewModel.class);

        workoutViewModel.getErrorMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                workoutViewModel.clearError();
            }
        });

        workoutId = getIntent().getIntExtra("workoutId", -1);

        if (workoutId == -1) {
            Toast.makeText(this,
                    "Не е намерена тренировка.",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadWorkout();

        updateButton.setOnClickListener(v -> updateWorkout());
        deleteButton.setOnClickListener(v -> deleteWorkout());
        shareButton.setOnClickListener(v -> shareWorkout());
    }

    private void loadWorkout() {
        workoutViewModel.getWorkoutById(workoutId, workout -> {
            if (workout != null) {
                originalWorkout = workout;
                sportEditText.setText(workout.sport);
                dateEditText.setText(workout.date);
                durationEditText.setText(String.valueOf(workout.duration));
                caloriesEditText.setText(String.valueOf(workout.calories));
                noteEditText.setText(workout.note);
            } else {
                Toast.makeText(this,
                        "Тренировката не е намерена.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateWorkout() {
        String sport = sportEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        String durationText = durationEditText.getText().toString().trim();
        String caloriesText = caloriesEditText.getText().toString().trim();
        String note = noteEditText.getText().toString().trim();

        if (sport.isEmpty() || date.isEmpty()
                || durationText.isEmpty() || caloriesText.isEmpty()) {
            Toast.makeText(this,
                    "Попълни задължителните полета.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidDate(date)) {
            Toast.makeText(this,
                    "Въведи датата във формат дд.ММ.гггг.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int duration = Integer.parseInt(durationText);
            int calories = Integer.parseInt(caloriesText);

            if (duration <= 0 || calories < 0) {
                Toast.makeText(this,
                        "Въведи валидни числа.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Workout workout = new Workout();
            workout.id = workoutId;
            workout.sport = sport;
            workout.date = date;
            workout.duration = duration;
            workout.calories = calories;
            workout.note = note;

            if (originalWorkout == null) {
                Toast.makeText(
                        this,
                        "Изчакай зареждането на тренировката.",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            workoutViewModel.getAllWorkouts(
                    new WorkoutRepository.Callback<List<Workout>>() {

                        @Override
                        public void onComplete(List<Workout> existingWorkouts) {

                            int maxOtherDuration = 0;
                            int maxOtherCalories = 0;

                            if (existingWorkouts != null) {
                                for (Workout existing : existingWorkouts) {

                                    if (existing.id == workoutId) {
                                        continue;
                                    }

                                    maxOtherDuration = Math.max(
                                            maxOtherDuration,
                                            existing.duration
                                    );

                                    maxOtherCalories = Math.max(
                                            maxOtherCalories,
                                            existing.calories
                                    );
                                }
                            }

                            boolean newDurationRecord =
                                    workout.duration > originalWorkout.duration
                                            && workout.duration > maxOtherDuration;

                            boolean newCaloriesRecord =
                                    workout.calories > originalWorkout.calories
                                            && workout.calories > maxOtherCalories;

                            workoutViewModel.update(workout, () -> {

                                Toast.makeText(
                                        EditWorkoutActivity.this,
                                        "Промените са запазени!",
                                        Toast.LENGTH_SHORT
                                ).show();

                                if (newDurationRecord) {
                                    PersonalRecordNotifier.show(
                                            EditWorkoutActivity.this,
                                            "🏆 Нов личен рекорд!",
                                            "Най-дълга тренировка: "
                                                    + workout.duration + " минути!",
                                            2001
                                    );
                                }

                                if (newCaloriesRecord) {
                                    PersonalRecordNotifier.show(
                                            EditWorkoutActivity.this,
                                            "🔥 Нов личен рекорд!",
                                            "Най-много изгорени калории: "
                                                    + workout.calories + "!",
                                            2002
                                    );
                                }

                                finish();
                            });
                        }

                        @Override
                        public void onError(Exception e) {
                            // Грешката се показва чрез LiveData във ViewModel.
                        }
                    }
            );

        } catch (NumberFormatException e) {
            Toast.makeText(this,
                    "Продължителността и калориите трябва да са числа.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isValidDate(String date) {
        SimpleDateFormat format =
                new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        format.setLenient(false);

        ParsePosition position = new ParsePosition(0);
        format.parse(date, position);

        return position.getIndex() == date.length()
                && position.getErrorIndex() == -1;
    }

    private void deleteWorkout() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Изтриване")
                .setMessage("Сигурен ли си, че искаш да изтриеш тренировката?")
                .setPositiveButton("Да", (dialog, which) -> {

                    workoutViewModel.getWorkoutById(workoutId, workout -> {
                        if (workout != null) {
                            workoutViewModel.delete(workout, () -> {
                                Toast.makeText(this,
                                        "Тренировката е изтрита!",
                                        Toast.LENGTH_SHORT).show();
                                finish();
                            });
                        } else {
                            Toast.makeText(this,
                                    "Тренировката не е намерена.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Не", null)
                .show();
    }

    private void shareWorkout() {
        String sport = sportEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        String duration = durationEditText.getText().toString().trim();
        String calories = caloriesEditText.getText().toString().trim();
        String note = noteEditText.getText().toString().trim();

        String message = "Моята тренировка:\n"
                + "Спорт: " + sport + "\n"
                + "Дата: " + date + "\n"
                + "Продължителност: " + duration + " минути\n"
                + "Калории: " + calories + "\n"
                + "Бележка: " + note;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);

        startActivity(Intent.createChooser(
                shareIntent,
                "Сподели тренировката"
        ));
    }
}
