package com.phairy.taxionly;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.util.Log;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class GpsCatcher extends Service implements LocationListener {  //} implements View.OnClickListener{
    private Logger mLogger = Logger.getLogger(GpsCatcher.class);
    private String TAG = Start.TAG;

    SQLiteDatabase database;
    String DATABASENAME = "PART";
    String TABLENAME = "PARTINFO";

    private LocationManager locationManager;
    private LocationListener locationListener;
    static Location mlocation = null;

    public static Context context;

    public boolean first = true;

    static int distance;

    private double x1, x2, x3;
    private double y1, y2, y3;
    private double v1, v3, v2;
    private double e1, e2, e3;
    static String fileName;

    static long startTime;
    static int timeLimit;

    static int t;  //시간 측정을 할까...?
    int i = 2;  // 3번째 점 부터 저장

    int isWorking;
    static boolean trigger = false;

    File file;
    FileOutputStream fos;
    SharedPreferences pref;     //비정상 종료시 마지막으로 수정된 파일 이름에 저장
    Calendar calendar;
    Calendar firstTime;
    Calendar secTime;

    int dailyCount = 0;

    static ArrayList<Double> lonList = new ArrayList<>();
    static ArrayList<Double> latList = new ArrayList<>();
    static ArrayList<Double> distList = new ArrayList<>();
    static ArrayList<Integer> nightList = new ArrayList<>();
    static ArrayList<Integer> timeList = new ArrayList<>();


    @Override
    public void onCreate() {
        super.onCreate();

        context = this;

        isWorking = Start.getIsWorking(context);

        pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        timeLimit = pref.getInt("timeLimit", 6);
        mLogger.info("onCreate_ isWorking == " + isWorking + ", 작업 시간 - " + timeLimit);

        calendar = Calendar.getInstance();

        if (isWorking == 1) {

            NotificationBroadcast.setNotification(context, 2);  // 안전 운행 시작!


            startTime = System.currentTimeMillis();
            fileName = "" + String.format("%02d", calendar.get(Calendar.MONTH) + 1) + "-" + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) +
                    " " + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + "";


            SharedPreferences.Editor editor;

            editor = pref.edit();
            editor.putLong("startTime", startTime);
            editor.putString("fileName", fileName);
            editor.commit();

        } else if (isWorking == 2) {
            startTime = pref.getLong("startTime", 0);
            fileName = pref.getString("fileName", "errorFile");
            distance = pref.getInt("distance", 0);
        } else {
            mLogger.error("onCreate_isWorking == 0 BUT restarted. fileName = " + fileName);
        }


        String mSdPath;

        String ext = Environment.getExternalStorageState();
        if (ext.equals(Environment.MEDIA_MOUNTED)) {
            mSdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            mSdPath = Environment.MEDIA_UNMOUNTED;
        }

        file = new File(mSdPath + File.separator + "TaxiOnly" + File.separator + "Temp" + File.separator + fileName + ".gpx");  //파일 생성!


        if (isWorking == 1) {


            try {
                File dir = new File(mSdPath + File.separator + "TaxiOnly" + File.separator + "Temp" + File.separator);
                dir.mkdir();

                fos = new FileOutputStream(file, true);  //mode_append
                String header = "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" " +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" version=\"1.1\" " +
                        "creator=\"TAXIONLY\" xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">" + System.lineSeparator() + "" +
                        "<trk>" + System.lineSeparator() + "" +
                        "<name>TAXIONLY</name>" + System.lineSeparator() + "<trkseg>" + System.lineSeparator() + "";

                fos.write(header.getBytes());
                fos.close();

                Start.toggleIsWorking(context, 2);  // 파일 생성 끝
                isWorking = 2;
                mLogger.debug("onCreate_file 생성... 이름 = " + fileName + "");

            } catch (Exception e) {
                mLogger.error("onCreate_file 생성/오픈 오류");
            }
        } else if (isWorking == 2) {
            mLogger.error("onCreate_file이 생성되어 있음");
        }


        if (isWorking != 0) {         // 종료되지 않을때
            unregisterRestartAlarm();

            try {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);     //GPS로부터 위치정보 업데이트를 요청함

                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_FINE); //정확도 설정
//        criteria.setPowerRequirement(Criteria.NO_REQUIREMENT); //전력 소모량
                String bestProvider = locationManager.getBestProvider(criteria, false);

                locationListener = this;
                locationManager.requestLocationUpdates(bestProvider, 5000, 0, this);
                t = 5; //주기를 5초로 설정
                //기지국으로부터 위치정보 업데이트를 요청함

            } catch (Exception e) {
                mLogger.error("onCreate_ locationManager Error");
            }

        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (isWorking != 0) {

            Thread thread = new Thread() {

                public void run() {
                    super.run();

                    while (isWorking != 0) {

                        try {
                            Thread.sleep(t * 1000); //1초 또는 5초에  실행중 띄움


                            mLogger.debug("GPSCatcher:onStartCommand_servive 실행 중 - " + t + "초 간격, " + (System.currentTimeMillis() - startTime) / 1000 + "초 경과");


                            if (((System.currentTimeMillis() - startTime) / (60000) >= timeLimit) || trigger) {     //6시간 이상 일을 하였다면 /3600*1000 == 1h ,  /1000 == 1s으로 설정 /60000은 1분

                                try {
                                    mLogger.error(":onStartCommand_servive 작업 종료됨 ");

                                    Start.toggleIsWorking(context, 0);  // 종료
                                    isWorking = 0;

                                    if (!trigger) {      //수동 종료아닐 때 상단 알림

                                        NotificationBroadcast.setNotification(getContext(), 3); //timeout으로 종료 상단바 알림

                                    } else {              //수동 종료일 때 상단바 알림 제거
                                        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                                        notificationManager.cancel(0);
                                    }


                                    updatePart();           //이동 거리 업데이트


                                    locationManager.removeUpdates(locationListener);
                                    locationManager = null;

                                } catch (Exception e) {

                                    mLogger.error("onStartCommand_error on setNotify or removeUpdate or toggleGPSCatcher");

                                }

                                try {

                                    fos = new FileOutputStream(file, true);  //mode_append
                                    calendar = Calendar.getInstance();
                                    String waypoint = "<wpt lon=\"" + x2 + "\" lat=\"" + y2 + "\">" + System.lineSeparator() + "" + "<name>fin " +
                                            String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" +
                                            String.format("%02d", calendar.get(Calendar.SECOND)) + "</name></wpt>";
                                    fos.write(waypoint.getBytes());

                                    String trailer = "</trkseg></trk>" + System.lineSeparator() + "" + "</gpx>";


                                    fos.write(trailer.getBytes());
                                    fos.close();


                                    String mSdPath;
                                    String ext = Environment.getExternalStorageState();
                                    if (ext.equals(Environment.MEDIA_MOUNTED)) {
                                        mSdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                                    } else {
                                        mSdPath = Environment.MEDIA_UNMOUNTED;
                                    }

                                    File f = new File(mSdPath + File.separator + "TaxiOnly" + File.separator + "Temp" + File.separator + fileName + ".gpx");
                                    if(!f.renameTo(new File(mSdPath + File.separator + "TaxiOnly" + File.separator + "GPS" + File.separator + fileName + ".gpx"))){
                                        mLogger.error(" onStartCommand_파일 이동 실패. 파일이 없거나 이상함");
                                    }

                                } catch (Exception e) {

                                    mLogger.error(" onStartCommand_파일 마무리 에러");
                                }

                                Intent i = new Intent(getApplicationContext(), MainMenu.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("HashMap", getData());
                                bundle.putInt("action", 1234);
                                bundle.putInt("dailyCount", dailyCount);
                                bundle.putInt("distance", distance);
                                 /*
                (intent내부 - int flag)
                          ( ㄴBundle data - int action)
                                          ㄴSerializable Hashmap - lonList
                                          ㄴint dailyCount       ㄴlatList
                                          ㄴint distance         ㄴtimeList
                                                                 ㄴ ...

                                */
                                i.putExtra("data", bundle);
                                i.putExtra("flag", 12345);  //주행 종료를 의미

                                startActivity(i);

                                mLogger.info(" onStartCommand_ Activity Start, StopSelf...");

                                stopSelf();

                            }
                        } catch (InterruptedException e) {

                            e.printStackTrace();
                        }
                    }
                }
            };
            thread.start();

        } else { //isWorking == false라면
            mLogger.error("onStartCommand_ but isWorking ==  " + isWorking);

            stopSelf();

        }

        return super.onStartCommand(intent, START_STICKY, startId);
    }


    @Override
    public void onLocationChanged(Location location) {

        int intervalTime;
        double intervalDistance;

        mlocation = location;

        x3 = mlocation.getLatitude();
        y3 = mlocation.getLongitude();
        v3 = mlocation.getSpeed();  // m/s
        e3 = mlocation.getAltitude();

        if (first) {             //   p0  p1   p2  p3... 일때 p2부터 기록 시작
            mLogger.debug("GPSCatcher:onCreate_GPS 수신 시작..." + i);


            x1 = x2;
            y1 = y2;
            e1 = e2;
            x2 = x3;
            y2 = y3;
            e2 = e3;
            v2 = v3;

            i--;

            if (i == 0) {                               // p = 3 일 때
                first = false;

                try {
                    fos = new FileOutputStream(file, true);  //mode_append
                    String waypoint = "<wpt lon=\"" + x1 + "\" lat=\"" + y1 + "\">" +
                            "<name>출발 지점</name>" +
                            "</wpt>";
                    fos.write(waypoint.getBytes());
                    fos.close();

                    secTime = Calendar.getInstance();
                } catch (Exception e) {

                }
                return;
            } else if (i == 1) {
                firstTime = Calendar.getInstance();      // p = 1 일 때
                return;
            }


        }

        double maxInterval = 0;
        double c;

        float[] result = new float[3];

        Location.distanceBetween(x1, y1, x2, y2, result); // m 단위

        //TODO 속력을 시간값으로 나누서 다시 해야함!


        if (result[0] <= 166) { //166미터 이내일때

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

                intervalDistance =  result[0];
                distance += intervalDistance;


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

                Location.distanceBetween(x1, y1, x2, y2, result);  //보정된 x2,y2로 다시 계산 meter!!
                intervalDistance = result[0];
                distance += intervalDistance;
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
            intervalDistance =  result[0];
            distance += intervalDistance;

        }

        x1 = x2;
        y1 = y2;
//        e1 = e2;
        x2 = x3;
        y2 = y3;
        v2 = v3;
        e2 = e3;
//        v1 = v2; //v1을 저장할 필요가 있나?


        firstTime = secTime;
        secTime = calendar;
        calendar = Calendar.getInstance();

        intervalTime = (int) (secTime.getTimeInMillis() - firstTime.getTimeInMillis()) / 1000; //초단위로 환산

        try {
            timeList.add(intervalTime);                                                                                // Tn+1 - Tn ,second
            lonList.add(y2);                                                                                          // yn+1
            latList.add(x2);                                                                                          // xn+1
            distList.add(intervalDistance);                                                                           // (xn,yn)~(xn+1,yn+1), ,meter
            if (secTime.get(Calendar.HOUR_OF_DAY) < 4 || secTime.get(Calendar.HOUR_OF_DAY) > 0) { //할증
                nightList.add(1);
            } else {
                nightList.add(0);
            }
            //할증 여부
            dailyCount++;
        } catch (Exception e) {
            e.printStackTrace();
            mLogger.debug("GPSCatcher:onCreate_GPS 데이터 저장 에러!!");
        }
        try {

            String gpsLog = "<trkpt lat=\"" + x2 + "\" lon=\"" + y2 + "\"><ele>" + e2 + "</ele>" +
                    "<time>" + String.format("%d", calendar.get(Calendar.YEAR)) + "-" + String.format("%02d", calendar.get(Calendar.MONTH) + 1) + "-" + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) + "T" +
                    String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE)) + ":" + String.format("%02d", calendar.get(Calendar.SECOND)) + "Z</time></trkpt>" + System.lineSeparator() + "";


//        if( location.getSpeed() < 10 ){
//            locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
//            Log.d(TAG, "GPSCatcher:onLocationChanged_(" + x2 + "), (" + y2 + "), < 10");
//            t=1;
//        }else{
//            locationManager.requestLocationUpdates(bestProvider, 5000, 0, this);
//            Log.d(TAG, "GPSCatcher:onLocationChanged_(" + x2 + "), (" + y2 + "), >= 10");
//            t=5;
//        }


            fos = new FileOutputStream(file, true);  //mode_append
            fos.write(gpsLog.getBytes());
            fos.close();
        } catch (Exception e) {
            mLogger.error("onLocationChanged_파일 출력 에러");
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        switch (i) {
            case LocationProvider.OUT_OF_SERVICE:
                mLogger.error("onStatusChanged_서비스 지역이 아닙니다");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                mLogger.error("onStatusChanged_일시적인 장애로 인하여 위치정보를 수신할 수 없습니다.");
                break;
            case LocationProvider.AVAILABLE:
                mLogger.error("onStatusChanged_GPS 수신 가능 상태");
                break;
        }
    }

    @Override
    public void onProviderEnabled(String s) {
        mLogger.info("onProviderEnabled_위치정보 공급 가능");
    }

    @Override
    public void onProviderDisabled(String s) {
        mLogger.error("onProviderDisabled_위치정보 공급받을수 없음");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        throw new UnsupportedOperationException("Not yet implemented");

    }


    static public String getFileName() {
        return fileName;
    }
    /*
        static public int getDistanceMeter() {
            return distance;
        }
    */
    private Context getContext() {
        return this;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        SharedPreferences.Editor editor = pref.edit();

        if (isWorking != 0) {

            registerRestartAlarm();

            mLogger.debug("onDestroy_Destroy 호출 ㅡㅡㅡㅡㅡㅡㅡㅡ 서비스를 계속 진행합니다..." + distance + "m 주행 중");

            editor.putInt("distance", distance);
            editor.commit();

        } else {

            mLogger.debug("onDestroy_Destroy 호출 ㅡㅡㅡㅡㅡㅡㅡㅡ 종료! 현재 이동 거리 =  " + distance + "m 주행,  " + fileName + "에 저장되었습니다");

            editor.putInt("distance", 0);
            editor.commit();

        }
    }

    private void updatePart() {
        mLogger.debug(":updatePart_주행 기록 갱신...");

        database = context.openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
        Cursor cursor = database.rawQuery("Select partCurrentValue,partName,etc FROM " + TABLENAME, null);

        int size = cursor.getCount();   // 자동차 부품 총 량

        double value;

        Double d;
        for (int i = 0; i < size; i++) {

            cursor.moveToPosition(i);
            value = cursor.getDouble(0);

            String etc = cursor.getString(2);

            if (etc.equals("km")) {   // etc로 km와 day 구분

                value += Double.parseDouble(String.format("%.1f", ((double) distance) / 1000));  //100m단위 까지

            } else {
                //          TODO   km가 아니면 하루 추가...는 여기서는 안하겠음

//                value++;

            }

            database.execSQL("UPDATE " + TABLENAME + " SET partCurrentValue = '" + value + "' WHERE partName = '" + cursor.getString(1) + "'");


            mLogger.error("GpsCatcher:updatePart_ " + cursor.getString(1) + "의 값이 " + value + etc + " 증가...");

        }
        cursor.close();
    }

    public void registerRestartAlarm() {

        mLogger.info("registerRestartAlarm_GPS 유지 알람 등록");

        Intent intent = new Intent(GpsCatcher.this, NotificationBroadcast.class);
        intent.putExtra("flag", 2000);           //flag = 2000
        PendingIntent pendingIntent = PendingIntent.getBroadcast(GpsCatcher.this, 2000, intent, 0); //requestcode와 flag?
        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 01 * 1000; //1초 후 알림이벤트 //

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 10, pendingIntent); //1초후 10초마다 알림 이벤트

    }

    public void unregisterRestartAlarm() {

        mLogger.info("unregisterRestartAlarm_GPS 유지 알람 삭제");

        Intent intent = new Intent(GpsCatcher.this, NotificationBroadcast.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(GpsCatcher.this, 2000, intent, 0); //requestcode와 flag?

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

    }

    static public Location getLocation() {
        return mlocation;
    }


    static public HashMap getData() {

        HashMap<String, ArrayList> data = new HashMap<>();
        data.put("lonList", lonList);
        data.put("latList", latList);
        data.put("distList", distList);
        data.put("timeList", timeList);
        data.put("nightList", nightList);

        return data;
    }
}

