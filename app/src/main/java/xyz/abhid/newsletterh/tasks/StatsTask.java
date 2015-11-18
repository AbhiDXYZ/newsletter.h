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

package xyz.abhid.newsletterh.tasks;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import java.util.Date;

import de.greenrobot.event.EventBus;
import xyz.abhid.newsletterh.entities.Stats;
import xyz.abhid.newsletterh.events.StatsUpdateEvent;
import xyz.abhid.newsletterh.provider.NewsletterContract.Articles;

public class StatsTask extends AsyncTask<Void, Stats, Stats> {

    private final Context mContext;

    public StatsTask(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    protected Stats doInBackground(Void... params) {
        Stats stats = new Stats();

        ContentResolver resolver = mContext.getContentResolver();
        final Cursor queryArticles = resolver.query(Articles.CONTENT_URI,
                new String[]{Articles._ID, Articles.IS_FAVORITE, Articles.IS_READ},
                null, null, null);

        if (queryArticles != null) {
            int favorites = 0;
            int read = 0;
            while (queryArticles.moveToNext()) {
                if (queryArticles.getInt(1) == 1) {
                    favorites++;
                }
                if (queryArticles.getInt(2) == 1) {
                    read++;
                }
            }
            queryArticles.close();
            stats.favorite = favorites;
            stats.read = read;
        }

        if (isCancelled()) {
            return stats;
        }

        final Cursor queryLatest = resolver.query(Articles.CONTENT_URI,
                new String[]{Articles._ID, "MAX(" + Articles.ISSUE + ")", Articles.ISSUE_DATE,
                        Articles.IS_FAVORITE, Articles.IS_READ}, null, null, null);
        if (queryLatest != null) {
            int latestIssue = 0;
            int articlesInLatestIssue = 0;
            Date latestIssueDate = new Date();
            int latestIssueRead = 0;
            int latestIssueFavorite = 0;
            articlesInLatestIssue = queryLatest.getCount();
            if (queryLatest.moveToFirst()) {
                latestIssue = queryLatest.getInt(1);
                latestIssueDate = new Date(queryLatest.getLong(2));
            }
            queryLatest.moveToPosition(-1);
            while (queryLatest.moveToNext()) {
                if (queryLatest.getInt(3) == 1) {
                    latestIssueFavorite++;
                }
                if (queryLatest.getInt(4) == 1) {
                    latestIssueRead++;
                }
            }
            queryLatest.close();
            stats.latestIssue = latestIssue;
            stats.latestIssueDate = latestIssueDate;
            stats.latestIssueRead = latestIssueRead;
            stats.latestIssueFavorite = latestIssueFavorite;
            stats.articlesInLatestIssue = articlesInLatestIssue;
        }
        return stats;
    }

    @Override
    protected void onPostExecute(Stats stats) {
        EventBus.getDefault().post(new StatsUpdateEvent(stats, true));
    }
}
