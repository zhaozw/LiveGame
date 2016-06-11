package com.bokecc.live.adapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bokecc.live.demo.R;
import com.bokecc.live.pojo.QAMsg;
import com.bokecc.sdk.mobile.live.pojo.Answer;
import com.bokecc.sdk.mobile.live.pojo.Question;
import com.bokecc.sdk.mobile.live.pojo.Viewer;

public class MyQAListViewAdapter extends BaseAdapter {
	private Context context;
	private LinkedHashMap<String, QAMsg> qaMap;
	private Viewer viewer;
	
	public MyQAListViewAdapter(Context context, Viewer viewer, LinkedHashMap<String, QAMsg> qaMap) {
		this.context = context;
		this.qaMap = qaMap;
		this.viewer = viewer;
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
		return qaMap.size();
	}

	@Override
	public Object getItem(int position) {
		List<String> list = new ArrayList<String>(qaMap.keySet());
		return qaMap.get(list.get(position));
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
		
		List<String> questionIds = new ArrayList<String>(qaMap.keySet());
		QAMsg qaMsg = qaMap.get(questionIds.get(position));
		Question question = qaMsg.getQuestion();
		Set<Answer> answers = qaMsg.getAnswers();
		
		if (question.getQuestionUserId().equals(viewer.getId())) {
			viewHolder.question.setText("我问：" + question.getContent());
		} else {
			if (answers.size() < 1) {
				viewHolder.question.setVisibility(View.GONE);
				viewHolder.answer.setVisibility(View.GONE);
				return convertView;
			}
			viewHolder.question.setText(question.getQuestionUserName() + "问：" + question.getContent());
		}
		
		StringBuilder sb = new StringBuilder();
		for (Answer answer: answers) {
			sb.append(answer.getAnswerUserName() + "答：" + answer.getContent() + "\n");
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
