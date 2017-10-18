package com.tape_hencoder;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TapeView tapeView= (TapeView) findViewById(R.id.tape_view);
        final TextView tv_value= (TextView) findViewById(R.id.tv_value);
        tv_value.setText(tapeView.getCurrentValue()+"");
        tapeView.setValueListener(new TapeView.ValueListener() {
            @Override
            public void OnValue(int value) {
                tv_value.setText(value+"");
            }
        });
    }
}
