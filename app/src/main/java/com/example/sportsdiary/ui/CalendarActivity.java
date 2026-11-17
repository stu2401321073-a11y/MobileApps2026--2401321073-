package com.example.sportsdiary.ui;

import android.os.Bundle;
import android.widget.CalendarView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.sportsdiary.R;
import com.example.sportsdiary.SportsDiaryApplication;
import com.example.sportsdiary.data.Workout;
import com.example.sportsdiary.viewmodel.WorkoutViewModel;
import com.example.sportsdiary.viewmodel.WorkoutViewModelFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {

    private CalendarView calendarView;
    private LinearLayout workoutsContainer;
    private TextView selectedDateText;
    private List<Workout> allWorkouts;

    private final SimpleDateFormat dateFormat =
            new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        calendarView = findViewById(R.id.calendarView);
        workoutsContainer = findViewById(R.id.calendarWorkoutsContainer);
        selectedDateText = findViewById(R.id.selectedDateText);

        SportsDiaryApplication app =
                (SportsDiaryApplication) getApplication();

        WorkoutViewModelFactory factory =
                new WorkoutViewModelFactory(
                        app.getAppContainer().workoutRepository
                );

        WorkoutViewModel viewModel =
                new ViewModelProvider(this, factory)
                        .get(WorkoutViewModel.class);

        viewModel.getWorkouts().observe(this, workouts -> {
            allWorkouts = workouts;
            showWorkoutsForDate(dateFormat.format(new Date(calendarView.getDate())));
        });

        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            String date = String.format(
                    Locale.getDefault(),
                    "%02d.%02d.%04d",
                    dayOfMonth,
                    month + 1,
                    year
            );

            showWorkoutsForDate(date);
        });

        viewModel.loadWorkouts();
    }

    private void showWorkoutsForDate(String date) {

        selectedDateText.setText("Тренировки за " + date);
        workoutsContainer.removeAllViews();

        if (allWorkouts == null) return;

        int count = 0;

        for (Workout workout : allWorkouts) {
            if (date.equals(workout.date)) {

                TextView item = new TextView(this);

                item.setText(
                        "Спорт: " + workout.sport +
                                "\nПродължителност: " + workout.duration + " мин." +
                                "\nКалории: " + workout.calories +
                                "\nБележка: " + workout.note
                );

                item.setTextSize(16);
                item.setPadding(20, 20, 20, 20);

                workoutsContainer.addView(item);
                count++;
            }
        }

        if (count == 0) {
            TextView empty = new TextView(this);
            empty.setText("Няма тренировки за тази дата.");
            empty.setTextSize(16);
            empty.setPadding(16, 16, 16, 16);
            workoutsContainer.addView(empty);
        }
    }
}

