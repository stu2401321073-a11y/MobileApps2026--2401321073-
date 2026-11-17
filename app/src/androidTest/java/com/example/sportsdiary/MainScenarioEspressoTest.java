package com.example.sportsdiary;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import com.example.sportsdiary.ui.MainActivity;

@RunWith(AndroidJUnit4.class)
public class MainScenarioEspressoTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void userCanAddWorkout() {

        // 1. Натиска бутона за добавяне на тренировка
        onView(withId(R.id.addWorkoutButton))
                .perform(scrollTo(), click());

        // 2. Проверява дали екранът за добавяне е отворен
        onView(withId(R.id.saveWorkoutButton))
                .check(matches(isDisplayed()));

        // 3. Въвежда данни за тренировката
        onView(withId(R.id.sportEditText))
                .perform(replaceText("Running"), closeSoftKeyboard());

        onView(withId(R.id.dateEditText))
                .perform(replaceText("16.11.2026"), closeSoftKeyboard());

        onView(withId(R.id.durationEditText))
                .perform(replaceText("45"), closeSoftKeyboard());

        onView(withId(R.id.caloriesEditText))
                .perform(replaceText("350"), closeSoftKeyboard());

        onView(withId(R.id.noteEditText))
                .perform(replaceText("Espresso test"), closeSoftKeyboard());

        // 4. Запазва тренировката
        onView(withId(R.id.saveWorkoutButton))
                .perform(click());

        // 5. Проверява дали приложението се е върнало на главния екран
        onView(withId(R.id.addWorkoutButton))
                .check(matches(isDisplayed()));
    }
}
