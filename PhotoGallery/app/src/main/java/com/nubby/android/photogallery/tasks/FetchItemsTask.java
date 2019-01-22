package com.nubby.android.photogallery.tasks;

import android.os.AsyncTask;

import com.nubby.android.photogallery.model.GalleryItem;
import com.nubby.android.photogallery.utils.FlickrFetcher;
import com.nubby.android.photogallery.utils.JSONParser;

import java.util.List;


public class FetchItemsTask extends AsyncTask<Void, Void, List<GalleryItem>> {

    private static final String TAG = "FetchItemsTask";
    private Callback mCallback;
    private final int mPages;
    private final String mQuery;

    public FetchItemsTask(Callback callback, int pages, String query) {
        mCallback = callback;
        mPages = pages;
        mQuery = query;
    }

    public void unregisterCallback() {
        mCallback = null;
    }

    @Override
    protected List<GalleryItem> doInBackground(Void... voids) {

        List<GalleryItem> items;
        if (mQuery == null || mQuery.equals(""))
            items = JSONParser.parseBody(new FlickrFetcher().fetchRecentPhotos(mPages));
        else
            items = JSONParser.parseBody(new FlickrFetcher().fetchSearch(mQuery, mPages));
        return items;
    }

    @Override
    protected void onPostExecute(List<GalleryItem> galleryItems) {
        super.onPostExecute(galleryItems);
        if (mCallback != null) mCallback.onFetchDone(galleryItems);
    }

    public interface Callback {
        void onFetchDone(List<GalleryItem> list);
    }
}
