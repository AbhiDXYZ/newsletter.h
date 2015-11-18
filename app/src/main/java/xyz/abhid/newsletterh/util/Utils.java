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

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.AnyRes;
import android.support.annotation.AttrRes;
import android.text.TextUtils;
import android.util.TypedValue;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import xyz.abhid.newsletterh.Analytics;
import xyz.abhid.newsletterh.R;
import xyz.abhid.newsletterh.settings.AppSettings;

public class Utils {

    private static ColorGenerator sGenerator = ColorGenerator.MATERIAL;

    public static String getVersion(Context context) {
        String version;
        try {
            version = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_META_DATA).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            version = "UnknownVersion";
        }
        return version;
    }

    /**
     * Track a screen view. This is commonly called in {@link android.support.v4.app.Fragment#onStart()}.
     */
    public static void trackView(Context context, String screenName) {
        Tracker tracker = Analytics.getTracker(context);
        tracker.setScreenName(screenName);
        tracker.send(new HitBuilders.AppViewBuilder().build());
    }

    /**
     * Track a custom event that does not fit the {@link #trackAction(android.content.Context,
     * String, String)}, {@link #trackContextMenu(android.content.Context, String, String)} or
     * {@link #trackClick(android.content.Context, String, String)} trackers. Commonly important
     * status information.
     */
    public static void trackCustomEvent(Context context, String tag, String action,
                                        String label) {
        Analytics.getTracker(context).send(new HitBuilders.EventBuilder()
                .setCategory(tag)
                .setAction(action)
                .setLabel(label)
                .build());
    }

    /**
     * Track an action event, e.g. when an action item is clicked.
     */
    public static void trackAction(Context context, String tag, String label) {
        Analytics.getTracker(context).send(new HitBuilders.EventBuilder()
                .setCategory(tag)
                .setAction("Action Item")
                .setLabel(label)
                .build());
    }

    /**
     * Track a context menu event, e.g. when a context item is clicked.
     */
    public static void trackContextMenu(Context context, String tag, String label) {
        Analytics.getTracker(context).send(new HitBuilders.EventBuilder()
                .setCategory(tag)
                .setAction("Context Item")
                .setLabel(label)
                .build());
    }

    /**
     * Track a generic click that does not fit {@link #trackAction(android.content.Context, String,
     * String)} or {@link #trackContextMenu(android.content.Context, String, String)}.
     */
    public static void trackClick(Context context, String tag, String label) {
        Analytics.getTracker(context).send(new HitBuilders.EventBuilder()
                .setCategory(tag)
                .setAction("Click")
                .setLabel(label)
                .build());
    }

    /**
     * Resolves the given attribute to the resource id for the given theme.
     */
    @AnyRes
    public static int resolveAttributeToResourceId(Resources.Theme theme,
                                                   @AttrRes int attributeResId) {
        TypedValue outValue = new TypedValue();
        theme.resolveAttribute(attributeResId, outValue, true);
        return outValue.resourceId;
    }

    public static TextDrawable getTextDrawable(String value) {
        return TextDrawable.builder().buildRound(value, sGenerator.getColor(value));
    }

    /**
     * Returns false if there is an active, but metered (pre-Jelly Bean: non-WiFi) connection and
     * the user did not approve it for large data downloads (e.g. images).
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean isAllowedLargeDataConnection(Context context) {
        boolean isConnected;
        boolean largeDataOverWifiOnly = AppSettings.isLargeDataOverWiFiOnly(context);
        // check connection state
        if (largeDataOverWifiOnly) {
            if (AndroidUtils.isJellyBeanOrHigher()) {
                // better: only allow large data downloads over non-metered connections
                isConnected = AndroidUtils.isUnmeteredNetworkConnected(context);
            } else {
                // only allow large data downloads over WiFi,
                // assuming it is most likely to be not metered
                isConnected = AndroidUtils.isWifiConnected(context);
            }
        } else {
            isConnected = AndroidUtils.isNetworkConnected(context);
        }
        return isConnected;
    }

    public static boolean tryStartActivity(Context context, Intent intent, boolean displayError) {
        boolean handled = false;
        // check if an implicit intent can be handled (always true for explicit intents)
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            try {
                context.startActivity(intent);
                handled = true;
            } catch (ActivityNotFoundException ignored) {
                // catch failure to handle explicit intents
            }
        }
        if (displayError && !handled) {
            Toast.makeText(context, R.string.app_not_available, Toast.LENGTH_LONG).show();
        }
        return handled;
    }

    /**
     * Tries to launch a web browser loading the given URL. Sets a flag to exit the browser if
     * coming back to the app.
     */
    public static void launchWebsite(Context context, String url, String logTag, String logItem) {
        if (context == null || TextUtils.isEmpty(url)) {
            return;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        // try to launch web browser
        Utils.tryStartActivity(context, intent, true);

        Utils.trackAction(context, logTag, logItem);
    }
}
