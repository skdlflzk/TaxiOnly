
package com.phairy.taxionly;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.location.Location;
import android.location.LocationManager;
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

import android.widget.TextView;
import android.widget.Toast;

import org.apache.log4j.Logger;


public class HomeFragment extends Fragment {
    private Logger mLogger = Logger.getLogger(HomeFragment.class);
    private String TAG = Start.TAG;

    static public Context context;

    private Button serviceButton;


    public HomeFragment() {
        context = getActivity();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_fragment, container, false);
        mLogger.error("--HomeFragment--");


        Button takeButton = (Button) view.findViewById(R.id.TakeButton);
        takeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Location mlocation;
                mlocation = GpsCatcher.getLocation();

                try {
                    if (mlocation != null) {
                        mLogger.error("onTakeButton_ (" + mlocation.getLatitude() + "," + mlocation.getLongitude() + ")");

                    }
                } catch (Exception e) {
                    mLogger.error("onTakeButton_위치 받기 실패");

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
                                                 mLogger.debug("onServiceButtonClicked_");


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


    private void manuallyStartGpsCatcher() {


        try {

            Start.toggleIsWorking(getActivity(), 1);

            Intent intent = new Intent(getActivity(), GpsCatcher.class);
//        intent.putExtra("flag",1000);//시작

            getActivity().startService(intent);
            GpsCatcher.trigger = false;  // trigger 끄기

            mLogger.error("manuallyStartGpsCatcher_수동으로 GPSCatcher를 실행합니다");




        } catch (Exception e) {
            mLogger.error("manuallyStartGpsCatcher_GPSCatcher 수동 실행 실패!");

        }
    }

    public void toggleGPSCatcher(int isW) {

        mLogger.error("toggleGPSCatcher_");


        if (isW == 2) {  //isWorking 켜져있다면

            try {

                mLogger.error("toggleGPSCatcher_서비스를 종료합니다");


                Start.toggleIsWorking(getActivity(), 0);   //0로 변경
                GpsCatcher.trigger = true;  // 종료 시킴

                Toast.makeText(getActivity(), "GPS를 종료합니다", Toast.LENGTH_SHORT).show();

                serviceButton.setText("GPS 시작하기");

            } catch (Exception e) {
                mLogger.error("toggleGPSCatcher_서비스 종료 실패!");

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


                mLogger.error("toggleGPSCatcher_ GPS ON");

                serviceButton.setText("GPS 종료하기");

                manuallyStartGpsCatcher();

                Toast.makeText(getActivity(), "GPS가 켜졌습니다", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                mLogger.error("toggleGPSCatcher_GPS ON 실패!");


            }
        }
    }

}