 package com.vipheyue.livegame.neliveplayer;

 import android.app.Activity;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.TextView;

 import com.netease.neliveplayer.NEMediaPlayer;
 import com.vipheyue.livegame.R;

 import java.util.List;

 public class NEVideoPlayerActivity extends Activity {
     public final static String TAG = "NELivePlayer/NEVideoPlayerActivity";
     public NEVideoView mVideoView;  //用于画面显示
     private View mBuffer; //用于指示缓冲状态
     private NEMediaController mMediaController; //用于控制播放

     public static final int NELP_LOG_UNKNOWN = 0; //!< log输出模式：输出详细
     public static final int NELP_LOG_DEFAULT = 1; //!< log输出模式：输出详细
     public static final int NELP_LOG_VERBOSE = 2; //!< log输出模式：输出详细
     public static final int NELP_LOG_DEBUG   = 3; //!< log输出模式：输出调试信息
     public static final int NELP_LOG_INFO    = 4; //!< log输出模式：输出标准信息
     public static final int NELP_LOG_WARN    = 5; //!< log输出模式：输出警告
     public static final int NELP_LOG_ERROR   = 6; //!< log输出模式：输出错误
     public static final int NELP_LOG_FATAL   = 7; //!< log输出模式：一些错误信息，如头文件找不到，非法参数使用
     public static final int NELP_LOG_SILENT  = 8; //!< log输出模式：不输出

     private String mVideoPath; //文件路径
     private String mDecodeType;//解码类型，硬解或软解
     private String mMediaType; //媒体类型
     private boolean mHardware = true;
     private ImageButton mPlayBack;
     private TextView mFileName; //文件名称
     private ImageView mAudioRemind; //播音频文件时提示
     private String mTitle;
     private Uri mUri;
     private boolean pauseInBackgroud = true;

     private RelativeLayout mPlayToolbar;

     NEMediaPlayer mMediaPlayer = new NEMediaPlayer();

     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_player);

         //接收MainActivity传过来的参数
         mMediaType   = getIntent().getStringExtra("media_type");
         mDecodeType = getIntent().getStringExtra("decode_type");
         mVideoPath  = getIntent().getStringExtra("videoPath");


         if (mMediaType.equals("localaudio")) { //本地音频文件采用软件解码
             mDecodeType = "software";
         }

         Intent intent = getIntent();
         String intentAction = intent.getAction();
         if (!TextUtils.isEmpty(intentAction) && intentAction.equals(Intent.ACTION_VIEW)) {
             mVideoPath = intent.getDataString();
             Log.d(TAG, "videoPath = "+ mVideoPath);
         }

         if (mDecodeType.equals("hardware")) {
             mHardware = true;
         }
         else if (mDecodeType.equals("software")) {
             mHardware = false;
         }

         mPlayBack = (ImageButton)findViewById(R.id.player_exit);//退出播放
         mPlayBack.getBackground().setAlpha(0);
         mFileName = (TextView)findViewById(R.id.file_name);

         mUri = Uri.parse(mVideoPath);
         if (mUri != null) { //获取文件名，不包括地址
             List<String> paths = mUri.getPathSegments();
             String name = paths == null || paths.isEmpty() ? "null" : paths.get(paths.size() - 1);
             setFileName(name);
         }

         mAudioRemind = (ImageView)findViewById(R.id.audio_remind);
         if (mMediaType.equals("localaudio")) {
             mAudioRemind.setVisibility(View.VISIBLE);
             //mAudioRemind.setBackgroundColor(Color.rgb(255, 0, 0));
         }
         else {
             mAudioRemind.setVisibility(View.INVISIBLE);
         }

         mPlayToolbar = (RelativeLayout)findViewById(R.id.play_toolbar);
         mPlayToolbar.setVisibility(View.INVISIBLE);

         mBuffer = findViewById(R.id.buffering_prompt);
         mMediaController = new NEMediaController(this);

         mVideoView = (NEVideoView) findViewById(R.id.video_view);

         if (mMediaType.equals("livestream")) {
             mVideoView.setBufferStrategy(0); //直播低延时
         }
         else {
             mVideoView.setBufferStrategy(1); //点播抗抖动
         }
         mVideoView.setMediaController(mMediaController);
         mVideoView.setBufferPrompt(mBuffer);
         mVideoView.setMediaType(mMediaType);
         mVideoView.setHardwareDecoder(mHardware);
         mVideoView.setPauseInBackground(pauseInBackgroud);
         mVideoView.setVideoPath(mVideoPath);
         mMediaPlayer.setLogLevel(NELP_LOG_SILENT); //设置log级别
         mVideoView.requestFocus();
         mVideoView.start();
         Log.d(TAG, "Version = "+ mMediaPlayer.getVersion()); //获取解码库版本号

         mPlayBack.setOnClickListener(mOnClickEvent); //监听退出播放的事件响应
         mMediaController.setOnShownListener(mOnShowListener); //监听mediacontroller是否显示
         mMediaController.setOnHiddenListener(mOnHiddenListener); //监听mediacontroller是否隐藏
     }

     OnClickListener mOnClickEvent = new OnClickListener() {
         @Override
         public void onClick(View v) {
             if (v.getId() == R.id.player_exit) {
                 mVideoView.release_resource();
                 onDestroy();
                 finish();
             }
         }
     };

     NEMediaController.OnShownListener mOnShowListener = new NEMediaController.OnShownListener() {

         @Override
         public void onShown() {
             mPlayToolbar.setVisibility(View.VISIBLE);
             mPlayToolbar.requestLayout();
                mVideoView.invalidate();
                mPlayToolbar.postInvalidate();
         }
     };

     NEMediaController.OnHiddenListener mOnHiddenListener = new NEMediaController.OnHiddenListener() {

         @Override
         public void onHidden() {
             mPlayToolbar.setVisibility(View.INVISIBLE);
         }
     };

     public void setFileName(String name) { //设置文件名并显示出来
         mTitle = name;
         if (mFileName != null)
             mFileName.setText(mTitle);

         mFileName.setGravity(Gravity.CENTER);
     }

     @Override
     protected void onStop() {
         Log.d(TAG, "NEVideoPlayerActivity onStop");
         super.onStop();
     }

     @Override
     protected void onPause() {
         Log.d(TAG, "NEVideoPlayerActivity onPause");

         if (pauseInBackgroud)
             mVideoView.pause(); //锁屏时暂停
         super.onPause();
     }

     @Override
     protected void onDestroy() {
         Log.d(TAG, "NEVideoPlayerActivity onDestroy");
         mVideoView.release_resource();
         super.onDestroy();
     }

     @Override
     protected void onStart() {
         Log.d(TAG, "NEVideoPlayerActivity onStart");
         super.onStart();
     }

     @Override
     protected void onResume() {
         Log.d(TAG, "NEVideoPlayerActivity onResume");
         if (pauseInBackgroud && !mVideoView.isPaused()) {
             mVideoView.start(); //锁屏打开后恢复播放
         }
         super.onResume();
     }

     @Override
     protected void onRestart() {
         Log.d(TAG, "NEVideoPlayerActivity onRestart");
         super.onRestart();
     }
 }