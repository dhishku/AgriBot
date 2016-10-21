package gov.dacfw.agribot.model.contexts;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import ai.api.model.AIResponse;
import ai.api.model.Result;
import gov.dacfw.agribot.model.Constants;

/**
 * Created by dhishku on 18/10/16.
 */
public class WeatherContext {
    private Date currentDate;
    private String sub_weather_intent;
    private Date myDate;
    private String myLocation;
    private String tense;


    public WeatherContext() {

    }

    public Date getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }

    public WeatherContext(AIResponse aiResponse) {
        if (aiResponse == null)
            return;

        Result result = aiResponse.getResult();
        if (result == null)
            return;
        if (result.getAction().equalsIgnoreCase(Constants.WEATHER_INFO_ACTION)) {
            // yes it is a weather intent
            System.out.println("Creating a WeatherContext.");
            this.currentDate = aiResponse.getTimestamp();
            System.out.println("currentDate: " + this.currentDate);

            Map<String, JsonElement> context = result.getContext(Constants.WEATHER_INFO_CONTEXT).getParameters();
            if (!context.get(Constants.TENSE).isJsonNull())
                this.tense = context.get(Constants.TENSE).getAsString();
            else
                this.tense = "";
            System.out.println("Tense: " + this.tense);
            this.sub_weather_intent = context.get(Constants.SUB_WEATHER_INTENT).getAsString();
            System.out.println("sub-weather-intent: " + this.sub_weather_intent);
            this.myLocation = context.get(Constants.MY_LOCATION).getAsString();
            System.out.println("location: " + this.myLocation);
            // Fixing date
            JsonElement md = context.get(Constants.MY_DATE);
            this.myDate = getDate(md, this.tense);
            System.out.println("myDate: " + this.myDate);
            // resetting tense
            Calendar c1 = Calendar.getInstance();
            c1.setTime(currentDate);

            Calendar c2 = Calendar.getInstance();
            c2.setTime(this.myDate);
            if (c2.get(Calendar.DATE) == c1.get(Calendar.DATE))
                this.tense = "present";
            else {
                if (this.myDate.after(this.currentDate))
                    this.tense = "future";
                if (this.myDate.before(this.currentDate))
                    this.tense = "past";
            }
            System.out.println("Tense has been reset to: " + this.tense);
        }

    }

    private Date getDate(JsonElement md, String tense) {
        DateFormat yyyymmdd = new SimpleDateFormat("yyyy-mm-dd");
        System.out.println("Inside WeatherContext.getDate() with date: " + md.toString());
        if (md == null)
            return currentDate;

        if (md.isJsonNull())
            return currentDate;

        if (tense == null)
            tense = "";

        // Start handling cases in whihc date can occur
        /*"myDate": "today"*/
        if (md.isJsonPrimitive()) {
            // it is of the type today, yesterday etc.
            String str_date = md.getAsString();
            if (str_date.equalsIgnoreCase("today"))
                return currentDate;
        }

        /*Now we handle the types myDate:{...}*/
        if (md.isJsonObject()) {
            JsonObject jmd = md.getAsJsonObject();

            /*Now we handle the types
            "myDate": {
            "myDateSynonyms": "today",
            "myDateSynonyms.original": "aaj"
            },*/
            if (jmd.has(Constants.MY_DATE_SYNONYMS)) {
                // if it is of the type today
                String str_date = jmd.get(Constants.MY_DATE_SYNONYMS).getAsString();
                if (str_date.equalsIgnoreCase("today"))
                    return currentDate;
                // 1 day: kal
                if (str_date.equalsIgnoreCase("1 day")) {
                    if (tense.equalsIgnoreCase("past"))
                        // case of yesterday kal. subtract 1 day.
                        return addOrSubtractDays(1, false);
                    else
                        // case of tomorrow kal. add 1 day.
                        return addOrSubtractDays(1, true);
                }
                // 2 day: parson
                if (str_date.equalsIgnoreCase("2 days")) {
                    if (tense.equalsIgnoreCase("past"))
                        // case of yesterday kal. subtract 2 days.
                        return addOrSubtractDays(2, false);
                    else
                        // case of tomorrow kal. add 2 days.
                        return addOrSubtractDays(2, true);
                }
            }

            /*"myDate": {
            "date": "2016-10-19",
            "date.original": "today"
            }*/
            if (jmd.has(Constants.DATE)) {
                try {
                    Date d = yyyymmdd.parse(jmd.get(Constants.DATE).getAsString());
                    return d;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        /*  "myDate": {
            "myDatePeriod.original": "picchle hafte",
            "myDatePeriod": {
                ...
            }}*/

            if (jmd.has(Constants.MY_DATE_PERIOD)) {
                /*"myDatePeriod": {
                "myNum": ...,
                "myNum.original": ...,
                "myDateSynonyms": ...,
                "myDateSynonyms.original": ...
                }
                */
                if (jmd.get(Constants.MY_DATE_PERIOD).isJsonObject()) {
                    // myDatePeriod is a JsonObject
                    JsonObject jmdp = jmd.get(Constants.MY_DATE_PERIOD).getAsJsonObject();
                    if (jmdp.has(Constants.MY_NUM) && jmdp.has(Constants.MY_DATE_SYNONYMS)) {
                        // it has both the number and datesynonym
                        String mds = jmdp.get(Constants.MY_DATE_SYNONYMS).getAsString();
                        int factor = 1;
                        switch (mds) {
                            case "week":
                                factor = 7;
                                break;
                            case "month":
                                factor = 30;
                                break;
                            case "year":
                                factor = 365;
                                break;
                            case "day":
                                factor = 1;
                                break;
                            case "days":
                                factor = 1;
                                break;
                        }
                        /* Matching this condition would mean types
                        *
                          "myDatePeriod": {
                          "myNum": "3",
                          "myNum.original": "3 ",
                          "myDateSynonyms": "day",
                          "myDateSynonyms.original": "din "
                          },*/
                        /*if (jmdp.get(Constants.MY_NUM).getAsJsonPrimitive().isNumber()) {
                            int mn = jmdp.get(Constants.MY_NUM).getAsInt();

                            if (tense.equalsIgnoreCase("past")) {
                                // subtract from currentdate
                                return addOrSubtractDays(mn * factor, false);
                            } else {
                                // add into currentdate
                                return addOrSubtractDays(mn * factor, false);
                            }
                        }*/

                        String mn = jmdp.get(Constants.MY_NUM).getAsString();
                        try{
                            int int_mn = Integer.parseInt(mn);
                            if (tense.equalsIgnoreCase("past")) {
                                // subtract from currentdate
                                return addOrSubtractDays(int_mn * factor, false);
                            } else {
                                // add into currentdate
                                return addOrSubtractDays(int_mn * factor, true);
                            }
                        } catch (NumberFormatException e){
                            if (mn.equalsIgnoreCase("previous"))
                                // case of last week. subtract days.
                                return addOrSubtractDays(factor, false);

                            if (mn.equalsIgnoreCase("next"))
                                // case of next week. add days.
                                return addOrSubtractDays(factor, true);
                            e.printStackTrace();
                        }

                    }
                }
            }

        }
        return currentDate;
    }

    private Date addOrSubtractDays(int numDays, boolean isAdd) {
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        if (isAdd)
            c.add(Calendar.DATE, numDays);
        else
            c.add(Calendar.DATE, -numDays);

        return c.getTime();

    }

    public String getSub_weather_intent() {
        return sub_weather_intent;
    }

    public void setSub_weather_intent(String sub_weather_intent) {
        this.sub_weather_intent = sub_weather_intent;
    }

    public Date getMyDate() {
        return myDate;
    }

    public String getMyDateAsString(){
        System.out.println("Inside weatherContext.getMyDateAsString. myDate in Date is: " + myDate);
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        System.out.println("Inside weatherContext.getMyDateAsString. myDate in String is: " + df.format(myDate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(myDate);
        System.out.println("Inside weatherContext.getMyDateAsString. Calendar approach myDate is: " + cal.get(Calendar.YEAR)
        +"-"+(cal.get(Calendar.MONTH)+1)+"-"+cal.get(Calendar.DAY_OF_MONTH));
        return df.format(myDate);
    }
    public void setMyDate(Date myDate) {
        this.myDate = myDate;
    }

    public String getMyLocation() {
        return myLocation;
    }

    public void setMyLocation(String myLocation) {
        this.myLocation = myLocation;
    }

    public String getTense() {
        return tense;
    }

    public void setTense(String tense) {
        this.tense = tense;
    }
}
