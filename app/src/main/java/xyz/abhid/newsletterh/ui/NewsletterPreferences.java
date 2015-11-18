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

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.ActionBar;

import com.google.android.gms.analytics.GoogleAnalytics;

import xyz.abhid.newsletterh.R;
import xyz.abhid.newsletterh.provider.NewsletterDatabase;
import xyz.abhid.newsletterh.settings.AppSettings;
import xyz.abhid.newsletterh.util.ThemeUtils;
import xyz.abhid.newsletterh.util.Utils;

public class NewsletterPreferences extends BaseOnTopActivity {

    public static final String TAG = "Settings";

    public static @StyleRes int THEME = R.style.Theme_Newsletter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_single_pane);
        setupActionBar();

        if (savedInstanceState == null) {
            Fragment f = new SettingsFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.containerFragment, f);
            ft.commit();
        }
    }

    @Override
    protected void setupActionBar() {
        super.setupActionBar();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.settings);
        }
    }

    public static class SettingsFragment extends PreferenceFragment {

        public static void setListPreferenceSummary(@Nullable ListPreference listPref) {
            if (listPref == null) {
                return;
            }
            // Set summary to be the user-description for the selected value
            CharSequence entry = listPref.getEntry();
            listPref.setSummary(entry == null ? "" : entry.toString().replaceAll("%", "%%"));
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings);

            setupBasicSettings();
            setupAdvancedSettings();
            setupAboutSettings();
        }

        private void setupBasicSettings() {
            // Theme switcher
            Preference themePref = findPreference(AppSettings.KEY_THEME);
            themePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (AppSettings.KEY_THEME.equals(preference.getKey())) {
                        ThemeUtils.updateTheme((String) newValue);

                        // restart to apply new theme, go back to this settings screen
                        TaskStackBuilder.create(getActivity())
                                .addNextIntent(new Intent(getActivity(), LatestIssueActivity.class))
                                .addNextIntent(getActivity().getIntent())
                                .startActivities();

                        fireTrackerEvent("Theme");
                    }

                    return true;
                }
            });
            setListPreferenceSummary((ListPreference) themePref);
        }

        private void setupAdvancedSettings() {
            findPreference(AppSettings.KEY_CLEAR_CACHE)
                    .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                        public boolean onPreferenceClick(Preference preference) {
                            // try to open app info where user can clear app cache folders
                            Intent intent = new Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.setData(Uri.parse("package:" + getActivity().getPackageName()));
                            if (!Utils.tryStartActivity(getActivity(), intent, false)) {
                                // try to open all apps view if detail view not available
                                intent = new Intent(android.provider.
                                        Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                Utils.tryStartActivity(getActivity(), intent, true);
                            }
                            fireTrackerEvent("Clear cache");
                            return true;
                        }
                    });

            // GA opt-out
            findPreference(AppSettings.KEY_GOOGLE_ANALYTICS).setOnPreferenceChangeListener(
                    new Preference.OnPreferenceChangeListener() {
                        @Override
                        public boolean onPreferenceChange(Preference preference, Object newValue) {
                            if (preference.getKey().equals(AppSettings.KEY_GOOGLE_ANALYTICS)) {
                                boolean isEnabled = (Boolean) newValue;
                                GoogleAnalytics.getInstance(getActivity()).setAppOptOut(isEnabled);
                                Utils.trackCustomEvent(getActivity(), TAG, "Google Analytics",
                                        isEnabled ? "Enable" : "Disable");
                                return true;
                            }
                            return false;
                        }
                    });
        }

        private void setupAboutSettings() {
            // display version number and database version in About pref
            final String versionFinal = Utils.getVersion(getActivity());
            findPreference(AppSettings.KEY_VERSION).setSummary("v" + versionFinal + " (Database v"
                    + NewsletterDatabase.DATABASE_VERSION + ")");

            findPreference(AppSettings.KEY_VERSION).setOnPreferenceClickListener(
                    new Preference.OnPreferenceClickListener() {
                        @Override
                        public boolean onPreferenceClick(Preference preference) {
                            Utils.launchWebsite(getActivity(),
                                    getString(R.string.url_project), TAG, "Version");
                            return false;
                        }
                    });
        }

        private void fireTrackerEvent(String label) {
            Utils.trackAction(getActivity(), TAG, label);
        }
    }
}
