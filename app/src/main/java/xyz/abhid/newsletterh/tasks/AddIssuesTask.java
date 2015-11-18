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

import android.content.Context;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import timber.log.Timber;
import xyz.abhid.newsletterh.api.NewsletterDB;
import xyz.abhid.newsletterh.entities.IssueResultsPage;
import xyz.abhid.newsletterh.util.AndroidUtils;
import xyz.abhid.newsletterh.util.ArticleTools;
import xyz.abhid.newsletterh.util.ServiceUtils;

public class AddIssuesTask extends BaseAsyncTask {

    public AddIssuesTask(Context context) {
        super(context);
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (!AndroidUtils.isNetworkConnected(getContext())) {
            Timber.d("Finished. No internet connection.");
            publishProgress(ADD_OFFLINE);
            return null;
        }

        NewsletterDB newsletterDB = ServiceUtils.getNewsletterDB(getContext());
        Call<IssueResultsPage> call = newsletterDB.articlesService().issues();
        call.enqueue(new Callback<IssueResultsPage>() {
            @Override
            public void onResponse(Response<IssueResultsPage> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    IssueResultsPage page = response.body();
                    if (page != null && page.results != null) {
                        ArticleTools.get(getContext()).insertIssues(getContext(), page);
                        publishProgress(ADD_SUCCESS);
                    } else {
                        publishProgress(ADD_EMPTY);
                    }
                }
            }

            @Override
            public void onFailure(Throwable t) {
                publishProgress(ADD_ERROR);
                Timber.e(t, "Adding articles failed");
            }
        });
        return null;
    }

}
