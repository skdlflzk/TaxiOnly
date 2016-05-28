package com.phairy.taxionly;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.apache.log4j.Logger;

import java.util.Calendar;

public class AlarmActivity extends Activity {

    private Logger mLogger = Logger.getLogger(AlarmActivity.class);



    private Button acceptButton;
    private Button cancelButton;
    static Context context;
    private Vibrator vibrator;

    PowerManager pm;
    PowerManager.WakeLock wl;


    private String TAG = Start.TAG;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        LayoutInflater inflater = LayoutInflater.from(this);

//        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        setContentView(inflater.inflate(R.layout.activity_alarm, null));

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        mLogger.error("--alarm!--");



        SQLiteDatabase database;
        String DATABASENAME = "PART";
        String TABLENAME = "PARTINFO";

        //하루 추가

        try {
            database = context.openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
            Cursor cursor = database.rawQuery("Select partCurrentValue,partName,etc FROM " + TABLENAME, null);

            int size = cursor.getCount();   // 자동차 부품 총 량

            double value;

            for (int i = 0; i < size; i++) {

                cursor.moveToPosition(i);
                value = cursor.getDouble(0);

                String etc = cursor.getString(2);

                if (etc.equals("km")) {   // etc로 km와 day 구분


                } else { //             km가 아니면 하루 추가...는 여기서는 안하겠음

                    value++;

                    database.execSQL("UPDATE " + TABLENAME + " SET partCurrentValue = '" + value + "' WHERE partName = '" + cursor.getString(1) + "'");


                    Log.d(TAG, "GpsCatcher:updatePart_ " + cursor.getString(1) + "의 값이 " + value + etc + " 로...");

                }


            }
            cursor.close();
        }catch(Exception e){

        }
        //알람 재설정
        SharedPreferences pref = getSharedPreferences("pref", Context.MODE_PRIVATE);


        int hour = pref.getInt("timeHour", 100);
        int minute = pref.getInt("timeMinute", 100);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent2 = new Intent(this, AlarmActivity.class);
        intent2.putExtra("flag", 1000);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1111, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        try {

            if (hour == 100 || minute == 100) {
                Log.e(Start.TAG, "AlarmActivity : alarm이 제대로 설정되지 않음");
                Toast.makeText(getApplicationContext(), "알람 시간이 제대로 설정되지 않음", Toast.LENGTH_SHORT).show();
            }

            //이전 알람 삭제
            alarmManager.cancel(pendingIntent);
            Log.e(Start.TAG, "AlarmActivity : alarm 제거됨");
        }catch(Exception e){
            mLogger.error(" alarm 제거 에러");
        }
        //


        //화면 켜기

        pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TaxiOnly");

        wl.acquire();



        //다음 알람 등록
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.DATE, 1); // 다음날!
            calendar.set(Calendar.HOUR_OF_DAY, hour);   //1~24 범위(아마)
            calendar.set(Calendar.SECOND, 00);
            calendar.set(Calendar.MINUTE, minute );

//            calendar.set(Calendar.MINUTE, minute + 1);
//            SharedPreferences.Editor editor = pref.edit();
//
//            editor.putInt("timeMinute", (minute+1));
//            editor.commit();

//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);  //부정확 / 배터리 절약
//            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);      //정확 / 배터리 소모

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);      //정확 / 배터리 소모

            mLogger.error( hour+"시"+ minute+"분에 alarm 등록 성공");
        }catch(Exception e){
            mLogger.error("alarm 등록 에러");
        }
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        vibrator.vibrate( new long[] { 200, 100, 500, 300 }, 0);

        acceptButton = (Button) findViewById(R.id.acceptButton);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ContentResolver res = getContentResolver();
                boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(res, LocationManager.GPS_PROVIDER);

                wl.release();

                if (!gpsEnabled) {

                    Toast.makeText(getApplicationContext(), "GPS 수신기를 먼저 켜주세요", Toast.LENGTH_SHORT).show();

                    NotificationBroadcast.setNotification(getApplicationContext(), 0);

                    return;
                }

                if (Start.getIsWorking(context) == 0) {
                    mLogger.error("alarm! GPS 수집을 시작합니다");
                    Toast.makeText(getApplicationContext(), "주행 거리를 측정합니다\n오늘도 안전하게!", Toast.LENGTH_SHORT).show();
                    Intent intentService = new Intent(context, GpsCatcher.class);

                    Start.toggleIsWorking(context, 1);
                    startService(intentService); //서비스 시작

                    NotificationBroadcast.setNotification(context, 2);  //  정상 시작
                } else {
                    Toast.makeText(getApplicationContext(), "이미 기록중입니다!", Toast.LENGTH_SHORT).show();
                }

                finish();
//              System.exit(0);
            }
        });

        cancelButton = (Button) findViewById(R.id.cancelButton);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wl.release();
                finish();

            }
        });

        /*
            10 분 뒤에 또 울릴 지 결정
         */

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        vibrator.cancel();

    }
}

