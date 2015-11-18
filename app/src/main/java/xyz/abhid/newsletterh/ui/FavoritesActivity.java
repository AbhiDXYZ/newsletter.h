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

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import xyz.abhid.newsletterh.R;
import xyz.abhid.newsletterh.adapters.ArticlesAdapter;
import xyz.abhid.newsletterh.provider.NewsletterContract.Articles;
import xyz.abhid.newsletterh.util.AndroidUtils;
import xyz.abhid.newsletterh.widgets.CustomRecyclerView;

public class FavoritesActivity extends BaseTopActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = "Favorites";

    private ArticlesAdapter mResultsAdapter;

    @Bind(R.id.recyclerView) CustomRecyclerView mRecyclerView;
    @Bind(R.id.textViewEmpty) TextView mTextViewEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_articles);
        setupActionBar();
        setupNavDrawer();
        setupProgressBar();

        ButterKnife.bind(this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mResultsAdapter = new ArticlesAdapter(this, TAG, null);
        mRecyclerView.setAdapter(mResultsAdapter);
        mRecyclerView.setEmptyView(mTextViewEmpty);

        setProgressVisible(true, false);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void setupActionBar() {
        super.setupActionBar();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.favorites);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        setDrawerSelectedItem(R.id.navigationItemFavorites);
    }

    @Override
    protected void onResume() {
        super.onResume();

        supportInvalidateOptionsMenu();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        mTextViewEmpty.setText(R.string.loading);
        String selection = Articles.IS_FAVORITE + "=1";
        return new CursorLoader(this, Articles.CONTENT_URI, Articles.PROJECTION,
                selection, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!AndroidUtils.isNetworkConnected(this)) {
            setProgressVisible(false, false);
            mTextViewEmpty.setText(R.string.offline);
        } else {
            setProgressVisible(false, false);
            mTextViewEmpty.setText(R.string.favorites_empty);
        }
        mResultsAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mResultsAdapter.swapCursor(null);
    }
}
