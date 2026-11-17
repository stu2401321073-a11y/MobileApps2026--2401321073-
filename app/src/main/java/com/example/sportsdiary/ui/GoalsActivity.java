
package com.example.sportsdiary.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.sportsdiary.R;
import com.example.sportsdiary.SportsDiaryApplication;
import com.example.sportsdiary.data.Workout;
import com.example.sportsdiary.viewmodel.WorkoutViewModel;
import com.example.sportsdiary.viewmodel.WorkoutViewModelFactory;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GoalsActivity extends AppCompatActivity {

    private EditText goalWorkoutsInput;
    private EditText goalMinutesInput;
    private TextView workoutsProgressText;
    private TextView minutesProgressText;
    private ProgressBar workoutsProgress;
    private ProgressBar minutesProgress;

    private SharedPreferences preferences;

    private int completedWorkouts = 0;
    private int completedMinutes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_goals);

        goalWorkoutsInput = findViewById(R.id.goalWorkoutsInput);
        goalMinutesInput = findViewById(R.id.goalMinutesInput);
        workoutsProgressText = findViewById(R.id.workoutsProgressText);
        minutesProgressText = findViewById(R.id.minutesProgressText);
        workoutsProgress = findViewById(R.id.workoutsProgress);
        minutesProgress = findViewById(R.id.minutesProgress);

        preferences = getSharedPreferences("sports_goals", MODE_PRIVATE);

        goalWorkoutsInput.setText(
                String.valueOf(preferences.getInt("workouts_goal", 5))
        );

        goalMinutesInput.setText(
                String.valueOf(preferences.getInt("minutes_goal", 150))
        );

        Button saveButton = findViewById(R.id.saveGoalsButton);

        saveButton.setOnClickListener(v -> saveGoals());

        SportsDiaryApplication app =
                (SportsDiaryApplication) getApplication();

        WorkoutViewModelFactory factory =
                new WorkoutViewModelFactory(
                        app.getAppContainer().workoutRepository
                );

        WorkoutViewModel viewModel =
                new ViewModelProvider(this, factory)
                        .get(WorkoutViewModel.class);

        viewModel.getWorkouts().observe(this, this::calculateProgress);
        viewModel.loadWorkouts();
    }

    private void saveGoals() {

        try {
            int workoutGoal = Integer.parseInt(
                    goalWorkoutsInput.getText().toString().trim()
            );

            int minuteGoal = Integer.parseInt(
                    goalMinutesInput.getText().toString().trim()
            );

            if (workoutGoal <= 0 || minuteGoal <= 0) {
                Toast.makeText(this,
                        "Целите трябва да са по-големи от нула.",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            preferences.edit()
                    .putInt("workouts_goal", workoutGoal)
                    .putInt("minutes_goal", minuteGoal)
                    .apply();

            calculateProgress(null);

            Toast.makeText(this,
                    "Целите са запазени!",
                    Toast.LENGTH_SHORT).show();

        } catch (NumberFormatException e) {
            Toast.makeText(this,
                    "Въведи валидни числа.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void calculateProgress(List<Workout> workouts) {

        completedWorkouts = 0;
        completedMinutes = 0;

        if (workouts != null) {

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_WEEK,
                    calendar.getFirstDayOfWeek());

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            Calendar end = (Calendar) calendar.clone();
            end.add(Calendar.DAY_OF_YEAR, 7);

            SimpleDateFormat format =
                    new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

            for (Workout workout : workouts) {
                try {
                    Date workoutDate = format.parse(workout.date);

                    if (workoutDate != null
                            && !workoutDate.before(calendar.getTime())
                            && workoutDate.before(end.getTime())) {

                        completedWorkouts++;
                        completedMinutes += workout.duration;
                    }

                } catch (Exception ignored) {
                }
            }
        }

        updateProgress();
    }

    private void updateProgress() {

        int workoutGoal = preferences.getInt("workouts_goal", 5);
        int minuteGoal = preferences.getInt("minutes_goal", 150);

        workoutsProgress.setMax(workoutGoal);
        workoutsProgress.setProgress(
                Math.min(completedWorkouts, workoutGoal)
        );

        minutesProgress.setMax(minuteGoal);
        minutesProgress.setProgress(
                Math.min(completedMinutes, minuteGoal)
        );

        workoutsProgressText.setText(
                "Тренировки: " + completedWorkouts + " / " + workoutGoal
        );

        minutesProgressText.setText(
                "Минути: " + completedMinutes + " / " + minuteGoal
        );
    }
}
