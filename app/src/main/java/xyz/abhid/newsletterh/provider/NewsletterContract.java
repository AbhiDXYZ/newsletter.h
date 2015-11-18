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

import android.net.Uri;
import android.provider.BaseColumns;

import xyz.abhid.newsletterh.NewsletterApplication;

@SuppressWarnings("unused")
public class NewsletterContract {

    public static final String PATH_ARTICLES = "articles";
    public static final String PATH_ISSUES = "issues";
    public static final String PATH_RENEW_FTS_TABLE = "renewFTSTable";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://"
            + NewsletterApplication.CONTENT_AUTHORITY);

    public interface ArticlesColumns {

        String REF_ARTICLE_ID = "articleId";

        String ISSUE = "issue";
        String ISSUE_DATE = "issueDate";
        String TITLE = "title";
        String BODY = "body";
        String POSTER = "poster";
        String AUTHOR = "author";
        String AUTHOR_YEAR = "authorYear";
        String IS_FAVORITE = "isFavorite";
        String IS_READ = "isRead";

        String[] PROJECTION = new String[]{BaseColumns._ID, ISSUE, ISSUE_DATE, TITLE, BODY, POSTER,
                AUTHOR, AUTHOR_YEAR, IS_FAVORITE, IS_READ};
    }

    public interface IssuesColumns {

        String REF_ISSUE_ID = "issueId";

        String ISSUE_DATE = "issueDate";

        String[] PROJECTION = new String[]{BaseColumns._ID, ISSUE_DATE};
    }

    public static class Articles implements ArticlesColumns, BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_ARTICLES)
                .build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.newsletterh.article";

        public static final String CONTENT_ITEM_TYPE
                = "vnd.android.cursor.item/vnd.newsletterh.article";

        public static Uri buildArticleUri(String articleId) {
            return CONTENT_URI.buildUpon().appendPath(articleId).build();
        }

        public static Uri buildArticleUri(int articleId) {
            return buildArticleUri(String.valueOf(articleId));
        }

        public static String getArticleId(Uri uri) {
            return uri.getLastPathSegment();
        }
    }

    public static class Issues implements IssuesColumns, BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_ISSUES)
                .build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.newsletterh.issues";

        public static final String CONTENT_ITEM_TYPE
                = "vnd.android.cursor.item/vnd.newsletterh.issues";

        public static Uri buildIssueUri(String issueId) {
            return CONTENT_URI.buildUpon().appendPath(issueId).build();
        }

        public static Uri buildIssueUri(int issueId) {
            return buildIssueUri(String.valueOf(issueId));
        }

        public static String getIssueId(Uri uri) {
            return uri.getLastPathSegment();
        }
    }
}