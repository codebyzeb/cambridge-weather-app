package uk.ac.cam.groupseven.weatherapp.viewmodelsources;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import hu.akarnokd.rxjava2.swing.SwingSchedulers;
import io.reactivex.Observable;
import uk.ac.cam.groupseven.weatherapp.datasources.RowingInfoSource;
import uk.ac.cam.groupseven.weatherapp.datasources.WeatherSource;
import uk.ac.cam.groupseven.weatherapp.models.FlagStatus;
import uk.ac.cam.groupseven.weatherapp.models.Weather;
import uk.ac.cam.groupseven.weatherapp.models.Wind;
import uk.ac.cam.groupseven.weatherapp.viewmodels.HomeViewModel;
import uk.ac.cam.groupseven.weatherapp.viewmodels.HomeViewModelImageIcon;
import uk.ac.cam.groupseven.weatherapp.viewmodels.Loadable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class HomeViewModelSource implements ViewModelSource<Loadable<HomeViewModel>> {
    @Inject
    private RowingInfoSource rowingInfoSource;
    @Inject
    private WeatherSource weatherSource;
    @Inject
    @Named("flagsDirectory")
    private Path flagDir;

    public Observable<Loadable<HomeViewModel>> getViewModel(Observable<Object> refresh) {
        return refresh
                //.throttleFirst(6, TimeUnit.SECONDS)
                .flatMap(x ->
                Observable
                        .just(new Loadable<HomeViewModel>()) //Return loading followed by the actual data
                        .concatWith(
                                rowingInfoSource.getFlagStatus()//Get flag and observe result
                                        .flatMap(
                                                flagStatus ->
                                                        weatherSource.getWeatherNow()//Get weather and observe result
                                                                .map(weather -> buildModel(flagStatus, weather))
                                        )
                                        .map(Loadable<HomeViewModel>::new)
                                        .onErrorReturn(Loadable::new)
                        )


                )

                .observeOn(SwingSchedulers.edt());
    }

    private HomeViewModel buildModel(FlagStatus flagStatus, Weather weather) throws IOException {
        float temperature = 0.0f;
        float windSpeed = 0.0f;
        String windDir = "None";


        if (weather.getWind() != null) {
            Wind wind = weather.getWind();
            if (wind.getSpeedMPS() != null) windSpeed = wind.getSpeedMPS();
            if (wind.getDirection() != null) windDir = wind.getDirection();
        }
        if (weather.getTemperature() != null) temperature = weather.getTemperature();


        // Set flag icon
        BufferedImage flagImage = ImageIO.read(new File(flagDir.resolve(flagStatus.getCode() + ".png").toAbsolutePath().toString()));
        HomeViewModelImageIcon flagIcon = new HomeViewModelImageIcon(flagImage.getScaledInstance(250, 250, Image.SCALE_FAST));

        return new HomeViewModel(flagStatus,
                flagIcon,
                temperature,
                windSpeed,
                windDir);
    }


}
