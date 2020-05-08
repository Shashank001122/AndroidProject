package com.londonappbrewery.climapm.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.londonappbrewery.climapm.AutoSuggestAdapter;
import com.londonappbrewery.climapm.R;
import com.londonappbrewery.climapm.RecyclerViewAdapter;
import com.londonappbrewery.climapm.SearchResult;
import com.londonappbrewery.climapm.SharedPreference;
import com.londonappbrewery.climapm.Utils.BottomNavigationViewHelper;
import com.londonappbrewery.climapm.WeatherDataModel;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
public class WeatherController extends AppCompatActivity {
    ProgressBar spinner;
    public static SharedPreference sharedPreference = new SharedPreference();
    // Constants:
    String WEATHER_URL = "http://ninehome.us-east-1.elasticbeanstalk.com/weatherfind";
        final String Guardian_URL="http://ninehome.us-east-1.elasticbeanstalk.com/GuardianNine";
    // App ID to use OpenWeather data
    final String APP_ID = "8bfe21bd9235112278292aec24582b38";
    // Time between location updates (5000 milliseconds or 5 seconds)
    final long MIN_TIME = 5000;
    // Distance between location updates (1000m or 1km)
    final float MIN_DISTANCE = 1000;
    final int REQUEST_CODE=123;
    // TODO: Set LOCATION_PROVIDER here:
    String LOCATION_PROVIDER = LocationManager.GPS_PROVIDER;

    private static final String TAG = "LikesActivity";
    private static final int ACTIVITY_NUM = 0;
    private static int run=0;

    private Context mContext = WeatherController.this;
    // Member Variables:
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;
    //vars
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> mArticleId=new ArrayList<>();
    private  ArrayList<String> mSectionNames=new ArrayList<>();
    private  ArrayList<String> mPublication=new ArrayList<>();

    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;
    private RequestQueue mQueue;
    private JSONObject jsonObj;
    private List<String> dataArr;
    private String locationAddress;
    private Toolbar tlbr;
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;

    private ArrayList<JSONObject> SabkaResultGuardian = new ArrayList<JSONObject>();
    // TODO: Declare a LocationManager and a LocationListener here:
    LocationManager mLocationManager;
    LocationListener mLocationListener;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        // Linking the elements in the layout to Java code
        mCityLabel = (TextView) findViewById(R.id.locationTV);
        mWeatherImage = (ImageView) findViewById(R.id.weatherSymbolIV);
        mTemperatureLabel = (TextView) findViewById(R.id.tempTV);
         spinner= (ProgressBar)findViewById(R.id.progressBar1);
         spinner.setVisibility(View.GONE);
         tlbr= (androidx.appcompat.widget.Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(tlbr);
        setupBottomNavigationView();
        final SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getGuardianData();
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(swipeContainer.isRefreshing()) {
                            swipeContainer.setRefreshing(false);
                        }
                    }
                }, 1000);

            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

@Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the search menu action bar.
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.yourlayout, menu);
        // Get the search menu
    MenuItem searchMenu = menu.findItem(R.id.search);

        final SearchView searchView = (SearchView) searchMenu.getActionView();
        // Get SearchView object.
    final SearchView.SearchAutoComplete searchAutoComplete = (SearchView.SearchAutoComplete)searchView.findViewById(R.id.search_src_text);
        searchAutoComplete.setBackgroundColor(Color.WHITE);
        searchAutoComplete.setTextColor(Color.BLACK);
        searchAutoComplete.setDropDownBackgroundResource(android.R.color.white);


        autoSuggestAdapter = new AutoSuggestAdapter(this, android.R.layout.simple_dropdown_item_1line);

        searchAutoComplete.setAdapter(autoSuggestAdapter);
        searchAutoComplete.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        String queryString=(String)parent.getItemAtPosition(position);
                        searchAutoComplete.setText("" + queryString);
                        Toast.makeText(WeatherController.this, "you clicked " + queryString, Toast.LENGTH_LONG).show();

                        getCurrentWeatherFromCity(queryString);
                        searchView.clearFocus();
                    }
                });

        searchAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                        AUTO_COMPLETE_DELAY);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(searchAutoComplete.getText())) {
                        try {
                            makeApiCall(searchAutoComplete.getText().toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                return false;
            }
        });


        return super.onCreateOptionsMenu(menu);

    }
    private void getCurrentWeatherFromCity(final String city) {
        String url = "http://ninehome.us-east-1.elasticbeanstalk.com/barsearchGuardian?val="+city;
        mQueue =  Volley.newRequestQueue(WeatherController.this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            mArticleId.clear();
                            mSectionNames.clear();
                            mImageUrls.clear();
                            mNames.clear();
                            mPublication.clear();
                            JSONArray SabkaResultGuardian = response.getJSONObject("response").getJSONArray("results");
                            for (int i = 0; i < 10; i++) {
                                mArticleId.add(SabkaResultGuardian.getJSONObject(i).getString("id"));
                                mSectionNames.add(SabkaResultGuardian.getJSONObject(i).getString("sectionName"));
                                mNames.add(SabkaResultGuardian.getJSONObject(i).getString("webTitle"));

                                ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("UTC"));
                                ZonedDateTime zoned = ZonedDateTime.parse(SabkaResultGuardian.getJSONObject(i).getString("webPublicationDate"));
                                Duration duration = Duration.between(zoned, zdt);
                                Long dur = duration.getSeconds();

                                if (dur < 60) {
                                    mPublication.add(Long.toString(dur) + "s ago");
                                } else if (dur >= 60 && dur < 3600) {
                                    mPublication.add(Long.toString(dur / 60) + "m ago");
                                } else if (dur >= 3600 && dur < 3600 * 24) {
                                    mPublication.add(Long.toString(dur / (60 * 60)) + "h ago");
                                } else if (dur >= 3600 * 24) {
                                    mPublication.add(Long.toString(dur / (60 * 60 * 24)) + "d ago");
                                }

                                if (SabkaResultGuardian.getJSONObject(i).getJSONObject("blocks").has("main") && SabkaResultGuardian.getJSONObject(i).getJSONObject("blocks").getJSONObject("main").has("elements") &&
                                        SabkaResultGuardian.getJSONObject(i).getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).has("assets") &&
                                        SabkaResultGuardian.getJSONObject(i).getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).getJSONArray("assets").length()>0) {
                                    mImageUrls.add(SabkaResultGuardian.getJSONObject(i).getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).getJSONArray("assets").getJSONObject(0).getString("file"));
                                } else {
                                    mImageUrls.add("https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png");
                                }
                            }

                            Intent intent = new Intent(WeatherController.this,  SearchResult.class);
                            intent.putExtra("mImages",mImageUrls);
                            intent.putExtra("mNames",mNames);
                            intent.putExtra("mArticleId",mArticleId);
                            intent.putExtra("mPublication",mPublication);
                            intent.putExtra("mSectionNames",mSectionNames);
                            startActivity(intent);

                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                                            }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        }
        );

        mQueue.add(request);
    }

    private void makeApiCall(String text) throws Exception {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://api.cognitive.microsoft.com/bing/v7.0/suggestions?mkt=en-US?&q="+text;
        StringRequest getRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {


                        List<String> stringList = new ArrayList<>();
                        try {
                            JSONObject responseObject = new JSONObject(response);
                            JSONArray array = responseObject.getJSONArray("suggestionGroups").getJSONObject(0).getJSONArray("searchSuggestions");
                            for (int i = 0; i < 5; i++) {
                                JSONObject row = array.getJSONObject(i);

                                stringList.add(row.getString("displayText"));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        //IMPORTANT: set data here and notify
                        autoSuggestAdapter.setData(stringList);
                        autoSuggestAdapter.notifyDataSetChanged();


                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("Ocp-Apim-Subscription-Key", "5634121ddbb64aa1a99d0d08766e9408");
                return params;
            }
        };
        queue.add(getRequest);
}
    private void setupBottomNavigationView(){

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomnav);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }



    // TODO: Add onResume() here:
    @Override
    protected void onResume() {
        super.onResume();

        spinner.setVisibility(View.VISIBLE);
        Intent myIntent=getIntent();
        String city=myIntent.getStringExtra("City");
        if(city!=null){
            getWeatherForNewCity(city);
        }else {
            getWeatherForCurrentLocation();
            getGuardianData();
        }
    }


    // TODO: Add getWeatherForNewCity(String city) here:
    private void getWeatherForNewCity(String city){
        RequestParams params=new RequestParams();
        params.put("q",city);
        params.put("appid",APP_ID);
        letsDoSomeNetworking(params);

    }


    // TODO: Add getWeatherForCurrentLocation() here:
    private void getWeatherForCurrentLocation() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                String longitude=String.valueOf(location.getLongitude());
                String latitude=String.valueOf(location.getLatitude());
                RequestParams params=new RequestParams();
                params.put("lat",latitude);
                params.put("lon",longitude);
                params.put("appid",APP_ID);
                letsDoSomeNetworking(params);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_CODE);
            return;
        }
        mLocationManager.requestLocationUpdates(LOCATION_PROVIDER, MIN_TIME, MIN_DISTANCE, mLocationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
if(requestCode==REQUEST_CODE){
if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_DENIED){

    getWeatherForCurrentLocation();
}
else{
    Log.d("Clima","Permission Denied");
}
}
    }
    // TODO: Add letsDoSomeNetworking(RequestParams params) here:
private void letsDoSomeNetworking(RequestParams params){
        WEATHER_URL=WEATHER_URL+"?"+params;
    AsyncHttpClient client=new AsyncHttpClient();
    client.get(WEATHER_URL,new JsonHttpResponseHandler(){
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response){

            WeatherDataModel weatherData=WeatherDataModel.fromJson(response);
            updateUI(weatherData);
        }

        @Override
        public void onFailure(int statusCode,Header[] headers, Throwable e, JSONObject response){
            Toast.makeText(WeatherController.this,"Request Failed", Toast.LENGTH_SHORT).show();
        }
    });
}

private void getGuardianData() {
        mArticleId.clear();
        mSectionNames.clear();
        mImageUrls.clear();
        mNames.clear();

    AsyncHttpClient client = new AsyncHttpClient();
        client.get(Guardian_URL, new JsonHttpResponseHandler() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                try {

                    JSONArray SabkaResultGuardian = res.getJSONObject("response").getJSONArray("results");
                    for (int i = 0; i < 10; i++) {
                        mArticleId.add(SabkaResultGuardian.getJSONObject(i).getString("id"));
                        mSectionNames.add(SabkaResultGuardian.getJSONObject(i).getString("sectionName"));
                        mNames.add(SabkaResultGuardian.getJSONObject(i).getString("webTitle"));

                        ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("UTC"));
                        ZonedDateTime zoned = ZonedDateTime.parse(SabkaResultGuardian.getJSONObject(i).getString("webPublicationDate"));
                        Duration duration = Duration.between(zoned, zdt);
                        Long dur = duration.getSeconds();

                        if (dur < 60) {

                            mPublication.add(Long.toString(dur) + "s ago");
                        } else if (dur >= 60 && dur < 3600) {
                            mPublication.add(Long.toString(dur / 60) + "m ago");
                        } else if (dur >= 3600 && dur < 3600 * 24) {
                            mPublication.add(Long.toString(dur / (60 * 60)) + "h ago");
                        } else if (dur >= 3600 * 24) {
                            mPublication.add(Long.toString(dur / (60 * 60 * 24)) + "d ago");
                        }

                        if (SabkaResultGuardian.getJSONObject(i).getJSONObject("fields").has("thumbnail")) {
                            mImageUrls.add(SabkaResultGuardian.getJSONObject(i).getJSONObject("fields").getString("thumbnail"));
                        } else {
                            mImageUrls.add("https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png");
                        }
                    }
                    initRecyclerView();
                    spinner.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable e, JSONObject response) {

            }
        });
    }

    // TODO: Add updateUI() here
    private void updateUI(WeatherDataModel weather){
        mTemperatureLabel.setText(weather.getTemperature());
        mCityLabel.setText(weather.getCity());
        int resourceID=getResources().getIdentifier(weather.getIconName(),"drawable",getPackageName());
        mWeatherImage.setImageResource(resourceID);
    }

    // TODO: Add onPause() here:
    protected void onPause(){
        super.onPause();
        if(mLocationManager!=null){
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    private void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this,mNames,mImageUrls,mSectionNames,mPublication,mArticleId);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
