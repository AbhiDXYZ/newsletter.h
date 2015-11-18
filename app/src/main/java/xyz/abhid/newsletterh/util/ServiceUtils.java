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

import android.content.Context;
import android.os.Build;
import android.os.StatFs;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.util.concurrent.TimeUnit;

import xyz.abhid.newsletterh.api.NewsletterDB;

public class ServiceUtils {

    static final int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
    static final int READ_TIMEOUT_MILLIS = 20 * 1000; // 20s
    private static final String API_CACHE = "api-cache";
    private static final int MIN_DISK_API_CACHE_SIZE = 2 * 1024 * 1024; // 2MB
    private static final int MAX_DISK_API_CACHE_SIZE = 10 * 1024 * 1024; // 10MB

    private static OkHttpClient sCachingHttpClient;

    private static NewsletterDB sNewsletterDB;

    private static Picasso sPicasso;

    /**
     * Returns this apps {@link com.squareup.okhttp.OkHttpClient} with enabled response cache.
     * Should be used with API calls.
     */
    @NonNull
    public static synchronized OkHttpClient getCachingOkHttpClient(Context context) {
        if (sCachingHttpClient == null) {
            sCachingHttpClient = new OkHttpClient();
            sCachingHttpClient.setConnectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            sCachingHttpClient.setReadTimeout(READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
            File cacheDir = createApiCacheDir(context);
            sCachingHttpClient.setCache(new Cache(cacheDir, calculateApiDiskCacheSize(cacheDir)));
        }
        return sCachingHttpClient;
    }

    static File createApiCacheDir(Context context) {
        File cache = new File(context.getApplicationContext().getCacheDir(), API_CACHE);
        if (!cache.exists()) {
            cache.mkdirs();
        }
        return cache;
    }

    static long calculateApiDiskCacheSize(File dir) {
        long size = MIN_DISK_API_CACHE_SIZE;
        try {
            StatFs statFs = new StatFs(dir.getAbsolutePath());
            long available;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                available = statFs.getBlockCountLong() * statFs.getBlockSizeLong();
            } else {
                available = ((long) statFs.getBlockCount()) * statFs.getBlockSize();
            }
            // Target 2% of the total space.
            size = available / 50;
        } catch (IllegalArgumentException ignored) {
        }
        // Bound inside min/max size for disk cache.
        return Math.max(Math.min(size, MAX_DISK_API_CACHE_SIZE), MIN_DISK_API_CACHE_SIZE);
    }

    @NonNull
    public static synchronized NewsletterDB getNewsletterDB(Context context) {
        if (sNewsletterDB == null) {
            sNewsletterDB = new NewsletterDB(context);
        }
        return sNewsletterDB;
    }

    @NonNull
    public static synchronized Picasso getPicasso(Context context) {
        if (sPicasso == null) {
            sPicasso = new Picasso.Builder(context).build();
        }
        return sPicasso;
    }

    @NonNull
    public static RequestCreator loadWithPicasso(Context context, String path) {
        RequestCreator requestCreator = ServiceUtils.getPicasso(context).load(path);
        if (!Utils.isAllowedLargeDataConnection(context)) {
            // avoid the network, hit the cache immediately + accept stale images.
            requestCreator.networkPolicy(NetworkPolicy.OFFLINE);
        }
        return requestCreator;
    }

    @NonNull
    public static RequestCreator loadWithPicasso(Context context, @DrawableRes int resId) {
        RequestCreator requestCreator = ServiceUtils.getPicasso(context).load(resId);
        if (!Utils.isAllowedLargeDataConnection(context)) {
            // avoid the network, hit the cache immediately + accept stale images.
            requestCreator.networkPolicy(NetworkPolicy.OFFLINE);
        }
        return requestCreator;
    }
}
