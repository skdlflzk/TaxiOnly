
package com.phairy.taxionly;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;


public class HomeFragment extends Fragment implements LocationListener {

    private String TAG = Start.TAG;

    static public Context context;
    private LocationManager locationManager;
    private Location mlocation;
    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private TextView logText;
    private String bestProvider;
    private Button serviceButton;

    private String gpsLog;
    private double latitude;
    private double longitude;


    public HomeFragment() {
        context = getActivity();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_fragment, container, false);

        Log.e(TAG, "--HomeFragment--");


//        latitudeTextView = (TextView) view.findViewById(R.id.latitudeTextview);
//        longitudeTextView = (TextView) view.findViewById(R.id.longitudeTextview);
//
//        logText = (TextView) view.findViewById(R.id.logText);

//        CalendarView calendarView = (CalendarView) view.findViewById(R.id.calendarView);
//        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
//            @Override
//            public void onSelectedDayChange(CalendarView calendarView, int i, int i1, int i2) {
//            }
//        });

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

//                    float[] result = new float[3];
//                Location.distanceBetween(37.63660853, 127.02408667, 37.63661281,127.02408684, result);
//                Log.e(TAG, "Homefragment: res = " + result[0]);


                } catch (Exception e) {
                    Log.e(TAG, "Homefragment: onTakeButton_위치 받기 실패");
                }
                try {

                    Toast.makeText(getActivity(), mlocation.getSpeed() + "m/s로 총" + GpsCatcher.distance + "m 이동함", Toast.LENGTH_SHORT).show();
//                        openMap(mlocation.getLatitude(), mlocation.getLongitude());

                } catch (Exception e) {
                    Toast.makeText(getActivity(), "정보가 아직 수신되지 않음", Toast.LENGTH_SHORT).show();
                }
            }
        });

        serviceButton = (Button) view.findViewById(R.id.serviceButton);


        if (Start.getIsWorking(getActivity()) == 2) {

            serviceButton.setText("GPS 종료하기");
        } else {

            serviceButton.setText("GPS 시작하기");

        }

        serviceButton.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View view) {

                                                 Vibrator vibe = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                                                 vibe.vibrate(70);
                                                 Log.d(Start.TAG, "HomeFragment: onServiceButtonClicked_");

                                                 String question;

                                                 if (Start.getIsWorking(getActivity()) == 0) {
                                                     question = "운행을 기록할까요?";

                                                 } else {
                                                     question = "운행 기록을 종료할까요?";

                                                 }

                                                 new AlertDialog.Builder(getActivity()).
                                                         setIcon(android.R.drawable.ic_dialog_alert).
                                                         setTitle("확인").
                                                         setMessage(question).
                                                         setNegativeButton("취소", null).
                                                         setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                                             @Override
                                                             public void onClick(DialogInterface dialogInterface, int i) {
                                                                 toggleGPSCatcher(Start.getIsWorking(getActivity()));
                                                                 return;
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

//        latitudeTextView.setText(String.format("%f", location.getLatitude()));
//        longitudeTextView.setText(String.format("%f", location.getLongitude()));
//
//        gpsLog = "(" + latitude + "," + longitude + ")\n";


//        logText.setText(logText.getText().toString() + gpsLog);

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

/*
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

            File file = new File(mSdPath + "/TaxiOnly/gps" + GpsCatcher.getFileName() + ".gpx");

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
            front = tempString.indexOf("<desc>") + 6;
            end = tempString.indexOf("<desc/>");

            distance = Double.valueOf(tempString.substring(front, end));

        } catch (Exception e) {
            distance = 0;
        }

        return distance;
    }
*/


    private void manuallyStartGpsCatcher() {


        try {

            Start.toggleIsWorking(getActivity(), 1);

            Intent intent = new Intent(getActivity(), GpsCatcher.class);
//        intent.putExtra("flag",1000);//시작

            getActivity().startService(intent);
            GpsCatcher.trigger = false;  // trigger 끄기

            Log.e(Start.TAG, "HomeFragment : manuallyStartGpsCatcher_수동으로 GPSCatcher를 실행합니다");

        } catch (Exception e) {

            Log.e(Start.TAG, "HomeFragment : manuallyStartGpsCatcher_GPSCatcher 수동 실행 실패!");
        }
    }

    public void toggleGPSCatcher(int isW) {

        Log.e(TAG, "Homefragment: toggleGPSCatcher_");

        if (isW == 2) {  //isWorking 켜져있다면

            try {

                Log.e(TAG, "Homefragment: toggleGPSCatcher_서비스를 종료합니다");

                Start.toggleIsWorking(getActivity(), 0);   //0로 변경
                GpsCatcher.trigger = true;  // 종료 시킴

                Toast.makeText(getActivity(), "GPS를 종료합니다", Toast.LENGTH_SHORT).show();

                serviceButton.setText("GPS 시작하기");

            } catch (Exception e) {
                Log.e(TAG, "Homefragment: toggleGPSCatcher_서비스 종료 실패!");
            }

            /*
            가계부로 이동
             */

        } else {  //isWorking == false 였다면

            try {

                ContentResolver res = getActivity().getContentResolver();
                boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(res, LocationManager.GPS_PROVIDER);

                if (!gpsEnabled) {

                    Toast.makeText(getActivity(), "GPS 수신기를 먼저 켜주세요", Toast.LENGTH_SHORT).show();

                    return;
                }


                Log.e(TAG, "Homefragment: toggleGPSCatcher_ GPS ON");



                serviceButton.setText("GPS 종료하기");

                manuallyStartGpsCatcher();

                Toast.makeText(getActivity(), "GPS가 켜졌습니다", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {

                Log.e(TAG, "Homefragment: toggleGPSCatcher_GPS ON 실패!");

            }
        }
    }

}