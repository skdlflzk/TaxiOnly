package com.phairy.taxionly;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class NotificationBroadcast extends BroadcastReceiver {

    String TAG = Start.TAG;
    public NotificationBroadcast() {
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.


        final Context mContext = context;
        final int flag = intent.getIntExtra("flag",0);

//        Toast.makeText(context, "Alarm Received!", Toast.LENGTH_LONG).show();
        Log.e(TAG, "NotificationBroadcast: onReceive_");

        if(flag == 1000){
//            Vibrator vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
//            vibe.vibrate(70);
            setNotification(context, 0);
//            String question;
//
//            question = "운행을 기록할까요?";
//
//            new AlertDialog.Builder(context).
//                    setIcon(android.R.drawable.ic_dialog_alert).
//                    setTitle("확인").
//                    setMessage(question).
//                    setNegativeButton("취소", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//
//                            setNotification(context, 0);
//
//                        }
//                    }).
//                    setPositiveButton("확인", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialogInterface, int i) {
//                            try {
//
//                                ContentResolver res = mContext.getContentResolver();
//                                boolean gpsEnabled = Settings.Secure.isLocationProviderEnabled(res, LocationManager.GPS_PROVIDER);
//                                if (!gpsEnabled) {
//
//                                    Toast.makeText(mContext, "GPS 수신기를 먼저 켜주세요", Toast.LENGTH_SHORT).show();
//
//
//
//                                    return;
//                                }
//
//                                Log.e(TAG, "Homefragment: serviceButton_GPS를 시작합니다");
//
//                                Intent mIntent = new Intent(context, MainMenu.class);
//                                mIntent.putExtra("flag",flag);
//                                context.startActivity(mIntent);
//
//                            } catch (Exception e) {
//
//                            }
//                            return;
//                        }
//                    }).show();

        }

//        throw new UnsupportedOperationException("Not yet implemented");
    }



    static public void setNotification(Context context, int isWorking) {

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(0);
        long[] vi = new long[2];
        vi[0] = 500;
        vi[1] = 500;

        Intent intent = new Intent(context, Start.class);
        if(isWorking != 3 ){
            intent.putExtra("flag",1000);  //클릭하면 서비스를 실행한다
        }else{
            intent.putExtra("flag",4444);  //서비스가 죽었다 누르면 가계부를 작성하러감
        }
        PendingIntent pi = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setContentTitle("TAXIONLY");

        builder.setContentIntent(pi);
//        builder.addAction(R.drawable.notification_template_icon_bg, "C", pi);
        builder.setVibrate(vi); //노티가 등록될 때 진동 패턴 1초씩 두번.
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);

        String tickText = "";
        int flag ;
        if (isWorking == 0) {
            tickText = "운행을 시작하셨습니까?";
            builder.addAction(R.drawable.notification_template_icon_bg, "운행 시작!", pi);
            flag = Notification.FLAG_ONLY_ALERT_ONCE;

        } else if(isWorking == 1){
            tickText = "";
            flag = Notification.FLAG_NO_CLEAR;

        }else if(isWorking == 2){
        tickText = "운행 중 입니다 오늘도 안전운전 되세요!";
//        builder.addAction(R.drawable.notification_template_icon_bg, "가계부 작성하기", pi);
//            builder.addAction(R.drawable.notification_template_icon_bg, "", pi);
        flag = Notification.FLAG_NO_CLEAR;

        }else{

            tickText = "운행이 종료되었습니다";

            builder.addAction(R.drawable.notification_template_icon_bg, "가계부 작성하기", pi);
//            builder.addAction(R.drawable.notification_template_icon_bg, "", pi);
            flag = Notification.FLAG_ONLY_ALERT_ONCE;
        }
        builder.setContentText(tickText).setSmallIcon(R.drawable.gon);

        Notification notification = builder.build();
        notification.flags = flag;

        notificationManager.notify(0, notification);

    }

}
