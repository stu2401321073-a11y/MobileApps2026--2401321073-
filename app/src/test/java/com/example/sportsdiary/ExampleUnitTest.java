package com.example.sportsdiary;

import com.example.sportsdiary.data.Workout;

import org.junit.Test;

import static org.junit.Assert.*;

public class ExampleUnitTest {

    @Test
    public void newWorkoutHasDefaultId() {
        Workout workout = new Workout();

        assertEquals(0, workout.id);
    }

    @Test
    public void workoutSportIsSavedCorrectly() {
        Workout workout = new Workout();
        workout.sport = "Бягане";

        assertEquals("Бягане", workout.sport);
    }

    @Test
    public void workoutDateIsSavedCorrectly() {
        Workout workout = new Workout();
        workout.date = "15.11.2026";

        assertEquals("15.11.2026", workout.date);
    }

    @Test
    public void workoutDurationIsSavedCorrectly() {
        Workout workout = new Workout();
        workout.duration = 45;

        assertEquals(45, workout.duration);
    }

    @Test
    public void workoutCaloriesAreSavedCorrectly() {
        Workout workout = new Workout();
        workout.calories = 250;

        assertEquals(250, workout.calories);
    }

    @Test
    public void workoutNoteIsSavedCorrectly() {
        Workout workout = new Workout();
        workout.note = "Сутрешна тренировка";

        assertEquals("Сутрешна тренировка", workout.note);
    }

    @Test
    public void workoutCanStoreAllFieldsTogether() {
        Workout workout = new Workout();

        workout.id = 1;
        workout.sport = "Плуване";
        workout.date = "15.11.2026";
        workout.duration = 60;
        workout.calories = 400;
        workout.note = "Тренировка в басейн";

        assertEquals(1, workout.id);
        assertEquals("Плуване", workout.sport);
        assertEquals("15.11.2026", workout.date);
        assertEquals(60, workout.duration);
        assertEquals(400, workout.calories);
        assertEquals("Тренировка в басейн", workout.note);
    }

    @Test
    public void twoWorkoutsAreIndependent() {
        Workout first = new Workout();
        Workout second = new Workout();

        first.sport = "Бягане";
        second.sport = "Колоездене";

        assertNotEquals(first.sport, second.sport);
    }

    @Test
    public void workoutNoteCanBeEmpty() {
        Workout workout = new Workout();
        workout.note = "";

        assertEquals("", workout.note);
    }

    @Test
    public void workoutCanHaveZeroCalories() {
        Workout workout = new Workout();
        workout.calories = 0;

        assertEquals(0, workout.calories);
    }
}
