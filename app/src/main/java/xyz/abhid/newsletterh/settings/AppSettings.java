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

package xyz.abhid.newsletterh.settings;

import android.content.Context;
import android.preference.PreferenceManager;

public class AppSettings {

    public static final String KEY_HAS_SEEN_NAV_DRAWER = "hasSeenNavDrawer";

    public static final String KEY_WIFI_ONLY = "autoUpdateWiFiOnly";

    public static final String KEY_THEME = "theme";

    public static final String KEY_CLEAR_CACHE = "clearCache";

    public static final String KEY_GOOGLE_ANALYTICS = "enableGAnalytics";

    public static final String KEY_VERSION = "version";

    /**
     * Whether the user was shown the nav drawer for this app.
     */
    public static boolean hasSeenNavDrawer(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_HAS_SEEN_NAV_DRAWER, false);
    }

    /**
     * Whether the user wants us to download larger chunks of data (e.g. images) only over a Wi-Fi
     * connection.
     */
    public static boolean isLargeDataOverWiFiOnly(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                KEY_WIFI_ONLY, false);
    }

    public static String getThemeIndex(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).
                getString(KEY_THEME, "0");
    }

    public static boolean isGaAppOptOut(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_GOOGLE_ANALYTICS, false);
    }
}
