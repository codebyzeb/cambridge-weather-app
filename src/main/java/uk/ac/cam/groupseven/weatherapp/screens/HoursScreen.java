package uk.ac.cam.groupseven.weatherapp.screens;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import hu.akarnokd.rxjava2.swing.SwingObservable;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import uk.ac.cam.groupseven.weatherapp.Screen;
import uk.ac.cam.groupseven.weatherapp.ScreenLayout;
import uk.ac.cam.groupseven.weatherapp.styles.*;
import uk.ac.cam.groupseven.weatherapp.viewmodels.HourViewModel;
import uk.ac.cam.groupseven.weatherapp.viewmodels.HourlyWeather;
import uk.ac.cam.groupseven.weatherapp.viewmodels.Loadable;
import uk.ac.cam.groupseven.weatherapp.viewmodelsources.ViewModelSource;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.concurrent.TimeUnit;

public class HoursScreen implements Screen {
    @Inject
    ViewModelSource<Loadable<HourViewModel>> viewModelSource;
    @ApplyStyle(BackgroundStyle.class)
    private JPanel panel;
    @ApplyStyle(ButtonStyle.class)
    private JButton leftButton;
    @ApplyStyle(ButtonStyle.class)
    private JButton rightButton;
    @ApplyStyle(BackgroundStyle.class)
    private JPanel midPanel;
    @ApplyStyle(BackgroundStyle.class)
    private JPanel topPanel;
    @ApplyStyle({BackgroundStyle.class, HoursTableStyle.class})
    private JTable hoursTable;
    @ApplyStyle(BackgroundStyle.class)
    private JPanel bottomPanel;
    @ApplyStyle({BackgroundStyle.class, BigTextStyle.class})
    private JLabel timeLabel;
    @ApplyStyle(ButtonStyle.class)
    private JScrollPane scrollPanel;

    @Inject
    @Named("tempSmallIcon")
    private ImageIcon scaledTempIcon;
    @Inject
    @Named("windSmallIcon")
    private ImageIcon scaledWindIcon;

    @Override
    public Disposable start() {
        return viewModelSource.getViewModel(getRefreshObservable()).subscribe(this::updateScreen);
    }

    @Override
    public Observable<ScreenLayout.Direction> getScreenChanges() {
        // Map the correct action to each of the buttons
        return SwingObservable.actions(leftButton).map(x -> ScreenLayout.Direction.LEFT)
                .mergeWith(SwingObservable.actions(rightButton).map(x -> ScreenLayout.Direction.RIGHT));
    }

    private void updateScreen(Loadable<HourViewModel> viewModelLoadable) {
        // Deal with errors when getting the data from the viewmodel
        if (viewModelLoadable.getLoading()) {
            timeLabel.setText("Loading...");
        } else if (viewModelLoadable.getError() != null) {
            timeLabel.setText("Error");
        } else {
            HourViewModel viewModel = viewModelLoadable.getViewModel();
            assert viewModel != null;

            // Set the label to have the current time
            timeLabel.setText(viewModel.getCurrentTime().toString());

            // Set the table model to get the correct data
            hoursTable.setModel(new DefaultTableModel() {
                @Override
                public int getRowCount() {
                    return viewModel.getHourlyWeather().size();
                }

                @Override
                public int getColumnCount() {
                    return 3;
                }

                @Override
                public Object getValueAt(int row, int column) {
                    HourlyWeather hourlyWeather = viewModel.getHourlyWeather().get(row);

                    // Make each column display the correct information
                    switch (column) {
                        case 0:
                            return hourlyWeather.getTime();
                        case 1:
                            return hourlyWeather.getTemperature();
                        case 2:
                            return hourlyWeather.getWindSpeed();
                        default:
                            return null;
                    }
                }


            });

            // Make columns display the correct information and render correctly
            hoursTable.getColumnModel().getColumn(0)
                    .setHeaderRenderer((table, value, isSelected, hasFocus, row, column) -> {
                        JLabel jLabel = new JLabel((String) value);
                        jLabel.setFont(table.getTableHeader().getFont());
                        jLabel.setForeground(table.getTableHeader().getForeground());
                        return jLabel;
                    });
            hoursTable.getColumnModel().getColumn(1)
                    .setHeaderRenderer((table, value, isSelected, hasFocus, row, column) -> new JLabel((Icon) value));
            hoursTable.getColumnModel().getColumn(2)
                    .setHeaderRenderer((table, value, isSelected, hasFocus, row, column) -> new JLabel((Icon) value));
            hoursTable.getColumnModel().getColumn(0).setHeaderValue("Time");
            hoursTable.getColumnModel().getColumn(1).setHeaderValue(scaledTempIcon);
            hoursTable.getColumnModel().getColumn(2).setHeaderValue(scaledWindIcon);

            hoursTable.invalidate();
        }

    }

    public JPanel getPanel() {
        return panel;
    }

    private Observable<Object> getRefreshObservable() {
        return
                Observable.just(new Object()) // Refresh immediately
                        .mergeWith(Observable.interval(15, TimeUnit.SECONDS)); // And then refresh every 15 seconds
    }
}
