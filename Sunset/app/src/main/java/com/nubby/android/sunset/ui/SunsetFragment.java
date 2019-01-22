package com.nubby.android.sunset.ui;

import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import com.nubby.android.sunset.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class SunsetFragment extends Fragment {
    private enum AnimatorDirection{
        FORWARD, BACKWARD;
    }
    private View mSceneView;
    private View mSunView;
    private View mSkyView;
    private int mBlueSkyColor;
    private int mSunsetSkyColor;
    private int mNightSkyColor;
    private float mSunStartingPos;
    private float mSunHeight;
    private AnimatorSet mCurrentAnimation;
    private AnimatorDirection mAnimatorDirection;

    public static SunsetFragment newInstance() {

        Bundle args = new Bundle();

        SunsetFragment fragment = new SunsetFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sunset, container, false);
        mSceneView = view;
        mSkyView = view.findViewById(R.id.sky);
        mSunView = view.findViewById(R.id.sun);

        Resources resources = getResources();
        mBlueSkyColor = resources.getColor(R.color.blue_sky);
        mSunsetSkyColor = resources.getColor(R.color.sunset_sky);
        mNightSkyColor = resources.getColor(R.color.night_sky);


        mAnimatorDirection = AnimatorDirection.FORWARD;
        mSceneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mSunStartingPos = mSunView.getTop();
                startAnimation(mAnimatorDirection);
                if (mAnimatorDirection == AnimatorDirection.FORWARD) {
                    mAnimatorDirection = AnimatorDirection.BACKWARD;
                } else {
                    mAnimatorDirection = AnimatorDirection.FORWARD;
                }
            }
        });

        ObjectAnimator sunAnimator = ObjectAnimator.ofFloat(mSunView, "rotation", 0f, 360f);
        sunAnimator.setDuration(10000);
        sunAnimator.setRepeatCount(ValueAnimator.INFINITE);
        sunAnimator.setRepeatMode(ValueAnimator.RESTART);
        sunAnimator.setInterpolator(new LinearInterpolator());
        sunAnimator.start();

        return view;
    }

    private void startAnimation(AnimatorDirection direction) {
        float sunYStart;
        float sunYEnd;

        if (direction == AnimatorDirection.FORWARD) {
            sunYStart = mSunStartingPos;
            sunYEnd = mSkyView.getHeight();
        } else {
            sunYStart = mSkyView.getHeight();
            sunYEnd = mSunStartingPos;
        }

        ObjectAnimator heightAnimator = ObjectAnimator
                .ofFloat(mSunView, "y", sunYStart, sunYEnd)
                .setDuration(3000);
        ObjectAnimator colorAnimator;
        ObjectAnimator nightSkyAnimator;

        if (direction == AnimatorDirection.FORWARD) {
            colorAnimator = ObjectAnimator
                    .ofInt(mSkyView, "backgroundColor", mBlueSkyColor, mSunsetSkyColor)
                    .setDuration(3000);
            nightSkyAnimator = ObjectAnimator
                    .ofInt(mSkyView, "backgroundColor", mSunsetSkyColor, mNightSkyColor)
                    .setDuration(1500);
        } else {
            colorAnimator = ObjectAnimator
                    .ofInt(mSkyView, "backgroundColor",mSunsetSkyColor, mBlueSkyColor)
                    .setDuration(3000);
            nightSkyAnimator = ObjectAnimator
                    .ofInt(mSkyView, "backgroundColor", mNightSkyColor, mSunsetSkyColor)
                    .setDuration(1500);
        }


        heightAnimator.setInterpolator(new AccelerateInterpolator());
        colorAnimator.setEvaluator(new ArgbEvaluator());
        nightSkyAnimator.setEvaluator(new ArgbEvaluator());

        if (mCurrentAnimation != null && mCurrentAnimation.isRunning()) {
            mCurrentAnimation.cancel();
            return;
        }
        AnimatorSet mCurrentAnimation = new AnimatorSet();
        if (direction == AnimatorDirection.FORWARD) {
            mCurrentAnimation.play(heightAnimator)
                    .with(colorAnimator)
                    .before(nightSkyAnimator);
        } else {
            mCurrentAnimation.play(nightSkyAnimator)
                    .before(colorAnimator)
                    .before(heightAnimator);
        }

        mCurrentAnimation.start();
    }
}
