package com.example.usbupdatedemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.usbmonitor.Constant;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        intentFilter = new IntentFilter();
        intentFilter.addAction(Constant.ACTION_USB_RECEIVER);
        localReceiver = new LocalReceiver();
        //注册本地接收器
        localBroadcastManager.registerReceiver(localReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        localBroadcastManager.unregisterReceiver(localReceiver);
    }

    private class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String data = intent.getStringExtra("data");
            Message msg = Message.obtain();
            Log.d(TAG, "收到本地广播==>>" + data);
            if (data.equals("USB_MOUNT")) {
                //msg.what = MyHandler.USB_MOUNT;
                msg.obj = intent.getStringExtra("path");
                Log.d(TAG, "收到本地广播=path=>>" + msg.obj);
                startUpdate(intent.getStringExtra("path"));
            }
            //mHandler.sendMessage(msg);
        }
    }

    private void startUpdate(String path) {
        Intent updateIntent = new Intent(this, com.example.usbupdaelibrary.UpdateActivity.class);
        updateIntent.putExtra("path", path);
        startActivity(updateIntent);
    }


}
