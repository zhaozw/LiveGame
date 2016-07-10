package com.vipheyue.livegame.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by heyue on 16/7/10.
 */
public class ContactBean extends BmobObject{
    String todayTip;
    String contactTip;

    public String getTodayTip() {
        return todayTip;
    }

    public void setTodayTip(String todayTip) {
        this.todayTip = todayTip;
    }

    public String getContactTip() {
        return contactTip;
    }

    public void setContactTip(String contactTip) {
        this.contactTip = contactTip;
    }
}
