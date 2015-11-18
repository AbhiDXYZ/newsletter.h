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

import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import xyz.abhid.newsletterh.R;

public class BaseTopActivity extends BaseNavDrawerActivity {

    @Override
    protected void setupActionBar() {
        super.setupActionBar();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void setupNavDrawer() {
        super.setupNavDrawer();

        // show a drawer indicator
        setDrawerIndicatorEnabled();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        // use special animation when navigating away from a top activity
        // but not when exiting the app (use the default system animations)
        if (!isTaskRoot()) {
            overridePendingTransition(R.anim.activity_fade_enter, R.anim.activity_fade_exit);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // check if we should toggle the navigation drawer (app icon was touched)
        if (toggleDrawer(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
