package com.londonappbrewery.climapm;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;

import java.util.ArrayList;

public class SearchResult extends AppCompatActivity {
    private ArrayList<String> mNames = new ArrayList<>();
    private ArrayList<String> mImageUrls = new ArrayList<>();
    private ArrayList<String> mArticleId=new ArrayList<>();
    private  ArrayList<String> mSectionNames=new ArrayList<>();
    private  ArrayList<String> mPublication=new ArrayList<>();
    private ImageView backbutton;
    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        setTheme(R.style.detailTheme);
        //ImageView backbutton=findViewById(R.id.backbutton);
        try {
            getIncomingIntent();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ImageView backbutton=findViewById(R.id.backbutton);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getIncomingIntent() throws JSONException {
        if(getIntent().hasExtra("mNames") && getIntent().hasExtra("mImages") && getIntent().hasExtra("mSectionNames")
        && getIntent().hasExtra("mPublication") && getIntent().hasExtra("mArticleId")){
            mNames=getIntent().getStringArrayListExtra("mNames");
            mImageUrls=getIntent().getStringArrayListExtra("mImages");
            mPublication=getIntent().getStringArrayListExtra("mPublication");
            mSectionNames=getIntent().getStringArrayListExtra("mSectionNames");
            mArticleId=getIntent().getStringArrayListExtra("mArticleId");

            initRecyclerView();
        };
    }
    private void initRecyclerView(){
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(this,mNames,mImageUrls,mSectionNames,mPublication,mArticleId);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
