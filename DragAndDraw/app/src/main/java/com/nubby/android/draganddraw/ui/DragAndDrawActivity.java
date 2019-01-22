package com.nubby.android.draganddraw.ui;

import androidx.fragment.app.Fragment;


import com.nubby.android.draganddraw.SingleFragmentActivity;

public class DragAndDrawActivity extends SingleFragmentActivity {

    @Override
    protected Fragment CreateFragment() {
        return DragAndDrawFragment.newInstance();
    }

}
