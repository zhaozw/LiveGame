package com.vipheyue.livegame.fragment;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.bokecc.sdk.mobile.live.DWLive;
import com.bokecc.sdk.mobile.live.DWLiveListener;
import com.bokecc.sdk.mobile.live.Exception.DWLiveException;
import com.bokecc.sdk.mobile.live.pojo.Answer;
import com.bokecc.sdk.mobile.live.pojo.ChatMessage;
import com.bokecc.sdk.mobile.live.pojo.Question;
import com.bokecc.sdk.mobile.live.pojo.Viewer;
import com.bokecc.sdk.mobile.live.util.HttpUtil;
import com.bokecc.sdk.mobile.live.widget.DocView;
import com.vipheyue.livegame.R;
import com.vipheyue.livegame.cc.com.bokecc.live.adapter.MyQAListViewAdapter;
import com.vipheyue.livegame.cc.com.bokecc.live.pojo.QAMsg;
import com.vipheyue.livegame.cc.com.bokecc.live.view.BarrageLayout;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class PlayCCFragment extends Fragment implements SurfaceHolder.Callback, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnVideoSizeChangedListener, IMediaPlayer.OnErrorListener, IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnCompletionListener {

    private IjkMediaPlayer player;
    private SurfaceView sv;
    private SurfaceHolder holder;
    private DocView docView;
    private DWLive dwLive;
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
    private View view;

    public PlayCCFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.room_live, container, false);

        wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
        init();
        initPagerItemView();
        initPlayer();
        initDwLive();
        initRoomShow();
        HttpUtil.LOG_LEVEL = HttpUtil.HttpLogLevel.DETAIL;
        return view;
    }
    private boolean isStop = false;
    private BarrageLayout mBarrageLayout;

    private LinkedHashMap<String, QAMsg> qaMap = new LinkedHashMap<String, QAMsg>();
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case PUBLIC_MSG:
                    ChatMessage publicMsg = (ChatMessage) msg.obj;

                    if (mBarrageLayout.getVisibility() == View.VISIBLE) {
                        mBarrageLayout.addNewInfo(publicMsg.getMessage());
                    }

                    chatMsgs.add(publicMsg);
                    break;
                case PRIVATE_QUESTION_MSG:
                case PRIVATE_ANSWER_MSG:
                    ChatMessage privateMsg = (ChatMessage) msg.obj;
                    chatMsgs.add(privateMsg);
                    break;
                case QUESTION:
                    Question question = (Question) msg.obj;
                    String questionId = question.getId();
                    if (!qaMap.containsKey(questionId)) {
                        QAMsg qaMsg = new QAMsg();
                        qaMsg.setQuestion(question);
                        qaMap.put(questionId, qaMsg);
                        qaAdapter.notifyDataSetChanged();
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
                    break;

                case FINISH:
                    setHolderBlack("直播结束");
                    break;
                default:
                    break;
            }

        }
    };

    private List<View> pagerViewList = new ArrayList<View>();

    private void initPagerItemView() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View picView = inflater.inflate(R.layout.pic_layout, null);
        pagerViewList.add(picView);
        initPicLayout(picView);
    }
    private void initPicLayout(View view) {
        docView = (DocView) view.findViewById(R.id.live_docView);
    }

    private void initLvQa() {
        qaAdapter = new MyQAListViewAdapter(getActivity(), viewer, qaMap);
    }

    private Viewer viewer;
    private WindowManager wm;


    private void initRoomShow() {
        initLvQa();
    }

    private void initDwLive() {
        dwLive = DWLive.getInstance();
        dwLive.setDWLivePlayParams(dwLiveListener, docView, player);
        viewer = dwLive.getViewer();
    }

    private TextView tvPlayMsg;

    private void init() {
        tvPlayMsg = (TextView) view.findViewById(R.id.tv_play_msg);
        mBarrageLayout = (BarrageLayout) view.findViewById(R.id.bl_barrage);
        rlPlay = (RelativeLayout) view.findViewById(R.id.rl_play);
        setRelativeLayoutPlay(true);
        rlPlay.setClickable(true);
        sv = (SurfaceView) view.findViewById(R.id.sv);
        holder = sv.getHolder();
        holder.addCallback(this);
    }


    private boolean isPortrait() {
        int mOrientation = getActivity().getApplicationContext().getResources().getConfiguration().orientation;
        if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        } else {
            return true;
        }
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
    public void onDestroyView() {
        dwLive.stop();
        super.onDestroyView();
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


    @Override
    public void onPrepared(IMediaPlayer mp) {
        Log.i("demo", "onPrepared");
        tvPlayMsg.setVisibility(View.GONE);
        player.start();
    }
    @Override
    public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
        Log.i("demo", "onVideoSizeChanged" + "width" + width + "height" + height);
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
    public void onBufferingUpdate(IMediaPlayer mp, int percent) {
    }

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
            switch (status) {
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
            Message msg = new Message();
            msg.what = FINISH;
            handler.sendMessage(msg);
        }
    };
    private boolean isOnPause = false;

    @Override
    public void onPause() {
        qaMap.clear();
        dwLive.stop();
        isOnPause = true;
        super.onPause();
    }

    @Override
    public void onResume() {
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
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRelativeLayoutPlay(true);
            mBarrageLayout.stop();
            mBarrageLayout.setVisibility(View.GONE);


        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRelativeLayoutPlay(false);
            mBarrageLayout.start();
            mBarrageLayout.setVisibility(View.VISIBLE);
        }
        sv.setLayoutParams(getScreenSizeParams());
    }



    private void setRelativeLayoutPlay(boolean isPortraitOrien) {
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        LinearLayout.LayoutParams layoutParams = null;
        if (isPortraitOrien) {
            layoutParams = new LinearLayout.LayoutParams(width, height / 3);
        } else {
            layoutParams = new LinearLayout.LayoutParams(width, height);
        }
        rlPlay.setLayoutParams(layoutParams);
    }



}