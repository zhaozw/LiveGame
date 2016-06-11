package com.vipheyue.livegame.cc.com.bokecc.live.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;

import com.vipheyue.livegame.cc.com.bokecc.live.util.FaceStringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 弹幕布局类
 * 
 * @author liufh
 *
 */
public class BarrageLayout extends RelativeLayout{
	
	private List<String> infos = new ArrayList<String>(); // 存储收到的弹幕信息
	
	private boolean isStart = false;
	
	private long duration = 7000L;
	
	private Context context;
	
	private int width, height;
	
	private List<BarrageView> bvs = new ArrayList<BarrageView>();
	
	private Timer timer = new Timer();
	
	private TimerTask timerTask;
	
	private Long barrageInterval = 2000L;
	
	private int maxBrragePerShow = 8;
	
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			if (infos.size() > maxBrragePerShow) {
				insertBrrageView(maxBrragePerShow);
			} else if (infos.size() > 0) {
				insertBrrageView(infos.size());
			}
			
			Iterator<BarrageView> iterator = bvs.iterator();
			while (iterator.hasNext()) {
				BarrageView bv = iterator.next();
				if (bv.getFinish()) {
					iterator.remove();
					BarrageLayout.this.removeView(bv);
				}
			}
		}
	};
		
	private void insertBrrageView(int size) {
		Iterator<String> iterator = infos.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			String info = iterator.next();
			addBarrageView(height * i/maxBrragePerShow, duration, info + "");
			iterator.remove();
			if (i == size-1) {
				break;
			}
			i++;
		}
	}
	
	@SuppressLint("NewApi")
	private void addBarrageView(float heightPosition, long duration, String text) {
		BarrageView bv = new BarrageView(context);
		bv.setText(FaceStringUtil.parseFaceMsg(context, text));
		bv.setTextSize(24);
		bv.setTextColor(Color.WHITE);
		bv.setSingleLine(true);
		LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		
		Rect bounds = new Rect();
		TextPaint paint = bv.getPaint();
		paint.getTextBounds(text, 0, text.length(), bounds);
		int paintWidth = bounds.width();
		
		lp.setMargins(paintWidth * -1, 0, 0, 0);
		bv.setLayoutParams(lp);
		
		bvs.add(bv);
		this.addView(bv);
		bv.move(width + paintWidth, width * -1, heightPosition, heightPosition, duration);
	}	

	public BarrageLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	public BarrageLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public BarrageLayout(Context context) {
		super(context);
		this.context = context;
	}
	
	/**
	 * 初始化，注册监听获取布局的宽和高
	 */
	private void init() {
		ViewTreeObserver vto = this.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				BarrageLayout.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				height = BarrageLayout.this.getHeight();
				width = BarrageLayout.this.getWidth();
			}
		});
	}
	
	/**
	 * 添加新信息
	 * @param info
	 */
	public void addNewInfo(String info) {
		if (isStart) {
			if (height != 0 && width != 0) {
				infos.add(info);
			}
		}
	}
	
	/**
	 * 弹幕开始
	 */
	public void start() {
		
		if (isStart) {
			return;
		}
		
		init();
		
		timerTask = new TimerTask() {
			@Override
			public void run() { handler.sendEmptyMessage(0); }
		};
		
		timer.schedule(timerTask, 0, barrageInterval);
		this.setVisibility(View.VISIBLE);
		isStart = true;
	}
	
	/**
	 * 停止弹幕
	 */
	public void stop() {
		isStart = false;
		if (timerTask != null) {
			timerTask.cancel();
		}
		infos.clear();
		
	}
	
	/**
	 * 设置屏幕刷新弹幕的间隔，最小2s
	 * @param interval 毫秒
	 */
	public void setInterval(long interval) {
		if (interval > 2000) {
			this.barrageInterval = interval;
		}
	}
	
	/**
	 * 设置弹幕在屏幕的动画时间
	 * @param duration
	 */
	public void setBarrageDuration(long duration) {
		this.duration = duration;
	}
	
	/**
	 * 设置每次获取的最大的弹幕个数
	 * @param maxBarragePerShow
	 */
	public void setMaxBarragePerShow(int maxBarragePerShow) {
		if (maxBarragePerShow > 0) {
			this.maxBrragePerShow = maxBarragePerShow;
		}
		
	}

}
