package com.nubby.android.criminalintent;

import java.util.Date;
import java.util.UUID;

public class Crime implements Comparable{
    private UUID mID;
    private String mTitle;
    private Date mDate;
    private boolean mSolved;
    private String mSuspect;

    public Crime() {
        mID = UUID.randomUUID();
        mDate = new Date();
        mTitle = "";
    }

    public Crime(UUID uuid) {
        mID = uuid;
        mDate = new Date();
        mTitle = "";
    }

    public UUID getID() {
        return mID;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public boolean isSolved() {
        return mSolved;
    }

    public void setSolved(boolean solved) {
        mSolved = solved;
    }

    @Override
    public int compareTo(Object o) {
        if (!(o instanceof Crime)) throw new IllegalArgumentException("Cannot cast " + o.getClass().getSimpleName() + " to Crime");
        return this.mTitle.compareTo(((Crime) o).mTitle);
    }

    public String getSuspect() {
        return mSuspect;
    }

    public void setSuspect(String suspect) {
        mSuspect = suspect;
    }

    public String getPhotoFilename() {
        return "IMG_" + getID().toString() + ".jpg";
    }
}
