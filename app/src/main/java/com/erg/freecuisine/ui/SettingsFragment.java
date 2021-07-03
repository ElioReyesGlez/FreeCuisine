package com.erg.freecuisine.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.erg.freecuisine.R;
import com.erg.freecuisine.controller.network.helpers.SharedPreferencesHelper;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.TagModel;
import com.erg.freecuisine.util.Util;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;

import static com.erg.freecuisine.util.Constants.TAG_KEY;
import static com.erg.freecuisine.util.Constants.URL_KEY;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SettingsFragment";

    private View rootView;
    private SharedPreferencesHelper spHelper;
    private SwitchMaterial vibrationSwitch, shuffleSwitch;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spHelper = new SharedPreferencesHelper(requireContext());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_settings, container, false);
        return setUpView(rootView);
    }

    private View setUpView(View rootView) {
        RelativeLayout rlAbout = rootView.findViewById(R.id.rl_about_container);
        RelativeLayout rlBookmarks = rootView.findViewById(R.id.rl_bookmarks_container);
        rlAbout.setOnClickListener(this);
        rlBookmarks.setOnClickListener(this);

        vibrationSwitch = rootView.findViewById(R.id.switch_vibration);
        vibrationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            buttonView.setChecked(isChecked);
            spHelper.setVibrationStatus(isChecked);
        });

        shuffleSwitch = rootView.findViewById(R.id.switch_shuffle);
        shuffleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            buttonView.setChecked(isChecked);
            spHelper.setShuffleStatus(isChecked);
        });


        setSwitchesStateSaved();

        return rootView;
    }

    private void setSwitchesStateSaved() {
        vibrationSwitch.setChecked(spHelper.getVibrationStatus());
        shuffleSwitch.setChecked(spHelper.getShuffleStatus());
    }

    @Override
    public void onClick(View v) {
        Util.vibrate(requireContext());
        int viewId = v.getId();
        switch (viewId) {
            case R.id.rl_about_container:
                loadFragment(R.id.action_navigation_settings_to_aboutFragment);
                break;
            case R.id.rl_bookmarks_container:
                loadFragment(R.id.action_navigation_settings_to_bookmarksFragment);
                break;
        }
    }

    private void loadFragment(int actionId) {
        NavController navController = Navigation
                .findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(actionId);
    }
}