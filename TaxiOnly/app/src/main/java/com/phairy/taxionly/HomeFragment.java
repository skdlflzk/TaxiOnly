
package com.phairy.taxionly;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ExecutionException;


public class HomeFragment extends Fragment implements LocationListener {  //} implements View.OnClickListener{

    SQLiteDatabase database;
    String DATABASENAME = "PART";
    String TABLENAME = "PARTINFO";

    private String TAG = Start.TAG;

    static public Context context;
    private LocationManager locationManager;
    private Location mlocation;
    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private TextView logText;
    private String bestProvider;
    private Button serviceButton;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String gpsLog;
    private double latitude;
    private double longitude;

    public HomeFragment() {
        context = getActivity();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        Log.e(TAG, "--HomeFragment--");


        View view = inflater.inflate(R.layout.home_fragment, container, false);

        latitudeTextView = (TextView) view.findViewById(R.id.latitudeTextview);
        longitudeTextView = (TextView) view.findViewById(R.id.longitudeTextview);
        logText = (TextView) view.findViewById(R.id.logText);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        //GPS로부터 위치정보 업데이트를 요청함

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); //정확도 설정
//        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT); //전력 소모량

        bestProvider = locationManager.getBestProvider(criteria, false);

        locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
        //기지국으로부터 위치정보 업데이트를 요청함

        Button takeButton = (Button) view.findViewById(R.id.TakeButton);
        takeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mlocation = GpsCatcher.mlocation;

                    latitude = mlocation.getLatitude();
                    longitude = mlocation.getLongitude();

                    Log.e(TAG, "Homefragment: onClick_위치 : (" + latitude + "," + longitude + ")");
                    float[] result = new float[3];

//                Location.distanceBetween(37.63660853, 127.02408667, 37.63661281,127.02408684, result);
//                Log.e(TAG, "Homefragment: res = " + result[0]);


//                    MainMenu.setNotification(getActivity(), true);

                }catch(Exception e){
                    Log.e(TAG, "Homefragment: onTakeButton_위치 받기 실패");
                }
                try {
                    if ((int) mlocation.getLatitude() != 0) {

                        Toast.makeText(getActivity(), mlocation.getSpeed()+"m/s로 총"+ GpsCatcher.distance+"m 이동함", Toast.LENGTH_SHORT).show();
                        openMap(mlocation.getLatitude(), mlocation.getLongitude());
                    } else {
                        Toast.makeText(getActivity(), "( " + latitude + ", " + longitude + ")", Toast.LENGTH_SHORT).show();

                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "정보가 아직 수신되지 않음", Toast.LENGTH_SHORT).show();
                }
            }
        });

        serviceButton = (Button) view.findViewById(R.id.serviceButton);
        if(GpsCatcher.isWorking){
            serviceButton.setText("GPS 종료하기");
        }else{
            serviceButton.setText("GPS 시작하기");
        }
        serviceButton.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View view) {
                                                 Vibrator vibe = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                                                 vibe.vibrate(70);
                                                 Log.d(Start.TAG, "HomeFragment: onServiceButtonClicked_");
                                                 String question;
                                                 if(GpsCatcher.isWorking){
                                                     question = "운행 기록을 종료할까요?";
                                                 }else{
                                                     question = "운행을 기록할까요?";
                                                 }
                                                 new AlertDialog.Builder(getActivity()).
                                                         setIcon(android.R.drawable.ic_dialog_alert).
                                                         setTitle("확인").
                                                         setMessage(question).
                                                         setNegativeButton("취소", null).
                                                         setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                             @Override
                                                             public void onClick(DialogInterface dialogInterface, int i) {
                                                                 toggleGPSCatcher();
                                                                 return ;
                                                             }
                                                         }).show();

                                             }
                                         }
        );


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void openMap(double latitude, double longitude) {

        Uri geoURI = Uri.parse(String.format("geo:%f,%f", latitude, longitude));
        Intent intent = new Intent(Intent.ACTION_VIEW, geoURI);
        startActivity(intent);

    }

    @Override
    public void onLocationChanged(Location location) {
        mlocation = location;

        latitude = mlocation.getLatitude();
        longitude = mlocation.getLongitude();

        Log.e(TAG, "HomeFragment:onLocationChanged_(" + latitude + "), (" + longitude + ")");

        latitudeTextView.setText(String.format("%f", location.getLatitude()));
        longitudeTextView.setText(String.format("%f", location.getLongitude()));

        gpsLog = "(" + latitude + "," + longitude + ")\n";


        logText.setText(logText.getText().toString() + gpsLog);

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        switch (i) {
            case LocationProvider.OUT_OF_SERVICE:
                Toast.makeText(getActivity(), "서비스 지역이 아닙니다..", Toast.LENGTH_SHORT).show();
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Toast.makeText(getActivity(), "일시적인 장애로 인하여 위치정보를 수신할 수 없습니다.", Toast.LENGTH_SHORT).show();
                break;
            case LocationProvider.AVAILABLE:
                Toast.makeText(getActivity(), "GPS 수신 가능 상태", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(getActivity(), "위치정보 공급 가능.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(getActivity(), "위치정보 공급받을수 없음.", Toast.LENGTH_SHORT).show();
    }


    public double calculateGPSDistance() {

        String log = "";

        try {

            String mSdPath;

            String ext = Environment.getExternalStorageState();
            if (ext.equals(Environment.MEDIA_MOUNTED)) {
                mSdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            } else {
                mSdPath = Environment.MEDIA_UNMOUNTED;
            }

            File dir = new File(mSdPath + "/TaxiOnly");
            dir.mkdir();

            Calendar calendar = Calendar.getInstance();

            File file = new File(mSdPath + "/TaxiOnly/gpsLog" + GpsCatcher.getFileName() + ".txt");

            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);

            String line = null;
            while ((line = reader.readLine()) != null) {
                log += line;
            }

        } catch (Exception e) {
            Log.e(TAG, "Homefragment : calculateGPSDistance_파일 입력 에러");
        }

        double distance;
        try {

            String tempString = log.substring(log.length() - 60);       // trailer 33자 + <desc/> 7자+ 거리 20


            int front, end;
            front = tempString.indexOf(",d=");
            end = tempString.indexOf("#");

            distance = Double.valueOf(tempString.substring(front + 1, end));
        }catch(Exception e){
            distance = 0;
        }

        return distance;
    }

    public void updatePart(double distance) {

        Log.e(Start.TAG, "HomeFragment : updatePart_");
        database = getActivity().openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
        Cursor cursor = database.rawQuery("Select partCurrentValue,partName,etc FROM " + TABLENAME, null);

        int size = cursor.getCount();   // 자동차 부품 총 량

        double value;

        for (int i = 0; i < size; i++) {

            cursor.moveToPosition(i);
            value = cursor.getDouble(0);

            String etc = cursor.getString(2);

            if (etc.equals("km")) {   // etc로 km와 day 구분

                value += distance;

            } else { //             km가 아니면 하루 추가

                value++;

            }

            database.execSQL("UPDATE " + TABLENAME + " SET partCurrentValue = '" + value + "' WHERE partName = '" + cursor.getString(1) + "'");

            Log.d(TAG, "AccountFragment:updatePart_ "+cursor.getString(1)+"의 값이 "+value+ etc +" 로...");

        }
    }
    private void manuallyStartGpsCatcher(){

        Log.e(Start.TAG, "HomeFragment : 수동으로 GPSCatcher를 실행");

        Intent intent = new Intent(getActivity(), GpsCatcher.class);


        getActivity().startService(intent);

    }



    public void toggleGPSCatcher(){

        Log.e(TAG, "Homefragment: toggleGPSCatcher_");

        if (GpsCatcher.isWorking) {  //isWorking 켜져있다면
            try{
                GpsCatcher.isWorking = !GpsCatcher.isWorking;

                Intent intent = new Intent(context,GpsCatcher.class);
                getActivity().stopService(intent);

                Log.e(TAG, "Homefragment: toggleGPSCatcher_서비스를 종료합니다");

//                Toast.makeText(getActivity(), "GPS가 "+ GpsCatcher.isWorking+"되었습니다", Toast.LENGTH_SHORT).show();
//                serviceButton.setText("GPS 시작하기");

            }catch (Exception e){
                Log.e(TAG, "Homefragment: toggleGPSCatcher_서비스 종료 실패");
            }

            try {

                double distance;
                distance = calculateGPSDistance();  //distance = 오늘 움직인 거리 총량

                updatePart( distance );        //오늘 이동 거리 적용

                                                         /*
                                                         AccountFragment 새로고침
                                                         */

                Log.e(TAG, "AccountFragment:toggleGPSCatcher_today's update -------Success," + distance + " m 이동");

            } catch (Exception e) {

                e.printStackTrace();
                Log.e(TAG, "Homefragment: toggleGPSCatcher_distance update Failed-----;");

            }

            /*
            가계부로 이동
             */

        } else {  //isWorking == false 였다면

            try{


                ContentResolver res = getActivity().getContentResolver();
                boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(res, LocationManager.GPS_PROVIDER);
                if(!gpsEnabled){

                    Toast.makeText(getActivity(), "GPS 수신기를 먼저 켜주세요", Toast.LENGTH_SHORT).show();
//                    NotificationBroadcast.setNotification(getActivity(),0);
                    return;
                }

                GpsCatcher.isWorking = !GpsCatcher.isWorking;
                Log.e(TAG, "Homefragment: onClick_위치 : GPS가 켜짐( " + GpsCatcher.isWorking + "로 바뀜)");
                Toast.makeText(getActivity(), "GPS가 "+ GpsCatcher.isWorking+"되었습니다", Toast.LENGTH_SHORT).show();

                Log.e(TAG, "Homefragment: serviceButton_GPS를 시작합니다");
                serviceButton.setText("GPS 종료하기");

                manuallyStartGpsCatcher();

            }catch (Exception e){

            }
        }
    }
}