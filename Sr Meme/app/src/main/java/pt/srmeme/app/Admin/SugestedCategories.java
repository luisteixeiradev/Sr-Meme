package pt.srmeme.app.Admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import pt.srmeme.app.R;

public class SugestedCategories extends Fragment {


    public SugestedCategories() {
        // Required empty public constructor
    }

    public static SugestedCategories newInstance(String param1, String param2) {
        SugestedCategories fragment = new SugestedCategories();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sugested_categories, container, false);
    }

}