package com.example.sportsdiary.ui;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.sportsdiary.R;
import com.example.sportsdiary.SportsDiaryApplication;
import com.example.sportsdiary.data.Workout;
import com.example.sportsdiary.viewmodel.WorkoutViewModel;
import com.example.sportsdiary.viewmodel.WorkoutViewModelFactory;

import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private LinearLayout achievementsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        achievementsContainer =
                findViewById(R.id.achievementsContainer);

        SportsDiaryApplication app =
                (SportsDiaryApplication) getApplication();

        WorkoutViewModelFactory factory =
                new WorkoutViewModelFactory(
                        app.getAppContainer().workoutRepository
                );

        WorkoutViewModel viewModel =
                new ViewModelProvider(this, factory)
                        .get(WorkoutViewModel.class);

        viewModel.getWorkouts().observe(this, this::showAchievements);
        viewModel.loadWorkouts();
    }

    private void showAchievements(List<Workout> workouts) {

        achievementsContainer.removeAllViews();

        int count = workouts == null ? 0 : workouts.size();
        int totalMinutes = 0;
        int totalCalories = 0;

        if (workouts != null) {
            for (Workout workout : workouts) {
                totalMinutes += workout.duration;
                totalCalories += workout.calories;
            }
        }

        addAchievement("🌱", "Първа стъпка",
                "Добави първата си тренировка.", count >= 1);

        addAchievement("🏃", "Постоянство",
                "Направи 5 тренировки.", count >= 5);

        addAchievement("🏆", "Спортна звезда",
                "Направи 10 тренировки.", count >= 10);

        addAchievement("⏱️", "Часове усилия",
                "Натрупай 300 минути тренировки.",
                totalMinutes >= 300);

        addAchievement("🔥", "Издръжливост",
                "Натрупай 1000 минути тренировки.",
                totalMinutes >= 1000);

        addAchievement("❤️", "Енергия",
                "Изгори 1000 калории.", totalCalories >= 1000);
    }

    private void addAchievement(
            String emoji,
            String title,
            String description,
            boolean unlocked) {

        TextView item = new TextView(this);

        item.setText(
                emoji + "  " + title +
                        "\n" + description +
                        "\n" + (unlocked ? "Отключено!" : "Все още заключено")
        );

        item.setTextSize(17);
        item.setPadding(20, 24, 20, 24);
        item.setBackgroundResource(
                android.R.drawable.dialog_holo_light_frame
        );

        item.setAlpha(unlocked ? 1f : 0.45f);

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        params.setMargins(0, 8, 0, 8);

        achievementsContainer.addView(item, params);
    }
}
