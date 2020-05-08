package com.londonappbrewery.climapm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.londonappbrewery.climapm.Utils.BottomNavigationViewHelper;
import com.londonappbrewery.climapm.Utils.Product;
import com.londonappbrewery.climapm.home.WeatherController;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Bookmark extends AppCompatActivity {
    private static final int ACTIVITY_NUM = 3;
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> mSectionNames= new ArrayList<>();
    private ArrayList<String> mPublication= new ArrayList<>();
    private ArrayList<String> mArticleId=new ArrayList<>();
    private ArrayList<String> mIds=new ArrayList<>();
    private Context mContext = Bookmark.this;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;
    private RequestQueue mQueue;
    private JSONObject jsonObj;
    private List<String> dataArr;
    private String locationAddress;
    private Toolbar tlbr;
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private RequestQueue mRequestQueue;
    private LineChart mchart;
    private String keyword;
    private ArrayList<Entry> values;
    TextView mCityLabel;
    ImageView mWeatherImage;
    TextView mTemperatureLabel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmark);
        setTheme(R.style.detailTheme);

        // Linking the elements in the layout to Java code
        setupBottomNavigationView();
        ArrayList<Product> favs = WeatherController.sharedPreference.getFavorites(Bookmark.this);
        if(favs!=null){
            for (Product fav : favs) {
                mImageUrls.add(fav.images);
                mNames.add(fav.imagenames);
                mSectionNames.add(fav.sectionNames);
                mPublication.add(fav.publication);
                mIds.add(fav.id);
            }
        }
        tlbr= (androidx.appcompat.widget.Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(tlbr);
        final SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

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
                        Log.d("queryString",queryString);
                        searchAutoComplete.setText("" + queryString);
                        Toast.makeText(Bookmark.this, "you clicked " + queryString, Toast.LENGTH_LONG).show();

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
        mQueue =  Volley.newRequestQueue(Bookmark.this);;
        Log.d("guardiancall", url);
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
                            Log.d("here",response.toString());
                            for (int i = 0; i < 10; i++) {
                                mArticleId.add(SabkaResultGuardian.getJSONObject(i).getString("id"));
                                mSectionNames.add(SabkaResultGuardian.getJSONObject(i).getString("sectionName"));
                                mNames.add(SabkaResultGuardian.getJSONObject(i).getString("webTitle"));

                                ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("UTC"));
                                ZonedDateTime zoned = ZonedDateTime.parse(SabkaResultGuardian.getJSONObject(i).getString("webPublicationDate"));
                                Duration duration = Duration.between(zoned, zdt);
                                Long dur = duration.getSeconds();

                                if (dur < 60) {
                                    Log.d("Climax", dur.toString());
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
                            Log.d("here",mArticleId.toString());
                            Intent intent = new Intent(Bookmark.this,  SearchResult.class);
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
                        //Log.d("resp",response.toString());

                        List<String> stringList = new ArrayList<>();
                        try {
                            JSONObject responseObject = new JSONObject(response);
//                            //Log.d("kya",responseObject.getJSONArray("suggestionGroups").toString());
                            JSONArray array = responseObject.getJSONArray("suggestionGroups").getJSONObject(0).getJSONArray("searchSuggestions");
                            for (int i = 0; i < 5; i++) {
                                JSONObject row = array.getJSONObject(i);
                                Log.d("kya",row.getString("displayText"));
                                stringList.add(row.getString("displayText"));
                            }
                        } catch (Exception e) {
                            Log.d("kya error",e.getMessage()) ;
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
                        Log.d("ERROR","error => "+error.toString());
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


    // TODO: Add onResume() here:
    @Override
    protected void onResume() {
        super.onResume();
        initRecyclerView();
    }

    private void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.recycler_bookmark);
        TextView txtvw = findViewById(R.id.text1);
        if(mPublication.size()>0) {
            RecyclerBookmark adapter = new RecyclerBookmark(mContext, mNames, mImageUrls, mSectionNames, mPublication, mIds);
            Log.d("adapter", String.valueOf(adapter));
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
            txtvw.setVisibility(View.INVISIBLE);
        }
        else{
            txtvw.setVisibility(TextView.VISIBLE);
        }
    }
    private void setupBottomNavigationView(){
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomnav);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
        initRecyclerView();
    }
}
