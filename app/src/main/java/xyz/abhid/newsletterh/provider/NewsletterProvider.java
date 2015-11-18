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

package xyz.abhid.newsletterh.provider;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;

import timber.log.Timber;
import xyz.abhid.newsletterh.NewsletterApplication;
import xyz.abhid.newsletterh.provider.NewsletterContract.Articles;
import xyz.abhid.newsletterh.provider.NewsletterContract.Issues;
import xyz.abhid.newsletterh.provider.NewsletterDatabase.Tables;
import xyz.abhid.newsletterh.util.SelectionBuilder;

public class NewsletterProvider extends ContentProvider {

    public static final boolean LOG_V = false;

    private static final int ARTICLES = 100;
    private static final int ARTICLES_ID = 101;
    private static final int ISSUES = 200;
    private static final int ISSUES_ID = 201;
    private static final int RENEW_FTS_TABLE = 1000;

    private static UriMatcher sUriMatcher;
    private final ThreadLocal<Boolean> mApplyingBatch = new ThreadLocal<>();

    protected SQLiteDatabase mDb;
    private NewsletterDatabase mDbHelper;

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri} variations supported by
     * this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher(Context context) {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = NewsletterApplication.CONTENT_AUTHORITY;

        matcher.addURI(authority, NewsletterContract.PATH_ARTICLES, ARTICLES);
        matcher.addURI(authority, NewsletterContract.PATH_ARTICLES + "/*", ARTICLES_ID);

        matcher.addURI(authority, NewsletterContract.PATH_ISSUES, ISSUES);
        matcher.addURI(authority, NewsletterContract.PATH_ISSUES + "/*", ISSUES_ID);

        // Ops
        matcher.addURI(authority, NewsletterContract.PATH_RENEW_FTS_TABLE, RENEW_FTS_TABLE);

        return matcher;
    }

    /**
     * Builds selection using a {@link SelectionBuilder} to match the requested {@link Uri}.
     */
    private static SelectionBuilder buildSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();

        switch (match) {
            case ARTICLES: {
                return builder.table(Tables.ARTICLES);
            }
            case ARTICLES_ID: {
                final String articleId = Articles.getArticleId(uri);

                return builder.table(Tables.ARTICLES).where(Articles._ID + "=?", articleId);
            }
            case ISSUES: {
                return builder.table(Tables.ISSUES);
            }
            case ISSUES_ID: {
                final String issueId = Issues.getIssueId(uri);
                return builder.table(Tables.ISSUES).where(Issues._ID + "=?", issueId);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    @Override
    public void shutdown() {
        /**
         * If we ever do unit-testing, nice to have this already (no bug-hunt).
         */
        if (mDbHelper != null) {
            mDbHelper.close();
            mDbHelper = null;
            mDb = null;
        }
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        sUriMatcher = buildUriMatcher(context);
        mDbHelper = new NewsletterDatabase(context);
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        if (LOG_V) {
            Timber.v("query(uri=" + uri + ", proj=" + Arrays.toString(projection) + ")");
        }

        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            default: {
                // Most cases are handled with simple SelectionBuilder
                final SelectionBuilder builder = buildSelection(uri, match);
                Cursor query = builder
                        .where(selection, selectionArgs)
                        .query(db, projection, sortOrder);
                if (query != null) {
                    query.setNotificationUri(getContext().getContentResolver(), uri);
                }
                return query;
            }
        }
    }

    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ARTICLES:
                return Articles.CONTENT_TYPE;
            case ARTICLES_ID:
                return Articles.CONTENT_ITEM_TYPE;
            case ISSUES:
                return Issues.CONTENT_TYPE;
            case ISSUES_ID:
                return Issues.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Uri newItemUri;
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        if (!applyingBatch()) {
            db.beginTransaction();
            try {
                newItemUri = insertInTransaction(db, uri, values);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } else {
            newItemUri = insertInTransaction(db, uri, values);
        }
        if (newItemUri != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return newItemUri;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        int numValues = values.length;
        boolean notifyChange = false;
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < numValues; i++) {
                Uri result = insertInTransaction(db, uri, values[i]);
                if (result != null) {
                    notifyChange = true;
                }
                db.yieldIfContendedSafely();
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        if (notifyChange) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return numValues;
    }

    private Uri insertInTransaction(SQLiteDatabase db, Uri uri, ContentValues values) {
        if (LOG_V) {
            Timber.v("insert(uri=" + uri + ", values=", values.toString(), ")");
        }

        Uri notifyUri = null;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ARTICLES: {
                long id = db.insert(Tables.ARTICLES, null, values);
                if (id < 0) {
                    break;
                }
                notifyUri = Articles.buildArticleUri(values.getAsString(Articles._ID));
                break;
            }
            case ISSUES: {
                long id = db.insert(Tables.ISSUES, null, values);
                if (id < 0) {
                    break;
                }
                notifyUri = Issues.buildIssueUri(values.getAsString(Issues._ID));
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
        return notifyUri;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if (LOG_V) {
            Timber.v("update(uri=" + uri + ", values=" + values.toString() + ")");
        }

        int count = 0;
        if (!applyingBatch()) {
            final SQLiteDatabase db = mDbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                count = buildSelection(uri, sUriMatcher.match(uri))
                        .where(selection, selectionArgs)
                        .update(db, values);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } else {
            mDb = mDbHelper.getWritableDatabase();
            count = buildSelection(uri, sUriMatcher.match(uri))
                    .where(selection, selectionArgs)
                    .update(mDb, values);
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        if (LOG_V) {
            Timber.v("delete(uri=" + uri + ")");
        }

        int count = 0;
        if (!applyingBatch()) {
            final SQLiteDatabase db = mDbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                count = buildSelection(uri, sUriMatcher.match(uri))
                        .where(selection, selectionArgs)
                        .delete(db);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        } else {
            mDb = mDbHelper.getWritableDatabase();
            count = buildSelection(uri, sUriMatcher.match(uri))
                    .where(selection, selectionArgs)
                    .delete(mDb);
        }
        if (count > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return count;
    }

    /**
     * Apply the given set of {@link ContentProviderOperation}, executing inside a {@link
     * SQLiteDatabase} transaction. All changes will be rolled back if any single one fails.
     */
    @NonNull
    @Override
    public ContentProviderResult[] applyBatch(
            @NonNull ArrayList<ContentProviderOperation> operations)
            throws OperationApplicationException {
        final int numOperations = operations.size();
        if (numOperations == 0) {
            return new ContentProviderResult[0];
        }

        mDb = mDbHelper.getWritableDatabase();
        mDb.beginTransaction();
        try {
            mApplyingBatch.set(true);
            final ContentProviderResult[] results = new ContentProviderResult[numOperations];
            for (int i = 0; i < numOperations; i++) {
                final ContentProviderOperation operation = operations.get(i);
                if (i > 0 && operation.isYieldAllowed()) {
                    mDb.yieldIfContendedSafely();
                }
                results[i] = operation.apply(this, results, i);
            }
            mDb.setTransactionSuccessful();
            return results;
        } finally {
            mApplyingBatch.set(false);
            mDb.endTransaction();
        }
    }

    private boolean applyingBatch() {
        return mApplyingBatch.get() != null && mApplyingBatch.get();
    }
}