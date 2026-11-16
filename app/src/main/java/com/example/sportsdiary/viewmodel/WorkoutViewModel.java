package com.example.sportsdiary.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sportsdiary.data.Workout;
import com.example.sportsdiary.data.WorkoutRepository;

import java.util.List;

public class WorkoutViewModel extends ViewModel {

    private final WorkoutRepository repository;

    private final MutableLiveData<List<Workout>> workouts =
            new MutableLiveData<>();

    private final MutableLiveData<Integer> totalWorkouts =
            new MutableLiveData<>(0);

    private final MutableLiveData<Integer> totalDuration =
            new MutableLiveData<>(0);

    private final MutableLiveData<Integer> totalCalories =
            new MutableLiveData<>(0);

    private final MutableLiveData<String> errorMessage =
            new MutableLiveData<>();

    public WorkoutViewModel(WorkoutRepository repository) {
        this.repository = repository;
    }

    public LiveData<List<Workout>> getWorkouts() {
        return workouts;
    }

    public LiveData<Integer> getTotalWorkouts() {
        return totalWorkouts;
    }

    public LiveData<Integer> getTotalDuration() {
        return totalDuration;
    }

    public LiveData<Integer> getTotalCalories() {
        return totalCalories;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void clearError() {
        errorMessage.setValue(null);
    }

    // Зареждане на тренировките
    public void loadWorkouts() {
        repository.getAllWorkouts(
                new WorkoutRepository.Callback<List<Workout>>() {

                    @Override
                    public void onComplete(List<Workout> result) {
                        workouts.setValue(result);
                        calculateStatistics(result);
                    }

                    @Override
                    public void onError(Exception e) {
                        errorMessage.setValue(
                                "Грешка при зареждане на тренировките."
                        );
                    }
                }
        );
    }

    // Еднократно зареждане на тренировките за проверка на рекорди
    public void getAllWorkouts(
            WorkoutRepository.Callback<List<Workout>> callback) {

        repository.getAllWorkouts(
                new WorkoutRepository.Callback<List<Workout>>() {
                    @Override
                    public void onComplete(List<Workout> result) {
                        if (callback != null) {
                            callback.onComplete(result);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        errorMessage.setValue(
                                "Грешка при проверка на личните рекорди."
                        );

                        if (callback != null) {
                            callback.onError(e);
                        }
                    }
                }
        );
    }

    // Изчисляване на статистиката
    private void calculateStatistics(List<Workout> list) {
        int count = 0;
        int duration = 0;
        int calories = 0;

        if (list != null) {
            count = list.size();

            for (Workout workout : list) {
                duration += workout.duration;
                calories += workout.calories;
            }
        }

        totalWorkouts.setValue(count);
        totalDuration.setValue(duration);
        totalCalories.setValue(calories);
    }

    // Добавяне
    public void insert(Workout workout, Runnable onComplete) {
        repository.insert(workout,
                new WorkoutRepository.OperationCallback() {

                    @Override
                    public void onSuccess() {
                        loadWorkouts();

                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        errorMessage.setValue(
                                "Грешка при добавяне на тренировка."
                        );
                    }
                });
    }

    // Редактиране - старият метод се запазва
    public void update(Workout workout) {
        update(workout, null);
    }

    // Редактиране с потвърждение за успех
    public void update(Workout workout, Runnable onComplete) {
        repository.update(workout,
                new WorkoutRepository.OperationCallback() {

                    @Override
                    public void onSuccess() {
                        loadWorkouts();

                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        errorMessage.setValue(
                                "Грешка при редактиране на тренировка."
                        );
                    }
                });
    }

    // Изтриване - старият метод се запазва
    public void delete(Workout workout) {
        delete(workout, null);
    }

    // Изтриване с потвърждение за успех
    public void delete(Workout workout, Runnable onComplete) {
        repository.delete(workout,
                new WorkoutRepository.OperationCallback() {

                    @Override
                    public void onSuccess() {
                        loadWorkouts();

                        if (onComplete != null) {
                            onComplete.run();
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        errorMessage.setValue(
                                "Грешка при изтриване на тренировка."
                        );
                    }
                });
    }

    // Зареждане по ID
    public void getWorkoutById(
            int id,
            WorkoutRepository.Callback<Workout> callback) {

        repository.getWorkoutById(
                id,
                new WorkoutRepository.Callback<Workout>() {

                    @Override
                    public void onComplete(Workout result) {
                        if (callback != null) {
                            callback.onComplete(result);
                        }
                    }

                    @Override
                    public void onError(Exception e) {
                        errorMessage.setValue(
                                "Грешка при зареждане на тренировката."
                        );

                        if (callback != null) {
                            callback.onError(e);
                        }
                    }
                }
        );
    }
}
