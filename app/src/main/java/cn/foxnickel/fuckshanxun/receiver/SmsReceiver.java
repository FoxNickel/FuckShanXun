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

public class SmsReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getSimpleName();
    private TextView mTextView;

    public SmsReceiver(TextView textView) {
        mTextView = textView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle bundle = intent.getExtras();
        Object[] pdus = (Object[]) bundle.get("pdus");
        SmsMessage[] messages = new SmsMessage[pdus.length];
        for (int i = 0; i < messages.length; i++) {
            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
        }
        String address = messages[0].getOriginatingAddress();
        StringBuilder content = new StringBuilder();
        for (SmsMessage message : messages) {
            content.append(message.getMessageBody());
        }

        if (!TextUtils.isEmpty(content)) {

            Log.i(TAG, content.toString());

            String pass = content.substring(18, 24);
            Log.i(TAG, "onReceive: " + pass);

            /*记录密码*/
            SharedPreferences sharedPreferences = context.getSharedPreferences("shared_preferences_pass", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("pass", pass);
            editor.apply();

            mTextView.setText(content.substring(18, 24));
        } else {
            Toast.makeText(context, "短信为空", Toast.LENGTH_SHORT).show();
        }
    }
}
