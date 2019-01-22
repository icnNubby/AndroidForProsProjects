package com.nubby.android.geoquiz;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class QuizActivity extends AppCompatActivity {
    private Button mButtonTrue;
    private Button mButtonFalse;
    private Button mButtonNext;
    private Button mButtonCheat;
    private TextView mTextViewQuestion;
    private static final String bundleQuestionID = "CURRENT_QUESTION";
    private static final String bundleCorrectAnswersID = "CURRENT_CORRECT_ANSWERS";
    private static final String bundleState = "CURRENT_STATE";
    private static final String bundleCheatsUsed = "CURRENT_AMOUNT_OF_CHEATS_USED";
    private static final String TAG = QuizActivity.class.getSimpleName();
    private static final int REQUEST_CODE_CHEAT = 0;

    private Question[] mQuestionBank = new Question[] {
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true),
            new Question(R.string.question_australia, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_oceans, true)
    };
    private int mCurrentIndex = 0;
    private int mCurrentCorrectAnswers = 0;
    private int mCurrentCheatsUsed = 0;
    private boolean mIsCheater;

    private enum State  {
        STATE_TEST_IN_PROGRESS,
        STATE_TEST_FINISHED
    }

    private State mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        mButtonFalse = findViewById(R.id.button_false);
        mButtonTrue = findViewById(R.id.button_true);
        mButtonNext = findViewById(R.id.button_next);
        mButtonCheat = findViewById(R.id.cheat_button);
        mTextViewQuestion = findViewById(R.id.text_view_question);

        if (savedInstanceState == null) {
            mCurrentIndex = 0;
            mCurrentCorrectAnswers = 0;
            mCurrentCheatsUsed = 0;
            mState = State.STATE_TEST_IN_PROGRESS;
            updateQuestion();
        }


        mButtonTrue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mState != State.STATE_TEST_FINISHED)
                    checkAnswerAndContinue(true);
            }
        });

        mButtonFalse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mState != State.STATE_TEST_FINISHED)
                    checkAnswerAndContinue(false);
            }
        });

        mButtonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mState != State.STATE_TEST_FINISHED) {
                    mIsCheater = false;
                    setNextQuestion();
                    updateQuestion();
                }
            }
        });

        mButtonCheat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = CheatActivity.newIntent(QuizActivity.this,
                        mQuestionBank[mCurrentIndex].isAnswerCorrect(), mCurrentCheatsUsed);
                startActivityForResult(intent, REQUEST_CODE_CHEAT);
            }
        });
    }

    private void setState(State state) {
        switch (state){
            case STATE_TEST_FINISHED: {
                setAnswersButtonAvailability(false);
                mCurrentIndex = -1;
                //mCurrentCorrectAnswers = 0;
                mState = State.STATE_TEST_FINISHED;
                break;
            }
            case STATE_TEST_IN_PROGRESS: {
                setAnswersButtonAvailability(true);
                mState = State.STATE_TEST_IN_PROGRESS;
                break;
            }
        }
    }

    private void setNextQuestion() {
        if (mCurrentIndex >= mQuestionBank.length - 1) {
            setState(State.STATE_TEST_FINISHED);
            endOfTest();
        } else
            mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
        mIsCheater = false;
    }

    private void updateQuestion(){
        if (mCurrentIndex > -1) {
            int question = mQuestionBank[mCurrentIndex].getTextResId();
            mTextViewQuestion.setText(question);
        } else
            mTextViewQuestion.setText(getString(R.string.test_finished));
    }

    private void checkAnswerAndContinue(boolean answer) {
        int messageId;
        if (mIsCheater)
            messageId = R.string.judgment_toast;
        else {
            if (answer == mQuestionBank[mCurrentIndex].isAnswerCorrect()) {
                messageId = R.string.toast_correct;
                mCurrentCorrectAnswers++;
            } else
                messageId = R.string.toast_incorrect;
        }

        Toast.makeText(this, messageId, Toast.LENGTH_LONG).show();
        setNextQuestion();
        updateQuestion();
    }

    private void endOfTest() {
        Toast.makeText(this,
                "You got " + ((int) mCurrentCorrectAnswers*100/mQuestionBank.length) + "% of correct answers",
                Toast.LENGTH_LONG).show();
    }

    private void setAnswersButtonAvailability(boolean flag) {
        mButtonTrue.setEnabled(flag);
        mButtonFalse.setEnabled(flag);
        mButtonNext.setEnabled(flag);
        mButtonCheat.setEnabled(flag);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(bundleQuestionID, mCurrentIndex);
        outState.putInt(bundleCorrectAnswersID, mCurrentCorrectAnswers);
        outState.putInt(bundleCheatsUsed, mCurrentCheatsUsed);
        outState.putString(bundleState, mState.toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mCurrentIndex = savedInstanceState.getInt(bundleQuestionID, 0);
        mCurrentCorrectAnswers = savedInstanceState.getInt(bundleCorrectAnswersID, 0);
        mCurrentCheatsUsed = savedInstanceState.getInt(bundleCheatsUsed, 0);
        mState = State.valueOf(savedInstanceState.getString(bundleState, "STATE_TEST_IN_PROGRESS"));
        setState(mState);
        updateQuestion();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }
            mIsCheater = CheatActivity.wasAnswerShown(data);
            if (mIsCheater) mCurrentCheatsUsed++;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
