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

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import xyz.abhid.newsletterh.R;
import xyz.abhid.newsletterh.entities.Article;
import xyz.abhid.newsletterh.provider.NewsletterContract.Articles;
import xyz.abhid.newsletterh.util.AnimationUtils;
import xyz.abhid.newsletterh.util.ArticleTools;
import xyz.abhid.newsletterh.util.ServiceUtils;
import xyz.abhid.newsletterh.util.Utils;

public class ViewArticleActivity extends BaseOnTopActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = "View article";

    private Cursor mCursor;

    private int mArticleId;
    private int mResIdFavorite;
    private int mResIdFavoriteZero;
    private boolean mAnimationPlayedOnce = false;

    @Bind(R.id.collapsingToolbar) CollapsingToolbarLayout mCollapsingToolbar;
    @Bind(R.id.imageViewBackdrop) ImageView mImageViewBackdrop;
    @Bind(R.id.imageViewBackground) ImageView mImageViewBackground;
    @Bind(R.id.imageViewIssue) ImageView mImageViewIssue;
    @Bind(R.id.textViewTitle) TextView mTextViewTitle;
    @Bind(R.id.textViewBody) TextView mTextViewBody;
    @Bind(R.id.textViewDetails) TextView mTextViewDetails;
    @Bind(R.id.fabFavorite) FloatingActionButton mFabFavorite;

    public static Intent getStartIntent(Context context, int articleId) {
        Intent starter = new Intent(context, ViewArticleActivity.class);
        starter.putExtra(InitBundle.ID, articleId);
        return starter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_article);
        setupActionBar();

        ButterKnife.bind(this);

        mArticleId = getIntent().getExtras().getInt(InitBundle.ID);

        mResIdFavorite = R.drawable.ic_action_favorite;
        mResIdFavoriteZero = R.drawable.ic_action_favorite_outline;

        mFabFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onFavoriteArticle();
                AnimationUtils.tada(v, 3f).start();
                fireTrackerEvent("Favorite");
            }
        });

        getSupportLoaderManager().initLoader(0, null, this);
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

    @Override
    protected void setupActionBar() {
        super.setupActionBar();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        String selection = Articles._ID + "=" + mArticleId;
        return new CursorLoader(this, Articles.CONTENT_URI, Articles.PROJECTION,
                selection, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        onPopulateArticleData(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursor = null;
    }

    public void setFavoriteState(FloatingActionButton fab, boolean isFavorite) {
        fab.setImageResource(isFavorite ? mResIdFavorite : mResIdFavoriteZero);
    }

    private void onPopulateArticleData(Cursor cursor) {
        mCursor = cursor;
        if (mCursor != null && mCursor.moveToFirst()) {
            Article article = Article.fromCursor(mCursor);
            if (TextUtils.isEmpty(article.poster)) {
                ServiceUtils.loadWithPicasso(this, R.drawable.ic_background_land)
                        .into(mImageViewBackdrop);
                ServiceUtils.loadWithPicasso(this, R.drawable.ic_background)
                        .into(mImageViewBackground);
            } else {
                ServiceUtils.loadWithPicasso(this, article.poster).into(mImageViewBackdrop);
                ServiceUtils.loadWithPicasso(this, article.poster).into(mImageViewBackground);
            }
            mImageViewBackdrop.getViewTreeObserver().addOnGlobalLayoutListener(
                    new ViewTreeObserver.OnGlobalLayoutListener() {

                        @Override
                        public void onGlobalLayout() {
                            if (!mAnimationPlayedOnce) {
                                AnimationUtils.circularReveal(getApplicationContext(),
                                        mImageViewBackdrop);
                                mAnimationPlayedOnce = true;
                            }
                        }
                    });
            mImageViewIssue.setImageDrawable(Utils.getTextDrawable(String.valueOf(article.issue)));
            mTextViewTitle.setText(article.title);
            mTextViewBody.setText(article.body);
            mTextViewDetails.setText(buildDetailsString(article));
            setFavoriteState(mFabFavorite, article.isFavorite);
        }
    }

    public String buildDetailsString(Article article) {
        StringBuilder issueDetails = new StringBuilder();
        if (!TextUtils.isEmpty(article.author)) {
            issueDetails.append(article.author);
            if (article.authorYear != 0) {
                issueDetails.append(" | ").append(article.authorYear).append(" year");
            }
            issueDetails.append("\n");
        }
        issueDetails.append(DateFormat.getDateInstance(DateFormat.MEDIUM)
                .format(article.issueDate));
        return issueDetails.toString();
    }

    private void onFavoriteArticle() {
        if (mCursor != null && mCursor.moveToFirst()) {
            Article article = Article.fromCursor(mCursor);
            ArticleTools.get(this).storeIsFavorite(article.id, !article.isFavorite);
        }
    }

    public interface InitBundle {
        String ID = "article";
    }

    private void fireTrackerEvent(String label) {
        Utils.trackAction(this, TAG, label);
    }
}
