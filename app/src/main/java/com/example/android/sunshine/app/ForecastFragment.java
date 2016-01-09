package com.example.android.sunshine.app;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        String[] data = {
                "Mon 6/23 - Sunny - 31/17",
                "Mon 6/23 - Sunny - 31/17",
                "Mon 6/23 - Sunny - 31/17",
                "Mon 6/23 - Sunny - 31/17",
                "Mon 6/23 - Sunny - 31/17",
                "Mon 6/23 - Sunny - 31/17",
                "Mon 6/23 - Sunny - 31/17",
        };
        ArrayList<String> weekForecast = new ArrayList<String>(Arrays.asList(data));
        mForecastAdapter = new ArrayAdapter<String>(getActivity(),
                R.layout.list_item_forrcaset,
                R.id.list_item_forecast_textview,
                weekForecast);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        new FetchWeatherTask().execute("http://api.openweathermap.org/data/2.5/forecast/daily?q=Chengdu,cn&mode=json&units=metric&cnt=7&appid=2de143494c0b295cca9337e1e96b00e0");
        return rootView;
    }

    class FetchWeatherTask extends AsyncTask<String, Void, String> {
        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();
        String forecastStr;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i(LOG_TAG, "onPreExecute");
        }

        @Override
        protected String doInBackground(String... params) {
            String urlStr = params[0];
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(urlStr);
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
            return forecastStr;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i(LOG_TAG, "receive data is : " + s);
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        inflater.inflate(R.menu.forecast, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.action_refesh:
                Log.i(TAG, "option refresh click");
                return true;
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
}