package com.example.sportsdiary.viewmodel;

import com.example.sportsdiary.data.WorkoutRepository;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class WorkoutViewModelFactory implements ViewModelProvider.Factory {

    private final WorkoutRepository repository;

    public WorkoutViewModelFactory(WorkoutRepository repository) {
        this.repository = repository;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(
            @NonNull Class<T> modelClass) {

        if (modelClass.isAssignableFrom(WorkoutViewModel.class)) {
            return (T) new WorkoutViewModel(repository);
        }

        throw new IllegalArgumentException(
                "Unknown ViewModel class: " + modelClass.getName()
        );
    }
}
