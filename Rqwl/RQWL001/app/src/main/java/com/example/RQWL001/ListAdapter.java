package com.example.RQWL001;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class ListAdapter extends BaseAdapter {
    List<CostList> mList;

//    public ListAdapter(List<CostList> list) {
//        mList = list;
//    }

    private static class ViewHolder {
        TextView tv_title;
        TextView tv_remark;
        TextView tv_date;
        TextView tv_money;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder ;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mLayoutInflater.inflate(R.layout.list_item,  parent, false);
            //取出数据赋值
            //        CostList item = mList.get(position);
            viewHolder.tv_title = (TextView)convertView.findViewById(R.id.tv_title);
            viewHolder.tv_remark = (TextView)convertView.findViewById(R.id.tv_remark);
            viewHolder.tv_date = (TextView)convertView.findViewById(R.id.tv_date);
            viewHolder.tv_money = (TextView)convertView.findViewById(R.id.tv_money);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //绑定
        viewHolder.tv_title.setText(mList.get(position).getTitle());
        viewHolder.tv_remark.setText(mList.get(position).getRemark());
        viewHolder.tv_date.setText(mList.get(position).getDate());
        String strMoney = mList.get(position).getInOut() + mList.get(position).getMoney();
        viewHolder.tv_money.setText(strMoney);
//*
        if ((mList.get(position).getInOut()).equals("送礼") | (mList.get(position).getMoney().charAt(0)==('-'))) {
            viewHolder.tv_money.setTextColor(Color.GREEN);
        }else
        {
            viewHolder.tv_money.setTextColor(Color.RED);
        }

        //*/
        return convertView;

    }

//    private List<CostList> getmList;
    //把item布局转换成ListView
    private final LayoutInflater mLayoutInflater;

    public ListAdapter(Context context, List<CostList> list) {
        mList = list;
        //通过外部传来的Context初始化LayoutInflater对象
        mLayoutInflater = LayoutInflater.from(context);
    }
}
