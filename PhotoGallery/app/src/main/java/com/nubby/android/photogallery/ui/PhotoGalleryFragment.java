package com.nubby.android.photogallery.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.nubby.android.photogallery.R;
import com.nubby.android.photogallery.model.GalleryItem;
import com.nubby.android.photogallery.tasks.FetchItemsTask;
import com.nubby.android.photogallery.utils.QueryPreferences;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";

    private RecyclerView mPhotoRecyclerView;
    private FetchItemsTask mFetchUpdate;
    private String mPreviousSearch;
    private ProgressBar mProgressBar;
    private Callbacks mCallback;
    private int mAdapterPosition;
    private SwipeRefreshLayout mRefreshLayout;

    public interface Callbacks {
        void onPhotoSelected(GalleryItem item);
    }

    public static PhotoGalleryFragment newInstance() {
        Bundle args = new Bundle();
        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        fragment.setArguments(args);
        return fragment;
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
        setRetainInstance(true);
        setHasOptionsMenu(true);
        mPreviousSearch = QueryPreferences.getStoredQuery(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = getView();
        if (v == null) {
            v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);
            mPhotoRecyclerView = v.findViewById(R.id.photo_recycler_view);
            mPhotoRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
            mProgressBar = v.findViewById(R.id.loading_progress_bar);
            v.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int width = mPhotoRecyclerView.getMeasuredWidth();
                    ((GridLayoutManager) mPhotoRecyclerView.getLayoutManager()).setSpanCount(width / 100);
                }
            });
        }

        mRefreshLayout = v.findViewById(R.id.nested_layout_for_photo_recycler_view);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateItems(1);
            }
        });


        if (setupAdapter()) updateItems(1);
        if (mPhotoRecyclerView != null) {
            mPhotoRecyclerView.getLayoutManager().scrollToPosition(mAdapterPosition);
        }
        return v;
    }


    @Override
    public void onStop() {
        PhotoAdapter adapter = (PhotoAdapter) (mPhotoRecyclerView.getAdapter());
        if (adapter != null) mAdapterPosition = adapter.getCurrentPosition();
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private class PhotoHolder extends android.support.v7.widget.RecyclerView.ViewHolder implements View.OnClickListener {
        private ImageView mItemImageView;
        private GalleryItem mGalleryItem;

        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            mItemImageView = itemView.findViewById(R.id.item_image_view);
            mItemImageView.setOnClickListener(this);
        }

        public void bindGalleryItem(GalleryItem item) {
            Picasso.get().load(item.getUrl_s())
                    .placeholder(R.drawable.bill_up_close)
                    .into(mItemImageView);
            mGalleryItem = item;
        }

        @Override
        public void onClick(View v) {
            mCallback.onPhotoSelected(mGalleryItem);
        }
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder> implements FetchItemsTask.Callback {
        private List<GalleryItem> mGalleryItems;
        private int mPagesfetched = 0;
        private FetchItemsTask mCurrentTask;
        private int mCurrentPosition;

        public PhotoAdapter(List<GalleryItem> items) {
            mGalleryItems = items;
        }

        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(R.layout.gallery_item, viewGroup, false);
            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder photoHolder, int i) {
            photoHolder.bindGalleryItem(mGalleryItems.get(i));
            mCurrentPosition = i;
            if (i == mGalleryItems.size() - 3) {
                mCurrentTask = new FetchItemsTask(this, mPagesfetched + 1, mPreviousSearch);
                mCurrentTask.execute();
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public int getItemCount() {
            return mGalleryItems.size();
        }

        @Override
        public void onFetchDone(List<GalleryItem> list) {
            //mProgressBar.setVisibility(View.GONE);
            mPagesfetched++;
            int sizeBeforeAdding = mGalleryItems.size();
            mGalleryItems.addAll(list);
            mPhotoRecyclerView.getAdapter().notifyItemRangeInserted(sizeBeforeAdding,
                    mGalleryItems.size() - sizeBeforeAdding);
            mRefreshLayout.setRefreshing(false);
        }

        public void clearAdapter() {
            mPagesfetched = 0;
            mGalleryItems.clear();
            mPhotoRecyclerView.getAdapter().notifyDataSetChanged();
        }

        public int getCurrentPosition() {
            return mCurrentPosition;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        final SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (!s.equals(mPreviousSearch)) {
                    if (s.equals(""))
                        mPreviousSearch = null;
                    else
                        mPreviousSearch = s;
                    QueryPreferences.setStoredQuery(getActivity(), mPreviousSearch);
                    Log.d(TAG, "Query text submitted " + s);
                    updateItems(1);
                }
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(TAG, "onQueryTextChange");
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = QueryPreferences.getStoredQuery(getActivity());
                searchView.setQuery(query, false);
            }
        });

        MenuItem toggleItem = menu.findItem(R.id.menu_item_toggle_polling);
        if (QueryPreferences.getPollingEnabled(getActivity())) {
            toggleItem.setTitle(R.string.stop_polling);
            QueryPreferences.setPollingEnabled(getActivity(), true);
        } else {
            toggleItem.setTitle(R.string.start_polling);
            QueryPreferences.setPollingEnabled(getActivity(), false);
        }

    }

    private void updateItems(int page) {
        PhotoAdapter adapter = (PhotoAdapter) (mPhotoRecyclerView.getAdapter());
        if (page == 1 && adapter != null) adapter.clearAdapter();
        if (mFetchUpdate != null) mFetchUpdate.unregisterCallback();
        mFetchUpdate = new FetchItemsTask((PhotoAdapter) mPhotoRecyclerView.getAdapter(), page, mPreviousSearch);
        mFetchUpdate.execute();
        mRefreshLayout.setRefreshing(true);
        mRefreshLayout.animate().start();
        //mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_clear_search:
                mPreviousSearch = null;
                QueryPreferences.setStoredQuery(getActivity(), mPreviousSearch);
                updateItems(1);
                return true;
            case R.id.menu_item_toggle_polling:
                QueryPreferences.setPollingEnabled(getActivity(),
                        !QueryPreferences.getPollingEnabled(getActivity()));
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean setupAdapter() {
        if (isAdded() && mPhotoRecyclerView.getAdapter() == null) {
            mPhotoRecyclerView.setAdapter(new PhotoAdapter(new ArrayList<GalleryItem>()));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mFetchUpdate.unregisterCallback();
    }
}
