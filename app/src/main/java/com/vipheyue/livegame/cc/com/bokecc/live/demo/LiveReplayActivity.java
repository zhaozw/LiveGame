package com.vipheyue.livegame.cc.com.bokecc.live.demo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TabHost;
import android.widget.TextView;

import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplay;
import com.bokecc.sdk.mobile.live.replay.DWLiveReplayListener;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayChatMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQAMsg;
import com.bokecc.sdk.mobile.live.util.HttpUtil;
import com.bokecc.sdk.mobile.live.widget.DocView;
import com.vipheyue.livegame.R;
import com.vipheyue.livegame.cc.com.bokecc.live.adapter.MyReplayChatListViewAdapter;
import com.vipheyue.livegame.cc.com.bokecc.live.adapter.MyReplayQAListViewAdapter;

import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class LiveReplayActivity extends Activity implements SurfaceHolder.Callback, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnBufferingUpdateListener, View.OnClickListener, IMediaPlayer.OnCompletionListener {

	private IjkMediaPlayer player;
    private SurfaceView sv;
    private SurfaceHolder holder;
    private DocView pv;
    private DWLiveReplay dwLiveReplay;
    private Button playBtn; 
    private ListView lvReplayChat, lvReplayQa;
    private RelativeLayout bl;

    private LinearLayout playControler;
    private SeekBar playSeekBar;
    
    private TextView currentTime, totalTime;
    
    private Button btnFullScreen;
    
	private Timer timer;
	private TimerTask timerTask;
	
	private final int seekBarMax = 10000;
	private TreeSet<ReplayQAMsg> qaMsgs;
	private TreeSet<ReplayChatMsg> replayChatMsgs;
	private MyReplayChatListViewAdapter myReplayChatListViewAdapter;
	private MyReplayQAListViewAdapter myReplayQAListViewAdapter;
	
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
				case 0:
					if (player != null && player.isPlayable() && player.isPlaying()) {
						long currentPosition = player.getCurrentPosition();
						currentTime.setText(parseTime(currentPosition));
						int progress = (int)(seekBarMax * (double)currentPosition / (double)player.getDuration()); 
						playSeekBar.setProgress(progress);
					}
					break;
				case 1:
					myReplayQAListViewAdapter = new MyReplayQAListViewAdapter(getApplicationContext(), qaMsgs);
					lvReplayQa.setAdapter(myReplayQAListViewAdapter);
					myReplayQAListViewAdapter.notifyDataSetChanged();
					break;
				case 2:
					myReplayChatListViewAdapter = new MyReplayChatListViewAdapter(getApplicationContext(), replayChatMsgs);
					lvReplayChat.setAdapter(myReplayChatListViewAdapter);
					myReplayChatListViewAdapter.notifyDataSetChanged();
					break;
			}
		}
	};
	
	private DWLiveReplayListener replayListener = new DWLiveReplayListener() {
		
		@Override
		public void onQuestionAnswer(TreeSet<ReplayQAMsg> qaMsgs) {
			LiveReplayActivity.this.qaMsgs = qaMsgs;
			handler.sendEmptyMessage(1);
		}
		
		@Override
		public void onException(DWLiveException exception) {}
		
		@Override
		public void onChatMessage(TreeSet<ReplayChatMsg> replayChatMsgs) {
			LiveReplayActivity.this.replayChatMsgs = replayChatMsgs;
			handler.sendEmptyMessage(2);
		}

		@Override
		public void onInitFinished() {
			handler.sendEmptyMessage(3);
		}
	};
	
	private boolean isPrepared = false;
	private TabHost tabHost;
	private WindowManager wm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.replay_room_live);
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        tabHost = (TabHost) findViewById(R.id.tabhost);  
        tabHost.setup();  
        tabHost.setVisibility(View.INVISIBLE);
        
        init();

        initRoomShow();
        
        initPlayer();
        
        HttpUtil.LOG_LEVEL = HttpUtil.HttpLogLevel.DETAIL;
    }
    
    private void initRoomShow() {
    	
    	Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        
        chatStr = bundle.getString("chat");
        pdfStr = bundle.getString("pdf");
        qaStr = bundle.getString("qa");
    	
    	if ("1".equals(chatStr)) {
			tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("聊天").setContent(R.id.msg_layout));  
		}
		
		if ("1".equals(pdfStr)) {
			tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("文档").setContent(R.id.replay_docView));
		}
		
		if ("1".equals(qaStr)) {
			tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("问答").setContent(R.id.qa_layout));
		}
        
        tabHost.setVisibility(View.VISIBLE);
    }

    private ProgressBar pbReplayLoading;
    private void init() {
    	
    	timer = new Timer();
    	pbReplayLoading = (ProgressBar) findViewById(R.id.pb_replay_loading);
    	bl = (RelativeLayout) findViewById(R.id.bl);
    	bl.setOnClickListener(this);
        
        lvReplayChat = (ListView) findViewById(R.id.lv_replay_chat);
        
        lvReplayQa = (ListView) findViewById(R.id.lv_replay_qa);
        
        pv = (DocView) findViewById(R.id.replay_docView);
        sv = (SurfaceView) findViewById(R.id.sv);
        sv.setFocusable(true);
        sv.requestFocus();
        sv.setFocusableInTouchMode(true);
        holder = sv.getHolder();
        holder.addCallback(this);
        
        currentTime = (TextView) findViewById(R.id.current_time);
        totalTime = (TextView) findViewById(R.id.total_time);
        
        btnFullScreen = (Button) findViewById(R.id.btn_replay_fullscreen);
        btnFullScreen.setOnClickListener(this);
        
        playControler = (LinearLayout) findViewById(R.id.play_control);
        playBtn = (Button) findViewById(R.id.play_btn);
        playBtn.setOnClickListener(this);
        
        playSeekBar = (SeekBar) findViewById(R.id.play_seekBar);
        playSeekBar.setMax(seekBarMax);
        playSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int currentPosition = seekBar.getProgress();
				if (player.isPlayable()) {
					player.seekTo(player.getDuration() * currentPosition / seekBar.getMax());
				}
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
		});
    }
    
    private String chatStr, pdfStr, qaStr;
    private void initPlayer() {
    	player = new IjkMediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnVideoSizeChangedListener(this);
        player.setOnErrorListener(this);
        player.setOnBufferingUpdateListener(this);
        player.setOnCompletionListener(this);
        
        dwLiveReplay = DWLiveReplay.getInstance();
        dwLiveReplay.setReplayParams(player, pv, replayListener);
    }

    @Override
    protected void onDestroy() {
    	player.pause();
    	player.stop();
    	player.release();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        player.setScreenOnWhilePlaying(true);
        this.holder = surfaceHolder;
        player.setDisplay(holder);
        dwLiveReplay.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        Log.i("demo", "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.i("demo", "surfaceDestroyed");
        isPrepared = false;
        stopTimer();
        dwLiveReplay.stop();
        
    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        Log.i("demo", "onPrepared");
        isPrepared = true;
        pbReplayLoading.setVisibility(View.GONE);
        player.start();
        playControler.setVisibility(View.VISIBLE);
        totalTime.setText(parseTime(player.getDuration()));
        startTimer();
    }
    
    private String parseTime(long timeLong) {
    	StringBuilder sb = new StringBuilder();
    	timeLong = timeLong / 1000;
    	long minuteTime = timeLong / 60;
    	long secondTime = timeLong % 60;
    	processTime(sb, minuteTime);
    	sb.append(":");
    	processTime(sb, secondTime);
    	return sb.toString();
    }
    
    private void processTime(StringBuilder sb, long convertTime) {
    	if (convertTime == 0) {
    		sb.append("00");
    	} else if (convertTime < 10 && convertTime >0) {
    		sb.append("0"+convertTime);
    	} else {
    		sb.append(convertTime);
    	}
    }
    
    private void startTimer() {
    	timerTask = new TimerTask() {
			@Override
			public void run() {
				handler.sendEmptyMessage(0);
			}
		};
		timer.schedule(timerTask, 0, 1000);
    }
    
    private void stopTimer() {
    	if (timerTask != null) {
    		timerTask.cancel();
    	}
    }

    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
        Log.i("demo", "onVideoSizeChanged"+"width"+width+"height"+height);
        setSurfaceViewLayout();
    }

    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        Log.i("demo", "player onError");
        return false;
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {}

    @Override
    public void onClick(View view) {
    	switch(view.getId()) {
	    	case R.id.play_btn:
	    		if (player.isPlaying()) {
	    			player.pause();
	    			playBtn.setText("开始");
	    		} else {
	    			player.start();
	    			playBtn.setText("暂停");
	    		}
	    		break;
	    	case R.id.bl:
	    		if (isPrepared) {
	    			if (playControler.getVisibility() == View.VISIBLE) {
		    			playControler.setVisibility(View.INVISIBLE);
		    		} else {
		    			playControler.setVisibility(View.VISIBLE);
		    		}
	    		}
	    		break;
	    	case R.id.btn_replay_fullscreen:
	    		if (isPortrait()) {
	    			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    		} else {
	    			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    		}
	    		break;
    	}
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isPrepared) {
        	player.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPrepared) {
        	player.start();
        }
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
//        if (dwLiveReplay != null) {
//        	dwLiveReplay.stop();
//        }
    }
    
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		
		super.onConfigurationChanged(newConfig);
		
		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			tabHost.setVisibility(View.VISIBLE);
			btnFullScreen.setBackgroundResource(R.drawable.fullscreen_close);
		} else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			tabHost.setVisibility(View.GONE);
			btnFullScreen.setBackgroundResource(R.drawable.fullscreen_open);
		}
		
		setSurfaceViewLayout();
	}

 // 设置surfaceview的布局
 	private void setSurfaceViewLayout() {
 		LayoutParams params = getScreenSizeParams();
 		params.addRule(RelativeLayout.CENTER_IN_PARENT);
 		sv.setLayoutParams(params);
 	}

 	private LayoutParams getScreenSizeParams() {
		int width = 600;
		int height = 400;
		if (isPortrait()) {
			width = wm.getDefaultDisplay().getWidth();
			height = wm.getDefaultDisplay().getHeight() / 3; //TODO 根据当前布局更改
		} else {
			width = wm.getDefaultDisplay().getWidth();
			height = wm.getDefaultDisplay().getHeight();
		}
		
		int vWidth = player.getVideoWidth();
		if (vWidth == 0) {
			vWidth = 600;
		}

		int vHeight = player.getVideoHeight();
		if (vHeight == 0) {
			vHeight = 400;
		}

		if (vWidth > width || vHeight > height) {
			float wRatio = (float) vWidth / (float) width;
			float hRatio = (float) vHeight / (float) height;
			float ratio = Math.max(wRatio, hRatio);

			width = (int) Math.ceil((float) vWidth / ratio);
			height = (int) Math.ceil((float) vHeight / ratio);
		} else {
			float wRatio = (float) width / (float) vWidth;
			float hRatio = (float) height / (float) vHeight;
			float ratio = Math.min(wRatio, hRatio);

			width = (int) Math.ceil((float) vWidth * ratio);
			height = (int) Math.ceil((float) vHeight * ratio);
		}
		LayoutParams params = new LayoutParams(width, height);
		return params;
	}
 	
 // 获得当前屏幕的方向
 	private boolean isPortrait() {
 		int mOrientation = getApplicationContext().getResources().getConfiguration().orientation;
 		if ( mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
 			return false;
 		} else{
 			return true;
 		}
 	}

}
