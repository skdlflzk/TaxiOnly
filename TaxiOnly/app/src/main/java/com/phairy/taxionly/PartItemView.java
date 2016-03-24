//ClassItemView.java
package com.phairy.taxionly;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PartItemView extends LinearLayout {
    private TextView partName ;
    private TextView partMaxValue ;
    private TextView partCurrentValue ;
    private ProgressBar progressBar ;
    private int maxValue;
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
    public void setPartMaxValue(int Value){
        
        maxValue = Value;
        if(Value < 10){
            partMaxValue.setText(Value+"년");
        }else{
            partMaxValue.setText(Value+"Value");
        }
        
    }
    public void setPartCurrentValue(int Value){
       
        if( maxValue < 10){
            partCurrentValue.setText(Value+"일째");     
        }else{
            partCurrentValue.setText(Value+"Value");
        }
        
    }
    public void setProgressBar(int maxValue, int currentValue){
        int percent;
        if( maxValue < 10){
           percent =  (10000*currentValue)/(360*maxValue);
        }else{
            percent =  currentValue*10000 / maxValue;
        }

        progressBar.setProgress(100);
        
    }

    
}