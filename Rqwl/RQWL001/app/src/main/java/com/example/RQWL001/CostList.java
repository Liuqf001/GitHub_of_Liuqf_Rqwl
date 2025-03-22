package com.example.RQWL001;

public class CostList {
    private String _id;
    private String Title = "";
    private String Remark = "";
    private String Date;
    private String Money;
    private String InOut;

    public String getMoney() {
        return Money;
    }

    public void setMoney(String money) {
        Money = money;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String remark) {
        Remark = remark;
    }

    public String getInOut() {
        return InOut;
    }

    public void setInOut(String remark) {
        InOut = remark;
    }

}
