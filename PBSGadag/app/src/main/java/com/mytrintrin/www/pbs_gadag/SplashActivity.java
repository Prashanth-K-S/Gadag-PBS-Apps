package com.mytrintrin.www.pbs_gadag;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class SplashActivity extends AppCompatActivity {

    private ImageView mSplashLogo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //To set full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash);

        mSplashLogo = findViewById(R.id.iv_splashlogo);

        //Image to load in all lower resolution devices
        Glide.with(this).load(R.drawable.trintrin).into(mSplashLogo);

        Thread timer = new Thread()
        {
          public  void run()
          {
              try {
                  sleep(3000);
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
              finally {
                    startActivity(new Intent(SplashActivity.this,GetStartedActivity.class));

              }
          }
        };
        timer.start();
    }

    //To finish activity after going to next activity
    @Override
    protected void onPause() {
        super.onPause();
        finish();
    }
}
