
package com.phairy.taxionly;

import android.app.ActivityManager;
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
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;


public class HomeFragment extends Fragment implements LocationListener {  //} implements View.OnClickListener{

    private String TAG = Start.TAG;

    private LocationManager locationManager;
    private Location mlocation;
    private TextView latitudeTextView;
    private TextView longitudeTextView;
    private TextView logText;
    private String bestProvider;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private String gpsLog;
    private double latitude;
    private double longitude;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "--HomeFragment--");

        View view = inflater.inflate(R.layout.home_fragment, container, false);

//        pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
//       editor = pref.edit();

        //gpsLog = pref.getString("gpslog", "GPS...");
        //지금까지 저장된 gps정보를 불러옴

        latitudeTextView = (TextView) view.findViewById(R.id.latitudeTextview);
        longitudeTextView = (TextView) view.findViewById(R.id.longitudeTextview);
        logText = (TextView) view.findViewById(R.id.logText);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        //GPS로부터 위치정보 업데이트를 요청함

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); //정확도 설정
//        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT); //전력 소모량

        bestProvider = locationManager.getBestProvider(criteria,false);

        locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
        //기지국으로부터 위치정보 업데이트를 요청함



        Button takeButton = (Button) view.findViewById(R.id.TakeButton);

        takeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //     locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
                mlocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                latitude = mlocation.getLatitude();
                longitude = mlocation.getLongitude();

                Log.e(TAG, "Homefragment: onClick_위치 : (" + latitude + ","+ longitude + ")");
                try {
                    if ((int) mlocation.getLatitude() != 0) {
                        Toast.makeText(getActivity(), "( " + latitude + ", " + longitude + ")", Toast.LENGTH_SHORT).show();
                        openMap(mlocation.getLatitude(), mlocation.getLongitude());
                    } else {
                        Toast.makeText(getActivity(), "( " + latitude + ", " + longitude + ")", Toast.LENGTH_SHORT).show();

                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), "정보가 아직 수신되지 않음", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
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

        gpsLog = "("+latitude +","+longitude + ")\n";




        logText.setText(logText.getText().toString() + gpsLog);

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

            File file = new File(mSdPath + "/TaxiOnly/gpsLog"+ calendar.get(Calendar.DAY_OF_MONTH) + ".txt");

            fos = new FileOutputStream( file, true );  //mode_append
            fos.write(gpsLog.getBytes());
            fos.close();

        } catch(Exception e) {

            Log.e(TAG, "Homefragment : onLocationChanged_파일 출력 에러");
        }
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





}