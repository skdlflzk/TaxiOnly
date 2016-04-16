
package com.phairy.taxionly;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.util.Calendar;


public class MyFragment extends Fragment {
    String TAG = Start.TAG;
    static int d=6;

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "--MyFragment--");
        View view = inflater.inflate(R.layout.my_fragment, container, false);





        Button TakeButton = (Button) view.findViewById(R.id.TakeButton);
        TakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText hinput = (EditText) getActivity().findViewById(R.id.HourInput);
                EditText minput = (EditText) getActivity().findViewById(R.id.MinuteInput);
                EditText dinput = (EditText) getActivity().findViewById(R.id.DurationInput);

                  pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
                editor = pref.edit();

                boolean isAlarmEnrolled = pref.getBoolean("isAlarmEnrolled", false);
                if (isAlarmEnrolled == false) {
                    Log.e(TAG, "MainManu:onCreate_first excute");

                    int h = Integer.parseInt(hinput.getText().toString());
                    int m = Integer.parseInt(minput.getText().toString());
                    d = Integer.parseInt(dinput.getText().toString());
                    enrollAlarm(h,m);
                    editor.putBoolean("isAlarmEnrolled", true);
                    editor.commit();
                }else{
                    Log.d(TAG, "AccountFragment:onCreateView() / is not first");
                }
            }
        });

        return view;
    }

    private void enrollAlarm(int hour, int minute) {

        long now = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);   //1~24 범위(아마)
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        Log.e(Start.TAG, "MainMenu :  enrollAlarm_"+ hour + "시 " +minute+"분 예약되었습니다");
//        if(now-calendar.getTime()) {
//            return;
//        }

        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(getActivity().ALARM_SERVICE);
        Intent intent2 = new Intent(getActivity(), NotificationBroadcast.class);
        intent2.putExtra("flag", 1000);
        // intent2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent2, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+10000, AlarmManager.INTERVAL_DAY, pendingIntent);//1000==1초 1000*60*60*24//하루 뒤에 시작!
//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1000*60*60*24, AlarmManager.INTERVAL_DAY, pendingIntent);//1000==1초 1000*60*60*24//하루 뒤에 시작!

    }
    static int getDuration(){
        return d;
    }

}

 