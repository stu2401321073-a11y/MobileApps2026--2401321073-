package com.example.sportsdiary.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import java.util.List;

@Dao
public interface WorkoutDao {

    @Insert
    void insert(Workout workout);

    @Update
    void update(Workout workout);

    @Delete
    void delete(Workout workout);

    @Query("SELECT * FROM workouts ORDER BY id DESC")
    List<Workout> getAllWorkouts();

    @Query("SELECT * FROM workouts WHERE id = :id LIMIT 1")
    Workout getWorkoutById(int id);
}