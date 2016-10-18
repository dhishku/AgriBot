package gov.dacfw.agribot;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.JsonElement;

import java.util.Map;

import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIService;
import ai.api.AIServiceException;
import ai.api.model.AIContext;
import ai.api.model.AIError;
import ai.api.model.AIOutputContext;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity implements AIListener, View.OnClickListener{


    // setting up view variables
    private ImageButton ibListen;
    private EditText etQuery;
    private Button bSend;
    private TextView tvResult;
    private boolean isIbListenPressed;
    private String query;

    // setting up API.AI variables
    private AIService aiService;
    private AIDataService aiDataService;
    private AIRequest aiRequest;
    private AIConfiguration config;
    private static final String CLIENT_ACCESS_TOKEN = "d66aa3183338463cb8b8f7ad76c0d7f9";
    private AIOutputContext aiOutputContext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize the variables and set the buttons listening for clicks
        initializeVars();
    }

    private void initializeVars() {
        ibListen = (ImageButton)findViewById(R.id.ibListen);
        etQuery = (EditText)findViewById(R.id.etQuery);
        bSend = (Button)findViewById(R.id.bSend);
        tvResult = (TextView)findViewById(R.id.tvResult);
        // setting IB Listen Pressed to false
        isIbListenPressed = false;

        // Setting both buttons to listen for clicks
        ibListen.setOnClickListener(this);
        bSend.setOnClickListener(this);
        
        // Setting up API.AI variables
        config = new AIConfiguration(CLIENT_ACCESS_TOKEN,
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        // Setting up variables for the speech input
        aiService = AIService.getService(this, config);
        aiService.setListener(this);

        // Setting up variables for the text input
        aiDataService = new AIDataService(this, config);
        aiRequest = new AIRequest();
    }

    @Override
    public void onResult(AIResponse response) {
        Result result = response.getResult();
        // Get parameters
        /*String parameterString = "";
        if (result.getParameters() != null && !result.getParameters().isEmpty()) {
            for (final Map.Entry<String, JsonElement> entry : result.getParameters().entrySet()) {
                parameterString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
            }
        }*/
        String paramString = "";
        switch (result.getAction()){
            case "getWeatherInfo":
                // if the query is related to weather
                aiOutputContext = result.getContext("weatherinfocontext");
                Map<String, JsonElement> contextParams = aiOutputContext.getParameters();
                paramString = "(Name," + aiOutputContext.getName() + ")";
                for (final Map.Entry<String, JsonElement> entry : contextParams.entrySet()){
                    paramString += "(" + entry.getKey() + ", " + entry.getValue() + ") ";
                }
                break;
        }
        // Show results in TextView.
        tvResult.setText("Query:" + result.getResolvedQuery() +
                "\nAction: " + result.getAction() +
                "\nParameters: " + paramString);
    }

    @Override
    public void onError(AIError error) {
        tvResult.setText(error.toString());
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
        switch (v.getId()){
            case R.id.ibListen:
                if (!isIbListenPressed){
                    // this means the button was unpressed earlier and is pressed now
                    // So change the display icon and the boolean state variable.
                    ibListen.setImageResource(R.drawable.microphone_pressed);
                    isIbListenPressed = true;

                    // start listening i.e. set the aiService to listening mode
                    aiService.startListening();
                } else{
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
                            final AIResponse response = aiDataService.request(aiRequest);
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



}
