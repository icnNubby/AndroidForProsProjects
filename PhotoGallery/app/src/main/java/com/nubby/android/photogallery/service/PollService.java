package com.nubby.android.photogallery.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.nubby.android.photogallery.R;
import com.nubby.android.photogallery.model.GalleryItem;
import com.nubby.android.photogallery.tasks.FetchItemsTask;
import com.nubby.android.photogallery.ui.PhotoGalleryActivity;
import com.nubby.android.photogallery.utils.QueryPreferences;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class PollService extends JobService implements FetchItemsTask.Callback {
    private static final String TAG = "PollService";
    //private static final int SYNC_INTERVAL_HOURS = 1;
    private static final int SYNC_INTERVAL_SECONDS = 60 * 60 * 10;
    private static final int FLEX_INTERVAL_SECONDS = 60 * 15;
    private static final long SYNC_INTERVAL_MILLIS = TimeUnit.SECONDS.toMillis(SYNC_INTERVAL_SECONDS);
    private static final long FLEX_INTERVAL_MILLIS = TimeUnit.SECONDS.toMillis(FLEX_INTERVAL_SECONDS);
    private static final int POLL_SERVICE_JOB_ID = 42;
    private static final String NOTIFICATION_CHANNEL_ID = "photo_gallery_polling_service_channel_id";
    private FetchItemsTask mFetchItemsTask;
    private JobParameters mJobParameters;

    public static void setServiceAlarm(Context context, boolean isOn) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName jobServiceName = new ComponentName(
                context, PollService.class);
        if (isOn) {
            jobScheduler.schedule(
                    new JobInfo.Builder(POLL_SERVICE_JOB_ID, jobServiceName)
                            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
                            .setRequiresCharging(false)
                            .setMinimumLatency(FLEX_INTERVAL_MILLIS)
                            .setOverrideDeadline(SYNC_INTERVAL_MILLIS)
                            .setPersisted(true)
                            .build());
        } else {
            jobScheduler.cancel(POLL_SERVICE_JOB_ID);
        }
    }

    public static boolean isServiceAlarmOn(Context context) {
        JobScheduler jobScheduler = (JobScheduler)
                context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        boolean hasBeenScheduled = false;

        for (JobInfo jobInfo : jobScheduler.getAllPendingJobs()) {
            if (jobInfo.getId() == POLL_SERVICE_JOB_ID) {
                hasBeenScheduled = true;
                break;
            }
        }
        return hasBeenScheduled;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        if (!isNetworkAvailableAndConnected())
            return false;
        String query = QueryPreferences.getStoredQuery(this);
        mFetchItemsTask = new FetchItemsTask(this, 1, query);
        mFetchItemsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        mJobParameters = params;
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (mFetchItemsTask != null) {
            mFetchItemsTask.cancel(true);
            mFetchItemsTask.unregisterCallback();
            mFetchItemsTask = null;
        }
        return true;
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean available = (cm.getActiveNetworkInfo() != null);
        boolean connected = available && cm.getActiveNetworkInfo().isConnected();
        return connected;
    }

    @Override
    public void onFetchDone(List<GalleryItem> list) {
        String lastResultId = QueryPreferences.getLastResultId(this);
        if (list.size() == 0) return;
        String resultId = list.get(0).getId();
        if (resultId.equals(lastResultId)) {
            Log.i(TAG, "Received an old result " + resultId);
        } else {
            Log.i(TAG, "Received a new result " + resultId);
            Resources res = getResources();
            Intent galleryIntent = PhotoGalleryActivity.newIntent(this);
            NotificationManager manager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent galleryPendingIntent =
                    PendingIntent.getActivity(this, 0, galleryIntent, 0);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel mChannel = new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        res.getString(R.string.main_notification_channel_name),
                        NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(mChannel);
            }
            Notification notification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setContentIntent(galleryPendingIntent)
                    .setSmallIcon(android.R.drawable.ic_menu_report_image)
                    .setContentTitle(res.getString(R.string.new_pictures_title))
                    .setContentInfo(res.getString(R.string.new_pictures_text))
                    .setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_VIBRATE)
                    .setDefaults(Notification.DEFAULT_LIGHTS)
                    .build();
            manager.notify(0, notification);
            QueryPreferences.setLastResultId(this, resultId);
        }
        jobFinished(mJobParameters, true);
    }
}
