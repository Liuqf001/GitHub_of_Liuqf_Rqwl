package com.example.RQWL001;

//防止输入法把按钮顶上去  在 AndroidManifest.xml文件中，            android:windowSoftInputMode="adjustPan|stateHidden"
import me.rosuh.filepicker.bean.FileItemBeanImpl;
import me.rosuh.filepicker.config.AbstractFileFilter;
import me.rosuh.filepicker.config.FilePickerConfig;
import me.rosuh.filepicker.config.FilePickerManager;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import org.jetbrains.annotations.NotNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;

import android.view.ViewOutlineProvider;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

//import com.leon.lfilepickerlibrary.utils.Constant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private static final String VersionString = " Version: 2025.03.27 ";
    int CurrentIndex = -1;
    int m_total_Num = 0;  //显示总收支差额
    //    int m_appStart_reFlag = 1 ; //app start flag: 1 ;   0:started
    int m_total_reFlag = -2;//returnflag 按钮次数  初始化为-2，用来启动时可导入备份数据，导入后 修改为-1，
    int m_Thing_Char = 5;//事由的字符数， 2,5
    int m_isblueorblack = 0; //0,blue;1,grey,2:black
    private AutoCompleteTextView completeSearchView;  //    private AutoCompleteTextView completeText;
    private SearchView simpleSearchView;
    private ImageView sipSearchImgView;
    ArrayAdapter<String> adapter;
    private long lastClickTime = 0;
    private static final long DOUBLE_CLICK_DELAY = 300;  //300ms为双击时间间隔

    String isAscorDesc = " asc" ;//
    String CsvTabChar = "\t";  // Tab 键分割 csv文件  ,也可以用 分隔符 ","
    String CurrentName = "";   //当前姓名
    String CurrentRemark = ""; //当前事由
    int CurrentId = -1;
    int CurWorkmodeFlag = 1;    //1: 按联系人显示    2：按事由事件显示  3:查询框search模式
    int CurPageNoFlag = 1;    //1:APP首页    2：App第二页
    String datainOut = "收礼";
    String sortbyStr = "Title,Date,Remark";
    private DBHelper helper;
    private ListView listView;  //
    ImageButton Add;
    ImageButton BtnThings;  //事由
    ImageButton BtnContacts;//联系人
    private ImageButton Retflag;
    private ImageButton Expdata;
    private EditText Mtotal;
    private EditText Men_total;
    private List<CostList> list;
    ActivityResultLauncher<Intent> launcherAdd;
    ActivityResultLauncher<Intent> launcherEdit;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViews();
        gestureDetector = new GestureDetector(this, new GestureListener());  //2024.06.22


        //让键盘的回车键设置成搜索
        simpleSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        // 设置搜索文本监听
        simpleSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // 当点击搜索按钮时触发该方法
            @Override
            public boolean onQueryTextSubmit(String query) {
                m_total_reFlag = 0;
                initQueryData(query);
                return false;
            }

            // 当搜索内容改变时触发该方法
            @Override
            public boolean onQueryTextChange(String query) {
                initQueryData(query);   //                query = query;
                return false;
            }
        });

        initView();

        if (PermissionUtils.isGrantExternalRW(MainActivity.this, 1)) {
            initData(CurrentName);  //初始化主界面，显示记录
        }

        launcherAdd =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            int resultCode = result.getResultCode();
                            if (resultCode==RESULT_OK) {
                                Intent data = result.getData();
                                if (data != null) {
                                    CurrentName =
                                            data.getStringExtra("CurrentName"); // data  return from  son's View(editAccount , 修改)
                                    CurrentRemark =
                                            data.getStringExtra("CurrentRemark"); // data  return from  son's View(editAccount , 修改)
                                    datainOut = result
                                            .getData().getStringExtra("datainOut"); // data  return from  son's View(editAccount , 修改)
                                }
                            }
                            CurPageNoFlag = 2;
                            m_total_reFlag = -1;
                            // 增加新的记录数据 ，备份数据
                            CurWorkmodeFlag = 1; // 按照联系人显示模式，显示结果 ，在第二页显示
                            initData(CurrentName); // 修改/添加/修改记录后 重新显示
                            if ( resultCode==RESULT_OK ) // 添加数据记录成功
                                ExportCsv("FinalAccountAdd", true); // 增加新的记录数据后，自动导出一次记录 to  FinalAccountAdd
                        });

        launcherEdit =
                registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            int resultCode = result.getResultCode();
                            if (resultCode==RESULT_OK) {
                                Intent data = result.getData();
                                if (data != null) {
                                    CurrentName =
                                            data.getStringExtra("CurrentName"); // data  return from  son's View(editAccount , 修改)
                                    CurrentRemark =
                                            data.getStringExtra("CurrentRemark"); // data  return from  son's View(editAccount , 修改)
                                    datainOut = result
                                            .getData().getStringExtra("datainOut"); // data  return from  son's View(editAccount , 修改)

                                }
                            }
                            CurPageNoFlag = 2;
                            m_total_reFlag = -1;
                            // 删除指定行记录数据 ，备份数据
                            initData(CurrentName); // 修改/添加/修改记录后 重新显示
                            if ( resultCode==RESULT_OK ) // 修改数据记录成功  1，2，202
                                ExportCsv("FinalAccountDel", true); // 增加新的记录数据后，自动导出一次记录 to  FinalAccountAdd
                        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(diffX) > Math.abs(diffY)
                    && Math.abs(diffX) > SWIPE_THRESHOLD
                    && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffX > 0) {
                    // 左滑
                    onBackPressed();
                }
            }

            return true;
        }
    }

    @Override
    public void onBackPressed() {
        // 在用户按下返回键时执行的代码
/*
//        Toast.makeText(this, "返回键被点击了！", Toast.LENGTH_SHORT).show();
//        super.onBackPressed();
*/
        retOnFlag(this.Retflag);
    }

    private void removeUnderLine() {
        simpleSearchView = findViewById(R.id.et_search_title);
        simpleSearchView.setSubmitButtonEnabled(true);  //控件上添加默认的提交按钮
        sipSearchImgView = findViewById(R.id.et_search_image);
        //Removing searchview underline
        int searchPlateId = simpleSearchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlate = simpleSearchView.findViewById(searchPlateId);
        if (searchPlate != null) {
            searchPlate.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void ShowToast(String strToShow, int backColor) {
        Toast toast = Toast.makeText(MainActivity.this, strToShow, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);  //提示显示时间短
        //         toast.getView().setBackgroundColor(R.color.colorPrimaryDark);  //0xff0fFFFF);
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

    public void OntitleCLick(View v) {
        if(isAscorDesc.equals(" asc"))  isAscorDesc = " desc" ;
        else    isAscorDesc = " asc" ;
        int i = v.getId();  //点击表头，排序
        if (i == R.id.tv_title3) {
            sortbyStr = "Title";
        } else if (i == R.id.tv_remark3) {
            sortbyStr = "Remark";
            if (CurPageNoFlag == 1) {  //first page     //                if (m_Thing_Char == 5) m_Thing_Char = 2;
                if (CurWorkmodeFlag == 1) m_Thing_Char = 5;  //person
                else
                    m_Thing_Char = 2;                       // thing   //                else m_Thng_Char = 5; //2.4.6 ;
            }
            findViews();  //重新组织提示信息
        } else if (i == R.id.tv_date3) {
            sortbyStr = "Date";
        } else if (i == R.id.tv_money3) {
            sortbyStr = "Money";
        } else if (i == R.id.et_search_image) {
            int id = 0;
            m_isblueorblack = (m_isblueorblack + 1) % 3;
            switch (m_isblueorblack) {//更改查询框线条颜色
                case 0:
                    id = getResources().getIdentifier("groundblue", "drawable", "com.example.RQWL001");
                    break;
                case 1:
                    id = getResources().getIdentifier("groundgrey", "drawable", "com.example.RQWL001");
                    break;
                case 2:
                    id = getResources().getIdentifier("groundblack", "drawable", "com.example.RQWL001");
                    break;
            }
            Drawable myDrawable = ResourcesCompat.getDrawable(getResources(), id, null); //getResources().getDrawable(id, null);
            sipSearchImgView.setImageDrawable(myDrawable);
        }
        sortbyStr = sortbyStr.replace(",,", ",");
        String queryStr = completeSearchView.getText().toString();
//        if ("".equals(queryStr))
        if (sortbyStr.equals("Money")) sortbyStr = sortbyStr + "*1" ;  //, ,用来保证数字排序正确
        sortbyStr = sortbyStr +  isAscorDesc;
        if (CurWorkmodeFlag <= 2)
            initSortData(sortbyStr);  //按工作模式排序后，显示记录
        else
            initQueryData(queryStr);  //按輸入條件字符，查询后排序后，显示记录

    }

    private void initView() {
        //显示界面
        helper = new DBHelper(MainActivity.this);
        listView = findViewById(R.id.list_view);
        Add = findViewById(R.id.add);
        Retflag = findViewById(R.id.retFlag);
        Expdata = findViewById(R.id.expdata);
        Mtotal = findViewById(R.id.mtotal);
        Men_total = findViewById(R.id.mtotal_men);
        BtnThings = findViewById(R.id.things);
        BtnContacts = findViewById(R.id.contacts);
//        return;
    }

    private void SetItemClickandLongClick() {
        listView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
//                position = position;
            //单击某一行
            CurrentIndex = position;
            if ((parent).getTag() != null) {
                ((View) (parent).getTag()).setBackgroundColor(Color.TRANSPARENT);
            }
            (parent).setTag(view);   //                    view.setBackgroundColor(Color.RED);
//                String tmpid = list.get(CurrentIndex).get_id();   //_id
//                if ("-1".equals(tmpid))  //"-1"=_id  //page1,-->显示此人,或者此事件 的所有记录
            if (CurWorkmodeFlag == 3) CurPageNoFlag = 2;     //查询模式  设置为第二页
            if (CurPageNoFlag == 1)  //page1,  //page1,-->显示此人,或者此事件 的所有记录
            {
                CurPageNoFlag = 2;   //go to page2,
                CurrentIndex = position;
                CurrentName = list.get(CurrentIndex).getTitle();     //联系人
                CurrentRemark = list.get(CurrentIndex).getRemark();  //事由
                initData(CurrentName);  //单击选中一行 ，显示记录
                m_total_reFlag = 0;
                m_total_reFlag--;
            } else {  ////page2,   修改此选中记录
                editAccount(view);
            }

//            Retflag.setVisibility(View.VISIBLE);  //2025.03.23
//                Del.setVisibility(View.VISIBLE);
            Expdata.setVisibility(View.VISIBLE);
            if (CurrentName.equals("")) {
                Expdata.setVisibility(View.VISIBLE);
            }
        });

        listView.setOnItemLongClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            m_total_reFlag = 0;
            CurrentIndex = position;
            String tmpid = list.get(CurrentIndex).get_id();   //_id
            if ((parent).getTag() != null) {
                ((View) (parent).getTag()).setBackgroundColor(Color.TRANSPARENT);
            }
            (parent).setTag(view);
            view.setBackgroundColor(Color.RED);
            if (!"-1".equals(tmpid))  //"-1"=_id  单条记录，才可以删除
            {
                delAccount(view);  // delete one person or  one record
            }
            return true;
        });
    }

    private void InsertTongjiData(SQLiteDatabase db, CostList mlists) {
        ContentValues values = new ContentValues();
        values.put("Title", mlists.getTitle());
        values.put("Remark", mlists.getRemark());
        values.put("Date", mlists.getDate());
        values.put("Money", Integer.parseInt(mlists.getMoney()));  //        values.put("Money",mlists.getMoney());
        values.put("InOut", mlists.getInOut());
        long account;
        if (CurWorkmodeFlag == 1)
            account = db.insert("accountPerson", null, values); //联系人模式
        else  // 事件模式
            account = db.insert("accountThing", null, values);
        Log.v("InsertTongjiData", Long.toString(account));
    }

    private void initData(String selectArgs) {
//        ImportCsv();//  check db is not empty？
        CurrentIndex = -1;
        list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sqlwhere = "select * from account "; //Title ;
        Cursor cursor;
        CurrentName = selectArgs;
        if(CurrentRemark==null) CurrentRemark = ""; //initial
        db.delete("accountThing", null, null);   //每次重新初始化统计数据库表
        db.delete("accountPerson", null, null); //每次重新初始化统计数据库表
        if (CurPageNoFlag == 1)    // first page 查询
        {
            if (CurWorkmodeFlag == 1) {   //联系人模式 ;
                sqlwhere = sqlwhere + "where Title like ? order by " + "Title,Date,Remark"; //联系人模式 ; /首页 //查询全表
                cursor = db.rawQuery(sqlwhere, new String[]{"%" + CurrentName + "%"});
            } else if (CurWorkmodeFlag == 2) {  // CurWorkmodeFlag ==2               //事件模式 ;  /首页
                sqlwhere = sqlwhere + "where Remark like ? order by " + "Remark,Title,Date"; //事件模式 ;  //查询全表
                cursor = db.rawQuery(sqlwhere, new String[]{"%" + CurrentRemark + "%"});
            } else {
                cursor = db.rawQuery(sqlwhere, null);
            }
        } else //if(CurPageNoFlag==2)    // second page   CurPageNoFlag==2
        {
            if (sortbyStr.equals("Money")) sortbyStr = sortbyStr + "*1";  //, ,用来保证数字排序正确
            if (CurWorkmodeFlag == 1) {
                sqlwhere = sqlwhere + "where Title like ? order by " + sortbyStr;    //查该姓名的所有记录
                cursor = db.rawQuery(sqlwhere, new String[]{CurrentName + "%"});
            } else if (CurWorkmodeFlag == 2) {  //(CurWorkmodeFlag==2)
                sqlwhere = sqlwhere + "where Remark like ? order by " + sortbyStr;    //查询单一事件所有记录 ;
                cursor = db.rawQuery(sqlwhere, new String[]{CurrentRemark + "%"});
            } else {
                cursor = db.rawQuery(sqlwhere, null);
            }
        }
        m_total_Num = 0;
        int m_Name_Num = 0;  //单个人，或者单件事  的金额汇总
        int m_record_Num = 0;  //记录条目数，单个人，或者单件事
        int m_record_Count = 0; //本次查询总条目，记录条目数
        int m_Tongji_Count = 0; //统计表，记录条目数
        String tmpNameRemark = "";
        String newNameRemark;
        while (cursor.moveToNext()) {//数据库表记录遍历
            CostList clist = new CostList();//构造实例
            if (CurWorkmodeFlag == 1)
                newNameRemark = cursor.getString(cursor.getColumnIndex("Title")); //联系人模式 ;
            else {
                newNameRemark = cursor.getString(cursor.getColumnIndex("Remark")); //事件模式 ;
                if (newNameRemark.length() > m_Thing_Char) {
                    newNameRemark = newNameRemark.substring(0, m_Thing_Char);  //事件只取2、5个汉字
                }
            }
            if (CurPageNoFlag == 1) //第一页
            {
//                if(tmpNameRemark.equals(""))  tmpNameRemark = newNameRemark ;   // 名字为空的，不处理
                if (!tmpNameRemark.equals(newNameRemark)) {
                    clist.set_id("-1");     //  新的联系人或者 事件时， _id为负数
                    clist.setTitle("");
                    clist.setDate("");
                    clist.setInOut("");
                    if (CurWorkmodeFlag == 1)
                        clist.setTitle(tmpNameRemark);  //联系人模式
                    else if (CurWorkmodeFlag == 2) {
                        clist.setRemark(tmpNameRemark);
                    }
                    if (m_Name_Num > 0)
                        clist.setMoney("+" + m_Name_Num);
                    else
                        clist.setMoney(Integer.toString(m_Name_Num));
                    if (m_record_Num > 0)  //数据库确实有记录，才统计显示一个人名，或者一件事
                    {
                        InsertTongjiData(db, clist);
                        list.add(clist);
                        m_Tongji_Count++;
                    }
                    tmpNameRemark = newNameRemark;  //新人名 或者  新的事件
                    m_Name_Num = 0;
                    m_record_Num = 0;
                }
            } else  //第二页
            {
                clist.set_id(cursor.getString(cursor.getColumnIndex("_id")));
                clist.setTitle(cursor.getString(cursor.getColumnIndex("Title")));
                clist.setRemark(cursor.getString(cursor.getColumnIndex("Remark")));
                clist.setDate(cursor.getString(cursor.getColumnIndex("Date")));
                clist.setMoney(cursor.getString(cursor.getColumnIndex("Money")));
                clist.setInOut(cursor.getString(cursor.getColumnIndex("InOut")));
                clist.setMemo(cursor.getString(cursor.getColumnIndex("Memo")));
                list.add(clist);
/*
                if (CurWorkmodeFlag == 1) //第二页 ，查询单个人的全部记录
                {
                } else //if (CurWorkmodeFlag == 2)  //第二页 ，查询单个事件，所有记录
                {
                }
                */
            }
            //统计 金额 汇总  ， 总的收支，或者单个事件， 或者单个人汇总
            if ((cursor.getString(cursor.getColumnIndex("InOut")).equals("收礼"))) {
                m_total_Num = m_total_Num + cursor.getInt(cursor.getColumnIndex("Money"));
                m_Name_Num = m_Name_Num + cursor.getInt(cursor.getColumnIndex("Money"));
            } else {
                m_total_Num = m_total_Num - cursor.getInt(cursor.getColumnIndex("Money"));
                m_Name_Num = m_Name_Num - cursor.getInt(cursor.getColumnIndex("Money"));
            }
            m_record_Num++;  //单件事 或者 单个人 记录条目数
            m_record_Count++;  //本次查询总条目，记录条目数
        }

        if (CurPageNoFlag == 1) //人名or事件统计是有效   Page one
        {  //最后一个
            CostList clist = new CostList();//构造实例
            clist.setTitle("");
            clist.set_id("-1");
            clist.setDate("");
            clist.setInOut("");
            if (CurWorkmodeFlag == 1) clist.setTitle(tmpNameRemark);  //联系人
            else clist.setRemark(tmpNameRemark); //事件
            clist.setMoney(Integer.toString(m_Name_Num));
            if (m_Name_Num > 0) clist.setMoney("+" + m_Name_Num);
            if (m_record_Num > 0) {
                InsertTongjiData(db, clist);
                list.add(clist);  //数据库确实有记录，才统计显示一个人名，或者一件事
                m_Tongji_Count++;
            }
        }
        String charShow = "" + m_Tongji_Count + "行(" + m_record_Count + "笔)";   //笔";
        if(m_Tongji_Count == 0)
            charShow = ""  + m_record_Count + "笔";   //笔";; //人名or事件统计是有效   Page two
        Men_total.setText(charShow); //显示总条目数量
        if (m_total_Num >= 0) {
            if (m_total_Num == 0) {
                Mtotal.setText(""); //显示总收支差额
            } else {
                charShow = "+" + m_total_Num;
                Mtotal.setText(charShow); //显示总收支差额
//            Mtotal.setTextColor(Color.RED);
            }
        } else {
            charShow = "" + m_total_Num;
            Mtotal.setText(charShow); //显示总收支差额
//            Mtotal.setTextColor(Color.GREEN);
        }
//        Mtotal.setVisibility(View.VISIBLE);

        //绑定适配器
        listView.setAdapter(new ListAdapter(this, list));
        cursor.close();
        db.close();
        SetItemClickandLongClick();

//        SQLiteToExcel sqliteToExcel = new SQLiteToExcel(this, "account_daily.db");
    }

    private void initSortData(String sortbyStr) {
        CurrentIndex = -1;
        list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sqlwhere = "select * from account "; //Title
        Cursor cursor;
//        CurrentName = selectArgs;

        if (CurPageNoFlag == 1)    // first page 查询
        {
            if (CurWorkmodeFlag == 1) {                     //姓名模式 ;  /首页
                sqlwhere = "select * from accountPerson order by " + sortbyStr;                //                cursor = db.rawQuery(sqlwhere, null);
            } else if (CurWorkmodeFlag == 2) {  // CurWorkmodeFlag ==2               //事件模式 ;
                sqlwhere = "select * from accountThing order by " + sortbyStr;/**/
            } else {// CurWorkmodeFlag ==3         //搜索模式 ;//搜索模式 ;
                sqlwhere = "select * from account order by " + sortbyStr;/**/
            }
            cursor = db.rawQuery(sqlwhere, null);
        } else //if(CurPageNoFlag==2)    // second page   CurPageNoFlag==2
        {
            if (CurWorkmodeFlag == 1) {  // CurWorkmodeFlag ==1
                sqlwhere = sqlwhere + "where Title like ? order by " + sortbyStr;    //查该姓名的所有记录 , *1 ,用来保证数字排序正确
                cursor = db.rawQuery(sqlwhere, new String[]{CurrentName + "%"});
            } else if (CurWorkmodeFlag == 2) {  // CurWorkmodeFlag ==2               //事件模式 ;
                sqlwhere = sqlwhere + "where Remark like ? order by " + sortbyStr;    //查询单一事件所有记录 ;
                cursor = db.rawQuery(sqlwhere, new String[]{CurrentRemark + "%"});
            } else {// CurWorkmodeFlag ==3         //搜索模式 ;
                sqlwhere = sqlwhere + "order by " + sortbyStr;    //查询所有记录 ;
                cursor = db.rawQuery(sqlwhere, null);
            }

        }
        int m_Tmp_Num;
        String m_Tmp_char;
        int m_record_Count = 0; //本次查询总条目，记录条目数
        while (cursor.moveToNext()) {//数据库表记录遍历
            CostList clist = new CostList();//构造实例
            clist.set_id(cursor.getString(cursor.getColumnIndex("_id")));
            clist.setTitle(cursor.getString(cursor.getColumnIndex("Title")));
            clist.setRemark(cursor.getString(cursor.getColumnIndex("Remark")));
            clist.setDate(cursor.getString(cursor.getColumnIndex("Date")));
            m_Tmp_Num = cursor.getInt(cursor.getColumnIndex("Money"));
            m_Tmp_char = cursor.getString(cursor.getColumnIndex("Money"));
            if ((m_Tmp_Num > 0)&&(CurPageNoFlag == 1)) //第一页 有加号， 第二页没有
                clist.setMoney("+" + m_Tmp_Num);
            else
                clist.setMoney(m_Tmp_char);
            if (CurWorkmodeFlag >= 3)        //搜索模式 ;//搜索单个人或者单个事件的记录 ;
                clist.setMoney(m_Tmp_char);
            clist.setInOut(cursor.getString(cursor.getColumnIndex("InOut")));
            list.add(clist);
            m_record_Count++;  //本次查询总条目，记录条目数
        }

        String charShow = "共" + m_record_Count + "条";
        Men_total.setText(charShow); //显示总条目数量
        //绑定适配器
        listView.setAdapter(new ListAdapter(this, list));
        cursor.close();
        db.close();

        //        SQLiteToExcel sqliteToExcel = new SQLiteToExcel(this, "account_daily.db");
    }

    private void initQueryData(String selectArgName) {
        CurWorkmodeFlag = 3;  // CurWorkmodeFlag ==3         //搜索模式 ;
        CurrentIndex = -1;
        list = new ArrayList<>();
        SQLiteDatabase db = helper.getReadableDatabase();
        String sqlwhere = "select * from account "; //Title
        Cursor cursor;

        //        if (CurPageNoFlag == 1) //first page  一次查询，查询 姓名,事件中包含改字符串的所有记录
        //        {
        sqlwhere = sqlwhere + "where Title like ? or Remark like ? order by " + sortbyStr;
        cursor = db.rawQuery(sqlwhere, new String[]{"%" + selectArgName + "%", "%" + selectArgName + "%"});
        /*
        //                }
        //                else
        //                { //second page  二次查询，精确查询 姓名,事件都匹配各自字符串的所有记录
        //                    sqlwhere = sqlwhere + "where Title like ? and Remark like ? order by 1*" + sortbyStr;    //二次查询
        //                    cursor = db.rawQuery(sqlwhere, new String[]{"%" + CurrentName + "%", "%" + CurrentRemark + "%"});
        //                }
        */

        m_total_Num = 0;
        int m_Name_Num = 0;  //单个人，或者单件事  的金额汇总
        int m_record_Num = 0;  //记录条目数，单个人，或者单件事
//        String tmpNameRemark = ""; //        String newNameRemark = "";
        int m_Tmp_Num;
        String m_Tmp_char;
        while (cursor.moveToNext()) {//数据库表记录遍历
            CostList clist = new CostList();//构造实例
            clist.set_id(cursor.getString(cursor.getColumnIndex("_id")));
            clist.setTitle(cursor.getString(cursor.getColumnIndex("Title")));
            clist.setRemark(cursor.getString(cursor.getColumnIndex("Remark")));
            clist.setDate(cursor.getString(cursor.getColumnIndex("Date")));
            m_Tmp_Num = cursor.getInt(cursor.getColumnIndex("Money"));
            m_Tmp_char = cursor.getString(cursor.getColumnIndex("Money"));
            if (m_Tmp_Num > 0)
                clist.setMoney("" + m_Tmp_Num);
            else
                clist.setMoney(m_Tmp_char); //            clist.setMoney(cursor.getString(cursor.getColumnIndex("Money")));
            clist.setInOut(cursor.getString(cursor.getColumnIndex("InOut")));
            list.add(clist);

            //统计 金额 汇总  ， 总的收支，或者单个事件， 或者单个人汇总
            if ((cursor.getString(cursor.getColumnIndex("InOut")).equals("收礼"))) {
                m_total_Num = m_total_Num + cursor.getInt(cursor.getColumnIndex("Money"));
                m_Name_Num = m_Name_Num + cursor.getInt(cursor.getColumnIndex("Money"));
            } else {
                m_total_Num = m_total_Num - cursor.getInt(cursor.getColumnIndex("Money"));
                m_Name_Num = m_Name_Num - cursor.getInt(cursor.getColumnIndex("Money"));
            }
            m_record_Num++;  //单件事 或者 单个人 记录条目数
        }
        String charShow = "共" + m_record_Num + "笔";
        Men_total.setText(charShow); //显示总条目数量
        if (m_total_Num >= 0) {
            if (m_total_Num == 0) {
                Mtotal.setText(""); //显示总收支差额
            } else {
                charShow = "+" + m_total_Num;
                Mtotal.setText(charShow); //显示总收支差额
            }
        } else {
            charShow = "" + m_total_Num;
            Mtotal.setText(charShow); //显示总收支差额
        }
        //绑定适配器
        listView.setAdapter(new ListAdapter(this, list));
        cursor.close();
        db.close();

    }

    public void selectBackupFile_ToRecovery(View view) {//首先清空数据库记录，然后导入文件数据至数据库
//     //   ComponentActivity activity = ...; // 获取当前Activity实例
//        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
//                new ActivityResultContracts.StartActivityForResult(),
//                new ActivityResultCallback<Intent>() {
//                    @Override
//                    public void onActivityResult(Intent result) {
//                        if (result != null) {
//                            String returnedData = result.getStringExtra("data_return");
//                            // 处理返回的数据
//                        }
//                    }
//                }
//        );
//        launcher.launch(new Intent(MainActivity.this, Edit_cost.class));
//        return ;

        //Android FilePicker
        FilePickerConfig filePickerConfig = FilePickerManager.from(this);
        filePickerConfig.enableSingleChoice() ;   //                        .filter(new AbstractFileFilter().)
        filePickerConfig.showHiddenFiles(false);
        AbstractFileFilter aFilter = new AbstractFileFilter() {
            @NotNull
            @Override
            public ArrayList<FileItemBeanImpl> doFilter(@NotNull final ArrayList<FileItemBeanImpl> arrayList) {
                ArrayList<FileItemBeanImpl> fileItemBeans = new ArrayList<>();
                for (FileItemBeanImpl fileItemBean : arrayList){
                    if(!fileItemBean.isDir())  //只 显示文件，不显示文件夹
                    {
                        String str = fileItemBean.getFileName();
                        int length = str.length();
                        if (length >= 4) {
                            String lastFourChars = str.substring(length - 4, length);
                            System.out.println(lastFourChars);
                            if (lastFourChars.toLowerCase(Locale.ROOT).equals(".csv"))   // only display .csv files
                                fileItemBeans.add(fileItemBean);
                        } else {
                            System.out.println("字符串长度小于4");
                        }
                    }
                }
                return fileItemBeans;
            }
        };
        filePickerConfig.filter(aFilter) ;
        filePickerConfig.forResult(8899);

        //LFilePicker
//        LFilePicker lFilePicker = new LFilePicker();
//        lFilePicker.withActivity(MainActivity.this)
//                .withTitle("请选择一个备份数据文件")
//                .withFileFilter(new String[]{".csv"})
//                .withChooseMode(true)
//                .withMutilyMode(false)
//                .withStartPath("/storage/emulated/0")//指定初始显示路径
//                .withRequestCode(9999);
//        lFilePicker.start();

//                Intent intent = new Intent(Intent.ACTION_VIEW);
//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.addCategory(Intent.CATEGORY_DEFAULT);
//                intent.setType("text/*");
//                startActivityForResult(intent, 999);

        //ACTION_GET_CONTENT
//        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        String sdCardDir = getSDPath(MainActivity.this);
//        Uri uri = Uri.parse(sdCardDir); // 替换为具体的路径
//        intent.setDataAndType(uri, "text/*"); // 设置数据类型为所有类型
//                startActivityForResult(intent, 999); //                startActivity(intent);//

//                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//                intent.setType("text/*");//                intent.setType("application/vnd.android.package-archive");
//                Intent chooser = Intent.createChooser(intent, "选择一个数据文件");
//                startActivityForResult(chooser, 999);

    }

    public void importExportDataFromCsv(View view) {//首先清空数据库记录，然后导入文件数据至数据库
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastClickTime) < DOUBLE_CLICK_DELAY)//双击事件，导入数据
        {//双击事件
            if (m_total_reFlag == -2) //app start , check table data  is empty?
            {
                selectBackupFile_ToRecovery(view);
            } else  //双击事件，导出数据
                expData();   //  导出数据，备份数据
        }
        //单击此按钮
        lastClickTime = currentTime;
        //        return;
    }


    public void expData() {//导出文件
        int lineNum = listView.getCount();
        if (lineNum <= 0) {
            ShowToast(" 没有数据需要备份！", Color.RED);
//            Toast.makeText(MainActivity.this, "没有数据需要备份！", Toast.LENGTH_SHORT).show();
//            return;
        } else {   //导出提醒
            Date now = new Date();
            // 创建一个SimpleDateFormat对象，指定日期格式
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            // 格式化日期
            String formattedDate = dateFormat.format(now);

            String bakFilename = "FinalAccount" + formattedDate;
            AlertDialog.Builder dialog02 = new AlertDialog.Builder(this);
            dialog02.setTitle("数据备份提示");
            dialog02.setMessage("您确认导出数据到" + bakFilename + "文件？");//（以前的历史备份将被覆盖）？");
            dialog02.setPositiveButton("确定", ((DialogInterface dialog, int whichButton) ->
                    ExportCsv(bakFilename,false)));
            dialog02.setNegativeButton("取消", ((DialogInterface dialog, int whichButton) -> {
//                return;
            }));
            dialog02.show();
//            return;
        }
    }

    public void editAccount(View view) {// edit record
        Intent intent = new Intent(MainActivity.this, Edit_cost.class);
        CurrentId = Integer.parseInt(list.get(CurrentIndex).get_id());
        if ((CurrentIndex >= 0) & (CurrentId >= 0)) {         //有选中， 修改记录  第一页界面不具备修改功能
            CurrentName = list.get(CurrentIndex).getTitle();
            intent.putExtra("CurrentId", CurrentId);
            intent.putExtra("CurrentName", CurrentName);
            intent.putExtra("CurrentRemark", list.get(CurrentIndex).getRemark());
            intent.putExtra("CurrentDate", list.get(CurrentIndex).getDate());
            intent.putExtra("CurrentMoney", list.get(CurrentIndex).getMoney());
            intent.putExtra("datainOut", list.get(CurrentIndex).getInOut());
            intent.putExtra("CurrentMemo", list.get(CurrentIndex).getMemo());

            if(launcherEdit!=null) {
                launcherEdit.launch(intent);
            }
//            startActivityForResult(intent, 888);
//            Log.v("ok", "editAccount return");
        }
    }

    //事件：添加
    public void addAccount(View view) {//跳转
        Intent intent = new Intent(MainActivity.this, New_cost.class);
        intent.putExtra("CurrentName", CurrentName);
        intent.putExtra("datainOut", datainOut);

        if(launcherAdd!=null)
            launcherAdd.launch(intent);

//        startActivityForResult(intent, 777);
//        Log.v("ok", "addAccount return");
    }

    public void delAccount(View view) {//跳转
        //首页，全表没有选中记录， 则返回；  单个联系人页面，则可以全部删除记录
        if ((CurrentIndex < 0) & (CurrentName.equals(""))) {
            ShowToast("请选择要删除的人情记录！", Color.GREEN);
//            Toast.makeText(MainActivity.this, "请选择要删除的人情记录！", Toast.LENGTH_SHORT).show();
            return;
        }

//        m_total_reFlag = 0;  //returnflag =0
        AlertDialog.Builder dialog02 = new AlertDialog.Builder(this);
        if (CurrentIndex < 0)    //没有选中记录  全部删除？
        {
            dialog02.setTitle("重要提示");
            if (CurrentName.equals("")) {
                dialog02.setMessage("您的所有人情记录都将被删除，确认删除？");
            } else
                dialog02.setMessage(CurrentName + " 的所有人情记录都将被删除，确认删除？");
        } else {   //删除一条记录
            dialog02.setTitle("提示");
            dialog02.setMessage("确认删除该笔人情记录？");
        }
        dialog02.setPositiveButton("确定", ((DialogInterface dialog, int whichButton) -> {
            {
//                ExportCsv("FinalAccountDel");  //删除前，自动导出一次记录 to  FinalAccountDel
                SQLiteDatabase db = helper.getReadableDatabase();
 /*               //if (CurrentIndex < 0)    //没有选中记录  全部删除？
                 {
                    if(CurrentName.equals("")) {
                        db.delete("account", null,null );
                        db.execSQL("truncate table account");  //delete all
                    }
                    else
                    {//delete one person's all records
                        db.delete("account", "Title like ?", new String[]{"%"+CurrentName+"%"});
                    }
                }*/
                //else//delete one record
                {
                    String sqlwhere = "_id=" + list.get(CurrentIndex).get_id();
                    db.delete("account", sqlwhere, null);
                    ShowToast(" 该条记录被成功删除! ", Color.RED);
                }
                db.close();
//                ExportCsv("FinalAccount");  //删除记录后，自动导出一次记录  2024.06.12
                initData(CurrentName);  //删除后重新显示记录
                CurrentIndex = -1;
//                return;
            }
        }));
        dialog02.setNegativeButton("取消", ((DialogInterface dialog, int whichButton) -> {
//            return;
        }));
        dialog02.show();
    }

    private void findViews() {
        List<String> curName;   //光标提示字符串
        curName = new ArrayList<>();
        if (helper == null) helper = new DBHelper(MainActivity.this);
        SQLiteDatabase db = helper.getReadableDatabase();
        String sqlwhere = "select distinct Title from account "; //Title
        Cursor cursor;
        cursor = db.rawQuery(sqlwhere, null);
        while (cursor.moveToNext()) {   // find all name in database
            curName.add(cursor.getString(cursor.getColumnIndex("Title")));
        }
        cursor.close();
//        db.close();//        db=helper.getReadableDatabase();
        sqlwhere = "select distinct Remark from account "; //Remark
        cursor = db.rawQuery(sqlwhere, null);
        String newNameRemark;
        while (cursor.moveToNext()) {   // find all name in database
            newNameRemark = cursor.getString(cursor.getColumnIndex("Remark"));
            if (newNameRemark.length() > m_Thing_Char)
                newNameRemark = newNameRemark.substring(0, m_Thing_Char);  //事件只取2、5个汉字
            if (!curName.contains(newNameRemark))  //没有这个字符串，加上
                curName.add(newNameRemark);

        }
        cursor.close();
        db.close();
        helper.close();

        simpleSearchView = findViewById(R.id.et_search_title);
        int completeTextId = simpleSearchView.getResources().getIdentifier("android:id/search_src_text", null, null);
        completeSearchView = simpleSearchView.findViewById(completeTextId);
        completeSearchView.setThreshold(1);  // one char 开始提示
        adapter = new ArrayAdapter<>(this, R.layout.auto_list_item, curName);
        completeSearchView.setAdapter(adapter);
        completeSearchView.setOnItemClickListener((parent, view, position, id) -> {
                    String tmpSelectedStr = parent.getItemAtPosition(position).toString();
                    simpleSearchView.setQuery(tmpSelectedStr, true);
                }
        );
        removeUnderLine();
    }

    public void onThings(View view) {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastClickTime) < DOUBLE_CLICK_DELAY)//双击事件，导入数据
            ShowToast(VersionString, Color.GREEN);
        lastClickTime = currentTime;
        m_total_reFlag = 0;
/*
        CurWorkmodeFlag = 1;    //1: 按联系人显示    2：按事由事件显示
        CurPageNoFlag = 1;    // first page
        m_total_reFlag = 0;
        CurrentName = "";
        CurrentRemark = "";
        simpleSearchView.setQueryHint("输入姓名或事由");
        if (view == null)//后台调用
            CurrentName = completeSearchView.getText().toString();
        initData(CurrentName);  //联系人模式，第一页 ，显示记录
        */
//        return;
    }

    public void onContacts(View view) {
        if (CurWorkmodeFlag == 2) CurWorkmodeFlag = 1;   //1: 按联系人显示    2：按事由事件显示  , 模式切换
        else CurWorkmodeFlag = 2;

        m_Thing_Char = 2;
        CurPageNoFlag = 1;    // first page
        m_total_reFlag = 0;
        CurrentName = "";
        CurrentRemark = "";
        simpleSearchView.setQueryHint("输入姓名或事由");
        if (view == null) {
            if (CurWorkmodeFlag == 2)
                CurrentRemark = completeSearchView.getText().toString();//后台调用
            else CurrentName = completeSearchView.getText().toString();
        }
        initData(CurrentName);   //事由模式，第一页 ，显示记录

    }

    public void retOnFlag(View view) {//跳转
        CurPageNoFlag = 1;  //page1,  return to page1
        if (m_total_reFlag <= -2) m_total_reFlag = 0;  //首次启动软件后直接返回，复位为0
        m_total_reFlag++;
/*
        if (m_total_reFlag == 0)  //第0次return ，备份数据
            m_total_reFlag = 0;//ExportCsv("FinalAccount");   //准备退出之前，自动导出一次记录  2025.03.15
*/
        if (m_total_reFlag == 1) {  //第1次return ，给出提示再按一次即将退出
            ShowToast(" 再次返回将退出本软件！", Color.RED);
               /* Toast toast = Toast.makeText(MainActivity.this, " 再次返回将退出本软件！", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
//              toast.getView().setBackgroundColor(R.color.colorPrimaryDark);  //0xff0fFFFF);
                LinearLayout layout = (LinearLayout) toast.getView();
                layout.setBackgroundColor(0x8f0f0F0F);//0xff0fFF0F   //  Color.GRAY);
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
                toast.show();*/
        }
        if (m_total_reFlag >= 2)  //两次return ，退出程序
        {
            ExportCsv("FinalAccount",true);  //退出前，自动导出一次记录  2024.06.12
            finish();
        }
        if (CurWorkmodeFlag <= 2)     //联系人模式1 or 事件模式2
        {
            OnReturnFlag();
        } else {            //搜索框查询模式
            simpleSearchView.setQuery("", false);  // clear search box
        }
        //        return;
    }

    public void OnReturnFlag() {//跳转  返回按钮
        Expdata.setVisibility(View.VISIBLE);
        if (CurWorkmodeFlag == 1) CurrentName = "";   //联系人模式
        else if (CurWorkmodeFlag == 2) CurrentRemark = "";     //事件模式
        else {  //查询模式
            CurrentName = "";
            CurrentRemark = "";
        }
        //        sortbyStr = "Title,Date,Remark";  //重新查询排序，整理联系人的汇总情况
        initData(CurrentName);               //返回按钮后，重新显示记录
//        initSortData(sortbyStr);  //按工作模式排序后，显示记录  2024.06.18
        CurrentIndex = -1;
//        Retflag.setVisibility(View.VISIBLE);  //2025.03.23
//        return;
    }

    public static String getSDPath(Context context) {
        String sdPath;
        boolean isSDExist = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED); //判断SD卡是否存在
        if (isSDExist) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
            {
                File externalFileRootDir = context.getExternalFilesDir("");
                do {
                    externalFileRootDir = Objects.requireNonNull(externalFileRootDir).getParentFile();
                } while (Objects.requireNonNull(externalFileRootDir).getAbsolutePath().contains("/Android"));
                sdPath = Objects.requireNonNull(externalFileRootDir).getAbsolutePath();
            }
            //else
            {
//                sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            }
        } else {
            sdPath = Environment.getRootDirectory().toString();//获取跟目录
        }
        return sdPath;
    }


    private void ExportCsv(String oldfilename, boolean iKeepSilence) {  //导出全表数据
        String sdDir = getSDPath(MainActivity.this);  //        File sdCardDir = Environment.getExternalStorageDirectory();
        File saveFileOld = new File(sdDir, oldfilename + "001.csv");
        File saveFile = new File(sdDir, oldfilename + ".csv");
        int recordCount = 0;  //总记录条目数

/*        if (!saveFileOld.exists()) {       //判断文件是否存在 //不存在则创建多级目录
//            boolean mkdir =
            saveFileOld.getParentFile().mkdirs();
        }*/
        if (saveFileOld.exists()) {
            if (saveFileOld.delete()) {
                System.out.println(oldfilename + "001.csv文件删除成功");
            } else {
                System.out.println(oldfilename + "001.csv文件删除失败");
            }
        }
        //delete  old file
//        if (saveFile.exists())   // file rename to old  file
//        {
//            try {
//                // 拷贝文件，如果目标文件已存在则替换
//                Path sourcePath = Paths.get(sdDir + "/" + oldfilename + ".csv");
//                Path destinationPath = Paths.get(sdDir + "/" + oldfilename + "001.csv");
//                Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
//                System.out.println("文件拷贝成功！");
//            } catch (IOException e) {
//                e.printStackTrace();
//                System.out.println("文件拷贝失败！");
//            }

//            if (saveFile.renameTo(saveFileOld)) {
//                System.out.println(oldfilename + ".csv文件备份成功");
//            } else {
//                System.out.println(oldfilename + ".csv文件备份失败");
//            }
//        }

        FileWriter fw;
        BufferedWriter bfw;
        try {
            fw = null;
            bfw = null;
            SQLiteDatabase db = helper.getReadableDatabase();
            String sqlwhere = "select * from account order by _id";  //Title,Date"; //Title  ; 2024.06.16
            Cursor cursor;
            cursor = db.rawQuery(sqlwhere, null);
            String oneLineStr;
            while (cursor.moveToNext()) {//数据库表记录遍历
                oneLineStr = cursor.getString(cursor.getColumnIndex("_id")) + CsvTabChar;   // Tab 键分割 csv文件
                oneLineStr = oneLineStr + cursor.getString(cursor.getColumnIndex("Title")) + CsvTabChar;
                oneLineStr = oneLineStr + cursor.getString(cursor.getColumnIndex("Remark")) + CsvTabChar;
                oneLineStr = oneLineStr + cursor.getString(cursor.getColumnIndex("Date")) + CsvTabChar;
                oneLineStr = oneLineStr + cursor.getString(cursor.getColumnIndex("Money")) + CsvTabChar;
                oneLineStr = oneLineStr + cursor.getString(cursor.getColumnIndex("InOut")) + CsvTabChar;
                oneLineStr = oneLineStr + cursor.getString(cursor.getColumnIndex("Memo"));
                if (fw == null) {
                    fw = new FileWriter(saveFile, false);  // 有记录才重写备份数据文件。 覆盖模式写数据
                    bfw = new BufferedWriter(fw);
                }
                bfw.write(oneLineStr);
                bfw.newLine();
                recordCount++;

            }
            cursor.close();
            db.close();

            if (recordCount > 0) {
                bfw.flush();   // 有记录才先删除原记录， 然后重写备份数据文件。
            }
            if (bfw != null) {
                bfw.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (oldfilename.equals("FinalAccount")) ShowToast(" 备份数据失败！", Color.RED);
//            return false;
        }
        if ((recordCount > 0)&&(!iKeepSilence)) ShowToast(" 备份数据成功，共" + recordCount + "条！ ", Color.BLUE);
//        return true;
    }

    private void ImportCsv(String oldFileName) { // import data from csv to database
        SQLiteDatabase db = helper.getReadableDatabase();
        String sqlwhere = "select * from account "; //
        Cursor cursor;

        String sdCardDir = getSDPath(MainActivity.this);
        File saveFile = new File(sdCardDir, oldFileName);
        if (!saveFile.exists())         ////判断文件是否存在,不存在则返回
        {
            ShowToast("请确认文件" + oldFileName + "存在并放置于手机存储根目录下！", Color.RED);
            return;
        }

        if (m_total_reFlag == -2) //app start , check table data  is empty?
        {
            cursor = db.rawQuery(sqlwhere, null);
//            if (!(cursor.moveToNext()))   //数据库中为空时，则从csv文件导入数据到数据库
            {
                AlertDialog.Builder dialog01 = new AlertDialog.Builder(this);
                dialog01.setTitle("数据恢复提示");
                dialog01.setMessage("您确认将备份文件" + oldFileName + "中的数据导入系统（当前所有记录将被覆盖）？");
                //dialog01.setMessage("您的数据记录为空,将为您导入历史数据(FinalAccount.csv)，确认导入？");
                dialog01.setPositiveButton("确定", ((DialogInterface dialog, int whichButton) -> {
                    {
                        SQLiteDatabase dbTmp = helper.getReadableDatabase();
/*//                        String sqldbTmpwheredbTmp = "TRUNCATE TABLE account "; //Title ;
//                        Cursor cursordbTmp;
                        // cursordbTmp = dbTmp.rawQuery(sqldbTmpwheredbTmp, null);
                        */
                        dbTmp.delete("account", null, null);
                        dbTmp.close();
                        ImportOldCsv(oldFileName);
                        CurrentName = "";
                        OnReturnFlag();
                    }
                }));
                dialog01.setNegativeButton("取消", ((DialogInterface dialog, int whichButton) -> {
                    //                return;
                }));
                dialog01.show();
            }
//            sqlwhere = sqlwhere;
            cursor.close();
        }

        db.delete("accountThing", null, null);
        db.delete("accountPerson", null, null);
        db.close();
    }

    private void ImportOldCsv(String oldFileName) {
        int recordCount = 0; //记录条目数
//        File sdCardDir = Environment.getExternalStorageDirectory();
        String sdCardDir = getSDPath(MainActivity.this);
        if (oldFileName.equals("")) oldFileName = "FinalAccount.csv";
        File saveFile = new File(sdCardDir, oldFileName);
        if (!saveFile.exists()) {        ////判断文件是否存在,不存在则返回
            ShowToast("请确认文件" + oldFileName + "存在并放置于手机存储根目录下！", Color.RED);
//            return;
        } else {        //存在文件 , 备份数据文件存在
            FileReader fr;
            BufferedReader bfr;
            SQLiteDatabase db = helper.getWritableDatabase();
            ContentValues values = new ContentValues();
            try {
                fr = new FileReader(saveFile);
                bfr = new BufferedReader(fr);
                String line;
                String initMemo = "";
                long account ;
                while ((line = (bfr).readLine()) != null) {
                    String[] datas = line.split(CsvTabChar);   // Tab 键分割 csv文件   //                    String[] datas = line.split(",");
                    if (datas.length < 6) {
                        return;
                    }
//                    values.put("_id",   datas[0]);
                    values.put("Title", datas[1]);
                    values.put("Remark", datas[2]);
                    values.put("Date", datas[3]);
                    values.put("Money", datas[4]);
                    values.put("InOut", datas[5]);
                    if(datas.length > 6) initMemo = datas[6];  //也许原备份数据没有 memo字段
                    values.put("Memo", initMemo);
                    account = db.insert("account", null, values);
                    if(account>0)   recordCount++;
                }
                db.close();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            ShowToast(" 共成功导入数据 " + recordCount + " 条！ ", Color.BLUE);
        }
    }

    /*
    //    public String getRealPathFromUri(Uri uri) {
    //        //        String filePath = "null";
    //        String[] filePathColumn = {MediaStore.Images.Media.DATA};
    //        Cursor cursor = getContentResolver().query(uri, filePathColumn, null, null, null);
    //        if (cursor != null) {
    //            cursor.moveToFirst();
    //            int columnIndex = 256;  cursor.getColumnIndex(filePathColumn);
    //            filePath = cursor.getString(columnIndex);
    //            cursor.close();
    //        }
    //        return "";//filePath;
    //    }
    */
    public String getRealFileName(Uri uri) {
        String result = "";
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    cursor.moveToFirst();
                    //String resultpp = cursor.getString(256);
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (cursor != null) cursor.close();
        }
        return result;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // ACTION_GET_CONTENT  导入指定的备份数据文件
        if ((resultCode == RESULT_OK)&&(requestCode == 999)) {   // select file  导入指定的备份数据文件
            //            if( data==null ){}  //没有选择数据文件
            if (data != null) {  //选择了数据文件
                Uri uri = data.getData();
                String fileNme = getRealFileName(uri);  // get file path string
                ImportCsv(fileNme);     // 导入指定文件的数据到数据库
            }
        }
        // Androidfilepicker  or LFilePicker 导入指定的备份数据文件
        if (resultCode == RESULT_OK) {
            if ((requestCode == 8899) || (requestCode == 9988)) {
                String fileName = "";
                // Android filepicker
                if (requestCode == 8899) {
                    List<String> dataList = FilePickerManager.obtainData();
                    fileName = dataList.get(0); // get file path string
                }

                // LFilePicker
                //                if (requestCode == 9988) {
                //                    String mRESULT_INFO = "paths";
                //                    List<String> dataList = data.getStringArrayListExtra(mRESULT_INFO);
                //                    if (dataList != null) { // select one file
                //                        fileName = dataList.get(0);
                //                    }
                //                }

                int indexStart = fileName.lastIndexOf("/");
                if (indexStart >= 0) indexStart++;
                fileName = fileName.substring(indexStart);
                if (!fileName.equals(""))
                    ImportCsv(fileName); // 导入指定文件的数据到数据库                   //
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        RelativeLayout tmpLayout = findViewById(R.id.focus);  // SearchView 的上级 控件,焦点设为此控件
        tmpLayout.setFocusable(true);
        tmpLayout.setFocusableInTouchMode(true);
        tmpLayout.requestFocus();  //获取焦点
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //检验是否获取权限，如果获取权限，外部存储会处于开放状态，会弹出一个toast提示获得授权
                    String sdCard = Environment.getExternalStorageState();
                    if (sdCard.equals(Environment.MEDIA_MOUNTED)) {
                        //Toast.makeText(this, "获得授权", Toast.LENGTH_SHORT).show();
                        ShowToast("获得授权!",Color.GREEN);
                        {
                            initData(CurrentName);   //获得授权后，显示记录
                        }
                    }
                } else {
                    runOnUiThread((() -> ShowToast("未获授权!",Color.RED)));
                    //Toast.makeText(MainActivity.this, "未获授权", Toast.LENGTH_SHORT).show()));
                }
                break;
            case 2:
                break;
        }

    }
}