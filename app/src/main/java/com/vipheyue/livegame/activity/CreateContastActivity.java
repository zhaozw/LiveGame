package com.vipheyue.livegame.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.vipheyue.livegame.R;
import com.vipheyue.livegame.bean.ContactBean;

public class CreateContastActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_contast);
        createBean();
    }

    private void createBean() {
        ContactBean bean = new ContactBean();
         bean.setTodayTip("直播猜猜猜....今日活动....");
        bean.setContactTip("请联系QQ:346920463");
        bean.save(this);
    }
}
