package com.phairy.taxionly;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainMenu extends ActionBarActivity implements OnClickListener {

    Button btn[] = new Button[3];
    ViewPager viewPager = null;
    Thread thread = null;
    Handler handler = null;
    int p = 0;    //페이지번호
    int v = 1;    //화면 전환 뱡향
    Vibrator vibe;
    static Context context;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private String TAG = Start.TAG;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;
        enableGPSSetting();

        //viewPager
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(adapter);

        btn[0] = (Button) findViewById(R.id.btn_a);
        btn[1] = (Button) findViewById(R.id.btn_b);
        btn[2] = (Button) findViewById(R.id.btn_c);

        for (int i = 0; i < btn.length; i++) {
            btn[i].setOnClickListener(this);
        }



        pref = getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor = pref.edit();

        boolean isAlarmEnrolled = pref.getBoolean("isAlarmEnrolled", false);
        if (isAlarmEnrolled == false) {
            Log.e(TAG, "MainManu:onCreate_first excute");

            enrollAlarm(20,22);
            editor.putBoolean("isAlarmEnrolled", true);
            editor.commit();
        }else{
            Log.d(TAG, "AccountFragment:onCreateView() / is not first");
        }

       handleIntentFlag(getIntent());       //intent flag에 따른 처리( gps ON, 가계부 시작 등 )


//        handler = new Handler() {
//
//
//            public void handleMessage(android.os.Message msg) {
//
//                btn[p].setBackgroundColor(Color.parseColor("#00000000"));
//
//                if( v == 0){
//                    if( p == 0 ){
//                        p = 3;
//                    }else{
//                        p--;
//                    }
//                    viewPager.setCurrentItem(p);
//                }else if ( v == 1 ){
//                    if( p == 2 ){
//                        p = 0;
//                    }else{
//                        p++;
//                    }
//                    viewPager.setCurrentItem(p);
//                }else{
//                    //여기 왜옴
//                }
//
//                btn[p].setBackgroundColor(Color.parseColor("#ddaa5533"));
//            }
//        };

//        thread = new Thread(){
//            //run은 jvm이 쓰레드를 채택하면, 해당 쓰레드의 run메서드를 수행한다.
//            public void run() {
//                super.run();
//                while(true){
//                    try {
//                        Thread.sleep(4000);
//                        handler.sendEmptyMessage(0);
//                    } catch (InterruptedException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//
//
//                }
//            }
//        };
//        thread.start();




    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_a:
				btn[p].setBackgroundColor(Color.parseColor("#00000000"));
				p = 0;
				btn[p].setBackgroundColor(Color.parseColor("#bbbbbb"));
                viewPager.setCurrentItem(0);

                break;

            case R.id.btn_b:
				btn[p].setBackgroundColor(Color.parseColor("#00000000"));
				p = 1;
				v.setBackgroundColor(Color.parseColor("#bbbbbb"));
                viewPager.setCurrentItem(1);
                break;

            case R.id.btn_c:
				btn[p].setBackgroundColor(Color.parseColor("#00000000"));
				p = 2;
				v.setBackgroundColor(Color.parseColor("#bbbbbb"));
                viewPager.setCurrentItem(2);

                break;

            default:
                break;

        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                vibe.vibrate(70);
                Log.d(Start.TAG, "MainMenu : BACK button down");

                new AlertDialog.Builder(this).
                        setIcon(android.R.drawable.ic_dialog_alert).
                        setTitle("확인").
                        setMessage("프로그램을 종료할까요?").
                        setNegativeButton("취소", null).
                        setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
//                                moveTaskToBack(true);
                                finish();
                                System.exit(0);
                            }
                        }).show();
                break;
            case KeyEvent.KEYCODE_HOME:

                finish();
                System.exit(0);
                //    ActivityManager actman = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
                //     actman.restartPackage(getPackageName());
                //이를 위해선 permission.RESTART_PACKAGES 필요함
        }

        return super.onKeyDown(keyCode, event);
    }

    private void enableGPSSetting() {
        ContentResolver res = getContentResolver();
        boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(res, LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            new AlertDialog.Builder(this)
                    .setTitle("GPS 설정")
                    .setMessage("GPS가 필요한 서비스입니다. \nGPS를 켜시겠습니까?")
                    .setPositiveButton("GPS 켜기",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                    Intent intent = new Intent(
                                            Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(intent);
                                }
                            })
                    .setNegativeButton("닫기",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int which) {
                                }
                            }).show();
        }
    }

    private void handleIntentFlag(Intent intent){
    try{

        if( intent.getIntExtra("flag",0) == 1000 ) {
            ContentResolver res = getContentResolver();
            boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(res, LocationManager.GPS_PROVIDER);

            if(!gpsEnabled){

                Toast.makeText(getApplicationContext(), "GPS 수신기를 먼저 켜주세요", Toast.LENGTH_SHORT).show();

                NotificationBroadcast.setNotification(getApplicationContext(),0);

                return ;
            }


            Log.e(Start.TAG, "MainMenu : alarm! GPS 수집을 시작합니다");
            Toast.makeText(getApplicationContext(), "주행 거리를 측정합니다\n오늘도 안전하게!", Toast.LENGTH_SHORT).show();
            Intent intentService = new Intent(this, GpsCatcher.class);
            GpsCatcher.isWorking = true;
            startService(intentService); //서비스 시작

                /*

                HomeFragment 새로고침(버튼)

                 */

        }else {
            Log.e(Start.TAG, "MainMenu : 평소 열기");
        }
    }catch (Exception e){
        Log.e(Start.TAG, "MainMenu : putExtra 오류");
    }

    }
    private void enrollAlarm(int hour, int minute) {

        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);   //1~24 범위(아마)
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        Log.e(Start.TAG, "MainMenu :  enrollAlarm_"+ hour + "시 " +minute+"분 예약되었습니다");
//        if(now-calendar.getTime()) {
//            return;
//        }

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent2 = new Intent(getApplicationContext(), NotificationBroadcast.class);
        intent2.putExtra("flag", 1000);
        // intent2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+10000, AlarmManager.INTERVAL_DAY, pendingIntent);//1000==1초 1000*60*60*24//하루 뒤에 시작!
//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1000*60*60*24, AlarmManager.INTERVAL_DAY, pendingIntent);//1000==1초 1000*60*60*24//하루 뒤에 시작!

    }
}