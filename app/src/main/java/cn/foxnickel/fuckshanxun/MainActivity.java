package cn.foxnickel.fuckshanxun;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
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
    private static final int REQUEST_PERMISSIONS = 1;
    private final String TAG = getClass().getSimpleName();
    private SmsReceiver mSmsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtGetPass = (Button) findViewById(R.id.bt_get_pass);
        TextView tvPass = (TextView) findViewById(R.id.tv_pass);
        TextView tvTime = (TextView) findViewById(R.id.tv_time);

        SharedPreferences sharedPreferences = getSharedPreferences("shared_preferences_pass", MODE_PRIVATE);
        tvPass.setText(sharedPreferences.getString("pass", " "));
        tvTime.setText(sharedPreferences.getString("time", " "));

        IntentFilter receiveFilter = new IntentFilter();
        receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        receiveFilter.setPriority(1000);
        mSmsReceiver = new SmsReceiver(tvPass, tvTime);
        registerReceiver(mSmsReceiver, receiveFilter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                sendSMS();
            } else {
                if ("Xiaomi".equals(Build.MANUFACTURER)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("提示");
                    builder.setCancelable(false);
                    builder.setMessage("        检测到您的系统是MIUI系统，需要手动开启接收和发送短信的权限,否则将有可能无法使用。\n\n" +
                            "点击确定按钮将跳转到权限控制页面。");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getAppDetailSettingIntent(MainActivity.this);
                        }
                    });
                    builder.show();
                }
                requestPermission(Manifest.permission.SEND_SMS, Manifest.permission.READ_PHONE_STATE);
            }
        } else {
            sendSMS();
        }
    }

    /**
     * 打开应用权限管理页面
     *
     * @param context
     */
    private void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(localIntent);
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
