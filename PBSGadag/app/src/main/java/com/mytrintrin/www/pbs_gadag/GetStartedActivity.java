package com.mytrintrin.www.pbs_gadag;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class GetStartedActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageView mGetStartedLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);

        //Intializing views
        mToolbar = findViewById(R.id.getstartedtoolbar);
        setSupportActionBar(mToolbar);
        mGetStartedLogo = findViewById(R.id.iv_getstartedlogo);
        Glide.with(this).load(R.drawable.trintrin).into(mGetStartedLogo);

    }

    public void gotologin(View view)
    {
        startActivity(new Intent(GetStartedActivity.this,LoginActivity.class));
    }

    public void gotoregister(View view)
    {
        startActivity(new Intent(GetStartedActivity.this,RegisterActivity.class));
    }
}
