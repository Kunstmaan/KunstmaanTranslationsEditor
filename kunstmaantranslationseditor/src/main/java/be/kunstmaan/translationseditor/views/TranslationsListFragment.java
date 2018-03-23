package be.kunstmaan.translationseditor.views;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import be.kunstmaan.translationseditor.R;
import be.kunstmaan.translationseditor.adapters.TranslationsFragmentAdapter;
import be.kunstmaan.translationseditor.utils.TranslationPair;


public class TranslationsListFragment extends Fragment {

    private static final String TRANSLATIONS_PARAMS = "param1";

    private ArrayList<TranslationPair> mStringsParam;
    private RecyclerView mRecyclerView;
    private TranslationsFragmentAdapter mAdapter;

    public TranslationsListFragment() {
        // Required empty public constructor
    }

    public static TranslationsListFragment newInstance(ArrayList<TranslationPair> param1) {
        TranslationsListFragment fragment = new TranslationsListFragment();
        Bundle args = new Bundle();
        args.putSerializable(TRANSLATIONS_PARAMS, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mStringsParam = (ArrayList<TranslationPair>) getArguments().getSerializable(TRANSLATIONS_PARAMS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.translations_list_fragment_layout, container,false);
        mRecyclerView = v.findViewById(R.id.recycler_view);
        mAdapter = new TranslationsFragmentAdapter(mStringsParam, getActivity());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);
        return v;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(!item.isChecked());
        int id = item.getItemId();

        if (id == R.id.action_show_changed) {
            if(item.isChecked()){
                mAdapter.showEdited();
            }else {
                mAdapter.hideEdited();
            }
            return true;
        } else if (id == R.id.action_show_unchanged) {
            if(item.isChecked()){
                mAdapter.showUnedited();
            }else {
                mAdapter.hideUnedited();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void filterContent(CharSequence filter) {
        mAdapter.getFilter().filter(filter);
    }
}
