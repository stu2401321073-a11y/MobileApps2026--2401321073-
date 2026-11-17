package com.example.sportsdiary.ui;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.sportsdiary.R;
import com.example.sportsdiary.SportsDiaryApplication;
import com.example.sportsdiary.data.Workout;
import com.example.sportsdiary.viewmodel.WorkoutViewModel;
import com.example.sportsdiary.viewmodel.WorkoutViewModelFactory;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {

    private TextView totalWorkouts;
    private TextView totalDuration;
    private TextView totalCalories;

    private TextView monthlyReportTitle;
    private TextView monthlyWorkouts;
    private TextView monthlyDuration;
    private TextView monthlyCalories;
    private TextView previousMonthTitle;
    private TextView previousMonthWorkouts;
    private TextView monthlyComparison;

    private TextView recordLongestWorkout;
    private TextView recordMostCalories;
    private TextView recordFavoriteSport;
    private TextView recordBestWeek;

    private BarChart barChart;

    private final SimpleDateFormat fullDateFormat =
            new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        totalWorkouts = findViewById(R.id.statisticsTotalWorkouts);
        totalDuration = findViewById(R.id.statisticsTotalDuration);
        totalCalories = findViewById(R.id.statisticsTotalCalories);

        recordLongestWorkout = findViewById(R.id.recordLongestWorkout);
        recordMostCalories = findViewById(R.id.recordMostCalories);
        recordFavoriteSport = findViewById(R.id.recordFavoriteSport);
        recordBestWeek = findViewById(R.id.recordBestWeek);

        barChart = findViewById(R.id.workoutBarChart);

        monthlyReportTitle = findViewById(R.id.monthlyReportTitle);
        monthlyWorkouts = findViewById(R.id.monthlyWorkouts);
        monthlyDuration = findViewById(R.id.monthlyDuration);
        monthlyCalories = findViewById(R.id.monthlyCalories);
        previousMonthTitle = findViewById(R.id.previousMonthTitle);
        previousMonthWorkouts = findViewById(R.id.previousMonthWorkouts);
        monthlyComparison = findViewById(R.id.monthlyComparison);

        SportsDiaryApplication app =
                (SportsDiaryApplication) getApplication();

        WorkoutViewModelFactory factory =
                new WorkoutViewModelFactory(
                        app.getAppContainer().workoutRepository);

        WorkoutViewModel viewModel =
                new ViewModelProvider(this, factory)
                        .get(WorkoutViewModel.class);

        viewModel.getWorkouts().observe(this, this::updateStatistics);
        viewModel.loadWorkouts();
    }

    private void updateStatistics(List<Workout> workouts) {

        if (workouts == null) return;

        int duration = 0;
        int calories = 0;

        int longestWorkout = 0;
        int mostCalories = 0;

        Map<String, Integer> sportCounts = new HashMap<>();
        Map<String, Integer> weeklyCounts = new HashMap<>();

        for (Workout workout : workouts) {

            duration += workout.duration;
            calories += workout.calories;

            longestWorkout = Math.max(longestWorkout, workout.duration);
            mostCalories = Math.max(mostCalories, workout.calories);

            if (workout.sport != null && !workout.sport.trim().isEmpty()) {
                String sport = workout.sport.trim();
                sportCounts.put(sport,
                        sportCounts.getOrDefault(sport, 0) + 1);
            }

            Calendar workoutCalendar = getWorkoutCalendar(workout.date);

            if (workoutCalendar != null) {
                int year = workoutCalendar.get(Calendar.YEAR);
                int week = workoutCalendar.get(Calendar.WEEK_OF_YEAR);

                String weekKey = year + "-" + week;

                weeklyCounts.put(weekKey,
                        weeklyCounts.getOrDefault(weekKey, 0) + 1);
            }
        }

        totalWorkouts.setText("Общо тренировки: " + workouts.size());
        totalDuration.setText("Общо минути: " + duration);
        totalCalories.setText("Изгорени калории: " + calories);

        updatePersonalRecords(
                workouts.size(),
                longestWorkout,
                mostCalories,
                sportCounts,
                weeklyCounts
        );

        updateMonthlyReport(workouts);
        updateChart(workouts);
    }

    private void updateMonthlyReport(List<Workout> workouts) {

        Calendar currentMonth = Calendar.getInstance();

        Calendar previousMonth = (Calendar) currentMonth.clone();
        previousMonth.add(Calendar.MONTH, -1);

        int currentCount = 0;
        int currentDuration = 0;
        int currentCalories = 0;

        int previousCount = 0;

        for (Workout workout : workouts) {

            Calendar workoutDate = getWorkoutCalendar(workout.date);

            if (workoutDate == null) {
                continue;
            }

            int workoutYear = workoutDate.get(Calendar.YEAR);
            int workoutMonth = workoutDate.get(Calendar.MONTH);

            if (workoutYear == currentMonth.get(Calendar.YEAR)
                    && workoutMonth == currentMonth.get(Calendar.MONTH)) {

                currentCount++;
                currentDuration += workout.duration;
                currentCalories += workout.calories;

            } else if (workoutYear == previousMonth.get(Calendar.YEAR)
                    && workoutMonth == previousMonth.get(Calendar.MONTH)) {

                previousCount++;
            }
        }

        SimpleDateFormat monthFormat =
                new SimpleDateFormat("MMMM yyyy", new Locale("bg", "BG"));

        String currentMonthName =
                monthFormat.format(currentMonth.getTime());

        String previousMonthName =
                monthFormat.format(previousMonth.getTime());

        monthlyReportTitle.setText(
                "Текущ месец: " + currentMonthName
        );

        monthlyWorkouts.setText(
                "🏋️ Тренировки: " + currentCount
        );

        monthlyDuration.setText(
                "⏱ Общо минути: " + currentDuration
        );

        monthlyCalories.setText(
                "🔥 Изгорени калории: " + currentCalories
        );

        previousMonthTitle.setText(
                "Предходен месец: " + previousMonthName
        );

        previousMonthWorkouts.setText(
                "Тренировки: " + previousCount
        );

        int difference = currentCount - previousCount;

        if (difference > 0) {

            monthlyComparison.setText(
                    "📈 С " + difference +
                            " тренировки повече от предходния месец!"
            );

        } else if (difference < 0) {

            monthlyComparison.setText(
                    "📉 С " + Math.abs(difference) +
                            " тренировки по-малко от предходния месец."
            );

        } else {

            monthlyComparison.setText(
                    "➡️ Същият брой тренировки като предходния месец."
            );
        }
    }

    private void updatePersonalRecords(
            int workoutCount,
            int longestWorkout,
            int mostCalories,
            Map<String, Integer> sportCounts,
            Map<String, Integer> weeklyCounts) {

        recordLongestWorkout.setText(
                "⏱ Най-дълга тренировка: " +
                        (workoutCount == 0 ? 0 : longestWorkout) + " мин."
        );

        recordMostCalories.setText(
                "🔥 Най-много калории в тренировка: " +
                        (workoutCount == 0 ? 0 : mostCalories) + " kcal"
        );

        String favoriteSport = "Няма данни";
        int highestSportCount = 0;

        for (Map.Entry<String, Integer> entry : sportCounts.entrySet()) {
            if (entry.getValue() > highestSportCount) {
                highestSportCount = entry.getValue();
                favoriteSport = entry.getKey();
            }
        }

        recordFavoriteSport.setText(
                "🏃 Най-често практикуван спорт: " + favoriteSport
        );

        int bestWeek = 0;

        for (int count : weeklyCounts.values()) {
            bestWeek = Math.max(bestWeek, count);
        }

        recordBestWeek.setText(
                "📅 Най-активна седмица: " + bestWeek + " тренировки"
        );
    }

    private Calendar getWorkoutCalendar(String dateString) {

        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }

        try {
            Date date = fullDateFormat.parse(dateString);

            if (date == null) return null;

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            return calendar;

        } catch (ParseException e) {
            return null;
        }
    }

    private void updateChart(List<Workout> workouts) {

        Calendar calendar = Calendar.getInstance();

        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();

        SimpleDateFormat dayFormat =
                new SimpleDateFormat("EEE", new Locale("bg", "BG"));

        for (int i = 6; i >= 0; i--) {

            Calendar day = (Calendar) calendar.clone();
            day.add(Calendar.DAY_OF_YEAR, -i);

            String dayDate = fullDateFormat.format(day.getTime());
            String dayName = dayFormat.format(day.getTime());

            int dailyDuration = 0;

            for (Workout workout : workouts) {

                if (workout.date == null) continue;

                if (workout.date.equals(dayDate)) {
                    dailyDuration += workout.duration;
                }
            }

            entries.add(new BarEntry(6 - i, dailyDuration));
            labels.add(dayName);
        }

        BarDataSet dataSet =
                new BarDataSet(entries, "Тренировки (минути)");

        dataSet.setColor(0xFF3F51B5);
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(0xFF333333);
        dataSet.setHighlightEnabled(false);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        barChart.setData(data);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(12f);

        barChart.getAxisRight().setEnabled(false);
        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setGranularity(1f);

        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(true);
        barChart.getLegend().setTextSize(12f);

        barChart.setFitBars(true);
        barChart.setDrawGridBackground(false);
        barChart.setNoDataText("Все още няма данни за тренировките");
        barChart.setExtraBottomOffset(8f);

        barChart.animateY(800);
        barChart.invalidate();
    }
}
