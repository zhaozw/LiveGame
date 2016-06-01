package com.vipheyue.livegame.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.vipheyue.livegame.R;
import com.vipheyue.livegame.bean.ConnectData;
import com.vipheyue.livegame.bean.GameBean;
import com.vipheyue.livegame.utils.GsonUtils;

import org.json.JSONObject;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.BmobRealTimeData;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.ValueEventListener;

public class TestActivity extends AppCompatActivity {
    GameBean current;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
//        saveBean();
        connect();
//        getGameBean();
    }


    private void saveBean() {
        GameBean bean = new GameBean();
        bean.setTotalIn_bei(2);
        bean.setTotalIn_dong(2);
        bean.setTotalIn_nan(2);
        bean.setTotalIn_xi(2);
        bean.save(this, new SaveListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(TestActivity.this, "onSuccess", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int i, String s) {
                Toast.makeText(TestActivity.this, "onFailure"+s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void connect() {
        final BmobRealTimeData rtd = new BmobRealTimeData();
        rtd.start(this, new ValueEventListener() {
            @Override
            public void onDataChange(JSONObject data) {
                // TODO Auto-generated method stub
                Log.d("TestActivity", "(" + data.optString("action") + ")" + "数据：" + data);
                ConnectData bean=   GsonUtils.fromJson(data.toString(), ConnectData.class);
                Log.d("TestActivity", bean.getData().getTotalIn_dong() + " " + bean.getData().getTotalIn_nan() + " " + bean.getData().getTotalIn_xi() + " " + bean.getData().getTotalIn_bei());
            }

            @Override
            public void onConnectCompleted() {
                // TODO Auto-generated method stub
                Log.d("bmob", "连接成功:" + rtd.isConnected());
                if (rtd.isConnected()) {
                    // 监听表更新
                    rtd.subTableUpdate("GameBean");
                    // 监听行更新
//                    rtd.subRowUpdate("_User", "d7US000P");
                }
            }
        });
    }

    private void getGameBean() {
        BmobQuery<GameBean> query = new BmobQuery<GameBean>();
        query.setLimit(1); // 限制最多10条数据结果作为一页
        query.order("-updatedAt");
        //TODO 这里要不要加学校限制
        query.findObjects(this, new FindListener<GameBean>() {
            @Override
            public void onSuccess(List<GameBean> object) {
                current = object.get(0);
                Log.d("TestActivity", "currentGameBean.getTotalIn_dong():" + current.getTotalIn_dong());

            }

            @Override
            public void onError(int code, String msg) {

            }
        });
    }

}
