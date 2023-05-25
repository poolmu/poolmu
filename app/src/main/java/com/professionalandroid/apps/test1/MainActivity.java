package com.professionalandroid.apps.test1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationClient;
    private TextView weatherTextView;
    private Button weatherButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        weatherTextView = findViewById(R.id.weatherTextView);
        weatherButton = findViewById(R.id.weatherButton);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        weatherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            double lat = location.getLatitude();
                            double lon = location.getLongitude();
                            getWeatherFromApi(lat, lon);
                        } else {
                            weatherTextView.setText("위치를 찾을 수 없습니다.");
                        }
                    }
                });
            }
        });
    }

    private void getWeatherFromApi(double lat, double lon) {
        String apiKey = "8afc84d7b4bcec8b8147c10a27c30e80";
        String url = "http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey;

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    final String result = response.body().string();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONParser jsonParser = new JSONParser();
                                JSONObject jsonObj = (JSONObject) jsonParser.parse(result);

                                String location = "지역: " + jsonObj.get("name");

                                JSONArray weatherArray = (JSONArray) jsonObj.get("weather");
                                JSONObject obj = (JSONObject) weatherArray.get(0);
                                String weather = "날씨: " + obj.get("main");

                                JSONObject mainArray = (JSONObject) jsonObj.get("main");
                                double ktemp = Double.parseDouble(mainArray.get("temp").toString());
                                double temp = ktemp - 273.15;
                                String temperature = String.format("온도 : %.2f", temp);

                                weatherTextView.setText(location + "\n" + weather + "\n" + temperature);
                            } catch (Exception e) {
                                weatherTextView.setText("Error: " + e.getMessage());
                            }
                        }
                    });
                }
            }
        });
    }
}
