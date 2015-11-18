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

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import xyz.abhid.newsletterh.R;
import xyz.abhid.newsletterh.provider.NewsletterDatabase;
import xyz.abhid.newsletterh.util.Utils;

public class AnimatedLogoFragment extends Fragment {

    public static final String TAG = "Animated Logo";

    private float mInitialLogoOffset;
    private int mAnimDuration;

    @Bind(R.id.textViewVersion) TextView mTextViewVersion;
    @Bind(R.id.animatedLogoView) ImageView mImageViewLogo;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInitialLogoOffset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16,
                getResources().getDisplayMetrics());
        mAnimDuration = getResources().getInteger(android.R.integer.config_longAnimTime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_animated_logo, container, false);

        ButterKnife.bind(this, rootView);

        StringBuilder versionFinal = new StringBuilder();
        versionFinal.append("v").append(Utils.getVersion(getContext()));
        versionFinal.append(" (Database v").append(NewsletterDatabase.DATABASE_VERSION).append(")");
        mTextViewVersion.setText(versionFinal);

        mImageViewLogo.setAlpha(0f);
        mImageViewLogo.setVisibility(View.VISIBLE);
        mImageViewLogo.setTranslationY(mInitialLogoOffset);
        mTextViewVersion.setAlpha(0f);
        mTextViewVersion.setVisibility(View.VISIBLE);
        mTextViewVersion.setTranslationY(-mInitialLogoOffset);

        final AnimatorSet set = new AnimatorSet();
        Interpolator interpolator = new OvershootInterpolator();
        final ObjectAnimator a1 = ObjectAnimator.ofFloat(mImageViewLogo, View.TRANSLATION_Y, 0);
        final ObjectAnimator a2 = ObjectAnimator.ofFloat(mImageViewLogo, View.ALPHA, 1);
        final ObjectAnimator a3 = ObjectAnimator.ofFloat(mTextViewVersion, View.TRANSLATION_Y, 0);
        final ObjectAnimator a4 = ObjectAnimator.ofFloat(mTextViewVersion, View.ALPHA, 1);

        a1.setInterpolator(interpolator);
        a3.setInterpolator(interpolator);
        a2.setStartDelay(mAnimDuration);
        a2.setDuration(mAnimDuration).start();
        a2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                set.setDuration(mAnimDuration).playTogether(a1, a3, a4);
                set.start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        return rootView;
    }
}