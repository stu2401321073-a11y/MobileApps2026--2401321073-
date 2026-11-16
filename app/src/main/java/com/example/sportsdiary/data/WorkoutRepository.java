package com.example.sportsdiary.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.room.Room;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkoutRepository {

    private static final String TAG = "WorkoutRepository";

    private final WorkoutDao workoutDao;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public WorkoutRepository(Context context) {
        this(
                Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "sports_diary_database"
                ).build().workoutDao(),
                Executors.newSingleThreadExecutor(),
                new Handler(Looper.getMainLooper())
        );
    }

    // Конструктор за изолирано тестване
   public WorkoutRepository(
            WorkoutDao workoutDao,
            ExecutorService executor,
            Handler mainHandler
    ) {
        this.workoutDao = workoutDao;
        this.executor = executor;
        this.mainHandler = mainHandler;
    }

    // Зареждане на всички тренировки
    public void getAllWorkouts(Callback<List<Workout>> callback) {
        if (callback == null) return;

        executor.execute(() -> {
            try {
                List<Workout> result = workoutDao.getAllWorkouts();

                mainHandler.post(() ->
                        callback.onComplete(result));

            } catch (Exception e) {
                Log.e(TAG, "Error loading workouts", e);

                mainHandler.post(() ->
                        callback.onError(e));
            }
        });
    }

    // Добавяне на тренировка
    public void insert(Workout workout, Runnable onComplete) {
        insert(workout, new OperationCallback() {
            @Override
            public void onSuccess() {
                if (onComplete != null) {
                    onComplete.run();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error inserting workout", e);
            }
        });
    }

    public void insert(Workout workout, OperationCallback callback) {
        executeWrite(
                "Error inserting workout",
                () -> workoutDao.insert(workout),
                callback
        );
    }

    // Редактиране на тренировка
    public void update(Workout workout, Runnable onComplete) {
        update(workout, new OperationCallback() {
            @Override
            public void onSuccess() {
                if (onComplete != null) {
                    onComplete.run();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error updating workout", e);
            }
        });
    }

    public void update(Workout workout, OperationCallback callback) {
        executeWrite(
                "Error updating workout",
                () -> workoutDao.update(workout),
                callback
        );
    }

    // Изтриване на тренировка
    public void delete(Workout workout, Runnable onComplete) {
        delete(workout, new OperationCallback() {
            @Override
            public void onSuccess() {
                if (onComplete != null) {
                    onComplete.run();
                }
            }

            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Error deleting workout", e);
            }
        });
    }

    public void delete(Workout workout, OperationCallback callback) {
        executeWrite(
                "Error deleting workout",
                () -> workoutDao.delete(workout),
                callback
        );
    }

    // Общ метод за операциите по запис
    private void executeWrite(
            String errorMessage,
            Runnable databaseOperation,
            OperationCallback callback
    ) {
        executor.execute(() -> {
            try {
                databaseOperation.run();

                if (callback != null) {
                    mainHandler.post(callback::onSuccess);
                }

            } catch (Exception e) {
                Log.e(TAG, errorMessage, e);

                if (callback != null) {
                    mainHandler.post(() -> callback.onError(e));
                }
            }
        });
    }

    // Зареждане на тренировка по ID
    public void getWorkoutById(int id, Callback<Workout> callback) {
        if (callback == null) return;

        executor.execute(() -> {
            try {
                Workout result = workoutDao.getWorkoutById(id);

                mainHandler.post(() ->
                        callback.onComplete(result));

            } catch (Exception e) {
                Log.e(TAG, "Error loading workout by ID", e);

                mainHandler.post(() ->
                        callback.onError(e));
            }
        });
    }

    // Callback за резултати от заявки
    public interface Callback<T> {
        void onComplete(T result);

        default void onError(Exception e) {
            Log.e(TAG, "Database operation failed", e);
        }
    }

    // Callback за добавяне, редактиране и изтриване
    public interface OperationCallback {
        void onSuccess();

        void onError(Exception e);
    }
}
