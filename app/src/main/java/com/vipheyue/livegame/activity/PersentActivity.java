package com.vipheyue.livegame.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vipheyue.livegame.R;
import com.vipheyue.livegame.bean.MyUser;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.UpdateListener;

public class PersentActivity extends AppCompatActivity {

    @Bind(R.id.iv_menu_leftImg)
    ImageView iv_menu_leftImg;
    @Bind(R.id.rl_menu_left)
    RelativeLayout rl_menu_left;
    @Bind(R.id.tv_Menu_Title)
    TextView tv_Menu_Title;
    @Bind(R.id.tv_menu_right_title)
    TextView tv_menu_right_title;
    @Bind(R.id.iv_menu_right)
    ImageView iv_menu_right;
    @Bind(R.id.rl_menu_right)
    RelativeLayout rl_menu_right;
    @Bind(R.id.et_userName)
    EditText et_userName;
    @Bind(R.id.btn_query)
    Button btn_query;
    @Bind(R.id.et_presentMoney)
    EditText et_presentMoney;
    @Bind(R.id.btn_commit)
    Button btn_commit;
    private MyUser queryUser;
    private MyUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_persent);
        ButterKnife.bind(this);
        currentUser = BmobUser.getCurrentUser(this, MyUser.class);//这里只执行一次 因为需要操作的是临时的 currentUser 不是真实User

    }

    @OnClick({R.id.rl_menu_left, R.id.btn_query, R.id.btn_commit})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.rl_menu_left:
                finish();
                break;
            case R.id.btn_query:
                if (TextUtils.isEmpty(et_userName.getText().toString().trim())) {
                    Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
                } else {
                    queryUserName();
                }
                break;
            case R.id.btn_commit:

                startPresent();
                break;
        }
    }

    private void startPresent() {
        if (TextUtils.isEmpty(et_presentMoney.getText().toString().trim())) {
            Toast.makeText(this, "请输入需要赠送的金币", Toast.LENGTH_SHORT).show();
        } else {
            Integer presentMoney = Integer.parseInt(et_presentMoney.getText().toString().trim());
            checkMyMoney(presentMoney);
        }

    }


    /**
     * 检查 还有多少money 是否超标 如何没有就更新界面
     **/
    private Boolean checkMyMoney(Integer currentSelectAmount) {
        if (currentUser.getMoney() >= currentSelectAmount) {
            btn_commit.setEnabled(false);
            //自己的减少 被赠送的增加
            currentUser.setMoney(currentUser.getMoney() - currentSelectAmount);    // 荷包数量减少 总额增多 这里是数量减少
            currentUser.update(this, new UpdateListener() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onFailure(int i, String s) {
                    Toast.makeText(PersentActivity.this, i + " " + s, Toast.LENGTH_SHORT).show();
                    btn_commit.setEnabled(true);
                }
            });
            MyUser newUser = new MyUser();
            queryUser.setMoney(queryUser.getMoney() + currentSelectAmount);

            newUser.setMoney(queryUser.getMoney());
            newUser.update(this,queryUser.getObjectId(), new UpdateListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(PersentActivity.this, "赠送成功", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int i, String s) {
                    Toast.makeText(PersentActivity.this, i + " " + s, Toast.LENGTH_SHORT).show();
                    btn_commit.setEnabled(true);
                }
            });
            return false;
        } else {
            Toast.makeText(this, "余额不足", Toast.LENGTH_SHORT).show();
            return true; // FIXME: 16/5/13
//            return false;
        }
    }


    private void queryUserName() {
        BmobQuery<MyUser> query = new BmobQuery<MyUser>();
        query.addWhereEqualTo("username", et_userName.getText().toString().trim());
        query.findObjects(this, new FindListener<MyUser>() {
            @Override
            public void onSuccess(List<MyUser> object) {
                queryUser = object.get(0);
                Toast.makeText(PersentActivity.this, "查询成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(int code, String msg) {
            }
        });
    }
}
