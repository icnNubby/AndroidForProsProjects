package com.nubby.android.criminalintent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.nubby.android.criminalintent.database.CrimeBaseHelper;
import com.nubby.android.criminalintent.database.CrimeCursorWrapper;
import com.nubby.android.criminalintent.database.CrimeDbChema;
import com.nubby.android.criminalintent.database.CrimeDbChema.CrimeTable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.nubby.android.criminalintent.database.CrimeDbChema.CrimeTable.*;

public class CrimeLab {

    private  static CrimeLab sCrimeLab;
    private Context mContext;
    private SQLiteDatabase mSQLiteDatabase;

    public synchronized static CrimeLab get(Context context) {
            if (sCrimeLab == null) {
                    sCrimeLab = new CrimeLab(context);
            }
        return sCrimeLab;
    }

    private CrimeLab(Context context) {
        mContext = context.getApplicationContext();
        mSQLiteDatabase = new CrimeBaseHelper(mContext).getWritableDatabase();
    }

    private static ContentValues getContentValues(Crime crime) {
        ContentValues values = new ContentValues();
        values.put(Cols.UUID, crime.getID().toString());
        values.put(Cols.DATE, crime.getDate().getTime());
        values.put(Cols.SOLVED, crime.isSolved()? 1: 0);
        values.put(Cols.TITLE, crime.getTitle());
        values.put(Cols.SUSPECT, crime.getSuspect());
        return values;
    }

    private CrimeCursorWrapper queryCrimes(String whereClause, String[] whereArgs) {
        Cursor cursor = mSQLiteDatabase.query(
                CrimeTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new CrimeCursorWrapper(cursor);
    }

    public File getPhotoFile(Crime crime) {
        File filesDir = mContext.getFilesDir();
        return new File(filesDir, crime.getPhotoFilename());
    }

    public int updateCrime(Crime c) {
        String uuidString = c.getID().toString();
        ContentValues values = getContentValues(c);
        return mSQLiteDatabase.update(CrimeTable.NAME, values, Cols.UUID + " = ?",
                new String[] {uuidString});
    }

    public long addCrime(Crime c) {
        ContentValues values = getContentValues(c);
        return mSQLiteDatabase.insert(CrimeTable.NAME, null, values);
    }


    public int deleteCrime(Crime c){
        String uuidString = c.getID().toString();
        return mSQLiteDatabase.delete(CrimeTable.NAME, Cols.UUID + " = ?",
                new String[] {uuidString});
    }


    public List<Crime> getCrimes() {
        List<Crime> crimes = new ArrayList<>();
        CrimeCursorWrapper cursor = queryCrimes(null, null);
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                crimes.add(cursor.getCrime());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return crimes;
    }

    public Crime getCrime(UUID id) {
        CrimeCursorWrapper cursor = queryCrimes(Cols.UUID + " = ?",
                                                new String[] {id.toString()});
        try {
            cursor.moveToFirst();
            return cursor.getCrime();
        } finally {
            cursor.close();
        }
    }
}
