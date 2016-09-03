
package com.phairy.taxionly;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.databinding.DataBindingUtil;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.phairy.taxionly.databinding.HomeFragmentBinding;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

//TODO mpandroidchartlibrary-2-2-4 추가됨  API level 8 이상, 아파치 2.0 라이센스 애니메이션 쓰면 11이상
//https://github.com/PhilJay/MPAndroidChart

public class HomeFragment extends Fragment {

    private Logger mLogger = Logger.getLogger(HomeFragment.class);
    private String TAG = Start.TAG;

    static public Context context;

//    private Button serviceButton;
    private TextView logtext;

    static HomeFragmentBinding binding;
    private ViewGroup layoutGraphView;


    private LineChart mChart;
    private SeekBar mSeekBarX, mSeekBarY;
    private TextView tvX, tvY;

    public HomeFragment() {
        context = getActivity();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment,container,false);
        View view = binding.getRoot();
        mLogger.error("--HomeFragment--" + MainMenu.class.getSimpleName());

        logtext = (TextView )view.findViewById(R.id.logText);

        layoutGraphView = (ViewGroup) view.findViewById(R.id.graphView);



//        setLineGraph();


        binding.TakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                view.setEnabled(false);

                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        try {

                            ArrayList<Double> lonList = new ArrayList<>();
                            ArrayList<Double> latList = new ArrayList<>();
                            ArrayList<Double> distList = new ArrayList<>();
                            ArrayList<Integer> nightList = new ArrayList<>();
                            ArrayList<Integer> timeList = new ArrayList<>();
                            int distance = 0;
                            mLogger.debug("파싱 시작");

                            String data = null;
                            InputStream inputStream = getResources().openRawResource(R.raw.data2);
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            mLogger.debug("파일 지명");

                            try {
                                int i = inputStream.read();
                                while (i != -1) {
                                    byteArrayOutputStream.write(i);
                                    i = inputStream.read();
                                }

                                data = new String(byteArrayOutputStream.toByteArray(), "UTF-8");
                                inputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            mLogger.debug("data 파일 읽어옴");
                            StringTokenizer entertoken = new StringTokenizer(data, "\n");

                            StringTokenizer tabtoken = new StringTokenizer(entertoken.nextToken(), "\t");    //lat
                            double lattitude1 = Double.parseDouble(tabtoken.nextToken());//lattitude
                            double longitude1 = Double.parseDouble(tabtoken.nextToken());//longitude

                            double lattitude2;
                            double longitude2;

                            String[] strs = tabtoken.nextToken().split(":");
                            String h = strs[0];
                            String m = strs[1];
                            String s = strs[2];

                            int dailycount = entertoken.countTokens();

                            mLogger.debug("초기값 완료 dailycount = " + dailycount + "(" + lattitude1 + "," + longitude1 + ")");

                            Calendar c1 = Calendar.getInstance();

                            c1.set(Calendar.HOUR_OF_DAY, Integer.parseInt(h));   //1~24 범위(아마)
                            c1.set(Calendar.MINUTE, Integer.parseInt(m));
                            c1.set(Calendar.SECOND, Integer.parseInt(s));

                            int intervalTime = 0;
                            for (int i = 0; i < entertoken.countTokens(); i++) {
                                tabtoken = new StringTokenizer(entertoken.nextToken(), "\t");    //lat

                                lattitude2 = Double.parseDouble(tabtoken.nextToken());//lattitude
                                longitude2 = Double.parseDouble(tabtoken.nextToken());//longitude


                                strs = tabtoken.nextToken().split(":");
                                h = strs[0];
                                m = strs[1];
                                s = strs[2];

                                Calendar c2 = Calendar.getInstance();

                                if (Integer.parseInt(h) == 0) {
                                    int t = 24;
                                    c2.set(Calendar.HOUR_OF_DAY, t);
                                } else {
                                    c2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(h));   //1~24 범위(아마)
                                }
                                c2.set(Calendar.MINUTE, Integer.parseInt(m));
                                c2.set(Calendar.SECOND, Integer.parseInt(s));

                                intervalTime = (int) (c2.getTimeInMillis() - c1.getTimeInMillis()) / 1000; //초단위로 환산

                                if (intervalTime < 0) {
                                    c2.set(Calendar.HOUR_OF_DAY, Integer.parseInt(h) + 1);   //1~24 범위(아마)  24

                                    intervalTime = (int) (c2.getTimeInMillis() - c1.getTimeInMillis()) / 1000; //초단위로 환산
                                }

                                float[] result = new float[3];
                                Location.distanceBetween(lattitude1, longitude1, lattitude2, longitude2, result); // m 단위
                                double intervalDistance = result[0];
                                distance += intervalDistance;

                                timeList.add(intervalTime);                                                                                // Tn+1 - Tn ,second
                                lonList.add(longitude2);                                                                                          // yn+1
                                latList.add(lattitude2);                                                                                          // xn+1
                                distList.add(intervalDistance);                                                                           // (xn,yn)~(xn+1,yn+1), ,meter
                                if (c2.get(Calendar.HOUR_OF_DAY) < 4 || c2.get(Calendar.HOUR_OF_DAY) > 0) { //할증
                                    nightList.add(1);
                                } else {
                                    nightList.add(0);
                                }

//                                    mLogger.debug("좌표 = (" + lattitude2 + ", " + longitude2 + "), 이동 거리 = " + intervalDistance + "m, 속도 = " + 3.6 * intervalDistance / intervalTime + "m/s, 총 거리 = " + distance + ", 지금 시각 = " + h + "시 " + m + "분 " + s + "초 , " + intervalTime + "초 간격");

                                c1 = c2;
                                lattitude1 = lattitude2;
                                longitude1 = longitude2;

                            }
                            mLogger.error(lonList.size() + "개의 분석 끄");
                            HashMap<String, ArrayList> hashMap = new HashMap<>();
                            hashMap.put("lonList", lonList);
                            hashMap.put("latList", latList);
                            hashMap.put("distList", distList);
                            hashMap.put("timeList", timeList);
                            hashMap.put("nightList", nightList);

                            Bundle bundle = new Bundle();
                            bundle.putSerializable("HashMap", hashMap);
                            bundle.putInt("action", 1234);
                            bundle.putInt("dailyCount", dailycount);
                            bundle.putInt("distance", distance);


                            Intent intent1 = new Intent(getActivity().getApplicationContext(), HouseholdChartActivity.class);
                            intent1.putExtra("flag", 123); // 주행 끝 데이터 전달
                            intent1.putExtra("data", bundle);      //hashmap만을 전달하는 셈
                            intent1.setAction("CREATE");
                            startActivity(intent1);

                        } catch (Exception e) {

                            e.printStackTrace();
                            mLogger.debug("error while parsing data");
                        }
                    }
                };
                Thread t = new Thread(run);
                t.run();
                mLogger.error("읏!");

            }
        });

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
/*
그래프
 */

        ArrayList<String> labels = new ArrayList<String>();
        labels.add("January");
        labels.add("February");
        labels.add("March");
        labels.add("April");
        labels.add("May");
        labels.add("June");

        ArrayList<Entry> entries = new ArrayList<>();
        entries.add(new Entry(4f, 0));
        entries.add(new Entry(8f, 1));
        entries.add(new Entry(6f, 2));
        entries.add(new Entry(2f, 3));
        entries.add(new Entry(18f, 4));
        entries.add(new Entry(9f, 5));

        LineDataSet lineDataSet = new LineDataSet(entries,"# of Ex-Rates");

        lineDataSet.setColors(ColorTemplate.VORDIPLOM_COLORS);
        lineDataSet.setDrawCubic(true);
        lineDataSet.setDrawFilled(true); //선아래로 색상표시
        lineDataSet.setDrawValues(false);

        LineData lineData = new LineData(labels, lineDataSet);
        binding.chart.setData(lineData); // set the data and list of lables into chart

        MarkerView mv = new CustomMarkerView(getActivity(),R.layout.content_marker_view) ;//new CustomMarkerView(this,R.layout.content_marker_view);
        binding.chart.setMarkerView(mv);
        binding.chart.setDrawMarkerViews(true);

        YAxis y = binding.chart.getAxisLeft();
        y.setTextColor(Color.WHITE);

        XAxis x = binding.chart.getXAxis();
        x.setTextColor(Color.WHITE);

        Legend legend = binding.chart.getLegend();
        legend.setTextColor(Color.WHITE);

        binding.chart.animateXY(2000, 2000); //애니메이션 기능 활성화
        binding.chart.invalidate();





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
    public class CustomMarkerView extends MarkerView {

        private TextView tvContent;

        public CustomMarkerView (Context context, int layoutResource) {
            super(context, layoutResource);
            // this markerview only displays a textview
            tvContent = (TextView) findViewById(R.id.tvContent);
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            tvContent.setText("" + e.getVal()); // set the entry-value as the display text
        }

        @Override
        public int getXOffset(float xpos) {
            // this will center the marker-view horizontally
            return -(getWidth() / 2);
        }

        @Override
        public int getYOffset(float ypos) {
            // this will cause the marker-view to be above the selected value
            return -getHeight();
        }
    }
}