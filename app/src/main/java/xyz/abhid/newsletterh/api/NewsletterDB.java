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

package xyz.abhid.newsletterh.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import xyz.abhid.newsletterh.services.ArticlesService;
import xyz.abhid.newsletterh.util.ServiceUtils;

public class NewsletterDB {

    private static final String API_URL = "http://newsletterhcse.16mb.com";

    private Context mContext;

    private Retrofit mRetrofit;

    /**
     * Create a new manager instance.
     */
    public NewsletterDB(Context context) {
        mContext = context;
    }

    protected Retrofit getRetrofit() {
        if (mRetrofit == null) {
            Retrofit.Builder builder = newRetrofitBuilder();

            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd").create();

            builder.baseUrl(API_URL);
            builder.addConverterFactory(GsonConverterFactory.create(gson));
            mRetrofit = builder.build();
        }
        return mRetrofit;
    }

    protected Retrofit.Builder newRetrofitBuilder() {
        return new Retrofit.Builder().client(ServiceUtils.getCachingOkHttpClient(mContext));
    }

    public ArticlesService articlesService() {
        return getRetrofit().create(ArticlesService.class);
    }
}
