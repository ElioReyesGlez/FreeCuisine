package com.erg.freecuisine.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.erg.freecuisine.R;
import com.erg.freecuisine.helpers.RealmHelper;
import com.erg.freecuisine.helpers.SharedPreferencesHelper;
import com.erg.freecuisine.helpers.TimeHelper;
import com.erg.freecuisine.util.Util;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "SettingsFragment";

    private View rootView;
    private SharedPreferencesHelper spHelper;
    private SwitchMaterial vibrationSwitch, shuffleSwitch, scrollUpSwitch;
    private RealmHelper realmHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        spHelper = new SharedPreferencesHelper(requireContext());
        realmHelper = new RealmHelper();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
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

        scrollUpSwitch = rootView.findViewById(R.id.switch_scroll_up);
        scrollUpSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            buttonView.setChecked(isChecked);
            spHelper.setScrollUpStatus(isChecked);
        });


        setUserBoard();
        setSwitchesStateSaved();

        return rootView;
    }

    private void setUserBoard() {
        TextView tv_bookmarks_cont = rootView.findViewById(R.id.tv_bookmarks_cont);
        TextView tv_user_usage = rootView.findViewById(R.id.tv_user_usage);
        TextView tv_last_usage_date = rootView.findViewById(R.id.tv_last_usage_date);

        int bookmarks = 0;
        if (realmHelper.getRecipes() != null) {
            bookmarks = realmHelper.getRecipes().size();
        }

        long lastUsage = spHelper.getLastUsage();

        tv_bookmarks_cont.setText(String.valueOf(bookmarks));
        tv_user_usage.setText(getUsage());

        if (lastUsage != 0)
            tv_last_usage_date.setText(TimeHelper.dateFormatterShort(lastUsage));

    }

    private String getUsage() {
        float usage = 0;
        for (float value : spHelper.getUserActivity()) {
            usage += value;
        }
        return String.valueOf(usage);
    }

    private void setSwitchesStateSaved() {
        vibrationSwitch.setChecked(spHelper.getVibrationStatus());
        shuffleSwitch.setChecked(spHelper.getShuffleStatus());
        scrollUpSwitch.setChecked(spHelper.getScrollUpStatus());
    }

    @SuppressLint("NonConstantResourceId")
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