package com.qiaoza666.graduationproject;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DefenseGuide extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_defense_guide);
        showTitle1(R.id.txt_defense1,R.id.defense1,R.string.defenceContent1);
        showTitle1(R.id.txt_defense2,R.id.defense2,R.string.defenceContent2);
        showTitle1(R.id.txt_defense3,R.id.defense3,R.string.defenceContent3);
        showTitle1(R.id.txt_defense4,R.id.defense4,R.string.defenceContent4);

    }

    public void showTitle1(int id1,int id2,int txtid){
        TextView textView1 ;
        textView1 = findViewById(id1);
        textView1.setClickable(true);//设置点击事件
        textView1.setOnClickListener(new View.OnClickListener() {
            int state1=0;
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View arg0) {
                if(state1==0){
                    showContent1(id2,1,txtid);
                    state1=1;
                }
                else{
                    showContent1(id2,0,txtid);
                    state1=0;
                }
            }
        });
    }
    public void showContent1(int id,int sta,int txtid){
        LinearLayout ll;
        ll = findViewById(id);
        if(sta==0){
            Log.e("TAG","移除"); ll.removeAllViews();return ;}
        TextView textView = new TextView(this);
        textView.setText(txtid);
        textView.setTextSize(20);
        ll.addView(textView); //添加一个View
    }
}