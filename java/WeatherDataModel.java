package com.londonappbrewery.climapm;

import org.json.JSONException;
import org.json.JSONObject;

public class WeatherDataModel {

    // TODO: Declare the member variables here
    private  String  mTemperature;
    private String mCity;
    private String mIconName;
    private String mCondition;


    // TODO: Create a WeatherDataModel from a JSON:
    public static WeatherDataModel fromJson(JSONObject jsonObject) {
        try {
            WeatherDataModel weatherData = new WeatherDataModel();
            weatherData.mCity = jsonObject.getString("name");
            weatherData.mCondition=jsonObject.getJSONArray("weather").getJSONObject(0).getString("main");
            weatherData.mIconName=updateWeatherIcon(weatherData.mCondition);
            double tempResult=(jsonObject.getJSONObject("main").getDouble("temp"))-273.15;
            int roundValue=(int)Math.rint(tempResult);
            weatherData.mTemperature=Integer.toString(roundValue);
            return weatherData;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    // TODO: Uncomment to this to get the weather image name from the condition:
    private static String updateWeatherIcon(String condition) {
        if (condition.equals("Clear")) {
            return "clear_weather";
        }
        else if(condition.equals("Clouds")){
            return "cloudy_weather";
            }

        else if(condition.equals("Snow")){
            return "snowy_weather";
        }

        else if(condition.equals("Rain") || condition.equals("Drizzle")){
            return "rainy_weather";
        }
        else if(condition.equals("Thunderstorm")){
            return "thunder_weather";
        }
        else{
            return "sunny_weather";
        }

    }

    // TODO: Create getter methods for temperature, city, and icon name:
    public String getTemperature(){
        return mTemperature+"°C "+mCondition;
    }
public String getCity(){
        return mCity;
}
public String getIconName(){
        return mIconName;
}

}
