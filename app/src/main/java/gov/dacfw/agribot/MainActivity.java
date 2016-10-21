package gov.dacfw.agribot;

import android.app.Application;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.JsonElement;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIService;
import ai.api.AIServiceException;
import ai.api.RequestExtras;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIOutputContext;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

import gov.dacfw.agribot.model.Constants;
import gov.dacfw.agribot.model.Weather;
import gov.dacfw.agribot.model.contexts.WeatherContext;
import gov.dacfw.agribot.services.ChatBotService;
import gov.dacfw.agribot.services.MySingleton;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AIListener {


    // setting up view variables
    private ImageButton ibListen;
    private EditText etQuery;
    private Button bSend;
    public TextView tvResult;
    private boolean isIbListenPressed;
    private String query;
    private String queryResult;

    // setting up API.AI variables
    private AIService aiService;
    private AIDataService aiDataService;
    private AIRequest aiRequest;
    private AIConfiguration config;

    private AIOutputContext aiOutputContext;
    private AIContext prevContext;
    private boolean preservePreviousContext;


    // Setting up ChatBotService variable
    private ChatBotService chatBotService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Locking screen in portrait mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // initialize the variables and set the buttons listening for clicks
        initializeVars();
    }

    private void initializeVars() {
        ibListen = (ImageButton) findViewById(R.id.ibListen);
        etQuery = (EditText) findViewById(R.id.etQuery);
        bSend = (Button) findViewById(R.id.bSend);
        tvResult = (TextView) findViewById(R.id.tvResult);
        // setting IB Listen Pressed to false
        isIbListenPressed = false;

        // Setting both buttons to listen for clicks
        ibListen.setOnClickListener(this);
        bSend.setOnClickListener(this);

        // Setting up ChatBotService instance
        chatBotService = new ChatBotService(getBaseContext());

        // Setting up API.AI variables
        config = new AIConfiguration(Constants.CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        // voice variables
        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        // data variables
        aiDataService = new AIDataService(this, config);
        aiRequest = new AIRequest();

        // setting previous context preservation to false
        preservePreviousContext = false;
        prevContext = new AIContext();
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibListen:
                if (!isIbListenPressed) {
                    // this means the button was unpressed earlier and is pressed now
                    // So change the display icon and the boolean state variable.
                    ibListen.setImageResource(R.drawable.microphone_pressed);
                    isIbListenPressed = true;

                    // check for previous contexts
                    if (preservePreviousContext) {
                        // yes prevoius context needs to be preserved
                        List<AIContext> contexts = new ArrayList<>();
                        contexts.add(prevContext);
                        RequestExtras requestExtras = new RequestExtras(contexts, null);
                        aiService.startListening(requestExtras);
                    } else {
                        // start listening i.e. set the aiService to listening mode
                        aiService.startListening();
                    }
                } else {
                    // this means the button was pressed earlier and is unpressed now
                    // So change the display icon and the boolean state variable.
                    ibListen.setImageResource(R.drawable.microphone_unpressed);
                    isIbListenPressed = false;

                    // also stop listening i.e. set the aiService to stop listening
                    aiService.stopListening();
                }

                break;
            case R.id.bSend:
                // User has typed something instead of speaking
                query = etQuery.getText().toString();
                aiRequest.setQuery(query);

                new AsyncTask<AIRequest, Void, AIResponse>() {
                    @Override
                    protected AIResponse doInBackground(AIRequest... requests) {
                        final AIRequest request = requests[0];
                        try {
                            final AIResponse response;
                            // check for previous contexts
                            if (preservePreviousContext) {
                                // yes prevoius context needs to be preserved
                                List<AIContext> contexts = new ArrayList<>();
                                contexts.add(prevContext);
                                RequestExtras requestExtras = new RequestExtras(contexts, null);
                                System.out.println("Previous context saved. Before sending request to API.AI");
                                response = aiDataService.request(aiRequest, requestExtras);
                            } else {
                                // start listening i.e. set the aiService to listening mode
                                System.out.println("No previous context saved. Before sending request to API.AI");
                                response = aiDataService.request(aiRequest);
                            }
                            return response;
                        } catch (AIServiceException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(AIResponse aiResponse) {
                        if (aiResponse != null) {
                            // process aiResponse here
                            onResult(aiResponse);
                        }
                    }
                }.execute(aiRequest);
                break;

        }
    }

    @Override
    public void onResult(AIResponse response) {
        if (response == null)
            return;

        Result result = response.getResult();
        if (result == null)
            return;

        etQuery.setText(result.getResolvedQuery());
        // first check if the action is complete.
        // if not, ask for the mandatory field

        // if yes, do the processing

        switch (result.getAction()) {
            case Constants.WEATHER_INFO_ACTION:
                aiOutputContext = result.getContext(Constants.WEATHER_INFO_CONTEXT);
                Map<String, String> contextParams = chatBotService.convertToStringMap(aiOutputContext.getParameters());

                if (result.isActionIncomplete()) {
                    System.out.println("AIResponse action incomplete.");
                    // action is incomplete. Prompt for the mandatory variable.
                    tvResult.setText(R.string.promptLocation);

                    // create a context to save
                    prevContext = new AIContext(Constants.WEATHER_INFO_CONTEXT);
                    prevContext.setLifespan(aiOutputContext.getLifespan());
                    prevContext.setParameters(contextParams);

                    // set the boolean variable to be true to indicate that previous context is to be preserved
                    preservePreviousContext = true;
                    System.out.println("Saving the previous Context.'");
                } else {
                    // action is complete. Process the response.
                    System.out.println("AIResponse action is complete.");
                    WeatherContext weatherContext = new WeatherContext(response);
                    getWeather(weatherContext);
                }
                break;
        }


        String paramString = "";
        switch (result.getAction()) {
            case "getWeatherInfo":
                // if the query is related to weather
                aiOutputContext = result.getContext("weatherinfocontext");
                Map<String, JsonElement> contextParams = aiOutputContext.getParameters();
                paramString = "(Name," + aiOutputContext.getName() + ")";
                for (final Map.Entry<String, JsonElement> entry : contextParams.entrySet()) {
                    paramString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
                }
                break;
        }
        // Show results in TextView.
        /* tvResult.setText("Query:" + result.getResolvedQuery() +
                "\nAction: " + result.getAction() +
                "\nParameters: " + paramString);
                */
    }

    @Override
    public void onError(AIError error) {
        tvResult.setText(error.toString());
    }




    private Location getCurrentCoordinates() {
        LocationManager locationManager = (LocationManager) getSystemService(Application.LOCATION_SERVICE);
        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

// check if enabled and if not send user to the GSP settings
// Better solution would be to display a dialog and suggesting to
// go to the settings
        if (!enabled) {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }

        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return null;
        }
        return locationManager.getLastKnownLocation(provider);
    }

    private String getCity(Location loc){
        if (loc == null)
            return null;

        String cityName=null;
        Geocoder gcd = new Geocoder(getBaseContext(),
                Locale.getDefault());
        List<Address>  addresses;
        try {
            addresses = gcd.getFromLocation(loc.getLatitude(), loc
                    .getLongitude(), 1);
            cityName=addresses.get(0).getLocality();
            return cityName;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void getWeather(final WeatherContext weatherContext) {

        String url = chatBotService.constructURI(weatherContext);
        System.out.println("URI produced: " + url);
        queryResult = "";

        // right now we are not retrieving past data. So if past data is sought, we will return a blank string.
        if (url == null)
            return;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, (String) null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (weatherContext.getTense().equalsIgnoreCase("future")) {
                            System.out.println("Inside onResponse of Volley with future tense.");
                            queryResult = chatBotService.getResultString(Weather.getForecastInstance(response, weatherContext), weatherContext);
                            tvResult.setText(queryResult);
                        }
                        if (weatherContext.getTense().equalsIgnoreCase("present")) {
                            System.out.println("Inside onResponse of Volley with present tense");
                            queryResult = chatBotService.getResultString(Weather.getCurrentInstance(response), weatherContext);
                            System.out.println("Inside onResponse again present tense. Query result: " + queryResult);
                            MainActivity main = new MainActivity();
                            tvResult.setText(queryResult);
                        }
                        if (weatherContext.getTense().equalsIgnoreCase("past")) {
                            System.out.println("Inside onResponse of Volley with past tense");
                            queryResult = "Error in Weather Information Retrieval.\n";
                            tvResult.setText(queryResult);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                queryResult = "Required weather data not available at the moment.\n";
                tvResult.setText(queryResult);
                error.printStackTrace();
            }
        });

        MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        System.out.println("After MySingleton");
    }
}
