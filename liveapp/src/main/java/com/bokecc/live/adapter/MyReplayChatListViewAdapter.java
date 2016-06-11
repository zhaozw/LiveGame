package com.bokecc.live.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bokecc.live.demo.R;
import com.bokecc.live.util.FaceStringUtil;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayChatMsg;

public class MyReplayChatListViewAdapter extends BaseAdapter {
	private Context context;
	private List<ReplayChatMsg> replayChatMsgs;
	
	public MyReplayChatListViewAdapter(Context context, TreeSet<ReplayChatMsg> replayChatMsgs) {
		this.context = context;
		this.replayChatMsgs = new ArrayList<ReplayChatMsg>(replayChatMsgs);
	}
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}
	
	@Override
	public boolean isEnabled(int position) {
		return false;
	}

	@Override
	public int getCount() {
		return replayChatMsgs.size();
	}

	@Override
	public Object getItem(int position) {
		return replayChatMsgs.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.lv_chat_view, parent, false);
			viewHolder.tv = (TextView) convertView.findViewById(R.id.tv_chat);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		ReplayChatMsg chatMessage = replayChatMsgs.get(position);
		String userName = chatMessage.getUserName();
		String text = userName + "：" + chatMessage.getContent();
		viewHolder.tv.setTextColor(context.getResources().getColor(R.color.public_chat));
		
		viewHolder.tv.setText(FaceStringUtil.parseFaceMsg(context, text));
		return convertView;
	}
	
	 private class ViewHolder {
	    	public TextView tv;
	 }
}
