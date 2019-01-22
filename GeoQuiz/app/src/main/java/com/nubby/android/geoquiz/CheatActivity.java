package com.nubby.android.geoquiz;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewAnimator;

public class CheatActivity extends AppCompatActivity {
    private static final String EXTRA_ANSWER_IS_TRUE =
            "answer_is_true";
    private static final String EXTRA_ANSWER_SHOWN =
            "answer_shown";
    private static final String EXTRA_CHEATS_USED =
            "cheats_used";
    private boolean mAnswerIsTrue;
    private TextView mAnswerTextView;
    private TextView mVersionTextView;
    private Button mShowAnswerButton;
    private int mAmountOfCheatsUsed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cheat);
        mAnswerIsTrue = getIntent().getBooleanExtra(EXTRA_ANSWER_IS_TRUE, false);
        mAmountOfCheatsUsed = getIntent().getIntExtra(EXTRA_CHEATS_USED, 0);
        mAnswerTextView = findViewById(R.id.text_view_answer);
        mVersionTextView = findViewById(R.id.text_view_version);
        mVersionTextView.setText("Cheats already used " + mAmountOfCheatsUsed +". You can cheat maximum 3 times.");
        mShowAnswerButton = findViewById(R.id.button_show);
        if (mAmountOfCheatsUsed >= 3) mShowAnswerButton.setEnabled(false);
        mShowAnswerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAnswerIsTrue) {
                    mAnswerTextView.setText(R.string.button_true_text);
                } else {
                    mAnswerTextView.setText(R.string.button_false_text);
                }
                setAnswerShownResult(true);

                int cx = mShowAnswerButton.getWidth()/2;
                int cy = mShowAnswerButton.getHeight()/2;
                float radius = mShowAnswerButton.getWidth();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    Animator anim = ViewAnimationUtils.
                            createCircularReveal(mShowAnswerButton, cx, cy, radius, 0);
                    anim.addListener(new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mShowAnswerButton.setVisibility(View.INVISIBLE);
                        }

                    });
                    anim.start();
                } else {
                    mShowAnswerButton.setVisibility(View.INVISIBLE);
                }

            }
        });
    }

    public static Intent newIntent(Context packageContext, boolean answerIsTrue, int amountOfCheatsUsed) {
        Intent intent = new Intent(packageContext, CheatActivity.class);
        intent.putExtra(EXTRA_ANSWER_IS_TRUE, answerIsTrue);
        intent.putExtra(EXTRA_CHEATS_USED, amountOfCheatsUsed);
        return intent;
    }

    private void setAnswerShownResult(boolean isAnswerShown) {
        Intent data = new Intent();
        data.putExtra(EXTRA_ANSWER_SHOWN, isAnswerShown);
        setResult(RESULT_OK, data);
    }

    public static boolean wasAnswerShown(Intent result) {
        return result.getBooleanExtra(EXTRA_ANSWER_SHOWN, false);
    }
}
