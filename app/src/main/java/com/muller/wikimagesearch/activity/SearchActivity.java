package com.muller.wikimagesearch.activity;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.muller.wikimagesearch.R;
import com.muller.wikimagesearch.adapter.PageAdapter;
import com.muller.wikimagesearch.anim.HeroAnimation;
import com.muller.wikimagesearch.anim.WikipediaLogo;
import com.muller.wikimagesearch.model.Page;
import com.muller.wikimagesearch.model.Query;
import com.muller.wikimagesearch.volley.GsonRequest;
import com.muller.wikimagesearch.volley.SearchRequest;
import com.muller.wikimagesearch.volley.Volley;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SearchActivity extends ActionBarActivity {
	@InjectView(R.id.recycler_view) RecyclerView mRecyclerView;
	@InjectView(R.id.globe_image) ImageView mGlobeImageView;
	@InjectView(R.id.word_image) ImageView mWordImageView;

	private String mSearchQuery = "";
	private List<Page> mPages = new ArrayList<>();
	private static final String SAVED_STATE_SEARCH_QUERY = "SEARCH_QUERY";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		ButterKnife.inject(this);

		// Only run the animation if we're coming from a different activity
		if (savedInstanceState == null) {
			HeroAnimation globe = (HeroAnimation)getIntent().getExtras().getSerializable(WikipediaLogo.GLOBE);
			HeroAnimation wordMark = (HeroAnimation)getIntent().getExtras().getSerializable(WikipediaLogo.WORD_MARK);

			initHeroAnimation(globe, mGlobeImageView);
			initHeroAnimation(wordMark, mWordImageView);
		}

		initView();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putString(SAVED_STATE_SEARCH_QUERY, mSearchQuery);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mSearchQuery = savedInstanceState.getString(SAVED_STATE_SEARCH_QUERY);
		search();
	}

	private void initHeroAnimation(final HeroAnimation heroAnimation, final ImageView imageView) {
		imageView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				imageView.getViewTreeObserver().removeOnPreDrawListener(this);
				heroAnimation.play(imageView, 500);
				return true;
			}
		});
	}

	private void initView() {
		//set alpha on imageViews to create 'watermark' effect
		ViewHelper.setAlpha(mGlobeImageView, 0.05f);
		ViewHelper.setAlpha(mWordImageView, 0.05f);

		mRecyclerView.setHasFixedSize(true);
		mRecyclerView.setAdapter(new PageAdapter(mRecyclerView, mPages));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		MenuItem searchMenuItem = menu.findItem(R.id.search);
		MenuItemCompat.setActionView(searchMenuItem, R.layout.menu_search_edittext);

		EditText searchTextView = (EditText)MenuItemCompat.getActionView(searchMenuItem);
		MenuItemCompat.expandActionView(searchMenuItem); //start with editText visible

		if (mSearchQuery.length() > 0) {
			searchTextView.setText(mSearchQuery); //set saved value
		}

		searchTextView.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mSearchQuery = s.toString();
				search();
			}

			@Override
			public void afterTextChanged(Editable s) {

			}
		});

		return true;
	}

	private void search() {
		if (mSearchQuery.length() == 0)
			return;

		Volley.cancelAllRequests();	//cancel all pending requests

		int imageSize = (int)getResources().getDimension(R.dimen.column_width);

		Volley.getRequestQueue().add(new SearchRequest(mSearchQuery, imageSize, new com.android.volley.Response.Listener<Query>() {
			@Override
			public void onResponse(Query response) {
				mPages.clear();
				mPages.addAll(response.getPages());
				((PageAdapter)mRecyclerView.getAdapter()).refresh();
			}
		}, new com.android.volley.Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				if (error instanceof TimeoutError) {
					toast("Timed out. Please try again");
				} else {
					toast("No results");
				}
			}
		}));
	}

	private void toast(String text) {
		Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
	}
}
