package com.example.android.sunshine.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by lcooper2 on 11/4/2016.
 */

public class ForecastFragment extends Fragment {

    ArrayAdapter<String> mForecastAdapter;
    public static String EXTRA_TEXT = "CATS AND DOGS";

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Report that this fragment would like to participate in populating the options
        // menu by receiving a call to onCreateOptionsMenu(Menu menu, MenuInflater inflater)
        setHasOptionsMenu(true);
    }

    @Override
    // Initialize the contents of the Activity's standard options menu. You should place your menu items in to menu.
    // For this method to be called, you must have first called setHasOptionsMenu(true).
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forcast_fragment, menu);
    }

    @Override
    // Describes what to do when items in options menu are selected. Uses item resource id
    // to differentiate between buttons.
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    // Update weather data
    private void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = sharedPreferences.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default));
        weatherTask.execute(location);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Dummy Data
        String[] data = {
                "Mon 6/23 - Sunny 31/17",
                "Tue 6/24 - Foggy 21/8",
                "Wed 6/25 - Cloudy 22/17",
                "Thu 6/26 - Rainy 18/11",
                "Fri 6/27 - Foggy 21/10",
                "Sat 6/28 - TRAPPED IN WEATHERSTATION 23/18",
                "Sun 6/29 - Sunny 20/7"
        };
        // Turn data into an arraylist and create its adapter
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(data));
        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(), // Context of the activity
                R.layout.listview_item_forecast, // Resource ID of layout
                R.id.list_item_forecast_textview, // Resource ID of textview
                weekForecast); // Array of data

        View rootView = inflater.inflate(R.layout.forecast_fragment, container, false);

        // Get ListView reference and hook up adapter
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String forecast = mForecastAdapter.getItem(position);
                Intent intent = DetailFragmentActivity.newIntent(getActivity(), forecast);
                startActivity(intent);
            }
        });
        listView.setAdapter(mForecastAdapter);
        return rootView;
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... Params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            String format = "json";
            String units = "metric";
            int numDays = 7;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL = "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q=";
                final String FORMAT_PARAM = "mode=";
                final String UNITS_PARAM = "units=";
                final String DAYS_PARAM = "cnt=";
                final String APPID_PARAM = "&APPID";

               URL url = new URL(FORECAST_BASE_URL
                        + QUERY_PARAM + Params[0] + "&"
                        + FORMAT_PARAM + format + "&"
                        + UNITS_PARAM + units + "&"
                        + DAYS_PARAM + Integer.toString(numDays)
                        + APPID_PARAM + BuildConfig.OPEN_WEATHER_MAP_API_KEY);


                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                System.out.println(forecastJsonStr);
            } catch (IOException e) {
                Log.e("ForecastFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }

            try {
                return getWeatherDataFromJson(forecastJsonStr, numDays);
            }
            catch(JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        } // end of doInBackground


        public String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) throws
            JSONException {
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMP = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            Time dayTime = new Time();
            dayTime.setToNow();
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);
            dayTime = new Time();

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            String[] resultStrings = new String[numDays];
            for(int i = 0; i < weatherArray.length(); i++) {
                String day;
                String description;
                String highAndLow;

                JSONObject dayForecast = weatherArray.getJSONObject(i);
                long dateTime = dayTime.setJulianDay(julianStartDay + i);
                Date date = new Date(dateTime);
                SimpleDateFormat format = new SimpleDateFormat("EEE MMM dd");
                day = format.format(date);

                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMP);
                double high = temperatureObject.getDouble(OWM_MAX);
                high = celsiusToFahrenheit(high);
                double low = temperatureObject.getDouble(OWM_MIN);
                low = celsiusToFahrenheit(low);
                highAndLow = formatHighLow(high, low);
                resultStrings[i] = day + " - " + description + " - " + highAndLow;
            }

            for(String s : resultStrings) {
                Log.v(LOG_TAG, "Weather entry " + s);
            }
            return resultStrings;




        }

        public String formatHighLow(double high, double low) {
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);
            String highLowString = roundedHigh + "/" + roundedLow;
            return highLowString;
        }

        public Double celsiusToFahrenheit(Double temp) {
            return ((9/5) * temp) + 32;
        }

        @Override
        protected void onPostExecute(String[] result) {
            if(result != null) {
                mForecastAdapter.clear();
            }
            for(String dayForecastStr: result) {
                mForecastAdapter.add(dayForecastStr);
            }
        }

        } // end of inner class

    } // end of outter class