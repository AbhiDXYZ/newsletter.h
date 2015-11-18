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

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DateFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import xyz.abhid.newsletterh.R;
import xyz.abhid.newsletterh.entities.Stats;
import xyz.abhid.newsletterh.events.StatsUpdateEvent;
import xyz.abhid.newsletterh.tasks.StatsTask;
import xyz.abhid.newsletterh.util.AndroidUtils;

public class StatsActivity extends BaseTopActivity {

    public static final String TAG = "Statistics";

    private StatsTask mStatsTask;

    @Bind(R.id.textViewArticlesInLatestIssue) TextView mTextViewArticlesInLatestIssue;
    @Bind(R.id.progressBarLatestIssueRead) ProgressBar mProgressBarLatestIssueRead;
    @Bind(R.id.textViewLatestIssueRead) TextView mTextViewLatestIssueRead;
    @Bind(R.id.progressBarLatestIssueFavorite) ProgressBar mProgressBarLatestIssueFavorite;
    @Bind(R.id.textViewLatestIssueFavorite) TextView mTextViewLatestIssueFavorite;
    @Bind(R.id.textViewLatestIssueDate) TextView mTextViewLatestIssueDate;
    @Bind(R.id.textViewRead) TextView mTextViewRead;
    @Bind(R.id.textViewFavorite) TextView mTextViewFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stats);
        setupActionBar();
        setupNavDrawer();

        ButterKnife.bind(this);
    }

    @Override
    protected void setupActionBar() {
        super.setupActionBar();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.statistics);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        setDrawerSelectedItem(R.id.navigationItemStatistics);

        loadStats();
    }

    @Override
    public void onStop() {
        super.onStop();

        // prefs might have changed, update menu
        supportInvalidateOptionsMenu();

        cleanupStatsTask();
    }

    private void loadStats() {
        cleanupStatsTask();
        mStatsTask = new StatsTask(this);
        AndroidUtils.executeOnPool(mStatsTask);
    }

    private void cleanupStatsTask() {
        if (mStatsTask != null && mStatsTask.getStatus() != AsyncTask.Status.FINISHED) {
            mStatsTask.cancel(true);
        }
        mStatsTask = null;
    }

    public void onEventMainThread(StatsUpdateEvent event) {
        updateStats(event.stats);
    }

    private void updateStats(Stats stats) {
        mTextViewArticlesInLatestIssue.setText(String.valueOf(stats.articlesInLatestIssue));

        mProgressBarLatestIssueRead.setMax(stats.articlesInLatestIssue);
        mProgressBarLatestIssueRead.setProgress(stats.latestIssueRead);

        String latestIssueRead = stats.latestIssueRead + " " + getResources().getString(R.string.stats_read);
        mTextViewLatestIssueRead.setText(latestIssueRead);

        mProgressBarLatestIssueFavorite.setMax(stats.articlesInLatestIssue);
        mProgressBarLatestIssueFavorite.setProgress(stats.latestIssueFavorite);

        String latestIssueFavorite = stats.latestIssueFavorite + " " + getResources().getString(R.string.stats_favorite);
        mTextViewLatestIssueFavorite.setText(latestIssueFavorite);

        mTextViewLatestIssueDate.setText(
                DateFormat.getDateInstance(DateFormat.MEDIUM).format(stats.latestIssueDate));

        mTextViewRead.setText(String.valueOf(stats.read));

        mTextViewFavorite.setText(String.valueOf(stats.favorite));
    }
}
