package com.londonappbrewery.climapm.Newssari;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.londonappbrewery.climapm.R;
import com.londonappbrewery.climapm.RecyclerViewAdapter;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class PoliticsFragment extends Fragment {
    ProgressBar spinner;
    private static final String TAG = "WorldFragment";
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> mArticleId = new ArrayList<>();
    private ArrayList<String> mSectionNames = new ArrayList<>();
    private ArrayList<String> mPublication = new ArrayList<>();
    private ArrayList<JSONObject> SabkaResultGuardian = new ArrayList<JSONObject>();
    private String WorldUrl = "http://ninehome.us-east-1.elasticbeanstalk.com/GuardianPol";

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_world, container, false);
        spinner= (ProgressBar) view.findViewById(R.id.progressBar1);
        spinner.setVisibility(View.VISIBLE);
        final RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_container);

        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getFragmentManager().beginTransaction().detach(PoliticsFragment.this).attach(PoliticsFragment.this).commit();

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(swipeLayout.isRefreshing()) {
                            swipeLayout.setRefreshing(false);
                        }
                    }
                }, 1000);
                swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                        android.R.color.holo_green_light,
                        android.R.color.holo_orange_light,
                        android.R.color.holo_red_light);

            }
        });

        AsyncHttpClient client = new AsyncHttpClient();
        RequestHandle requestHandle = client.get(WorldUrl, new JsonHttpResponseHandler() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                try {
                    JSONArray SabkaResultGuardian = res.getJSONObject("response").getJSONArray("results");
                    int len=SabkaResultGuardian.length();
                    Log.d("Length", String.valueOf(len));
                    for (int i = 0; i < 10 && i<len; i++) {
                        mArticleId.add(SabkaResultGuardian.getJSONObject(i).getString("id"));
                        mSectionNames.add(SabkaResultGuardian.getJSONObject(i).getString("sectionName"));
                        mNames.add(SabkaResultGuardian.getJSONObject(i).getString("webTitle"));
                        ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("UTC"));
                        ZonedDateTime zoned = ZonedDateTime.parse(SabkaResultGuardian.getJSONObject(i).getString("webPublicationDate"));
                        Duration duration = Duration.between(zoned,zdt);
                        Long dur = duration.getSeconds();

                        if (dur < 60 ) {
                            Log.d("Climax", dur.toString());
                            mPublication.add(Long.toString(dur) + "s ago");
                        } else if (dur >= 60 && dur < 3600) {
                            mPublication.add(Long.toString(dur / 60) + "m ago");
                        } else if (dur >= 3600 && dur < 3600 * 24) {
                            mPublication.add(Long.toString(dur / (60 * 60)) + "h ago");
                        } else if (dur >= 3600 * 24) {
                            mPublication.add(Long.toString(dur / (60 * 60 * 24)) + "d ago");
                        }
                        try {
                            if (SabkaResultGuardian.getJSONObject(i).getJSONObject("blocks").has("main") && SabkaResultGuardian.getJSONObject(i).getJSONObject("blocks").getJSONObject("main").has("elements") &&
                                    SabkaResultGuardian.getJSONObject(i).getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).has("assets") &&
                                    SabkaResultGuardian.getJSONObject(i).getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).getJSONArray("assets").length()>0) {
                                mImageUrls.add(SabkaResultGuardian.getJSONObject(i).getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).getJSONArray("assets").getJSONObject(0).getString("file"));
                                Log.d("Data", mNames.toString());
                            } else {
                                mImageUrls.add("https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png");
                            }

                            Context context = inflater.getContext();
                            RecyclerViewAdapter adapter = new RecyclerViewAdapter(context, mNames, mImageUrls, mSectionNames, mPublication,mArticleId);
                            recyclerView.setAdapter(adapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(context));
                            spinner.setVisibility(View.GONE);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            };


        });

        return view;
    }
}
