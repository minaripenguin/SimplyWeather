package com.chae.weather;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements SharedPreferences {
    EditText etCity, etCountry;
    TextView tvResult, cityText, noticeText;
    TextClock dateClock;
    private final String url = "https://api.openweathermap.org/data/2.5/weather";
    private final String appid = "e53301e27efa0b66d05045d91b2742d3";
    boolean isLoadingData;
    String description;
    DecimalFormat df = new DecimalFormat("#.#");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etCity = findViewById(R.id.etCity);
        etCountry = findViewById(R.id.etCountry);
        tvResult = findViewById(R.id.tvResult);
        cityText = findViewById(R.id.cityText);
        dateClock  = findViewById(R.id.dateClock);
        noticeText  = findViewById(R.id.noticeText);
        restoreCity();
    }

    public void restoreCity() {
        SharedPreferences sh = getSharedPreferences("city", Context.MODE_PRIVATE);
        String s1 = sh.getString("city", "");
        if (!"".equals(cityText)) {
            cityText.setText(s1);
            loadWeatherDetails(tvResult);
        }
    }

    public void loadWeatherDetails(View view) {
        isLoadingData = true;
        retrieveData();
    }

    public void getWeatherDetails(View view) {
        isLoadingData = false;
        String city1 = etCity.getText().toString();
        cityText.setText(city1);
        SharedPreferences sharedPreferences = getSharedPreferences("city",MODE_PRIVATE);
        SharedPreferences.Editor userCity = sharedPreferences.edit();
        userCity.putString("city", cityText.getText().toString());
        userCity.commit();
        retrieveData();
    }

    public void retrieveData() {
        String tempUrl = "";
        String city;
        if (isLoadingData) {
            city = cityText.getText().toString().trim();
        } else {
            city = etCity.getText().toString().trim();
        }
        String country = etCountry.getText().toString().trim();
        if ("".equals(city)) {
            tvResult.setText(R.string.cityIsEmpty);
        } else {
            if (!country.equals("")) {
                tempUrl = url + "?q=" + city + "," + country + "&appid=" + appid;
            } else {
                tempUrl = url + "?q=" + city + "&appid=" + appid;
            }
            StringRequest stringRequest = new StringRequest(Request.Method.POST, tempUrl, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    String output = "";
                    try {
                        JSONObject jsonResponse = new JSONObject(response);
                        JSONArray jsonArray = jsonResponse.getJSONArray("weather");
                        JSONObject jsonObjectWeather = jsonArray.getJSONObject(0);
                        description = jsonObjectWeather.getString("description");
                        JSONObject jsonObjectMain = jsonResponse.getJSONObject("main");
                        double temp = jsonObjectMain.getDouble("temp") - 273.15;
                        double feelsLike = jsonObjectMain.getDouble("feels_like") - 273.15;
                        float pressure = jsonObjectMain.getInt("pressure");
                        int humidity = jsonObjectMain.getInt("humidity");
                        JSONObject jsonObjectWind = jsonResponse.getJSONObject("wind");
                        String wind = jsonObjectWind.getString("speed");
                        JSONObject jsonObjectClouds = jsonResponse.getJSONObject("clouds");
                        String clouds = jsonObjectClouds.getString("all");
                        JSONObject jsonObjectSys = jsonResponse.getJSONObject("sys");
                        String countryName = jsonObjectSys.getString("country");
                        String cityName = jsonResponse.getString("name");
                        output += "Current location: " + cityName + " (" + countryName + ")"
                                + "\nStatistics Temperature: " + df.format(temp) + " °C"
                                + "\nReal World Temperature: " + df.format(feelsLike) + " °C"
                                + "\nHumidity: " + humidity + "%"
                                + "\nDescription: " + description
                                + "\nWind Speed: " + wind + "m/s (meters per second)"
                                + "\nCloudiness: " + clouds + "%"
                                + "\nPressure: " + pressure + " hPa";
                        tvResult.setText(output);
                        noticeText.setText(R.string.UpToDateInfo);
                        updateWeatherIcon();
                        SharedPreferences sharedPreferences = getSharedPreferences("output", MODE_PRIVATE);
                        SharedPreferences.Editor savedOutput = sharedPreferences.edit();
                        savedOutput.putString("output", output);
                        savedOutput.commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, error -> Toast.makeText(getApplicationContext(), R.string.toastError, Toast.LENGTH_LONG).show());
            SharedPreferences outputRestore = getSharedPreferences("output", Context.MODE_PRIVATE);
            String tvResult1 = outputRestore.getString("output", "");
            description = "offline";
            tvResult.setText(tvResult1);
            noticeText.setText(R.string.lastCachedResults);
            updateWeatherIcon();
            RequestQueue requestQueue;
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            final Request<String> add = requestQueue.add(stringRequest);
        }
    }

    public void updateWeatherIcon() {
        if (description.contains("clear sky")) {
            int cloudy = R.drawable.ic_baseline_wb_sunny_24;
            dateClock.setCompoundDrawablesWithIntrinsicBounds(cloudy, 0,0,0);
        } else if (description.contains("few clouds")) {
            int sunny = R.drawable.weather_partly_cloudy;
            dateClock.setCompoundDrawablesWithIntrinsicBounds(sunny, 0,0,0);
        } else if (description.contains("scattered clouds")) {
            int cloud = R.drawable.clouds;
            dateClock.setCompoundDrawablesWithIntrinsicBounds(cloud, 0,0,0);
        } else if (description.contains("broken clouds")) {
            int brokenCloud = R.drawable.clouds;
            dateClock.setCompoundDrawablesWithIntrinsicBounds(brokenCloud, 0,0,0);
        } else if (description.contains("shower rain")) {
            int shower = R.drawable.weather_rainy;
            dateClock.setCompoundDrawablesWithIntrinsicBounds(shower, 0,0,0);
        } else if (description.contains("rain")) {
            int rain = R.drawable.weather_pouring;
            dateClock.setCompoundDrawablesWithIntrinsicBounds(rain, 0,0,0);
        } else if (description.contains("thunderstorm")) {
            int thunder = R.drawable.weather_lightning_rainy;
            dateClock.setCompoundDrawablesWithIntrinsicBounds(thunder, 0,0,0);
        } else if (description.contains("snow")) {
            int snow = R.drawable.weather_snowy;
            dateClock.setCompoundDrawablesWithIntrinsicBounds(snow, 0,0,0);
        } else if (description.contains("mist")) {
            int mist = R.drawable.weather_fog;
            dateClock.setCompoundDrawablesWithIntrinsicBounds(mist, 0,0,0);
        } else if (description.contains("overcast clouds")) {
            int overcast = R.drawable.ic_baseline_wb_cloudy_24;
            dateClock.setCompoundDrawablesWithIntrinsicBounds(overcast, 0,0,0);
        } else {
            int unknown = R.drawable.weather_cloudy_alert;
            dateClock.setCompoundDrawablesWithIntrinsicBounds(unknown, 0,0,0);
        }
    }

    public void refreshActivity(View view) {
        Intent intent = getIntent();
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        overridePendingTransition(0, 0);
    }

    @Override
    public Map<String, ?> getAll() {
        return null;
    }

    @Nullable
    @Override
    public String getString(String s, @Nullable String s1) {
        return null;
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String s, @Nullable Set<String> set) {
        return null;
    }

    @Override
    public int getInt(String s, int i) {
        return 0;
    }

    @Override
    public long getLong(String s, long l) {
        return 0;
    }

    @Override
    public float getFloat(String s, float v) {
        return 0;
    }

    @Override
    public boolean getBoolean(String s, boolean b) {
        return false;
    }

    @Override
    public boolean contains(String s) {
        return false;
    }

    @Override
    public Editor edit() {
        return null;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

    }

}
