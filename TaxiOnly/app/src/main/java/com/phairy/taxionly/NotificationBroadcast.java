package com.phairy.taxionly;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.net.ConnectivityManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;


import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;
import java.util.HashMap;

public class NotificationBroadcast extends BroadcastReceiver {

//    static Context mContext;
private Logger mLogger = Logger.getLogger(NotificationBroadcast.class);

    String TAG = Start.TAG;

    public NotificationBroadcast() {

    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

//        mContext = context;
        int flag = intent.getIntExtra("flag", 0);

        Log.e(TAG, "NotificationBroadcast: onReceive_ ..." + flag);

        if (flag == 2000) {  // 서비스가 강종이 되었을 경우
            Log.e(TAG, "NotificationBroadcast: onReceive_강제종료...service 재시작");
            Intent mIntent = new Intent(context, GpsCatcher.class);
            context.startService(mIntent);

            return ;
        } else if (flag == 1000) {  //flag == 1000이면 정상 시작
            Log.e(TAG, "NotificationBroadcast: onReceive_알람! service 시작 요청");
            Intent mIntent = new Intent(context, AlarmActivity.class);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(mIntent);

//            setNotification(context, 0);
            return ;
        } else if( flag == 44){         //경찰 발견!

            try{
                Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                context.sendBroadcast(it);

            }catch (Exception e){

            }

            try {

                mLogger.debug("NotificationBroadcast: onReceive_전송 중...");
                AsyncHttpSet asyncHttpSet = new AsyncHttpSet(true);

                RequestParams params = new RequestParams();

                if(GpsCatcher.getLocation() != null){
                    double lat = GpsCatcher.getLocation().getLatitude();
                    double lon = GpsCatcher.getLocation().getLongitude();
                    params.put("CODE", "1");  //경찰 코드
                    params.put("LAT", lat);
                    params.put("LON", lon);
                    params.put("FAPP", "true");

                }else{
                    mLogger.error("NotificationBroadcast: 위치 정보 없음, 전송 실패");
                     return;
                }

                asyncHttpSet.post("", params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
//                        Toast.makeText(context.getApplicationContext(), "",Toast.LENGTH_SHORT);

                        mLogger.info("NotificationBroadcast: onReceive_전송 성공");

                        String ticker = "서버에 전송했습니다";


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

                        return ;

                    }

                    @Override
                    public void onFailure(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes, Throwable throwable) {
//                        Toast.makeText(context.getApplicationContext(), "전송 실패 다시 시도해주세요",Toast.LENGTH_SHORT);
                        mLogger.error("NotificationBroadcast: onReceive_전송 실패");
                        String ticker = "전송하지 못했습니다";


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
                            String mSdPath;

                            String ext = Environment.getExternalStorageState();
                            if (ext.equals(Environment.MEDIA_MOUNTED)) {
                                mSdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
                            } else {
                                mSdPath = Environment.MEDIA_UNMOUNTED;
                            }


                            File file = new File(mSdPath + "/TaxiOnly/alert.txt");  //파일 생성!
                            FileOutputStream fos = new FileOutputStream(file, true);  //mode_append

                            Calendar calendar = Calendar.getInstance();

                            String contents = "경찰발견-" + String.format("%02d", calendar.get(Calendar.MONTH) + 1) + "-" + String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH)) +
                                    " " + String.format("%02d", calendar.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", calendar.get(Calendar.MINUTE));

                            if (GpsCatcher.getLocation() != null) {
                                contents += "" + GpsCatcher.getLocation().getLatitude() + ", " + GpsCatcher.getLocation().getLatitude() + ")" + System.lineSeparator();
                            } else {
                                contents += System.lineSeparator();
                            }

                            fos.write(contents.getBytes());
                            fos.close();

                        }catch (Exception e){

                        }
                        return ;
                    }
                });


            } catch (Exception e) {
                mLogger.error("NotificationBroadcast: onReceive_전송 실패_unhandled ERROR");
                return ;
            }


        }

    }


    static public void setNotification(Context context, int state) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        try {

            notificationManager.cancel(0);

        } catch(Exception e) {

        }
        long[] vi = new long[2];
        vi[0] = 500;
        vi[1] = 500;

        Intent intent = new Intent(context, Start.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);


//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("TAXIONLY");

        if (state == 0) {
            intent.putExtra("flag", 1000);  //클릭하면 서비스를 실행한다
            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        } else if (state == 3) {
            intent.putExtra("flag", 4444);  //서비스가 종료됨 / (가계부를 작성하러감)
            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        } else {                        //state==2
            intent.putExtra("flag", 0);
            vi[0] = 0;
            vi[1] = 0;
        }

        PendingIntent pi = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);


//        builder.addAction(R.drawable.notification_template_icon_bg, "C", pi);
//        builder.setVibrate(vi); //노티가 등록될 때 진동 패턴 1초씩 두번.

        String tickText = "";
        int flag = 0;
        if (state == 0) {
            tickText = "운행을 시작하셨습니까?";
            builder.addAction(R.drawable.notification_template_icon_bg, "운행 시작!", pi);
            flag = Notification.FLAG_AUTO_CANCEL;

            //여기는 이제 안옴

        } else if (state == 1) {
            tickText = "";
            flag = Notification.FLAG_NO_CLEAR;

        } else if ( state == 2 ) { // 운행 중 표시

         //TODO 언제시작했고, 운행종료까지 몇분이 남았는지!

            tickText = "운행 중 입니다 오늘도 안전운전 되세요!";

            Intent mintent = new Intent(context, NotificationBroadcast.class);
            mintent.putExtra("flag", 44);

            PendingIntent dynamicBroad = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), mintent, 0);
            builder.addAction(R.drawable.notification_template_icon_bg, "경찰 주의!", dynamicBroad);

            flag = Notification.FLAG_NO_CLEAR;


        } else if( state == 3 ) {   // 시간 도달 정상 종료

            tickText = "운행이 종료되었습니다";

//            intent = new Intent(context, HouseholdChartActivity.class);
//            intent.putExtra("Hashmap",GpsCatcher.getDada());
//            pi = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);

//            builder.addAction(R.drawable.notification_template_icon_bg, "가계부 작성하기", pi);
//            builder.addAction(R.drawable.notification_template_icon_bg, "", pi);
            flag = Notification.FLAG_AUTO_CANCEL;

        }

        builder.setContentIntent(pi);
        builder.setContentText(tickText).setSmallIcon(R.drawable.gon);

        Notification notification = builder.build();
        notification.flags = flag;

        notificationManager.notify(0, notification);

    }


}
