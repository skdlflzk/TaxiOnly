package com.phairy.taxionly;

import android.app.Activity;
import android.app.AlarmManager;
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
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.apache.log4j.Logger;

import java.util.Calendar;

public class ControlPartActivity extends AppCompatActivity {
    private Logger mLogger = Logger.getLogger(ControlPartActivity.class);

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

            Log.e(Start.TAG, "AlarmActivity : alarm!");


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
            } catch (Exception e) {

            }

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

                        Log.e(Start.TAG, "AlarmActivity : alarm! GPS 수집을 시작합니다");
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
        }

}
