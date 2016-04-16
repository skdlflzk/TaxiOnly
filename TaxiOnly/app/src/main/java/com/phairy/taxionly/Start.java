package com.phairy.taxionly;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class Start extends Activity {

    int firstExecute ;
    static String TAG = "TaxiOnly";
    static String URL = "http://192.168.0.15:8080/";
    static String SURL = "https://192.168.0.15:8080/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        Log.e(Start.TAG, "--Start--");

        final Intent myIntent;

        firstExecute = 1;
        //로그인 값 저장되어있을 경우//
        if (firstExecute == 1) {
       //     Log.e("hairyd", "Start->");
            myIntent = new Intent(getApplicationContext(), MainMenu.class);

        } else {
            Log.d(Start.TAG, "Start->MainMenu");
            myIntent = new Intent(getApplicationContext(), MainMenu.class);
        }
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                int flag = getIntent().getIntExtra("flag",0);
                myIntent.putExtra("flag",flag);
                startActivity(myIntent);
                finish();
            }
        };

        handler.sendEmptyMessageDelayed(0, 1000);
    }

}