package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class ForecastFragment extends Fragment {
    private final String TAG = ForecastFragment.class.getSimpleName();
    ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void updateWeather(){
        FetchWeatherTask fetchWeatherTask = new FetchWeatherTask();
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = spf.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_defalut));
        String unit = spf.getString(getString(R.string.pref_pick_units_key),
                getString(R.string.pref_pick_units_default));
        new FetchWeatherTask().execute(location, unit);
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

        mForecastAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forrcaset,
                R.id.list_item_forecast_textview,
                new ArrayList<String>());
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, mForecastAdapter.getItem(position));
                startActivity(intent);
            }
        });
        return rootView;
    }

    class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        String forecastStr;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(LOG_TAG, "onPreExecute");
        }

        @Override
        protected String[] doInBackground(String... params) {

            final String QUERY_PARAM = "q";
            final String TYPE_PARAM = "mode";
            final String UNITS_PARAM = "units";
            final String NUMDAYS_PARAM = "cnt";
            final String APPID_PARAM = "appid";
            String mode = "json";
            String units = params[1];
            int numDays = 7;

            Uri buildUri = Uri.parse("http://api.openweathermap.org/data/2.5/forecast/daily?")
                    .buildUpon()
                    .appendQueryParameter(QUERY_PARAM, params[0])
                    .appendQueryParameter(TYPE_PARAM, mode)
                    .appendQueryParameter(UNITS_PARAM, units)
                    .appendQueryParameter(NUMDAYS_PARAM, Integer.toString(numDays))
                    .appendQueryParameter(APPID_PARAM, BuildConfig.OPEN_WEATHER_APPID)
                    .build();
            Log.v(LOG_TAG, "BuildUri " + buildUri.toString());
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(buildUri.toString());
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                InputStream ins = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(ins));

                if (ins == null) {
                    return null;
                }
                StringBuffer buffer = new StringBuffer();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    return null;
                }
                forecastStr = buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            try {
                return getWeatherDataFromJson(forecastStr, numDays);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] s) {
            if (s == null){
                return;
            }
            mForecastAdapter.clear();
            mForecastAdapter.addAll(s);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_refesh:
                Log.i(TAG, "option refresh click");
                if (checkInternetAccess()){
                    updateWeather();
                    return true;
                }else {
                    Toast.makeText(getActivity(),"Wifi is not connected.", Toast.LENGTH_SHORT).show();
                    return false;
                }
            default:
                return  super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (menu.findItem(R.id.action_refesh) == null){
            MenuItem item = menu.add(1, R.id.action_refesh, 9,getResources().getString(R.string.action_refesh));
        }
        super.onPrepareOptionsMenu(menu);
    }

    public boolean checkInternetAccess(){
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeInfo = connectivityManager.getActiveNetworkInfo();
        if (activeInfo != null && activeInfo.isConnected()){
            return activeInfo.getType() == ConnectivityManager.TYPE_WIFI;
        }else {
            return false;
        }
    }

    // 格式化时期显示
    private String getReadableDateString(long time){
        SimpleDateFormat shortenedDataFormat = new SimpleDateFormat("EEE MM dd");
        return shortenedDataFormat.format(time);
    }

    private String formatHighLow(double high, double low){
        SharedPreferences spf = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String unitType = spf.getString(getString(R.string.pref_pick_units_key),
                getString(R.string.pref_pick_units_default));
        if (unitType.equals(getString(R.string.pref_units_imperial))){
            high = (high * 1.8) + 32;
            low = (low * 1.8) + 32;
        }
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);
        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) throws JSONException {
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        if (forecastJsonStr == null){
            return null;
        }

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        if (forecastJson == null){
            return null;
        }
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);
        if (weatherArray == null || weatherArray.length() == 0){
            return null;
        }

        Time dayTime = new Time();
        dayTime.setToNow();
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        dayTime = new Time();
        String[] resultStr = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++){
            String day;
            String desciption;
            String highAndLow;
            JSONObject dayForecast = weatherArray.getJSONObject(i);
            long dateTime;
            dateTime = dayTime.setJulianDay(julianStartDay + i);
            day = getReadableDateString(dateTime);
            JSONObject weatherObject = (JSONObject) dayForecast.getJSONArray(OWM_WEATHER).get(0);
            desciption = weatherObject.getString(OWM_DESCRIPTION);

            JSONObject temperature = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperature.getLong(OWM_MAX);
            double low = temperature.getLong(OWM_MIN);
            highAndLow = formatHighLow(high, low);
            resultStr[i] = day + " - " + desciption + " - " + highAndLow;
        }

        for (String s : resultStr){
            Log.v(TAG, "Forecast entry: " + s );
        }
        return  resultStr;
    }
}