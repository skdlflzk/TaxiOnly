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
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;

public class AlarmActivity extends Activity {

    private Button acceptButton;
    private Button cancelButton;
    static Context context;
    private Vibrator vibrator;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        LayoutInflater inflater = LayoutInflater.from(this);

//        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        setContentView(inflater.inflate(R.layout.activity_alarm, null));


//        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE); // 이게 필요 한가...


        SharedPreferences pref = getSharedPreferences("pref", Context.MODE_PRIVATE);

        int hour = pref.getInt("timeHour", 100);

        if(  hour != 100 ){

            int minute = pref.getInt("timeMinute",100);

            AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
            Intent intent2 = new Intent(this, AlarmActivity.class);
            intent2.putExtra("flag", 1000);
            // intent2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1111, intent2, PendingIntent.FLAG_UPDATE_CURRENT);


            alarmManager.cancel(pendingIntent);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.set(Calendar.HOUR_OF_DAY, hour);   //1~24 범위(아마)
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, 00);


//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);  //부정확 / 배터리 절약
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);      //정확 / 배터리 소모

        }

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        long vi[] = new long[] {500, 500,500, 500,500, 500};
        vibrator.vibrate(vi,1);

        acceptButton = (Button) findViewById(R.id.acceptButton);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ContentResolver res = getContentResolver();
                boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(res, LocationManager.GPS_PROVIDER);

                if(!gpsEnabled){

                    Toast.makeText(getApplicationContext(), "GPS 수신기를 먼저 켜주세요", Toast.LENGTH_SHORT).show();

                    NotificationBroadcast.setNotification(getApplicationContext(),0);

                    return ;
                }

                if( Start.getIsWorking(context) == 0) {

                    Log.e(Start.TAG, "MainMenu : alarm! GPS 수집을 시작합니다");
                    Toast.makeText(getApplicationContext(), "주행 거리를 측정합니다\n오늘도 안전하게!", Toast.LENGTH_SHORT).show();
                    Intent intentService = new Intent(context, GpsCatcher.class);

                    Start.toggleIsWorking(context, 1);
                    startService(intentService); //서비스 시작

                    NotificationBroadcast.setNotification(context, 2);  //  정상 시작
                }else{
                    Toast.makeText(getApplicationContext(), "이미 기록중입니다!", Toast.LENGTH_SHORT).show();
                }

                finish();
//              System.exit(0);
            }
        });

        cancelButton = (Button) findViewById(R.id.cancelButton);

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

