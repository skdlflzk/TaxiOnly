package com.phairy.taxionly;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.Calendar;

public class WifiReceiver extends BroadcastReceiver {
    private Logger mLogger = Logger.getLogger(NotificationBroadcast.class);

    String TAG = Start.TAG;

    public WifiReceiver() {
    }

    static boolean isProgress;

    @Override
    public void onReceive(final Context context, Intent intent) {


        if( isProgress ){
            mLogger.info("WifiReceiver:  isProgress = true");
            return;
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final boolean isConnected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
//        int status = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);

        mLogger.info("WifiReceiver:  WIFI connection = " + isConnected);


        if (isConnected) {

            isProgress = true;

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
                    params.put("FAPP","true");

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

                        Toast.makeText(context,""+s,Toast.LENGTH_LONG).show();

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
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.gon)
                            .setTicker(ticker);

                    notificationManager.notify(1, mBuilder.build());

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancel(1);
                        }
                    }, 2000);

                }

                @Override
                public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {

                    mLogger.error("WifiReceiver:운행 정보 전송 실패");
                    String ticker = "서버에 운행 정보를 백업하지 못했습니다";

                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.gon)
                            .setTicker(ticker);

                    notificationManager.notify(1, mBuilder.build());

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                            notificationManager.cancel(1);
                        }
                    }, 2000);


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
