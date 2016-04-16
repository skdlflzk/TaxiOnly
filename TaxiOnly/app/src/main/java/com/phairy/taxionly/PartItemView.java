//ClassItemView.java
package com.phairy.taxionly;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PartItemView extends LinearLayout {
    private TextView partName ;
    private TextView partMaxValue ;
    private TextView partCurrentValue ;
    private ProgressBar progressBar ;

    public PartItemView(Context context) {
        super(context);
        init(context);
    }
    public PartItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    public void init(Context context){
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.part_item, this, true);
        partName = (TextView) findViewById(R.id.partName);
        partMaxValue = (TextView) findViewById(R.id.partMaxValue);
        partCurrentValue = (TextView) findViewById(R.id.partCurrentValue);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
    }
    public void setPartName(String name){
        partName.setText(name);
    }
    public void setPartMaxValue(double Value, String Etc){
        

        if(Etc.equals("day")){
            partMaxValue.setText((int)Value+"일");
        }else{
            partMaxValue.setText(Value+"Km");
        }
        
    }
    public void setPartCurrentValue(double Value ,String Etc){
       
        if( Etc.equals("day")){
            partCurrentValue.setText((int)Value+"일 째");
        }else{
            partCurrentValue.setText(Value+"Km");
        }
        
    }

    public int setProgress(double maxValue, double currentValue){
        double percent = currentValue/maxValue;

        progressBar.setIndeterminate(true);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress((int)(percent*100));
        return (int)(percent*100);
    }

    
}