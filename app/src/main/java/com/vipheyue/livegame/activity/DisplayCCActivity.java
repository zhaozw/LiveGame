package com.vipheyue.livegame.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.orhanobut.logger.Logger;
import com.vipheyue.livegame.R;
import com.vipheyue.livegame.bean.ConnectData;
import com.vipheyue.livegame.bean.ContactBean;
import com.vipheyue.livegame.bean.GameBean;
import com.vipheyue.livegame.bean.MyUser;
import com.vipheyue.livegame.utils.GsonUtils;

import org.json.JSONObject;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.GetListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.ValueEventListener;


public class DisplayCCActivity extends AppCompatActivity {
    @Bind(R.id.tv_dong_total)
    TextView tv_dong_total;
    @Bind(R.id.tv_dong_Mytotal)
    TextView tv_dong_Mytotal;
    @Bind(R.id.tv_nan_total)
    TextView tv_nan_total;
    @Bind(R.id.tv_nan_Mytotal)
    TextView tv_nan_Mytotal;
    @Bind(R.id.tv_xi_total)
    TextView tv_xi_total;
    @Bind(R.id.tv_xi_Mytotal)
    TextView tv_xi_Mytotal;
    @Bind(R.id.tv_bei_total)
    TextView tv_bei_total;
    @Bind(R.id.tv_bei_Mytotal)
    TextView tv_bei_Mytotal;
    GameBean currentGameBean = new GameBean();
    @Bind(R.id.tv_userName)
    TextView tv_userName;

    @Bind(R.id.tv_userMoney)
    TextView tv_userMoney;
    @Bind(R.id.tv_indicator)
    TextView tv_indicator;
    @Bind(R.id.tv_indicator_Time)
    TextView tv_indicator_Time;
    @Bind(R.id.ftv_tip)
    FocusedTextView ftv_tip;
    @Bind(R.id.iv_direction_dong)
    ImageView iv_direction_dong;
    @Bind(R.id.iv_direction_nan)
    ImageView iv_direction_nan;
    @Bind(R.id.iv_direction_xi)
    ImageView iv_direction_xi;
    @Bind(R.id.iv_direction_bei)
    ImageView iv_direction_bei;
    @Bind(R.id.main_amount_10)
    ImageView main_amount_10;
    @Bind(R.id.main_amount_100)
    ImageView main_amount_100;
    @Bind(R.id.main_amount_50)
    ImageView main_amount_50;
    @Bind(R.id.main_amount_500)
    ImageView main_amount_500;
    @Bind(R.id.tv_bottom_recharge)
    TextView tv_bottom_recharge;
    @Bind(R.id.tv_bottom_exchange)
    TextView tv_bottom_exchange;
    @Bind(R.id.tv_bottom_presented)
    TextView tv_bottom_presented;
    @Bind(R.id.tv_bottom_out)
    TextView tv_bottom_out;

    private int currentSelectAmount;
    private int direction_mIn_dong;//我的下注
    private int direction_mIn_nan;
    private int direction_mIn_xi;
    private int direction_mIn_bei;

    //最多:2000+( 4个区域总和-2000)/4

    private MyUser currentUser;
    private String tempObjectId;
    private NiftyDialogBuilder dialogBuilder;
    private CountDownTimer betCountDown;
    private CountDownTimer resultCountDown;
    private String contactTip = "请联系QQ:346920463";
    private String contactTip2 = "请联系QQ:346920463";
    private String contactTip3 = "请联系QQ:346920463";
    Boolean isFirstUse = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_cc);
        ButterKnife.bind(this);
        currentUser = BmobUser.getCurrentUser(this, MyUser.class);//这里只执行一次 因为需要操作的是临时的 currentUser 不是真实User
        queryTodayTip();
        updateAccount();
        getLatestGameBean();
    }

    private void queryTodayTip() {
        BmobQuery<ContactBean> query = new BmobQuery<ContactBean>();
        query.getObject(this, "672481cacc", new GetListener<ContactBean>() {

            @Override
            public void onSuccess(ContactBean object) {
                ftv_tip.setText(object.getTodayTip());
                contactTip = object.getContactTip();
                contactTip2 = object.getContactTip2();
                contactTip3 = object.getContactTip3();
            }

            @Override
            public void onFailure(int code, String arg0) {
            }

        });

    }

    /**
     * 1 更新用户金币,注意这里不能再重新获取一次 不能执行 currentUser = BmobUser.getCurrentUser(this, MyUser.class); 因为操作的是临时的user
     **/
    private void updateAccount() {
        tv_userName.setText("账号:" + currentUser.getUsername());
        tv_userMoney.setText("财富:" + currentUser.getMoney());
    }

    /**
     * 2 获取服务器上最新的User 并且赋值给临时user currentUser
     **/
    private void getLatestGameBean() {
        currentUser = BmobUser.getCurrentUser(this, MyUser.class);
        BmobQuery<GameBean> query = new BmobQuery<GameBean>();
        query.setLimit(1); // 限制最多10条数据结果作为一页
        query.order("-updatedAt");
        query.findObjects(this, new FindListener<GameBean>() {
            @Override
            public void onSuccess(List<GameBean> object) {
                currentGameBean = object.get(0);
                tempObjectId = currentGameBean.getObjectId();
                Log.d("TestActivity", "currentGameBean.getTotalIn_dong():" + currentGameBean.getTotalIn_dong());
                initTotalDirection();//TODO 新增 这儿之前做掉了
                LongConnectListener();

                clearAllCountDown();
                switch (currentGameBean.getState()) {
                    case 0://空闲状态不能下注
                        tv_indicator.setText("空闲状态");
                        tv_indicator_Time.setText("-");
                        break;

                    case 1://下注状态
                        if (isFirstUse) {
                            tv_indicator.setText("空闲状态");
                            tv_indicator_Time.setText("-");
                            currentGameBean.setState(0);
                        }else{
                            tv_indicator.setText("下注时间");
                            startBetCountDown();
                        }
                        isFirstUse=false;
                        break;

                    case 2://等待开奖中  开奖倒计时
                        tv_indicator.setText("开奖时间");
                        waitResultCountDown(); //开始开奖的倒计时
                        break;

                    case 3://开奖结束
                        tv_indicator.setText("空闲状态");
                        tv_indicator_Time.setText("-");
                        break;

                }

            }

            @Override
            public void onError(int code, String msg) {
                Toast.makeText(DisplayCCActivity.this, "获取最新数据错误码: " + code + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearAllCountDown() {
        if (betCountDown != null) {
            betCountDown.cancel();
        }
        if (resultCountDown != null) {
            resultCountDown.cancel();
        }
    }

    private void waitResultCountDown() {
        resultCountDown = new CountDownTimer(99000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                tv_indicator_Time.setText("" + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                tv_indicator_Time.setText("请等待");
            }
        };
        resultCountDown.start();
    }

    //倒计时
    private void startBetCountDown() {

        betCountDown = new CountDownTimer(30000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                tv_indicator_Time.setText("" + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                tv_indicator.setText("开奖时间");
                currentGameBean.setState(2);//进入等待开奖
                waitResultCountDown();
            }
        };
        betCountDown.start();
    }

    /**
     * 3 监听表 实时跟新GameBean
     **/
    private void LongConnectListener() {
        final BmobRealTimeData rtd = new BmobRealTimeData();
        rtd.start(this, new ValueEventListener() {
            @Override
            public void onDataChange(JSONObject data) {
                Log.d("bmob", "(" + data.optString("action") + ")" + "数据：" + data);
                ConnectData bean = GsonUtils.fromJson(data.toString(), ConnectData.class);

                currentGameBean = bean.getData();
                String leastObjId = currentGameBean.getObjectId();
                if (leastObjId.equals(tempObjectId)) {
                    //obj 相等
                } else {
                    // 取消监听表更新 obj 不相等
                    rtd.unsubTableUpdate("GameBean");
                    //重新获取数据
                    getLatestGameBean();
                    init_mIn_direction();
                    return;
                }
                //TODO 这里需要更新界面
                initTotalDirection();

                if (currentGameBean.getFinish()) {
                    //如果 finish 了 开始结算
                    int answer = currentGameBean.getAnswer();
                    int prize = 0;
                    String lotteryResult = "";
                    switch (answer) {
                        case 1:
                            prize = direction_mIn_dong * 39 / 10;
                            lotteryResult = "东";
                            break;
                        case 2:
                            prize = direction_mIn_nan * 39 / 10;
                            lotteryResult = "南";
                            break;
                        case 3:
                            prize = direction_mIn_xi * 39 / 10;
                            lotteryResult = "西";
                            break;
                        case 4:
                            prize = direction_mIn_bei * 39 / 10;
                            lotteryResult = "北";
                            break;
                    }

                    Logger.d("开奖结果: " + lotteryResult);
                    dialogShow("开奖", "开奖结果: " + lotteryResult);
                    tv_indicator.setText("开奖结果");
                    tv_indicator_Time.setText("" + lotteryResult);
                    clearAllCountDown();
                    currentUser.setMoney(currentUser.getMoney() + prize);
                    currentUser.update(DisplayCCActivity.this, new UpdateListener() {
                        @Override
                        public void onSuccess() {
                            updateAccount();
                            init_mIn_direction();
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            Toast.makeText(DisplayCCActivity.this, "结算失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onConnectCompleted() {
                Log.d("bmob", "连接成功:" + rtd.isConnected());
                if (rtd.isConnected()) {
                    // 监听表更新
                    rtd.subTableUpdate("GameBean");
                }
            }
        });
    }

    private void initTotalDirection() {
        tv_dong_total.setText(currentGameBean.getTotalIn_dong() + "");
        tv_nan_total.setText(currentGameBean.getTotalIn_nan() + "");
        tv_xi_total.setText(currentGameBean.getTotalIn_xi() + "");
        tv_bei_total.setText(currentGameBean.getTotalIn_bei() + "");
    }


    /**
     * 初始化我的下注
     **/
    private void init_mIn_direction() {
        direction_mIn_dong = 0;
        direction_mIn_nan = 0;
        direction_mIn_xi = 0;
        direction_mIn_bei = 0;
        currentSelectAmount = 0;
        tv_dong_Mytotal.setText(direction_mIn_dong + "");
        tv_nan_Mytotal.setText(direction_mIn_nan + "");
        tv_xi_Mytotal.setText(direction_mIn_xi + "");
        tv_bei_Mytotal.setText(direction_mIn_bei + "");
    }

    @OnClick({R.id.iv_direction_dong, R.id.iv_direction_nan, R.id.iv_direction_xi, R.id.iv_direction_bei, R.id.main_amount_10, R.id.main_amount_100, R.id.main_amount_50, R.id.main_amount_500, R.id.tv_bottom_recharge, R.id.tv_bottom_exchange, R.id.tv_bottom_presented, R.id.tv_bottom_out})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_direction_dong:
                pressDirection("dong");
                break;
            case R.id.iv_direction_nan:
                pressDirection("nan");
                break;
            case R.id.iv_direction_xi:
                pressDirection("xi");
                break;
            case R.id.iv_direction_bei:
                pressDirection("bei");
                break;
            case R.id.main_amount_10:
                selectAmount(10);
                break;
            case R.id.main_amount_100:
                selectAmount(100);
                break;
            case R.id.main_amount_50:
                selectAmount(50);
                break;
            case R.id.main_amount_500:
                selectAmount(500);
                break;
            case R.id.tv_bottom_recharge:
                dialogShow("欢迎充值", contactTip);
                break;
            case R.id.tv_bottom_exchange:
                dialogShow("兑换", contactTip2);
                break;
            case R.id.tv_bottom_presented:
                dialogShow("赠送", contactTip3);

//                startActivity(new Intent(this,PersentActivity.class));
                break;
            case R.id.tv_bottom_out:
                BmobUser.logOut(this);   //清除缓存用户对象
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
        }
    }

    /**
     * 按下方位按钮 并更新用户金币(这里需要检查用户金币数)
     **/

    private void pressDirection(final String direction) {
        if (currentGameBean.getState() != 1) {//如果不是下注时间就return;
            return;
        }
        switch (direction) {
            case "dong":
                if (checkSystemMoney(direction_mIn_dong)) {
                    break;
                }
                currentGameBean.setTotalIn_dong(currentGameBean.getTotalIn_dong() + currentSelectAmount);//总额增加
                break;
            case "nan":
                if (checkSystemMoney(direction_mIn_nan)) {
                    break;
                }
                currentGameBean.setTotalIn_nan(currentGameBean.getTotalIn_nan() + currentSelectAmount);
                break;
            case "xi":
                if (checkSystemMoney(direction_mIn_xi)) {
                    break;
                }
                currentGameBean.setTotalIn_xi(currentGameBean.getTotalIn_xi() + currentSelectAmount);
                break;
            case "bei":
                if (checkSystemMoney(direction_mIn_bei)) {
                    break;
                }
                currentGameBean.setTotalIn_bei(currentGameBean.getTotalIn_bei() + currentSelectAmount);
                break;

        }

        //上传数据 total 实时更新GameBean
        currentGameBean.update(this, new UpdateListener() {
            @Override
            public void onSuccess() {
                switch (direction) {
                    case "dong":
                        direction_mIn_dong += currentSelectAmount;
                        tv_dong_Mytotal.setText(direction_mIn_dong + "");
                        break;
                    case "nan":
                        direction_mIn_nan += currentSelectAmount;
                        tv_nan_Mytotal.setText(direction_mIn_nan + "");
                        break;
                    case "xi":
                        direction_mIn_xi += currentSelectAmount;
                        tv_xi_Mytotal.setText(direction_mIn_xi + "");
                        break;
                    case "bei":
                         direction_mIn_bei += currentSelectAmount;
                        tv_bei_Mytotal.setText(direction_mIn_bei + "");
                        break;

                }
            }

            @Override
            public void onFailure(int i, String s) {
                Toast.makeText(DisplayCCActivity.this, "currentGameBean上传到服务器失败" + s, Toast.LENGTH_SHORT).show();

            }
        });

    }

    /**
     * 所选择的一次下注金额
     **/
    private void selectAmount(int i) {
        currentSelectAmount = i;
    }

    /**
     * 判断 下注是否符合系统规则 里面 还有多少money 是否超标
     **/
    // 荷包数量减少 总额增多,然后上传
    //如果监听到 gamebean 的 finish字段为true,就判断数据,然后同步数据;
    public Boolean checkSystemMoney(int direction_TotalMonty) {
        //最多:2000+( 4个区域总和-2000)/4
        int fourDirecBySubtionMoney = (currentGameBean.getTotal()) - 2000;//todo  这里因该是总金额 不是自己投的
        int topMonty = 2000 + (fourDirecBySubtionMoney > 0 ? fourDirecBySubtionMoney / 4 : 0);//目前支持最大的下注
        int tempMyTop = direction_TotalMonty + currentSelectAmount;//选择之后 预计会达到的下注
        if ((tempMyTop) > topMonty) {
            return true;//超标了
        } else {
            return checkMyMoney();//false为没有超标
        }
    }

    /**
     * 检查 还有多少money 是否超标 如何没有就更新界面
     **/
    private Boolean checkMyMoney() {
        if (currentUser.getMoney() >= currentSelectAmount) {
            currentUser.setMoney(currentUser.getMoney() - currentSelectAmount);    // 荷包数量减少 总额增多 这里是数量减少
            updateAccount();
            return false;
        } else {
            Toast.makeText(this, "余额不足", Toast.LENGTH_SHORT).show();
            return true; // FIXME: 16/5/13
//            return false;
        }
    }


    /**
     * 结算时弹出的对话框
     **/
    public void dialogShow(String title, String lotteryResult) {
        dialogBuilder = NiftyDialogBuilder.getInstance(this);
        dialogBuilder
                .withTitle(title)                                  //.withTitle(null)  no title
                .withTitleColor("#FFFFFF")                                  //def
                .withDividerColor("#11000000")                              //def
                .withMessage(lotteryResult)                     //.withMessage(null)  no Msg
                .withMessageColor("#FFFFFFFF")                              //def  | withMessageColor(int resid)
                .withDialogColor("#A935B5")                               //def  | withDialogColor(int resid)                               //def
//                .withIcon(getResources().getDrawable(R.drawable.icon))
                .isCancelableOnTouchOutside(true)                           //def    | isCancelable(true)
                .withDuration(700)                                          //def
                .withEffect(Effectstype.Fliph)                                         //def Effectstype.Slidetop
                .withButton1Text("确定")                                      //def gone
                .withButton2Text("取消")                                  //def gone
                .setCustomView(R.layout.custom_view, this)         //.setCustomView(View or ResId,context)
                .setButton1Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogBuilder.dismiss();
                    }
                })
                .setButton2Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogBuilder.dismiss();
                    }
                })
                .show();
    }

}
