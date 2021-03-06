package com.phairy.taxionly;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.FragmentManager;
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
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import org.apache.log4j.Logger;

import java.util.Calendar;

public class MainMenu extends ActionBarActivity implements OnClickListener {
    private Logger mLogger = Logger.getLogger(MainMenu.class);
    Button btn[] = new Button[4];
    ViewPager viewPager = null;

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
        //requestWindowFeature(Window.FEATURE_NO_TITLE);

        context = this;

       // enableGPSSetting();

        //viewPager
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(adapter);

        btn[0] = (Button) findViewById(R.id.btn_a);
        btn[1] = (Button) findViewById(R.id.btn_b);
        btn[2] = (Button) findViewById(R.id.btn_c);
        btn[3] = (Button) findViewById(R.id.btn_d);

        for (int i = 0; i < btn.length; i++) {
            btn[i].setOnClickListener(this);
        }

        handleIntentFlag(getIntent());       //intent flag에 따른 처리( gps ON, 가계부 시작 등 )

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
            case R.id.btn_d:
                btn[p].setBackgroundColor(Color.parseColor("#00000000"));
                p = 3;
                v.setBackgroundColor(Color.parseColor("#bbbbbb"));
                viewPager.setCurrentItem(3);

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
                mLogger.error("BACK button down");

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
                                    /*
                                        이렇게 하면 설정창이 켜져있는 상태로 열린다
                                    Intent intent = new Intent( Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivityForResult(intent,0);
                                    */
                                    startActivityForResult(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS), 0);

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

    private void handleIntentFlag(Intent intent) {
        try {
            if (intent.getIntExtra("flag", 0) == 12345) {    //주행 끝 데이터 전송

//                FragmentManager fm = getFragmentManager();
//                HomeFragment fragment = new HomeFragment();
//                fragment.setArguments(intent.getBundleExtra("data"));   //HomeFragment에 주행 데이터 전송

                try {

//                    String description = getArguments().getString("A");
//                    mLogger.error("descript = " +description);

                        Intent intent1 = new Intent(context, HouseholdChartActivity.class);
                        intent1.putExtra("flag",123); // 주행 끝 데이터 전달
                        intent1.putExtra("data", intent.getBundleExtra("data"));      //hashmap만을 전달하는 셈
                        intent1.setAction("CREATE");
                        startActivity(intent1);

                /*
                (intent내부 - int flag)
                          ( ㄴBundle data - int action)
                                          ㄴSerializable Hashmap - lonList
                                          ㄴint dailyCount       ㄴlatList
                                          ㄴint distance         ㄴtimeList
                                                                 ㄴ ...

                 */

                }catch(Exception e){
                    e.printStackTrace();
                }

//                Bundle args = new Bundle();
//                args.putString("A", "testing");
//                fragment.setArguments(args);   //HomeFragment에 주행 데이터 전송
//
//                getSupportFragmentManager().beginTransaction()
//                        .replace(R.id.viewPager, fragment).commit();
                mLogger.debug("handleIntentFlag()_주행 끝, 데이터 fragment로 전송");
                /*
                (intent내부 - int flag)
                          ( ㄴBundle data - int action)
                                          ㄴSerializable Hashmap - lonList
                                          ㄴint dailyCount       ㄴlatList
                                                                 ㄴtimeList
                                                                 ㄴ ...

                 */

            } else {
                mLogger.info("MainMenu : 평소 열기");
            }
        } catch (Exception e) {
            mLogger.error("putExtra 오류");
        }

    }


}