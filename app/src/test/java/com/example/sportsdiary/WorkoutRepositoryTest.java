package com.example.sportsdiary;

import android.os.Handler;

import com.example.sportsdiary.data.Workout;
import com.example.sportsdiary.data.WorkoutDao;
import com.example.sportsdiary.data.WorkoutRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class WorkoutRepositoryTest {

    @Mock
    private WorkoutDao workoutDao;

    @Mock
    private ExecutorService executor;

    @Mock
    private Handler mainHandler;

    private WorkoutRepository repository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        repository = new WorkoutRepository(
                workoutDao,
                executor,
                mainHandler
        );

        // Изпълнява веднага подадените задачи.
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(executor).execute(any(Runnable.class));

        // Изпълнява веднага callback функциите.
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return true;
        }).when(mainHandler).post(any(Runnable.class));
    }

    private Workout createWorkout() {
        Workout workout = new Workout();
        workout.sport = "Бягане";
        workout.date = "16.11.2026";
        workout.duration = 45;
        workout.calories = 350;
        workout.note = "Тренировка";
        return workout;
    }

    @Test
    public void getAllWorkoutsReturnsList() {
        List<Workout> workouts =
                Collections.singletonList(createWorkout());

        when(workoutDao.getAllWorkouts()).thenReturn(workouts);

        final List<Workout>[] result = new List[1];

        repository.getAllWorkouts(new WorkoutRepository.Callback<List<Workout>>() {
            @Override
            public void onComplete(List<Workout> value) {
                result[0] = value;
            }
        });

        assertNotNull(result[0]);
        assertEquals(1, result[0].size());
        verify(workoutDao).getAllWorkouts();
    }

    @Test
    public void getAllWorkoutsReturnsEmptyList() {
        when(workoutDao.getAllWorkouts())
                .thenReturn(Collections.emptyList());

        final List<Workout>[] result = new List[1];

        repository.getAllWorkouts(new WorkoutRepository.Callback<List<Workout>>() {
            @Override
            public void onComplete(List<Workout> value) {
                result[0] = value;
            }
        });

        assertNotNull(result[0]);
        assertTrue(result[0].isEmpty());
    }

    @Test
    public void getAllWorkoutsWithNullCallbackDoesNothing() {
        repository.getAllWorkouts(null);

        verify(executor, never()).execute(any(Runnable.class));
        verify(workoutDao, never()).getAllWorkouts();
    }

    @Test
    public void getAllWorkoutsHandlesDatabaseError() {
        RuntimeException error = new RuntimeException("Database error");

        when(workoutDao.getAllWorkouts()).thenThrow(error);

        final Exception[] receivedError = new Exception[1];

        repository.getAllWorkouts(new WorkoutRepository.Callback<List<Workout>>() {
            @Override
            public void onComplete(List<Workout> value) {
            }

            @Override
            public void onError(Exception e) {
                receivedError[0] = e;
            }
        });

        assertEquals(error, receivedError[0]);
    }

    @Test
    public void getWorkoutByIdReturnsWorkout() {
        Workout workout = createWorkout();

        when(workoutDao.getWorkoutById(1)).thenReturn(workout);

        final Workout[] result = new Workout[1];

        repository.getWorkoutById(1, new WorkoutRepository.Callback<Workout>() {
            @Override
            public void onComplete(Workout value) {
                result[0] = value;
            }
        });

        assertNotNull(result[0]);
        assertEquals("Бягане", result[0].sport);
        verify(workoutDao).getWorkoutById(1);
    }

    @Test
    public void getWorkoutByIdReturnsNullWhenNotFound() {
        when(workoutDao.getWorkoutById(99)).thenReturn(null);

        final Workout[] result = new Workout[1];

        repository.getWorkoutById(99, new WorkoutRepository.Callback<Workout>() {
            @Override
            public void onComplete(Workout value) {
                result[0] = value;
            }
        });

        assertNull(result[0]);
    }

    @Test
    public void getWorkoutByIdWithNullCallbackDoesNothing() {
        repository.getWorkoutById(1, null);

        verify(executor, never()).execute(any(Runnable.class));
        verify(workoutDao, never()).getWorkoutById(1);
    }

    @Test
    public void getWorkoutByIdHandlesDatabaseError() {
        RuntimeException error = new RuntimeException("Read error");

        when(workoutDao.getWorkoutById(1)).thenThrow(error);

        final Exception[] receivedError = new Exception[1];

        repository.getWorkoutById(1, new WorkoutRepository.Callback<Workout>() {
            @Override
            public void onComplete(Workout value) {
            }

            @Override
            public void onError(Exception e) {
                receivedError[0] = e;
            }
        });

        assertEquals(error, receivedError[0]);
    }

    @Test
    public void insertCallsDaoAndSuccessCallback() {
        Workout workout = createWorkout();
        final boolean[] success = {false};

        repository.insert(workout, new WorkoutRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                success[0] = true;
            }

            @Override
            public void onError(Exception e) {
            }
        });

        verify(workoutDao).insert(workout);
        assertTrue(success[0]);
    }

    @Test
    public void insertHandlesDatabaseError() {
        Workout workout = createWorkout();
        RuntimeException error = new RuntimeException("Insert error");

        org.mockito.Mockito.doThrow(error)
                .when(workoutDao).insert(workout);

        final Exception[] receivedError = new Exception[1];

        repository.insert(workout, new WorkoutRepository.OperationCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(Exception e) {
                receivedError[0] = e;
            }
        });

        assertEquals(error, receivedError[0]);
    }

    @Test
    public void updateCallsDaoAndSuccessCallback() {
        Workout workout = createWorkout();
        final boolean[] success = {false};

        repository.update(workout, new WorkoutRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                success[0] = true;
            }

            @Override
            public void onError(Exception e) {
            }
        });

        verify(workoutDao).update(workout);
        assertTrue(success[0]);
    }

    @Test
    public void updateHandlesDatabaseError() {
        Workout workout = createWorkout();
        RuntimeException error = new RuntimeException("Update error");

        org.mockito.Mockito.doThrow(error)
                .when(workoutDao).update(workout);

        final Exception[] receivedError = new Exception[1];

        repository.update(workout, new WorkoutRepository.OperationCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(Exception e) {
                receivedError[0] = e;
            }
        });

        assertEquals(error, receivedError[0]);
    }

    @Test
    public void deleteCallsDaoAndSuccessCallback() {
        Workout workout = createWorkout();
        final boolean[] success = {false};

        repository.delete(workout, new WorkoutRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                success[0] = true;
            }

            @Override
            public void onError(Exception e) {
            }
        });

        verify(workoutDao).delete(workout);
        assertTrue(success[0]);
    }

    @Test
    public void deleteHandlesDatabaseError() {
        Workout workout = createWorkout();
        RuntimeException error = new RuntimeException("Delete error");

        org.mockito.Mockito.doThrow(error)
                .when(workoutDao).delete(workout);

        final Exception[] receivedError = new Exception[1];

        repository.delete(workout, new WorkoutRepository.OperationCallback() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(Exception e) {
                receivedError[0] = e;
            }
        });

        assertEquals(error, receivedError[0]);
    }

    @Test
    public void insertWithRunnableCallsRunnable() {
        Workout workout = createWorkout();
        final boolean[] completed = {false};

        repository.insert(workout, () -> completed[0] = true);

        assertTrue(completed[0]);
        verify(workoutDao).insert(workout);
    }

    @Test
    public void updateWithRunnableCallsRunnable() {
        Workout workout = createWorkout();
        final boolean[] completed = {false};

        repository.update(workout, () -> completed[0] = true);

        assertTrue(completed[0]);
        verify(workoutDao).update(workout);
    }

    @Test
    public void deleteWithRunnableCallsRunnable() {
        Workout workout = createWorkout();
        final boolean[] completed = {false};

        repository.delete(workout, () -> completed[0] = true);

        assertTrue(completed[0]);
        verify(workoutDao).delete(workout);
    }

    @Test
    public void insertWithNullRunnableStillInserts() {
        Workout workout = createWorkout();

        repository.insert(workout, (Runnable) null);

        verify(workoutDao).insert(workout);
    }

    @Test
    public void updateWithNullCallbackStillUpdates() {
        Workout workout = createWorkout();

        repository.update(workout, (WorkoutRepository.OperationCallback) null);

        verify(workoutDao).update(workout);
    }

    @Test
    public void deleteWithNullCallbackStillDeletes() {
        Workout workout = createWorkout();

        repository.delete(workout, (WorkoutRepository.OperationCallback) null);

        verify(workoutDao).delete(workout);
    }

    @Test
    public void multipleWorkoutsCanBeLoaded() {
        Workout first = createWorkout();

        Workout second = createWorkout();
        second.sport = "Фитнес";

        List<Workout> workouts = Arrays.asList(first, second);

        when(workoutDao.getAllWorkouts()).thenReturn(workouts);

        final List<Workout>[] result = new List[1];

        repository.getAllWorkouts(new WorkoutRepository.Callback<List<Workout>>() {
            @Override
            public void onComplete(List<Workout> value) {
                result[0] = value;
            }
        });

        assertEquals(2, result[0].size());
        assertEquals("Фитнес", result[0].get(1).sport);
    }
}
