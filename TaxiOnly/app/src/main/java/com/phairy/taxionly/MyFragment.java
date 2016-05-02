
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
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;


public class MyFragment extends Fragment {
    String TAG = Start.TAG;
    static int d = 6;

    private SharedPreferences  pref;
    private SharedPreferences.Editor editor;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "--MyFragment--");
        View view = inflater.inflate(R.layout.my_fragment, container, false);

        pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);

        int h = pref.getInt("timeHour",10);
        int m = pref.getInt("timeMinute",0);
        d = pref.getInt("timeLimit",8);

        EditText H = (EditText) view.findViewById(R.id.HourInput);
        EditText M = (EditText) view.findViewById(R.id.MinuteInput);
        EditText D = (EditText) view.findViewById(R.id.DurationInput);

        H.setText(""+h);
        M.setText(""+m);
        D.setText("" + d);

        Button TakeButton = (Button) view.findViewById(R.id.TakeButton);

        TakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText hinput = (EditText) getActivity().findViewById(R.id.HourInput);
                EditText minput = (EditText) getActivity().findViewById(R.id.MinuteInput);
                EditText dinput = (EditText) getActivity().findViewById(R.id.DurationInput);

                int h = Integer.parseInt(hinput.getText().toString());
                int m = Integer.parseInt(minput.getText().toString());
                d = Integer.parseInt(dinput.getText().toString());

                enrollAlarm(h, m);

            }
        });

        return view;
    }

    private void enrollAlarm(int hour, int minute) {


        editor = pref.edit();

        editor.putInt("timeLimit", d);
        editor.putInt("timeHour", hour);
        editor.putInt("timeMinute", minute);
        editor.apply();


        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);   //1~24 범위(아마)
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 00);

        Log.e(Start.TAG, "MyFragment :  enrollAlarm_ 매일 " + hour + "시 " + minute + "분 예약되었습니다");

        Toast.makeText(getActivity(), "기록 시간을 " + hour + "시 " + minute + "분에 " + d + "시간으로 지정했습니다", Toast.LENGTH_SHORT).show();

        AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(getActivity().ALARM_SERVICE);
        Intent intent2 = new Intent(getActivity(), AlarmActivity.class);
        intent2.putExtra("flag", 1000);
        // intent2.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 1111, intent2, PendingIntent.FLAG_UPDATE_CURRENT);


        alarmManager.cancel(pendingIntent);

//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);  //부정확 / 배터리 절약
//        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);      //정확 / 배터리 소모
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);      //정확 / 배터리 소모


        /*

           setRepeating으로 setExact를 실행하게 하면 배터리가 덜 나가지 않을까?

         */
//        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+1000*60*60*24, AlarmManager.INTERVAL_DAY, pendingIntent);//1000==1초 1000*60*60*24//하루 뒤에 시작!

    }

    static int getDuration() {
        return d;
    }

}

 