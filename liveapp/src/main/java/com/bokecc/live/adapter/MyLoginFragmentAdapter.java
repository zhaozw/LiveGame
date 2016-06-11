package com.bokecc.live.adapter;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.bokecc.live.fragment.LoginFragment;

public class MyLoginFragmentAdapter extends FragmentPagerAdapter {

	private FragmentManager fm;
	
	private LoginFragment liveLoginFragment, replayLoginFragment;
	
	private List<LoginFragment> fragments = new ArrayList<LoginFragment>();
	
	public MyLoginFragmentAdapter(FragmentManager fm) {
		super(fm);
		this.fm = fm;
		
		liveLoginFragment = new LoginFragment(true);
		replayLoginFragment = new LoginFragment(false);
		
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
	
	public List<LoginFragment> getFragments() {
		return fragments;
	}

}
