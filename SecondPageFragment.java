package com.cookandroid.myapplication;
// 실제 프로젝트 패키지 이름이 맞는지 확인해주세요!

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient; // WebChromeClient 사용을 위해 추가
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.ValueCallback; // 파일 선택 콜백을 위해 추가
import android.webkit.JsPromptResult; // JS 다이얼로그 처리가 필요하다면 추가
import android.webkit.JsResult; // JS 다이얼로그 처리가 필요하다면 추가
import android.widget.EditText; // JS prompt 처리가 필요하다면 추가

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

public class SecondPageFragment extends Fragment {

    private WebView webView;

    // 💡 파일 첨부 기능을 위한 변수 및 상수
    private static final int FILE_CHOOSER_REQUEST_CODE = 1;
    private ValueCallback<Uri[]> filePathCallback; // 웹뷰에 파일 경로를 전달할 콜백 객체

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // 1. fragment_page_2 레이아웃 인플레이트
        View view = inflater.inflate(R.layout.fragment_page_2, container, false);

        // 2. 인플레이트된 View에서 WebView 찾기
        webView = view.findViewById(R.id.webview);

        // 3. WebSettings 설정
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true); // 로컬 저장소 활성화

        // 4. WebChromeClient 설정 (JS 다이얼로그 및 파일 첨부 처리)
        // 💡 이 부분이 파일 첨부 버튼을 작동시키는 핵심입니다!
        webView.setWebChromeClient(new MyWebChromeClient());

        // 5. 외부 링크 클릭 시 앱 내부에서 열리도록 설정 (권장)
        webView.setWebViewClient(new WebViewClient());

        // 6. assets 폴더의 HTML 파일 로드
        webView.loadUrl("file:///android_asset/searchplant.html");

        return view;
    }

    // 7. 파일 선택 후 결과를 받는 메서드 (핵심 수정 부분)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 파일 선택 요청 코드인지 확인
        if (requestCode == FILE_CHOOSER_REQUEST_CODE) {

            // 콜백 객체가 유효한지 확인
            if (filePathCallback == null) return;

            Uri[] results = null;

            // 사용자가 파일을 선택했는지 확인
            if (resultCode == AppCompatActivity.RESULT_OK) {
                // 단일 파일 또는 여러 파일 처리
                if (data != null) {
                    if (data.getDataString() != null) {
                        // 단일 파일 선택
                        results = new Uri[]{Uri.parse(data.getDataString())};
                    } else if (data.getClipData() != null) {
                        // 여러 파일 선택
                        final int count = data.getClipData().getItemCount();
                        results = new Uri[count];
                        for (int i = 0; i < count; i++) {
                            results[i] = data.getClipData().getItemAt(i).getUri();
                        }
                    }
                }
            }

            // 웹뷰에 결과 전달
            filePathCallback.onReceiveValue(results);
            filePathCallback = null; // 콜백 사용 후 초기화
        }
    }

    // 8. 뒤로가기 버튼 처리 로직 (Activity에서 호출할 수 있도록 메서드 정의)
    public boolean canGoBack() {
        if (webView != null) {
            return webView.canGoBack();
        }
        return false;
    }

    public void goBack() {
        if (webView != null) {
            webView.goBack();
        }
    }


    // 9. WebChromeClient를 상속받는 내부 클래스 정의
    public class MyWebChromeClient extends WebChromeClient {

        // alert() 대화 상자 처리가 필요하다면 여기에 구현합니다.
        /*
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            // ... (Alert 다이얼로그 처리 코드) ...
            return true;
        }
        */

        // 💡 HTML의 <input type="file"> 태그를 처리하는 메서드 (핵심 수정 부분)
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {

            // 기존 콜백을 정리하고 새 콜백 저장
            if (SecondPageFragment.this.filePathCallback != null) {
                SecondPageFragment.this.filePathCallback.onReceiveValue(null);
            }
            SecondPageFragment.this.filePathCallback = filePathCallback;

            // 파일 선택 Intent 생성
            Intent intent = fileChooserParams.createIntent();
            try {
                // 파일 선택 화면 실행
                // 💡 Fragment의 startActivityForResult를 사용합니다.
                startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);
            } catch (ActivityNotFoundException e) {
                // 오류 발생 시 콜백에 null 전달 후 초기화
                SecondPageFragment.this.filePathCallback = null;
                return false;
            }
            return true;
        }
    }
}