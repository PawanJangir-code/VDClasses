package com.vd.vdclasses;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class RecyclerViewAnimator {

    private int lastAnimatedPosition = -1;
    private static final long ANIMATION_DURATION = 400;
    private static final long STAGGER_DELAY = 50;

    public void animateItem(View view, int position) {
        if (position > lastAnimatedPosition) {
            view.setAlpha(0f);
            view.setTranslationY(100f);

            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);
            ObjectAnimator translateAnim = ObjectAnimator.ofFloat(view, "translationY", 100f, 0f);

            AnimatorSet animSet = new AnimatorSet();
            animSet.playTogether(alphaAnim, translateAnim);
            animSet.setDuration(ANIMATION_DURATION);
            animSet.setStartDelay(position * STAGGER_DELAY);
            animSet.setInterpolator(new DecelerateInterpolator());
            animSet.start();

            lastAnimatedPosition = position;
        }
    }

    public void reset() {
        lastAnimatedPosition = -1;
    }

    public static void animateButtonClick(View view) {
        view.animate()
                .scaleX(0.92f)
                .scaleY(0.92f)
                .setDuration(100)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start())
                .start();
    }
}
