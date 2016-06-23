package com.vipheyue.livegame.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.DWLiveLoginListener;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.pojo.RoomInfo;
import com.bokecc.sdk.mobile.live.pojo.TemplateInfo;
import com.bokecc.sdk.mobile.live.pojo.Viewer;
import com.vipheyue.livegame.R;
import com.vipheyue.livegame.bean.MyBmobInstallation;
import com.vipheyue.livegame.bean.MyUser;
import com.vipheyue.livegame.utils.SharePreferencesUtil;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobInstallation;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.LogInListener;


public class LoginActivity extends AppCompatActivity {


    @Bind(R.id.et_phone)
    EditText et_phone;
    @Bind(R.id.et_password)
    EditText et_password;
    @Bind(R.id.bt_login)
    Button bt_login;
    @Bind(R.id.tv_restPassword)
    TextView tv_restPassword;
    @Bind(R.id.tv_register)
    TextView tv_register;
    private int requestCode = 5555;


    private DWLive dwLive = DWLive.getInstance();
    private DWLiveLoginListener dwLiveLoginListener = new DWLiveLoginListener() {

        @Override
        public void onLogin(TemplateInfo info, Viewer viewer, RoomInfo roomInfo) {
            startActivity(new Intent(LoginActivity.this,DisplayCCActivity.class));
            finish();
        }

        @Override
        public void onException(DWLiveException exception) {
            Toast.makeText(LoginActivity.this, "exception", Toast.LENGTH_SHORT).show();
        }
    };
    private void initCC() {
        dwLive.setDWLiveLoginParams(dwLiveLoginListener, "F3C902A33FFD4BB3", "3F5A7B0FDE82736A9C33DC5901307461", "xxxx", "xxxx");
        dwLive.startLogin();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
//        CrashHandler.upCrashInfo(this);
        MyUser bmobUser = BmobUser.getCurrentUser(this,MyUser.class);//r
        if(bmobUser != null){ //缓存用户对象不为空时
            initCC();
        }

        et_phone.setText( SharePreferencesUtil.getSpString("userName", "", this));
        et_password.setText( SharePreferencesUtil.getSpString("userPassWord", "", this));

    }

    @OnClick({R.id.bt_login, R.id.tv_restPassword, R.id.tv_register})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.bt_login:
                startLogin();
                break;
            case R.id.tv_restPassword:
                startMyIntent("忘记密码");
                break;
            case R.id.tv_register:
                startMyIntent("注册");
                break;
        }
    }

    private void startLogin() {
        bt_login.setClickable(false);
        BmobUser.loginByAccount(this, et_phone.getText().toString().trim(), et_password.getText().toString().trim(), new LogInListener<MyUser>() {

            @Override
            public void done(MyUser user, BmobException e) {
                // TODO Auto-generated method stub
                if(user!=null){
                    initCC();
                    SharePreferencesUtil.putSpString("userName",et_phone.getText().toString().trim(),LoginActivity.this);
                    SharePreferencesUtil.putSpString("userPassWord",et_password.getText().toString().trim(),LoginActivity.this);
                    upDateInstallation();

                }else{
                    Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    Toast.makeText(LoginActivity.this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
                    bt_login.setClickable(true);
                }
            }
        });
    }

    private void upDateInstallation() {
        BmobQuery<MyBmobInstallation> query = new BmobQuery<MyBmobInstallation>();
        query.addWhereEqualTo("installationId", BmobInstallation.getInstallationId(this));
        query.findObjects(this, new FindListener<MyBmobInstallation>() {

            @Override
            public void onSuccess(List<MyBmobInstallation> object) {
                // TODO Auto-generated method stub
                if(object.size() > 0){
                    MyBmobInstallation mbi = object.get(0);
                    mbi.setUid(BmobUser.getCurrentUser(LoginActivity.this, MyUser.class).getObjectId());
                    mbi.update(LoginActivity.this);
                }else{
                }
            }

            @Override
            public void onError(int code, String msg) {
                // TODO Auto-generated method stub
            }
        });
    }

    public void startMyIntent(String type) {
        Intent forgetIntent = new Intent(this, GetSMSActivity.class);
        forgetIntent.putExtra("type", type);
        startActivityForResult(forgetIntent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != -1) {
            return;
        }
        String phone = data.getStringExtra("phone");
        String password = data.getStringExtra("password");
        if (!TextUtils.isEmpty(phone)) {
            et_phone.setText(phone);
        }
        if (!TextUtils.isEmpty(password)) {
            et_password.setText(password);
        }
        super.onActivityResult(requestCode, resultCode, data);

    }
}
