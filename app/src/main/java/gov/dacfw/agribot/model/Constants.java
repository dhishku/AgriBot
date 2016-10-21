package gov.dacfw.agribot.model;

/**
 * Created by dhishku on 18/10/16.
 */
public class Constants {
    // Defining the constants for the Weather Context
    public static final String WEATHER_INFO_ACTION = "getWeatherInfo";
    public static final String CLIENT_ACCESS_TOKEN = "d66aa3183338463cb8b8f7ad76c0d7f9";
    public static final String WEATHER_INFO_CONTEXT = "weatherinfocontext";
    public static final String SUB_WEATHER_INTENT = "sub-weather-intent";
    public static final String MY_DATE = "myDate";
    public static final String MY_LOCATION = "myLocation";
    public static final String TENSE = "tense";
    public static final String MY_DATE_SYNONYMS = "myDateSynonyms";
    public static final String DATE = "date";
    public static final String MY_DATE_PERIOD = "myDatePeriod";
    public static final String MY_NUM = "myNum";


    // constants for constructing weather query
    public static final String BASE_WEATHER_URI = "http://api.openweathermap.org/data/2.5/";
    public static final String WEATHER_CURRENT_URI = "weather?";
    public static final String WEATHER_FORECAST_URI = "forecast/daily?";
    public static final String WEATHER_HISTORY_URI = "http://history.openweathermap.org/data/2.5/history/city?";
    public static final String WEATHER_API_KEY = "appid=8610a6150667733fd43f11e1d267d3a8";
    public static final String WEATHER_URI_UNITS = "units=metric";
    public static final String WEATHER_PARAMETER_CITY = "q=";
    public static final String WEATHER_PARAMETER_NUMDAYS_FORECAST = "days=";
    public static final String WEATHER_PARAMETER_DT_HISTORY = "dt=";



}
