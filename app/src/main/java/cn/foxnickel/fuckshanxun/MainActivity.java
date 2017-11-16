package cn.foxnickel.fuckshanxun;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import cn.foxnickel.fuckshanxun.receiver.SmsReceiver;

public class MainActivity extends Activity {

    private static final int REQUEST_PERMISSIONS = 1;
    private final String TAG = getClass().getSimpleName();
    private Button mBtGetPass;
    private SharedPreferences sharedPreferences;
    private SmsReceiver mSmsReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_second);

        setTransparentStatusBar();

        sharedPreferences = getSharedPreferences("shared_preferences_pass", MODE_PRIVATE);

        mBtGetPass = (Button) findViewById(R.id.bt_get_pass);
        TextView tvPass = (TextView) findViewById(R.id.tv_pass);
        TextView tvTime = (TextView) findViewById(R.id.tv_time);
        TextView tvVersion = (TextView) findViewById(R.id.tv_version);

        /*设置界面显示的版本号*/
        try {
            String version = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            String versionInfo = "版本：" + version;
            tvVersion.setText(versionInfo);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        /*获取存储的上次获取的数据*/
        tvPass.setText(sharedPreferences.getString("pass", " "));
        tvTime.setText(sharedPreferences.getString("time", " "));

        /*打开app时注册短信监听器*/
        IntentFilter receiveFilter = new IntentFilter();
        receiveFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        receiveFilter.setPriority(1000);
        mSmsReceiver = new SmsReceiver(tvPass, tvTime);
        registerReceiver(mSmsReceiver, receiveFilter);

        /*运行时权限处理*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                sendSMS();
            } else {
                /*小米手机手动开启权限指引*/
                if ("Xiaomi".equals(Build.MANUFACTURER)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("提示");
                    builder.setCancelable(false);
                    builder.setMessage("        检测到您的系统是MIUI系统，需要手动开启接收和发送短信的权限,否则将有可能无法使用。\n\n" +
                            "点击确定按钮将跳转到权限控制页面。");
                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            getAppDetailSettingIntent();
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

    private void setTransparentStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
    }

    /**
     * 打开应用权限管理页面（小米手机会用到）
     */
    private void getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        localIntent.setData(Uri.fromParts("package", getPackageName(), null));
        startActivity(localIntent);
    }

    /**
     * 请求权限函数
     *
     * @param permissions 要请求的权限
     */
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "onRequestPermissionsResult: card: " + sharedPreferences.getInt("card", 0));
            sendSMS();
        } else {
            Toast.makeText(this, "请授予软件发送短信的权限，否则将无法使用", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 发送获取密码的短信
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private void sendSMS() {
        mBtGetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getInt("card", 0) == 0) {
                    Log.i(TAG, "onClick: card1");
                    Toast.makeText(MainActivity.this, "获取密码中...", Toast.LENGTH_SHORT).show();
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage("+86106593005", null, "mm", null, null);
                } else {
                    Log.i(TAG, "onClick: card2");
                    TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                    Toast.makeText(MainActivity.this, "获取密码中...", Toast.LENGTH_SHORT).show();
                    Class<?> telephonyClass;
                    if (telephonyManager != null) {
                        telephonyClass = telephonyManager.getClass();
                        try {
                            Method method = telephonyClass.getMethod("getSubscriberId", int.class);
                            String subIds[] = new String[10];
                            int sim2Id = 0;
                            for (int i = 0; i < 10; i++) {
                                String subId = (String) method.invoke(telephonyManager, i);
                                subIds[i] = subId;
                            }
                            if (!subIds[0].equals(subIds[1]) && !subIds[0].equals(subIds[2])) {
                                sim2Id = 0;
                            } else {
                                for (int i = 1; i < 10; i++) {
                                    if (!subIds[i].equals(subIds[0])) {
                                        sim2Id = i;
                                    }
                                }
                            }
                            Log.i(TAG, "sendSMS2: sim2Id: " + sim2Id);

                            SmsManager smsManager = SmsManager.getSmsManagerForSubscriptionId(sim2Id);
                            smsManager.sendTextMessage("+86106593005", null, "mm", null, null);

                        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mSmsReceiver);//取消注册的广播监听器
    }

    public void setSimCard(View view) {
        final String[] cards = new String[]{"SIM1", "SIM2"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("双卡设置")
                .setSingleChoiceItems(cards, sharedPreferences.getInt("card", 0), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("card", which);
                        editor.apply();
                    }
                });
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(MainActivity.this, "已保存", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }
}
