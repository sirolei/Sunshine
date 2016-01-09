package com.example.android.sunshine.app;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
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


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        ArrayAdapter<String> mForecastAdapter;

        public PlaceholderFragment() {
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

            new Thread(new Runnable() {
                @Override
                public void run() {
                    //进行网络请求
                    HttpURLConnection connection = null;
                    String url = "http://api.openweathermap.org/data/2.5/forecast/daily?q=Chengdu,cn&mode=json&units=metric&cnt=7&appid=2de143494c0b295cca9337e1e96b00e0";
                    BufferedReader reader = null;
                    try {
                        URL forecastUrl = new URL(url);
                        connection = (HttpURLConnection) forecastUrl.openConnection();
                        connection.setRequestMethod("GET");
                        connection.connect();

                        // 读取ins
                        InputStream ins = connection.getInputStream();
                        StringBuffer stringBuffer = new StringBuffer();
                        if (ins == null){
                            return;
                        }
                        reader = new BufferedReader(new InputStreamReader(ins));
                        String line;

                        while ((line = reader.readLine()) != null){
                            stringBuffer.append("/n");
                        }

                        if (stringBuffer.length() == 0){
                            return ;
                        }

                        String forecastStr = stringBuffer.toString();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (connection != null){
                            connection.disconnect();
                        }

                        if (reader != null){
                            try {
                                reader.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }).run();

            return rootView;
        }
    }
}
