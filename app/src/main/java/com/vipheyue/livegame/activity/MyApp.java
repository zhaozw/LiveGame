package com.vipheyue.livegame.activity;

import android.app.Application;

import com.vipheyue.livegame.bean.MyUser;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.BmobInstallation;

/**
 * Created by heyue on 2015/11/9.
 */
public class MyApp extends Application {
    public   static MyApp instance=null;
    private MyUser user;

    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
//        Bmob.initialize(this,helloFromCBM());//正式环境
        Bmob.initialize(this,"e64952ec5a041da32b1c23568730fc4d");//开发环境
        // 使用推送服务时的初始化操作
        BmobInstallation.getCurrentInstallation(this).save();
        // 启动推送服务
//        BmobPush.startWork(this, helloFromCBM());//正式环境
//        BmobPush.startWork(this, "8bc3229d5d5676e5f0a2296de5b65fd3");//开发环境
//        x.Ext.init(this);
//        MobclickAgent.setDebugMode( true );
//        Logger.init("HELPBYLOG");
//        CrashHandler crashHandler = CrashHandler.getInstance();
//        crashHandler.init(getApplicationContext());
//        crashHandler.upCrashInfo(this);

    }
//    public native String helloFromCBM();

    public MyUser getUser() {
        return user;
    }

    public void setUser(MyUser user) {
        this.user = user;
    }

}
