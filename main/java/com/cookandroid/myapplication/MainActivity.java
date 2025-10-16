package com.cookandroid.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.ads.AdView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;

public class MainActivity extends AppCompatActivity {

    private AdView mAdView;

    private BluetoothAdapter bluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Your XML layout file name

        ImageButton imageButton2 = findViewById(R.id.imageButton2);
        ImageButton imageButton3 = findViewById(R.id.imageButton3);
        ImageButton imageButton4 = findViewById(R.id.imageButton4);
        ImageButton imageButton5 = findViewById(R.id.imageButton5);







        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다음 페이지(액티비티)로 이동
                Intent intent = new Intent(MainActivity.this, MainActivity2.class); // NextActivity.class를 원하는 다음 페이지 클래스 이름으로 바꾸세요.
                startActivity(intent);
            }
        });
        imageButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다음 페이지(액티비티)로 이동
                Intent intent = new Intent(MainActivity.this, MainActivity3.class); // NextActivity.class를 원하는 다음 페이지 클래스 이름으로 바꾸세요.
                startActivity(intent);
            }
        });
        imageButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다음 페이지(액티비티)로 이동
                Intent intent = new Intent(MainActivity.this, MainActivity4.class); // NextActivity.class를 원하는 다음 페이지 클래스 이름으로 바꾸세요.
                startActivity(intent);
            }
        });
        imageButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 다음 페이지(액티비티)로 이동
                Intent intent = new Intent(MainActivity.this, MainActivity5.class); // NextActivity.class를 원하는 다음 페이지 클래스 이름으로 바꾸세요.
                startActivity(intent);
            }
        });


            // XML 레이아웃의 AdView를 찾기
            mAdView = findViewById(R.id.adView);

            // AdManager 클래스의 메소드 호출
            AdManager.loadBannerAd(this, mAdView);

    }

    private void initializeBluetooth() {
        // BluetoothManager를 통해 BluetoothAdapter를 가져옵니다.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // 기기가 블루투스를 지원하는지 확인
        if (bluetoothAdapter == null) {
            // 블루투스를 지원하지 않는 기기입니다. 사용자에게 알림.
            // Toast.makeText(this, "이 기기는 블루투스를 지원하지 않습니다.", Toast.LENGTH_LONG).show();
            finish(); // 앱 종료 또는 기능 비활성화
        }
    }
}