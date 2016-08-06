
package com.phairy.taxionly;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.databinding.DataBindingUtil;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.phairy.taxionly.databinding.HomeFragmentBinding;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.Calendar;


public class HomeFragment extends Fragment {

    private Logger mLogger = Logger.getLogger(HomeFragment.class);
    private String TAG = Start.TAG;

    static public Context context;

//    private Button serviceButton;
    private TextView logtext;
    HomeFragmentBinding binding;

    public HomeFragment() {
        context = getActivity();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment,container,false);
        View view = binding.getRoot();
        mLogger.error("--HomeFragment--" + MainMenu.class.getSimpleName());

        logtext = (TextView )view.findViewById(R.id.logText);




        binding.TakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

/*
                AsyncHttpSet asyncHttpSet = new AsyncHttpSet(true);
                RequestParams params = new RequestParams();
                params.setContentEncoding("UTF-8");
                try {
                    String[] fileList = getTitleList();

                    if (fileList.length == 0) {
                        mLogger.debug("WifiReceiver: /TaxiOnly/GPS 내 백업할 파일이 없습니다");
                        return;
                    } else {

                        String count = ""+fileList.length;

                        params.put("COUNT", count);
                        params.put("FAPP",123);

                    }
                    mLogger.debug("WifiReceiver: GPS 백업 시작... 업로드 파일 개수 : "+ fileList.length);
                    String mSdPath;
                    String ext = Environment.getExternalStorageState();
                    if (ext.equals(Environment.MEDIA_MOUNTED)) {
                        mSdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                    } else {
                        mSdPath = Environment.MEDIA_UNMOUNTED;
                    }

                    for (int i = 0; i < fileList.length; i++) {

                        File file = new File( mSdPath+File.separator + "TaxiOnly" + File.separator + "GPS" + File.separator + fileList[i]);
                        params.put("GPX"+ (i + 1), file);  //사용자 구분이 필요한가?              GPX1, GPX2, ...
                        params.put("FileName" + (i + 1), "" + fileList[i]);    //해당 파일과 파일 이름 전송함  FileName1, FileName2, ...
                    }

                } catch (Exception e) {
                    mLogger.error("WifiReceiver: GPS 파일 폼 생성 중 에러...");
                    e.printStackTrace();
                }


                asyncHttpSet.post("/GPSbackup", params, new AsyncHttpResponseHandler() {   //업데이트할 php의 주소 정하기



                    @Override
                    public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                        mLogger.error("WifiReceiver:onSuccess statusCode i =" +i+", charset = "  +getCharset());
                        try{

                            String s = new String(bytes,getCharset());

                            int r = s.indexOf("result=");
                            String result = s.substring(r+7,r+8);
                            if(result.equals("1")){
                                mLogger.warn("WifiReceiver:onSuccess result success ");
                            }else{
                                mLogger.warn("WifiReceiver:onSuccess but result = "+ result);
                            }


                            logtext.setText(""+s);


//                        String str="";
//                        for(int i1= 0; i1 < bytes.length; i1++){
//
//                            str += Character.toString ((char) bytes[i1]);
//
//                        }
//

//                        mLogger.warn("WifiReceiver:onSuccess length = "+ str.length());

                        }catch(Exception e){

                            e.printStackTrace();
                            mLogger.error("WifiReceiver:onSuccess string trans error");

                        }


                        mLogger.info("WifiReceiver: 경로 백업 성공");

                        String ticker = "서버에 운행 정보를 백업했습니다";

                        try {
                            String[] fileList = getTitleList();
                            int isDeleted = 0;

                            String mSdPath;
                            String ext = Environment.getExternalStorageState();
                            if (ext.equals(Environment.MEDIA_MOUNTED)) {
                                mSdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                            } else {
                                mSdPath = Environment.MEDIA_UNMOUNTED;
                            }

                            for (int j = 0; j < fileList.length; j++) {

//                            File fileName = new File(mSdPath + File.separator + "TaxiOnly" + File.separator + "GPS" + File.separator + fileList[j]);
//                            if(fileName.delete()) {
//                                isDeleted++;
//                            }
                            }

                            if (isDeleted == fileList.length) {
                                mLogger.info("WifiReceiver: 백업된 파일 삭제 성공");
                            } else {
                                mLogger.info("WifiReceiver: 백업된 파일 삭제 실패");
                            }

                        }catch(Exception e){
                            mLogger.error("WifiReceiver: 백업된 파일 삭제 중 Exception Error");
                        }

                    }

                    @Override
                    public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {

                        mLogger.error("WifiReceiver:운행 정보 전송 실패");
                        String ticker = "서버에 운행 정보를 백업하지 못했습니다";

                        try {

                            Calendar calendar = Calendar.getInstance();
                            String contents = "" + String.format("%02d", calendar.get(Calendar.MONTH) + 1) + "-" + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) +
                                    " " + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE));


                            if (GpsCatcher.getLocation() != null) {
                                contents += " at (" + GpsCatcher.getLocation().getLatitude() + ", " + GpsCatcher.getLocation().getLatitude() + ")" + System.lineSeparator();
                            } else {
                                contents += System.lineSeparator();
                            }

                            mLogger.error("WifiReceiver: onFailure...at " + contents);

                        } catch (Exception e) {
                            mLogger.error("WifiReceiver: FAIL to send!! FAIL to write!!");
                        }
                    }
                });
*/
//                Location mlocation;
//                mlocation = GpsCatcher.getLocation();
//
//                try {
//                    if (mlocation != null) {
//                        mLogger.error("onTakeButton_ (" + mlocation.getLatitude() + "," + mlocation.getLongitude() + ")");
//
//                    }
//                } catch (Exception e) {
//                    mLogger.error("onTakeButton_위치 받기 실패");
//
//                }
//
//                try {
//                    Toast.makeText(getActivity(), mlocation.getSpeed() + "m/s로 총" + GpsCatcher.distance + "m 이동함", Toast.LENGTH_SHORT).show();
////                        openMap(mlocation.getLatitude(), mlocation.getLongitude());
//
//                } catch (Exception e) {
//                    Toast.makeText(getActivity(), "정보가 아직 수신되지 않음", Toast.LENGTH_SHORT).show();
//                }
            }
        });

//        serviceButton = (Button) view.findViewById(R.id.serviceButton);


        if (Start.getIsWorking(getActivity()) == 2) {

            binding.serviceButton.setText("GPS 종료하기");
        } else {

            binding.serviceButton.setText("GPS 시작하기");

        }

        binding.serviceButton.setOnClickListener(new View.OnClickListener() {
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

                binding.serviceButton.setText("GPS 시작하기");

            } catch (Exception e) {
                mLogger.error("toggleGPSCatcher_서비스 종료 실패!");

            }

            /*

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

                binding.serviceButton.setText("GPS 종료하기");

                manuallyStartGpsCatcher();

                Toast.makeText(getActivity(), "GPS가 켜졌습니다", Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                mLogger.error("toggleGPSCatcher_GPS ON 실패!");


            }
        }
    }

    private String[] getTitleList() //알아 보기 쉽게 메소드 부터 시작합니다.
    {
        try {

//            FilenameFilter fileFilter = new FilenameFilter()  //이부분은 특정 확장자만 가지고 오고 싶을 경우 사용하시면 됩니다.
//            {
//                public boolean accept(File dir, String name)
//                {
//                    return name.endsWith("gpx"); //이 부분에 사용하고 싶은 확장자를 넣으시면 됩니다.
//                } //end accept
//            };
            String mSdPath;
            String ext = Environment.getExternalStorageState();
            if (ext.equals(Environment.MEDIA_MOUNTED)) {
                mSdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            } else {
                mSdPath = Environment.MEDIA_UNMOUNTED;
            }

            File file = new File(mSdPath +File.separator + "TaxiOnly" + File.separator + "GPS" + File.separator);

            File[] files = file.listFiles();//위에 만들어 두신 필터를 넣으세요. 만약 필요치 않으시면 fileFilter를 지우세요.

            String[] titleList = new String[files.length]; //파일이 있는 만큼 어레이 생성했구요

            for (int i = 0; i < files.length; i++) {
                titleList[i] = files[i].getName();    //루프로 돌면서 어레이에 하나씩 집어 넣습니다.
                mLogger.debug("WifiReceiver: fileName = " + files[i].getName());
            }//end for
            return titleList;

        } catch (Exception e) {
            mLogger.error("WifiReceiver: 파일 리스트를 가져오지 못했습니다");
            return null;
        }//end catch()
    }//end getTitleList


}