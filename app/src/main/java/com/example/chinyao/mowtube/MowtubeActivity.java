package com.example.chinyao.mowtube;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.github.pedrovgs.DraggableListener;
import com.github.pedrovgs.DraggableView;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MowtubeActivity extends AppCompatActivity {

    // TODO: Use Shared Preferences
    public static Boolean autoplay_on_wifi_only = false;

    // tmdb
    // https://www.themoviedb.org/documentation/api/sessions?language=en
    // http://docs.themoviedb.apiary.io/
    // https://www.themoviedb.org/documentation/api
    public static final String TMDB_API_KEY = "a07e22bc18f5cb106bfe4cc1f83ad8ed";

    // https://console.developers.google.com/
    public static final String YOUTUBE_API_KEY = "AIzaSyDclFRxzBdoqRGHVftdG1WFqBX2C2mVe04";
    public static final String YOUTUBE_DEFAULT_LINK = "664VCs3c1HU";

    // ButterKnife
    // http://guides.codepath.com/android/Reducing-View-Boilerplate-with-Butterknife
    @BindView(R.id.m_app_bar_layout) AppBarLayout theAppBarLayout;
    @BindView(R.id.m_toolbar) Toolbar toolbar;
    @BindView(R.id.m_tab_layout) TabLayout tabLayout;
    @BindView(R.id.m_view_pager) ViewPager viewPager;
    @BindView(R.id.m_draggable_view) DraggableView theDraggableView;
    @BindView(R.id.dv_b_sub_slider_layout) SliderLayout mDemoSlider;

    YouTubePlayer mYouTubePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mowtube_activity);

        // ButterKnife
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            // up right menu button
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            // up left drawer button
            // ab.setDisplayHomeAsUpEnabled(true);
        }

        setupViewPager();

        setupDraggableView();
    }

    private void setupViewPager() {
        viewPager.setOffscreenPageLimit(3);

        MowtubeViewPagerAdapter mowtubeViewPagerAdapter = new MowtubeViewPagerAdapter(getSupportFragmentManager());
        mowtubeViewPagerAdapter.addFragment(MowtubeListFragment.newInstance(1), getString(R.string.home));
        mowtubeViewPagerAdapter.addFragment(MowtubeListFragment.newInstance(2), getString(R.string.upcoming));
        mowtubeViewPagerAdapter.addFragment(MowtubeListFragment.newInstance(3), getString(R.string.trending));
        mowtubeViewPagerAdapter.addFragment(MowtubeListFragment.newInstance(4), getString(R.string.favorite));
        viewPager.setAdapter(mowtubeViewPagerAdapter);

        tabLayout.setupWithViewPager(viewPager);
        // use         |     tab1    |     tab2    |
        // instead of  |  tab1  |  tab2  |         |
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (autoplay_on_wifi_only) {
            menu.findItem(R.id.autoplay_on_wifi_only).setChecked(true);
        }
        else {
            menu.findItem(R.id.autoplay_always).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.autoplay_on_wifi_only:
                autoplay_on_wifi_only = true;
                return true;
            case R.id.autoplay_always:
                autoplay_on_wifi_only = false;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupDraggableView() {
        int orientation = getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            theDraggableView.setXTopViewScaleFactor((float)1.5);
            theDraggableView.setYTopViewScaleFactor((float)1.5);
        }

        theDraggableView.setVisibility(View.INVISIBLE);
        //theDraggableView.setVisibility(View.GONE);

        // theDraggableView.bringToFront();
        // ViewGroup viewGroup = ((ViewGroup) viewPager.getParent());
        // int index = viewGroup.indexOfChild(viewPager);
        // for(int i = 0; i<index; i++) {
        //     viewGroup.bringChildToFront(viewGroup.getChildAt(i));
        // }

        // theAppBarLayout.setVisibility(View.INVISIBLE);

        theDraggableView.setDraggableListener(new DraggableListener() {
            @Override public void onMaximized() {
                if (theAppBarLayout.getVisibility() != View.INVISIBLE) {
                    theAppBarLayout.setVisibility(View.INVISIBLE);
                    mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.DEFAULT);
                }
            }

            @Override public void onMinimized() {
                if (theAppBarLayout.getVisibility() != View.VISIBLE) {
                    theAppBarLayout.setVisibility(View.VISIBLE);
                    mYouTubePlayer.setPlayerStyle(YouTubePlayer.PlayerStyle.CHROMELESS);
                }
            }

            @Override public void onClosedToLeft() {
                mYouTubePlayer.pause();
            }

            @Override public void onClosedToRight() {
                theDraggableView.maximize();
            }
        });

        initializeYoutubeFragment();
    }

    // DraggableView and YouTubePlayerSupportFragment have problems to call initializeYoutubeFragment() again
    private void initializeYoutubeFragment() {
        YouTubePlayerSupportFragment mYouTubeContainer = YouTubePlayerSupportFragment.newInstance();
        mYouTubeContainer.initialize(YOUTUBE_API_KEY, new YouTubePlayer.OnInitializedListener() {
            @Override
            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
                if (!wasRestored) {
                    mYouTubePlayer = youTubePlayer;
                    mYouTubePlayer.cueVideo(MowtubeActivity.YOUTUBE_DEFAULT_LINK);
                    mYouTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
                        @Override
                        public void onLoading() {}
                        @Override
                        public void onLoaded(String s) {}
                        @Override
                        public void onAdStarted() {}
                        @Override
                        public void onVideoStarted() {}
                        @Override
                        public void onVideoEnded() {}
                        @Override
                        public void onError(YouTubePlayer.ErrorReason errorReason) {
                            Log.d("MowtubeActivity", "onError " + errorReason.toString());
                        }
                    });
                }
            }
            @Override
            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
                Log.d("MowtubeActivity", "onInitializationFailure " + youTubeInitializationResult.toString());
            }
        });

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.dv_t_youtube, mYouTubeContainer)
                .commit();
    }

    public void loadYoutube(String input) {
        if (autoplay_on_wifi_only && !isWifiConnected()) {
            mYouTubePlayer.cueVideo(input);
        }
        else {
            mYouTubePlayer.loadVideo(input);
        }
        // mYouTubePlayer.play();
        theDraggableView.maximize();

        HashMap<String,String> url_maps = new HashMap<String, String>();
        url_maps.put("Hannibal", "http://static2.hypable.com/wp-content/uploads/2013/12/hannibal-season-2-release-date.jpg");
        url_maps.put("Big Bang Theory", "http://tvfiles.alphacoders.com/100/hdclearart-10.png");
        url_maps.put("House of Cards", "http://cdn3.nflximg.net/images/3093/2043093.jpg");
        url_maps.put("Game of Thrones", "http://images.boomsbeat.com/data/images/full/19640/game-of-thrones-season-4-jpg.jpg");

        for(String name : url_maps.keySet()){
            TextSliderView textSliderView = new TextSliderView(this);
            // initialize a SliderLayout
            textSliderView
                    .description(name)
                    .image(url_maps.get(name));

            mDemoSlider.addSlider(textSliderView);
        }
        theDraggableView.setVisibility(View.VISIBLE);
    }

    // TODO: implement this function
    private boolean isWifiConnected() {
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        // Save custom values into the bundle
        savedInstanceState.putInt("key1", 123);
        savedInstanceState.putString("key2", "abc");
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);
        // Restore state members from saved instance
        int someIntValue = savedInstanceState.getInt("key1");
        String someStringValue = savedInstanceState.getString("key2");
        Toast.makeText(this,
                "onRestoreInstanceState " + someIntValue + " " + someStringValue,
                Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    protected void onStop() {
        mDemoSlider.stopAutoCycle();
        super.onStop();
    }
}
