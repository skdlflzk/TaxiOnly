package com.phairy.taxionly;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.log4j.Logger;


public class Start extends Activity {

    private Logger mLogger = Logger.getLogger(Start.class);

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

        handler.sendEmptyMessageDelayed(0, 500);  //n초간 대기
    }

    static public int getIsWorking( Context context ){

        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);

        return pref.getInt("isWorking", 0);

    }

    static public void toggleIsWorking( Context context, int i){   // 0~2 사이 범위

        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;

        editor = pref.edit();
        editor.putInt("isWorking", i);
        editor.commit();

    }

    /*

    펜딩 인텐트 req code- 1111 알람 번호

    intent flag- 1000 정상 시작 4444 정상 종료

    notifacation.notify 상단바 알림 구분자 - 0

     */
}