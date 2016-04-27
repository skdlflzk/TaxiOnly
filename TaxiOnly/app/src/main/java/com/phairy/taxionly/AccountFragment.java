
package com.phairy.taxionly;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class AccountFragment extends Fragment {
    SQLiteDatabase database;
    String DATABASENAME = "PART";
    String TABLENAME = "PARTINFO";
    String partInfo = "partInfo.txt";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private String TAG = Start.TAG;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.account_fragment, container, false);


        Log.e(TAG, "--AccountFragment--");

        pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor = pref.edit();

        boolean isCreated = pref.getBoolean("isCreated", false);
        if (isCreated == false) {
            initPartData();
            editor.putBoolean("isCreated", true);
            editor.commit();
        }else{
            Log.d(TAG, "AccountFragment:onCreateView() / is not first");
        }

//        getPartData();

        ListView listview = (ListView) view.findViewById(R.id.listView);
        ClassAdapter classAdapter = new ClassAdapter();
        listview.setAdapter(classAdapter);
//리스트뷰에 리스너 설정
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Intent myIntent = new Intent(getActivity(), ClassCheck.class);
//                myIntent.putExtra("classnetId", );
//                myIntent.putExtra("className", "" + classAdapter.getItem(position));
//                myIntent.putExtra("classValue", "" +
//                        classAdapter.getClassValue(position));
//                ;
//                Log.e("error", "ClassSelect: id = " + classnetId + " className = " +
//                        classAdapter.getItem(position) + " classValue = " +
//                        classAdapter.getClassValue(position));
//                Log.e("error", "ClassSelect->ClassCheck");
//                startActivity(myIntent);
            }
        });


        return view;
    }


    private void initPartData() {
        try {
            database = getActivity().openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
            database.execSQL("CREATE TABLE if not exists " + TABLENAME + "("
                    + "_id integer PRIMARY KEY autoincrement, "
                    + "partName text, "
                    + "partMaxValue REAL, "
                    + "partCurrentValue REAL, "
                    + "etc text)");
            Log.d(TAG, "AccountFragment:initPartData() / Creating " + TABLENAME + " Success");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "AccountFragment:initPartData() / Creating " + TABLENAME + " Failed");
        }
        try {

            String data = null;
            InputStream inputStream = getResources().openRawResource(R.raw.partinfo);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

            int maxSize;
            try {
               int i = inputStream.read();
                while (i != -1) {
                    byteArrayOutputStream.write(i);
                    i = inputStream.read();
                }

                data = new String(byteArrayOutputStream.toByteArray(),"UTF-8");
                inputStream.close();
                maxSize = Integer.parseInt(data.substring(data.indexOf("size=") + 5, data.indexOf("#")));
            } catch (IOException e) {
                maxSize = 0;
                e.printStackTrace();
            }


            database = getActivity().openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
            String partName;
            String partMaxValue;
            String etc;
            int front , moreTen = 0, end , middle,tail ;
            String tempString;

            if (database != null) {
                for (int i = 0; i < maxSize; i++) {
                    front = data.indexOf("/" + (i + 1) + ".");

                    if (i == 9) moreTen = 1;
                    if ((front != -1) && (i == maxSize - 1)) {                //마지막 문자열에 포함되어있을 경우

                        front = front + 3 + moreTen;
                        end = data.indexOf("/size");

                        tempString = data.substring(front, end);
                        middle = tempString.indexOf(",");
                        tail = tempString.indexOf("-");

                        partName = tempString.substring(0, middle);
                        partMaxValue = tempString.substring(middle + 1, tail);
                        etc = tempString.substring(tail + 1, tempString.length());

                        database = getActivity().openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
                        database.execSQL("INSERT INTO " + TABLENAME + "(partName, partMaxValue, partCurrentValue, etc) VALUES " + "( '" + partName + "', '" + partMaxValue + "', " + 10000 + ", '"+ etc+"')");

                    } else if (front != -1) { //문자열을 찾은 경우
                        front = front + 3 + moreTen;
                        end = data.indexOf("/" + (i + 2) + ".");

                        tempString = data.substring(front, end);//
                        middle = tempString.indexOf(",");
                        tail = tempString.indexOf("-");


                        partName = tempString.substring(0, middle);
                        partMaxValue = tempString.substring(middle + 1, tail);
                        etc = tempString.substring(tail + 1, tempString.length());

                        database = getActivity().openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
                        database.execSQL("INSERT INTO " + TABLENAME + "(partName, partMaxValue, partCurrentValue, etc) VALUES " + "( '" + partName + "', '" + partMaxValue + "', " + 10000 + ", '"+ etc+"')");

                        database.close();
                    } else {
                        //문자열 없음, 다음으로 이동
                        continue;
                    }
                    Log.d(TAG, "AccountFragment:initPartData() / Inserting " + TABLENAME + " Success");
                }
            } else {
                Log.e(TAG, "AccountFragment:initPartData() / database is null");
            }

        } catch (Exception e) {
            e.printStackTrace();

            Log.e(TAG, "AccountFragment:initPartData() / Inserting " + TABLENAME + " Failed");

        }

    }

    private void getPartData() {
        int size = 0;
        String partName ;
        double partMaxValue, partCurrentValue;

        try {
            Cursor cursor = database.rawQuery("Select partName, partMaxValue, partCurrentValue  FROM " + TABLENAME, null);
            size = cursor.getCount();
            if (size == 0) {
                Log.e(TAG, "AccountFragment:getPartData() / " + TABLENAME + " data not exist");

            } else {
                for (int i = 0; i < size; i++) {
                    cursor.moveToLast();
                    partName = cursor.getString(0);
                    partMaxValue = cursor.getDouble(1);
                    partCurrentValue = cursor.getDouble(2);
                }
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("error", "MainActicity: Getting Selector Fail");
        }
    }


    class ClassAdapter extends BaseAdapter {
        //리스트 뷰의 정보를 관리할 어댑터 설정

        String DATABASENAME = "PART";
        String TABLENAME = "PARTINFO";
        SQLiteDatabase database = getActivity().openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
        Cursor cursor = database.rawQuery("SELECT partName, partMaxValue, partCurrentValue,etc FROM " + TABLENAME, null);

        int[] progress;
        @Override
        public int getCount() {
            return cursor.getCount();
        }

        @Override
        public Object getItem(int position) {
            cursor.moveToPosition(position);
            return cursor.getString(0);
        }

        public double getPartValue(int position) {
            cursor.moveToPosition(position);
            return cursor.getDouble(1);
        }

        public double getPartCurrentValue(int position) {
            cursor.moveToPosition(position);
            return cursor.getDouble(2);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            cursor.moveToPosition(position);

            PartItemView partItemView = new PartItemView(getActivity());

            partItemView.setPartName(cursor.getString(0));
            partItemView.setPartMaxValue(cursor.getDouble(1), cursor.getString(3));
            partItemView.setPartCurrentValue(cursor.getDouble(2), cursor.getString(3));
//            partItemView.setProgress(cursor.getInt(1),cursor.getInt(2));
            ProgressBar progressBar = new ProgressBar(getActivity(), null, android.R.attr.progressBarStyleHorizontal);
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);

            double percent = Double.valueOf(getPartValue(position))/ Double.valueOf(getPartCurrentValue(position));
            progressBar.setProgress((int) (percent * 100));
//            progressBar.setMinimumHeight(10);
//            progressBar.setProgress(100);
//            partItemView.addView(progressBar);

//            TextView t = new TextView(getActivity());
//            t.setText("asdsd");
//            String strColor = "#000000";
//            t.setTextColor(Color.parseColor(strColor));
//            partItemView.addView(t);
            return partItemView;
        }
    }


}

 