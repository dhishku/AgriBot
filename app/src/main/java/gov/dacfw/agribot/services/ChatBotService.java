package gov.dacfw.agribot.services;

import android.content.Context;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.util.HashMap;
import java.util.Map;

import gov.dacfw.agribot.model.Constants;
import gov.dacfw.agribot.model.Weather;
import gov.dacfw.agribot.model.contexts.WeatherContext;

/**
 * Created by dhishku on 18/10/16.
 */
public class ChatBotService {
    private Context context;
    private String queryResult;
    private TextView result;


    public ChatBotService() {

    }

    public ChatBotService(Context context) {
        this.context = context;
        //result = (TextView)((MainActivity)context).findViewById(R.id.tvResult);
    }


    public Map<String, String> convertToStringMap(Map<String, JsonElement> contextParams) {
        Map<String, String> result = new HashMap<>();
        if (contextParams == null)
            return result;

        System.out.println("Inside convertToStringMap.");
        for (final Map.Entry<String, JsonElement> entry : contextParams.entrySet()) {
            //if (entry.getValue().isJsonPrimitive())
            result.put(entry.getKey(), entry.getValue().toString());
            System.out.println("(" + entry.getKey() +" , " + entry.getValue().toString() + ")");
        }
        return result;
    }



    public String getResultString(Weather currentWeather, WeatherContext weatherContext) {
        System.out.println("Inside getResultString");
        String result = "Location: " + weatherContext.getMyLocation() +"\n";
        result += "Date: " + weatherContext.getMyDateAsString() + "\n";
        switch (weatherContext.getSub_weather_intent()){
            case "rainfall":
                result += "Rainfall: " + currentWeather.getRain() + "mm\n";
                break;
            case "humidity":
                result += "Humidity: " + currentWeather.getHumidity() + "%\n";
                break;
            case "temperature":
                result += "Temp: " + currentWeather.getCurrTemp() +
                        "C\nMax Temp: " + currentWeather.getMaxTemp() +
                        "C\nMin Temp: " + currentWeather.getMinTemp() + "C\n";
                break;
            case "wind-speed":
                    result += "Wind speed: " + currentWeather.getWindSpeed() + "\n";
                break;
            case "wind-direction":
                result += "Wind direction: " + currentWeather.getWindDir() + "\n";
                break;
            case "all":
                result += "Weather: " + currentWeather.getWeather() + "\n";
                result += "Weather description: " + currentWeather.getWeatherDescription() + "\n";
                break;
        }
        System.out.println("result of query: " + result);
        return result;
    }

    public String constructURI(WeatherContext weatherContext) {
        System.out.println("Inside constructURI()");
        String url = Constants.BASE_WEATHER_URI;
        if (weatherContext.getTense().equalsIgnoreCase("future")) {
            url += Constants.WEATHER_FORECAST_URI;
        }
        if (weatherContext.getTense().equalsIgnoreCase("present")) {
            url += Constants.WEATHER_CURRENT_URI;
        }
        url += Constants.WEATHER_PARAMETER_CITY + weatherContext.getMyLocation();
        url += "&" + Constants.WEATHER_URI_UNITS;
        url += "&" + Constants.WEATHER_API_KEY;

        if (weatherContext.getTense().equalsIgnoreCase("past")) {
            url = Constants.WEATHER_HISTORY_URI;
            url += Constants.WEATHER_PARAMETER_CITY + weatherContext.getMyLocation();
            url+= "&type=daily&start="+weatherContext.getMyDateAsString();
            url+= "&" + Constants.WEATHER_URI_UNITS;
            url += "&" + Constants.WEATHER_API_KEY;
        }
        return url;
    }

}
