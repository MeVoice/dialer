package com.ebookfrenzy.dialerintent;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 */
public class RuleAddFragment extends CommonFragment {


    public RuleAddFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initAppData();
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.group_edit, container, false);
    }

}
