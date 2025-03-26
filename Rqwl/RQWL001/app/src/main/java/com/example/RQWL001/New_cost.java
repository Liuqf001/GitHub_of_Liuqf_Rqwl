package com.example.RQWL001;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.nanchen.calendarview.MyCalendarView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class New_cost extends AppCompatActivity {
    private DBHelper helper;
    private EditText[] et_cost_title;
    private EditText[] et_cost_money;
    private EditText et_cost_remark;
    //    private DatePicker dp_cost_date;
    private Button buttonInOut;

    String currentselectDate;

    ArrayAdapter<String> adapter;
    ArrayAdapter<String> adapterRe;
    AutoCompleteTextView completeTextViewRe;
    AutoCompleteTextView completeTextView;
    AutoCompleteTextView completeTextView2;
    AutoCompleteTextView completeTextView3;
    AutoCompleteTextView completeTextView4;
    AutoCompleteTextView completeTextView5;
    List<String> curName;   //光标提示姓名
    List<String> curThing;   //光标提示事由

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_cost);
        findViews();  //准备姓名提示
        initView();

        MyCalendarView calendarView = (MyCalendarView) findViewById(R.id.calendarView);
        calendarView.setClickDataListener((int year, int month, int day)-> {
//                String date = String.format(Locale.CHINA, "%04d-%02d-%02d", year, month, day);
//                Toast toast = Toast.makeText(this, date, Toast.LENGTH_SHORT);
//                toast.setGravity(Gravity.CENTER, 0, 0);
//                toast.show();
            currentselectDate = String.format(Locale.CHINA, "%04d-%02d-%02d", year, month, day);
        });
    }


    @Override
    public void onBackPressed() {
        // 在用户按下返回键时执行的代码
//        Toast.makeText(this, "返回键被点击了！", Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }


    private void ShowToast(String strToShow, int backColor) {
        Toast toast = Toast.makeText(New_cost.this, strToShow, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);  //提示显示时间短
        LinearLayout layout = (LinearLayout) toast.getView();
        layout.setBackgroundColor(backColor);//(0x8f0f0F0F);//0xff0fFF0F   //  Color.GRAY);Color.BLUE
        layout.setClipToOutline(true);
        layout.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), 40);
            }
        });
        TextView tv = (TextView) layout.getChildAt(0);
        tv.setTextSize(20);
        tv.setTextColor(Color.WHITE);
        toast.show();
    }

    public void retOnFlag(View view) {//跳转
        finish();  //return ;
//        return;

    }

    private void findViews() {
        curName = new ArrayList<>();
        if (helper == null) helper = new DBHelper(New_cost.this);
        SQLiteDatabase db = helper.getReadableDatabase();
        String sqlwhere = "select distinct Title from account "; //Title like ?" ;
        Cursor cursor;
        cursor = db.rawQuery(sqlwhere, null);
        while (cursor.moveToNext()) {   // find all name in database
            curName.add(cursor.getString(cursor.getColumnIndex("Title")));
        }
        cursor.close();
        db.close();
        completeTextView = findViewById(R.id.et_cost_title);
        adapter = new ArrayAdapter<>(this, R.layout.auto_list_item, curName);
        completeTextView.setAdapter(adapter);

        curThing = new ArrayList<>();
        db = helper.getReadableDatabase();
        sqlwhere = "select distinct Remark from account ";
        cursor = db.rawQuery(sqlwhere, null);
        while (cursor.moveToNext()) {   // find all remark in database
            curThing.add(cursor.getString(cursor.getColumnIndex("Remark")));
        }
        cursor.close();
        db.close();
        completeTextView2 = findViewById(R.id.et_cost_title2);
        completeTextView3 = findViewById(R.id.et_cost_title3);
        completeTextView4 = findViewById(R.id.et_cost_title4);
        completeTextView5 = findViewById(R.id.et_cost_title5);
        completeTextViewRe = findViewById(R.id.et_cost_remark);
        adapterRe = new ArrayAdapter<>(this, R.layout.auto_list_item, curThing);
        completeTextViewRe.setAdapter(adapterRe);
        completeTextView2.setAdapter(adapter);
        completeTextView3.setAdapter(adapter);
        completeTextView4.setAdapter(adapter);
        completeTextView5.setAdapter(adapter);
    }


    private void initView() {
        if (helper == null)
            helper = new DBHelper(New_cost.this);
        et_cost_title = new EditText[]{
                (EditText) findViewById(R.id.et_cost_title),
                (EditText) findViewById(R.id.et_cost_title2),
                (EditText) findViewById(R.id.et_cost_title3),
                (EditText) findViewById(R.id.et_cost_title4),
                (EditText) findViewById(R.id.et_cost_title5)
        };
        et_cost_money = new EditText[]{
                (EditText) findViewById(R.id.et_cost_money),
                (EditText) findViewById(R.id.et_cost_money2),
                (EditText) findViewById(R.id.et_cost_money3),
                (EditText) findViewById(R.id.et_cost_money4),
                (EditText) findViewById(R.id.et_cost_money5)
        };

        et_cost_remark = findViewById(R.id.et_cost_remark);
//        dp_cost_date = findViewById(R.id.dp_cost_date);
        buttonInOut = findViewById(R.id.buttonInOut);
        Intent intent = getIntent();
        String dataName = intent.getStringExtra("CurrentName");
        String datainOut = intent.getStringExtra("datainOut");
        if(datainOut!=null) {
            if (datainOut.equals("收礼")) {
                buttonInOut.setText("收礼");
                GradientDrawable gdOne = (GradientDrawable) buttonInOut.getBackground();// get drabable
                gdOne.setColor(Color.RED);      //change color
                for (int i = 0; i < 5; i++)
                    et_cost_money[i].setTextColor(Color.RED);
            } else {
                buttonInOut.setText("送礼");
                GradientDrawable gdOne = (GradientDrawable) buttonInOut.getBackground();// get drabable
                gdOne.setColor(Color.GREEN);      //change color
                for (int i = 0; i < 5; i++)
                    et_cost_money[i].setTextColor(Color.GREEN);
            }
        }
        if((dataName!=null)&& (!dataName.equals("")))
            et_cost_title[0].setText(dataName);
//        et_cost_money.setFocusable(true);
//        et_cost_money.setFocusableInTouchMode(true);
    }

    public void InOut(View view) {
        GradientDrawable gdOne = (GradientDrawable) buttonInOut.getBackground();// get drabable
        if (buttonInOut.getText().toString().equals("收礼")) {
            buttonInOut.setText("送礼");
            gdOne.setColor(Color.GREEN);      //change color
            for (int i = 0; i < 5; i++)
                et_cost_money[i].setTextColor(Color.GREEN);
        } else {
            buttonInOut.setText("收礼");
            gdOne.setColor(Color.RED);      //change color
            for (int i = 0; i < 5; i++)
                et_cost_money[i].setTextColor(Color.RED);
        }
    }

//    public void ondateclick(View view) {
//        String dateStr = dp_cost_date.getYear() + "-" + (dp_cost_date.getMonth() + 1) + "-"
//                + dp_cost_date.getDayOfMonth();
//    }

    public void okButton(View view) {
        String titleStr = et_cost_title[0].getText().toString().trim();
        String moneyStr ;
        String remarkStr = et_cost_remark.getText().toString().trim();
        String inoutStr = buttonInOut.getText().toString().trim();
        String dateStr;

        MyCalendarView calendarView = (MyCalendarView) findViewById(R.id.calendarView);
        dateStr = calendarView.getcurrentselectDate() + " " + calendarView.getcurrentsellunarDate();

//        calendarView.setWindowAnimations(R.style.AnimBottom);
        long account ;
        int iNumber = 0;
        Intent intent = getIntent();
        intent.putExtra("CurrentName", titleStr);  //传参数出去， bring data to father view 带出数据
        intent.putExtra("CurrentRemark", remarkStr);
        intent.putExtra("CurrentDate", dateStr);
        intent.putExtra("datainOut", inoutStr);

        for (int i = 0; i < 5; i++) {
            titleStr = et_cost_title[i].getText().toString().trim();
            moneyStr = et_cost_money[i].getText().toString().trim();
            if ((!"".equals(moneyStr)) && (!"".equals(titleStr)))  // 姓名和金额不能为空
                iNumber = iNumber + 1;
        }
        if (iNumber > 0) {
            iNumber = 0;
            for (int i = 0; i < 5; i++) {
                SQLiteDatabase db = helper.getWritableDatabase();
                ContentValues values = new ContentValues();
                titleStr = et_cost_title[i].getText().toString().trim();
                moneyStr = et_cost_money[i].getText().toString().trim();
                values.put("Title", titleStr);
                values.put("Remark", remarkStr);
                values.put("Money", moneyStr);
                values.put("Date", dateStr);
                values.put("InOut", inoutStr);
                if ((!"".equals(moneyStr)) & (!"".equals(titleStr))) {
                    account = db.insert("account", null, values);
                    if (account > 0) iNumber = iNumber + 1;
                    db.close();
                }
            }
            if (iNumber > 0) {  //添加了一条或者多条数据
                String charShow = " "+iNumber+ "条记录被保存成功 " ;
                ShowToast(charShow,Color.BLUE);
                setResult(RESULT_OK, intent);//传参数出去
            } else {
                ShowToast("请重试",Color.RED);
                setResult(RESULT_CANCELED, intent);//，添加记录失败，传参数出去
            }
            //Close the input soft keyboard
            View view1 = getCurrentFocus();
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view1.getWindowToken(),0);
            finish(); //销毁本页面
        }
        else
        {
            ShowToast("请填写姓名和金额", Color.RED); // 姓名和金额不能为空
            setResult(RESULT_CANCELED, intent); // 传参数出去
        }
    }
}
