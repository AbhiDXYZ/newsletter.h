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

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import timber.log.Timber;
import xyz.abhid.newsletterh.provider.NewsletterContract.Articles;
import xyz.abhid.newsletterh.provider.NewsletterContract.Issues;

@SuppressWarnings("unused")
public class NewsletterDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "newsletterDB";

    public static final int DB_VER_1 = 1;

    public static final int DATABASE_VERSION = DB_VER_1;
    private static final String CREATE_ARTICLES_TABLE = "CREATE TABLE " + Tables.ARTICLES

            + " ("

            + Articles._ID + " INTEGER PRIMARY KEY,"

            + Articles.ISSUE + " INTEGER NOT NULL,"

            + Articles.ISSUE_DATE + " INTEGER DEFAULT 0,"

            + Articles.TITLE + " TEXT NOT NULL,"

            + Articles.BODY + " TEXT DEFAULT '',"

            + Articles.POSTER + " TEXT DEFAULT '',"

            + Articles.AUTHOR + " TEXT DEFAULT '',"

            + Articles.AUTHOR_YEAR + " INTEGER DEFAULT 0,"

            + Articles.IS_FAVORITE + " INTEGER DEFAULT 0,"

            + Articles.IS_READ + " INTEGER DEFAULT 0"

            + ");";
    private static final String CREATE_ISSUES_TABLE = "CREATE TABLE " + Tables.ISSUES

            + " ("

            + Issues._ID + " INTEGER PRIMARY KEY,"

            + Issues.ISSUE_DATE + " INTEGER DEFAULT 0"

            + ");";

    public NewsletterDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Checks whether a table exists in the given database.
     */
    private static boolean isTableExisting(SQLiteDatabase db, String table) {
        Cursor cursor = db.query("sqlite_master", new String[]{"name"},
                "type='table' AND name=?", new String[]{table}, null, null, null, "1");
        if (cursor == null) {
            return false;
        }
        boolean isTableExisting = cursor.getCount() > 0;
        cursor.close();
        return isTableExisting;
    }

    /**
     * Checks whether the given column exists in the given table of the given database.
     */
    private static boolean isTableColumnMissing(SQLiteDatabase db, String table, String column) {
        Cursor cursor = db.query(table, null, null, null, null, null, null, "1");
        if (cursor == null) {
            return true;
        }
        boolean isColumnExisting = cursor.getColumnIndex(column) != -1;
        cursor.close();
        return !isColumnExisting;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ARTICLES_TABLE);
        db.execSQL(CREATE_ISSUES_TABLE);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.d("Can't downgrade from version " + oldVersion + " to " + newVersion);
        onResetDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.d("Upgrading from " + oldVersion + " to " + newVersion);

        // run necessary upgrades
        int version = oldVersion;

        // drop all tables if version is not right
        Timber.d("After upgrade at version " + version);
        if (version != DATABASE_VERSION) {
            onResetDatabase(db);
        }
    }

    /**
     * Drops all tables and creates an empty database.
     */
    private void onResetDatabase(SQLiteDatabase db) {
        Timber.w("Resetting database");
        db.execSQL("DROP TABLE IF EXISTS " + Tables.ARTICLES);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.ISSUES);
        onCreate(db);
    }

    public interface Tables {

        String ARTICLES = "articles";

        String ISSUES = "issues";
    }

}
