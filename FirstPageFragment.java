package com.cookandroid.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebChromeClient;
import android.webkit.JsResult;
import android.webkit.JsPromptResult;
import android.webkit.WebViewClient;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FirstPageFragment extends Fragment {

    private WebView webView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 1. fragment_page_1 레이아웃 인플레이트
        // 이 레이아웃(R.layout.fragment_page_1)에 <WebView android:id="@+id/smart_farm_webview">가 정의되어 있어야 합니다.
        View view = inflater.inflate(R.layout.fragment_page_1, container, false);

        // 2. 인플레이트된 View에서 WebView 객체를 찾습니다.
        webView = view.findViewById(R.id.smart_farm_webview);

        // 3. JavaScript를 활성화합니다.
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true); // 로컬 저장소 활성화

        // 4. WebChromeClient 설정: JS 다이얼로그 처리를 위해 필수!
        webView.setWebChromeClient(new MyWebChromeClient());

        // 5. WebViewClient 설정 (선택 사항: 페이지 이동을 웹 뷰 내에서 처리)
        webView.setWebViewClient(new WebViewClient());

        // 6. assets 폴더에 있는 HTML 파일을 웹 뷰에 로드합니다.
        // 파일 경로는 프로젝트에 맞게 "file:///android_asset/smartfarm.html"로 유지합니다.
        webView.loadUrl("file:///android_asset/smartfarm.html");

        return view;
    }

    // 7. WebViewClient 설정 (선택 사항: 페이지 이동을 웹 뷰 내에서 처리)
    // 이 메서드는 Fragment에서는 필요 없으며, 위 onCreateView에 포함했습니다.
    // webView.setWebViewClient(new WebViewClient());

    // 8. 뒤로가기 버튼 처리 로직 (Activity에서 호출할 수 있도록 메서드 정의)
    /**
     * 웹뷰가 뒤로갈 수 있는지 확인하고, 가능하다면 뒤로가기를 실행합니다.
     * Activity의 onBackPressed()에서 호출되어야 합니다.
     */
    public boolean canGoBackInWebView() {
        return webView != null && webView.canGoBack();
    }

    public void goBackInWebView() {
        if (webView != null) {
            webView.goBack();
        }
    }


    // 9. WebChromeClient를 상속받는 내부 클래스 정의 (기존 코드와 동일)
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

        // prompt() 대화 상자 처리 (이 부분이 핵심입니다!)
        @Override
        public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final JsPromptResult result) {
            // 입력 필드를 담을 EditText 생성
            final EditText input = new EditText(view.getContext());
            // 기본값 설정
            input.setText(defaultValue);

            new AlertDialog.Builder(view.getContext())
                    .setTitle("입력")
                    .setMessage(message)
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
    }
}