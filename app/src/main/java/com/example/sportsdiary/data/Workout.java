package com.example.sportsdiary.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "workouts")
public class Workout {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String sport;
    public String date;
    public int duration;
    public int calories;
    public String note;
}
