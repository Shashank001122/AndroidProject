package com.londonappbrewery.climapm;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.londonappbrewery.climapm.Utils.Product;
import com.londonappbrewery.climapm.home.WeatherController;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class GalleryActivity extends AppCompatActivity {
    private Context mContext;
    private ImageView bookmark;
    private ImageView btntwitter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        setTheme(R.style.detailTheme);
        ImageView backbutton=findViewById(R.id.backbutton);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        getIncomingIntent();

        final SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipe_container);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getIncomingIntent();
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
    private void getIncomingIntent(){
        if(getIntent().hasExtra("id")){
            String id=getIntent().getStringExtra("id");
            String imageUrl=getIntent().getStringExtra("image");

            setImage(id,imageUrl);
        };
    }
    private void  setImage(final String id, final String imageUrl){

        String Guardian_URL="http://ninehome.us-east-1.elasticbeanstalk.com/searchGuardian?val="+id;
        final TextView name=findViewById(R.id.image_description);
        final TextView h1=findViewById(R.id.h1);
        final TextView detail=findViewById(R.id.detail);
        final TextView section=findViewById(R.id.section);
        final TextView date=findViewById(R.id.date);
        final ImageView image=findViewById(R.id.image);
        final ImageView cornerbookmark=findViewById(R.id.btnbookmark);
        final TextView linkv=findViewById(R.id.linkhain);


        AsyncHttpClient client=new AsyncHttpClient();
        client.get(Guardian_URL, new JsonHttpResponseHandler() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject res) {
                try {
                    final JSONObject SabkaResultGuardian = res.getJSONObject("response").getJSONObject("content");
                    name.setText(SabkaResultGuardian.getString("webTitle"));
                    h1.setText(SabkaResultGuardian.getString("webTitle"));
                    final String tempo =SabkaResultGuardian.getString("webTitle");
                    section.setText(SabkaResultGuardian.getString("sectionName"));
                    ZonedDateTime zdt = ZonedDateTime.now(ZoneId.of("America/Los_Angeles"));
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
                    String formattedString = zdt.format(formatter);
                    date.setText(formattedString);
                    detail.setText(SabkaResultGuardian.getJSONObject("blocks").getJSONArray("body").getJSONObject(Integer.parseInt("0")).getString("bodyTextSummary"));

String weburl=SabkaResultGuardian.getString("webUrl");
linkv.setText("View full Article");
                    linkv.setClickable(true);
                    linkv.setMovementMethod(LinkMovementMethod.getInstance());
                    String text = "<a href="+weburl+"> View Full Article </a>";
                    linkv.setText(Html.fromHtml(text));
                    Glide.with(getApplicationContext()).asBitmap()
                            .load(imageUrl)
                            .into(image);
                    bookmark= (ImageView) findViewById(R.id.btnbookmark);
                    btntwitter=(ImageView) findViewById(R.id.btntwitter);
                    ZonedDateTime zdt2 = ZonedDateTime.now(ZoneId.of("UTC"));
                    ZonedDateTime zoned2 = ZonedDateTime.parse(SabkaResultGuardian.getString("webPublicationDate"));
                    Duration duration = Duration.between(zoned2, zdt2);
                    Long dur = duration.getSeconds();
String temptime = null;
                    if (dur < 60) {


                        temptime=Long.toString(dur) + "s ago";

                    } else if (dur >= 60 && dur < 3600) {
                        temptime=Long.toString(dur / 60) + "m ago";
                    } else if (dur >= 3600 && dur < 3600 * 24) {
                        temptime= Long.toString(dur / (60 * 60)) + "h ago";
                    } else if (dur >= 3600 * 24) {
                        temptime=Long.toString(dur / (60 * 60 * 24)) + "d ago";
                    }
                    final Product product = new Product(SabkaResultGuardian.getString("webTitle"),imageUrl,SabkaResultGuardian.getString("sectionName"),temptime,id);
                    if (checkFavoriteItem(product)) {
                        bookmark.setImageResource(R.drawable.baseline_bookmark_black_18dp);
                    } else {
                        bookmark.setImageResource(R.drawable.baseline_bookmark_border_black_18dp);
                    }

                    bookmark.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (! checkFavoriteItem(product)) {
                                WeatherController.sharedPreference.addFavorite(getApplicationContext(), product);
                                bookmark.setImageResource(R.drawable.baseline_bookmark_black_18dp);

                                Toast.makeText(getApplicationContext(), tempo +"was added to bookmarks", Toast.LENGTH_SHORT).show();
                            } else {
                                WeatherController.sharedPreference.removeFavorite(getApplicationContext(), product);
                                bookmark.setImageResource(R.drawable.baseline_bookmark_border_black_18dp);

                                Toast.makeText(getApplicationContext(), tempo+" was removed from favourites", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    btntwitter.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String tweetMessage = "Check out this link https://www.guardian.com/"+id;
                            String hashtag = "CSCI571NewsSearch";
                            String url = "https://twitter.com/intent/tweet?text=" + tweetMessage + "&hashtags=" + hashtag;
                            Intent tweetIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(tweetIntent);
                        }
                    });
                } catch (JSONException ex) {
                    ex.printStackTrace();
                }

            }
        });
    }
    public boolean checkFavoriteItem(Product checkProduct) {
        boolean check = false;
        List<Product> favorites = WeatherController.sharedPreference.getFavorites(getApplicationContext());
        if (favorites != null) {
            for (Product product : favorites) {
                if (product.equals(checkProduct)) {
                    check = true;
                    break;
                }
            }
        }
        return check;
    }
}
