package com.londonappbrewery.climapm.Newssari;

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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import com.londonappbrewery.climapm.AutoSuggestAdapter;
import com.londonappbrewery.climapm.R;
import com.londonappbrewery.climapm.SearchResult;
import com.londonappbrewery.climapm.Utils.BottomNavigationViewHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class News extends AppCompatActivity {
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
    private RequestQueue mRequestQueue;
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private static final int ACTIVITY_NUM = 1;

    private Context mContext = News.this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.news);
        setTheme(R.style.detailTheme);

        setupBottomNavigationView();
        mRequestQueue = Volley.newRequestQueue(this);
        final TextView textView = findViewById(R.id.textView_label);
        tlbr= (androidx.appcompat.widget.Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(tlbr);
        setupViewPager();
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
                        Toast.makeText(News.this, "you clicked " + queryString, Toast.LENGTH_LONG).show();

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
        mQueue =  Volley.newRequestQueue(News.this);;
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
                            Intent intent = new Intent(News.this,  SearchResult.class);
                            intent.putExtra("mImages",mImageUrls);
                            intent.putExtra("mNames",mNames);
                            intent.putExtra("mArticleId",mArticleId);
                            intent.putExtra("mPublication",mPublication);
                            intent.putExtra("mSectionNames",mSectionNames);
                            intent.putExtra("query",city);
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

    private void setupViewPager(){
        SectionsPagerAdapter adapter=new SectionsPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new WorldFragment());
        adapter.addFragment(new BusinessFragment());
        adapter.addFragment(new PoliticsFragment());
        adapter.addFragment(new SportsFragment());
        adapter.addFragment(new TechnologyFragment());
        adapter.addFragment(new ScienceFragment());
        ViewPager viewPager=(ViewPager) findViewById(R.id.container);
        viewPager.setOffscreenPageLimit(4);
        viewPager.setAdapter(adapter);
        TabLayout tabLayout=(TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(0).setText("World");
        tabLayout.getTabAt(1).setText("Business");
        tabLayout.getTabAt(2).setText("Politics");
        tabLayout.getTabAt(3).setText("Sports");
        tabLayout.getTabAt(4).setText("Technology");
        tabLayout.getTabAt(5).setText("Science");
    }
    private void setupBottomNavigationView(){
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomnav);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }

}
