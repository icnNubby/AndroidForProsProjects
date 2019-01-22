package com.nubby.android.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.nubby.android.criminalintent.utils.PictureUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "Dialog date";
    private static final String LARGE_PHOTO = "Large photo";
    private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 100;

    private Callbacks mCallback;

    private Crime mCrime;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mCrimeDateButton;
    private Button mCrimeTimeButton;
    private Button mCrimeDelete;
    private Button mChooseSuspectButton;
    private Button mReportButton;
    private Button mCallSuspectButton;
    private ImageView mPhotoView;
    private ImageButton mPhotoButton;

    private int mPhotoViewHeight;
    private int mPhotoViewWidth;

    private CheckBox mCrimeSolved;

    public interface Callbacks{
        void onCrimeUpdated(Crime crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeID = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeID);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime,container, false);
        mTitleField = v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mCrimeDateButton = v.findViewById(R.id.crime_date);
        mCrimeTimeButton = v.findViewById(R.id.crime_time);
        updateDate();

        mCrimeDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDateTimePickerFragment(RequestCodes.REQUEST_DATE);
            }
        });

        mCrimeTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callDateTimePickerFragment(RequestCodes.REQUEST_TIME);
            }
        });

        mCrimeSolved = v.findViewById(R.id.crime_solved);
        mCrimeSolved.setChecked(mCrime.isSolved());
        mCrimeSolved.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });

        mCrimeDelete = v.findViewById(R.id.delete_crime);
        mCrimeDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                getActivity().finish();
            }
        });

        mChooseSuspectButton = v.findViewById(R.id.choose_suspect_button);
        mReportButton = v.findViewById(R.id.send_crime_report_button);

        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setSubject(getString(R.string.crime_report_subject))
                        .setText(getCrimeReport())
                        .setChooserTitle(getString(R.string.send_report))
                        .getIntent();
                startActivity(intent);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mChooseSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, RequestCodes.REQUEST_CONTACT);
            }
        });

        if (mCrime.getSuspect() != null)
            mChooseSuspectButton.setText(mCrime.getSuspect());

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null)
            mChooseSuspectButton.setEnabled(false);

        mCallSuspectButton = v.findViewById(R.id.call_to_suspect);
        if (mCrime.getSuspect() == null) mCallSuspectButton.setEnabled(false);
        mCallSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callToSuspect();
            }
        });

        mPhotoButton = v.findViewById(R.id.crime_camera);
        mPhotoView = v.findViewById(R.id.crime_photo);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null && captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto(captureImage);
            }
        });
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewFullScreenPhoto();
            }
        });
        final ViewTreeObserver observer = mPhotoView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mPhotoViewHeight  = mPhotoView.getMeasuredHeight();
                mPhotoViewWidth = mPhotoView.getMeasuredWidth();
                updatePhotoView();
            }
        });

        return v;
    }

    private void takePhoto(Intent intent) {
        Uri uri = FileProvider.getUriForFile(getActivity(), "com.nubby.android.criminalintent.fileprovider",
                mPhotoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        List<ResolveInfo> cameraActivities = getActivity().getPackageManager()
                .queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
        for (ResolveInfo activity: cameraActivities)
            getActivity().grantUriPermission(activity.activityInfo.packageName,
                    uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, RequestCodes.REQUEST_PHOTO);
    }

    private void callToSuspect() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[] {Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            return;
        }
        Cursor cursor = getActivity().getContentResolver()
                .query(ContactsContract.Contacts.CONTENT_URI,
                        null, "DISPLAY_NAME = '" + mCrime.getSuspect() + "'"
                        , null, null);
        String contactID;
        try {
            if (cursor.getCount() == 0) return;
            cursor.moveToFirst();
            contactID = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
        } finally {
            cursor.close();
        }
        Cursor phones;
        phones = getActivity().getContentResolver().
                query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + contactID,
                    null,null);
        try {
            if (phones.getCount() == 0) return;
            phones.moveToFirst();
            String number = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            Uri call = Uri.parse("tel:" + number);
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(call);
            startActivity(intent);
        }finally {
            phones.close();
        }
    }

    private void callDateTimePickerFragment(int code) {
        FragmentManager manager = getFragmentManager();
        DateTimePickerFragment dialog = DateTimePickerFragment.newInstance(mCrime.getDate());
        dialog.setTargetFragment(CrimeFragment.this, code);
        dialog.show(manager, DIALOG_DATE);
    }

    private void viewFullScreenPhoto() {
        FragmentManager manager = getFragmentManager();
        FullScreenCrimePictureFragment dialog = FullScreenCrimePictureFragment.newInstance(mPhotoFile.toString());
        dialog.setTargetFragment(CrimeFragment.this, 0);
        dialog.show(manager, LARGE_PHOTO);
    }

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;
        switch (requestCode) {
            case RequestCodes.REQUEST_DATE :
            case RequestCodes.REQUEST_TIME: {
                Date date = (Date) data.getSerializableExtra(DateTimePickerFragment.EXTRA_DATE);
                mCrime.setDate(date);
                updateCrime();
                updateDate();
                break;
            }
            case RequestCodes.REQUEST_CONTACT: {
                Uri contact = data.getData();
                String[] queryFields = new String[]{ContactsContract.Contacts.DISPLAY_NAME};
                Cursor cursor = getActivity().getContentResolver().
                        query(contact, queryFields, null, null, null);
                try {
                    if (cursor.getCount() == 0)
                        return;
                    cursor.moveToFirst();
                    String suspect = cursor.getString(0);
                    mCrime.setSuspect(suspect);
                    updateCrime();
                    mChooseSuspectButton.setText(suspect);
                    mCallSuspectButton.setEnabled(true);
                } finally {
                    cursor.close();
                }
                break;
            }
            case RequestCodes.REQUEST_PHOTO: {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.nubby.android.criminalintent.fileprovider", mPhotoFile);
                getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                updateCrime();
                updatePhotoView();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    private void updateDate() {
        DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        String date = dateFormat.format(mCrime.getDate());
        mCrimeDateButton.setText(date);

        DateFormat timeFormat = new SimpleDateFormat("HH : mm");
        String time = timeFormat.format(mCrime.getDate());
        mCrimeTimeButton.setText(time);
    }

    private String getCrimeReport() {
        String solvedString;
        if (mCrime.isSolved())
           solvedString = getString(R.string.crime_report_solved);
        else
            solvedString = getString(R.string.crime_report_unsolved);

        DateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd");
        String dateString = dateFormat.format(mCrime.getDate());

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        return getString(R.string.crime_report, mCrime.getTitle(),
                dateString, solvedString, suspect);
    }

    private void updatePhotoView() {
        if (mPhotoFile == null || !mPhotoFile.exists()) {
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), mPhotoViewWidth, mPhotoViewHeight);
            mPhotoView.setImageBitmap(bitmap);
        }

    }

    private void updateCrime() {
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallback.onCrimeUpdated(mCrime);
    }

}
