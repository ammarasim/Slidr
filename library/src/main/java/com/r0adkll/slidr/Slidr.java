package com.r0adkll.slidr;


import android.app.Activity;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.widget.SliderPanel;


/**
 * This attacher class is used to attach the sliding mechanism to any {@link android.app.Activity}
 * that lets the user slide (or swipe) the activity away as a form of back or up action. The action
 * causes {@link android.app.Activity#finish()} to be called.
 */
public final class Slidr {

    /**
     * Attach a slideable mechanism to an activity that adds the slide to dismiss functionality
     *
     * @param activity      the activity to attach the slider to
     * @return              a {@link com.r0adkll.slidr.model.SlidrInterface} that allows
     *                      the user to lock/unlock the sliding mechanism for whatever purpose.
     */
    public static SlidrInterface attach(@NonNull Activity activity) {
        return attach(activity, -1, -1);
    }


    /**
     * Attach a slideable mechanism to an activity that adds the slide to dismiss functionality
     * and allows for the statusbar to transition between colors
     *
     * @param activity          the activity to attach the slider to
     * @param statusBarColor1   the primaryDark status bar color of the interface that this will slide back to
     * @param statusBarColor2   the primaryDark status bar color of the activity this is attaching to that will transition
     *                          back to the statusBarColor1 color
     *
     * @return              a {@link com.r0adkll.slidr.model.SlidrInterface} that allows
     *                      the user to lock/unlock the sliding mechanism for whatever purpose.
     */
    public static SlidrInterface attach(@NonNull Activity activity, @ColorInt int statusBarColor1,
                                        @ColorInt int statusBarColor2) {

		// Setup the slider panel and attach it to the decor
		final SliderPanel panel = attachSliderPanel(activity, null);

        // Set the panel slide listener for when it becomes closed or opened
        panel.setOnPanelSlideListener(new ColorPanelSlideListener(activity, statusBarColor1, statusBarColor2));

		// Return the lock interface
		return panel.getDefaultInterface();
    }


    /**
     * Attach a slider mechanism to an activity based on the passed {@link com.r0adkll.slidr.model.SlidrConfig}
     *
     * @param activity      the activity to attach the slider to
     * @param config        the slider configuration to make
     * @return              a {@link com.r0adkll.slidr.model.SlidrInterface} that allows
     *                      the user to lock/unlock the sliding mechanism for whatever purpose.
     */
    public static SlidrInterface attach(@NonNull Activity activity, @NonNull SlidrConfig config) {

        // Setup the slider panel and attach it to the decor
        final SliderPanel panel = attachSliderPanel(activity, config);

        // Set the panel slide listener for when it becomes closed or opened
        panel.setOnPanelSlideListener(new ConfigPanelSlideListener(activity, config));

        // Return the lock interface
        return panel.getDefaultInterface();
    }


    /**
     * Attach a new {@link SliderPanel} to the root of the activity's content
     */
	private static SliderPanel attachSliderPanel(@NonNull Activity activity, @NonNull SlidrConfig config) {
		// Hijack the decorview
		ViewGroup decorView = (ViewGroup)activity.getWindow().getDecorView();
		View oldScreen = decorView.getChildAt(0);
		decorView.removeViewAt(0);

		// Setup the slider panel and attach it to the decor
		SliderPanel panel = new SliderPanel(activity, oldScreen, config);
		panel.setId(R.id.slidable_panel);
		oldScreen.setId(R.id.slidable_content);
		panel.addView(oldScreen);
		decorView.addView(panel, 0);
		return panel;
	}

	private static SlidrInterface initInterface(final SliderPanel panel) {
		// Setup the lock interface
		SlidrInterface slidrInterface = new SlidrInterface() {
			@Override
			public void lock() {
				panel.lock();
			}

			@Override
			public void unlock() {
				panel.unlock();
			}
		};

		// Return the lock interface
		return slidrInterface;
	}

	public static SlidrInterface replace(final View oldScreen, final SlidrConfig config){
		ViewGroup parent = (ViewGroup) oldScreen.getParent();
		ViewGroup.LayoutParams params = oldScreen.getLayoutParams();
		parent.removeView(oldScreen);
		// Setup the slider panel and attach it
		final SliderPanel panel = new SliderPanel(oldScreen.getContext(), oldScreen, config);
		panel.setId(R.id.slidable_panel);
		oldScreen.setId(R.id.slidable_content);

		panel.addView(oldScreen);
		parent.addView(panel,0, params);

		// Set the panel slide listener for when it becomes closed or opened
		panel.setOnPanelSlideListener(new SliderPanel.OnPanelSlideListener() {

		    @Override
		    public void onStateChanged(int state) {
			if(config.getListener() != null){
			    config.getListener().onSlideStateChanged(state);
			}
		    }

		    @Override
		    public void onClosed() {
			if(config.getListener() != null){
			    config.getListener().onSlideClosed();
			}
			if(((AppCompatActivity)(oldScreen.getContext())).getSupportFragmentManager().getBackStackEntryCount()==0) {
			    ((AppCompatActivity)(oldScreen.getContext())).finish();
			    ((AppCompatActivity)(oldScreen.getContext())).overridePendingTransition(0, 0);
			}else{
			    ((AppCompatActivity)(oldScreen.getContext())).getSupportFragmentManager().popBackStack();
			}
		    }

		    @Override
		    public void onOpened() {
			if(config.getListener() != null){
			    config.getListener().onSlideOpened();
			}
		    }

		    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
		    @Override
		    public void onSlideChange(float percent) {
			if(config.getListener() != null){
			    config.getListener().onSlideChange(percent);
			}
		    }
		});

		// Setup the lock interface
		SlidrInterface slidrInterface = new SlidrInterface() {
		    @Override
		    public void lock() {
			panel.lock();
		    }

		    @Override
		    public void unlock() {
			panel.unlock();
		    }

		    @Override
		    public void addViewToPanel(View v) {

		    }

		    @Override
		    public void removeViewToPanel(View v) {

		    }
		};
		// Return the lock interface
		return slidrInterface;
    }

}
