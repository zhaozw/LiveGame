package com.vipheyue.livegame.cc.com.bokecc.live.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.bokecc.sdk.mobile.live.util.HttpUtil;
import com.vipheyue.livegame.R;
import com.vipheyue.livegame.cc.com.bokecc.live.adapter.MyLoginFragmentAdapter;
import com.vipheyue.livegame.cc.com.bokecc.live.config.Config;
import com.vipheyue.livegame.cc.com.example.qr_codescan.MipcaActivityCapture;
import com.vipheyue.livegame.fragment.PlayCCFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends ActionBarActivity implements OnClickListener, OnPageChangeListener {
	private final int qrRequestCode = 111;

	private TextView tvLive, tvReplay;

	private ViewPager mPager;
	
	private MyLoginFragmentAdapter loginFragmentAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		android.app.ActionBar ab = getActionBar();
		if (ab != null) {
			ab.setDisplayShowHomeEnabled(false);
		}

		setContentView(R.layout.login);

		init();
	}

	private void init() {
		tvLive = (TextView) findViewById(R.id.tv_live);
		tvReplay = (TextView) findViewById(R.id.tv_replay);
		mPager = (ViewPager) findViewById(R.id.vp_login_infos);
		
		tvLive.setOnClickListener(this);
		tvReplay.setOnClickListener(this);

		tvLive.performClick();
		loginFragmentAdapter = new MyLoginFragmentAdapter(getSupportFragmentManager());
		mPager.setAdapter(loginFragmentAdapter);
		mPager.setCurrentItem(0);
		mPager.setOnPageChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(this, MipcaActivityCapture.class);
			startActivityForResult(intent, qrRequestCode);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case qrRequestCode:
			if (resultCode == RESULT_OK) {
				Bundle bundle = data.getExtras();
				String result = bundle.getString("result");
				Map<String, String> map = parseUrl(result);
				if (map == null) {
					return;
				}
				getLiveIds(map);
				List<Fragment> list = getSupportFragmentManager().getFragments();
				for (int i =0; i<list.size(); i++) {
					PlayCCFragment fragment = (PlayCCFragment) list.get(i);
//					Handler handler = fragment.getHander();
					Message msg = new Message();
					msg.what = 0;
					msg.obj = map;
//					handler.sendMessage(msg);
				}
			}
			break;
		default:
			break;
		}
	}
	
	private String liveUrl = "http://api.csslcloud.net/api/live/info";
	
	private void getLiveIds(final Map<String, String> map) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					String result = HttpUtil.getResult(liveUrl, map, Config.API_KEY);
					if (result != null) {
						parseResult(result);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	private void parseResult(String result) throws JSONException {
		
		List<String> liveIds = new ArrayList<String>();
		JSONObject jsonObject = new JSONObject(result);
		String jsonResult = jsonObject.getString("result");
		if ("OK".equals(jsonResult)) {
			JSONArray lives = jsonObject.getJSONArray("lives");
			for (int i=0;i<lives.length(); i++) {
				JSONObject liveObject = lives.getJSONObject(i);
				liveIds.add(liveObject.getString("id"));
			}
		} else {
			return;
		}
		
		List<Fragment> list = getSupportFragmentManager().getFragments();
		for (int i =0; i<list.size(); i++) {
			PlayCCFragment fragment = (PlayCCFragment) list.get(i);
//			Handler handler = fragment.getHander();
			Message msgLiveId = new Message();
			msgLiveId.what = 1;
			msgLiveId.obj = liveIds;
//			handler.sendMessage(msgLiveId);
		}
	}

	private Map<String, String> parseUrl(String url) {
		Map<String, String> map = new HashMap<String, String>();
		String param = url.substring(url.indexOf("?") + 1, url.length());
		String[] params = param.split("&");

		if (params.length < 2) {
			return null;
		}
		for (String p : params) {
			String[] en = p.split("=");
			map.put(en[0], en[1]);
		}

		return map;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_live:
			deSelected();
			tvLive.setSelected(true);
			mPager.setCurrentItem(0);
			break;
		case R.id.tv_replay:
			deSelected();
			tvReplay.setSelected(true);
			mPager.setCurrentItem(1);
			break;
		}
	}

	private void deSelected() {
		tvLive.setSelected(false);
		tvReplay.setSelected(false);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {}

	@Override
	public void onPageSelected(int arg0) {
		switch(arg0) {
		case 0:
			deSelected();
			tvLive.setSelected(true);
			break;
		case 1:
			deSelected();
			tvReplay.setSelected(true);
			break;
		}
	}
}
