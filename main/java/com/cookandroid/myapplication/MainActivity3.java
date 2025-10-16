package com.cookandroid.myapplication;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.cookandroid.myapplication.R;
import com.google.android.gms.ads.AdView;


public class MainActivity3 extends AppCompatActivity {
    private AdView mAdView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        PagerAdapter pagerAdapter = new PagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);



        // XML 레이아웃의 AdView를 찾기
        mAdView = findViewById(R.id.adView);
        // AdManager 클래스의 메소드 호출
        AdManager.loadBannerAd(this, mAdView);
    }


}