package com.vipheyue.livegame.neliveplayer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.vipheyue.livegame.R;


public class NEMainActivity extends Activity
{
	public final static String TAG = "NELivePlayer/NEMainActivity";
	
	private TextView mMediaOption;    //显示播放选项
	private NELocalDirectory mLocalDir; //本地文件目录
	private ListView localMediaList; //用于显示本地文件
	private OnClickListener mOnClickEvent; //用于监听按钮事件
	private Button mBtnLiveStream;
	private Button mBtnVideoOnDemand;
	private Button mBtnLocalVideo;
	private Button mBtnLocalAudio;
	private ImageView mMediaTypeSelected;
	//private ImageView mMediaTypeUnselected;
	private EditText mEditURL; //用于输入网络流地址
	private Button mBtnPlay;   //开始播放
	private RadioButton mHardware; //硬件解码
	private RadioButton mSoftware; //软件解码
	private TextView mHardwareReminder; //硬件解码提示语
	private String decodeType = "software";  //解码类型，默认软件解码
	private String mediaType = "livestream"; //媒体类型，默认网络直播
	
	private RadioButton mLocalHardware;
	private RadioButton mLocalSoftware;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mainne);
		
		mMediaOption = (TextView)findViewById(R.id.mediaOption);
		mMediaOption.setGravity(Gravity.CENTER);
		
		mLocalDir = new NELocalDirectory();
		localMediaList = (ListView)findViewById(R.id.local_meida_list);
		localMediaList.setAdapter(mLocalDir);
		localMediaList.setVisibility(View.INVISIBLE);
		
		mBtnLiveStream = (Button)findViewById(R.id.livestreamBtn);
		mBtnVideoOnDemand = (Button)findViewById(R.id.videoOnDemandBtn);
		mBtnLocalVideo = (Button)findViewById(R.id.localVideoBtn);
		mBtnLocalAudio = (Button)findViewById(R.id.localAudioBtn);
		
		mMediaTypeSelected = (ImageView)findViewById(R.id.mediaTypeSelected);
		
		mEditURL          = (EditText)findViewById(R.id.netVideoUrl);
		mBtnPlay          = (Button)findViewById(R.id.play_button);
		
		mHardware         = (RadioButton)findViewById(R.id.hardware);
		mSoftware         = (RadioButton)findViewById(R.id.software);
		
		mSoftware.setButtonDrawable(R.drawable.decode_type_selected);
		mHardware.setButtonDrawable(R.drawable.decode_type_unselected);
		mHardwareReminder = (TextView)findViewById(R.id.hardware_reminder);
		
		mLocalSoftware  = (RadioButton)findViewById(R.id.l_software);
		mLocalHardware  = (RadioButton)findViewById(R.id.l_hardware); 
		
		mLocalSoftware.setButtonDrawable(R.drawable.decode_type_selected);
		mLocalHardware.setButtonDrawable(R.drawable.decode_type_unselected);
		
		mLocalSoftware.setVisibility(View.INVISIBLE);
		mLocalHardware.setVisibility(View.INVISIBLE);
		
		DisplayMetrics dm = new DisplayMetrics(); //获取屏幕分辨率
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		final int screenW = dm.widthPixels;
		final int screenH = dm.heightPixels;
		
		Log.d(TAG, "screenW = " + screenW);
		Log.d(TAG, "screenH = " + screenH);
		
		final int buttonWidth = screenW / 4;
		mBtnLiveStream.setWidth(buttonWidth);
		mBtnVideoOnDemand.setWidth(buttonWidth);
		mBtnLocalVideo.setWidth(buttonWidth);
		mBtnLocalAudio.setWidth(buttonWidth);
		
		LayoutParams params = mMediaTypeSelected.getLayoutParams();
		params.width = screenW / 4;
		mMediaTypeSelected.setLayoutParams(params);
		
		mOnClickEvent = new OnClickListener() { 

			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {

				Intent intent = new Intent(NEMainActivity.this, NEVideoPlayerActivity.class);
				
				switch (v.getId()) {
				case R.id.livestreamBtn: 
					mBtnLiveStream.setEnabled(false);
					mBtnVideoOnDemand.setEnabled(true);
					mBtnLocalVideo.setEnabled(true);
					mBtnLocalAudio.setEnabled(true);
					
					mMediaTypeSelected.setX((float) 0.0);
					
					mEditURL.setVisibility(View.VISIBLE);
					mEditURL.setHint("请输入直播流地址：URL");
					localMediaList.setVisibility(View.INVISIBLE);
					mHardware.setVisibility(View.VISIBLE);
					mSoftware.setVisibility(View.VISIBLE);
					mLocalHardware.setVisibility(View.INVISIBLE);
					mLocalSoftware.setVisibility(View.INVISIBLE);
					mHardwareReminder.setVisibility(View.VISIBLE);
					mBtnPlay.setVisibility(View.VISIBLE);
					mediaType = "livestream";
					break;
				
				case R.id.videoOnDemandBtn:
					mBtnLiveStream.setEnabled(true);
					mBtnVideoOnDemand.setEnabled(false);
					mBtnLocalVideo.setEnabled(true);
					mBtnLocalAudio.setEnabled(true);
					
					mMediaTypeSelected.setX((float)buttonWidth);
					
					mEditURL.setVisibility(View.VISIBLE);
					mEditURL.setHint("请输入点播流地址：URL");
					localMediaList.setVisibility(View.INVISIBLE);
					mHardware.setVisibility(View.VISIBLE);
					mSoftware.setVisibility(View.VISIBLE);
					mLocalHardware.setVisibility(View.INVISIBLE);
					mLocalSoftware.setVisibility(View.INVISIBLE);
					mHardwareReminder.setVisibility(View.VISIBLE);
					mBtnPlay.setVisibility(View.VISIBLE);
					mediaType = "videoondemand";
					break;
				
				case R.id.localVideoBtn:
					mBtnLiveStream.setEnabled(true);
					mBtnVideoOnDemand.setEnabled(true);
					mBtnLocalVideo.setEnabled(false);
					mBtnLocalAudio.setEnabled(true);
					
					mMediaTypeSelected.setX((float)buttonWidth * 2);
				
					mLocalDir.setAudioMode(false);
					mLocalDir.refresh();
					localMediaList.setVisibility(View.VISIBLE);
					mEditURL.setVisibility(View.INVISIBLE);
					mHardware.setVisibility(View.INVISIBLE);
					mSoftware.setVisibility(View.INVISIBLE);
					mLocalHardware.setVisibility(View.VISIBLE);
					mLocalSoftware.setVisibility(View.VISIBLE);
					mHardwareReminder.setVisibility(View.VISIBLE);
					mBtnPlay.setVisibility(View.INVISIBLE);
	
					mediaType = "localvideo";
					break;
					
				case R.id.localAudioBtn:
					mBtnLiveStream.setEnabled(true);
					mBtnVideoOnDemand.setEnabled(true);
					mBtnLocalVideo.setEnabled(true);
					mBtnLocalAudio.setEnabled(false);
					
					mMediaTypeSelected.setX((float)buttonWidth * 3);
				
					mLocalDir.setAudioMode(true);
					mLocalDir.refresh();
					localMediaList.setVisibility(View.VISIBLE);
					mEditURL.setVisibility(View.INVISIBLE);
					mHardware.setVisibility(View.INVISIBLE);
					mSoftware.setVisibility(View.INVISIBLE);
					mLocalHardware.setVisibility(View.INVISIBLE);
					mLocalSoftware.setVisibility(View.INVISIBLE);
					mHardwareReminder.setVisibility(View.INVISIBLE);
					mBtnPlay.setVisibility(View.INVISIBLE);
					//decodeType = "software";
					mediaType = "localaudio";
					
					break;
					
					case R.id.hardware:
						mSoftware.setButtonDrawable(R.drawable.decode_type_unselected);
						mHardware.setButtonDrawable(R.drawable.decode_type_selected);
						mLocalSoftware.setButtonDrawable(R.drawable.decode_type_unselected);
						mLocalHardware.setButtonDrawable(R.drawable.decode_type_selected);
						decodeType = "hardware";
						break;
					
					case R.id.software:
						mSoftware.setButtonDrawable(R.drawable.decode_type_selected);
						mHardware.setButtonDrawable(R.drawable.decode_type_unselected);
						mLocalSoftware.setButtonDrawable(R.drawable.decode_type_selected);
						mLocalHardware.setButtonDrawable(R.drawable.decode_type_unselected);
						decodeType = "software";
						break;
						
					case R.id.l_hardware:
						mLocalSoftware.setButtonDrawable(R.drawable.decode_type_unselected);
						mLocalHardware.setButtonDrawable(R.drawable.decode_type_selected);
						mSoftware.setButtonDrawable(R.drawable.decode_type_unselected);
						mHardware.setButtonDrawable(R.drawable.decode_type_selected);
						decodeType = "hardware";
						break;
						
					case R.id.l_software:
						mLocalSoftware.setButtonDrawable(R.drawable.decode_type_selected);
						mLocalHardware.setButtonDrawable(R.drawable.decode_type_unselected);
						mSoftware.setButtonDrawable(R.drawable.decode_type_selected);
						mHardware.setButtonDrawable(R.drawable.decode_type_unselected);
						decodeType = "software";
						break;
						
					case R.id.play_button:
						String url = mEditURL.getText().toString();
						Log.d(TAG, "url = "+ url);
						Log.d(TAG, "decode_type = "+ decodeType);
						
						if ((mediaType.equals("livestream") && url.isEmpty()) || (mediaType.equals("videoondemand") && url.isEmpty())) {
							AlertDialogBuild(0);
							break;
						}
//						else if ((mediaType.equals("livestream") && !Uri.parse(url).getScheme().equalsIgnoreCase("rtmp")) || 
//								(mediaType.equals("videoondemand") && !Uri.parse(url).getScheme().equalsIgnoreCase("http"))) {
//							AlertDialogBuild(1);
//							break;
//						}
				
					//把多个参数传给NEVideoPlayerActivity
					intent.putExtra("media_type", mediaType);
					intent.putExtra("decode_type", decodeType);
					intent.putExtra("videoPath", url);
					startActivity(intent);
					break;
				}	
			}
		};
		
		mBtnLiveStream.setOnClickListener(mOnClickEvent);
		mBtnVideoOnDemand.setOnClickListener(mOnClickEvent);
		mBtnLocalVideo.setOnClickListener(mOnClickEvent);
		mBtnLocalAudio.setOnClickListener(mOnClickEvent);
		mHardware.setOnClickListener(mOnClickEvent);
		mSoftware.setOnClickListener(mOnClickEvent);
		mBtnPlay.setOnClickListener(mOnClickEvent);
		mLocalHardware.setOnClickListener(mOnClickEvent);
		mLocalSoftware.setOnClickListener(mOnClickEvent);
		
		//用于监听本地文件列表的按键响应
		localMediaList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(NEMainActivity.this, NEVideoPlayerActivity.class);
				intent.putExtra("media_type", mediaType);
				intent.putExtra("decode_type", decodeType);
				intent.putExtra("videoPath", (String) mLocalDir.getItem(position));
				startActivity(intent);
			}
		});
	}
	
	public void AlertDialogBuild(int flag) //创建对话框
	{
		AlertDialog alertDialog;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("注意");
        if (flag == 0) {
	        if (mediaType.equals("livestream")) {
	        	alertDialogBuilder.setMessage("请输入直播流地址");
	        }
	        else if (mediaType.equals("videoondemand")) {
	        	alertDialogBuilder.setMessage("请输入点播流地址");
	        }
        }
        else if(flag == 1) {
        	if (mediaType.equals("livestream")) {
	        	alertDialogBuilder.setMessage("请输入正确的直播流地址");
	        }
	        else if (mediaType.equals("videoondemand")) {
	        	alertDialogBuilder.setMessage("请输入正确的点播流地址");
	        }
        }
        alertDialogBuilder.setCancelable(false)
        
            .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    ;
                }
            });
        alertDialog = alertDialogBuilder.create();
        alertDialog.show();
	}
	
	@Override
	public void onPause() {
		Log.d(TAG, "on pause");
		super.onPause();
	}
	
	@Override
	public void onDestroy() {
		Log.d(TAG, "on destroy");
		super.onDestroy();
	}
	
	@Override
	public void onRestart() {
		Log.d(TAG, "on restart");
		super.onRestart();
	}
	
	@Override
	public void onResume() {
		Log.d(TAG, "on resmue");
		super.onResume();
	}

	@Override
	public void onStart() {
		Log.d(TAG, "on start");
		super.onStart();
	}
		
}
