package com.vipheyue.livegame.cc.com.bokecc.live.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bokecc.sdk.mobile.live.pojo.ChatMessage;
import com.bokecc.sdk.mobile.live.pojo.Viewer;
import com.vipheyue.livegame.R;
import com.vipheyue.livegame.cc.com.bokecc.live.util.FaceStringUtil;

import java.util.List;

public class MyChatListViewAdapter extends BaseAdapter {

	private Context context;
	private List<ChatMessage> chatMsgs;
	private String viewerId;
	private String viewerName;
	
	public MyChatListViewAdapter(Context context, Viewer viewer, List<ChatMessage> chatMsgs) {
		this.context = context;
		this.chatMsgs = chatMsgs;
		this.viewerId = viewer.getId();
		this.viewerName = viewer.getName();
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
		return chatMsgs.size();
	}

	@Override
	public Object getItem(int position) {
		return chatMsgs.get(position);
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
		
		ChatMessage chatMessage = chatMsgs.get(position);
		String userId = chatMessage.getUserId();
		String text = null;
		if (chatMessage.isPublic()) {
			if (viewerId.equals(userId)) {
				text = "我说：" + chatMessage.getMessage();
			} else {
				text = chatMessage.getUserName() + "：" + chatMessage.getMessage();
			}
			viewHolder.tv.setTextColor(context.getResources().getColor(R.color.public_chat));
		} else {
			if (viewerId.equals(userId)) {
				text = "我对主讲说：" + chatMessage.getMessage();
			} else {
				text = chatMessage.getUserName() + "对我说：" + chatMessage.getMessage();
			}
			viewHolder.tv.setTextColor(context.getResources().getColor(R.color.private_chat));
		}
		
		viewHolder.tv.setText(FaceStringUtil.parseFaceMsg(context, text));
		return convertView;
	}
	
	 private class ViewHolder {
	    	public TextView tv;
	 }
}
