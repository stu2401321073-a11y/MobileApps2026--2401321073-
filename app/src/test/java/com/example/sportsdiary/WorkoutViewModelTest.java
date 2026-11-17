package com.example.sportsdiary;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.Observer;

import com.example.sportsdiary.data.Workout;
import com.example.sportsdiary.data.WorkoutRepository;
import com.example.sportsdiary.viewmodel.WorkoutViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class WorkoutViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule =
            new InstantTaskExecutorRule();

    private WorkoutRepository repository;
    private WorkoutViewModel viewModel;

    @Before
    public void setUp() {
        repository = mock(WorkoutRepository.class);
        viewModel = new WorkoutViewModel(repository);
    }

    @Test
    public void loadWorkoutsCalculatesTotalCount() {
        List<Workout> workouts = new ArrayList<>();
        workouts.add(createWorkout(30, 200));
        workouts.add(createWorkout(45, 300));

        doAnswer(invocation -> {
            WorkoutRepository.Callback<List<Workout>> callback =
                    invocation.getArgument(0);
            callback.onComplete(workouts);
            return null;
        }).when(repository).getAllWorkouts(any());

        viewModel.loadWorkouts();

        assertEquals(Integer.valueOf(2),
                viewModel.getTotalWorkouts().getValue());
    }

    @Test
    public void loadWorkoutsCalculatesTotalDuration() {
        List<Workout> workouts = new ArrayList<>();
        workouts.add(createWorkout(30, 200));
        workouts.add(createWorkout(45, 300));

        mockWorkoutList(workouts);
        viewModel.loadWorkouts();

        assertEquals(Integer.valueOf(75),
                viewModel.getTotalDuration().getValue());
    }

    @Test
    public void loadWorkoutsCalculatesTotalCalories() {
        List<Workout> workouts = new ArrayList<>();
        workouts.add(createWorkout(30, 200));
        workouts.add(createWorkout(45, 300));

        mockWorkoutList(workouts);
        viewModel.loadWorkouts();

        assertEquals(Integer.valueOf(500),
                viewModel.getTotalCalories().getValue());
    }

    @Test
    public void loadWorkoutsWithEmptyListReturnsZeroStatistics() {
        mockWorkoutList(new ArrayList<>());

        viewModel.loadWorkouts();

        assertEquals(Integer.valueOf(0),
                viewModel.getTotalWorkouts().getValue());
        assertEquals(Integer.valueOf(0),
                viewModel.getTotalDuration().getValue());
        assertEquals(Integer.valueOf(0),
                viewModel.getTotalCalories().getValue());
    }

    @Test
    public void loadWorkoutsWithNullListReturnsZeroStatistics() {
        mockWorkoutList(null);

        viewModel.loadWorkouts();

        assertEquals(Integer.valueOf(0),
                viewModel.getTotalWorkouts().getValue());
        assertEquals(Integer.valueOf(0),
                viewModel.getTotalDuration().getValue());
        assertEquals(Integer.valueOf(0),
                viewModel.getTotalCalories().getValue());
    }

    @Test
    public void loadWorkoutsPublishesWorkoutList() {
        List<Workout> workouts = new ArrayList<>();
        workouts.add(createWorkout(20, 150));

        mockWorkoutList(workouts);
        viewModel.loadWorkouts();

        assertEquals(workouts, viewModel.getWorkouts().getValue());
    }

    @Test
    public void loadWorkoutsHandlesRepositoryError() {
        doAnswer(invocation -> {
            WorkoutRepository.Callback<List<Workout>> callback =
                    invocation.getArgument(0);
            callback.onError(new Exception("Database error"));
            return null;
        }).when(repository).getAllWorkouts(any());

        viewModel.loadWorkouts();

        assertEquals(
                "Грешка при зареждане на тренировките.",
                viewModel.getErrorMessage().getValue()
        );
    }

    @Test
    public void clearErrorRemovesErrorMessage() {
        viewModel.clearError();

        assertNull(viewModel.getErrorMessage().getValue());
    }

    @Test
    public void insertCallsRepository() {
        Workout workout = createWorkout(30, 200);

        viewModel.insert(workout, null);

        verify(repository).insert(eq(workout),
                any(WorkoutRepository.OperationCallback.class));
    }

    @Test
    public void updateCallsRepository() {
        Workout workout = createWorkout(40, 250);

        viewModel.update(workout);

        verify(repository).update(eq(workout),
                any(WorkoutRepository.OperationCallback.class));
    }

    @Test
    public void deleteCallsRepository() {
        Workout workout = createWorkout(40, 250);

        viewModel.delete(workout);

        verify(repository).delete(eq(workout),
                any(WorkoutRepository.OperationCallback.class));
    }

    @Test
    public void insertSuccessCallsCallback() {
        Workout workout = createWorkout(30, 200);
        Runnable callback = mock(Runnable.class);

        doAnswer(invocation -> {
            WorkoutRepository.OperationCallback operation =
                    invocation.getArgument(1);
            operation.onSuccess();
            return null;
        }).when(repository).insert(
                eq(workout),
                any(WorkoutRepository.OperationCallback.class)
        );

        mockWorkoutList(new ArrayList<>());
        viewModel.insert(workout, callback);

        verify(callback).run();
    }

    @Test
    public void updateSuccessCallsCallback() {
        Workout workout = createWorkout(30, 200);
        Runnable callback = mock(Runnable.class);

        doAnswer(invocation -> {
            WorkoutRepository.OperationCallback operation =
                    invocation.getArgument(1);
            operation.onSuccess();
            return null;
        }).when(repository).update(
                eq(workout),
                any(WorkoutRepository.OperationCallback.class)
        );

        mockWorkoutList(new ArrayList<>());
        viewModel.update(workout, callback);

        verify(callback).run();
    }

    @Test
    public void deleteSuccessCallsCallback() {
        Workout workout = createWorkout(30, 200);
        Runnable callback = mock(Runnable.class);

        doAnswer(invocation -> {
            WorkoutRepository.OperationCallback operation =
                    invocation.getArgument(1);
            operation.onSuccess();
            return null;
        }).when(repository).delete(
                eq(workout),
                any(WorkoutRepository.OperationCallback.class)
        );

        mockWorkoutList(new ArrayList<>());
        viewModel.delete(workout, callback);

        verify(callback).run();
    }

    @Test
    public void insertErrorSetsErrorMessage() {
        Workout workout = createWorkout(30, 200);

        doAnswer(invocation -> {
            WorkoutRepository.OperationCallback operation =
                    invocation.getArgument(1);
            operation.onError(new Exception("Insert error"));
            return null;
        }).when(repository).insert(
                eq(workout),
                any(WorkoutRepository.OperationCallback.class)
        );

        viewModel.insert(workout, null);

        assertEquals(
                "Грешка при добавяне на тренировка.",
                viewModel.getErrorMessage().getValue()
        );
    }

    @Test
    public void updateErrorSetsErrorMessage() {
        Workout workout = createWorkout(30, 200);

        doAnswer(invocation -> {
            WorkoutRepository.OperationCallback operation =
                    invocation.getArgument(1);
            operation.onError(new Exception("Update error"));
            return null;
        }).when(repository).update(
                eq(workout),
                any(WorkoutRepository.OperationCallback.class)
        );

        viewModel.update(workout, null);

        assertEquals(
                "Грешка при редактиране на тренировка.",
                viewModel.getErrorMessage().getValue()
        );
    }

    @Test
    public void deleteErrorSetsErrorMessage() {
        Workout workout = createWorkout(30, 200);

        doAnswer(invocation -> {
            WorkoutRepository.OperationCallback operation =
                    invocation.getArgument(1);
            operation.onError(new Exception("Delete error"));
            return null;
        }).when(repository).delete(
                eq(workout),
                any(WorkoutRepository.OperationCallback.class)
        );

        viewModel.delete(workout, null);

        assertEquals(
                "Грешка при изтриване на тренировка.",
                viewModel.getErrorMessage().getValue()
        );
    }

    private Workout createWorkout(int duration, int calories) {
        Workout workout = new Workout();
        workout.sport = "Бягане";
        workout.date = "15.11.2026";
        workout.duration = duration;
        workout.calories = calories;
        workout.note = "Тест";
        return workout;
    }

    private void mockWorkoutList(List<Workout> workouts) {
        doAnswer(invocation -> {
            WorkoutRepository.Callback<List<Workout>> callback =
                    invocation.getArgument(0);
            callback.onComplete(workouts);
            return null;
        }).when(repository).getAllWorkouts(any());
    }
}
