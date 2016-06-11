package com.bokecc.live.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import com.bokecc.live.demo.R;

public class FaceStringUtil {
	
	public static int[] imgs = new int[] {
		R.drawable.em2_01, R.drawable.em2_02, R.drawable.em2_03, R.drawable.em2_04, R.drawable.em2_05, R.drawable.em2_06,
		R.drawable.em2_07, R.drawable.em2_08, R.drawable.em2_09, R.drawable.em2_10, R.drawable.em2_11, R.drawable.em2_12,
		R.drawable.em2_13, R.drawable.em2_14, R.drawable.em2_15, R.drawable.em2_16, R.drawable.em2_17, R.drawable.em2_18,
		R.drawable.em2_19, R.drawable.em2_20, R.drawable.icon_white, R.drawable.icon_white, R.drawable.icon_white, R.drawable.icon_del,
	};
		
	public static String[] imgNames = new String[] {
	    "[em2_01]", "[em2_02]", "[em2_03]", "[em2_04]", "[em2_05]", "[em2_06]", "[em2_07]", "[em2_08]", "[em2_09]", "[em2_10]", 
	    "[em2_11]", "[em2_12]", "[em2_13]", "[em2_14]", "[em2_15]", "[em2_16]", "[em2_17]", "[em2_18]", "[em2_19]", "[em2_20]",  
	};
	
	public static List<String> imgNamesList;
	
	static {
		imgNamesList = Arrays.asList(imgNames);
	}
	public static Pattern pattern = Pattern.compile("\\[em2_[0-2][0-9]\\]");
	
	public static SpannableString parseFaceMsg(Context context, String faceMsg) {
		
		SpannableString spanString = new SpannableString(faceMsg);
		Matcher m = pattern.matcher(faceMsg);
		while (m.find()) {
			String imgStr = m.group();
			int imgStrIndex = imgNamesList.indexOf(imgStr);
			if (imgStrIndex != -1) {
				Bitmap imgBitmap = BitmapFactory.decodeResource(context.getResources(), imgs[imgStrIndex]);
				imgBitmap = ThumbnailUtils.extractThumbnail(imgBitmap, 35, 35);  
			    ImageSpan imgSpan = new ImageSpan(context, imgBitmap);  
			    spanString.setSpan(imgSpan, m.start(), m.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);  
			}
		}
		return spanString;
	}
}
