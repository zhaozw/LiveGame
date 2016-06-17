package com.vipheyue.livegame.cc.com.bokecc.live.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.vipheyue.livegame.fragment.PlayCCFragment;

import java.util.ArrayList;
import java.util.List;

public class MyLoginFragmentAdapter extends FragmentPagerAdapter {

	private FragmentManager fm;
	
	private PlayCCFragment liveLoginFragment, replayLoginFragment;
	
	private List<PlayCCFragment> fragments = new ArrayList<PlayCCFragment>();
	
	public MyLoginFragmentAdapter(FragmentManager fm) {
		super(fm);
		this.fm = fm;
		
		liveLoginFragment = new PlayCCFragment();
		replayLoginFragment = new PlayCCFragment();
		
		fragments.add(liveLoginFragment);
		fragments.add(replayLoginFragment);
	}

	@Override
	public Fragment getItem(int arg0) {
		return fragments.get(arg0);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}
	
	public List<PlayCCFragment> getFragments() {
		return fragments;
	}

}
