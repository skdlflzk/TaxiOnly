package com.phairy.taxionly;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.phairy.taxionly.databinding.HouseholdChartLayoutBinding;

import org.apache.log4j.Logger;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        LayoutInflater inflater = LayoutInflater.from(this);
        binding = DataBindingUtil.setContentView(this, R.layout.household_chart_layout);

        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        setContentView(inflater.inflate(R.layout.household_chart_layout, null));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        String TAG = HouseholdChartActivity.class.getSimpleName();

        mLogger.info("--HouseholdChart!--" + TAG);


        pref = getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor = pref.edit();

        boolean isCreated = pref.getBoolean("chartCreated", false);
        if (!isCreated) {

            initChartData(context);  //DB 생성 및 초기화
            editor.putBoolean("chartCreated", true);
            editor.apply();

        } else {
            mLogger.error("onCreate() / chart init is not first");

        }

        Intent intent = getIntent();

        action = intent.getAction();

        if (action.equals("SHOW")) {        //차트 추가

            int last = pref.getInt("chartSize", -1);
            int index;

            if ((index = intent.getIntExtra("index", last)) == -1) {
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
                        + "list text, "         //형식은 어떻게?
                        + "netgain Integer, "
                        + "etc text)");
                */

                cursor.moveToPosition(index-1); // 0부터 시작
                int distance = cursor.getInt(0);
                int client = cursor.getInt(1);
                int income = cursor.getInt(2);
                int spending = cursor.getInt(3);
                int netGain = cursor.getInt(4);
                String list = cursor.getString(0); //파싱은 어떻게?

                String etc = cursor.getString(1);

                binding.distanceText.setText(distance);
                binding.clientText.setText(client);
                binding.incomeText.setText(income);
                binding.spendingText.setText(spending);
                binding.netGainText.setText(netGain);
                //binding.etc.setText(etc);

                cursor.close();

            } catch (Exception e) {
                mLogger.error("onCreate() / error while showing chart");
                e.printStackTrace();
            }

        } else if (action.equals("CREATE")) {
            binding.distanceText.setVisibility(View.INVISIBLE);
            binding.distanceEdit.setVisibility(View.VISIBLE);

            int distance = 0; //getDistance();
            binding.distanceEdit.setText(distance);    //입력이 끝나면 + km

            binding.clientText.setVisibility(View.INVISIBLE);
            binding.clientEdit.setVisibility(View.VISIBLE);

            binding.incomeText.setVisibility(View.INVISIBLE);
            binding.incomeEdit.setVisibility(View.VISIBLE);

            binding.spendingText.setVisibility(View.INVISIBLE);
            binding.spendingEdit.setVisibility(View.VISIBLE);

            //기본 2개 (만약 추가가 된다면, id를 지정하여 동적으로 계산)
            binding.gasText.setVisibility(View.INVISIBLE);
            binding.gasEdit.setVisibility(View.VISIBLE);

            binding.foodText.setVisibility(View.INVISIBLE);
            binding.foodEdit.setVisibility(View.VISIBLE);
            //
            binding.netGainText.setVisibility(View.INVISIBLE);
            binding.netGainEdit.setVisibility(View.VISIBLE);

        } else {
            return;
        }

    }

    private void initChartData(Context context) {

        try {

            database = context.openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE if not exists " + TABLENAME + "("
                    + "_id integer PRIMARY KEY autoincrement, "
                    + "distance Integer, "
                    + "client Integer, "
                    + "income Integer, "
                    + "spending Integer, "
                    + "list text, "
                    + "netgain Integer, "
                    + "etc text)");

        } catch (Exception e) {
            e.printStackTrace();
            mLogger.error("initPartData_ Creating \"" + TABLENAME + "\" Failed");

        } finally {
            database.close();
            database = null;
        }

    }

    private void onConfirmClicked(View v) {

        if (action.equals("SHOW")) {
            finish();
        } else if (action.equals("CREATE")) {
            try {
                int distance = Integer.parseInt(binding.distanceEdit.getText().toString());
                int client = Integer.parseInt(binding.clientEdit.getText().toString());
                int income = Integer.parseInt(binding.incomeEdit.getText().toString());
                int spending = Integer.parseInt(binding.spendingEdit.getText().toString());
                //기본 2개 (만약 추가가 된다면, id를 지정하여 동적으로 계산)
                int gas = Integer.parseInt(binding.gasEdit.getText().toString());

                int food = Integer.parseInt(binding.foodEdit.getText().toString());

                String etc= ",1-" +gas+",2-"+food+"/" ;
                //

                int netGain = Integer.parseInt(binding.netGainEdit.getText().toString());

                try {
                    database = context.openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);

                    String list= "";

                    database.execSQL("INSERT INTO " + TABLENAME + " VALUES " + "( " + distance + ", " + client + ", " + income + ", " + spending + ", '"+ list + "', " + netGain+", '"+etc +"' )");

                    mLogger.debug("onConfirmClicked_ Chart updated successfully");

                } catch (Exception e) {
                    mLogger.error("onConfirmClicked_ ERROR while updating chart");
                }
            } catch (Exception e) {
                mLogger.error("onConfirmClicked_ ERROR while parsing");
            }//catch

            int size = pref.getInt("chartSize", -1);
            size += 1;
            editor.putInt("chartSize", size);

            Intent intent = new Intent(getApplicationContext(), HouseholdChartActivity.class);
            intent.setAction("SHOW");
            intent.putExtra("index", size);
            startActivity(intent);
            finish();

        }//if action == "CREATE"
    }//onConfirmClicked

    private void onEditClicked(View v){
        toggleLayout(action);
    }//onEditClicked

    private void toggleLayout(String action) {
        if (action.equals("SHOW")) { //CREATE->SHOW로 간다면
            binding.distanceText.setVisibility(View.VISIBLE);
            binding.distanceEdit.setVisibility(View.INVISIBLE);

            binding.clientText.setVisibility(View.VISIBLE);
            binding.clientEdit.setVisibility(View.INVISIBLE);

            binding.incomeText.setVisibility(View.VISIBLE);
            binding.incomeEdit.setVisibility(View.INVISIBLE);

            binding.spendingText.setVisibility(View.VISIBLE);
            binding.spendingEdit.setVisibility(View.INVISIBLE);

            //기본 2개 (만약 추가가 된다면, id를 지정하여 동적으로 계산)
            binding.gasText.setVisibility(View.VISIBLE);
            binding.gasEdit.setVisibility(View.INVISIBLE);

            binding.foodText.setVisibility(View.VISIBLE);
            binding.foodEdit.setVisibility(View.INVISIBLE);
            //
            binding.netGainText.setVisibility(View.VISIBLE);
            binding.netGainEdit.setVisibility(View.INVISIBLE);

            binding.edit.setVisibility(View.INVISIBLE);
        }

    }//toggleLayout
}
