package com.example.android.sunshine;

/**
 * Created by davebarnum on 3/4/15.
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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

import com.example.android.sunshine.com.example.android.sunshine.json.WeatherDataParser;
import com.example.android.sunshine.tasks.DetailActivity;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;



/**
 * A placeholder fragment containing a simple view.
 */
public class ForecastFragment extends Fragment {

    private final String mockData[] = {"Today-Sunny-83/63","Tomorrow-Sunny-83/63","Wednesday-Sunny-83/63","Thursday-Sunny-83/63","Friday-Sunny-83/63"};
    private ArrayAdapter<String> mAdapter;
    private ArrayList<String> mForecastData;

    private ListView mListView;

    public ForecastFragment() {
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mForecastData = new ArrayList<String>(Arrays.asList(mockData));
        mAdapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,mForecastData);
        final ListView listView = (ListView) rootView.findViewById(R.id.list_view_forecast);
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String text = (String)listView.getItemAtPosition(position);
//                Toast toast = Toast.makeText(view.getContext(), text, Toast.LENGTH_LONG);
//                toast.show();
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT,text);
                startActivity(intent);
            }
        });
        listView.setAdapter(mAdapter);
        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            updateWeather();
           return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateWeather() {
        FetchWeatherTask task = new FetchWeatherTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        String location;
        String key = this.getActivity().getString(R.string.pref_location_key);
        String defaultValue = this.getActivity().getString(R.string.pref_location_default_value);
        location = prefs.getString(key,defaultValue);
        task.execute(location,"7");
    }


    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        private String[] params;

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);
            ArrayList<String> data = new ArrayList<String>(Arrays.asList(strings));
            mAdapter.clear();

            for (int i=0; i < data.size(); i++) {
                mAdapter.add(data.get(i));
            }
        }

        @Override
        protected String[] doInBackground(String... params) {
            this.params = params;
            String postalCode = params[0];
            Integer numDays = Integer.parseInt(params[1]);
            String forecastJsonStr = null;

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                Uri builtUri = Uri.parse("http://api.openweathermap.org/data/2.5/forecast/daily").buildUpon()
                        .appendQueryParameter("q",postalCode)
                        .appendQueryParameter("mode","json")
                        .appendQueryParameter("units","metric")
                        .appendQueryParameter("cnt","7")
                        .build();

                //URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");
                //Log.v(LOG_TAG, builtUri.toString());
                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    forecastJsonStr = null;
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
                    forecastJsonStr = null;
                }
                forecastJsonStr = buffer.toString();
                WeatherDataParser p = new WeatherDataParser();
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String tempUnits = prefs.getString(getString(R.string.pref_units_key), getString(R.string.pref_default_temperature));
                return p.getWeatherDataFromJson(forecastJsonStr,numDays.intValue(),tempUnits);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                forecastJsonStr = null;
            } catch (JSONException e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
            return null;
        }
    }




}