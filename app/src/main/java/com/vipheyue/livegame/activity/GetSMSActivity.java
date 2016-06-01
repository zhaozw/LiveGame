package com.vipheyue.livegame.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vipheyue.livegame.R;
import com.vipheyue.livegame.bean.MyUser;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.RequestSMSCodeListener;
import cn.bmob.v3.listener.ResetPasswordByCodeListener;
import cn.bmob.v3.listener.SaveListener;

public class GetSMSActivity extends AppCompatActivity {
    @Bind(R.id.rl_menu_left)
    RelativeLayout rl_menu_left;
    @Bind(R.id.tv_Menu_Title)
    TextView tv_Menu_Title;
    @Bind(R.id.rl_menu_right)
    RelativeLayout rl_menu_right;
    @Bind(R.id.et_phone)
    EditText et_phone;
    @Bind(R.id.tv_getSMS)
    TextView tv_getSMS;
    @Bind(R.id.et_codeSMS)
    EditText et_codeSMS;
    @Bind(R.id.et_passWord)
    EditText et_passWord;
    @Bind(R.id.btn_Send)
    Button btn_Send;
    private String type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_sms);

        ButterKnife.bind(this);
        type = getIntent().getStringExtra("type");
        tv_Menu_Title.setText(type);
    }

    @OnClick({R.id.tv_getSMS, R.id.btn_Send, R.id.rl_menu_left})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_getSMS:
                getBmobSms();
                seGetSMSState();
                break;
            case R.id.btn_Send:
                businesstypes();
                break;
            case R.id.rl_menu_left:
                finish();
                break;
        }
    }

    private void businesstypes() {
        switch (type) {
            case "忘记密码":
                restUser();
                break;
            case "注册":
                registerUser();
                break;
        }
    }

    private void restUser() {
        BmobUser.resetPasswordBySMSCode(this, et_codeSMS.getText().toString().trim(), et_passWord.getText().toString(), new ResetPasswordByCodeListener() {

            @Override
            public void done(BmobException ex) {
                // TODO Auto-generated method stub
                if (ex == null) {
                    Toast.makeText(GetSMSActivity.this, "密码重置成功", Toast.LENGTH_SHORT).show();
                    Log.i("smile", "密码重置成功");
                    finishAddString();
                } else {
                    Toast.makeText(GetSMSActivity.this, "重置失败" + ex.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    Log.i("smile", "重置失败：code =" + ex.getErrorCode() + ",msg = " + ex.getLocalizedMessage());
                }
            }
        });
    }

    private void finishAddString() {
        Intent intent= getIntent().putExtra("phone", et_phone.getText().toString().trim()).putExtra("password",et_passWord.getText(). toString().trim());
        setResult(RESULT_OK,intent );
        finish();
    }

    private void registerUser() {
        MyUser myUser = new MyUser();
        myUser.setMobilePhoneNumber(et_phone.getText().toString().trim());//设置手机号码（必填）
//        user.setUsername(xxx);                  //设置用户名，如果没有传用户名，则默认为手机号码
        myUser.setPassword(et_passWord.getText().toString().trim());                  //设置用户密码
        myUser.setMoney(0);
        myUser.signOrLogin(this, et_codeSMS.getText().toString().trim(), new SaveListener() {

            @Override
            public void onSuccess() {
                // TODO Auto-generated method stub
                Toast.makeText(GetSMSActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                finishAddString();
            }

            @Override
            public void onFailure(int code, String msg) {
                // TODO Auto-generated method stub
                Toast.makeText(GetSMSActivity.this, "错误码：" + code + ",错误原因：" + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getBmobSms() {
        BmobSMS.requestSMSCode(this, et_phone.getText().toString().trim(), "一键登录和注册模版", new RequestSMSCodeListener() {

            @Override
            public void done(Integer integer, BmobException e) {
                if (e == null) {//验证码发送成功
                    Toast.makeText(GetSMSActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                    Log.i("smile", "短信integer ：" + integer);//用于查询本次短信发送详情
                } else {
                    Toast.makeText(GetSMSActivity.this, "发送失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    private void seGetSMSState() {
        tv_getSMS.setClickable(false);
        tv_getSMS.setBackgroundResource(R.drawable.common_background_yellow_corner_dark);
        CountDownTimer cdt = new CountDownTimer(30000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tv_getSMS.setText("请等待:" + millisUntilFinished / 1000 + "S");
            }

            @Override
            public void onFinish() {
                tv_getSMS.setClickable(true);
                tv_getSMS.setText("获取验证码");
                tv_getSMS.setBackgroundResource(R.drawable.common_background_yellow_corner);
            }
        };
        cdt.start();
    }


}
