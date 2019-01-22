package com.nubby.android.geoquiz;

public class Question {
    private int mTextResId;
    private boolean mAnswerCorrect;

    public Question(int textResId, boolean correct){
        mAnswerCorrect = correct;
        mTextResId = textResId;
    }

    public boolean isAnswerCorrect() {
        return mAnswerCorrect;
    }

    public void setAnswerCorrect(boolean answerCorrect) {
        mAnswerCorrect = answerCorrect;
    }

    public int getTextResId() {
        return mTextResId;
    }

    public void setTextResId(int textResId) {
        mTextResId = textResId;
    }
}
