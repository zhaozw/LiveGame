package com.vipheyue.livegame.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.vipheyue.livegame.R;
import com.vipheyue.livegame.bean.ContactBean;

import cn.bmob.v3.listener.UpdateListener;

public class CreateContastActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contast);
//        createBean();
        updateBean();
    }

    private void updateBean() {

        ContactBean bean = new ContactBean();
        bean.setContactTip2("兑换请联系QQ:2222222");
        bean.setContactTip3("赠送请联系QQ:3333333");
bean.update(this, "672481cacc", new UpdateListener() {
    @Override
    public void onSuccess() {

    }

    @Override
    public void onFailure(int i, String s) {

    }
});

    }

    private void createBean() {
        ContactBean bean = new ContactBean();
         bean.setTodayTip("直播猜猜猜....今日活动....");
        bean.setContactTip("请联系QQ:346920463");
        bean.save(this);
    }
}
