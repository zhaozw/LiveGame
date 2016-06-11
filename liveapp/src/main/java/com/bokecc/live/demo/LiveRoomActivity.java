package com.bokecc.live.demo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import org.json.JSONException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.bokecc.live.adapter.MyChatListViewAdapter;
import com.bokecc.live.adapter.MyGridViewAdapter;
import com.bokecc.live.adapter.MyQAListViewAdapter;
import com.bokecc.live.pojo.QAMsg;
import com.bokecc.live.view.BarrageLayout;
import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.DWLive.PlayMode;
import com.bokecc.sdk.mobile.live.DWLiveListener;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.pojo.Answer;
import com.bokecc.sdk.mobile.live.pojo.ChatMessage;
import com.bokecc.sdk.mobile.live.pojo.Question;
import com.bokecc.sdk.mobile.live.pojo.Viewer;
import com.bokecc.sdk.mobile.live.util.HttpUtil;
import com.bokecc.sdk.mobile.live.widget.DocView;

public class LiveRoomActivity extends FragmentActivity implements SurfaceHolder.Callback, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnBufferingUpdateListener, View.OnClickListener, IMediaPlayer.OnCompletionListener {

	private IjkMediaPlayer player;
    private SurfaceView sv;
    private SurfaceHolder holder;
    private DocView docView;
    private DWLive dwLive;
    private Switch swi;
    private ImageButton sendMsgBtn; 
    private Button btnFullScreen, changeSource, changeSoundVideo; 
    private ImageButton sendQABtn;
    private EditText etMsg, etQA;
    private TextView tvCount;
    private ListView lvChat, lvQA;
    private MyChatListViewAdapter chatAdapter;
    private MyQAListViewAdapter qaAdapter;
    private List<ChatMessage> chatMsgs = new ArrayList<ChatMessage>();
    private RelativeLayout rlPlay;
    private final int PUBLIC_MSG = 0;
    private final int PRIVATE_QUESTION_MSG = 1;
    private final int PRIVATE_ANSWER_MSG = 2;
    private final int QUESTION = 10;
    private final int ANSWER = 11;
    private final int USER_COUNT = 20;
    private final int FINISH = 40;
    private final int KICK_OUT = -1;
    
    private boolean isSendPublicChatMsg = false;
    
    private int playSourceCount = 0;
    
    private int sourceChangeCount = 0;

    private boolean isStop = false;
    
	private LinearLayout llFullscreen;
	private EditText etFullscreen;
	private Button btnFullscreenSendMsg;
	private BarrageLayout mBarrageLayout;
	private RadioButton rbChat, rbPic, rbQa;
	private RadioGroup rgTitle;
	private List<RadioButton> rbs = new ArrayList<RadioButton>();
	
	private LinkedHashMap<String, QAMsg> qaMap = new LinkedHashMap<String, QAMsg>();
	private boolean isKickOut = false;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PUBLIC_MSG:
                    ChatMessage publicMsg = (ChatMessage)msg.obj;
                    
                    if (mBarrageLayout.getVisibility() == View.VISIBLE) {
                    	mBarrageLayout.addNewInfo(publicMsg.getMessage());
                    }
                    
                    chatMsgs.add(publicMsg);
                    chatAdapter.notifyDataSetChanged();
                    lvChat.setSelection(chatMsgs.size() - 1);
                    break;
                case PRIVATE_QUESTION_MSG:
                case PRIVATE_ANSWER_MSG:
					ChatMessage privateMsg = (ChatMessage)msg.obj;
					chatMsgs.add(privateMsg);
					chatAdapter.notifyDataSetChanged();
					lvChat.setSelection(chatMsgs.size() - 1);
                    break;
                case QUESTION:
                	Question question = (Question) msg.obj;
                	String questionId = question.getId();
                	if (!qaMap.containsKey(questionId)) {
                		QAMsg qaMsg = new QAMsg();
                    	qaMsg.setQuestion(question);
                    	qaMap.put(questionId, qaMsg);
                    	qaAdapter.notifyDataSetChanged();
                    	lvQA.setSelection(qaMap.size() - 1);
                	}
                    break;
                case ANSWER:
                	Answer answer = (Answer) msg.obj;
                	String qaId = answer.getQuestionId();
                	int indexQa = new ArrayList<String>(qaMap.keySet()).indexOf(qaId);
                	if (indexQa == -1) {
                		return; //没有收到answer对应的问题，直接返回
                	}
                	QAMsg qaMsg = qaMap.get(qaId);
                	qaMsg.setAnswer(answer);
                	qaAdapter.notifyDataSetChanged();
                	lvQA.setSelection(indexQa);
                    break;
                case USER_COUNT:
                    tvCount.setText("在线：" + (Integer)msg.obj + "人");
                    break;
                case FINISH:
                	setHolderBlack("直播结束");
                	break;
                case KICK_OUT:
                	isKickOut = true;
                	Toast.makeText(getApplicationContext(), "已被踢出", Toast.LENGTH_SHORT).show();
        			finish();
                	break;
                default:
                	break;
            }

        }
    };
    
    private GridView gvFace;
    private void initFace(View view) {
    	gvFace = (GridView) view.findViewById(R.id.gv_face);
    	gvFace.setAdapter(new MyGridViewAdapter(this, etMsg));
    }
    
    private List<View> pagerViewList = new ArrayList<View>();
    
    private void initPagerItemView() {
    	LayoutInflater inflater = LayoutInflater.from(this);
    	
    	View chatView = inflater.inflate(R.layout.chat_layout, null);
    	pagerViewList.add(chatView);
    	initChatLayout(chatView);
    	
    	View picView = inflater.inflate(R.layout.pic_layout, null);
    	pagerViewList.add(picView);
    	initPicLayout(picView);
    	
    	View qaView = inflater.inflate(R.layout.qa_layout, null);
    	pagerViewList.add(qaView);
    	initQaLayout(qaView);
    }
    
    private void initChatLayout(View view) {
    	swi = (Switch) view.findViewById(R.id.swi);
    	swi.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					isSendPublicChatMsg = true;
				} else {
					isSendPublicChatMsg = false;
				}
			}
		});
    	swi.performClick();
    	
        sendMsgBtn = (ImageButton) view.findViewById(R.id.btn_msg);
        sendMsgBtn.setOnClickListener(LiveRoomActivity.this);
        
        lvChat = (ListView) view.findViewById(R.id.lv_chat);

        etMsg = (EditText) view.findViewById(R.id.et_msg);
        etMsg.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_SEND) {
					sendChatMsg(true);
				}
				return false;
			}
		});
        
        etMsg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showNormalKeyBoard();
			}
		});
        
        initFace(view);
        initSmileKeyboard(view);
    }
    
    private ImageView ivSmile, ivKeyBoard;
    private void initSmileKeyboard(View view) {
    	ivSmile = (ImageView) view.findViewById(R.id.iv_smile);
    	ivSmile.setOnClickListener(this);
    	ivKeyBoard = (ImageView) view.findViewById(R.id.iv_keyboard);
    	ivKeyBoard.setOnClickListener(this);
    }
    
    private void initLvChat() {
    	chatAdapter = new MyChatListViewAdapter(this, viewer, chatMsgs);
    	lvChat.setAdapter(chatAdapter);
    }
    
    private void hideEditTextSoftInput(EditText editText) {
    	 imm.hideSoftInputFromWindow(editText.getWindowToken(), 0); 
    }
    
    private void hideKeyBoardEditTextSoftInput() {
    	imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
   }
    
    private void hideFocusKeyBoard() {
    	View view = getCurrentFocus();
    	if (view != null) {
    		IBinder binder = view.getWindowToken();
        	if (binder != null) {
        		imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        	}
    	}
    }
    
    private void showBottomEditTextSoftInput() {
    	imm.showSoftInput(etMsg, 0); 
    }
    
    private void initPicLayout(View view) {
    	docView = (DocView) view.findViewById(R.id.live_docView);
    }
    
    private void initQaLayout(View view) {
      sendQABtn = (ImageButton) view.findViewById(R.id.btn_qa);
      sendQABtn.setOnClickListener(LiveRoomActivity.this);
      
      lvQA = (ListView) view.findViewById(R.id.lv_qa);
      etQA = (EditText) view.findViewById(R.id.et_qa);
    }
    
    private void initLvQa() {
    	qaAdapter = new MyQAListViewAdapter(this, viewer, qaMap);
    	lvQA.setAdapter(qaAdapter);
    }
    
    
    private void initPager() {
    	
    	mPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int arg0) {
				rbs.get(arg0).setChecked(true);
				hideFocusKeyBoard();
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
				
			}
		});
        
        mPager.setAdapter(new PagerAdapter() {
			
			@Override
			public void destroyItem(ViewGroup container, int position, Object object) {
				
				container.removeView(pagerViewList.get(position));
			}

			@Override
			public int getItemPosition(Object object) {
				return super.getItemPosition(object);
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				View view = pagerViewList.get(position);
				container.addView(view);
				if (position == 0) {
					etMsg.requestFocus();
				}
				return view;
			}

			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}
			
			@Override
			public int getCount() {
				return pagerViewList.size();
			}
		});
    }
    
    private Viewer viewer;
    private ViewPager mPager;
    private InputMethodManager imm;
    private WindowManager wm;
    private String chatStr, pdfStr, qaStr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.room_live);
        
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        chatStr = bundle.getString("chat");
        pdfStr = bundle.getString("pdf");
        qaStr = bundle.getString("qa");
        
        init();
        initPagerItemView();
        initPlayer();
        initDwLive();
        initRoomShow();
        HttpUtil.LOG_LEVEL = HttpUtil.HttpLogLevel.DETAIL;
    }
    
    private void initRoomShow() {
    	if (!"1".equals(qaStr)) {
    		pagerViewList.remove(2);
    		rbs.remove(2);
    		rbQa.setVisibility(View.GONE);
    	}
    	if (!"1".equals(pdfStr)) {
    		pagerViewList.remove(1);
    		rbs.remove(1);
    		rbPic.setVisibility(View.GONE);
    	}
    	if (!"1".equals(chatStr)) {
    		pagerViewList.remove(0);
    		rbs.remove(0);
    		rbChat.setVisibility(View.GONE);
    	}
    	if (rbs.size() > 0) {
    		rgTitle.setVisibility(View.VISIBLE);
    		rbs.get(0).setChecked(true);
    	} else {
    		// TODO 如果没有的话，直接进入全屏模式
    	}
    	initPager();
    	initLvChat();
    	initLvQa();
    }

	private void initDwLive() {
        dwLive = DWLive.getInstance();
        dwLive.setDWLivePlayParams(dwLiveListener, docView, player);
        viewer = dwLive.getViewer();
	}
	
	private LinearLayout llBottomLayout;
	private TextView tvPlayMsg;
	private ImageView ivBack;
	private RelativeLayout rlPlayTop;
    private void init() {
    	rlPlayTop = (RelativeLayout) findViewById(R.id.rl_play_top);
    	ivBack = (ImageView) findViewById(R.id.iv_back);
    	ivBack.setOnClickListener(this);
    	llBottomLayout = (LinearLayout) findViewById(R.id.ll_bottom_layout);
    	tvPlayMsg = (TextView) findViewById(R.id.tv_play_msg);
    	mPager = (ViewPager) findViewById(R.id.pager);
    	
    	llFullscreen = (LinearLayout) findViewById(R.id.ll_fullscreen_msg_send);
    	
    	etFullscreen = (EditText) findViewById(R.id.et_fullscreen);
    	etFullscreen.setOnClickListener(this);
    	btnFullscreenSendMsg = (Button) findViewById(R.id.btn_fullscreen_send);
    	btnFullscreenSendMsg.setOnClickListener(this);
    	
    	mBarrageLayout = (BarrageLayout) findViewById(R.id.bl_barrage);
    	
    	tvCount = (TextView) findViewById(R.id.tv_count);
        
        btnFullScreen = (Button) findViewById(R.id.full_screen);
        btnFullScreen.setOnClickListener(this);
    	rlPlay = (RelativeLayout) findViewById(R.id.rl_play);
    	setRelativeLayoutPlay(true);
    	rlPlay.setClickable(true);
    	rlPlay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (isPrepared) {
					if (btnFullScreen.getVisibility() == View.VISIBLE) {
						hideEditTextSoftInput(etFullscreen);
						handler.removeCallbacks(playHideRunnable);
						setPlayControllerVisible(false);
					} else {
						setPlayControllerVisible(true);
						hidePlayHander();
					}
				}
			}
		});
      
    	changeSoundVideo = (Button) findViewById(R.id.sound_video);
    	changeSoundVideo.setOnClickListener(this);

    	changeSource = (Button) findViewById(R.id.play_source_change);
    	changeSource.setOnClickListener(this);

        sv = (SurfaceView) findViewById(R.id.sv);
        holder = sv.getHolder();
        holder.addCallback(this);
        
        rgTitle = (RadioGroup) findViewById(R.id.rg_title);
        rgTitle.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch(checkedId) {
				case R.id.rb_chat:
					mPager.setCurrentItem(0);
					break;
				case R.id.rb_pic:
					int indexPic = rbs.indexOf(rbPic);
					mPager.setCurrentItem(indexPic);
					break;
				case R.id.rb_qa:
					int indexQa = rbs.indexOf(rbQa);
					mPager.setCurrentItem(indexQa);
					break;
				}
			}
		});
        
        rbChat = (RadioButton) findViewById(R.id.rb_chat);
        rbPic = (RadioButton) findViewById(R.id.rb_pic);
        rbQa = (RadioButton) findViewById(R.id.rb_qa);
        rbs.add(rbChat);
        rbs.add(rbPic);
        rbs.add(rbQa);
    }
    
    private Runnable playHideRunnable = new Runnable() {
		
		@Override
		public void run() {
			setPlayControllerVisible(false);
		}
	};
	
	private void setPlayControllerVisible(boolean isVisible) {
		int visibility = 0;
		if (isVisible) {
			visibility = View.VISIBLE;
		} else {
			visibility = View.INVISIBLE;
		}
		
		if (!isPortrait() && "1".equals(chatStr)) {
			llFullscreen.setVisibility(visibility);
			etFullscreen.requestFocus();
		}
		btnFullScreen.setVisibility(visibility);
		tvCount.setVisibility(visibility);
		rlPlayTop.setVisibility(visibility);
	}
	
	private boolean isPortrait() {
		int mOrientation = getApplicationContext().getResources().getConfiguration().orientation;
		if ( mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
			return false;
		} else{
			return true;
		}
	}
    
    private void hidePlayHander() {
    	handler.removeCallbacks(playHideRunnable);
    	handler.postDelayed(playHideRunnable, 5000);
    }
    
    private void initPlayer() {
    	player = new IjkMediaPlayer();
        player.setOnPreparedListener(this);
        player.setOnVideoSizeChangedListener(this);
        player.setOnErrorListener(this);
        player.setOnBufferingUpdateListener(this);
        player.setOnCompletionListener(this);
    }

    @Override
    protected void onDestroy() {
    	dwLive.stop();
        handler.removeCallbacks(playHideRunnable);
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
    	if (dwLive != null) {
    		dwLive.start(holder);
    	}
        player.setScreenOnWhilePlaying(true);
        this.holder = surfaceHolder;
        setHolderBlack("请稍候……");
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
        Log.i("demo", "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.i("demo", "surfaceDestroyed");
    }

    private boolean isPrepared = false;
    @Override
    public void onPrepared(IMediaPlayer mp) {
        Log.i("demo", "onPrepared");
        isPrepared = true;
        
        tvPlayMsg.setVisibility(View.GONE);
        
        setPlayControllerVisible(true);
		hidePlayHander();
        
        llBottomLayout.setVisibility(View.VISIBLE);
        
        player.start();
    }

    
    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
        Log.i("demo", "onVideoSizeChanged"+"width"+width+"height"+height);
        if (width == 0 || height == 0) {
        	setHolderBlack("音频播放中……");
        	return;
        } 
        tvPlayMsg.setVisibility(View.GONE);
        sv.setLayoutParams(getScreenSizeParams());
    }
    
    private void setHolderBlack(String text) {
    	SurfaceHolder mHolder = sv.getHolder();
    	Canvas canvas = mHolder.lockCanvas();
    	canvas.drawColor(Color.BLACK);
    	mHolder.unlockCanvasAndPost(canvas);
    	
    	tvPlayMsg.setVisibility(View.VISIBLE);
    	tvPlayMsg.setText(text);
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
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		return params;
	}


    @Override
    public boolean onError(IMediaPlayer mp, int what, int extra) {
        Log.i("demo", "player onError");
        return false;
    }

    @Override
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {}

    private DWLiveListener dwLiveListener = new DWLiveListener() {
        @Override
        public void onQuestion(Question question) {
            Log.i("demo", "onQuestion:" + question.toString());
            Message msg = new Message();
            msg.what = QUESTION;
            msg.obj = question;
            handler.sendMessage(msg);
        }

        @Override
        public void onAnswer(Answer answer) {
            Log.i("demo", "onAnswer:" + answer.toString());
            Message msg = new Message();
            msg.what = ANSWER;
            msg.obj = answer;
            handler.sendMessage(msg);
        }

        @Override
        public void onLiveStatus(DWLive.PlayStatus status) {
            Log.i("demo", "onLiveStatusChange:" + status);
            switch(status) {
                case PLAYING:
                    isStop = false;
                    break;
                case PREPARING:
                    isStop = true;
            }
        }

        @Override
        public void onPublicChatMessage(ChatMessage msg) {
            Log.i("demo", "onPublicChatMessage:" + msg);
            Message handlerMsg = new Message();
            handlerMsg.what = PUBLIC_MSG;
            handlerMsg.obj = msg;
            handler.sendMessage(handlerMsg);
        }

        @Override
        public void onPrivateQuestionChatMessage(ChatMessage msg) {
            Log.i("demo", "onPrivateQuestionChatMessage:" + msg);
            Message handlerMsg = new Message();
            handlerMsg.what = PRIVATE_QUESTION_MSG;
            handlerMsg.obj = msg;
            handler.sendMessage(handlerMsg);
        }

        @Override
        public void onPrivateAnswerChatMessage(ChatMessage msg) {
            Log.i("demo", "onPrivateAnswerChatMessage:" + msg);
            Message handlerMsg = new Message();
            handlerMsg.what = PRIVATE_ANSWER_MSG;
            handlerMsg.obj = msg;
            handler.sendMessage(handlerMsg);
        }

        @Override
        public void onUserCountMessage(int count) {
            Message msg = new Message();
            msg.what = USER_COUNT;
            msg.obj = count;
            handler.sendMessage(msg);
        }

        @Override
        public void onNotification(String msg) {
            Log.i("demo", "onNotification:" + msg);
        }

        @Override
        public void onInformation(String msg) {
            Log.i("demo", "information:" + msg);
        }

        @Override
        public void onException(DWLiveException exception) {
        	Log.e("demo", exception.getMessage() + "");
        }

        @Override
        public void onInitFinished(int playSourceCount) {
        	LiveRoomActivity.this.playSourceCount = playSourceCount;
        }

		@Override
		public void onSilenceUserChatMessage(ChatMessage msg) {
			Log.i("demo", "onSilenceUserChatMessage:" + msg);
            Message handlerMsg = new Message();
            handlerMsg.what = PUBLIC_MSG; //收到禁言消息，作为公有消息展示出去，也可以不展示
            handlerMsg.obj = msg;
            handler.sendMessage(handlerMsg);
		}

		@Override
		public void onKickOut() {
			Message kickOutMsg = new Message();
            kickOutMsg.what = KICK_OUT;
            handler.sendMessage(kickOutMsg);
		}

		@Override
		public void onStreamEnd(boolean isNormal) {
			isStop = true;
			isPrepared = false;
			Message msg = new Message();
			msg.what = FINISH;
			handler.sendMessage(msg);
		}
    };

    
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
        	case R.id.iv_back:
        		if (isPortrait()) {
        			finish();
        		} else {
        			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        		}
            case R.id.btn_msg:
                sendChatMsg(false);
                break;
            case R.id.btn_qa:
                String qaMsg = etQA.getText().toString().trim();
                if (!"".equals(qaMsg)) {
                    try {
                        dwLive.sendQuestionMsg(qaMsg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                etQA.setText("");
                hideEditTextSoftInput(etQA);
                break;
            case R.id.full_screen:
            	if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            		hideEditTextSoftInput(etFullscreen);
            	} else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            		hideEditTextSoftInput(etMsg);
            		gvFace.setVisibility(View.GONE);
                	ivSmile.setVisibility(View.VISIBLE);
                	ivKeyBoard.setVisibility(View.GONE);
            	}
            	break;
            case R.id.play_source_change:
            	if (playSourceCount <= 1) {
            		break;
            	}
            	dwLive.changePlaySource((++sourceChangeCount) % playSourceCount);
            	changeSource.setText("切换线路("+ ((sourceChangeCount + 1) % playSourceCount + 1) + ")" );
            	break;
            case R.id.sound_video:
            	if ("切换模式(音频)".equals(changeSoundVideo.getText())) {
            		dwLive.changePlayMode(PlayMode.SOUND);
            		changeSoundVideo.setText("切换模式(视频)");
            	} else {
            		dwLive.changePlayMode(PlayMode.VIDEO);
            		changeSoundVideo.setText("切换模式(音频)");
            	}
            	break;
            case R.id.btn_fullscreen_send:
            	String info = etFullscreen.getText().toString().trim();
            	if (!"".equals(info)) {
            		dwLive.sendPublicChatMsg(info);
            		etFullscreen.setText("");
            	}
            	hideEditTextSoftInput(etFullscreen);
            	hidePlayHander();
            	break;
            case R.id.et_fullscreen:
            	handler.removeCallbacks(playHideRunnable);
            	break;
            case R.id.iv_smile:
            	showSmileKeyboard();
            	break;
            case R.id.iv_keyboard:
            	showNormalKeyBoard();
            	break;
            default:
            	break;
        }
    }
    
    private void showNormalKeyBoard() {
    	gvFace.setVisibility(View.GONE);
    	ivSmile.setVisibility(View.VISIBLE);
    	ivKeyBoard.setVisibility(View.GONE);
    	showBottomEditTextSoftInput();
    }
    
    private void showSmileKeyboard() {
    	hideEditTextSoftInput(etMsg);
    	etMsg.requestFocus();
    	ivSmile.setVisibility(View.GONE);
    	ivKeyBoard.setVisibility(View.VISIBLE);
    	gvFace.setVisibility(View.VISIBLE);
    }
    
    private void sendChatMsg(boolean isKeyboard) {
    	String msg = etMsg.getText().toString().trim();
        if (!"".equals(msg)) {
        	if (isSendPublicChatMsg) {
        		dwLive.sendPublicChatMsg(msg);
        	} else {
        		dwLive.sendPrivateChatMsg(msg);
        	}
        }
        etMsg.setText("");
        gvFace.setVisibility(View.GONE);
    	ivSmile.setVisibility(View.VISIBLE);
    	ivKeyBoard.setVisibility(View.GONE);
    	if (isKeyboard) {
    		hideKeyBoardEditTextSoftInput(); // TODO 为啥呢
    	} else {
    		hideEditTextSoftInput(etMsg);
    	}
    }

    private boolean isOnPause = false;
    @Override
    protected void onPause() {
    	qaMap.clear();
        dwLive.stop();
        isPrepared = false;
        isOnPause = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isOnPause) {
        	dwLive.start(holder);
        	isOnPause = false;
        }
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        if (dwLive != null && !isStop) {
        	qaMap.clear();
            dwLive.stop();
            dwLive.start(holder);
        }
    }
    
    @Override
   	public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
   		if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
           	getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
           	setRelativeLayoutPlay(true);           	
           	llFullscreen.setVisibility(View.GONE);
           	mBarrageLayout.stop();
           	mBarrageLayout.setVisibility(View.GONE);
           	btnFullScreen.setBackgroundResource(R.drawable.fullscreen_close);
           	
           	
   		} else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
           	getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
           	setRelativeLayoutPlay(false);
//           	llFullscreen.setVisibility(View.VISIBLE);
        	mBarrageLayout.start();
           	mBarrageLayout.setVisibility(View.VISIBLE);
//           	etFullscreen.requestFocus();
           	btnFullScreen.setBackgroundResource(R.drawable.fullscreen_open);
   		}
   		sv.setLayoutParams(getScreenSizeParams());
   		handler.removeCallbacks(playHideRunnable);
   		setPlayControllerVisible(true);
   		hidePlayHander();
   	}
    
    @Override
	public void onBackPressed() {
		if (isPortrait()) {
			if (gvFace.getVisibility() == View.VISIBLE) {
				gvFace.setVisibility(View.GONE);
		    	ivSmile.setVisibility(View.VISIBLE);
		    	ivKeyBoard.setVisibility(View.GONE);
			} else {
				super.onBackPressed();
			}
		} else {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
	}

    private void setRelativeLayoutPlay(boolean isPortraitOrien) {
    	int width = wm.getDefaultDisplay().getWidth();
		int	height = wm.getDefaultDisplay().getHeight();
		LinearLayout.LayoutParams layoutParams = null;
		if (isPortraitOrien) {
			layoutParams = new LinearLayout.LayoutParams(width, height / 3);
		} else {
			layoutParams = new LinearLayout.LayoutParams(width, height);
		}
       	rlPlay.setLayoutParams(layoutParams);
    }
}
