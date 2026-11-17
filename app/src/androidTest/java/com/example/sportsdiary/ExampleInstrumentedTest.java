package com.example.sportsdiary;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ActivityScenario;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.example.sportsdiary.ui.MainActivity;
import com.example.sportsdiary.R;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() {
        String packageName =
                androidx.test.platform.app.InstrumentationRegistry
                        .getInstrumentation()
                        .getTargetContext()
                        .getPackageName();

        assertEquals("com.example.sportsdiary", packageName);
    }

    @Test
    public void mainActivityLaunchesSuccessfully() {
        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {

            scenario.onActivity(activity ->
                    assertNotNull(activity)
            );
        }
    }

    @Test
    public void mainScreenViewsAreDisplayed() {
        try (ActivityScenario<MainActivity> scenario =
                     ActivityScenario.launch(MainActivity.class)) {

            scenario.onActivity(activity -> {
                assertNotNull(
                        activity.findViewById(R.id.workoutsContainer)
                );

                assertNotNull(
                        activity.findViewById(R.id.emptyText)
                );

                assertNotNull(
                        activity.findViewById(R.id.addWorkoutButton)
                );

                assertNotNull(
                        activity.findViewById(R.id.totalWorkoutsText)
                );

                assertNotNull(
                        activity.findViewById(R.id.totalDurationText)
                );

                assertNotNull(
                        activity.findViewById(R.id.totalCaloriesText)
                );
            });
        }
    }
}
