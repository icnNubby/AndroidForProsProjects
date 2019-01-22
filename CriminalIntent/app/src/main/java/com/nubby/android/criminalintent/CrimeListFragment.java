package com.nubby.android.criminalintent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

public class CrimeListFragment extends Fragment {
    private RecyclerView mCrimeRecyclerView;
    private CrimeAdapter mCrimeAdapter;
    private LinearLayout mGreetingsLayout;
    private Button mAddCrimeGreetingsButton;
    private boolean mSubtitleVisible;
    private static final String  SAVED_SUBTITLE_VISIBLE = "subtitle";
    private List<Crime> mCrimes;
    private Callbacks mCallbacks;

    public interface Callbacks {
        void onCrimeSelected(Crime crime);
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBLE);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
        mCrimeRecyclerView = view.findViewById(R.id.fragment_crime_recycler_view);
        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mGreetingsLayout = view.findViewById(R.id.greetings_screen);
        mAddCrimeGreetingsButton = view.findViewById(R.id.greetings_add_button);
        mAddCrimeGreetingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addCrime();
            }
        });
        updateUI();
        return view;
    }

    public void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();
        if (mCrimeAdapter == null) {
            mCrimeAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mCrimeAdapter);
            ItemTouchHelper.Callback callback = new CrimeListTouchHelperCallback(mCrimeAdapter);
            ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
            touchHelper.attachToRecyclerView(mCrimeRecyclerView);
        } else {
            mCrimeAdapter.setCrimes(crimes);
            mCrimeAdapter.notifyDataSetChanged();
        }
        setGreetingScreenVisibility(crimes);
        updateSubtitle();
    }

    private void setGreetingScreenVisibility(List<Crime> crimes) {
        if (crimes.size() == 0) {
            mGreetingsLayout.setVisibility(View.VISIBLE);
            mCrimeRecyclerView.setVisibility(View.GONE);
        } else {
            mGreetingsLayout.setVisibility(View.GONE);
            mCrimeRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter<CrimeViewHolder> implements ItemTouchHelperAdapter {

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        public void setCrimes(List<Crime> crimes) {
            mCrimes = crimes;
        }

        public int getIndex(Crime crime) {
            return mCrimes.indexOf(crime);
        }

        @NonNull
        @Override
        public CrimeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            int elementID;
            if (viewType == 0) elementID = R.layout.crime_element_serious;
            else elementID = R.layout.crime_element;
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(elementID, viewGroup, false);
            CrimeViewHolder viewHolder = new CrimeViewHolder(view, viewType);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull CrimeViewHolder viewHolder, int i) {
            viewHolder.bind(mCrimes.get(i));
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }

        @Override
        public int getItemViewType(int position) {
            if (position % 3 == 0) return 0;
            else return 1;
        }

        @Override
        public void onItemDismiss(int position) {
            CrimeLab.get(getActivity()).deleteCrime(mCrimes.get(position));
            mCrimes.remove(position);
            notifyItemRemoved(position);
            updateSubtitle();
            if (mCrimes.size() > 0) mCallbacks.onCrimeSelected(mCrimes.get(0));
            setGreetingScreenVisibility(mCrimes);
        }
    }

    private class CrimeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private Crime mCrime;
        private boolean mRequiresPolice;
        private ImageButton mCallPolice;
        private ImageView mCrimeSolved;

        public CrimeViewHolder(@NonNull View itemView, int i) {
            super(itemView);
            mTitleTextView = itemView.findViewById(R.id.crime_title);
            mDateTextView = itemView.findViewById(R.id.crime_date);
            mCrimeSolved = itemView.findViewById(R.id.image_crime_solved);
            mRequiresPolice = (i == 0);
            if (mRequiresPolice) {
                mCallPolice = itemView.findViewById(R.id.button_call_police);
                mCallPolice.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(), mCrime.getTitle() + " police called!", Toast.LENGTH_LONG).show();
                    }
                });
            }
            itemView.setOnClickListener(this);
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            DateFormat dateFormat = new SimpleDateFormat("EEEE, MMMM dd, yyyy");
            String out = dateFormat.format(mCrime.getDate());
            mDateTextView.setText(out);
            mCrimeSolved.setVisibility(mCrime.isSolved()? View.VISIBLE: View.GONE);
        }

        @Override
        public void onClick(View v) {
           mCallbacks.onCrimeSelected(mCrime);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);
        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if(mSubtitleVisible) {
            subtitleItem.setTitle(R.string.hide_subtitle);
        } else {
            subtitleItem.setTitle(R.string.show_subtitle);
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                addCrime();
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addCrime() {
        Crime crime = new Crime();
        CrimeLab.get(getActivity()).addCrime(crime);
        updateUI();
        mCallbacks.onCrimeSelected(crime);
    }

    private void updateSubtitle() {
        int crimeCount = mCrimes.size();
        String subtitle = null;
        if (mSubtitleVisible)  subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeCount, crimeCount);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SAVED_SUBTITLE_VISIBLE, mSubtitleVisible);
    }
}
