package com.vipheyue.livegame.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.vipheyue.livegame.R;
import com.vipheyue.livegame.bean.ConnectData;
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
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.ValueEventListener;


public class DisplayCCActivity extends AppCompatActivity {


    String url = "rtmp://v1.live.126.net/live/4c8a4ae25686439b9de048ec75e23c76";
    @Bind(R.id.tv_dong_total)
    TextView tv_dong_total;
    @Bind(R.id.tv_dong_Mytotal)
    TextView tv_dong_Mytotal;

    @Bind(R.id.iv_direction_dong)
    ImageView iv_direction_dong;
    @Bind(R.id.iv_direction_nan)
    ImageView iv_direction_nan;
    @Bind(R.id.tv_nan_total)
    TextView tv_nan_total;
    @Bind(R.id.tv_nan_Mytotal)
    TextView tv_nan_Mytotal;
    @Bind(R.id.iv_direction_xi)
    ImageView iv_direction_xi;
    @Bind(R.id.tv_xi_total)
    TextView tv_xi_total;
    @Bind(R.id.tv_xi_Mytotal)
    TextView tv_xi_Mytotal;
    @Bind(R.id.iv_direction_bei)
    ImageView iv_direction_bei;
    @Bind(R.id.tv_bei_total)
    TextView tv_bei_total;
    @Bind(R.id.tv_bei_Mytotal)
    TextView tv_bei_Mytotal;
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
    GameBean currentGameBean = new GameBean();

    @Bind(R.id.tv_userName)
    TextView tv_userName;
    @Bind(R.id.tv_userId)
    TextView tv_userId;
    @Bind(R.id.tv_userMoney)
    TextView tv_userMoney;

    private int currentSelectAmount;
    private int direction_mIn_dong;//我的下注
    private int direction_mIn_nan;
    private int direction_mIn_xi;
    private int direction_mIn_bei;

    //最多:2000+( 4个区域总和-2000)/4

    private MyUser currentUser;
    private String tempObjectId;
    private NiftyDialogBuilder dialogBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_cc);
        ButterKnife.bind(this);
        currentUser = BmobUser.getCurrentUser(this, MyUser.class);
        updateView();
        getLatestGameBean();
    }


    private void updateView() {
        tv_userName.setText("昵称:" + currentUser.getUsername());
        tv_userId.setText("ID:" + currentUser.getObjectId());
        tv_userMoney.setText("财富:" + currentUser.getMoney());
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
                break;
            case R.id.tv_bottom_exchange:
                break;
            case R.id.tv_bottom_presented:
                break;
            case R.id.tv_bottom_out:
                BmobUser.logOut(this);   //清除缓存用户对象
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
        }
    }

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
                LongConnectListener();
            }

            @Override
            public void onError(int code, String msg) {
                Toast.makeText(DisplayCCActivity.this, "获取最新数据错误码: " + code + msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void init_mIn_direction() {
        direction_mIn_dong = 0;
        direction_mIn_nan = 0;
        direction_mIn_xi = 0;
        direction_mIn_bei = 0;
        currentSelectAmount = 0;
        tv_dong_Mytotal.setText("我下注: " + direction_mIn_dong + "元");
        tv_nan_Mytotal.setText("我下注: " + direction_mIn_nan + "元");
        tv_xi_Mytotal.setText("我下注: " + direction_mIn_xi + "元");
        tv_bei_Mytotal.setText("我下注: " + direction_mIn_bei + "元");
    }

    private void pressDirection(String direction) {
        switch (direction) {
            case "dong":
                if (checkSystemMoney(direction_mIn_dong)) {
                    break;
                }
                currentGameBean.setTotalIn_dong(currentGameBean.getTotalIn_dong() + currentSelectAmount);//总额增加
                direction_mIn_dong += currentSelectAmount;
                tv_dong_Mytotal.setText("我下注: " + direction_mIn_dong + "元");
                break;
            case "nan":
                if (checkSystemMoney(direction_mIn_nan)) {
                    break;
                }
                currentGameBean.setTotalIn_nan(currentGameBean.getTotalIn_nan() + currentSelectAmount);
                direction_mIn_nan += currentSelectAmount;
                tv_nan_Mytotal.setText("我下注: " + direction_mIn_nan + "元");
                break;
            case "xi":
                if (checkSystemMoney(direction_mIn_xi)) {
                    break;
                }
                currentGameBean.setTotalIn_xi(currentGameBean.getTotalIn_xi() + currentSelectAmount);
                direction_mIn_xi += currentSelectAmount;
                tv_xi_Mytotal.setText("我下注: " + direction_mIn_xi + "元");
                break;
            case "bei":
                if (checkSystemMoney(direction_mIn_bei)) {
                    break;
                }
                currentGameBean.setTotalIn_bei(currentGameBean.getTotalIn_bei() + currentSelectAmount);
                direction_mIn_bei += currentSelectAmount;
                tv_bei_Mytotal.setText("我下注: " + direction_mIn_bei + "元");
                break;
        }

        //上传数据 total
        currentGameBean.update(this, new UpdateListener() {
            @Override
            public void onSuccess() {
//                Toast.makeText(DisplayCCActivity.this, "currentGameBean上传到服务器成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i, String s) {
                Toast.makeText(DisplayCCActivity.this, "currentGameBean上传到服务器失败" + s, Toast.LENGTH_SHORT).show();

            }
        });

    }

    private void selectAmount(int i) {
        currentSelectAmount = i;
    }

    // 判断 荷包里面 还有多少money
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

    private Boolean checkMyMoney() {
        if (currentUser.getMoney() >= currentSelectAmount) {
            currentUser.setMoney(currentUser.getMoney() - currentSelectAmount);    // 荷包数量减少 总额增多 这里是数量减少
            updateView();
            return false;
        } else {
            Toast.makeText(this, "余额不足", Toast.LENGTH_SHORT).show();
            return true; // FIXME: 16/5/13
//            return false;
        }
    }

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
                    Log.d("DisplayCCActivity", "obj 相等");
                } else {
                    Log.d("DisplayCCActivity", "boj 不相等");//TODO
                    // 取消监听表更新
                    rtd.unsubTableUpdate("GameBean");
                    //重新获取数据?
                    getLatestGameBean();
                    init_mIn_direction();

                }
                Log.d("bmob", bean.getData().getTotalIn_dong() + " " + bean.getData().getTotalIn_nan() + " " + bean.getData().getTotalIn_xi() + " " + bean.getData().getTotalIn_bei());
                //TODO 这里需要更新界面
                tv_dong_total.setText("总下注: " + currentGameBean.getTotalIn_dong() + "元");
                tv_nan_total.setText("总下注: " + currentGameBean.getTotalIn_nan() + "元");
                tv_xi_total.setText("总下注: " + currentGameBean.getTotalIn_xi() + "元");
                tv_bei_total.setText("总下注: " + currentGameBean.getTotalIn_bei() + "元");

                if (currentGameBean.getFinish()) {
                    //如果 finish 了 开始结算
//                    Toast.makeText(DisplayCCActivity.this, "当前 game 已经完了,下面开始结算程序", Toast.LENGTH_SHORT).show();
                    int answer = currentGameBean.getAnswer();
                    int prize = 0;
                    String lotteryResult = null;
                    switch (answer) {
                        case 1:
                            prize = direction_mIn_dong * 2;
                            lotteryResult = "东";
                            break;
                        case 2:
                            prize = direction_mIn_nan * 2;
                            lotteryResult = "南";
                            break;
                        case 3:
                            prize = direction_mIn_xi * 2;
                            lotteryResult = "西";
                            break;
                        case 4:
                            prize = direction_mIn_bei * 2;
                            lotteryResult = "北";
                            break;
                    }
                    dialogShow(answer,lotteryResult);

//                    currentUser.setMoney(currentUser.getMoney() + prize - direction_mIn_dong - direction_mIn_nan - direction_mIn_xi - direction_mIn_bei);
                    currentUser.setMoney(currentUser.getMoney() + prize);
                    currentUser.update(DisplayCCActivity.this, new UpdateListener() {
                        @Override
                        public void onSuccess() {
                            updateView();
//                            Toast.makeText(DisplayCCActivity.this, "结算成功", Toast.LENGTH_SHORT).show();
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
// 监听行更新
//                    rtd.subRowUpdate("GameBean", currentGameBean.getObjectId());
                }
            }
        });
    }

    public void dialogShow(int answer, String lotteryResult) {
        dialogBuilder = NiftyDialogBuilder.getInstance(this);
        dialogBuilder
                .withTitle("开奖")                                  //.withTitle(null)  no title
                .withTitleColor("#FFFFFF")                                  //def
                .withDividerColor("#11000000")                              //def
                .withMessage("开奖结果: "+lotteryResult)                     //.withMessage(null)  no Msg
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
