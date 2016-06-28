package com.vipheyue.livegame.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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


public class DisplayActivity extends AppCompatActivity {


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

    @Bind(R.id.tv_userMoney)
    TextView tv_userMoney;

    private int currentSelectAmount;
    private int direction_mIn_dong;//我的下注
    private int direction_mIn_nan;
    private int direction_mIn_xi;
    private int direction_mIn_bei;
//    private int direction_TotalIn_dong = 0;//总下注
//    private int direction_TotalIn_nan = 0;
//    private int direction_TotalIn_xi = 0;
//    private int direction_TotalIn_bei = 0;

    //最多:2000+( 4个区域总和-2000)/4

    private MyUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        ButterKnife.bind(this);
        initView();
        getGameBean();
        connect();
    }

    private void initView() {
        MyUser userInfo = BmobUser.getCurrentUser(this,MyUser.class);
        tv_userName.setText("昵称:"+userInfo.getUsername());
        tv_userMoney.setText("财富:"+userInfo.getMoney());
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

    private void getGameBean() {
        currentUser = BmobUser.getCurrentUser(this, MyUser.class);

        BmobQuery<GameBean> query = new BmobQuery<GameBean>();
        query.setLimit(1); // 限制最多10条数据结果作为一页
        query.order("-updatedAt");
        //TODO 这里要不要加学校限制
        query.findObjects(this, new FindListener<GameBean>() {
            @Override
            public void onSuccess(List<GameBean> object) {
                currentGameBean = object.get(0);
                Log.d("TestActivity", "currentGameBean.getTotalIn_dong():" + currentGameBean.getTotalIn_dong());
            }

            @Override
            public void onError(int code, String msg) {

            }
        });
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
//                tv_dong_total.setText("总下注: " + currentGameBean.getTotalIn_dong() + "元");
                break;
            case "nan":
                if (checkSystemMoney(direction_mIn_nan)) {
                    break;
                }
                currentGameBean.setTotalIn_nan(currentGameBean.getTotalIn_nan() + currentSelectAmount);
                direction_mIn_nan += currentSelectAmount;
                tv_nan_Mytotal.setText("我下注: " + direction_mIn_nan + "元");
//                tv_nan_total.setText("总下注: " + currentGameBean.getTotalIn_nan() + "元");
                break;
            case "xi":
                if (checkSystemMoney(direction_mIn_xi)) {
                    break;
                }
                currentGameBean.setTotalIn_xi(currentGameBean.getTotalIn_xi() + currentSelectAmount);
                direction_mIn_xi += currentSelectAmount;
                tv_xi_Mytotal.setText("我下注: " + direction_mIn_xi + "元");
//                tv_xi_total.setText("总下注: " + currentGameBean.getTotalIn_xi() + "元");
                break;
            case "bei":
                if (checkSystemMoney(direction_mIn_bei)) {
                    break;
                }
                currentGameBean.setTotalIn_bei(currentGameBean.getTotalIn_bei() + currentSelectAmount);
                direction_mIn_bei += currentSelectAmount;
                tv_bei_Mytotal.setText("我下注: " + direction_mIn_bei + "元");
//                tv_bei_total.setText("总下注: " + currentGameBean.getTotalIn_bei() + "元");
                break;
        }

        //上传数据 total
        currentGameBean.update(this, new UpdateListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(DisplayActivity.this, "currentGameBean上传到服务器成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i, String s) {
                Toast.makeText(DisplayActivity.this, "currentGameBean上传到服务器失败" + s, Toast.LENGTH_SHORT).show();

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
//        Toast.makeText(this, "fourDirecBySubtionMoney   " + fourDirecBySubtionMoney + " topMonty: " + topMonty + "    direction_TotalMonty + currentSelectAmount " + tempMyTop, Toast.LENGTH_SHORT).show();
        if ((tempMyTop) > topMonty) {
            return true;//超标了
        } else {
            return checkMyMoney();//false为没有超标
        }
    }

    private Boolean checkMyMoney() {
        if (currentUser.getMoney() >= currentSelectAmount) {
            currentUser.setMoney(currentUser.getMoney() - currentSelectAmount);    // 荷包数量减少 总额增多 这里是数量减少
            return false;
        } else {
            Toast.makeText(this, "余额不足", Toast.LENGTH_SHORT).show();
            return true; // FIXME: 16/5/13
//            return false;
        }
    }

    private void connect() {
        final BmobRealTimeData rtd = new BmobRealTimeData();
        rtd.start(this, new ValueEventListener() {
            @Override
            public void onDataChange(JSONObject data) {
                Toast.makeText(DisplayActivity.this, "监听到数据改变", Toast.LENGTH_SHORT).show();
                Log.d("bmob", "(" + data.optString("action") + ")" + "数据：" + data);
                ConnectData bean = GsonUtils.fromJson(data.toString(), ConnectData.class);
                currentGameBean = bean.getData();
                Log.d("bmob", bean.getData().getTotalIn_dong() + " " + bean.getData().getTotalIn_nan() + " " + bean.getData().getTotalIn_xi() + " " + bean.getData().getTotalIn_bei());
                //TODO 这里需要更新界面
                tv_dong_total.setText("总下注: " + currentGameBean.getTotalIn_dong() + "元");
                tv_nan_total.setText("总下注: " + currentGameBean.getTotalIn_nan() + "元");
                tv_xi_total.setText("总下注: " + currentGameBean.getTotalIn_xi() + "元");
                tv_bei_total.setText("总下注: " + currentGameBean.getTotalIn_bei() + "元");

                if (currentGameBean.getFinish()) {                //如果 finish 了 开始结算
                    Toast.makeText(DisplayActivity.this, "当前 game 已经完了,下面开始结算程序", Toast.LENGTH_SHORT).show();
                    int answer = currentGameBean.getAnswer();
                    int prize = 0;
                    switch (answer) {
                        case 1:
                            prize = direction_mIn_dong * 2;
                            break;
                        case 2:
                            prize = direction_mIn_nan * 2;
                            break;
                        case 3:
                            prize = direction_mIn_xi * 2;
                            break;
                        case 4:
                            prize = direction_mIn_bei * 2;
                            break;
                    }
                    currentUser.setMoney(currentUser.getMoney() + prize - direction_mIn_dong - direction_mIn_nan - direction_mIn_xi - direction_mIn_bei);
                    currentUser.update(DisplayActivity.this, new UpdateListener() {
                        @Override
                        public void onSuccess() {
                            Toast.makeText(DisplayActivity.this, "结算成功", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            Toast.makeText(DisplayActivity.this, "结算失败", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }

            @Override
            public void onConnectCompleted() {
                // TODO Auto-generated method stub
                Log.d("bmob", "连接成功:" + rtd.isConnected());
                if (rtd.isConnected()) {
                    // 监听表更新
                    rtd.subTableUpdate("GameBean");

                }
            }
        });
    }

}
