package cn.foxnickel.fuckshanxun.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author NickelFox
 */
public class SmsReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getSimpleName();
    private final TextView mTvPass;
    private final TextView mTvTime;

    public SmsReceiver(TextView tvPass, TextView tvTime) {
        mTvPass = tvPass;
        mTvTime = tvTime;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        /**
         * 获取短信内容
         */
        Bundle bundle = intent.getExtras();
        Object[] pdus = new Object[0];
        if (bundle != null) {
            pdus = (Object[]) bundle.get("pdus");
        }
        SmsMessage[] messages = new SmsMessage[pdus != null ? pdus.length : 0];
        for (int i = 0; i < messages.length; i++) {
            if (pdus != null) {
                messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
            }
        }

        StringBuilder content = new StringBuilder();
        for (SmsMessage message : messages) {
            content.append(message.getMessageBody());
        }


        /**
         * 将短信内容显示到界面
         */
        if (!TextUtils.isEmpty(content)) {

            Log.i(TAG, content.toString());

            String pass = content.substring(18, 24);
            Log.i(TAG, "onReceive: " + pass);

            String time = content.substring(25,51);
            Log.i(TAG, "onReceive: time "+time);

            /*记录密码*/
            SharedPreferences sharedPreferences = context.getSharedPreferences("shared_preferences_pass", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("pass", pass);
            editor.putString("time",time);
            editor.apply();

            mTvPass.setText(pass);
            mTvTime.setText(time);
        } else {
            Toast.makeText(context, "短信为空", Toast.LENGTH_SHORT).show();
        }
    }
}
