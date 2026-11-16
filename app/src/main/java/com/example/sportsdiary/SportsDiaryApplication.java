package com.example.sportsdiary;

import android.app.Application;

import com.example.sportsdiary.di.AppContainer;

public class SportsDiaryApplication extends Application {

    private AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();

        appContainer = new AppContainer(this);
    }

    public AppContainer getAppContainer() {
        return appContainer;
    }
}
