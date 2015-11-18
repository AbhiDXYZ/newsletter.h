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

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import xyz.abhid.newsletterh.R;
import xyz.abhid.newsletterh.util.Utils;

public class BaseNavDrawerActivity extends BaseActivity {

    public static final String TAG = "Navigation drawer";

    private static final int NAV_DRAWER_CLOSE_DELAY = 250;
    private static final int NAV_ITEM_HEADER = 0;

    private Handler mHandler;

    private Toolbar mActionBarToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new Handler();
    }

    @Override
    protected void setCustomTheme() {
        if (NewsletterPreferences.THEME == R.style.Theme_Newsletter) {
            setTheme(R.style.Theme_Newsletter_Drawer);
        } else if (NewsletterPreferences.THEME == R.style.Theme_Newsletter_Dark) {
            setTheme(R.style.Theme_Newsletter_Dark_Drawer);
        } else if (NewsletterPreferences.THEME == R.style.Theme_Newsletter_Light) {
            setTheme(R.style.Theme_Newsletter_Light_Drawer);
        } else {
            setTheme(R.style.Theme_Newsletter_Drawer);
        }
    }

    /**
     * Initializes the navigation drawer. Overriding activities should call this in their {@link
     * #onCreate(android.os.Bundle)} after {@link #setContentView(int)}.
     */
    public void setupNavDrawer() {
        mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        mNavigationView = (NavigationView) findViewById(R.id.navigationView);

        // setup nav drawer account header
        View headerLayout = mNavigationView.inflateHeaderView(R.layout.drawer_header);
        headerLayout.findViewById(R.id.drawerHeader).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onNavItemClick(NAV_ITEM_HEADER);
                    }
                });

        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        onNavItemClick(menuItem.getItemId());
                        return true;
                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (isNavDrawerOpen()) {
            closeNavDrawer();
            return;
        }
        super.onBackPressed();
    }

    private void onNavItemClick(int itemId) {
        Intent launchIntent = null;

        switch (itemId) {
            case NAV_ITEM_HEADER: {
                Utils.trackAction(this, TAG, "Header");
                break;
            }
            case R.id.navigationItemLatestIssue:
                if (this instanceof LatestIssueActivity) {
                    break;
                }
                launchIntent = new Intent(this, LatestIssueActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                fireTrackerEvent(LatestIssueActivity.TAG);
                break;
            case R.id.navigationItemArchive:
                if (this instanceof ArchiveActivity) {
                    break;
                }
                launchIntent = new Intent(this, ArchiveActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                fireTrackerEvent(ArchiveActivity.TAG);
                break;
            case R.id.navigationItemFavorites:
                if (this instanceof FavoritesActivity) {
                    break;
                }
                launchIntent = new Intent(this, FavoritesActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                fireTrackerEvent(FavoritesActivity.TAG);
                break;
            case R.id.navigationItemStatistics:
                if (this instanceof StatsActivity) {
                    break;
                }
                launchIntent = new Intent(this, StatsActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                fireTrackerEvent(StatsActivity.TAG);
                break;
            case R.id.navigationItemSettings:
                launchIntent = new Intent(this, NewsletterPreferences.class);
                fireTrackerEvent(NewsletterPreferences.TAG);
                break;
            case R.id.navigationItemHelp:
                launchIntent = new Intent(this, HelpActivity.class);
                fireTrackerEvent(HelpActivity.TAG);
                break;
            case R.id.navigationItemAbout:
                launchIntent = new Intent(this, AboutActivity.class);
                fireTrackerEvent(AboutActivity.TAG);
                break;
        }

        // already displaying correct screen
        if (launchIntent != null) {
            final Intent finalLaunchIntent = launchIntent;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    goToNavDrawerItem(finalLaunchIntent);
                }
            }, NAV_DRAWER_CLOSE_DELAY);
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void goToNavDrawerItem(Intent intent) {
        startActivity(intent);
        overridePendingTransition(R.anim.activity_fade_enter, R.anim.activity_fade_exit);
    }

    /**
     * Returns true if the navigation drawer is open.
     */
    public boolean isNavDrawerOpen() {
        return mDrawerLayout.isDrawerOpen(mNavigationView);
    }

    public void setDrawerIndicatorEnabled() {
        mActionBarToolbar.setNavigationIcon(R.drawable.ic_drawer);
        mActionBarToolbar.setNavigationContentDescription(R.string.drawer_open);
    }

    /**
     * Highlights the given position in the drawer menu. Activities listed in the drawer should call
     * this in {@link #onStart()}.
     */
    public void setDrawerSelectedItem(@IdRes int menuItemId) {
        mNavigationView.getMenu().findItem(menuItemId).setChecked(true);
    }

    public void openNavDrawer() {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    public void closeNavDrawer() {
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    public boolean toggleDrawer(MenuItem item) {
        if (item != null && item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerVisible(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return false;
    }

    private void fireTrackerEvent(String label) {
        Utils.trackAction(this, TAG, label);
    }
}
