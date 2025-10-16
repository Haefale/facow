package com.cookandroid.myapplication;


import android.Manifest;
import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;
import android.os.Build;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity5 extends AppCompatActivity {
    private static final String TAG = "MainActivity5";

    // UI 요소
    private TextView mTvBluetoothStatus;
    private TextView mTvReceiveData;
    private EditText mEtSendData; // EditText로 변경
    private Button mBtnBluetoothOn;
    private Button mBtnBluetoothOff;
    private Button mBtnConnect;
    private Button mBtnSendData;

    // 블루투스 관련
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> mPairedDevices;
    private BluetoothDevice mBluetoothDevice;
    private ConnectedBluetoothThread mThreadConnectedBluetooth;
    private BluetoothSocket mBluetoothSocket;

    // 상수
    private final static int BT_REQUEST_ENABLE = 10;
    private final static int BT_MESSAGE_READ = 2;
    private final static int BT_CONNECTING_STATUS = 3;
    private final static int REQUEST_CODE_BLUETOOTH_PERMISSIONS = 20; // 권한 요청 코드
    private final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // UI 업데이트를 위한 핸들러
    private Handler mBluetoothHandler;

    // ActivityResultLauncher를 사용하여 onActivityResult 대체
    private final ActivityResultLauncher<Intent> startBluetoothIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_LONG).show();
                    mTvBluetoothStatus.setText("활성화");
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Toast.makeText(getApplicationContext(), "활성화 취소", Toast.LENGTH_LONG).show();
                    mTvBluetoothStatus.setText("비활성화");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);

        // findViewById 한 번에 처리 및 타입 수정
        mTvBluetoothStatus = findViewById(R.id.tvBluetoothStatus);
        mTvReceiveData = findViewById(R.id.tvReceiveData);
        mEtSendData = findViewById(R.id.tvSendData); // EditText로 변경
        mBtnBluetoothOn = findViewById(R.id.btnBluetoothOn);
        mBtnBluetoothOff = findViewById(R.id.btnBluetoothOff);
        mBtnConnect = findViewById(R.id.btnConnect);
        mBtnSendData = findViewById(R.id.btnSendData);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // ------------------ 리스너 설정 ------------------
        mBtnBluetoothOn.setOnClickListener(v -> checkBluetoothPermissions(true)); // 권한 체크 후 활성화
        mBtnBluetoothOff.setOnClickListener(v -> bluetoothOff());
        mBtnConnect.setOnClickListener(v -> checkBluetoothPermissions(false)); // 권한 체크 후 페어링 리스트
        mBtnSendData.setOnClickListener(v -> sendData());



        // ------------------ Handler 초기화 ------------------
        mBluetoothHandler = new Handler(Looper.getMainLooper()){
            public void handleMessage(@NonNull android.os.Message msg){
                if(msg.what == BT_MESSAGE_READ){
                    // 데이터 수신 처리
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, 0, msg.arg1, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    mTvReceiveData.setText(readMessage);
                } else if (msg.what == BT_CONNECTING_STATUS) {
                    // 연결 상태 변경 처리 (성공/실패)
                    if(msg.arg1 == 1) {
                        mTvBluetoothStatus.setText("연결됨");
                        Toast.makeText(getApplicationContext(), "블루투스 연결 성공!", Toast.LENGTH_SHORT).show();
                    } else {
                        mTvBluetoothStatus.setText("연결 실패");
                        Toast.makeText(getApplicationContext(), "블루투스 연결 실패!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
    }

    // ==========================================================
    // 1. 권한 확인 및 요청 (최신 API 대응)
    // ==========================================================

    private void checkBluetoothPermissions(boolean turnOn) {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 이상 (스캔, 연결 권한 필요)
            permissions = new String[]{
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_SCAN
            };
        } else {
            // 🔥🔥🔥 이 부분이 수정되었습니다! (API 30 이하) 🔥🔥🔥
            // Android 11 이하: FINE Location 요청 시 COARSE Location도 함께 요청해야 합니다.
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION // <-- 이 항목을 추가해야 합니다!
            };
        }

        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (!allGranted) {
            // 권한 요청
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permissions, REQUEST_CODE_BLUETOOTH_PERMISSIONS);
            }
        } else {
            // 권한이 모두 부여됨
            if (turnOn) {
                bluetoothOn();
            } else {
                listPairedDevices();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_BLUETOOTH_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                // 권한이 부여된 후 다음 단계 (활성화 또는 연결 리스트)를 수행합니다.
                // 이 예시에서는 리스너에서 어떤 버튼이 눌렸는지 알 수 없으므로,
                // 권한 부여 후 사용자가 다시 버튼을 누르게 하거나, 더 복잡한 플래그 로직을 사용해야 합니다.
                // 여기서는 간단히 블루투스 활성화/비활성화 상태를 확인하도록 합니다.
                if (mBluetoothAdapter.isEnabled()) {
                    listPairedDevices(); // 활성화 되어 있으면 연결 목록 보여줌
                } else {
                    bluetoothOn(); // 활성화 안되어 있으면 활성화 요청
                }
            } else {
                Toast.makeText(this, "블루투스 기능을 사용할 권한이 필요합니다.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // ==========================================================
    // 2. 블루투스 ON/OFF
    // ==========================================================

    private void bluetoothOn() {
        if(mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
        }
        else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어 있습니다.", Toast.LENGTH_LONG).show();
                mTvBluetoothStatus.setText("활성화");
            }
            else {
                // mBluetoothAdapter.ACTION_REQUEST_ENABLE Intent 실행
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startBluetoothIntent.launch(intentBluetoothEnable); // ActivityResultLauncher 사용
            }
        }
    }

    private void bluetoothOff() {
        // BLUETOOTH_CONNECT 권한 체크는 이미 checkBluetoothPermissions에서 처리되었다고 가정
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "블루투스 제어 권한이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.disable();
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되었습니다.", Toast.LENGTH_SHORT).show();
            mTvBluetoothStatus.setText("비활성화");
        }
        else {
            Toast.makeText(getApplicationContext(), "블루투스가 이미 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // ==========================================================
    // 3. 연결 관련 로직
    // ==========================================================

    private void listPairedDevices() {
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // BLUETOOTH_CONNECT 권한 체크 (API 31+ 대응)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED)
        {
            Toast.makeText(this, "페어링된 장치 목록 조회 권한이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        mPairedDevices = mBluetoothAdapter.getBondedDevices();

        if (mPairedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("장치 선택");

        final List<String> listPairedDevices = new ArrayList<>();
        for (BluetoothDevice device : mPairedDevices) {
            listPairedDevices.add(device.getName() != null ? device.getName() : "Unknown Device");
        }

        final CharSequence[] items = listPairedDevices.toArray(new CharSequence[0]);

        builder.setItems(items, (dialog, item) -> connectSelectedDevice(items[item].toString()));
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void connectSelectedDevice(String selectedDeviceName) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "블루투스 연결 권한이 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // 기존 연결 스레드가 있다면 정리
        if (mThreadConnectedBluetooth != null) {
            mThreadConnectedBluetooth.cancel();
            mThreadConnectedBluetooth = null;
        }

        // 선택된 기기 찾기
        for(BluetoothDevice tempDevice : mPairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())) { // <--- getName() 호출 지점
                mBluetoothDevice = tempDevice;
                break;
            }
        }

        if (mBluetoothDevice == null) return;

        // 연결 스레드 시작 (메인 스레드에서 직접 connect() 호출 방지)
        new Thread(() -> {

            // ==========================================================
            // 🔥 핵심 수정: 스레드 내부에서 BLUETOOTH_CONNECT 권한 다시 확인 🔥
            // ==========================================================
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // 권한이 없으므로 연결 시도 중지 및 사용자에게 실패 알림
                    Log.e(TAG, "BLUETOOTH_CONNECT 권한 없음. 연결 중단.");
                    mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 0, -1).sendToTarget(); // 0: 실패
                    return;
                }
            }
            // ==========================================================

            try {
                // 이제 권한 체크를 통과했으므로 Lint 경고가 사라집니다.
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
                mBluetoothSocket.connect(); // <--- 블로킹 호출

                mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
                mThreadConnectedBluetooth.start();
                mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();

            } catch (IOException e) {
                Log.e(TAG, "블루투스 연결 중 오류 발생", e);
                mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 0, -1).sendToTarget(); // 0: 실패
                try {
                    if (mBluetoothSocket != null) mBluetoothSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "소켓 닫기 실패", closeException);
                }
            }
        }).start();
    }
    private void sendData() {
        if(mThreadConnectedBluetooth != null) {
            String data = mEtSendData.getText().toString(); // EditText 참조 변수명 변경 반영
            mThreadConnectedBluetooth.write(data);
            mEtSendData.setText("");
        } else {
            Toast.makeText(getApplicationContext(), "연결된 장치가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    // ==========================================================
    // 4. 데이터 송수신 스레드
    // ==========================================================

    private class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                // 소켓 해제 시 토스트는 액티비티 핸들러로 위임하는 것이 좋습니다.
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                // 연결이 끊어졌음을 핸들러로 알림
                mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 0, -1).sendToTarget();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    // **최적화:** available() 대신 read()를 사용하여 블로킹 방식으로 변경
                    // available() 사용 시 바이트가 없을 때 CPU 낭비 및 sleep() 필요
                    // read()는 데이터가 들어올 때까지 대기하여 더 효율적입니다.
                    bytes = mmInStream.read(buffer);

                    if (bytes > 0) {
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    // 연결이 끊어졌음을 액티비티 핸들러에 알림
                    mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 0, -1).sendToTarget();
                    break;
                }
            }
        }

        // 송신
        public void write(String str) {
            // BLUETOOTH_CONNECT 권한 체크는 액티비티에서 이미 처리되었다고 가정
            byte[] bytes = str.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                mBluetoothHandler.post(() ->
                        Toast.makeText(getApplicationContext(), "데이터 전송 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show());
            }
        }

        // 연결 해제
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                // Log로 기록만 하고 UI에는 별도 알림을 주지 않습니다.
                Log.e(TAG, "소켓 해제 중 오류 발생", e);
            }
        }
    }

    // ==========================================================
    // 5. 생명 주기 관리
    // ==========================================================

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 액티비티 종료 시 소켓과 스레드를 안전하게 해제
        if (mThreadConnectedBluetooth != null) {
            mThreadConnectedBluetooth.cancel();
        }
    }
}