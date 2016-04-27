package com.phairy.taxionly;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class AlarmActivity extends Activity {
    private Button acceptButton;
    private Button cancelButton;
    static Context context;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        LayoutInflater inflater = LayoutInflater.from(this);

//        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        setContentView(inflater.inflate(R.layout.activity_alarm, null));


//        final NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE); // 이게 필요 한가...

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





}

