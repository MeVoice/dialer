package com.mevoice.callrouter;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


public class EULAFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_eula, container, false);
        getDialog().setTitle(getString(R.string.title_eula));
        Button eula_agree = (Button) rootView.findViewById(R.id.eula_agree);
        eula_agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CRApp.appdata.setEULA(true);
                CRApp.appdataaccess.saveAppData();
                dismiss();
            }
        });
        return rootView;
    }
}
