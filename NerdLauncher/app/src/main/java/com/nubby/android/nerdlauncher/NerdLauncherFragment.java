package com.nubby.android.nerdlauncher;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ShareCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NerdLauncherFragment extends Fragment {

    private static final String TAG = NerdLauncherFragment.class.getSimpleName();
    RecyclerView mRecyclerView;

    public static NerdLauncherFragment newInstance() {

        Bundle args = new Bundle();

        NerdLauncherFragment fragment = new NerdLauncherFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_nerd_launcher, container, false);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        setupAdapter();
        return view;
    }

    private void setupAdapter() {
        Intent startupIntent = new Intent(Intent.ACTION_MAIN);
        startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PackageManager packageManager = getActivity().getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(startupIntent, 0);
        Collections.sort(activities, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo o1, ResolveInfo o2) {
                PackageManager pm = getActivity().getPackageManager();
                return String.CASE_INSENSITIVE_ORDER.compare(
                        o1.loadLabel(pm).toString(),
                        o2.loadLabel(pm).toString());

            }
        });
        Log.i(TAG, "Found " + activities.size() + " activities.");
        IntentAdapter adapter = new IntentAdapter(activities);
        mRecyclerView.setAdapter(adapter);
    }

    private class IntentHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ResolveInfo mResolveInfo;
        private TextView mNameTextView;
        private ImageView mIconView;

        public IntentHolder(@NonNull View itemView) {
            super(itemView.getRootView());
            mNameTextView = itemView.findViewById(R.id.intent_label);
            mNameTextView.setOnClickListener(this);
            mIconView = itemView.findViewById(R.id.intent_icon);
        }

        public void bindActivity(ResolveInfo resolveInfo) {
            mResolveInfo = resolveInfo;
            mNameTextView.setText(resolveInfo.
                    loadLabel(getActivity().getPackageManager()).toString());
            mIconView.setImageDrawable(resolveInfo.loadIcon(getActivity().getPackageManager()));
        }

        @Override
        public void onClick(View v) {
            ActivityInfo activityInfo = mResolveInfo.activityInfo;
            ComponentName componentName = new ComponentName(activityInfo.packageName, activityInfo.name);
            Intent intent = new Intent(Intent.ACTION_MAIN)
                    .setClassName(activityInfo.packageName, activityInfo.name)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);;
            startActivity(intent);
        }
    }

    public class IntentAdapter extends RecyclerView.Adapter<IntentHolder> {
        private final List<ResolveInfo> mResolveInfosList;

        public IntentAdapter(List<ResolveInfo> list) {
            mResolveInfosList = list;
        }
        @NonNull
        @Override
        public IntentHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.simple_list_item, viewGroup, false);
            return new IntentHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull IntentHolder intentHolder, int i) {
            intentHolder.bindActivity(mResolveInfosList.get(i));
        }

        @Override
        public int getItemCount() {
            return mResolveInfosList.size();
        }
    }
}
