package com.vipheyue.livegame.fragment;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.netease.neliveplayer.NEMediaPlayer;
import com.vipheyue.livegame.R;
import com.vipheyue.livegame.neliveplayer.NEMediaController;
import com.vipheyue.livegame.neliveplayer.NEVideoView;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class PlayFragment extends Fragment {
    public NEVideoView mVideoView;  //用于画面显示
    private View mBuffer; //用于指示缓冲状态
    private NEMediaController mMediaController; //用于控制播放

    public static final int NELP_LOG_UNKNOWN = 0; //!< log输出模式：输出详细
    public static final int NELP_LOG_DEFAULT = 1; //!< log输出模式：输出详细
    public static final int NELP_LOG_VERBOSE = 2; //!< log输出模式：输出详细
    public static final int NELP_LOG_DEBUG = 3; //!< log输出模式：输出调试信息
    public static final int NELP_LOG_INFO = 4; //!< log输出模式：输出标准信息
    public static final int NELP_LOG_WARN = 5; //!< log输出模式：输出警告
    public static final int NELP_LOG_ERROR = 6; //!< log输出模式：输出错误
    public static final int NELP_LOG_FATAL = 7; //!< log输出模式：一些错误信息，如头文件找不到，非法参数使用
    public static final int NELP_LOG_SILENT = 8; //!< log输出模式：不输出

    private String mVideoPath; //文件路径
    private String mDecodeType;//解码类型，硬解或软解
    private String mMediaType; //媒体类型
    private boolean mHardware = true;
    private ImageView mAudioRemind; //播音频文件时提示
    private String mTitle;
    private Uri mUri;
    private boolean pauseInBackgroud = true;


    NEMediaPlayer mMediaPlayer = new NEMediaPlayer();

    public PlayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_play, container, false);
        initPlayer(view);
        return view;
    }

    private void initPlayer(View view) {
        //接收MainActivity传过来的参数
        mMediaType = "livestream";
        mDecodeType =  "software";
        mVideoPath = "rtmp://v1.live.126.net/live/4c8a4ae25686439b9de048ec75e23c76";
            mHardware = false;

        mUri = Uri.parse(mVideoPath);
        if (mUri != null) { //获取文件名，不包括地址
            List<String> paths = mUri.getPathSegments();
            String name = paths == null || paths.isEmpty() ? "null" : paths.get(paths.size() - 1);
        }

        mAudioRemind = (ImageView) view.findViewById(R.id.audio_remind);
        if (mMediaType.equals("localaudio")) {
            mAudioRemind.setVisibility(View.VISIBLE);
            //mAudioRemind.setBackgroundColor(Color.rgb(255, 0, 0));
        } else {
            mAudioRemind.setVisibility(View.INVISIBLE);
        }


        mBuffer = view.findViewById(R.id.buffering_prompt);
        mMediaController = new NEMediaController(getActivity());

        mVideoView = (NEVideoView) view.findViewById(R.id.video_view);

        if (mMediaType.equals("livestream")) {
            mVideoView.setBufferStrategy(0); //直播低延时
        } else {
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

        mMediaController.setOnShownListener(mOnShowListener); //监听mediacontroller是否显示
    }



    NEMediaController.OnShownListener mOnShowListener = new NEMediaController.OnShownListener() {

        @Override
        public void onShown() {
            mVideoView.invalidate();
        }
    };




    @Override
    public void onPause() {
        if (pauseInBackgroud)
            mVideoView.pause(); //锁屏时暂停
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mVideoView.release_resource();
        super.onDestroy();
    }


    @Override
    public void onResume() {
        if (pauseInBackgroud && !mVideoView.isPaused()) {
            mVideoView.start(); //锁屏打开后恢复播放
        }
        super.onResume();
    }

}
