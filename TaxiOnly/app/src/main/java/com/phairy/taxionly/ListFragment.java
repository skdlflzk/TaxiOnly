
package com.phairy.taxionly;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.apache.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;



public class ListFragment extends Fragment {

    private Logger mLogger = Logger.getLogger(ListFragment.class);

    SQLiteDatabase database;
    String DATABASENAME = "PART";
    String TABLENAME = "PARTINFO";
    String partInfo = "partInfo.txt";

    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private String TAG = Start.TAG;


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_fragment, container, false);

        mLogger.error("--ListFragment--");


        pref = getActivity().getSharedPreferences("pref", Context.MODE_PRIVATE);
        editor = pref.edit();

        boolean isCreated = pref.getBoolean("partCreated", false);
        if (!isCreated) {
            initPartData();
            editor.putBoolean("partCreated", true);
            editor.apply();
        }else{
            mLogger.error("onCreateView() / part init is not first");

        }

//        getPartData();

        ListView listview = (ListView) view.findViewById(R.id.listView);
        final ClassAdapter classAdapter = new ClassAdapter();
        listview.setAdapter(classAdapter);


//리스트뷰에 리스너 설정
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {

                final double mValue = classAdapter.getPartValue(position);
                final double cValue = classAdapter.getPartCurrentValue(position);
                final String name =  classAdapter.getPartName(position);

                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View layout = layoutInflater.inflate(R.layout.adjust_dialog, null);

                final EditText editText1 = (EditText) layout.findViewById(R.id.editText1);
                EditText editText2 = (EditText) layout.findViewById(R.id.editText2);
                editText1.setText( String.valueOf((int)cValue));
                editText2.setText( String.valueOf((int)mValue) );

                Button reset = (Button) layout.findViewById(R.id.resetButton);
                reset.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        editText1.setText( String.valueOf(0) );
                    }
                });
                new AlertDialog.Builder(getActivity())
                        .setTitle(name)
                        .setView(layout)
                        .setPositiveButton("취소",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {

                                    }
                                })
                        .setNegativeButton("확인",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int which) {
                                        try {
                                            EditText editText1 = (EditText) layout.findViewById(R.id.editText1);
                                            EditText editText2 = (EditText) layout.findViewById(R.id.editText2);
                                            double c = Double.parseDouble(editText1.getText().toString());
                                            double m = Double.parseDouble(editText2.getText().toString());

                                            if ( Math.abs(m - mValue) < 0.1 && Math.abs(c - cValue) < 0.1) {
                                                return;
                                            }

                                            database = getActivity().openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
                                            database.execSQL("UPDATE " + TABLENAME + " SET partMaxValue = " + Double.parseDouble(String.format("%.2f", m)) + " , partCurrentValue = " + Double.parseDouble(String.format("%.2f", c)) + " WHERE partName = '" + name + "'");

                                            database.close();
                                            database = null;
                                            classAdapter.notifyDataSetChanged();

                                            mLogger.error("ListViewClickListener_update success");



                                        } catch (Exception e) {
                                            mLogger.error("ListViewClickListener_update fail");

                                        } finally {


                                        }
                                    }
                                }).show();
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

        } catch (Exception e) {
            e.printStackTrace();
            mLogger.error("initPartData_ Creating " + TABLENAME + " Failed");

        } finally {
            database.close();
            database = null;
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

                        database.execSQL("INSERT INTO " + TABLENAME + "(partName, partMaxValue, partCurrentValue, etc) VALUES " + "( '" + partName + "', '" + partMaxValue + "', " + 0 + ", '"+ etc+"')");

                    } else if (front != -1) { //문자열을 찾은 경우
                        front = front + 3 + moreTen;
                        end = data.indexOf("/" + (i + 2) + ".");

                        tempString = data.substring(front, end);//
                        middle = tempString.indexOf(",");
                        tail = tempString.indexOf("-");


                        partName = tempString.substring(0, middle);
                        partMaxValue = tempString.substring(middle + 1, tail);
                        etc = tempString.substring(tail + 1, tempString.length());

                        database.execSQL("INSERT INTO " + TABLENAME + "(partName, partMaxValue, partCurrentValue, etc) VALUES " + "( '" + partName + "', '" + partMaxValue + "', " + 0 + ", '" + etc + "')");

                    } else {
                        //문자열 없음, 다음으로 이동
                        continue;
                    }
                    mLogger.debug("initPartData_Inserting " + TABLENAME + " Success");

                }
            } else {
                mLogger.debug("initPartData_database is null");
            }


        } catch (Exception e) {
            e.printStackTrace();

            mLogger.debug("initPartData_Inserting " + TABLENAME + " Failed");

        } finally {
            database.close();
            database = null;
        }

    }

    private void getPartData() {
        int size = 0;
        String partName ;
        double partMaxValue, partCurrentValue;
        Cursor cursor;
        try {
            cursor = database.rawQuery("Select partName, partMaxValue, partCurrentValue  FROM " + TABLENAME, null);
            size = cursor.getCount();
            if (size == 0) {
                Log.e(TAG, "ListFragment:getPartData() / " + TABLENAME + " data not exist");

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
        } finally {
            database.close();
            database = null;

        }
    }


    class ClassAdapter extends BaseAdapter {
        //리스트 뷰의 정보를 관리할 어댑터 설정

        String DATABASENAME = "PART";
        String TABLENAME = "PARTINFO";
        SQLiteDatabase database ;
        Cursor cursor;
        int[] progress;

        @Override
        public int getCount() {

            database = getActivity().openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
            cursor = database.rawQuery("SELECT partName, partMaxValue, partCurrentValue,etc FROM " + TABLENAME, null);
            int count = cursor.getCount();
            cursor.close();
            database.close();
            database = null;

            return count;
        }

        @Override
        public Object getItem(int position) {
            database = getActivity().openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
             cursor = database.rawQuery("SELECT partName, partMaxValue, partCurrentValue,etc FROM " + TABLENAME, null);
            cursor.moveToPosition(position);
            String n = cursor.getString(0);
            cursor.close();
            database.close();
            database = null;

            return n;
        }
        public String getPartName(int position) {
            database = getActivity().openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
             cursor = database.rawQuery("SELECT partName, partMaxValue, partCurrentValue,etc FROM " + TABLENAME, null);
            cursor.moveToPosition(position);
            String n = cursor.getString(0);
            cursor.close();

                database.close();
                database = null;


            return n;
        }
        public double getPartValue(int position) {
            database = getActivity().openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
            cursor = database.rawQuery("SELECT partName, partMaxValue, partCurrentValue,etc FROM " + TABLENAME, null);
            cursor.moveToPosition(position);
            double d = cursor.getDouble(1);
            cursor.close();
            database.close();
            database = null;

            return d;
        }

        public double getPartCurrentValue(int position) {
            database = getActivity().openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
            cursor = database.rawQuery("SELECT partName, partMaxValue, partCurrentValue,etc FROM " + TABLENAME, null);
            cursor.moveToPosition(position);
            double d = cursor.getDouble(2);
            cursor.close();
            database.close();
            database = null;

            return d;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            database = getActivity().openOrCreateDatabase(DATABASENAME, Context.MODE_PRIVATE, null);
            Cursor cursor = database.rawQuery("SELECT partName, partMaxValue, partCurrentValue,etc FROM " + TABLENAME, null);

            cursor.moveToPosition(position);

            PartItemView partItemView = new PartItemView(getActivity());

            partItemView.setPartName(cursor.getString(0));
            partItemView.setPartMaxValue(cursor.getDouble(1), cursor.getString(3));
            partItemView.setPartCurrentValue(cursor.getDouble(2), cursor.getString(3));
            partItemView.setProgress(cursor.getDouble(1),cursor.getDouble(2));


            cursor.close();
            database.close();
            database = null;

            return partItemView;
        }


    }


}

 