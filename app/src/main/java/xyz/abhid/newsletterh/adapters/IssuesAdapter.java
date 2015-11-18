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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DateFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import xyz.abhid.newsletterh.R;
import xyz.abhid.newsletterh.entities.Issue;
import xyz.abhid.newsletterh.ui.ViewIssueActivity;
import xyz.abhid.newsletterh.util.Utils;

public class IssuesAdapter extends CursorRecyclerViewAdapter<IssuesAdapter.ViewHolder> {

    public final String TAG;

    public IssuesAdapter(Context context, String TAG, Cursor cursor) {
        super(context, cursor);

        this.TAG = TAG;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_issue, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final Cursor cursor) {
        final Issue issue = Issue.fromCursor(cursor);

        holder.mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.startActivity((AppCompatActivity) getContext(),
                        ViewIssueActivity.getStartIntent(getContext(), issue.id),
                        ActivityOptionsCompat.makeScaleUpAnimation(v, 0, 0, v.getWidth(),
                                v.getHeight()).toBundle());
                fireTrackerEvent("Item");
            }
        });

        holder.mImageViewIssue.setImageDrawable(Utils.getTextDrawable(
                String.valueOf(issue.id)));
        String details = "Issue " + issue.id + " - "
                + DateFormat.getDateInstance(DateFormat.MEDIUM).format(issue.issueDate);
        holder.mTextViewDetails.setText(details);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View mRootView;

        @Bind(R.id.imageViewIssue) ImageView mImageViewIssue;
        @Bind(R.id.textViewDetails) TextView mTextViewDetails;

        public ViewHolder(View view) {
            super(view);

            this.mRootView = view;

            ButterKnife.bind(this, view);
        }
    }

    private void fireTrackerEvent(String label) {
        Utils.trackAction(getContext(), TAG, label);
    }
}