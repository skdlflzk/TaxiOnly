package com.phairy.taxionly;

import android.app.FragmentManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.Fragment;
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

public class GpsCatcher extends Service implements LocationListener {  //} implements View.OnClickListener{

    private String TAG = Start.TAG;

    private LocationManager locationManager;
    static Location mlocation;
    private String bestProvider;
    private LocationListener locationListener;

    public static Context context;

    Thread thread;

    private String gpsLog;
    private int timeLimit;
    public boolean first;
    static double distance;
    double x1, x2, x3;
    double y1, y2, y3;
    double v1, v3, v2;
    static String fileName;
    static long startTime;
    static long duration;
    int t;  //시간 측정..?
    int i = 2;  // 3번째 점 부터 저장
    public static Boolean isWorking = false;

    File file;
    FileOutputStream fos;


    public GpsCatcher() {

        Calendar calendar = Calendar.getInstance();
        first = true;
        startTime = System.currentTimeMillis();
        fileName = "" + (calendar.get(Calendar.MONTH) + 1) + calendar.get(Calendar.DAY_OF_MONTH) + calendar.get(Calendar.HOUR_OF_DAY)+calendar.get(Calendar.SECOND);

        String mSdPath;

        String ext = Environment.getExternalStorageState();
        if (ext.equals(Environment.MEDIA_MOUNTED)) {
            mSdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            mSdPath = Environment.MEDIA_UNMOUNTED;
        }

        try {
            File dir = new File(mSdPath + "/TaxiOnly");
            dir.mkdir();

            file = new File(mSdPath + "/TaxiOnly/gpsLog" + fileName + ".txt");

            fos = new FileOutputStream(file, true);  //mode_append
            String header = "<gpx xmlns=\"http://www.topografix.com/GPX/1/0\"" +
                    "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.0\" " +
                    "creator=\"TAXIONLY\"xsi:schemaLocation=\"http://www.topografix.com/GPX/1/0 http://www.topografix.com/GPX/1/0/gpx.xsd\">\n" +
                    "<trk>\n" +
                    "<name>트랙 경로<name/>\n" +
                    "<desc>Total Length = distance</desc>\n" +
                    "<trkseg>";
            fos.write(header.getBytes());
            fos.close();

        } catch (Exception e) {
            Log.e(TAG, "GPSCatcher:GpsCatcher_file 생성/오픈 오류");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "--GPSCatcher-- onCreate_");
        context = this;

        try {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            //GPS로부터 위치정보 업데이트를 요청함

            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE); //정확도 설정
//        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT); //전력 소모량

            bestProvider = locationManager.getBestProvider(criteria, false);

            locationListener = this;
            locationManager.requestLocationUpdates(bestProvider, 5000, 0, this);
            t = 5; //주기를 5초로 설정
            //기지국으로부터 위치정보 업데이트를 요청함

        } catch (Exception e) {
            Log.e(TAG, "GPSCatcher:onCreate_ locationManager Error");
        }


        if (isWorking) {
            thread = new Thread() {
                //run은 jvm이 쓰레드를 채택하면, 해당 쓰레드의 run메서드를 수행한다.
                public void run() {
                    super.run();
                    Log.d(TAG, "GPSCatcher:onCreate_servive 쓰레드... 현재 isWorking == " + isWorking);
                    while (isWorking) {
                        try {
                            Thread.sleep(t * 1000); //1초 또는 5초에  실행중 띄움


                            long curTime = System.currentTimeMillis();
                            duration = (System.currentTimeMillis() - startTime) / 1000;
                            Log.d(TAG, "GPSCatcher:onCreate_servive 실행 중 - " + t + "초 간격/실행 후 " + duration + "초 경과/isWorking == " + isWorking);

                            if ((System.currentTimeMillis() - startTime) / (1000) > 20) {     //6시간 이상 일을 하였다면 3600*1000 ==1시간

                                try {

                                    Log.e(TAG, "GPSCatcher:onCreate_servive 작업 종료됨 ");

                                    NotificationBroadcast.setNotification(getContext(), 3); //종료 상단바 알림


                                    //TODO 서비스/액티비티에서 다른 프래그먼트의 함수 호출(toggleGPSCatcher())하기.
                                    //액티비티가 꺼져도 서비스가 계속 남아있게 하기
                                    //GPX형식으로 변경하기


//                                    HomeFragment ttm = (HomeFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
//                                    ttm.favoritesButton();
//                                    ((HomeFragment) HomeFragment.context).toggleGPSCatcher();


                                    isWorking = false;

                                    locationManager.removeUpdates(locationListener);
                                    locationManager = null;

                                } catch (Exception e) {

                                    Log.d(TAG, "GPSCatcher : onCreate_error on setNotify or removeUpdate or toggleGPSCatcher");

                                }
                                try {

                                    String trailer = "</trkseg>\n" +"</trk>\n" +"</gpx>";
                                    fos = new FileOutputStream(file, true);  //mode_append
                                    fos.write(trailer.getBytes());
                                    fos.close();

                                } catch (Exception e) {

                                    Log.e(TAG, "GPSCatcher : onCreate_파일 마무리 에러");
                                }

                                Log.d(TAG, "GPSCatcher : onCreate_ stopingSelf...");
                                stopSelf();

                            }
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            };
            thread.start();

        } else { //isWorking == false라면

            stopSelf();

        }


    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        throw new UnsupportedOperationException("Not yet implemented");

    }


    @Override
    public void onLocationChanged(Location location) {

        mlocation = location;

        x3 = mlocation.getLatitude();
        y3 = mlocation.getLongitude();
        v3 = mlocation.getSpeed();  // m/s


        if (first) {             //   p0  p1   p2  p3... 일때 p2부터 기록 시작
            Log.e(TAG, "GPSCatcher:onCreate_servive 수신 시작..." + i);
            x1 = x2;
            y1 = y2;

            x2 = x3;
            y2 = y3;

            v2 = v3;

//            x1 = x2 = x3; y1 = y2 = y3;
//            v2 = v3;
            i--;

            if (i == 0) {
                first = false;
            }
            return;
        }

        double maxInterval = 0;
        double c;

        float[] result = new float[3];

        Location.distanceBetween(x1, y1, x2, y2, result);

        if (result[0] <= 166) {

            // 최대 이동거리 계산
            if (v3 - v2 > 50) {
                maxInterval = ((v3 + v2) / 2) * 5 + (350 / 8) * 5 / 2;
            } else if (v3 - v2 <= 50 && v3 - v2 >= 0) {
                c = 350 + v3 - v2;
                maxInterval = (c / 80) * (2 * v2 + c / 80) / 2 + (t - c / 80) * (2 * v3 + c / 80) / 2;
            } else if (v3 - v2 >= -50 && v3 - v2 < 0) {
                c = v3 - v2 - 350;
                maxInterval = (c / 80) * (2 * v2 - c / 80) / 2 + (t - c / 80) * (2 * v3 - c / 80) / 2;
            } else if (v3 - v2 < -50) {
                maxInterval = ((v2 + v3) / 2) * t + (350 / 8) * t / 2;
            }

            if (result[0] <= maxInterval) { // 용인범위

                distance += result[0];

            } else {  // 에러 인지, 보정

                double L0, L1, L2, alpha, beta, gamma, delta;
                double[] pa = new double[2];
                double[] pb = new double[2];
                double[] pnew = new double[2];

                L0 = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

                pnew[0] = (x1 + x3) / 2;
                pnew[1] = (y1 + y3) / 2;

                L1 = Math.sqrt((pnew[0] - x1) * (pnew[0] - x1) + (pnew[1] - y1) * (pnew[1] - y1));
                L2 = Math.sqrt((x3 - x2) * (x3 - x2) + (y3 - y2) * (y3 - y2));

                alpha = L1 * Math.abs(x2 - x1) / (L0 + L1);
                beta = L1 * Math.abs(y2 - y1) / (L0 + L1);
                gamma = L1 * Math.abs(x3 - x2) / (L2 + L0);
                delta = L1 * Math.abs(y3 - y2) / (L2 + L0);

                pa[0] = x1 + alpha;
                pa[1] = y1 + beta;
                pb[0] = x3 + gamma;
                pb[1] = y3 + delta;

                x2 = (pa[0] + pb[0]) / 2;
                y2 = (pa[1] + pb[1]) / 2;

                Location.distanceBetween(x1, y1, x2, y2, result);  //보정된 x2,y2로 다시 계산
                distance += result[0];
            }

        } else {

            double L0, L1, L2, alpha, beta, gamma, delta;
            double[] pa = new double[2];
            double[] pb = new double[2];
            double[] pnew = new double[2];

            L0 = Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));

            pnew[0] = (x1 + x3) / 2;
            pnew[1] = (y1 + y3) / 2;

            L1 = Math.sqrt((pnew[0] - x1) * (pnew[0] - x1) + (pnew[1] - y1) * (pnew[1] - y1));
            L2 = Math.sqrt((x3 - x2) * (x3 - x2) + (y3 - y2) * (y3 - y2));

            alpha = L1 * Math.abs(x2 - x1) / (L0 + L1);
            beta = L1 * Math.abs(y2 - y1) / (L0 + L1);
            gamma = L1 * Math.abs(x3 - x2) / (L2 + L0);
            delta = L1 * Math.abs(y3 - y2) / (L2 + L0);

            pa[0] = x1 + alpha;
            pa[1] = y1 + beta;
            pb[0] = x3 + gamma;
            pb[1] = y3 + delta;

            x2 = (pa[0] + pb[0]) / 2;
            y2 = (pa[1] + pb[1]) / 2;

            Location.distanceBetween(x1, y1, x2, y2, result);  //보정된 x2,y2로 다시 계산
            distance += result[0];

        }

        x1 = x2;
        y1 = y2;
        x2 = x3;
        y2 = y3;
        v2 = v3;
        v1 = v2; //v1을 저장할 필요가 있나?

        gpsLog = "<trkpt lat=\"" + x2 + "\" lon=\"" + y2 + "\"/>"+"<desc>v="+v2 + ",d=" + String.format("%.3f", distance) + "#<desc/>\n";

//        if( location.getSpeed() < 10 ){
//            locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
//            Log.d(TAG, "GPSCatcher:onLocationChanged_(" + x2 + "), (" + y2 + "), < 10");
//            t=1;
//        }else{
//            locationManager.requestLocationUpdates(bestProvider, 5000, 0, this);
//            Log.d(TAG, "GPSCatcher:onLocationChanged_(" + x2 + "), (" + y2 + "), >= 10");
//            t=5;
//        }

        try {

            fos = new FileOutputStream(file, true);  //mode_append
            fos.write(gpsLog.getBytes());
            fos.close();

        } catch (Exception e) {

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

        return super.onStartCommand(intent, START_NOT_STICKY, startId);
    }

    static public String getFileName() {
        return fileName;
    }

    static public double getDistance() {
        return distance;
    }

    private Context getContext() {
        return this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e(TAG, "GPSCatcher : onDestroy_Destroy 호출ㅡㅡㅡㅡㅡㅡㅡㅡdist =  "+ distance);
    }
}

