package uk.ac.cam.groupseven.weatherapp.datasources;


import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import uk.ac.cam.groupseven.weatherapp.models.Weather;
import uk.ac.cam.groupseven.weatherapp.models.Wind;

import javax.xml.bind.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class OpenWeatherSource {

    URL apiUrl;
    InputStream apiResponse;

    public Observable<Weather> getWeatherNow() {
        try {
            apiUrl = new URL("http://api.openweathermap.org/data/2.5/forecast?APPID=8b35f0643a9e43fac171d05738bd2b8d&id=2653941&units=metric&mode=xml"); // TODO Get Nathan to @Inject this (and maybe tidy up and separate url parameters)
        }
        catch(MalformedURLException e) {
            System.out.println(e.getMessage());
        }


        return Observable.fromCallable(() -> {
            Weather.Precipitation currentPrecipitation = Weather.Precipitation.NONE;
            int currentCloudCover = 0;
            float currentTemperature = 0.0f; // These may be bad initialization values.  IDK what would be good ones!
            Wind currentWind = new Wind(0.0f, "");
            float windSpeed = 0.0f;
            String windDir = "";
            try {
                apiResponse = apiUrl.openStream();
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document weatherDataDoc = builder.parse(apiResponse);
                NodeList readings = weatherDataDoc.getElementsByTagName("time");



                for(int i = 0; i < readings.item(0).getChildNodes().getLength(); i++){ /* Get first forecast in response (current-ish) */
                    Node dataNode = readings.item(0).getChildNodes().item(i);
                    if(dataNode.getNodeName().equals("temperature")) {
                        currentTemperature = Float.parseFloat(dataNode.getAttributes().getNamedItem("value").getNodeValue());
                        System.out.println("Temperature: " + dataNode.getAttributes().getNamedItem("value").getNodeValue()+"C");
                    }
                    if(dataNode.getNodeName().equals("windDirection")){
                        System.out.println("Wind Direction: "+dataNode.getAttributes().getNamedItem("code").getNodeValue());
                        windDir = dataNode.getAttributes().getNamedItem("code").getNodeValue();
                    }
                    if(dataNode.getNodeName().equals("windSpeed")){
                        windSpeed = Float.parseFloat(dataNode.getAttributes().getNamedItem("mps").getNodeValue());
                        System.out.println("Wind Speed: "+dataNode.getAttributes().getNamedItem("mps").getNodeValue()+", "+dataNode.getAttributes().getNamedItem("name").getNodeValue());
                    }
                    if(dataNode.getNodeName().equals("clouds")){
                        currentCloudCover = Integer.parseInt(dataNode.getAttributes().getNamedItem("all").getNodeValue());
                        System.out.println("Cloud cover: "+dataNode.getAttributes().getNamedItem("all").getNodeValue()+"%");
                    }
                    if(dataNode.getNodeName().equals("precipitation")){
                        NamedNodeMap attrs = dataNode.getAttributes();

                        for(int j = 0; j < attrs.getLength(); j++) {
                            if (attrs.item(j).getNodeName().equals("type")) {
                                if (attrs.item(j).getNodeValue().equals("rain")) {
                                    currentPrecipitation = Weather.Precipitation.RAIN;
                                } else if (attrs.item(j).getNodeValue().equals("snow")) {
                                    currentPrecipitation = Weather.Precipitation.SNOW;
                                }
                            }
                        }
                        System.out.println("Precipitation: " + currentPrecipitation);
                    }
                }

            }
            catch(IOException i) {
                System.out.println(i.getMessage());
            }

            currentWind = new Wind(windSpeed, windDir);
            Thread.sleep(1000); //Pretend to do some work;
            return new Weather(currentPrecipitation, currentCloudCover, currentTemperature, currentWind);
        })
                .subscribeOn(Schedulers.io()); //Do in background io thread

    }

    public Observable<Weather> getWeatherInHours(int hours) {
        return Observable.fromCallable(() -> {
            //TODO: Replace with API call
            Thread.sleep(1000); //Pretend to do some work;
            return new Weather(Weather.Precipitation.values()[hours % 3], 0, 0.0f, new Wind(0.0f, "NW")); //Fast and accurate weather prediction
        })
                .subscribeOn(Schedulers.io()); //Do in background io thread

    }

}
