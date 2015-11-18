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

package xyz.abhid.newsletterh.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import xyz.abhid.newsletterh.R;
import xyz.abhid.newsletterh.entities.Article;
import xyz.abhid.newsletterh.ui.ViewArticleActivity;
import xyz.abhid.newsletterh.util.AnimationUtils;
import xyz.abhid.newsletterh.util.ArticleTools;
import xyz.abhid.newsletterh.util.ServiceUtils;
import xyz.abhid.newsletterh.util.Utils;

public class ArticlesAdapter extends CursorRecyclerViewAdapter<ArticlesAdapter.ViewHolder> {

    public final String TAG;

    private final int mResIdFavorite;
    private final int mResIdFavoriteZero;

    public ArticlesAdapter(Context context, String TAG, Cursor cursor) {
        super(context, cursor);

        this.TAG = TAG;

        mResIdFavorite = Utils.resolveAttributeToResourceId(context.getTheme(),
                R.attr.drawableFavorite);
        mResIdFavoriteZero = Utils.resolveAttributeToResourceId(context.getTheme(),
                R.attr.drawableFavoriteOutline);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_article, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final Cursor cursor) {
        final Article article = Article.fromCursor(cursor);

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArticleTools.get(getContext()).storeIsRead(article.id, true);
                ActivityCompat.startActivity((AppCompatActivity) getContext(),
                        ViewArticleActivity.getStartIntent(getContext(), article.id),
                        ActivityOptionsCompat.makeScaleUpAnimation(v, 0, 0, v.getWidth(),
                                v.getHeight()).toBundle());
                fireTrackerEvent("Item");

            }
        });

        if (TextUtils.isEmpty(article.poster)) {
            ServiceUtils.loadWithPicasso(getContext(),
                    R.drawable.ic_background).into(holder.mImageViewPoster);
        } else {
            ServiceUtils.loadWithPicasso(getContext(), article.poster)
                    .into(holder.mImageViewPoster);
        }
        holder.mTextViewTitle.setText(article.title);
        holder.mTextViewIssueDetails.setText(
                buildIssueDetailsString(article.issue, cursor.getPosition() + 1));
        holder.mTextViewIssueDate.setText(DateFormat.getDateInstance(DateFormat.MEDIUM)
                .format(article.issueDate));
        StringBuilder authorDetails = new StringBuilder();
        authorDetails.append(article.author);
        if (article.authorYear != 0) {
            authorDetails.append(" | ").append(article.authorYear).append(" year");
        }
        holder.mTextViewAuthor.setText(authorDetails);
        setFavoriteState(holder.mImageViewFavorite, article.isFavorite);

        holder.mImageViewFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArticleTools.get(getContext()).storeIsFavorite(article.id, !article.isFavorite);
                AnimationUtils.tada(v).start();
                fireTrackerEvent("Favorite");
            }
        });
    }

    public void setFavoriteState(ImageView view, boolean isFavorite) {
        view.setImageResource(isFavorite ? mResIdFavorite : mResIdFavoriteZero);
    }

    public String buildIssueDetailsString(int issue, int position) {
        return (issue + "x" + position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        CardView mCardView;

        @Bind(R.id.imageViewPoster) ImageView mImageViewPoster;
        @Bind(R.id.textViewTitle) TextView mTextViewTitle;
        @Bind(R.id.textViewIssueDetails) TextView mTextViewIssueDetails;
        @Bind(R.id.textViewIssueDate) TextView mTextViewIssueDate;
        @Bind(R.id.textViewAuthor) TextView mTextViewAuthor;
        @Bind(R.id.imageViewFavorite) ImageView mImageViewFavorite;

        public ViewHolder(View view) {
            super(view);

            mCardView = (CardView) view;

            ButterKnife.bind(this, view);
        }
    }

    private void fireTrackerEvent(String label) {
        Utils.trackAction(getContext(), TAG, label);
    }
}
