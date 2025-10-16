package com.cookandroid.myapplication;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import android.content.Context;

public class AdManager {

    public static void loadBannerAd(Context context, AdView mAdView) {
        // 모바일 광고 SDK 초기화
        MobileAds.initialize(context, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                // 초기화 완료 시 필요한 작업이 있다면 여기에 추가
            }
        });

        // 광고 요청 생성
        AdRequest adRequest = new AdRequest.Builder().build();

        // 광고 로드
        if (mAdView != null) {
            mAdView.loadAd(adRequest);
        }
    }
}