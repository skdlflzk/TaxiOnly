package com.phairy.taxionly;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

public class GpsCatcher extends Service  implements LocationListener {  //} implements View.OnClickListener{

    private String TAG = Start.TAG;

    private LocationManager locationManager;
    private Location mlocation;
    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private TextView logText;
    private String bestProvider;

    private String gpsLog;
    private double latitude;
    private double longitude;

    public static Boolean isWorking = false;
    public GpsCatcher() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "--GPSCatcher--");
        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //GPS로부터 위치정보 업데이트를 요청함

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE); //정확도 설정
//        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT); //전력 소모량

            bestProvider = locationManager.getBestProvider(criteria, false);

            locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
            //기지국으로부터 위치정보 업데이트를 요청함
        }catch (Exception e){
            Log.e(TAG, "GPSCatcher:onCreate_ locationManager Error");
        }

        Thread  thread = new Thread(){
            //run은 jvm이 쓰레드를 채택하면, 해당 쓰레드의 run메서드를 수행한다.
            public void run() {
                super.run();
                while(true){
                    try {
                        Thread.sleep(5000);
                        Log.e(TAG, "GPSCatcher:onCreate_servive 실행 중");
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        throw new UnsupportedOperationException("Not yet implemented");

    }


    @Override
    public void onLocationChanged(Location location) {
        mlocation = location;

        latitude = mlocation.getLatitude();
        longitude = mlocation.getLongitude();

        Log.e(TAG, "GPSCatcher:onLocationChanged_(" + latitude + "), (" + longitude + ")");


        gpsLog = latitude +","+longitude + "/";


        String mSdPath;
        FileOutputStream fos;

        String ext = Environment.getExternalStorageState();
        if (ext.equals(Environment.MEDIA_MOUNTED)) {
            mSdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            mSdPath = Environment.MEDIA_UNMOUNTED;
        }


        try {
            File dir = new File(mSdPath + "/TaxiOnly");
            dir.mkdir();

            Calendar calendar = Calendar.getInstance();

            File file = new File(mSdPath + "/TaxiOnly/gpsLog"+ (calendar.get(Calendar.MONTH)+1) + calendar.get(Calendar.DAY_OF_MONTH) + ".txt");

            fos = new FileOutputStream( file, true );  //mode_append
            fos.write(gpsLog.getBytes());
            fos.close();

        } catch(Exception e) {

            Log.e(TAG, "GPSCatcher : onLocationChanged_파일 출력 에러");
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        switch (i) {
            case LocationProvider.OUT_OF_SERVICE:
                Log.e(TAG, "GPSCatcher : onStatusChanged_서비스 지역이 아닙니다");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.e(TAG, "GPSCatcher : onStatusChanged_일시적인 장애로 인하여 위치정보를 수신할 수 없습니다.");
                break;
            case LocationProvider.AVAILABLE:
                Log.e(TAG, "GPSCatcher : onStatusChanged_GPS 수신 가능 상태");
                break;
        }
    }

    @Override
    public void onProviderEnabled(String s) {
        Log.e(TAG, "GPSCatcher : onProviderEnabled_위치정보 공급 가능");
    }

    @Override
    public void onProviderDisabled(String s) {
        Log.e(TAG, "GPSCatcher : onProviderDisabled_위치정보 공급받을수 없음");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(isWorking == false){
            return super.onStartCommand(intent, START_NOT_STICKY, startId);
        }else{
            return super.onStartCommand(intent, START_STICKY, startId);
        }

    }
}

