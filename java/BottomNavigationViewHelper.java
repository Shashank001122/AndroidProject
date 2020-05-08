package com.londonappbrewery.climapm.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.londonappbrewery.climapm.Bookmark;
import com.londonappbrewery.climapm.Newssari.News;
import com.londonappbrewery.climapm.R;
import com.londonappbrewery.climapm.Up;
import com.londonappbrewery.climapm.home.WeatherController;

/**
 * Created by User on 5/28/2017.
 */

public class BottomNavigationViewHelper {

    private static final String TAG = "BottomNavigationViewHel";

    public static void enableNavigation(final Context context, BottomNavigationView view){
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){

                    case R.id.home:
                        Intent intent1 = new Intent(context, WeatherController.class);//ACTIVITY_NUM = 0
                        context.startActivity(intent1);
                        ((Activity) context).finish();
                        break;

                    case R.id.news:
                        Intent intent2  = new Intent(context, News.class);//ACTIVITY_NUM = 1
                        context.startActivity(intent2);
                        ((Activity) context).finish();
                        break;

                    case R.id.Up:
                        Intent intent3 = new Intent(context, Up.class);//ACTIVITY_NUM = 2
                        context.startActivity(intent3);
                        ((Activity) context).finish();
                        break;

                    case R.id.bookmark:
                        Intent intent4 = new Intent(context, Bookmark.class);//ACTIVITY_NUM = 3
                        context.startActivity(intent4);
                        ((Activity) context).finish();
                        break;

                }


                return false;
            }
        });
    }
}