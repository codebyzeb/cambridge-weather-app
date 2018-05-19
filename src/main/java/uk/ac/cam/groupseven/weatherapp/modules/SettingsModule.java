package uk.ac.cam.groupseven.weatherapp.modules;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;

import java.awt.*;
import java.util.Calendar;

public class SettingsModule implements Module {
    @Override
    public void configure(Binder binder) {
        binder.bind(Dimension.class).annotatedWith(Names.named("screenDimension"))
                .toInstance(new Dimension(700, 1132)); //Set screen size
        binder.bind(Calendar.class).toInstance(Calendar.getInstance());
        binder.bind(String.class).annotatedWith(Names.named("windowTitle")).toInstance("Weather App");
    }
}