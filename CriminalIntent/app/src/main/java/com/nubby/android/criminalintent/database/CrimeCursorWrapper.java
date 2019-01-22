package com.nubby.android.criminalintent.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.nubby.android.criminalintent.Crime;

import java.util.Date;
import java.util.UUID;

public class CrimeCursorWrapper extends CursorWrapper {
    public CrimeCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Crime getCrime() {

        String uuidString = getString(getColumnIndex(CrimeDbChema.CrimeTable.Cols.UUID));
        String title = getString(getColumnIndex(CrimeDbChema.CrimeTable.Cols.TITLE));
        String suspect = getString(getColumnIndex(CrimeDbChema.CrimeTable.Cols.SUSPECT));
        long date = getLong(getColumnIndex(CrimeDbChema.CrimeTable.Cols.DATE));
        int isSolved = getInt(getColumnIndex(CrimeDbChema.CrimeTable.Cols.SOLVED));

        Crime crime = new Crime(UUID.fromString(uuidString));
        crime.setDate(new Date(date));
        crime.setTitle(title);
        crime.setSuspect(suspect);
        crime.setSolved(isSolved == 1);

        return crime;
    }
}
