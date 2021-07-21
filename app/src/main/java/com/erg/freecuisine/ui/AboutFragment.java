package com.erg.freecuisine.ui;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.erg.freecuisine.BuildConfig;
import com.erg.freecuisine.R;
import com.erg.freecuisine.util.Util;

import static com.erg.freecuisine.util.Constants.GOOGLE_APP_DETAILS_URL;
import static com.erg.freecuisine.util.Constants.LINKEDIN_PACKAGE;
import static com.erg.freecuisine.util.Constants.MAIN_RECIPE_SERVER;
import static com.erg.freecuisine.util.Constants.MARKET_APP_DETAILS_URL;
import static com.erg.freecuisine.util.Constants.SPACE;
import static com.erg.freecuisine.util.Constants.URL_DEVELOPER;


public class AboutFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "AboutFragment";
    private View rootView;

    public AboutFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_about, container, false);
        setUpView();
        return rootView;
    }

    private void setUpView() {
        RelativeLayout rate_on_play_store_layout = rootView.findViewById(R.id.rate_on_play_store);
        TextView tv_app_description = rootView.findViewById(R.id.tv_app_description);
        RelativeLayout contact_us_layout = rootView.findViewById(R.id.contact_us);
        RelativeLayout developer1_layout = rootView.findViewById(R.id.developer1);

        /*String description = getString(R.string.description_about_page)
                + NEWLINE + getString(R.string.description_about_page2);
        tv_app_description.setText(description);*/
        setupHyperlink(tv_app_description);

        setVersion();

        rate_on_play_store_layout.setOnClickListener(this);
        contact_us_layout.setOnClickListener(this);
        developer1_layout.setOnClickListener(this);
    }

    private void setupHyperlink(TextView tv_app_description) {
        tv_app_description.setMovementMethod(LinkMovementMethod.getInstance());
        tv_app_description.setLinksClickable(true);
        tv_app_description.setLinkTextColor(getResources().getColor(R.color.blue));
        tv_app_description.setOnClickListener(this);
    }

    private void setVersion() {
        TextView tvVersion = rootView.findViewById(R.id.tv_app_version);
        String stringBuilder = getString(R.string.version)
                + SPACE + BuildConfig.VERSION_NAME;
        tvVersion.setText(stringBuilder);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        Util.vibrate(requireContext());
        switch (v.getId()) {
            case R.id.rate_on_play_store:
                rateOnPlayStore();
                break;
            case R.id.contact_us:
                contactUs(getString(R.string.email_about_page));
                break;
            case R.id.developer1:
                lookInLinkedInProfile();
                break;
            case R.id.tv_app_description:
                Util.goToBrowser(requireActivity(), MAIN_RECIPE_SERVER);
                break;
        }
    }

    //Set intent to go to MarketPlace on play store and rate the app*
    private void rateOnPlayStore() {
        try {
            Intent market = new Intent(Intent.ACTION_VIEW, Uri.parse(MARKET_APP_DETAILS_URL
                    + requireActivity().getPackageName()));
            startActivity(market);
        } catch (ActivityNotFoundException e) {
            Intent googlePlayStore = new Intent(Intent.ACTION_VIEW, Uri.parse(GOOGLE_APP_DETAILS_URL
                    + requireActivity().getPackageName()));
            startActivity(googlePlayStore);
        }
    }

    //Set intent to go to LinkedIn with the exact profile of the developer*
    private void lookInLinkedInProfile() {
        try {
            Uri uri = Uri.parse(URL_DEVELOPER);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage(LINKEDIN_PACKAGE);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(URL_DEVELOPER)));
        }
    }

    //Set intent to choose mail app and send to*
    private void contactUs(String email) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
        startActivity(intent);
    }

}
