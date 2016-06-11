package com.vipheyue.livegame.cc.com.bokecc.live.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bokecc.sdk.mobile.live.replay.pojo.ReplayAnswerMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQAMsg;
import com.bokecc.sdk.mobile.live.replay.pojo.ReplayQuestionMsg;
import com.vipheyue.livegame.R;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class MyReplayQAListViewAdapter extends BaseAdapter {
	private Context context;
	private List<ReplayQAMsg> qaMsgs;
	
	public MyReplayQAListViewAdapter(Context context, TreeSet<ReplayQAMsg> qaMsgs) {
		this.context = context;
		this.qaMsgs = new ArrayList<ReplayQAMsg>(qaMsgs);
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
		return qaMsgs.size();
	}

	@Override
	public Object getItem(int position) {
		
		return qaMsgs.get(position);
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
			convertView = LayoutInflater.from(context).inflate(R.layout.lv_qa_view, parent, false);
			viewHolder.answer = (TextView) convertView.findViewById(R.id.tv_answer);
			viewHolder.question = (TextView) convertView.findViewById(R.id.tv_question);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		
		ReplayQAMsg qaMsg = qaMsgs.get(position);
		
		ReplayQuestionMsg question = qaMsg.getReplayQuestionMsg();
		viewHolder.question.setText(question.getQuestionUserName() + "问：" + question.getContent());
		
		
		TreeSet<ReplayAnswerMsg> answers = qaMsg.getReplayAnswerMsgs();
		
		if (answers.size() < 1) {
			viewHolder.question.setVisibility(View.GONE);
			viewHolder.answer.setVisibility(View.GONE);
			return convertView;
		}
		
		StringBuilder sb = new StringBuilder();
		for (ReplayAnswerMsg answer: answers) {
			sb.append(answer.getUserName() + "答：" + answer.getContent() + "\n");
		}
		if (sb.length() > 2) {
			viewHolder.answer.setText(sb.substring(0, sb.length() - 1));
		} else {
			viewHolder.answer.setText("");
		}
		
		viewHolder.question.setVisibility(View.VISIBLE);
		viewHolder.answer.setVisibility(View.VISIBLE);
		return convertView;
	}
	
	 private class ViewHolder {
	    	public TextView question;
	    	public TextView answer;
	 }
}
