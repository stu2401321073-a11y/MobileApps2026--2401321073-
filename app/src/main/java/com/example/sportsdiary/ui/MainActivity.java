package com.example.sportsdiary.ui;

import android.content.Intent;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.sportsdiary.R;
import com.example.sportsdiary.SportsDiaryApplication;
import com.example.sportsdiary.data.Workout;
import com.example.sportsdiary.viewmodel.WorkoutViewModel;
import com.example.sportsdiary.viewmodel.WorkoutViewModelFactory;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TreeSet;
import java.util.TimeZone;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.net.Uri;
import android.content.ClipData;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.sportsdiary.ui.ReminderActivity;

public class MainActivity extends AppCompatActivity {

    private LinearLayout workoutsContainer;
    private TextView emptyText;
    private TextView motivationText;
    private TextView totalWorkoutsText;
    private TextView totalDurationText;
    private TextView totalCaloriesText;

    private TextView currentStreakText;
    private TextView longestStreakText;

    private MaterialAutoCompleteTextView sportFilter;
    private EditText workoutSearchEditText;

    private WorkoutViewModel workoutViewModel;

    private List<Workout> allWorkouts = new ArrayList<>();

    private String selectedSport = "Всички тренировки";

    private final String[] sports = {
            "Всички тренировки",
            "Бягане",
            "Фитнес",
            "Колоездене",
            "Плуване"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SportsDiaryApplication app =
                (SportsDiaryApplication) getApplication();

        WorkoutViewModelFactory factory =
                new WorkoutViewModelFactory(
                        app.getAppContainer().workoutRepository
                );

        workoutViewModel = new ViewModelProvider(this, factory)
                .get(WorkoutViewModel.class);

        initializeViews();
        updateWelcomeMessage();
        setupListeners();
        observeViewModel();
    }

    private void initializeViews() {

        workoutsContainer = findViewById(R.id.workoutsContainer);
        emptyText = findViewById(R.id.emptyText);

        motivationText = findViewById(R.id.motivationText);

        currentStreakText = findViewById(R.id.currentStreakText);
        longestStreakText = findViewById(R.id.longestStreakText);

        totalWorkoutsText = findViewById(R.id.totalWorkoutsText);
        totalDurationText = findViewById(R.id.totalDurationText);
        totalCaloriesText = findViewById(R.id.totalCaloriesText);

        sportFilter = findViewById(R.id.sportFilter);
        workoutSearchEditText = findViewById(R.id.workoutSearchEditText);

        sportFilter.setText(selectedSport, false);
    }

    private void setupListeners() {

        Button addWorkoutButton =
                findViewById(R.id.addWorkoutButton);

        addWorkoutButton.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AddWorkoutActivity.class
            );
            startActivity(intent);
        });

        sportFilter.setOnClickListener(v ->
                showSportFilterPopup()
        );

        Button statisticsButton =
                findViewById(R.id.statisticsButton);

        statisticsButton.setOnClickListener(v ->
                openActivity(StatisticsActivity.class)
        );

        findViewById(R.id.calendarButton).setOnClickListener(v ->
                openActivity(CalendarActivity.class)
        );

        findViewById(R.id.goalsButton).setOnClickListener(v ->
                openActivity(GoalsActivity.class)
        );

        findViewById(R.id.achievementsButton).setOnClickListener(v ->
                openActivity(AchievementsActivity.class)
        );

        findViewById(R.id.reminderButton).setOnClickListener(v ->
                openActivity(ReminderActivity.class)
        );

        findViewById(R.id.profileButton).setOnClickListener(v ->
                openActivity(ProfileActivity.class)
        );

        findViewById(R.id.exportWorkoutsButton).setOnClickListener(v ->
                exportWorkoutsToCsv()
        );

        workoutSearchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                filterWorkouts();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void openActivity(Class<?> activityClass) {
        Intent intent = new Intent(MainActivity.this, activityClass);
        startActivity(intent);
    }

    private void showSportFilterPopup() {

        LinearLayout popupContent = new LinearLayout(this);
        popupContent.setOrientation(LinearLayout.VERTICAL);
        popupContent.setBackgroundColor(Color.WHITE);

        PopupWindow popupWindow = new PopupWindow(
                popupContent,
                sportFilter.getWidth(),
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(
                new ColorDrawable(Color.WHITE)
        );

        popupWindow.setOutsideTouchable(true);
        popupWindow.setElevation(8f);

        for (String sport : sports) {

            TextView item = (TextView) getLayoutInflater().inflate(
                    R.layout.item_sport_filter,
                    popupContent,
                    false
            );

            item.setText(sport);
            item.setSelected(sport.equals(selectedSport));

            item.setOnHoverListener((view, event) -> {

                if (event.getAction() == MotionEvent.ACTION_HOVER_ENTER
                        || event.getAction() == MotionEvent.ACTION_HOVER_MOVE) {

                    view.setBackgroundResource(R.drawable.sport_item_hover);

                } else if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {

                    if (sport.equals(selectedSport)) {
                        view.setBackgroundResource(R.drawable.sport_item_selected);
                    } else {
                        view.setBackgroundResource(R.drawable.sport_item_normal);
                    }
                }

                return true;
            });

            item.setOnClickListener(view -> {

                selectedSport = sport;
                sportFilter.setText(selectedSport, false);

                filterWorkouts();

                popupWindow.dismiss();
            });

            popupContent.addView(item);
        }

        popupWindow.showAsDropDown(sportFilter);
    }

    private void observeViewModel() {

        workoutViewModel.getWorkouts().observe(this, workouts -> {

            allWorkouts = workouts != null
                    ? new ArrayList<>(workouts)
                    : new ArrayList<>();

            updateStreak(allWorkouts);
            filterWorkouts();
        });

        workoutViewModel.getTotalWorkouts().observe(this, value ->
                totalWorkoutsText.setText(
                        "Общо тренировки: " + value
                )
        );

        workoutViewModel.getTotalDuration().observe(this, value ->
                totalDurationText.setText(
                        "Общо минути: " + value
                )
        );

        workoutViewModel.getTotalCalories().observe(this, value ->
                totalCaloriesText.setText(
                        "Изгорени калории: " + value
                )
        );

        workoutViewModel.getErrorMessage().observe(this, message -> {

            if (message != null && !message.isEmpty()) {

                Toast.makeText(
                        MainActivity.this,
                        message,
                        Toast.LENGTH_LONG
                ).show();

                workoutViewModel.clearError();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (workoutViewModel != null) {
            workoutViewModel.loadWorkouts();
        }
    }

    private void filterWorkouts() {

        List<Workout> filteredWorkouts = new ArrayList<>();

        String query = workoutSearchEditText.getText()
                .toString()
                .trim()
                .toLowerCase(Locale.ROOT);

        for (Workout workout : allWorkouts) {

            boolean sportMatches =
                    selectedSport.equals("Всички тренировки")
                            || (workout.sport != null
                            && workout.sport.equalsIgnoreCase(selectedSport));

            String sport = workout.sport == null
                    ? "" : workout.sport.toLowerCase(Locale.ROOT);

            String date = workout.date == null
                    ? "" : workout.date.toLowerCase(Locale.ROOT);

            String note = workout.note == null
                    ? "" : workout.note.toLowerCase(Locale.ROOT);

            boolean searchMatches =
                    query.isEmpty()
                            || sport.contains(query)
                            || date.contains(query)
                            || note.contains(query);

            if (sportMatches && searchMatches) {
                filteredWorkouts.add(workout);
            }
        }

        displayWorkouts(filteredWorkouts);
    }

    private void displayWorkouts(List<Workout> workouts) {

        workoutsContainer.removeAllViews();

        if (workouts == null || workouts.isEmpty()) {

            String query = workoutSearchEditText.getText()
                    .toString().trim();

            if (!query.isEmpty()) {
                emptyText.setText("Няма намерени тренировки.");
            } else if (!selectedSport.equals("Всички тренировки")) {
                emptyText.setText("Няма тренировки за избрания спорт.");
            } else {
                emptyText.setText("Все още нямаш тренировки.");
            }

            emptyText.setVisibility(TextView.VISIBLE);
            return;
        }

        emptyText.setVisibility(TextView.GONE);

        for (Workout workout : workouts) {

            TextView workoutView = new TextView(this);

            String text =
                    "Спорт: " + workout.sport +
                            "\nДата: " + workout.date +
                            "\nПродължителност: " + workout.duration + " мин." +
                            "\nКалории: " + workout.calories +
                            "\nБележка: " + workout.note;

            workoutView.setText(text);
            workoutView.setTextSize(16);
            workoutView.setPadding(20, 20, 20, 20);

            workoutView.setOnClickListener(v -> {

                Intent intent = new Intent(
                        MainActivity.this,
                        EditWorkoutActivity.class
                );

                intent.putExtra("workoutId", workout.id);
                startActivity(intent);
            });

            workoutsContainer.addView(workoutView);
        }
    }

    private void exportWorkoutsToCsv() {

        if (allWorkouts == null || allWorkouts.isEmpty()) {
            Toast.makeText(this,
                    "Няма тренировки за експортиране.",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder csv = new StringBuilder();

        // BOM за правилно разчитане на кирилицата в Excel
        csv.append('\uFEFF');

        csv.append("Спорт;Дата;Продължителност (минути);Калории;Бележка\n");

        for (Workout workout : allWorkouts) {
            csv.append(csvValue(workout.sport)).append(";")
                    .append(csvValue(workout.date)).append(";")
                    .append(workout.duration).append(";")
                    .append(workout.calories).append(";")
                    .append(csvValue(workout.note))
                    .append("\n");
        }

        File exportDirectory = new File(getCacheDir(), "exports");

        if (!exportDirectory.exists() && !exportDirectory.mkdirs()) {
            Toast.makeText(this,
                    "Неуспешно създаване на CSV файла.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        String timestamp = new SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
        ).format(new Date());

        File csvFile = new File(
                exportDirectory,
                "sports_diary_" + timestamp + ".csv"
        );

        try (FileOutputStream outputStream =
                     new FileOutputStream(csvFile)) {

            outputStream.write(
                    csv.toString().getBytes(StandardCharsets.UTF_8)
            );

            Uri fileUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    csvFile
            );

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Моите тренировки - Спортен дневник"
            );
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            shareIntent.setClipData(
                    ClipData.newUri(getContentResolver(), "CSV файл", fileUri)
            );

            startActivity(Intent.createChooser(
                    shareIntent,
                    "Сподели тренировките"
            ));

        } catch (IOException e) {
            Toast.makeText(this,
                    "Грешка при експортиране на тренировките.",
                    Toast.LENGTH_LONG).show();
        }
    }

    private String csvValue(String value) {
        if (value == null) {
            return "\"\"";
        }

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private void updateStreak(List<Workout> workouts) {

        Set<Long> activeDays = new TreeSet<>();

        for (Workout workout : workouts) {
            Long day = getDayKey(workout.date);

            if (day != null) {
                activeDays.add(day);
            }
        }

        if (activeDays.isEmpty()) {
            currentStreakText.setText("🔥 Текуща серия: 0 дни");
            longestStreakText.setText("🏆 Най-дълга серия: 0 дни");
            return;
        }

        long today = getTodayKey();

        long currentStreak = 0;

        if (activeDays.contains(today)) {
            currentStreak = countBackwards(activeDays, today);
        } else if (activeDays.contains(today - 1)) {
            currentStreak = countBackwards(activeDays, today - 1);
        }

        long longestStreak = 0;
        long previousDay = Long.MIN_VALUE;
        long sequence = 0;

        for (Long day : activeDays) {

            if (previousDay != Long.MIN_VALUE
                    && day == previousDay + 1) {
                sequence++;
            } else {
                sequence = 1;
            }

            longestStreak = Math.max(longestStreak, sequence);
            previousDay = day;
        }

        currentStreakText.setText(
                "🔥 Текуща серия: " + currentStreak + " дни"
        );

        longestStreakText.setText(
                "🏆 Най-дълга серия: " + longestStreak + " дни"
        );
    }

    private long countBackwards(Set<Long> activeDays, long startDay) {

        long count = 0;
        long day = startDay;

        while (activeDays.contains(day)) {
            count++;
            day--;
        }

        return count;
    }

    private Long getDayKey(String dateString) {

        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            SimpleDateFormat format = new SimpleDateFormat(
                    "dd.MM.yyyy",
                    Locale.getDefault()
            );

            format.setLenient(false);

            Date date = format.parse(dateString);

            if (date == null) {
                return null;
            }

            Calendar local = Calendar.getInstance();
            local.setTime(date);

            Calendar utc = new GregorianCalendar(
                    TimeZone.getTimeZone("UTC")
            );

            utc.clear();

            utc.set(
                    local.get(Calendar.YEAR),
                    local.get(Calendar.MONTH),
                    local.get(Calendar.DAY_OF_MONTH)
            );

            return utc.getTimeInMillis() / 86400000L;

        } catch (ParseException e) {
            return null;
        }
    }

    private long getTodayKey() {

        Calendar today = Calendar.getInstance();

        Calendar utc = new GregorianCalendar(
                TimeZone.getTimeZone("UTC")
        );

        utc.clear();

        utc.set(
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
        );

        return utc.getTimeInMillis() / 86400000L;
    }

    private void updateWelcomeMessage() {

        android.content.SharedPreferences preferences =
                getSharedPreferences("sports_profile", MODE_PRIVATE);

        String name = preferences.getString("name", "").trim();

        TextView subtitleText = findViewById(R.id.subtitleText);

        if (name.isEmpty()) {
            subtitleText.setText("Добре дошъл в своя спортен дневник!");
        } else {
            subtitleText.setText("Здравей, " + name
                    + "! Радваме се, че тренираш! 💪");
        }

        String[] messages = {
                "💪 Всяка тренировка е крачка напред!",
                "🔥 Не се отказвай! Постоянството носи резултати!",
                "🏆 Най-доброто време за тренировка е сега!",
                "🚀 Малките стъпки водят до големи постижения!",
                "❤️ Тренирай за себе си и своето здраве!",
                "⭐ Вярвай в себе си и продължавай напред!",
                "🥇 Днес е чудесен ден да подобриш рекорда си!"
        };

        int day = java.util.Calendar.getInstance()
                .get(java.util.Calendar.DAY_OF_YEAR);

        int messageIndex = day % messages.length;

        motivationText.setText(messages[messageIndex]);
    }
}
