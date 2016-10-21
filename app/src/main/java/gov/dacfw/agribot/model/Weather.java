package gov.dacfw.agribot.model;

import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import gov.dacfw.agribot.model.contexts.WeatherContext;

/**
 * Created by dhishku on 19/10/16.
 */
public class Weather {
    private String weather;
    private String weatherDescription;
    private double currTemp;
    private double maxTemp;
    private double minTemp;
    private double humidity;
    private double windSpeed;
    private double windDir;
    private double rain;

    public Weather() {
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getWeatherDescription() {
        return weatherDescription;
    }

    public void setWeatherDescription(String weatherDescription) {
        this.weatherDescription = weatherDescription;
    }

    public double getCurrTemp() {
        return currTemp;
    }

    public void setCurrTemp(double avgTemp) {
        this.currTemp = avgTemp;
    }

    public double getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(double maxTemp) {
        this.maxTemp = maxTemp;
    }

    public double getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(double minTemp) {
        this.minTemp = minTemp;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public double getWindDir() {
        return windDir;
    }

    public void setWindDir(double windDir) {
        this.windDir = windDir;
    }

    public double getRain() {
        return rain;
    }

    public void setRain(double rain) {
        this.rain = rain;
    }

    /*{"city":{"id":1275339,"name":"Mumbai","coord":{"lon":72.847939,"lat":19.01441},"country":"IN",
    "population":0},"cod":"200","message":0.0059,"cnt":7,"list":[
    {"dt":1476856800,"temp":{"day":32.11,"min":22.51,"max":32.11,"night":22.51,"eve":29.85,"morn":32.11},"pressure":1009.39,"humidity":59,"weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"01d"}],"speed":3.03,"deg":318,"clouds":0},
    {"dt":1476943200,"temp":{"day":30.03,"min":20.14,"max":31.61,"night":23.01,"eve":29.01,"morn":20.14},"pressure":1011.87,"humidity":70,"weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"01d"}],"speed":1.82,"deg":327,"clouds":0},
    {"dt":1477029600,"temp":{"day":29.79,"min":22.91,"max":29.79,"night":24.12,"eve":29.22,"morn":22.91},"pressure":1005.17,"humidity":0,"weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"01d"}],"speed":1.25,"deg":100,"clouds":0},
    {"dt":1477116000,"temp":{"day":30.14,"min":22.48,"max":30.14,"night":24.26,"eve":29.06,"morn":22.48},"pressure":1006.5,"humidity":0,"weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"01d"}],"speed":1.51,"deg":14,"clouds":0},
    {"dt":1477202400,"temp":{"day":30.11,"min":22.44,"max":30.11,"night":22.96,"eve":28.91,"morn":22.44},"pressure":1006.64,"humidity":0,"weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"01d"}],"speed":2.38,"deg":332,"clouds":0},
    {"dt":1477288800,"temp":{"day":29.71,"min":21.35,"max":29.71,"night":21.87,"eve":28.63,"morn":21.35},"pressure":1005.41,"humidity":0,"weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"01d"}],"speed":1.35,"deg":295,"clouds":0},
    {"dt":1477375200,"temp":{"day":29.67,"min":20.42,"max":29.67,"night":21.35,"eve":28.58,"morn":20.42},"pressure":1004.87,"humidity":0,"weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"01d"}],"speed":0.87,"deg":64,"clouds":22}]}*/
    public static Weather getForecastInstance(JSONObject response, WeatherContext weatherContext) {
        Weather w = new Weather();
        if (response == null || weatherContext == null)
            return w;
        Calendar myDate = Calendar.getInstance();
        myDate.setTime(weatherContext.getMyDate());

        try{
            JSONArray jsonArray = response.getJSONArray("list");
            JSONObject obj = new JSONObject();
            // Obtain unix timestamp and convert to date. iterate through the array and see which date matches ours.
            for (int i = 0; i < jsonArray.length(); i++){
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(jsonArray.getJSONObject(i).getLong("dt")*1000);

                if (myDate.get(Calendar.DATE) == cal.get(Calendar.DATE)){
                    obj = jsonArray.getJSONObject(i);
                    break;
                }
                if (i == jsonArray.length()-1)
                    // if we have reached end of the list, declare last forecast day as the match.
                    obj = jsonArray.getJSONObject(i);
            }

            // Now obj stores the forecast we are looking for.
            System.out.println("Inside weather.getForecastInstance(). Matched forecast object is: " + obj.toString());
            // {"dt":1476943200,"temp":{"day":30.03,"min":20.14,"max":31.61,"night":23.01,"eve":29.01,"morn":20.14},"pressure":1011.87,"humidity":70,"weather":[{"id":800,"main":"Clear","description":"clear sky","icon":"01d"}],"speed":1.82,"deg":327,"clouds":0},
            if (!obj.isNull("weather")) {
                w.weather = obj.getJSONArray("weather").getJSONObject(0).getString("main");
                w.weatherDescription = obj.getJSONArray("weather").getJSONObject(0).getString("description");
                System.out.println("Setting weather: " + w.weather + " , weather description: " + w.weatherDescription);
            }

            if (!obj.isNull("temp")) {
                w.currTemp = obj.getJSONObject("temp").getDouble("day");
                w.minTemp = obj.getJSONObject("temp").getDouble("min");
                w.maxTemp = obj.getJSONObject("temp").getDouble("max");
                System.out.println("Setting temp: " + w.currTemp + " , max temp: " + w.maxTemp + " , min temp: " + w.minTemp);
            }

            if (!obj.isNull("humidity")) {
                w.humidity = obj.getDouble("humidity");
                System.out.println("Setting humidity: " + w.humidity);
            }

            if (!obj.isNull("speed")) {
                w.windSpeed = obj.getDouble("speed");
                System.out.println("Setting wind speed: " + w.windSpeed);
            }

            if (!obj.isNull("deg")) {
                w.windDir = obj.getDouble("deg");
                System.out.println("Setting wind dir: " + w.windDir);
            }
            if (!obj.isNull("rain")) {
                w.rain = obj.getDouble("rain");
                System.out.println("Setting rain: " + w.rain);
            }
        }catch (JSONException e){
            e.printStackTrace();
        } finally{
            return w;
        }
    }

    /*{"coord":{"lon":139.69,"lat":35.69},
    "weather":[{"id":803,"main":"Clouds","description":"broken clouds","icon":"04d"}],
    "base":"stations",
    "main":{"temp":294.728,"pressure":1027.43,"humidity": 89,"temp_min":294.728,"temp_max":294.728,"sea_level":1031.21,"grnd_level":1027.43},
    "wind":{" speed":3.05,"deg":49.5004},"clouds":{"all":56},"dt":1476840799,
    "sys":{"message":0.0151,"country ":"JP","sunrise":1476823872,"sunset":1476864037},"id":1850147,"name":"Tokyo","cod":200}
    * */
    public static Weather getCurrentInstance(JSONObject response) {
        Weather w = new Weather();
        if (response == null)
            return w;

        try {
            if (!response.isNull("weather")) {
                w.weather = response.getJSONArray("weather").getJSONObject(0).getString("main");
                w.weatherDescription = response.getJSONArray("weather").getJSONObject(0).getString("description");
            }
            if (!response.isNull("main")) {
                w.currTemp = response.getJSONObject("main").getDouble("temp");
                w.minTemp = response.getJSONObject("main").getDouble("temp_min");
                w.maxTemp = response.getJSONObject("main").getDouble("temp_max");
                w.humidity = response.getJSONObject("main").getDouble("humidity");
            }
            if (!response.isNull("wind")) {
                w.windSpeed = response.getJSONObject("wind").getDouble("speed");
                w.windDir = response.getJSONObject("wind").getDouble("deg");
            }
            if (!response.isNull("rain")) {
                w.rain = response.getJSONObject("rain").getDouble("3h");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            return w;
        }

    }
}
