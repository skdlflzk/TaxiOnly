package com.phairy.taxionly;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class NotificationBroadcast extends BroadcastReceiver {

//    static Context mContext;


    String TAG = Start.TAG;

    public NotificationBroadcast() {

    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

//        mContext = context;
        int flag = intent.getIntExtra("flag", 0);

        Log.e(TAG, "NotificationBroadcast: onReceive_");

        if (flag == 2000) {  // 서비스가 강종이 되었을 경우
            Log.e(TAG, "NotificationBroadcast: onReceive_강제종료...service 재시작");
            Intent mIntent = new Intent(context, GpsCatcher.class);
            context.startService(mIntent);

        } else if (flag == 1000) {  //flag == 1000이면 정상 시작
            Log.e(TAG, "NotificationBroadcast: onReceive_알람! service 시작 요청");
            Intent mIntent = new Intent(context, AlarmActivity.class);
            mIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(mIntent);

//            setNotification(context, 0);

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
        } else if(state == 3){
            intent.putExtra("flag", 4444);  //서비스가 죽었다 누르면 가계부를 작성하러감
            builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        }  else {
            intent.putExtra("flag", 0);
            vi[0] = 0;
            vi[1] = 0;
        }

        PendingIntent pi = PendingIntent.getActivity(context, (int) System.currentTimeMillis(), intent, 0);


        builder.setContentIntent(pi);
//        builder.addAction(R.drawable.notification_template_icon_bg, "C", pi);
//        builder.setVibrate(vi); //노티가 등록될 때 진동 패턴 1초씩 두번.

        String tickText = "";
        int flag = 0;
        if (state == 0) {
            tickText = "운행을 시작하셨습니까?";
            builder.addAction(R.drawable.notification_template_icon_bg, "운행 시작!", pi);
            flag = Notification.FLAG_AUTO_CANCEL;

        } else if (state == 1) {
            tickText = "";
            flag = Notification.FLAG_NO_CLEAR;

        } else if ( state == 2 ) {
            tickText = "운행 중 입니다 오늘도 안전운전 되세요!";
//        builder.addAction(R.drawable.notification_template_icon_bg, "가계부 작성하기", pi);
//            builder.addAction(R.drawable.notification_template_icon_bg, "", pi);
            flag = Notification.FLAG_NO_CLEAR;

        } else if( state == 3 ) {

            tickText = "운행이 종료되었습니다";

            builder.addAction(R.drawable.notification_template_icon_bg, "가계부 작성하기", pi);
//            builder.addAction(R.drawable.notification_template_icon_bg, "", pi);
            flag = Notification.FLAG_AUTO_CANCEL;
        }
        builder.setContentText(tickText).setSmallIcon(R.drawable.gon);

        Notification notification = builder.build();
        notification.flags = flag;

        notificationManager.notify(0, notification);

    }

}
