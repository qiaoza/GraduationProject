package com.qiaoza666.graduationproject;

import static android.media.CamcorderProfile.get;
import static com.qiaoza666.graduationproject.TYPhoonList.askIdList;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps2d.AMap;
import com.amap.api.maps2d.AMapOptions;
import com.amap.api.maps2d.CameraUpdate;
import com.amap.api.maps2d.CameraUpdateFactory;
import com.amap.api.maps2d.LocationSource;
import com.amap.api.maps2d.MapView;
import com.amap.api.maps2d.UiSettings;
import com.amap.api.maps2d.model.BitmapDescriptor;
import com.amap.api.maps2d.model.BitmapDescriptorFactory;
import com.amap.api.maps2d.model.CameraPosition;
import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.Marker;
import com.amap.api.maps2d.model.MarkerOptions;
import com.amap.api.maps2d.model.Polyline;
import com.amap.api.maps2d.model.PolylineOptions;
import com.google.gson.Gson;
import com.qweather.sdk.bean.base.Basin;
import com.qweather.sdk.bean.base.Code;
import com.qweather.sdk.bean.base.Lang;
import com.qweather.sdk.bean.base.Unit;
import com.qweather.sdk.bean.tropical.StormListBean;
import com.qweather.sdk.bean.tropical.StormTrackBean;
import com.qweather.sdk.bean.weather.WeatherNowBean;
import com.qweather.sdk.view.HeConfig;
import com.qweather.sdk.view.QWeather;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper helper;//??????DatabaseHelper??????
    SQLiteDatabase db;//???????????????
    private MapView mapView;
    private AMap aMap;

    NotificationManager notificationManager; //NotificationManager??????????????????????????????????????????????????????????????????????????????

    private TextView textViewMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewMain = findViewById(R.id.text_id);
        textViewMain.setMarqueeRepeatLimit(Integer.MAX_VALUE);
        textViewMain.setFocusable(true);
        textViewMain.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        textViewMain.setSingleLine();
        textViewMain.setFocusableInTouchMode(true);
        textViewMain.setHorizontallyScrolling(true);

        //???????????????
        helper = new DatabaseHelper(this, "TyPhoon.db", null, 1);
        //???????????????????????????????????????????????????(?????????.db)???????????????????????????
        db = helper.getWritableDatabase();
        /*?????????????????????*//*
        for(int i=2022;i>=2018;i--){
            String year=i+"";
            String sql="insert into typhoon_year_list(typhoon_year) values ('"+year+"')";
            db.execSQL(sql);
        }
        */
        /*???????????????????????????????????????????????? *//*
        Table_typhoon_list("2022");
        Table_typhoon_list("2021");
        Table_typhoon_list("2020");
        Table_typhoon_list("2019");
        Table_typhoon_list("2018");
        */
        /*????????????????????????????????? *//*
        Table_typhoon_Data("2022");
        Table_typhoon_Data("2021");
        Table_typhoon_Data("2020");
        Table_typhoon_Data("2019");
        Table_typhoon_Data("2018");
         */
        delTestData();
//????????????
        mapView = findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        aMapInit();

//????????????
        HeConfig.init("HE2205041116051230", "702fe692ff0d4281b12c7417acfa2c62");//?????????????????????
        HeConfig.switchToDevService();//???????????????????????????
//??????
        //??????????????????
        createNotificationChannel();
//????????????
        Toast.makeText(this, "??????????????????", Toast.LENGTH_LONG).show();
        QWeather.getStormList(MainActivity.this, "2022", Basin.NP, new QWeather.OnResultTropicalStormListListener() {
            @Override
            public void onError(Throwable throwable) {
                Log.i("TAG", "??????????????????");
            }

            @Override
            public void onSuccess(StormListBean stormListBean) {
                Log.i("TAG", "??????????????????: " + new Gson().toJson(stormListBean));
                if (Code.OK == stormListBean.getCode()) {
                    Toast.makeText(MainActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
                    List<StormListBean.StormBean> data = stormListBean.getStormList();
                    String stormId = data.get(0).getId();
                    List<String> list = stormBeanToList(data.get(0));
                    QWeather.OnResultTropicalStormTrackListener listener = new QWeather.OnResultTropicalStormTrackListener() {
                        @Override
                        public void onError(Throwable throwable) {
                            Log.i("TAG", "??????????????????");
                        }

                        @Override
                        public void onSuccess(StormTrackBean stormTrackBean) {
                            if (Code.OK == stormTrackBean.getCode()) {
                                List<StormTrackBean.StormTrackBaseBean> data = stormTrackBean.getTrackList();
                                List<List<String>> DT = stormTrackBaseBeanToList(data);
                                upList(list, DT);
                            } else {
                                Code code = stormTrackBean.getCode();
                            }
                            Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                            sendNotification();
                        }
                    };
                    QWeather.getStormTrack(MainActivity.this, stormId, listener);
                } else {
                    Code code = stormListBean.getCode();
                    Toast.makeText(MainActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        add24_48Line(aMap);
        searchLive();
        for (int i = 0; i < askIdList.size(); i++) {
            addTyphoon(aMap, askIdList.get(i));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause(); //?????????????????????
    }

    @Override
    protected void onResume() {
        super.onResume();
        //????????????????????????
        mapView.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        aMap.clear();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        delTestData();
        mapView.onDestroy();//????????????
    }

    /*????????????*/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu1:/*????????????*/
                askIdList.clear();
                Intent intent = new Intent(this, TYPhoonList.class);
                this.startActivity(intent);
                break;
            case R.id.menu2:/*??????????????????*/
                Toast.makeText(this, "??????????????????", Toast.LENGTH_SHORT).show();
                QWeather.getStormList(MainActivity.this, "2022", Basin.NP, new QWeather.OnResultTropicalStormListListener() {
                    @Override
                    public void onError(Throwable throwable) {
                        Log.i("TAG", "??????????????????");
                    }

                    @Override
                    public void onSuccess(StormListBean stormListBean) {
                        Log.i("TAG", "??????????????????: " + new Gson().toJson(stormListBean));
                        if (Code.OK == stormListBean.getCode()) {
                            Toast.makeText(MainActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
                            List<StormListBean.StormBean> data = stormListBean.getStormList();
                            String stormId = data.get(0).getId();
                            List<String> list = stormBeanToList(data.get(0));
                            QWeather.OnResultTropicalStormTrackListener listener = new QWeather.OnResultTropicalStormTrackListener() {
                                @Override
                                public void onError(Throwable throwable) {
                                    Log.i("TAG", "??????????????????");
                                }

                                @Override
                                public void onSuccess(StormTrackBean stormTrackBean) {
                                    if (Code.OK == stormTrackBean.getCode()) {
                                        List<StormTrackBean.StormTrackBaseBean> data = stormTrackBean.getTrackList();
                                        List<List<String>> DT = stormTrackBaseBeanToList(data);
                                        upList(list, DT);
                                    } else {
                                        Code code = stormTrackBean.getCode();
                                    }
                                    Toast.makeText(MainActivity.this, "????????????", Toast.LENGTH_SHORT).show();
                                    sendNotification();
                                }
                            };
                            QWeather.getStormTrack(MainActivity.this, stormId, listener);
                        } else {
                            Code code = stormListBean.getCode();
                            Toast.makeText(MainActivity.this, "????????????????????????", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                searchLive();
                break;
            case R.id.menu3:/*??????????????????*/

                addTestData();
                Toast.makeText(this, "??????????????????????????????????????????????????????", Toast.LENGTH_LONG).show();
                searchLive();
                break;
            case R.id.menu4:/*????????????*/
                Intent intent2 = new Intent(this, DefenseGuide.class);
                this.startActivity(intent2);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    String channel_id = "ID"; //NotificationChannel???ID
    String channel_name = "????????????";  //NotificationChannel?????????
    String channel_desc = "???????????????"; //NotificationChannel?????????
    String notification_title = "?????????????????????????????????";
    String notification_text = "????????????????????????????????????";
    int notificationId = 10086;

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            //????????????????????????
            NotificationChannel channel = new NotificationChannel(channel_id, channel_name, importance);
            //????????????????????????
            channel.setDescription(channel_desc);
            // ?????????????????????????????????????????????????????????
            channel.setSound(null, null);
            // ??????????????????????????????????????? android ?????????????????????
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            // ??????????????????????????????????????? android ?????????????????????
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
            //??????NotificationManager??????
            notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
            //??? notificationManager ????????????????????????
            notificationManager.createNotificationChannel(channel);
        } else {//Android8.0(API26)??????
            notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    /**
     * ????????????
     */
    private void sendNotification() {
        //????????????PendingIntent??????Notification???????????????Activity
        Intent it = new Intent(this, MainActivity.class);
        PendingIntent pit = PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE);

        //??????????????????????????????
        Notification notification = new NotificationCompat.Builder(this, channel_id)
                .setContentTitle(notification_title) //??????
                .setContentText(notification_text) //??????
                .setWhen(System.currentTimeMillis()) //????????????????????????????????????????????????
                .setSmallIcon(R.mipmap.ic_launcher) //???????????????
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_round)) //???????????????
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE)    //????????????????????????????????????
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true) //??????????????????????????????????????????
                .setContentIntent(pit)  //??????PendingIntent
                .build();
        //???????????????????????????????????????id??????????????????id?????????????????????????????????????????????????????????
        notificationManager.notify(notificationId, notification);
    }

    //???????????????
    public void addMark(AMap aMap, double latitude, double longitude, String title, String snippet, String type) {
//          ?????????
        LatLng latLng = new LatLng(latitude, longitude);
//        ???????????????
        MarkerOptions options = new MarkerOptions();
        options.anchor((float) 0.5, (float) 0.5);//??????marker ???????????????????????????marker ???????????????????????????????????????????????????????????????0.5,1.0??????
        options.position(latLng)
                .title(title)
//        ??????????????????
                .snippet(snippet);
//        ??????????????????
        if (type.equals("SuperTY") || type.equals("STY") || type.equals("TY")) {
            options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                    .decodeResource(getResources(), R.drawable.red)));
        } else if (type.equals("STS")) {
            options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                    .decodeResource(getResources(), R.drawable.orange)));
        } else if (type.equals("TS")) {
            options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                    .decodeResource(getResources(), R.drawable.yellow)));
        } else if (type.equals("TD")) {
            options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                    .decodeResource(getResources(), R.drawable.blue)));
        } else {
            options.icon(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                    .decodeResource(getResources(), R.drawable.black)));
        }

//        ?????????
        Marker marker = aMap.addMarker(options);

    }

    public void addLastMark(AMap aMap, double latitude, double longitude, String title, String snippet) {
        Log.i("TAG", "??????????????????");
//          ?????????
        LatLng latLng = new LatLng(latitude, longitude);
//        ???????????????
        MarkerOptions options = new MarkerOptions();
        options.anchor((float) 0.5, (float) 0.5);//??????marker ???????????????????????????marker ???????????????????????????????????????????????????????????????0.5,1.0??????
        options.position(latLng)
                .title(title)
//        ??????????????????
                .snippet(snippet);
//        ??????????????????
        java.util.ArrayList<BitmapDescriptor> list = new ArrayList<>();
        list.add(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                .decodeResource(getResources(), R.drawable.ty0)));
        list.add(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                .decodeResource(getResources(), R.drawable.ty45)));
        list.add(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                .decodeResource(getResources(), R.drawable.ty90)));
        list.add(BitmapDescriptorFactory.fromBitmap(BitmapFactory
                .decodeResource(getResources(), R.drawable.ty135)));
        options.icons(list);
        options.period(1);

//        ?????????
        Marker marker = aMap.addMarker(options);
    }

    //???????????????
    private void addLine(AMap aMap, double latitude1, double longitude1, double latitude2, double longitude2, int color, boolean setDottedLine) {
//      ????????????????????????
        LatLng latLng1 = new LatLng(latitude1, longitude1);
        LatLng latLng2 = new LatLng(latitude2, longitude2);

        int col = this.getResources().getColor(color);
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.add(latLng1, latLng2)
                .color(col)//0xFFFFFFFF??????
                .setDottedLine(setDottedLine)// ???????????????
                .width(5);//?????????
        Polyline polyline = aMap.addPolyline(polylineOptions);
    }

    //????????????24??????????????????48???????????????
    private void add24_48Line(AMap aMap) {
        //24???????????????,???????????????
        addLine(aMap, 34, 127, 22, 127, R.color.yellow, false);
        addLine(aMap, 22, 127, 18, 119, R.color.yellow, false);
        addLine(aMap, 18, 119, 11, 119, R.color.yellow, false);
        addLine(aMap, 11, 119, 4.5, 113, R.color.yellow, false);
        addLine(aMap, 4.5, 113, 0, 105, R.color.yellow, false);
        //48???????????????,???????????????
        addLine(aMap, 34, 132, 15, 132, R.color.yellow, true);
        addLine(aMap, 15, 132, 0, 120, R.color.yellow, true);
        addLine(aMap, 0, 120, 0, 105, R.color.yellow, true);
    }

    //??????????????????
    public String TP_state(String tp_type) {
        switch (tp_type) {
            case "SuperTY":
                return "????????????";
            case "STY":
                return "?????????";
            case "TY":
                return "??????";
            case "STS":
                return "???????????????";
            case "TS":
                return "????????????";
            case "TD":
                return "????????????";
        }
        return "??????";
    }
    public class MyHandler extends Handler {
        double last_longitude;
        double last_latitude;

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            String typhoonID = msg.getData().getString("typhoonID");
            String typhoonName = msg.getData().getString("typhoonName");
            String sql = "select * from typhoon_" + typhoonID;
            Cursor cursor = db.rawQuery(sql, null);
            cursor.moveToPosition(msg.arg1);

            String start_time = cursor.getString(cursor.getColumnIndexOrThrow("start_time"));
            double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));
            double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
            String tp_type = cursor.getString(cursor.getColumnIndexOrThrow("tp_type"));
            int central_pressure = cursor.getInt(cursor.getColumnIndexOrThrow("central_pressure"));
            int wind = cursor.getInt(cursor.getColumnIndexOrThrow("wind"));
            String direction = cursor.getString(cursor.getColumnIndexOrThrow("direction"));
            int feature_speed = cursor.getInt(cursor.getColumnIndexOrThrow("feature_speed"));
            String denglu = cursor.getString(cursor.getColumnIndexOrThrow("denglu"));
            String snippet;
            if (denglu.equals("0")) {
                snippet = start_time + "\n??????:" + TP_state(tp_type) + "\n????????????:" + central_pressure + "(??????)\n????????????:" + wind + "(???/???)\n????????????:" + feature_speed + "(??????/??????)\n???????????????" + direction;
            } else {
                snippet = start_time + "\n" + denglu;
            }
            if (msg.arg1 == msg.arg2 - 1) {
                addLastMark(aMap, latitude, longitude, typhoonName, snippet);
            } else {
                addMark(aMap, latitude, longitude, typhoonName, snippet, tp_type);
            }
            if (msg.what == 1) {
                addLine(aMap, last_latitude, last_longitude, latitude, longitude, R.color.white, false);
            } else {
                msg.what = 1;
            }
            last_longitude = longitude;
            last_latitude = latitude;
            //Log.i("TAG",msg.arg1+"????????????");
            msg.arg1 = msg.arg1 + 1;
            if (msg.arg1 >= msg.arg2) {
                Log.i("TAG", typhoonID + "????????????");
            } else {
                Message msg1 = Message.obtain(this, 0x0);
                Bundle data = new Bundle();
                data.putString("typhoonID", typhoonID);
                data.putString("typhoonName", typhoonName);
                msg1.setData(data);
                msg1.arg1 = msg.arg1;
                msg1.arg2 = msg.arg2;
                msg1.what = msg.what;
                sendMessageDelayed(msg1, 250);
            }
        }
    }
    public void addTyphoon(AMap aMap, String typhoonID) {
        String sql_list = "select * from typhoon_list_20" + typhoonID.substring(0, 2) + " where typhoonID=" + typhoonID;
        Cursor cursor_Name = db.rawQuery(sql_list, null);
        cursor_Name.moveToFirst();
        String typhoonName = cursor_Name.getString(cursor_Name.getColumnIndexOrThrow("typhoonCinName"));
        cursor_Name.close();
        //Log.i("TAG",typhoonName);
        String sql = "select * from typhoon_" + typhoonID;
        Cursor cursor = db.rawQuery(sql, null);
        int Count = cursor.getCount();

        MyHandler handler = new MyHandler();
        Message msg = Message.obtain(handler, 0x0);
        Bundle data = new Bundle();
        data.putString("typhoonID", typhoonID);
        data.putString("typhoonName", typhoonName);
        msg.setData(data);
        msg.arg1 = 0;
        msg.arg2 = Count;
        msg.what = 0;

        handler.sendMessageDelayed(msg, 250);
    }

    //??????????????????????????????
    private Sheet read_test_Data(Context context) throws BiffException, IOException {
        String name = "testData.xls";
        AssetManager assetManager = context.getAssets();
        Workbook book = Workbook.getWorkbook(assetManager.open(name));
        Sheet sheet = book.getSheet(0);
        Log.i("TAG", "???????????????");
        return sheet;
    }

    public void delTestData() {
        del_table("typhoon_2299");
        try {
            String sql = "delete from typhoon_list_2022 where typhoonID=2299";
            db.execSQL(sql);
        } catch (SQLException e) {
        }
    }

    public void addTestData() {
        delTestData();
        List<String> testData = Arrays.asList("2299", "????????????", "TESTSTORM", "live");
        try {
            String tableName = "typhoon_2299";
            String sql = "CREATE TABLE " + tableName + "(tpId integer primary key autoincrement," +
                    "start_time text not null," +
                    "longitude double not null," +
                    "latitude double not null," +
                    "tp_type text not null," +
                    "central_pressure int not null," +
                    "wind int not null," +
                    "direction text not null," +
                    "feature_speed int not null," +
                    "denglu text not null)";
            db.execSQL(sql);
            Sheet sheet = read_test_Data(this);
            for (int i = 0; i < 45; i++) {
                String start_time = sheet.getCell(1, i).getContents();
                String s_longitude = sheet.getCell(2, i).getContents();
                String s_latitude = sheet.getCell(3, i).getContents();
                String tp_type = sheet.getCell(4, i).getContents();
                String s_central_pressure = sheet.getCell(5, i).getContents();
                String s_wind = sheet.getCell(6, i).getContents();
                String direction = sheet.getCell(7, i).getContents();
                String s_feature_speed = sheet.getCell(8, i).getContents();
                String denglu = sheet.getCell(9, i).getContents();
                int central_pressure;
                int wind;
                int feature_speed;
                double longitude = Double.valueOf(s_longitude);
                double latitude = Double.valueOf(s_latitude);
                if (!denglu.equals("0")) {
                    central_pressure = 0;
                    wind = 0;
                    direction = "0";
                    feature_speed = 0;
                } else {
                    central_pressure = Integer.parseInt(s_central_pressure);
                    wind = Integer.parseInt(s_wind);
                    feature_speed = Integer.parseInt(s_feature_speed);
                }
                sql = "insert into " + tableName +
                        "(start_time,longitude,latitude,tp_type,central_pressure,wind,direction,feature_speed,denglu)" +
                        " values ('" + start_time + "','" + longitude + "','" + latitude + "','" + tp_type + "','" + central_pressure + "','" + wind + "','" + direction + "','" + feature_speed + "','" + denglu + "')";
                db.execSQL(sql);
            }
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String ID = testData.get(0);
        String EngName = testData.get(2);
        String CinName = testData.get(1);
        String Live = testData.get(3);
        String sql = "insert into typhoon_list_2022(typhoonID,typhoonEngName,typhoonCinName,typhoonLive) values ('" + ID + "','" + EngName + "','" + CinName + "','" + Live + "')";
        db.execSQL(sql);
        Log.i("TAG", "????????????");
    }

    //????????????????????????
    private Sheet read_xlx_list(Context context, String year, int sheet_id) throws BiffException, IOException {
        String name = year + "List.xls";
        // ???data_table.xls??? ???????????????????????????????????????
        AssetManager assetManager = context.getAssets();
        Workbook book = Workbook.getWorkbook(assetManager.open(name));
        //??????????????????????????????(ecxel???sheet????????????0??????,0,1,2,3,???.)
        Sheet sheet = book.getSheet(sheet_id);
        return sheet;
    }

    //??????????????????
    private Sheet[] read_xlx_Data(Context context, String year) throws BiffException, IOException {
        String name = year + "Data.xls";
        AssetManager assetManager = context.getAssets();
        Workbook book = Workbook.getWorkbook(assetManager.open(name));
        //??????????????????????????????(ecxel???sheet????????????0??????,0,1,2,3,???.)
        Sheet[] sheet = book.getSheets();
        return sheet;
    }

    //???????????????????????????
    private void Table_typhoon_list(String year) {

        String tableName = "typhoon_list_" + year;
        String sql = "CREATE TABLE " + tableName + "(tpId integer primary key autoincrement," +
                "typhoonID text not null," +
                "typhoonEngName text not null," +
                "typhoonCinName text not null," +
                "typhoonLive text not null)";
        db.execSQL(sql);

        try {
            Sheet sheet = read_xlx_list(this, year, 0);
            for (int i = 0; i < sheet.getRows(); i++) {

                String ID = sheet.getCell(1, i).getContents();
                String EngName = sheet.getCell(2, i).getContents();
                String CinName = sheet.getCell(3, i).getContents();
                String Live = sheet.getCell(4, i).getContents();

                sql = "insert into " + tableName + "(typhoonID,typhoonEngName,typhoonCinName,typhoonLive) values ('" + ID + "','" + EngName + "','" + CinName + "','" + Live + "')";
                db.execSQL(sql);
            }
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    //?????????????????????
    private void Table_typhoon_Data(String year) {

        try {
            Sheet[] sheet = read_xlx_Data(this, year);
            for (int i = 0; i < sheet.length; i++) {
                String typhoonID = sheet[i].getCell(0, 0).getContents();
                String tableName = "typhoon_" + typhoonID;

                String sql = "CREATE TABLE " + tableName + "(tpId integer primary key autoincrement," +
                        "start_time text not null," +
                        "longitude double not null," +
                        "latitude double not null," +
                        "tp_type text not null," +
                        "central_pressure int not null," +
                        "wind int not null," +
                        "direction text not null," +
                        "feature_speed int not null," +
                        "denglu text not null)";
                db.execSQL(sql);
                for (int j = 0; j < sheet[i].getRows(); j++) {

                    String start_time = sheet[i].getCell(1, j).getContents();
                    String s_longitude = sheet[i].getCell(2, j).getContents();
                    String s_latitude = sheet[i].getCell(3, j).getContents();
                    String tp_type = sheet[i].getCell(4, j).getContents();
                    String s_central_pressure = sheet[i].getCell(5, j).getContents();
                    String s_wind = sheet[i].getCell(6, j).getContents();
                    String direction = sheet[i].getCell(7, j).getContents();
                    String s_feature_speed = sheet[i].getCell(8, j).getContents();
                    String denglu = sheet[i].getCell(9, j).getContents();

                    int central_pressure;
                    int wind;
                    int feature_speed;
                    double longitude = Double.valueOf(s_longitude);
                    double latitude = Double.valueOf(s_latitude);
                    if (!denglu.equals("0")) {
                        central_pressure = 0;
                        wind = 0;
                        direction = "0";
                        feature_speed = 0;
                    } else {
                        central_pressure = Integer.parseInt(s_central_pressure);
                        wind = Integer.parseInt(s_wind);
                        feature_speed = Integer.parseInt(s_feature_speed);
                    }
                    sql = "insert into " + tableName +
                            "(start_time,longitude,latitude,tp_type,central_pressure,wind,direction,feature_speed,denglu)" +
                            " values ('" + start_time + "','" + longitude + "','" + latitude + "','" + tp_type + "','" + central_pressure + "','" + wind + "','" + direction + "','" + feature_speed + "','" + denglu + "')";
                    db.execSQL(sql);
                }

            }
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //?????????
    private void del_table(String name) {
        try {
            String sql_del = "DROP TABLE " + name;
            db.execSQL(sql_del);
        } catch (SQLException e) {
            return;
        }
    }

    public List<String> stormBeanToList(StormListBean.StormBean stormBean) {
        List<String> list = new ArrayList<>();
        list.add(stormBean.getId());
        list.add(stormBean.getName());
        list.add(stormBean.getName());
        if (stormBean.getActive().equals("1")) {
            list.add("live");
        } else {
            list.add("stop");
        }
        return list;
    }

    public List<List<String>> stormTrackBaseBeanToList(List<StormTrackBean.StormTrackBaseBean> stormTrackBaseBean) {
        List<List<String>> data = null;
        for (int i = 0; i < stormTrackBaseBean.size(); i++) {
            List<String> temp = new ArrayList<>();
            temp.add("id");
            temp.add(stormTrackBaseBean.get(i).getTime());//????????????????????????
            temp.add(stormTrackBaseBean.get(i).getLon());//??????????????????
            temp.add(stormTrackBaseBean.get(i).getLat());//??????????????????
            temp.add(stormTrackBaseBean.get(i).getType());//????????????
            temp.add(stormTrackBaseBean.get(i).getPressure());//??????????????????
            temp.add(stormTrackBaseBean.get(i).getWindSpeed());//????????????????????????
            temp.add(stormTrackBaseBean.get(i).getMoveDir());//??????????????????
            temp.add(stormTrackBaseBean.get(i).getMoveSpeed());//??????????????????
            temp.add("0");
            data.add(temp);
        }
        return data;
    }

    /*????????????????????????*/
    public void upList(List<String> list, List<List<String>> data) {
        String typhoonID = list.get(0);
        String sql = "SELECT * FROM typhoon_list_2022 where typhoonID=" + typhoonID;
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor != null && cursor.getCount() >= 0) {
            upData(list, data);
        } else {
            String ID = list.get(0);
            String EngName = list.get(1);
            String CinName = list.get(2);
            String Live = list.get(3);
            sql = "insert into typhoon_list_2022(typhoonID,typhoonEngName,typhoonCinName,typhoonLive) values ('" + ID + "','" + EngName + "','" + CinName + "','" + Live + "')";
            db.execSQL(sql);
            upData(list, data);
        }
    }

    /*?????????????????????*/
    public void upData(List<String> list, List<List<String>> data) {
        String typhoonID = list.get(0);
        String tableName = "typhoon_" + typhoonID;
        del_table(tableName);
        String sql = "CREATE TABLE " + tableName + "(tpId integer primary key autoincrement," +
                "start_time text not null," +
                "longitude double not null," +
                "latitude double not null," +
                "tp_type text not null," +
                "central_pressure int not null," +
                "wind int not null," +
                "direction text not null," +
                "feature_speed int not null," +
                "denglu text not null)";
        db.execSQL(sql);
        for (int i = 0; i < data.size(); i++) {
            String start_time = data.get(i).get(1);
            String s_longitude = data.get(i).get(2);
            String s_latitude = data.get(i).get(3);
            String tp_type = data.get(i).get(4);
            String s_central_pressure = data.get(i).get(5);
            String s_wind = data.get(i).get(6);
            String direction = data.get(i).get(7);
            String s_feature_speed = data.get(i).get(8);
            String denglu = data.get(i).get(9);
            int central_pressure;
            int wind;
            int feature_speed;
            double longitude = Double.valueOf(s_longitude);
            double latitude = Double.valueOf(s_latitude);
            if (!denglu.equals("0")) {
                central_pressure = 0;
                wind = 0;
                direction = "0";
                feature_speed = 0;
            } else {
                central_pressure = Integer.parseInt(s_central_pressure);
                wind = Integer.parseInt(s_wind);
                feature_speed = Integer.parseInt(s_feature_speed);
            }
            sql = "insert into " + tableName +
                    "(start_time,longitude,latitude,tp_type,central_pressure,wind,direction,feature_speed,denglu)" +
                    " values ('" + start_time + "','" + longitude + "','" + latitude + "','" + tp_type + "','" + central_pressure + "','" + wind + "','" + direction + "','" + feature_speed + "','" + denglu + "')";
            db.execSQL(sql);
        }
    }

    List<String> searchResult = new ArrayList<String>();
    /*???????????????????????????*/
    public void searchLive(){
        if (searchDataLive()) {
            sendNotification();
            for(int i=0;i<searchResult.size();i=i+2){
                addTyphoon(aMap,searchResult.get(i));
                textViewMain.setText("????????????"+searchResult.get(i)+searchResult.get(i+1)+"????????????");
            }

        } else {
            textViewMain.setText("????????????????????????");
        }
    }
    public boolean searchDataLive() {
        boolean hasFind = false;

        String sql = "select * from typhoon_list_2022 where typhoonLive = \"live\" ";
        Cursor cursor1 = db.rawQuery(sql, null);//??????
        cursor1.moveToFirst();
        while (!cursor1.isAfterLast()) {//????????????
            String typhoonID = cursor1.getString(cursor1.getColumnIndexOrThrow("typhoonID"));
            String typhoonCinName = cursor1.getString(cursor1.getColumnIndexOrThrow("typhoonCinName"));
            hasFind = true;
            searchResult.add(typhoonID);
            searchResult.add(typhoonCinName);
            cursor1.moveToNext();
        }
        return hasFind;
    }


    private void aMapInit() {
        if (aMap == null) {
            aMap = mapView.getMap();

            //???????????????+????????????
            CameraUpdate mCameraUpdate = CameraUpdateFactory.newCameraPosition(new CameraPosition(new LatLng(25.05, 121.50), 4, 30, 0));
            aMap.moveCamera(mCameraUpdate);

            UiSettings mUiSettings = aMap.getUiSettings();

            mUiSettings.setScaleControlsEnabled(true);//??????????????????????????????????????????
        }
    }
}
