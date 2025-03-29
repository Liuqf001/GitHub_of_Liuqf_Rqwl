package com.example.RQWL001;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import com.nanchen.calendarview.MyCalendarView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;


public class Edit_cost extends AppCompatActivity {
    private DBHelper helper;
    private EditText et_cost_title;
    private EditText et_cost_money;
    private EditText et_cost_remark;
    private EditText et_cost_memo;
    private EditText dp_cost_date;
    private Button buttonInOut;
    int CurrentId;   //页面初始化时，传入六个的参数
    String CurrentName;
    String CurrentInOut;
    String CurrentRemark;
    String CurrentDate;
    String CurrentMoney;
    String CurrentMemo;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch aSwitchButton;

    ArrayAdapter<String> adapter;
    ArrayAdapter<String> adapter2;
    AutoCompleteTextView completeTextView2;
    AutoCompleteTextView completeTextView;
    List<String> curName;   //光标提示姓名
    List<String> curThing;   //光标提示事由

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_cost);
        findViews();    //准备姓名提示
        initView();

        aSwitchButton  =  findViewById(R.id.switch_batch);
        aSwitchButton.setOnCheckedChangeListener(((CompoundButton compoundButton, boolean isChecked)-> {
                if(isChecked){
                    ShowToast("打开此开关后，您可批量修改姓名、事由、日期, 但不可修改金额数据！",Color.RED);
                    et_cost_money.setEnabled(false);
                    buttonInOut.setEnabled(false);
                    et_cost_memo.setEnabled(false);

                    if (!buttonInOut.getText().toString().equals(CurrentInOut))
                        buttonInOut.setText(CurrentInOut);
                    if (!et_cost_memo.getText().toString().equals(CurrentMemo))
                        et_cost_memo.setText(CurrentMemo);
                    GradientDrawable gdOne = (GradientDrawable) buttonInOut.getBackground();
                    if (CurrentInOut.equals("送礼")) {
                        gdOne.setColor(Color.GREEN);
                        et_cost_money.setTextColor(Color.GREEN);
                    } else {
                        gdOne.setColor(Color.RED);
                        et_cost_money.setTextColor(Color.RED);
                    }
                    aSwitchButton.setThumbTintList(ColorStateList.valueOf(Color.GREEN));//批量开关颜色变化
                }
                else  {
                    et_cost_money.setEnabled(true);
                    buttonInOut.setEnabled(true);
                    et_cost_memo.setEnabled(true);
                    aSwitchButton.setThumbTintList(ColorStateList.valueOf(Color.LTGRAY));
                }
            }
        ));
    }

    @Override
    public void onBackPressed() {
        // 在用户按下返回键时执行的代码
//        Toast.makeText(this, "返回键被点击了！", Toast.LENGTH_SHORT).show();
        super.onBackPressed();
    }

    private void ShowToast(String strToShow, int backColor) {
        Toast toast = Toast.makeText(Edit_cost.this, strToShow, Toast.LENGTH_SHORT);
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
        if (helper == null) helper = new DBHelper(Edit_cost.this);
        SQLiteDatabase db = helper.getReadableDatabase();
        String sqlString = "select distinct Title from account "; //Title like ?" ;
        Cursor cursor;
        cursor = db.rawQuery(sqlString, null);
        while (cursor.moveToNext()) {   // find all name in database
            curName.add(cursor.getString(cursor.getColumnIndex("Title")));
        }
        cursor.close();
        db.close();
        helper.close();
        completeTextView = findViewById(R.id.et_cost_title);
        adapter = new ArrayAdapter<>(this, R.layout.auto_list_item, curName);
        completeTextView.setAdapter(adapter);

        curThing = new ArrayList<>();
        db = helper.getReadableDatabase();
        sqlString = "select distinct Remark from account ";
        cursor = db.rawQuery(sqlString, null);
        while (cursor.moveToNext()) {   // find all remark in database
            curThing.add(cursor.getString(cursor.getColumnIndex("Remark")));
        }
        cursor.close();
        db.close();
        helper.close();
        completeTextView2 = findViewById(R.id.et_cost_remark);
        adapter2 = new ArrayAdapter<>(this, R.layout.auto_list_item, curThing);
        completeTextView2.setAdapter(adapter2);

    }

    private void initView() {
        if (helper == null)
            helper = new DBHelper(Edit_cost.this);
        et_cost_title = findViewById(R.id.et_cost_title);
        et_cost_remark = findViewById(R.id.et_cost_remark);
        et_cost_money = findViewById(R.id.et_cost_money);
        dp_cost_date = findViewById(R.id.dp_cost_date);
        buttonInOut = findViewById(R.id.buttonInOut);
        et_cost_memo = findViewById(R.id.et_cost_memo);
        Intent intent = getIntent();

        CurrentId = intent.getIntExtra("CurrentId", -1);
        CurrentName = intent.getStringExtra("CurrentName");
        CurrentRemark = intent.getStringExtra("CurrentRemark");
        CurrentMoney = intent.getStringExtra("CurrentMoney");
        CurrentDate = intent.getStringExtra("CurrentDate");
        CurrentInOut = intent.getStringExtra("CurrentInOut");
        CurrentMemo = intent.getStringExtra("CurrentMemo");
        if (CurrentId >= 0) {         //有选中， 修改记录
            et_cost_title.setText(CurrentName);
            et_cost_remark.setText(CurrentRemark);
            dp_cost_date.setText(CurrentDate);  //日期
            et_cost_money.setText(CurrentMoney);
            et_cost_memo.setText(CurrentMemo);

        }

        if (Objects.equals(CurrentInOut,"收礼")) {
            buttonInOut.setText("收礼");
            GradientDrawable gdOne = (GradientDrawable) buttonInOut.getBackground();
            gdOne.setColor(Color.RED);      //change color
            et_cost_money.setTextColor(Color.RED);
        } else {
            buttonInOut.setText("送礼");
            GradientDrawable gdOne = (GradientDrawable) buttonInOut.getBackground();
            gdOne.setColor(Color.GREEN);//change color
            et_cost_money.setTextColor(Color.GREEN);
        }

        final MyCalendarView calendarView = findViewById(R.id.calendarView);
        calendarView.setcurrentselectDate(CurrentDate.substring(0, 10));     //10个字符，日期 ;
        calendarView.setOnClickListener((View v)->{
                   // ShowToast("Date was clicked", Color.BLUE) ;
                });

        calendarView.setClickDataListener(((int year, int month, int day)-> {
                String date ;//= String.format(Locale.CHINA, "%04d-%02d-%02d", year, month, day);
                date = String.format(Locale.CHINA, "%04d-%02d-%02d ", year, month, day);
                date = date + calendarView.getcurrentsellunarDate();
                dp_cost_date.setText(date);  //修改日期,显示公历 和 农历
//                dialog.cancel();
            }));

    }

    public void InOut(View view) {
        GradientDrawable gdOne = (GradientDrawable) buttonInOut.getBackground();
        if (buttonInOut.getText().toString().equals("收礼")) {
            buttonInOut.setText("送礼");
            gdOne.setColor(Color.GREEN);
            et_cost_money.setTextColor(Color.GREEN);
        } else {
            buttonInOut.setText("收礼");
            gdOne.setColor(Color.RED);
            et_cost_money.setTextColor(Color.RED);
        }
    }

    //批量修改姓名，事由，日期
    public void batchUpdate(int iModeFlag) {  //return 1: succeed ; 0: failed
        SQLiteDatabase db = helper.getWritableDatabase();
        String titleStr = et_cost_title.getText().toString().trim();
        //        String moneyStr = et_cost_money.getText().toString().trim();
        String remarkStr = et_cost_remark.getText().toString().trim();
        //        String inoutStr = buttonInOut.getText().toString().trim();
        String dateStr = dp_cost_date.getText().toString().trim();

        ContentValues values = new ContentValues();
        int account ;
        switch (iModeFlag) {
            case 1:   //同一个人，批量修改姓名
                values.put("Title", titleStr);
                account = db.update("account", values, "Title=?", new String[]{CurrentName});
                if (account > 0)
                    ShowToast("姓名信息批量修改成功!", Color.BLUE) ;
                break;
            case 2:   //同一事件（事件描述相同，且在同一天发生），批量修改事由
                values.put("Remark", remarkStr);
                account = db.update("account", values, "Remark=? and Date=?", new String[]{CurrentRemark,CurrentDate});
                if (account > 0)
                    ShowToast("事由信息批量修改成功!", Color.BLUE) ;
                break;
            case 3:   //同一事件（事件描述相同，且在同一天发生），批量修改日期
                values.put("Date", dateStr);
                account = db.update("account", values, "Remark=? and Date=?", new String[]{CurrentRemark,CurrentDate});
                if (account > 0)
                    ShowToast("日期信息批量修改成功!", Color.BLUE) ;
                break;
        }
        db.close();
    }

    public void okButton(View view) {
        String new_titleStr = et_cost_title.getText().toString().trim();
        String new_moneyStr = et_cost_money.getText().toString().trim();
        String new_remarkStr = et_cost_remark.getText().toString().trim();
        String new_inoutStr = buttonInOut.getText().toString().trim();
        String new_dateStr = dp_cost_date.getText().toString().trim();
        String new_memoStr = et_cost_memo.getText().toString().trim();

        //批量修改记录开关标志
        boolean isBatchFlag = aSwitchButton.isChecked();
        boolean isNoChangeFlag = false ; //是否有修改数据 true -- no chang

        //可以不填写Title，但是不能不填金额， 姓名和金额不能为空
        if ("".equals(new_moneyStr) | "".equals(new_titleStr)) {
            ShowToast("请填写姓名和金额!", Color.RED) ;
            return;
        }
        // 没有修改任何数据， 直接返回 上一个界面
        if(new_titleStr.equals(CurrentName) &&new_moneyStr.equals(CurrentMoney)
                && new_remarkStr.equals(CurrentRemark) && new_inoutStr.equals(CurrentInOut)
                && new_dateStr.equals(CurrentDate) && new_memoStr.equals(CurrentMemo))
            isNoChangeFlag = true ;
        else if((isBatchFlag) && new_titleStr.equals(CurrentName)  && new_remarkStr.equals(CurrentRemark) &&  new_dateStr.equals(CurrentDate))
            isNoChangeFlag = true ; // 批量修改，  只可以修改姓名，事由，日期三类信息，

        if (isNoChangeFlag) {
            ShowToast("您没有修改任何信息!", Color.RED) ;
            finish();// 没有修改数据 ，直接返回
            return;
        }

        int  m_result;
        long account ;
        Intent intent = getIntent();
        intent.putExtra("CurrentName", new_titleStr);  //传参数出去， bring data to father view 带出数据
        intent.putExtra("CurrentRemark", new_remarkStr);
        intent.putExtra("CurrentDate", new_dateStr);
        intent.putExtra("CurrentInOut", new_inoutStr);
        intent.putExtra("CurrentMemo", new_memoStr);
        //批量开关没打开 ,  修改单条记录数据
        if (!isBatchFlag) {
            //修改单条记录,单条记录修改, 姓名，事由，日期没有改动的情况下，直接单条修改即可
            ContentValues values = new ContentValues();
            values.put("Title", new_titleStr);
            values.put("Remark", new_remarkStr);
            values.put("Money", new_moneyStr);
            values.put("Date", new_dateStr);
            values.put("InOut", new_inoutStr);
            values.put("Memo", new_memoStr);
            SQLiteDatabase db = helper.getWritableDatabase();
            account = db.update("account", values, "_id=" + CurrentId, null);
            db.close();

            if (account > 0) {
                ShowToast("修改信息保存成功!", Color.BLUE) ;//  修改成功， 传参数出去
                m_result = RESULT_OK ;
            } else {
                m_result = RESULT_CANCELED ;  //  修改失败， 传参数出去
                ShowToast("请重试!", Color.BLUE) ;
            }
        } else {
            //批量开关已经打开 // 用户修改了 三个数据之一  批量修改姓名，事由，日期信息，有修改时才批量处理
            String tmpAlertInfo = "您确认要批量修改如下信息？\n\n";
            if (!new_titleStr.equals(CurrentName))
                tmpAlertInfo = tmpAlertInfo + " 姓名: " + CurrentName + "->" + new_titleStr + "\n";
            if (!new_remarkStr.equals(CurrentRemark))
                tmpAlertInfo = tmpAlertInfo + " 事由: " + CurrentRemark + "->" + new_remarkStr + "\n";
            if (!new_dateStr.equals(CurrentDate))
                tmpAlertInfo = tmpAlertInfo + " 事件日期: " + CurrentDate.substring(0, 10) + "->" + new_dateStr.substring(0, 10);

            boolean isOk = ConfirmDialog.showConfirmDialog(this, "重要提示", tmpAlertInfo);
            if (isOk) {  //确认批量修改数据
                if (!new_titleStr.equals(CurrentName)) batchUpdate(1);
                if (!new_remarkStr.equals(CurrentRemark)) batchUpdate(2);
                if (!new_dateStr.equals(CurrentDate)) batchUpdate(3);
                m_result = RESULT_OK ;//批量修改数据成功,传参数出去
            } else {  //取消批量修改数据
                return;
            }
        }
        setResult(m_result, intent);  //  单条记录修改 or 批量修改数据， 传参数出去

        //Close the input soft keyboard
        View view1 = getCurrentFocus();
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view1.getWindowToken(),0);

        finish();

    }

    public void delAccount(View view) {
        //修改页面，可以删除该记录
        AlertDialog.Builder dialog02 = new AlertDialog.Builder(this);
        dialog02.setTitle("提示");
        dialog02.setMessage("确认删除该笔人情记录？");
        dialog02.setPositiveButton("确定", ((DialogInterface dialog, int whichButton)-> {
            SQLiteDatabase db = helper.getReadableDatabase();
            //delete one record
            String sqlString = "_id=" + CurrentId;
            db.delete("account", sqlString, null);
            db.close();
            ShowToast(" 该条记录被成功删除! ", Color.RED);
            //删除记录后, 返回前一页
            Intent intent = getIntent();
            setResult(RESULT_OK, intent);//传参数出去202 ,201,202为修改页面参数
            finish();//                return;
        }));
        dialog02.setNegativeButton("取消", ((DialogInterface dialog, int whichButton)-> {
        //                return;
        }));
        dialog02.show();
    }

}
