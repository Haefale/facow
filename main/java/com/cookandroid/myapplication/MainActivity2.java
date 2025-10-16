package com.cookandroid.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.JsPromptResult;
import android.webkit.WebView;
import android.webkit.WebSettings;
// 1. 필요한 클래스 import
import android.webkit.WebChromeClient;
import android.webkit.JsResult;
import android.webkit.WebViewClient; // (선택 사항이지만 안전을 위해 추가)
import android.content.Context;
import android.app.AlertDialog;
import android.widget.EditText;
import android.content.DialogInterface;

public class MainActivity2 extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2); // activity_main.xml 레이아웃 연결

        // 1. XML 레이아웃에서 WebView 객체를 찾습니다.
        webView = (WebView) findViewById(R.id.smart_farm_webview);

        // 2. JavaScript를 활성화합니다.
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true); // 로컬 저장소 활성화

        // 3. WebChromeClient 설정: JS 다이얼로그(alert, prompt, confirm) 처리를 위해 필수!
        webView.setWebChromeClient(new MyWebChromeClient()); // 새로 정의한 클래스 설정

        // 4. assets 폴더에 있는 HTML 파일을 웹 뷰에 로드합니다.
        webView.loadUrl("file:///android_asset/smartfarm.html");
    }

    // 5. WebViewClient 설정 (선택 사항: 페이지 이동을 웹 뷰 내에서 처리)
    // webView.setWebViewClient(new WebViewClient()); // 이 코드를 onCreate에 추가하면 좋아요.

    // 뒤로가기 버튼 처리 (기존 코드 유지)
    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    // 6. WebChromeClient를 상속받는 내부 클래스 정의
    public class MyWebChromeClient extends WebChromeClient {

        // alert() 대화 상자 처리
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            new AlertDialog.Builder(view.getContext())
                    .setTitle("알림")
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new AlertDialog.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }

        // prompt() 대화 상자 처리 (⭐ 이 부분이 핵심이에요!)
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
            // 입력 필드를 담을 EditText 생성
            final EditText input = new EditText(view.getContext());
            // 기본값 설정
            input.setText(defaultValue);

            new AlertDialog.Builder(view.getContext())
                    .setTitle("입력") // 대화 상자 제목 (예: "새로운 농장 이름 입력")
                    .setMessage(message) // smartfarm.html의 prompt() 함수 메시지 (예: "새로운 농장 이름을 입력하세요:")
                    .setView(input)
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // 확인 버튼 클릭 시 입력된 값을 JavaScript로 전달
                            result.confirm(input.getText().toString());
                        }
                    })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // 취소 버튼 클릭 시 null 값을 JavaScript로 전달 (prompt()의 취소와 동일)
                            result.cancel();
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
            return true;
        }

        // confirm() 대화 상자 처리 (필요하다면 이 코드를 추가할 수 있어요)
        /*
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
            new AlertDialog.Builder(view.getContext())
                .setTitle("확인")
                .setMessage(message)
                .setPositiveButton("확인", (dialog, which) -> result.confirm())
                .setNegativeButton("취소", (dialog, which) -> result.cancel())
                .create()
                .show();
            return true;
        }
        */
    }
}