package cn.foxnickel.fuckshanxun;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.foxnickel.fuckshanxun.receiver.SmsReceiver;

public class MainActivity extends AppCompatActivity {

    private Button mBtGetPass;
    private TextView mTvPass;
    private TextView mTvTime;
    private static final int REQUEST_PERMISSIONS = 1;
    private final String TAG = getClass().getSimpleName();
    private SmsReceiver mSmsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtGetPass = (Button) findViewById(R.id.bt_get_pass);
        mTvPass = (TextView) findViewById(R.id.tv_pass);
        mTvTime = (TextView) findViewById(R.id.tv_time);

        SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences_pass",MODE_PRIVATE);
        mTvPass.setText(sharedPreferences.getString("pass"," "));
        mTvTime.setText(sharedPreferences.getString("time"," "));

        IntentFilter receiveFilter = new IntentFilter();
        receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        receiveFilter.setPriority(1000);
        mSmsReceiver = new SmsReceiver(mTvPass,mTvTime);
        registerReceiver(mSmsReceiver, receiveFilter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                sendSMS();
            } else {
                requestPermission(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE);
            }
        } else {
            sendSMS();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission(String... permissions) {
        List<String> permissionList = new ArrayList<>();//要申请的权限列表
        /**
         * 判断权限是否已经拥有，若未拥有则添加到List中
         */
        for (String permission : permissions) {
            switch (checkSelfPermission(permission)) {
                case PackageManager.PERMISSION_GRANTED:
                    Log.i(TAG, "onCreate: Granted...");
                    break;
                case PackageManager.PERMISSION_DENIED:
                    Log.i(TAG, "onCreate: Denied...");
                    permissionList.add(permission);
                    break;
            }
        }
        //申请未拥有的权限
        if (!permissionList.isEmpty()) {
            requestPermissions(permissionList.toArray(new String[permissionList.size()]), REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendSMS();
        } else {
            Toast.makeText(this, "请授予软件发送短信的权限，否则将无法使用", Toast.LENGTH_LONG).show();
        }
    }

    private void sendSMS() {
        mBtGetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "获取密码中...", Toast.LENGTH_SHORT).show();
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage("106593005", null, "mm", null, null);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSmsReceiver);
    }
}
