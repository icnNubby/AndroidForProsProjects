package com.nubby.android.criminalintent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.nubby.android.criminalintent.utils.PictureUtils;

public class FullScreenCrimePictureFragment extends DialogFragment {
    private static final String ARG_PATH = "path";

    public static FullScreenCrimePictureFragment newInstance(String path) {
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PATH, path);
        FullScreenCrimePictureFragment fragment = new FullScreenCrimePictureFragment();
        fragment.setArguments(bundle);
        return fragment;
    }



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        String path = bundle.getString(ARG_PATH);
        View v  = LayoutInflater.from(getActivity()).inflate(R.layout.crime_photo_big_size, null);
        ImageView image = v.findViewById(R.id.crime_photo_big_size_image);
        Bitmap bmp = PictureUtils.getScaledBitmap(path, getActivity());
        image.setImageBitmap(bmp);
        AlertDialog.Builder alertBuilder =
                new AlertDialog.Builder(getActivity());
        alertBuilder.setView(v).setPositiveButton("Close", null);
        return alertBuilder.create();
    }
}
