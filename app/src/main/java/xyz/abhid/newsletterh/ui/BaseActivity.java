/*
 * Copyright (c) 2015, Abhishek Dabholkar
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package xyz.abhid.newsletterh.ui;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.google.android.gms.analytics.GoogleAnalytics;

import de.greenrobot.event.EventBus;
import xyz.abhid.newsletterh.R;
import xyz.abhid.newsletterh.events.OnItemAddedEvent;

public class BaseActivity extends AppCompatActivity {

    private View mContainerContent;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setCustomTheme();
        super.onCreate(savedInstanceState);
    }

    protected void setCustomTheme() {
        // set a theme based on user preference
        setTheme(NewsletterPreferences.THEME);
    }

    /**
     * Implementers must call this in {@link #onCreate} after {@link #setContentView} if they want
     * to use the action bar.
     */
    protected void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onStart() {
        super.onStart();

        GoogleAnalytics.getInstance(this).reportActivityStart(this);
        registerEventBus();
    }

    @Override
    protected void onStop() {
        super.onStop();

        GoogleAnalytics.getInstance(this).reportActivityStop(this);
        unregisterEventBus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void registerEventBus() {
        EventBus.getDefault().register(this);
    }

    public void unregisterEventBus() {
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(OnItemAddedEvent event) {
        event.handle(this);
    }

    /**
     * Implementing classes may call this in {@link #onCreate(android.os.Bundle)} to setup a
     * progress bar which displays when syncing.
     */
    protected void setupProgressBar() {
        mContainerContent = findViewById(R.id.containerContent);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        setProgressVisible(false, false);
    }

    /**
     * Hides the content container and shows a progress bar.
     */
    public void setProgressVisible(boolean visible, boolean animate) {
        if (animate) {
            Animation out = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
            Animation in = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
            mContainerContent.startAnimation(visible ? out : in);
            mProgressBar.startAnimation(visible ? in : out);
        }
        mContainerContent.setVisibility(visible ? View.GONE : View.VISIBLE);
        mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
