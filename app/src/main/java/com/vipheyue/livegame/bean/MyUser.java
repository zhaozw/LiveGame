package com.vipheyue.livegame.bean;

import cn.bmob.v3.BmobUser;

/**
 * Created by heyue on 2015/12/9.
 */
public class MyUser extends BmobUser {
private  Integer money;

    public Integer getMoney() {
        return money;
    }

    public void setMoney(Integer money) {
        this.money = money;
    }
}