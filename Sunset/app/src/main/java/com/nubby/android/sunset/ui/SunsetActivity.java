package com.nubby.android.sunset.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;

import com.nubby.android.sunset.SingleFragmentActivity;

public class SunsetActivity extends SingleFragmentActivity {
    @Override
    protected Fragment CreateFragment() {
        return SunsetFragment.newInstance();
    }
}
