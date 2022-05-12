package com.qiaoza666.graduationproject;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;

public class TYPhoonList extends AppCompatActivity {
    SQLiteDatabase db=SQLiteDatabase.openDatabase ("/data/data/com.qiaoza666.graduationproject/databases/TyPhoon.db",null,SQLiteDatabase.OPEN_READONLY);

    boolean isShowingResult=false;
    List<String> searchResult=new ArrayList<String>();
    static ArrayList<String> askIdList = new ArrayList<String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_typhoon_list);

        LinearLayout ll1;
        ll1 = findViewById(R.id.ll_livelist);
        if(searchLive()){
            for(int i=0;i<searchResult.size();i++){
                TextView tv=findViewById(R.id.txt_live);
                tv.setText("当前正在活跃的台风：");
                showSearchList(ll1,searchResult.get(i),2022);
            }
            searchResult.clear();
        }

        showYear(R.id.txt2022,"2022",R.id.ll2022);
        showYear(R.id.txt2021,"2021",R.id.ll2021);
        showYear(R.id.txt2020,"2020",R.id.ll2020);
        showYear(R.id.txt2019,"2019",R.id.ll2019);
        showYear(R.id.txt2018,"2018",R.id.ll2018);

        Button btn = (Button) this.findViewById(R.id.btn_search);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText editText1 =(EditText) findViewById (R.id.txtyear);
                int year=Integer.parseInt(editText1.getText().toString());
                EditText editText2 =(EditText) findViewById (R.id.txtmonth);
                int month=Integer.parseInt(editText2.getText().toString());
                EditText editText3 =(EditText) findViewById (R.id.txtday);
                int day=Integer.parseInt(editText3.getText().toString());
                LinearLayout ll;
                ll = findViewById(R.id.searchResultList);
                if(isShowingResult){ll.removeAllViews();return ;}
                if(judgeDate(year,month,day)){

                    if(searchDataBase(year,month,day)){

                        for(int i=0;i<searchResult.size();i++){

                            String tyPhoonID=searchResult.get(i);
                            showSearchList(ll,tyPhoonID,year);
                            isShowingResult=true;
                        }
                    }
                    else{
                        Toast.makeText(TYPhoonList.this,"查询完成："+year+"年"+month+"月"+day+"日"+"没有台风",Toast.LENGTH_LONG).show();
                    }

                }
            }
        });
    }


    /*设置每年的点击事件*/
    public void showYear(int id1, String year, int id2){
        TextView textView1 ;
        textView1 = findViewById(id1);
        textView1.setClickable(true);//设置点击事件
        textView1.setOnClickListener(new View.OnClickListener() {
            int state1=0;
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View arg0) {
                if(state1==0){
                    showList(year,id2,1);
                    state1=1;
                }
                else{
                    showList(year,id2,0);
                    state1=0;
                }
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    public void showList(String year,int id,int sta){

        LinearLayout ll;
        ll = findViewById(id);
        if(sta==0){ll.removeAllViews();return ;}
        String tableName="typhoon_list_"+year;
        String sql="select * from "+tableName;
        Cursor cursor=db.rawQuery(sql, null);//游标
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){//进行遍历
            String typhoonID=cursor.getString(cursor.getColumnIndexOrThrow("typhoonID"));
            String typhoonEngName=cursor.getString(cursor.getColumnIndexOrThrow("typhoonEngName"));
            String typhoonCinName=cursor.getString(cursor.getColumnIndexOrThrow("typhoonCinName"));
            String typhoonLive=cursor.getString(cursor.getColumnIndexOrThrow("typhoonLive"));

            TextView textView = new TextView(this);

            if(typhoonLive.equals("stop")){
                textView.setText(" "+typhoonID+" "+typhoonEngName+" "+typhoonCinName+" [已停止]");
            }
            else{
                textView.setText(" "+typhoonID+" "+typhoonEngName+" "+typhoonCinName+" [活跃中]");
            }

            textView.setTextSize(20);
            textView.setTextColor(R.color.black);

            textView.setClickable(true);//设置点击事件
            textView.setOnClickListener(new View.OnClickListener() {
                int state=0;
                @SuppressLint("ResourceAsColor")
                @Override
                public void onClick(View arg0) {
                    if(state==0){
                        askIdList.add(typhoonID);
                        textView.setTextColor(R.color.purple_500);
                        String s=textView.getText().toString();
                        TextPaint paint = textView.getPaint();
                        paint.setFakeBoldText(true);
                        state=1;
                    }
                    else{
                        askIdList.remove(typhoonID);
                        textView.setTextColor(R.color.black);
                        TextPaint paint = textView.getPaint();
                        paint.setFakeBoldText(false);
                        state=0;
                    }
                }
            });
            ll.addView(textView); //添加一个View
            cursor.moveToNext();
        }
    }

    /*检查日期合法性*/
    public boolean judgeDate(int year, int month, int day) {
        int[] days = {31,28,31,30,31,30,31,31,30,31,30,31};
        //首先判断月份是否合法
        if (month >= 1 && month <= 12) {
            if(year>2022){
                Toast.makeText(TYPhoonList.this,"未来的事我不知道喵~",Toast.LENGTH_SHORT).show();
                return false;
            }
            else if(year<1949){
                Toast.makeText(TYPhoonList.this,"太久远的事我不记得喵~",Toast.LENGTH_SHORT).show();
                return false;
            }
            //判断是否为闰年
            if ((year % 100 == 0 && year % 400 == 0) || year % 4 == 0) {
                //判断当前月份是否为2月,因为闰年的2月份为29天
                if (month == 2 && day <= 29) {
                    return true;
                }
                else {
                    if (day <= days[month - 1]){
                        return true;
                    }
                }
            } else {
                if (day <= days[month - 1]){
                    return true;
                }
            }
        }
        Toast.makeText(TYPhoonList.this,"请输入正确日期，喵~",Toast.LENGTH_SHORT).show();
        return false;
    }
    /*数据库内查询该日期的台风*/
    public boolean searchDataBase(int year,int month,int day){
        boolean hasFind=false;
        try {
            String tableName="typhoon_list_"+year;
            String sql="select * from "+tableName;
            Cursor cursor1=db.rawQuery(sql, null);//游标
            cursor1.moveToFirst();

            while(!cursor1.isAfterLast()){//进行遍历
                String typhoonID=cursor1.getString(cursor1.getColumnIndexOrThrow("typhoonID"));
                tableName="typhoon_"+typhoonID;
                String m = String.format("%02d",month);
                String d = String.format("%02d",day);
                String date=year+"-"+m+"-"+d;
                sql="select * from "+tableName+" where start_time like '%"+date+"%';";
                Cursor cursor=db.rawQuery(sql, null);//游标
                if(cursor != null && cursor.getCount()>=0) {
                    hasFind=true;
                    searchResult.add(typhoonID);
                }
                cursor1.moveToNext();
            }
        }catch (SQLException e){
        }
        return hasFind;
    }
    public boolean searchLive(){
        boolean hasFind=false;

        String sql="select * from typhoon_list_2022 where typhoonLive = \"live\" ";
        Cursor cursor1=db.rawQuery(sql, null);//游标
        cursor1.moveToFirst();
        while(!cursor1.isAfterLast()) {//进行遍历
            String typhoonID=cursor1.getString(cursor1.getColumnIndexOrThrow("typhoonID"));
            hasFind = true;
            searchResult.add(typhoonID);
            cursor1.moveToNext();
        }
        return hasFind;
    }
    @SuppressLint("ResourceAsColor")
    public void showSearchList(LinearLayout ll,String typhoonID, int year){
        String tableName="typhoon_list_"+year;
        String sql="select * from "+tableName+" where typhoonID="+typhoonID;

        Cursor cursor=db.rawQuery(sql, null);//游标
        cursor.moveToFirst();
        String typhoonEngName=cursor.getString(cursor.getColumnIndexOrThrow("typhoonEngName"));
        String typhoonCinName=cursor.getString(cursor.getColumnIndexOrThrow("typhoonCinName"));
        String typhoonLive=cursor.getString(cursor.getColumnIndexOrThrow("typhoonLive"));

        TextView textView = new TextView(this);
        if(typhoonLive.equals("stop")){
            textView.setText(" "+typhoonID+" "+typhoonEngName+" "+typhoonCinName+" [已停止]");
        }
        else{
            textView.setText(" "+typhoonID+" "+typhoonEngName+" "+typhoonCinName+" [活跃中]");
        }

        textView.setTextSize(20);
        textView.setTextColor(R.color.black);

        textView.setClickable(true);//设置点击事件
        textView.setOnClickListener(new View.OnClickListener() {
            int state=0;
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View arg0) {
                if(state==0){
                    askIdList.add(typhoonID);
                    textView.setTextColor(R.color.purple_500);
                    TextPaint paint = textView.getPaint();
                    paint.setFakeBoldText(true);
                    state=1;
                }
                else{
                    askIdList.remove(typhoonID);
                    textView.setTextColor(R.color.black);
                    TextPaint paint = textView.getPaint();
                    paint.setFakeBoldText(false);
                    state=0;
                }
            }
        });
        ll.addView(textView); //添加一个View
    }

}