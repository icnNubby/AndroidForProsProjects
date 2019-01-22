package com.nubby.android.photogallery.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.nubby.android.photogallery.R;
import com.nubby.android.photogallery.SingleFragmentActivity;
import com.nubby.android.photogallery.model.GalleryItem;
import com.nubby.android.photogallery.service.PollService;
import com.nubby.android.photogallery.utils.QueryPreferences;

public class PhotoGalleryActivity extends SingleFragmentActivity  implements PhotoGalleryFragment.Callbacks{

    public static Intent newIntent(Context context) {
        return new Intent(context, PhotoGalleryActivity.class);
    }

    @Override
    protected Fragment CreateFragment() {
        return PhotoGalleryFragment.newInstance();
    }

    @Override
    public void onPhotoSelected(GalleryItem item) {
        Fragment photoView = PhotoPageFragment.newInstance(item.getPhotoPageUri());
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.fragment_container, photoView).addToBackStack(null).commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (PollService.isServiceAlarmOn(this))
            PollService.setServiceAlarm(this, false);

    }

    @Override
    protected void onPause() {
        super.onPause();
        PollService.setServiceAlarm(this, QueryPreferences.getPollingEnabled(this));
    }

}
