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

package xyz.abhid.newsletterh.util;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.RemoteException;

import java.util.ArrayList;

import xyz.abhid.newsletterh.NewsletterApplication;
import xyz.abhid.newsletterh.entities.Article;
import xyz.abhid.newsletterh.entities.Issue;
import xyz.abhid.newsletterh.provider.NewsletterContract.Articles;
import xyz.abhid.newsletterh.provider.NewsletterContract.Issues;

public class DBUtils {

    private static final int SMALL_BATCH_SIZE = 50;

    /**
     * Maps a {@link java.lang.Boolean} object to an int value to store in the database.
     */
    public static int convertBooleanToInt(Boolean value) {
        if (value == null) {
            return 0;
        }
        return value ? 1 : 0;
    }

    /**
     * Maps an integer value stored in the database to a boolean.
     */
    public static boolean restoreBooleanFromInt(int value) {
        return value == 1;
    }

    public static boolean isArticleExists(Context context, int articleId) {
        Cursor query = context.getContentResolver().query(Articles.buildArticleUri(articleId),
                new String[]{
                        Articles._ID
                }, null, null, null
        );
        if (query == null) {
            return false;
        }

        boolean isArticleExists = query.getCount() != 0;
        query.close();
        return isArticleExists;
    }

    public static boolean isIssueExists(Context context, int issueId) {
        Cursor query = context.getContentResolver().query(Issues.buildIssueUri(issueId),
                new String[]{
                        Issues._ID
                }, null, null, null
        );
        if (query == null) {
            return false;
        }

        boolean isIssueExists = query.getCount() != 0;
        query.close();
        return isIssueExists;
    }

    public static ContentProviderOperation buildArticleOp(Article article) {
        ContentValues values = new ContentValues();
        values.put(Articles._ID, article.id);
        values.put(Articles.ISSUE, article.issue);
        values.put(Articles.ISSUE_DATE, article.issueDate.getTime());
        values.put(Articles.TITLE, article.title);
        values.put(Articles.BODY, article.body);
        values.put(Articles.POSTER, article.poster);
        values.put(Articles.AUTHOR, article.author);
        values.put(Articles.AUTHOR_YEAR, article.authorYear);
        values.put(Articles.IS_FAVORITE, article.isFavorite);
        values.put(Articles.IS_READ, article.isRead);
        return ContentProviderOperation.newInsert(Articles.CONTENT_URI).withValues(values).build();
    }

    public static ContentProviderOperation buildIssueOp(Issue issue) {
        ContentValues values = new ContentValues();
        values.put(Articles._ID, issue.id);
        values.put(Articles.ISSUE_DATE, issue.issueDate.getTime());
        return ContentProviderOperation.newInsert(Issues.CONTENT_URI).withValues(values).build();
    }

    /**
     * Applies a large {@link ContentProviderOperation} batch in smaller batches as not to overload
     * the transaction cache.
     */
    public static void applyInSmallBatches(Context context,
                                           ArrayList<ContentProviderOperation> batch)
            throws OperationApplicationException {
        // split into smaller batches to not overload transaction cache
        // see http://developer.android.com/reference/android/os/TransactionTooLargeException.html
        ArrayList<ContentProviderOperation> smallBatch = new ArrayList<>();

        while (!batch.isEmpty()) {
            if (batch.size() <= SMALL_BATCH_SIZE) {
                // small enough already? apply right away
                applyBatch(context, batch);
                return;
            }

            // take up to 50 elements out of batch
            for (int count = 0; count < SMALL_BATCH_SIZE; count++) {
                if (batch.isEmpty()) {
                    break;
                }
                smallBatch.add(batch.remove(0));
            }
            // apply small batch
            applyBatch(context, smallBatch);
            // prepare for next small batch
            smallBatch.clear();
        }
    }

    private static void applyBatch(Context context, ArrayList<ContentProviderOperation> batch)
            throws OperationApplicationException {
        try {
            context.getContentResolver()
                    .applyBatch(NewsletterApplication.CONTENT_AUTHORITY, batch);
        } catch (RemoteException e) {
            // not using a remote provider, so this should never happen. crash if it does.
            throw new RuntimeException("Problem applying batch operation", e);
        }
    }
}
