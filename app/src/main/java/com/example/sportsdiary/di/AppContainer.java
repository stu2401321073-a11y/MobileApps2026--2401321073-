package com.example.sportsdiary.di;

import android.content.Context;
import com.example.sportsdiary.data.WorkoutRepository;

public class AppContainer {

    public final WorkoutRepository workoutRepository;

    public AppContainer(Context context) {
        workoutRepository = new WorkoutRepository(context);
    }
}
