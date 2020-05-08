package com.londonappbrewery.climapm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.Utils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.londonappbrewery.climapm.Utils.BottomNavigationViewHelper;

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

public class Up extends AppCompatActivity {
    private static final int ACTIVITY_NUM = 2;
    //private TrendingViewModel trendingViewModel;
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
    private RequestQueue mRequestQueue;
    private LineChart mchart;
    private String keyword;
    private ArrayList<Entry> values;
    //    View root;
    private Context cnxt;
    private ProgressBar spinner;

    private Context mContext = Up.this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.up);
        spinner= (ProgressBar)findViewById(R.id.progressBar1);
        spinner.setVisibility(View.GONE);
        setTheme(R.style.detailTheme);
        setupBottomNavigationView();
        mRequestQueue = Volley.newRequestQueue(this);
        final TextView textView = findViewById(R.id.textView_label);
        tlbr= (androidx.appcompat.widget.Toolbar)findViewById(R.id.my_toolbar);
        setSupportActionBar(tlbr);
    EditText editText = (EditText) findViewById(R.id.search);
        keyword = (String) editText.getHint();

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEND)
                {
                    Log.d("shashank", "" + v.getText());
                    String keywords =  v.getText().toString();
                    renderData(cnxt, keywords);
                    handled = true;
                }
                return handled;
            }
        });

        mchart = findViewById(R.id.charts);
        mchart.setTouchEnabled(true);
        mchart.setPinchZoom(true);
        MarkerView_charts mv = new MarkerView_charts(this, R.layout.custome_marker_view);
        mv.setChartView(mchart);
        mchart.setMarker(mv);
        cnxt = this;
        renderData( this, keyword);

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
                        Toast.makeText(Up.this, "you clicked " + queryString, Toast.LENGTH_LONG).show();

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
        mQueue =  Volley.newRequestQueue(Up.this);
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
                            Log.d("here",mArticleId.toString());
                            Intent intent = new Intent(Up.this,  SearchResult.class);
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


    private void renderData(final Context context, final String keywords) {
        String url = "http://ninehome.us-east-1.elasticbeanstalk.com/trends/"+  keywords;
        final float[] ymax = {0};
        final float[] xmax = {0};
        values = new ArrayList<>();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("res");
                            xmax[0] = jsonArray.length();
                            for (int index = 0 ; index < jsonArray.length(); index++){
                                int j = jsonArray.getInt(index);
                                if (j > ymax[0]){
                                    ymax[0] = j;
                                }

                                values.add(new Entry(index, j));
                            }
                            setData(context, keywords);
                            spinner.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

 mRequestQueue.add(request);
        //MySingleton.getInstance(context).addToRequestQueue(request);
        LimitLine llXAxis = new LimitLine(10f, "Index 10");
        llXAxis.setLineWidth(4f);
        llXAxis.enableDashedLine(10f, 10f, 0f);
        llXAxis.setLabelPosition(LimitLine.LimitLabelPosition.RIGHT_BOTTOM);
        llXAxis.setTextSize(10f);

        XAxis xAxis = mchart.getXAxis();
        xAxis.enableGridDashedLine(10f, 10f, 0f);
        xAxis.setAxisMinimum(0f);
        xAxis.setDrawLimitLinesBehindData(true);

        YAxis leftAxis = mchart.getAxisLeft();
        leftAxis.removeAllLimitLines();
        leftAxis.setAxisMinimum(0f);
        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);
        leftAxis.setDrawLimitLinesBehindData(false);

        mchart.getAxisRight().setEnabled(false);
        setData(context, keywords);

    }

    private void setData(final Context context, String keywords) {


        LineDataSet set1;
        if (mchart.getData() != null &&
                mchart.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) mchart.getData().getDataSetByIndex(0);
            set1.setValues(values);
            set1.setLabel("Trending Chart for" + keywords);
            mchart.getData().notifyDataChanged();
            mchart.notifyDataSetChanged();
            mchart.invalidate();
        } else {
            set1 = new LineDataSet(values, "Trending Chart for" + keywords);

            set1.setDrawIcons(false);
            set1.enableDashedLine(10f, 5f, 0f);
            set1.enableDashedHighlightLine(10f, 5f, 0f);
            set1.setColor(Color.BLUE);
            set1.setCircleColor(Color.BLUE);
            set1.setLineWidth(1f);
            set1.setCircleRadius(3f);
            set1.setDrawCircleHole(false);
            set1.setValueTextSize(15f);
            set1.setDrawFilled(true);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);

            if (Utils.getSDKInt() >= 18) {
                Drawable drawable = ContextCompat.getDrawable(context, R.drawable.fade_blue);
                set1.setFillDrawable(drawable);
            } else {
                set1.setFillColor(Color.DKGRAY);
            }
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            LineData data = new LineData(dataSets);
            mchart.setExtraBottomOffset(30);
            mchart.setData(data);
        }
    }


    private void setupBottomNavigationView(){
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomnav);
        BottomNavigationViewHelper.enableNavigation(mContext, bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
