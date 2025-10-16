package com.cookandroid.myapplication;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity4 extends AppCompatActivity {

    private View flipContainer;
    private View dogFace;
    private View catFace;

    private float tiltSensitivityY = 0.2f;
    private float tiltSensitivityX = -0.2f;
    private final float maxRotation = 180.0f;

    private float initialRotationY = 0.0f;
    private float initialRotationX = 0.0f;

    private Handler handler = new Handler();
    private Runnable resetRunnable = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        flipContainer = findViewById(R.id.flip_container);
        dogFace = findViewById(R.id.dog_face);
        catFace = findViewById(R.id.cat_face);

        // [핵심] 3D 회전을 위해 카메라 거리를 설정해야 원근감이 생깁니다.
        // 숫자가 클수록 원근감이 약해집니다.
        float density = getResources().getDisplayMetrics().density;
        flipContainer.setCameraDistance(8000 * density);

        // [핵심] 터치 리스너 설정
        flipContainer.setOnTouchListener(new View.OnTouchListener() {
            private float startX, startY;
            private float lastRotationY, lastRotationX;
            private int touchSlop = 5; // 터치 이동 최소 거리

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                // 마우스 떠나기 지연 타이머 취소
                if (resetRunnable != null) {
                    handler.removeCallbacks(resetRunnable);
                    resetRunnable = null;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        startY = event.getY();
                        lastRotationY = flipContainer.getRotationY();
                        lastRotationX = flipContainer.getRotationX();
                        // 애니메이션 중간이었다면 취소
                        flipContainer.clearAnimation();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float deltaX = event.getX() - startX;
                        float deltaY = event.getY() - startY;

                        // 이동량이 너무 작으면 처리 안함 (클릭과의 구별)
                        if (Math.abs(deltaX) < touchSlop && Math.abs(deltaY) < touchSlop) {
                            return true;
                        }

                        // 1. 회전 각도 계산 (웹 코드의 로직과 유사)
                        // 컨테이너 크기의 절반을 기준으로 상대적인 드래그 거리를 사용해야 일관된 회전이 됩니다.
                        float rotationY = deltaX * tiltSensitivityY;
                        float rotationX = deltaY * tiltSensitivityX;

                        // 현재 회전 값에 누적합니다.
                        rotationY += lastRotationY;
                        rotationX += lastRotationX;

                        // 2. -180도 ~ 180도 사이로 제한 (웹 코드의 maxRotation과 동일)
                        rotationY = Math.max(-maxRotation, Math.min(maxRotation, rotationY));
                        rotationX = Math.max(-maxRotation, Math.min(maxRotation, rotationX));

                        // 3. 뷰 회전 적용
                        flipContainer.setRotationY(rotationY);
                        flipContainer.setRotationX(rotationX);

                        // 4. [핵심: 앞/뒷면 가시성 조절] 회전 값에 따라 이미지 View 가시성 토글 (웹의 backface-visibility 역할)
                        updateFaceVisibility(rotationY);

                        // 5. [복귀 각도 저장] 90도를 넘으면 180도로 복귀하도록 목표 각도 설정
                        if (Math.abs(rotationY) > 90) {
                            initialRotationY = rotationY > 0 ? 180.0f : -180.0f;
                        } else {
                            initialRotationY = 0.0f;
                        }

                        if (Math.abs(rotationX) > 90) {
                            initialRotationX = rotationX > 0 ? 180.0f : -180.0f;
                        } else {
                            initialRotationX = 0.0f;
                        }

                        return true;

                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        // 1.5초 대기 후 복귀 로직 실행 (웹 코드의 mouseleave와 유사)
                        resetRunnable = () -> resetFlipEffect();
                        handler.postDelayed(resetRunnable, 1500);
                        return true;
                }
                return false;
            }
        });
    }

    // Y축 회전 각도에 따라 앞면/뒷면 가시성 조절 함수
    private void updateFaceVisibility(float rotationY) {
        // rotationY가 -90 ~ 90도 사이면 앞면(강아지) 보임, 아니면 뒷면(고양이) 보임
        if (Math.abs(rotationY) <= 90) {
            dogFace.setVisibility(View.VISIBLE);
            catFace.setVisibility(View.INVISIBLE);
        } else {
            dogFace.setVisibility(View.INVISIBLE);
            catFace.setVisibility(View.VISIBLE);
        }
    }

    // 카드 초기화 (진자 운동 유발)
    private void resetFlipEffect() {
        // Y축과 X축 복귀 목표 각도로 애니메이션
        PropertyValuesHolder pvhRotationY = PropertyValuesHolder.ofFloat("rotationY", initialRotationY);
        PropertyValuesHolder pvhRotationX = PropertyValuesHolder.ofFloat("rotationX", initialRotationX);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(flipContainer, pvhRotationY, pvhRotationX);
        animator.setDuration(2000); // 복귀 시간 2.0초

        // [핵심] 웹의 cubic-bezier(0.68, -0.55, 0.265, 2.05)와 유사한 진자 운동 Interploator
        // 안드로이드에는 내장된 기능이 없으므로 직접 구현해야 합니다.
        // 여기서는 가장 가까운 기본 Interpolator(Overshoot)를 사용하거나 Custom Interpolator를 구현해야 합니다.
        animator.setInterpolator(new TimeInterpolator() {
            // Cubic Bezier는 안드로이드에서 직접 지원하지 않아, Custom Interpolator가 필요합니다.
            // 여기서는 단순함을 위해 OvershootInterpolator를 가정하고, Custom Interpolator 구현을 생략합니다.
            // OvershootInterpolator는 뒤로 살짝 튕기는 효과를 줍니다.
            @Override
            public float getInterpolation(float input) {
                // 이 부분을 cubic-bezier(0.68, -0.55, 0.265, 2.05)에 해당하는
                // Custom Interpolator의 로직으로 대체해야 합니다.
                // 여기서는 예시로 Overshoot와 유사한 효과를 내는 간단한 식을 사용합니다.
                return (float) (1 - Math.pow(1 - input, 4) * Math.cos(input * Math.PI * 2.5)); // 예시
            }
        });

        animator.start();

        // 애니메이션이 끝나면 최종 각도에 따라 가시성을 다시 확인합니다.
        // 이 부분은 `animator.addListener(..)`를 사용하여 애니메이션 종료 시점에 처리해야 합니다.
        animator.addUpdateListener(animation -> {
            float currentRotationY = (float) animation.getAnimatedValue("rotationY");
            updateFaceVisibility(currentRotationY);
        });
    }
}