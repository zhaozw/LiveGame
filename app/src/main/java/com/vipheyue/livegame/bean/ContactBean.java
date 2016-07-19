package com.vipheyue.livegame.bean;

import cn.bmob.v3.BmobObject;

/**
 * Created by heyue on 16/7/10.
 */
public class ContactBean extends BmobObject{
    String todayTip;
    String contactTip;
    String contactTip2;
    String contactTip3;

    public String getContactTip2() {
        return contactTip2;
    }

    public void setContactTip2(String contactTip2) {
        this.contactTip2 = contactTip2;
    }

    public String getContactTip3() {
        return contactTip3;
    }

    public void setContactTip3(String contactTip3) {
        this.contactTip3 = contactTip3;
    }

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
