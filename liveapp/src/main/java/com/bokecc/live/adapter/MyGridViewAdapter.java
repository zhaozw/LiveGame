package com.bokecc.live.adapter;

import com.bokecc.live.util.FaceStringUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class MyGridViewAdapter extends BaseAdapter {
	
	private Context context;
	private EditText etMsg;
	public MyGridViewAdapter(Context context, EditText etMsg) {
		this.context = context;
		this.etMsg = etMsg;
	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public int getCount() {
		return FaceStringUtil.imgs.length;
	}

	@Override
	public Object getItem(int position) {
		return FaceStringUtil.imgs[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ImageView imageView = null;
		if (convertView == null) {
			imageView = new ImageView(context);
			imageView.setLayoutParams(new GridView.LayoutParams(80, 80));
			imageView.setScaleType(ScaleType.CENTER_INSIDE);
			imageView.setPadding(8, 8, 8, 8);
		} else {
			imageView = (ImageView) convertView;
		}
		
		imageView.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (position == 23 ) {
					Editable editable = etMsg.getText();
					int length = editable.length();
					if (length <= 0) {
						return;
					}
					
					int arrowPosition = etMsg.getSelectionStart();
					if (arrowPosition == 0) {
						return;
					}
					String subString = editable.toString().substring(0, arrowPosition);
					if (subString.length() >= 8) {
						int imgIndex = subString.lastIndexOf("[em2_");
						
						if ((imgIndex + 8) == arrowPosition ) {
							if (FaceStringUtil.pattern.matcher(editable.toString().substring(imgIndex, imgIndex+8)).find()) {
								editable.delete(arrowPosition - 8, arrowPosition);
							} else {
								editable.delete(arrowPosition - 1, arrowPosition);
							}
						} else {
							editable.delete(arrowPosition - 1, arrowPosition);
						}
					} else {
						editable.delete(arrowPosition - 1, arrowPosition);
					} 

				} else if (position < 20 && position >=0) {
					Bitmap b = BitmapFactory.decodeResource(context.getResources(), FaceStringUtil.imgs[position]);
					b = ThumbnailUtils.extractThumbnail(b, 35, 35);  
			        ImageSpan imgSpan = new ImageSpan(context, b);  
			        SpannableString spanString = new SpannableString(FaceStringUtil.imgNames[position]);  
			        spanString.setSpan(imgSpan, 0, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE); 
			        etMsg.append(spanString);
				}
			}
		});
		
		imageView.setImageResource(FaceStringUtil.imgs[position]);
		
		return imageView;
	}
	
};