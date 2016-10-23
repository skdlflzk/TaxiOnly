package com.phairy.taxionly;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.StringTokenizer;

import com.phairy.taxionly.databinding.HouseholdChartLayoutBinding;

/**
 * Created by aa on 2016-07-13.
 */
public class HouseholdChartActivity extends AppCompatActivity {


    Context context;
    private Logger mLogger = Logger.getLogger(HouseholdChartActivity.class);
    HouseholdChartLayoutBinding binding;

    SQLiteDatabase database;
    String DATABASENAME = "CHART";
    String TABLENAME = "CHARTINFO";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private String action;
    private int index;

    Intent intent;
    HashMap<String, Integer> driveResult;
    Bundle bundle;

    TextView dayText;
    TextView netincome;
    TextView spendingText;
    EditText incomeEdit;
    EditText gasEdit;
    EditText foodEdit;
    EditText etcEdit;
    EditText clientEdit;


    String day;
    int client = -4;
    int distance = -4;
    int money = -4;
    int moneyDist = -4;
    String fileName = "default";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;

        LayoutInflater inflater = LayoutInflater.from(this);
        binding = DataBindingUtil.setContentView(this, R.layout.household_chart_layout);

        setContentView(inflater.inflate(R.layout.household_chart_layout, null));

        String TAG = HouseholdChartActivity.class.getSimpleName();

        /*

            context = this;
            LayoutInflater inflater = LayoutInflater.from(this);

//        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
            setContentView(inflater.inflate(R.layout.activity_alarm, null));

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

         */

        mLogger.info("--HouseholdChart!--" + TAG);


        pref = getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor = pref.edit();

        boolean isCreated = pref.getBoolean("chartCreated", false);

        if (!isCreated) {

            initChartData(context);  //DB 생성 및 초기화

            editor.putBoolean("chartCreated", true);
            editor.apply();

        } else {
            mLogger.info("onCreate() / chart init is not first");

        }

        try {
            intent = getIntent();
            action = intent.getAction();

            distance = intent.getBundleExtra("data").getInt("distance", -1);
            fileName = intent.getBundleExtra("data").getString("fileName");

            mLogger.error("onCreate_ intent gets data  from MainMenu ... " + intent.getBundleExtra("data").getInt("distance", -1) + "km");
            bundle = intent.getBundleExtra("data");
            if (bundle == null) {
                intent.putExtra("flag", 0);
            }
        } catch (Exception e) {
            mLogger.error("onCreate_ intent doesn't have data");
            if (intent.getIntExtra("flag", 0) == 123) {
                Toast.makeText(getApplicationContext(), "주행 정보를 받지 못했습니다", Toast.LENGTH_LONG).show();
            }
        }

        int last = pref.getInt("chartSize", 0);
        index =  intent.getIntExtra("index", last);
        mLogger.info("onCreate() / action == " + action + ", getIntExtra(index = " + intent.getIntExtra("index", last) + ", last = "+ last);


        dayText = (TextView) findViewById(R.id.day);

        netincome = (TextView) findViewById(R.id.netGainText);
        spendingText = (TextView) findViewById(R.id.spendingText);

        incomeEdit = (EditText) findViewById(R.id.incomeEdit);
        //유류비 식비 기타 수입
        gasEdit = (EditText) findViewById(R.id.gasEdit);
        foodEdit = (EditText) findViewById(R.id.foodEdit);
        etcEdit = (EditText) findViewById(R.id.etcEdit);
        clientEdit = (EditText) findViewById(R.id.clientEdit);

        TextView.OnEditorActionListener editorActionListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                switch (textView.getId()) {
                    case R.id.clientEdit:
                        if (intent.getIntExtra("flag", 0) == 123) {

                            HashMap<String, ArrayList> hash = (HashMap<String, ArrayList>) bundle.getSerializable("HashMap");
                            ArrayList<Integer> ar = hash.get("distList");
                            mLogger.error("TEst_distcheck = " + ar.get(0) + ar.get(1) + ar.get(2) + ar.get(3) + ar.get(4) + ar.get(5) + ar.get(6) + ar.get(7) + ar.get(8) + ar.get(9) + ", dailyCount = " + bundle.get("dailyCount"));

                            client = Integer.parseInt(clientEdit.getText().toString());
                            driveResult = getParsedata(bundle, client);
                            Toast.makeText(getApplicationContext(), "money = " + driveResult.get("money") + ", 주행거리 = " + driveResult.get("moneyDist"), Toast.LENGTH_LONG).show();

                            money = driveResult.get("money");
                            moneyDist = driveResult.get("moneyDist");
                        }
                        break;

                    default:
                        switch (id) {
                            case EditorInfo.IME_ACTION_DONE:
                            case EditorInfo.IME_ACTION_NEXT:
                                int gas = Integer.parseInt(gasEdit.getText().toString());
                                int food = Integer.parseInt(foodEdit.getText().toString());
                                int etc = Integer.parseInt(etcEdit.getText().toString());
                                int income = Integer.parseInt(incomeEdit.getText().toString());

                                spendingText.setText("" + (gas + food + etc));
                                netincome.setText("" + (income - gas - food - etc));
                                break;

                            default:
                                //action_done

                        }
                }
                return false;
            }
        };

        incomeEdit.setOnEditorActionListener(editorActionListener);
        gasEdit.setOnEditorActionListener(editorActionListener);
        foodEdit.setOnEditorActionListener(editorActionListener);
        etcEdit.setOnEditorActionListener(editorActionListener);
        clientEdit.setOnEditorActionListener(editorActionListener);

        if (action.equals("SHOW")){        //보여주기

            if (index == 0) {
                mLogger.error("onCreate() /  index == 0 , 데이터가 없음");
                Toast.makeText(getApplicationContext(), " 데이터가 없음!", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                database = context.openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
                Cursor cursor = database.rawQuery("Select * FROM " + TABLENAME, null);

                /*
                + "_id integer PRIMARY KEY autoincrement, "
                        + "distance Integer, "
                        + "client Integer, "
                        + "income Integer, "
                        + "spending Integer, "
                        + "list text, "
                         + "detail text)");
                */


                mLogger.debug("index = " + index);

                cursor.moveToPosition(index - 1); // 0부터 시작하므로
                int distance = cursor.getInt(0);
                int client = cursor.getInt(1);
                int income = cursor.getInt(2);
                int spending = cursor.getInt(3);

                TextView distanceText = (TextView) findViewById(R.id.distanceText);
                TextView clientText = (TextView) findViewById(R.id.clientText);
                TextView incomeText = (TextView) findViewById(R.id.incomeText);
                TextView spendingText = (TextView) findViewById(R.id.spendingText);

//                TextView ;os = (TextView) findViewById(R.id.distanceText);
//                TextView distanceText = (TextView) findViewById(R.id.distanceText);


                TextView gasText = (TextView) findViewById(R.id.gasText);
                TextView foodText = (TextView) findViewById(R.id.foodText);
                TextView etcText = (TextView) findViewById(R.id.etcText);

                String list = cursor.getString(4);
                String detail = cursor.getString(5);
                day = cursor.getString(6);


                // TODO 보여주기 구현


                dayText.setText(day);

                distanceText.setText(""+distance);
                clientText.setText(""+client);
                incomeText.setText(""+income);
                spendingText.setText(""+spending);

                mLogger.debug("list = " + list);
                int front = list.indexOf("1-");
                int end = list.indexOf(",2");

                String  temp = list.substring(front + 2, end);
                int gas = Integer.parseInt(temp);

                front = list.indexOf("2-");
                end = list.indexOf(",3");
                temp = list.substring(front + 2, end);
                int food = Integer.parseInt(temp);

                front = list.indexOf("3-");
                end = list.indexOf("/");
                temp = list.substring(front + 2, end);
                int etc = Integer.parseInt(temp);


                mLogger.debug("불러오기 gas = " + gas + " food = " + food + ",etc = " + etc + "");

                gasText.setText("" + gas);
                foodText.setText(""+food);
                etcText.setText(""+etc);


//                binding.distanceText.setText(distance);
//                binding.clientText.setText(client);
//                binding.incomeText.setText(income);
//                binding.spendingText.setText(spending);
//                binding.netGainText.setText(netGain);
                //binding.etc.setText(etc);

                cursor.close();

            } catch (Exception e) {
                mLogger.error("onCreate() / error while showing chart");
                e.printStackTrace();
            }

        } else if (action.equals("CREATE"))

        {           //생성 시 레이아웃 토글

            Calendar calendar = Calendar.getInstance();
            day = ""+calendar.get(Calendar.MONTH)+"월 "+calendar.get(Calendar.DAY_OF_MONTH)+"일";

            dayText.setText(day);

            // String fileName = intent.getStringExtra("fileName");    //왜 파일 읽음?
            //   mLogger.error("onCreate() / attained fileName = " + fileName);  //왜 파일 읽음?

            driveResult = new HashMap<>();

//            binding.distanceText.setVisibility(View.INVISIBLE);
//            binding.distanceEdit.setVisibility(View.VISIBLE);

            TextView distanceText = (TextView) findViewById(R.id.distanceText);
            EditText distanceEdit = (EditText) findViewById(R.id.distanceEdit);
            distanceText.setVisibility(View.INVISIBLE);
            distanceEdit.setVisibility(View.VISIBLE);

            int distance = intent.getBundleExtra("data").getInt("distance", -1);
            try {
                distanceEdit.setText("" + distance);

            } catch (Exception e) {
                e.printStackTrace();
                mLogger.error("CREATING_" + distance);
            }

//            binding.distanceEdit.setText(distance);

            //driveResult.get("money")
            //driveResult.get("moneyDist")


//            binding.clientText.setVisibility(View.INVISIBLE);
//            binding.clientEdit.setVisibility(View.VISIBLE);
            TextView clientText = (TextView) findViewById(R.id.clientText);
            EditText clientEdit = (EditText) findViewById(R.id.clientEdit);
            clientText.setVisibility(View.INVISIBLE);
            clientEdit.setVisibility(View.VISIBLE);
            clientEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {

                    try {
                        if (intent.getIntExtra("flag", 0) == 123) {

//                            client = Integer.parseInt(editable.toString());


                 /*
                (intent내부 - int flag)
                          ( ㄴBundle data - int action)
                                          ㄴSerializable Hashmap - lonList
                                          ㄴint dailyCount       ㄴlatList
                                                                 ㄴtimeList
                                                                 ㄴ ...
                 */
//                                Toast.makeText(getApplicationContext(), "실행!", Toast.LENGTH_LONG).show();
//                                driveResult = getParsedata(bundle, Integer.parseInt(editable.toString()));
//                                Toast.makeText(getApplicationContext(), "money = " + driveResult.get("money") + ", 주행거리 = " + driveResult.get("moneyDist"), Toast.LENGTH_LONG).show();

//                            binding.distanceEdit.setText(driveResult.get("moneyDist"));    //입력이 끝나면 + km

                        } else {

                            //그냥 열기


                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        mLogger.error("getParsedata_errrrrrrrror");

                    }
                }
            });

            TextView incomeText = (TextView) findViewById(R.id.incomeText);
            EditText incomeEdit = (EditText) findViewById(R.id.incomeEdit);
            incomeText.setVisibility(View.INVISIBLE);
            incomeEdit.setVisibility(View.VISIBLE);

//            binding.incomeText.setVisibility(View.INVISIBLE);
//            binding.incomeEdit.setVisibility(View.VISIBLE);

//            binding.spendingText.setVisibility(View.INVISIBLE);
//            binding.spendingEdit.setVisibility(View.VISIBLE);

            //기본 2개 (만약 추가가 된다면, id를 지정하여 동적으로 계산)
            TextView gasText = (TextView) findViewById(R.id.gasText);
            EditText gasEdit = (EditText) findViewById(R.id.gasEdit);
            gasText.setVisibility(View.INVISIBLE);
            gasEdit.setVisibility(View.VISIBLE);
//            binding.gasText.setVisibility(View.INVISIBLE);
//            binding.gasEdit.setVisibility(View.VISIBLE);
//            gasEdit.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//                }
//
//                @Override
//                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//                }
//
//                @Override
//                public void afterTextChanged(Editable editable) {
//                    try {
//                        TextView spendingText = (TextView) findViewById(R.id.spendingText);
//                        int origin = Integer.parseInt(spendingText.getText().toString());
//                        int addition = Integer.parseInt(editable.toString());
//                        spendingText.setText("" + (origin + addition));
//                        EditText gasEdit = (EditText) findViewById( R.id.gasEdit);
//                        gasEdit.setText(Integer.parseInt(editable.toString()));
//                    } catch (Exception e) {
//
//                    }
//                }
//            });

            TextView foodText = (TextView) findViewById(R.id.foodText);
            EditText foodEdit = (EditText) findViewById(R.id.foodEdit);
            foodText.setVisibility(View.INVISIBLE);
            foodEdit.setVisibility(View.VISIBLE);

//            binding.foodText.setVisibility(View.INVISIBLE);
//            binding.foodEdit.setVisibility(View.VISIBLE);
//            foodEdit.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//                }
//
//                @Override
//                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//                }
//
//                @Override
//                public void afterTextChanged(Editable editable) {
//                    try {
//                        TextView spendingText = (TextView) findViewById(R.id.spendingText);
//                        int origin = Integer.parseInt(spendingText.getText().toString());
//                        int addition = Integer.parseInt(editable.toString());
//                        spendingText.setText("" + (origin + addition));
//
//                        EditText foodEdit = (EditText) findViewById( R.id.foodEdit);
//                        foodEdit.setText(Integer.parseInt(editable.toString()));
//                    } catch (Exception e) {
//
//                    }
//                }
//            });

            TextView etcText = (TextView) findViewById(R.id.etcText);
            EditText etcEdit = (EditText) findViewById(R.id.etcEdit);
            etcText.setVisibility(View.INVISIBLE);
            etcEdit.setVisibility(View.VISIBLE);

//            binding.etcText.setVisibility(View.INVISIBLE);
//            binding.etcEdit.setVisibility(View.VISIBLE);
//            etcEdit.addTextChangedListener(new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//                }
//
//                @Override
//                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//                }
//
//                @Override
//                public void afterTextChanged(Editable editable) {
//                    try {
//                        TextView spendingText = (TextView) findViewById(R.id.spendingText);
//                        int origin = Integer.parseInt(spendingText.getText().toString());
//                        int addition = Integer.parseInt(editable.toString());
//                        EditText etcEdit = (EditText) findViewById( R.id.etcEdit);
//                        etcEdit.setText(Integer.parseInt(editable.toString()));
//                        spendingText.setText("" + (origin + addition));
//
//                    } catch (Exception e) {
//
//                    }
//                }
//            });
            //

            TextView spendingText = (TextView) findViewById(R.id.spendingText);
            spendingText.setText("" + 0);


        } else

        {
            mLogger.error("onCreate() / NO action string ");
            return;
        }

    }

    private void initChartData(Context context) {

        try {

            database = context.openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE if not exists " + TABLENAME + "("
//                    + "_id integer PRIMARY KEY autoincrement, "
                    + "distance Integer, "
                    + "client Integer, "
                    + "income Integer, "
                    + "spending Integer, "
                    + "list text, "
//                    + "netgain Integer, "         //순수익은 지워도...될듯
                    + "detail text, "
                    + "day text)");

        } catch (Exception e) {
            e.printStackTrace();
            mLogger.error("initPartData_ Creating \"" + TABLENAME + "\" Failed");

        } finally {
            database.close();
            database = null;
        }

    }

    public void onConfirmClicked(View v) { //확인 버튼

        if (action.equals("SHOW")) {    //닫기
            finish();

        } else if (action.equals("CREATE") || action.equals("EDIT")) { //생성 action으로 열었을 때

            try {
//                int distance = Integer.parseInt(binding.distanceEdit.getText().toString());
//                int client = Integer.parseInt(binding.clientEdit.getText().toString());
//                int income = Integer.parseInt(binding.incomeEdit.getText().toString());
//                int spending = Integer.parseInt(binding.spendingEdit.getText().toString());
//                //기본 2개 (만약 추가가 된다면, id를 지정하여 동적으로 계산)
//                int gas = Integer.parseInt(binding.gasEdit.getText().toString());
//                int food = Integer.parseInt(binding.foodEdit.getText().toString());
//                int etc = Integet.parseInt(~);
                EditText distanceEdit = (EditText) findViewById(R.id.distanceEdit);
                EditText clientEdit = (EditText) findViewById(R.id.clientEdit);
                EditText incomeEdit = (EditText) findViewById(R.id.incomeEdit);
                EditText spendingEdit = (EditText) findViewById(R.id.spendingEdit);
                EditText gasEdit = (EditText) findViewById(R.id.gasEdit);
                EditText foodEdit = (EditText) findViewById(R.id.foodEdit);
                EditText etcEdit = (EditText) findViewById(R.id.etcEdit);

                int distance = Integer.parseInt(distanceEdit.getText().toString());
                int client = Integer.parseInt(clientEdit.getText().toString());
                int income = Integer.parseInt(incomeEdit.getText().toString());
                int spending = Integer.parseInt(spendingEdit.getText().toString());
                //기본 2개 (만약 추가가 된다면, id를 지정하여 동적으로 계산)
                int gas = Integer.parseInt(gasEdit.getText().toString());
                int food = Integer.parseInt(foodEdit.getText().toString());
                int etc = Integer.parseInt(etcEdit.getText().toString());

                String list = ",1-" + gas + ",2-" + food + ",3-" + etc + "/";
                //

                try {
                    database = context.openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);

                    String detail = "test";
                    mLogger.debug("onConfirmClicked_ 업데이트 값  SET " + " distance = " + distance + " , client =  " + client + " , " + income + ", spending = " + spending + ", list = '" + list + "', detail = '" + detail + "' WHERE" +
                            "_id == " + index);

                    if (action.equals("EDIT")) {

//                        database.execSQL("UPDATE INTO " + TABLENAME + " VALUES " + "( " + distance + ", " + client + ", " + income + ", " + spending + ", '" + list + "', '" + detail + "' )");
                        database.execSQL("UPDATE FROM " + TABLENAME + " SET " + " distance = " + distance + " , client =  " + client + " , income =" + income + ", spending = " + spending + ", list = '" + list + "', detail = '" + detail + "' WHERE" +
                                "_id == " + index); //index-1인지?


                        mLogger.debug("onConfirmClicked_ Chart updated successfully");

                    } else {

                        database.execSQL("INSERT INTO " + TABLENAME + " VALUES " + "( " + distance + ", " + client + ", " + income + ", " + spending + ", '" + list + "', '" + detail + "', '"+ day +"' )");

                        mLogger.debug("onConfirmClicked_ Chart created successfully");


                    }
                } catch (Exception e) {
                    mLogger.error("onConfirmClicked_ ERROR while updating chart");
                }
            } catch (Exception e) {
                mLogger.error("onConfirmClicked_ ERROR while parsing");
            }//catch

            AsyncHttpSet asyncHttpSet = new AsyncHttpSet(false);
            RequestParams params = new RequestParams();

            try {

                params.put("FAPP", "phairy");

                params.put("DISTANCE", distance);
                params.put("MONEY", incomeEdit.getText().toString());
                params.put("CLIENT", client);

                params.put("MONEYDIST", moneyDist);
                params.put("CALMONEY", money);

                params.put("FILENAME", fileName);


            } catch (Exception e) {
                mLogger.error("onConfirmClicked_: GPS 파일 폼 생성 중 에러...");
                e.printStackTrace();
            }


            asyncHttpSet.post("/Inputbackup", params, new AsyncHttpResponseHandler() {   //업데이트할 php의 주소 정하기


                @Override
                public void onSuccess(int i, cz.msebera.android.httpclient.Header[] headers, byte[] bytes) {
                    mLogger.error("onConfirmClicked_: 응답 받음...");
                    try {

                        String ss = new String(bytes);
                        Toast.makeText(getApplicationContext(), ss , Toast.LENGTH_LONG).show();
                        HomeFragment.binding.MainText.setText("" + ss);

                        int r = ss.indexOf("result=");
                        String result = ss.substring(r + 7, r + 8);
                        if (result.equals("1")) {
                            mLogger.warn("onConfirmClicked_:onSuccess result success ");
                        } else {
                            mLogger.warn("onConfirmClicked_:onSuccess but result = " + result);
                        }
                        mLogger.error("onConfirmClicked_:onSuccess contents = " + ss);


                    } catch (Exception e) {

                        e.printStackTrace();
                        mLogger.error("onConfirmClicked_:onSuccess string trans error");

                    }


                    mLogger.info("onConfirmClicked_: 경로 백업 성공");

                    String ticker = "서버에 운행 정보를 백업했습니다";

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

                    mLogger.error("onConfirmClicked_:운행 정보 전송 실패");
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
                }
            });


            int size = pref.getInt("chartSize", 0);
            if(action.equals("CREATE")) {
                size = size + 1;

                editor.putInt("chartSize", size);
            }
            mLogger.error("onConfirmClicked_: " + action + " 되었음, 갱신할 페이지 = " + size);

            editor = pref.edit();

            editor.putInt("chartSize", size);
            editor.apply();

            Intent intent = new Intent(getApplicationContext(), HouseholdChartActivity.class);
            intent.setAction("SHOW");
            intent.putExtra("index", size);
            startActivity(intent);
            finish();


        } else {
            mLogger.error("onConfirmClicked_: create도 아니고 edit 도 아닌데 어떻게 들어왔지? action = " + action);
        }

    }//onConfirmClicked


    public void onEditClicked(View v) {     //수정버튼      // SHOW->EDIT

//        binding.distanceText.setVisibility(View.INVISIBLE);
//        binding.distanceEdit.setVisibility(View.VISIBLE);
//
//        binding.clientText.setVisibility(View.INVISIBLE);
//        binding.clientEdit.setVisibility(View.VISIBLE);
//
//        binding.incomeText.setVisibility(View.INVISIBLE);
//        binding.incomeEdit.setVisibility(View.VISIBLE);
//
//        binding.spendingText.setVisibility(View.INVISIBLE);
//        binding.spendingEdit.setVisibility(View.VISIBLE);
//
//        //기본 2개 (만약 추가가 된다면, id를 지정하여 동적으로 계산)
//        binding.gasText.setVisibility(View.INVISIBLE);
//        binding.gasEdit.setVisibility(View.VISIBLE);
//
//        binding.foodText.setVisibility(View.INVISIBLE);
//        binding.foodEdit.setVisibility(View.VISIBLE);
//
//        int distance = Integer.parseInt(binding.distanceText.getText().toString());
//        int client = Integer.parseInt(binding.clientText.getText().toString());
//        int income = Integer.parseInt(binding.incomeText.getText().toString());
//        int spending = Integer.parseInt(binding.spendingText.getText().toString());
//        //기본 2개 (만약 추가가 된다면, id를 지정하여 동적으로 계산)
//        int gas = Integer.parseInt(binding.gasText.getText().toString());
//
//        int food = Integer.parseInt(binding.foodText.getText().toString());
//
//        String etc = ",1-" + gas + ",2-" + food + "/";
//        //
//
//        int netGain = income - spending;
//
//        binding.distanceEdit.setText(distance);
//        binding.clientEdit.setText(client);
//        binding.incomeEdit.setText(income);
//        binding.spendingEdit.setText(spending);
        //binding.etc.setText(etc);
    }//onEditClicked


    private HashMap getParsedata(Bundle bundle, int client) {

        mLogger.error("getParsedata() / init ");


        HashMap<String, ArrayList> data = (HashMap<String, ArrayList>) bundle.getSerializable("HashMap");

        int dailycount = 1540;//bundle.getInt("dailyCount"); //getDailycount;
        double velostop = 12 / 3.6; //  km/h->m/s는 /3.6 m/s->km/h 는 *3.6

        ArrayList<Double> lonList = data.get("lonList");
        ArrayList<Double> latList = data.get("latList");
        ArrayList<Double> distList = data.get("distList");
        ArrayList<Integer> timeList = data.get("timeList");
        ArrayList<Integer> nightList = data.get("nightList");

//        int[] delayTime = new int[dailycount];
        HashMap<Integer, Integer> delayTime = new HashMap<>();
        int sumOfTime = 0;


        int timecutON = 29;     //초 단위
        int timecutTW = 60;
        int timecutTR = 120;
        int countValueTR = 0;
        int countValueTW = 0;
        int countValueON = 0;

        HashMap<Integer, Integer> eventValue = new HashMap<>();
        HashMap<Integer, Integer> nofTR = new HashMap<>();
        HashMap<Integer, Integer> nofTW = new HashMap<>();
        HashMap<Integer, Integer> nofON = new HashMap<>();

        mLogger.error("getParsedata() / inited ");

        boolean outOfHome = false;
        float[] dist = new float[3];

        for (int n = 0; n < dailycount; n++) {          //이벤트 지정

            if (((distList.get(n) / (double) timeList.get(n))) < velostop) {  //n번째 점의 속도가 정지일때  m/s  km/h?

                sumOfTime += timeList.get(n);
                delayTime.put(n, 0);
                eventValue.put(n, 0);        //아무 일 없음
//                mLogger.error("getParsedata() / " + n + "번째 점에서 정지중 총 " +sumOfTime+"초, "+ timeList.get(n) + "초 경과 (" + latList.get(n) + "," + lonList.get(n) + "), " + ( (distList.get(n) /(double) timeList.get(n)))+"m/s");
            } else {    //이동하는 경우
                sumOfTime += timeList.get(n);
                delayTime.put(n, sumOfTime);

                if (sumOfTime >= timecutTR && sumOfTime < 300) {        //120초 이상 5분 미만 지체/ 집에서 2km 이상 벗어났을때

                    if (!outOfHome) {
                        Location.distanceBetween(latList.get(0), lonList.get(0), latList.get(n), lonList.get(n), dist); // m 단위
                        if (dist[0] > 2000) {
                            outOfHome = true;
                            mLogger.error("getParsedata() /  " + n + " 번째 점에서 집근처를 떠남" + latList.get(n) + "," + lonList.get(n));
                        }
                    }

                    if (outOfHome) {
                        eventValue.put(n, 3);
                        nofTR.put(countValueTR, n);      //n위치에서 3발생
                        countValueTR++;
                        mLogger.error("getParsedata() / 이벤트 3 - " + n + " 번째 점");
                    } else {
                        eventValue.put(n, 0);
                    }
                } else if (sumOfTime >= timecutTW && sumOfTime < timecutTR) {
                    eventValue.put(n, 2);
                    nofTW.put(countValueTW, n);       //n위치에서 2발생
                    countValueTW++;
//                    mLogger.error("getParsedata() / 이벤트 2 - "+ n +" 번째 점");
                } else if (sumOfTime >= timecutON && sumOfTime < timecutTW) {
                    eventValue.put(n, 1);
                    nofON.put(countValueON, n);         //n위치에서 1발생
                    countValueON++;
//                    mLogger.error("getParsedata() / 이벤트 1 - "+ n +" 번째 점");
                }

                sumOfTime = 0;

            }
        }//if----------------29초,60초,120초 별로 값을 배정--------

        mLogger.error("getParsedata() / 값 배정됨 countValueTR = " + countValueTR + ",countValueTW = " + countValueTW + ", countValueON =" + countValueON);

        int x = client; //오늘의 접객수
        int ttakeinTR = countValueTR;
        int ttakeoffTR = countValueTR;
        int ttakeinON = 0;
        int ttakeoffON = 0;
        int ttakeoffTW = 0;
        int ttakeinTW = 0;

        int[] TRrank = new int[countValueTR];
        int[] TWrank = new int[countValueTW];
        int[] ONrank = new int[countValueON];
        int a, b;   //어떤 변수지?
        for (int i = 0; i < countValueTR; i++) {
            TRrank[i] = nofTR.get(i);
        }
        for (int i = 0; i < countValueTW; i++) {
            TWrank[i] = nofTW.get(i);
        }
        for (int i = 0; i < countValueON; i++) {
            ONrank[i] = nofON.get(i);
        }

        int temp;
        for (int i = 0; i < countValueTW; i++) {
            for (int j = 0; j < countValueTW; j++) {
                a = delayTime.get(TWrank[i]);
                b = delayTime.get(TWrank[j]);

                if (a > b) {                // i > j 되도록 버블소트
                    temp = TWrank[i];
                    TWrank[i] = TWrank[j];
                    TWrank[j] = temp;
                }
            }
        }
        for (int i = 0; i < countValueTR; i++) {
            for (int j = 0; j < countValueTR; j++) {
                a = delayTime.get(TRrank[i]);
                b = delayTime.get(TRrank[j]);

                if (a > b) {                // i > j 되도록 버블소트
                    temp = TRrank[i];
                    TRrank[i] = TRrank[j];
                    TRrank[j] = temp;
                }
            }
        }


        for (int i = 0; i < countValueON; i++) {
            for (int j = 0; j < countValueON; j++) {
                a = delayTime.get(ONrank[i]);
                b = delayTime.get(ONrank[j]);

                if (a > b) {                // i > j 되도록 버블소트
                    temp = ONrank[i];
                    ONrank[i] = ONrank[j];
                    ONrank[j] = temp;
                }
            }
        }


//      for (int i = 0; i < countValueTW; i++) {
//            mLogger.error("TWrank["+i+"]" + TWrank[i]);
//        }
//
//        for (int i = 0; i < countValueON; i++) {
//            mLogger.error("ONrank["+i+"]" + ONrank[i]);
//        }
        mLogger.error("getParsedata() / 랭킹 ");
        //-----------------숫자 1,2,3 별로 탄건지 내린건지 나눔-----------
        // , 탑승값 = 10, 하차값 1000, ttake 탑승여부 저장, 10제곱승으로 해도 되긴하네..

        int[] ttake = new int[dailycount];
        for (int n = 0; n < countValueTR; n++) {
            ttake[nofTR.get(n)] = 1000 + 10;
        }

        if (x < countValueTR) {
            for (int i = 0; i < countValueTR - x; i++) {
                ttake[TRrank[countValueTR - i - 1]] = 0;
            }
        }

        if (x - countValueTR > countValueTW) {                //           //모든 2는 내린거, 1의 일부가 내린거, 1 나머지의 일부가 탄거
            mLogger.error("x - countValueTR > countValueTW");
            ttakeoffTW = countValueTW;
            ttakeinTW = 2 * (x - ttakeoffTR) - ttakeoffTW;
            ttakeoffON = x - countValueTW - countValueTR;
            ttakeinON = 2 * (x - ttakeoffTR) - ttakeoffTW - ttakeinTW;
            for (int n = 0; n < countValueTW; n++) {
                ttake[nofTW.get(n)] = 1000;
            }
            for (int n = 0; n < ttakeoffON; n++) {
                ttake[ONrank[n]] = 10;
            }
            for (int n = ttakeoffON; n < ttakeoffON + ttakeinON; n++) {
                ttake[ONrank[n]] = 10;
            }

        } else if (x - countValueTR < countValueTW) {              ////2의 일부가 내린거, 2의 나머지가 탄거, 1의 더 일부가 탄거
            mLogger.error("x - countValueTR < countValueTW");
            ttakeoffTW = x - countValueTR;
            ttakeoffON = 0;
            if (ttakeoffTW * 2 > countValueTW) {
                ttakeinTW = countValueTW - ttakeoffTW;
                ttakeinON = x - ttakeinTR - ttakeinTW;
            } else {
                ttakeinTW = 2 * (x - ttakeoffTR) - ttakeoffTW;
                ttakeinON = 2 * (x - ttakeoffTR) - ttakeoffTW - ttakeinTW;
            }

            mLogger.error("ttakeinON = " + ttakeinON + " x = " + x + ", countValueTR = " + countValueTR + ", countvalueTW = " + countValueTW + ", ttakiinTR = " + ttakeinTR + ", ttakinTW = " + ttakeinTW + "ttakeoffTW = " + ttakeoffTW);
            for (int n = 0; n < ttakeoffTW; n++) {
                ttake[TWrank[n]] = 1000;
            }
            for (int n = ttakeoffTW; n < ttakeoffTW + ttakeinTW; n++) {
                ttake[TWrank[n]] = 10;
            }
            for (int n = 0; n < ttakeinON; n++) {
                ttake[ONrank[n]] = 10;
            }

        } else {                                                    ////모든 2 가 내린거, 1 의 일부가 탄거

            mLogger.error("////모든 2 가 내린거, 1 의 일부가 탄거");

            ttakeoffTW = countValueTW;
            ttakeoffON = 0;
            ttakeinTW = 0;
            ttakeinON = x - ttakeinTR - ttakeinTW;

            for (int n = 0; n < countValueTW; n++) {
                ttake[nofTW.get(n)] = 1000;
            }
            for (int n = 0; n < ttakeinON; n++) {
                ttake[ONrank[n]] = 10;
            }
        }

        mLogger.error("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ초기 승하차 값 지정ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
        for (int n = 0; n < dailycount; n++) {
            if (ttake[n] != 0) {
                mLogger.error("ttake[" + n + "] = " + ttake[n]);
            }
        }
        mLogger.error("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
        int couplemaker = 1;
        int couplecounter = 0;
        int sumttake = 0;  ////초기값 0

        int maxtime = 0, mindex = 0, ttakeoff = 0, ttakein = 0, mincounter = 0;

        int[] numberttakeoff = new int[dailycount];
        HashMap<Integer, Integer> minlist = new HashMap<>();
//        int[] minlist = new int[dailycount / 2]; // 몇개나 있는지는 모르지만 일단 최대 점/2

        boolean firs = true;
        int ncontinue = 0;

        for (int n = dailycount - 1; n >= 0 && firs; n--) {       // //맨 뒤쪽이 하차로 끝나야 한다는 조건, 1번만 실행하면됨
            if (ttake[n] == 1010) {    //   //맨뒤가 승차로 끝나면, 그 뒤에서 가장 긴 시간을 하차로 하나 만들어줌
                mLogger.error("if ttake[" + n + "] == 1010");
                maxtime = 0;
                for (int i = n + 1; i < dailycount; i++) {
                    if (maxtime < delayTime.get(i)) {
                        mLogger.error("현재 mindex = " + i + ", maxtime = " + maxtime);
                        mindex = i;
                        maxtime = delayTime.get(i);

                    }  // //sumoftime()에 n~dailycount를 넣은 값 중, 가장 큰 sumoftime을 가진 위치(n’)값을 찾아냄
                }
                mLogger.error("가장 큰 mindex = " + mindex + ", maxtime = " + maxtime);
                ttake[mindex] = 1000;
                ttakeoff = 2;  //   //하+승차가 최우선이므로 유지하면서, 뒤에 하차를 하나 만듦…!!앞에 하차 하나 지워야됨…
                ttakein = 1;

                mincounter = 0;

                numberttakeoff[ttakeoff] = n;       // [2]는 내린 위치

                mLogger.error("하차 지정 => ttake[" + n + "] = " + ttake[n]);

                for (int k = n - 1; k >= 0; k--) {
                    if (ttake[k] >= 1000) {
                        minlist.put(mincounter, k);     // //다시 for로 가서 이어서 실행
                        mincounter++;
                        mLogger.error("mincounter = " + mincounter + ", k = " + k);
                    }
                }
                int mintime = 999;

                for (int i = 0; i < minlist.size(); i++) {
                    if (mintime > delayTime.get(minlist.get(i)) && delayTime.get(minlist.get(i)) != 0) {
                        mindex = minlist.get(i);
                        mintime = delayTime.get(minlist.get(i));
                    }  // //sumoftime()에 n~dailycount를 넣은 값 중, 가장 큰 sumoftime을 가진 위치(n’)값을 찾아냄
                }

                if (mintime == 999) {
                    mindex = n;
                    mLogger.error("mintime == 999");
                }

                //sumoftime()에 minlist(1)~minlist(mincounter)를 넣은 값 중, 가장 작은 sumoftime을 가진 위치(n’)값을 찾아냄
                mLogger.error("최소 mintime = " + mintime + " ttake[" + mindex + "]에 0을 저장");
                if (ttake[mindex] == 1000) {
                    ttake[mindex] = 0;
                } else if (ttake[mindex] == 1010) {
                    ttake[mindex] = 0;

                    int maxtemp = 0;
                    int prev = 0;

                    for (int h = 0; h < mindex; h++) {
                        if (maxtemp < delayTime.get(h)) {
                            maxtemp = delayTime.get(h);
                            if (ttake[h] == 0) {
                                ttake[prev] = 0;
                                ttake[h] = 10;
                                prev = h;
                            }
                        }
                    }
                    mLogger.error(maxtemp + "초를 가진 ttake[" + prev + "] 위치에 10을 지정함!");
                }

                for (int t = 0; t < dailycount; t++) {
                    if (ttake[t] != 0) {
                        mLogger.error("ttake[" + t + "] = " + ttake[t]);
                    }
                }

                n = dailycount - 1;

                ncontinue = n;
            } else if (ttake[n] == 10) {
                mLogger.error("if ttake[" + n + "] == 10 ! 0으로 만들고 다음으로...");
//                int t = ttakeinON;
//                while ( ONrank[t] >= n){
//                    t--;
//                }
//
                ttake[n] = 0;
//
//                mLogger.error("onrank["+t+"] = " + ONrank[t] + ". ttake[ONrank[t]] = " + ttake[ONrank[t]]);
//                ttakeoff = 0;   //  //승차가 최하위이므로, 아예 없애고 추후 앞쪽 하차지점 앞에 승차를 추가
//                ttakein = 0;
//                numberttakeoff[ttakeoff] = n;
//                firs = false;
                ncontinue = n;
            } else if (ttake[n] == 1000) {
                mLogger.error("if ttake[" + n + "] == 1000! 하차가 마지막이므로 일단 아웃");
                ttakeoff = 1;     //하차면 카운트만 하고 그냥 넘어감
                ttakein = 0;
                numberttakeoff[ttakeoff] = n;
                firs = false;
                ncontinue = n;
            } //    //맨 뒤 정리 하는거는 위에 셋 중 하나 발생하면 바로 종료
        }

        mLogger.error("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ맨 뒤 값이 하차로 지정됨ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
        mLogger.error("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ홀짝수 재배열ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");

        int tempNumberttakeoff = 0;
        int maxTime = 0;
        int minTime = 999;
        int currentNode = 0;

        ////----   그 다음 이어서…, n값 초기화 안하고 이어서 써도 될 것 같은데? 뭐 변수로 저장한다음 불러야되나?
        for (int k = ncontinue - 1; k >= 0; k--) {        ////중간값들에 대한 조건, 맨처음될때까지 반복실행해야함
            if (ttake[k] > 0) {
                mLogger.error("ttakein = " + ttakein + ", ttakeoff = " + ttakeoff + ", k = " + k);

                numberttakeoff[ttakeoff + 1] = k;

                for (int n = 0; n < numberttakeoff.length; n++) {

                    if (numberttakeoff[n] != 0) {
                        mLogger.error("numberttakeoff[" + n + "] = " + numberttakeoff[n]);
                    }

                }

                if ((ttakein + ttakeoff) % 2 == 0) {  //2로 나눠서 나머지 0 뭐 그런거 사람 없음상태
                    if (ttake[k] == 10) {

                        ttake[k] = 0;
                        //현재를 0으로 만들었으니 0 ~ k-1까지 중에서 1010과 1000을 제외한 가장 긴 시간에 10 넣기, ttakein,off 변동없음

                        //->보류
                        maxTime = 0;
                        currentNode = 0;

                        for (int h = 0; h < nofON.size(); h++) {
                            temp = nofON.get(h);
                            if (ttake[temp] < 1000 && maxTime < delayTime.get(temp)) {
                                maxTime = delayTime.get(temp);
                                currentNode = h;
                            }
                        }

                        for (int h = 0; h < nofTW.size(); h++) {
                            temp = nofTW.get(h);
                            if (ttake[temp] < 1000 && maxTime < delayTime.get(temp)) {
                                maxTime = delayTime.get(temp);
                                currentNode = h;
                            }
                        }
                        for (int h = 0; h < nofTR.size(); h++) {
                            temp = nofTR.get(h);
                            if (ttake[temp] < 1000 && maxTime < delayTime.get(temp)) {
                                maxTime = delayTime.get(temp);
                                currentNode = h;
                            }
                        }

                        ttake[currentNode] = 10;
                        mLogger.error("" + currentNode + "위치에 10을 지정 ttake[" + currentNode + "] = 10");

                        maxTime = 0;
                        currentNode = 0;
                    } else if (ttake[k] == 1010) {      //에러, 앞에 10을 추가(자동 계산) 짝수인데 1010
                        mLogger.error("사람없음 상태인데 ttake[k]=1010 , 앞에서 하차+승차 해버림");

                        //1010이 나오는 경우
                        //if(ttake[0~k]) 1000이 있는 경우

                        boolean pass = false;
                        for (int h = 0; h < k; h++) {
                            if (ttake[h] == 1000) {
                                pass = true;

                            }
                        }


                        if (pass) {
                            mLogger.error("1000이 있는 경우");

                            // ttake[k]를 수정하지 않고 k+1 ~ numberttakeoff[ttakeoff] 사이 위치 currentNode에 1000을 추가
                            temp = 0;
                            currentNode = 0;
                            maxTime = 0;
                            for (int h = k + 1; h < numberttakeoff[ttakeoff]; h++) {
                                if (ttake[h] == 0 && maxTime < delayTime.get(h)) {
                                    maxTime = delayTime.get(h);
                                    currentNode = h;
                                }
                            }
                            ttake[currentNode] = 1000;
                            mLogger.error("" + currentNode + "위치에 1000 지정. 하차 추가");

                            numberttakeoff[ttakeoff + 1] = currentNode;
                            numberttakeoff[ttakeoff + 2] = k;
                            ttakeoff++;
                            ttakeoff++;
                            ttakein++;

                            //앞에 작은 1000을 지운다.

                            currentNode = 0;
                            minTime = 999;

                            for (int h = 0; h < k; h++) {
                                if (ttake[h] == 1000) {
                                    if (minTime > delayTime.get(h)) {
                                        minTime = delayTime.get(h);
                                        currentNode = h;
                                    }
                                }
                            }
                            ttake[currentNode] = 0;
                            mLogger.error("" + currentNode + "위치에 1000에서 0 지정. 하차 지움");

                        } else {
                            mLogger.error("1000이 없는 경우! 앞의 가장큰 위치에 10을 추가하고 1010-> 1000으로 변경");

                            temp = 0;
                            currentNode = 0;
                            maxTime = 0;
                            for (int h = 0; h < k; h++) {
                                if (ttake[h] % 100 != 10) {
                                    if (maxTime < delayTime.get(h)) {
                                        maxTime = delayTime.get(h);
                                        currentNode = h;
                                    }
                                }
                            }
                            ttake[currentNode] = 10;
                            ttake[k] = 1000;
                            //  0~k-1 최대 시간에 ttake[ 0~k-1 ] %100 = 10이 아닐 때를 10으로 지정
                            ttakeoff++;

                        }

//
//                    ttake[tempNumberttakeoff] = 0;
//                    mLogger.error("numberttakeoff[tempNumberttakeoff] == " + numberttakeoff[tempNumberttakeoff]);
//                    mLogger.error("ㅡttake[tempNumberttakeoff] = " + ttake[tempNumberttakeoff] + ", tempNumberttakeoff = " + tempNumberttakeoff + ", ttakeoff+1 = " + ttakeoff + "+1ㅡ");
//                    ttakeoff++;
//
//
//                    int maxtemp = 0;
//                    int prev = 0;
//
//                    for (int h = 0; h < k; h++) {
//                        if (maxtemp < delayTime.get(h)) {
//                            maxtemp = delayTime.get(h);
//
//                            if (ttake[h] == 0) {
//                                ttake[prev] = 0;
//                                ttake[h] = 10;
//                                prev = h;
//
//                            }
//                        }
//                    }
//                    mLogger.error(maxtemp + "초를 가진 ttake[" + prev + "] 위치에 10을 지정함!");
//                    //ttakein ++ //해야하나???

                    } else if (ttake[k] == 1000) {

                        ttakeoff++;

                    }
                } else {    //홀수인경우 앞에 사람이 있어야 함
                    if (ttake[k] == 10) {
                        tempNumberttakeoff = numberttakeoff[ttakeoff + 1];
                        ttakein++;
                    } else if (ttake[k] == 1010) {
                        ttakein++;
                        ttakeoff++;
                    } else if (ttake[k] == 1000) {  //사람이 타지 않음-> 지점 간 최대 시간에 10을 만들고, 앞에서 10 중 최저 10의 위치를 -10

                        mLogger.error("사람이 있는 상태에서 재승차, ttake[k]=1000");

                        maxTime = 0;
                        currentNode = 0;
                        for (int h = numberttakeoff[ttakeoff] - 1; h > numberttakeoff[ttakeoff + 1]; h--) {
                            if (maxTime < delayTime.get(h)) {
                                maxTime = delayTime.get(h);
                                currentNode = h;
                            }
                        }

                        if (currentNode != 0) {
                            ttake[currentNode] = 10;
                            mLogger.error("그러니 뒷부분의 " + numberttakeoff[ttakeoff] + "에서 " + numberttakeoff[ttakeoff + 1] + "사이에서  ttake[" + currentNode + "]= 10을 지정");
                        } else {
                            mLogger.error("currentNode = 0 10 넣기 실패");
                        }
                        //10을 넣었으니 이제 앞에서 승차 하나를 빼줌

                        minTime = 999;
                        currentNode = 0;
                        for (int h = numberttakeoff[ttakeoff + 1] - 1; h > 0; h--) {
                            if (ttake[h] % 100 == 10) {
                                if (minTime > timeList.get(h)) {
                                    minTime = delayTime.get(h);
                                    currentNode = h;
                                }
                            }
                        }
                        if (currentNode != 0) {
                            ttake[currentNode] = ttake[currentNode] - 10;
                            mLogger.error("그리고 그 앞부분인 ttake[" + currentNode + "] = " + ttake[currentNode] + "에서 10을 뺌");
                        } else {
                            mLogger.error("currentNode = 0 10 빼기 실패!");
                        }
                        ttakein++;
                        ttakeoff++;
                    }
                }
            }
        }

        mLogger.error(" ttakein = " + ttakein + ", ttakeoff = " + ttakeoff);
        mLogger.error("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ홀짝수 재배열 결과ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
        for (
                int n = 0;
                n < dailycount; n++)

        {
            if (ttake[n] != 0) {
                mLogger.error("ttake[" + n + "] = " + ttake[n]);
            }
        }

        mLogger.error("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ처음을 승차로 지정ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
        int con;

        //ttake[con]??
        boolean seungcha = true;
        for (con = 0; seungcha; con++)

        {        //처음 시작이 승차로 시작해야 한다는 조건, 마지막에 한번만 실행하면 됨
            if (ttake[con] == 10) {
                ttakein++;
                seungcha = false;
            } else if (ttake[con] == 1010) {
                seungcha = false;
                mLogger.error("con_ttake[" + con + "] = 1010");
                maxtime = 0;

                int prev = 0;

                for (int i = 0; i < con; i++) {
                    if (maxtime < delayTime.get(i)) {

                        maxtime = delayTime.get(i);
                        prev = i;
                        mLogger.error("" + i + "위치에서 maxtime = " + maxtime + "으로 갱신, delaytime.get(i) = " + delayTime.get(i));

                    }
                }

                ttake[prev] = 10;

                mLogger.error("maxtime = " + maxtime + ", ttake[prev] = 10 == " + ttake[prev]);

                ttakein = ttakein + 2;
                ttakeoff++;

                mLogger.error("10을 추가했으니, 뒤의 10을 지운다");

                int mintemp = 999;
                int prevf = 0;
                boolean pass = false;

                for (int h = prev + 1; h < dailycount; h++) {
                    if (ttake[h] == 10) {
                        if (mintemp > delayTime.get(h)) {
                            prevf = h;
                            mintemp = delayTime.get(h);
                            pass = true;
                        }
                    }
                }
                ttake[prevf] = 0;

                if (!pass) {
                    for (int h = prev + 1; h < dailycount; h++) {
                        if (ttake[h] == 1010) {
                            if (mintemp > delayTime.get(h)) {
                                prevf = h;
                                mintemp = delayTime.get(h);
                                pass = true;
                            }
                        }
                    }
                    ttake[prevf] = 1000;
                }
                if (!pass) {
                    mLogger.error("일어나서는 안되는 일! 10도 1010도 없음");
                }
            } else if (ttake[con] == 1000) { //1000의 경우
                seungcha = false;
                mLogger.error("ttake[con] == 1000");

                maxtime = 0;

                for (int i = 0; i < con; i++) {
                    if (maxtime <= delayTime.get(i)) {
                        maxtime = delayTime.get(i);
                    }
                }
                ttake[maxtime] = 10;
                ttakein++;
                ttakeoff++;

                /*
                10이나 1010을 지운다
                 */
            }
        }

        int difttakecount = ttakeoff - ttakein;
        int[] lastcheck = new int[dailycount];
        int[] lastttake = new int[dailycount];


        for (
                int n = 0;
                n < dailycount; n++)

        {
            if (ttake[n] != 0) {
                mLogger.error("ttake[" + n + "] = " + ttake[n]);
            }
        }

        mLogger.error("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ처음 승차 처리, 검토 시작ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");


        if (difttakecount > 0)

        {             //마지막 검토, 승하차수 맞추기, 위에 꺼가 모든걸 다 포함하는지 알수가 없어서 썼음
            a = 0;
            for (int k = con; k < dailycount; k++) {
                if (ttake[k] > 0) {
                    lastcheck[a] = ttake[k];
                    lastttake[a] = k;
                    a++;
                    if (lastcheck[a] == 1000 && +lastcheck[a + 1] >= 1010) {
                        mLogger.error(a + "위치 에서 하차 후" + lastttake[a + 1] + "에서 또 하차");

                        if (lastttake[a + 1] > lastttake[a]) {
                            maxtime = lastttake[a + 1];
                        } else {
                            maxtime = lastttake[a];
                        }

                        ttake[maxtime] = 10;
                        difttakecount = difttakecount - 1;

                    } else {

                    }

                } else {
                }
            }

            if (difttakecount == 0) {
                //break;
            } else {
                //return;
            }
        } else if (difttakecount < 0)

        {
            a = 0;
            for (int k = con; k < dailycount; k++) {
                if (ttake[k] == 10) {
                    lastttake[a] = k;
                    a++;
                } else {
                    //        return;
                }
            }

            int mintime = 999;

            for (int i = 0; i < a; i++) {
                if (mintime >= lastcheck[i]) {
                    mintime = lastcheck[i];
                }
            }

            ttake[mintime] = 0;
            difttakecount = difttakecount + 1;

        } else

        {
            //   break;    //똑같으면 다 완벽하니 끝내고 다음 계산으로 들어가면됨
        }

        mLogger.error("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ검토끝ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");

        for (
                int n = 0;
                n < dailycount; n++)

        {
            if (ttake[n] != 0) {
                mLogger.error("ttake[" + n + "] = " + ttake[n]);
            }
        }

        mLogger.error("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ돈 계산ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");

        int basicmoney = 3000;
        int moneyStart, moneyEnd = 0, moneyofdist = 0, moneyRestart = 0, moneyEle, moneySum = 0, moneyBigele = 0, moneyBigdist = 0;
        //moneyEle는 주행 상태에 따른 증가량
        float timeC = 1;
        double twodist = 0;

        for (int n = 1; n < dailycount; n++) {

            if (ttake[n] % 100 == 10) {    //   100으로 나눠서 나머지가 10이면
                moneyStart = n; // 승차지점

                for (int i = n + 1; i < ttake.length; i++) { //하자지점
                    if (ttake[i] >= 1000) {
                        moneyEnd = i;
                        break;

                    }
                }

                moneySum = 0;
                moneyofdist = 0;
                twodist = 0;

                mLogger.error("getParsedata_승차지점 = " + moneyStart + " 하차지점 = " + moneyEnd);

                for (int j = moneyStart; j < moneyEnd; j++) {
                    moneyofdist += distList.get(j);
                }

                if (moneyofdist >= 2000) { //2000m이상 주행시
                    mLogger.error("2000m 이상 주행");

                    for (int m = moneyStart; twodist <= 2000; m++) {
                        twodist += distList.get(m);
                        moneyRestart = m + 1;       //2km가 넘어간 지점 지정
                    }

                    for (a = moneyRestart; a <= moneyEnd; a++) {

                        if (distList.get(a) / timeList.get(a) > 4.166) {       //km/s? m/s? 15km/h == 4.166
                            moneyEle = (int) (distList.get(a) * 100) / 142;
                        } else {
                            moneyEle = timeList.get(a) * 100 / 35;
                        }
                        moneySum = moneySum + (int) ((1 + 0.2 * nightList.get(a)) * moneyEle);

//                        mLogger.error("현재 금액  = " + moneySum + ", 더할 금액  = " + ((1 + 0.2 * nightList.get(a)) * moneyEle) + ", 심야 = "+nightList.get(a));
                    }
                    moneySum = ((moneySum / 100) * 100)+basicmoney;

                    moneyBigele = (int) (moneyBigele + timeC * basicmoney + moneySum);  //계속 더해서 최종요금 계산
                    moneyBigdist = moneyBigdist + moneyofdist;

                    mLogger.error("손님태우고 달린 거리 moneyofdist = " + moneyofdist + "m");
                    mLogger.error("손님한테 번 돈 = moneySum = " + moneySum + "원");

                } else {

                    if (nightList.get(moneyStart) == 0 && nightList.get(moneyEnd) == 0) {
                        moneySum = 3000;
                    } else {
                        moneySum = (int) (3000 * 1.2);
                    }


                    mLogger.error("손님태우고 달린 거리 moneyofdist = " + moneyofdist + "m");
                    mLogger.error("손님한테 번 돈 = " + moneySum);

                    moneyBigele += moneySum;
                    moneyBigdist += moneyofdist;

                }
                //심야할증 여부
            } else {
//                mLogger.error("getParsedata() / 나머지가 10이 아님 ttake["+n+"] = " + ttake[n]);
            }
        }

        HashMap<String, Integer> result = new HashMap<>();
        result.put("money", moneyBigele);
        result.put("moneyDist", moneyBigdist);

        mLogger.error("getParsedata() / 종료 money = " + moneyBigele + "moneydist = " + moneyBigdist);
        return result;
    }

}

