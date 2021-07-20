package com.erg.freecuisine.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.erg.freecuisine.R;
import com.erg.freecuisine.helpers.BillingHelper;
import com.erg.freecuisine.helpers.MessageHelper;
import com.erg.freecuisine.helpers.SharedPreferencesHelper;
import com.erg.freecuisine.helpers.StringHelper;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.TagModel;
import com.erg.freecuisine.util.Constants;
import com.erg.freecuisine.util.Util;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import static com.erg.freecuisine.util.Constants.BOOKMARK_FLAG_KEY;
import static com.erg.freecuisine.util.Constants.JSON_RECIPE_KEY;
import static com.erg.freecuisine.util.Constants.SAVED_STATE_KEY;
import static com.erg.freecuisine.util.Constants.TAG_KEY;
import static com.erg.freecuisine.util.Constants.URL_KEY;

public class AdMobFragment extends Fragment implements View.OnClickListener {

    public static final String TAG = "AdMobFragment";

    private View rootView;
    private CountDownTimer countDownTimer;
    private InterstitialAd mInterstitialAd;
    private TextView tvCountdown;
    private LinearLayout llCountdownContainer;
    private BillingHelper billingHelper;
    private long timerMilliseconds = 7000;
    private SharedPreferencesHelper spHelper;
    private Bundle args;
    private String url;
    private ArrayList<TagModel> tags;
    private Bundle savedState = null;
    private boolean finishedFlag = false;
    private Dialog dialogMission = null;

    public AdMobFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        spHelper = new SharedPreferencesHelper(requireContext());
        tags = new ArrayList<>();

        if (savedInstanceState != null && savedState == null) {
            savedState = savedInstanceState.getBundle(SAVED_STATE_KEY);
            Log.d(TAG, "onCreate: SAVED INSTANCE = " + savedState);
        }

        if (savedState != null) {
            args = savedState.getBundle("args");
        } else {
            args = getArguments();
        }

        if (args != null && !args.isEmpty()) {
            boolean isBookmarkRecipe = args.getBoolean(BOOKMARK_FLAG_KEY, false);
            if (isBookmarkRecipe) {
                String jsonRecipe = args.getString(JSON_RECIPE_KEY, "");
                if (!jsonRecipe.isEmpty()) {
                    RecipeModel recipe = StringHelper.getSingleRecipeFromJson(jsonRecipe);
                    url = recipe.getUrl();
                    tags = new ArrayList<>(recipe.getTags());
                }
            } else {
                url = args.getString(URL_KEY);
                if (args.getParcelableArrayList(TAG_KEY) != null) {
                    tags = args.getParcelableArrayList(TAG_KEY);
                }
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: starts");
        rootView = inflater.inflate(R.layout.fragment_ad_mob, container, false);
        setUpView();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initInterstitialAd(requireActivity());
        billingHelper = new BillingHelper(requireActivity());
        billingHelper.init();
    }

    private void setUpView() {
        Button btnGetPremium = rootView.findViewById(R.id.btn_get_premium);
        tvCountdown = rootView.findViewById(R.id.tv_countdown);
        TextView tvMission = rootView.findViewById(R.id.tv_mission_description);
        llCountdownContainer = rootView.findViewById(R.id.ll_countdown_container);

        btnGetPremium.setOnClickListener(this);
        tvMission.setOnClickListener(this);

        Log.d(TAG, "setUpView: Done!");
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        Util.vibrate(requireContext());
        switch (v.getId()) {
            case R.id.btn_get_premium:
                billingHelper.loadAllSkusAndStartBillingFlow();
                break;
            case R.id.tv_mission_description:
                linkToMissionView();
                break;
        }
    }

    private void linkToMissionView() {
        if (isVisible()) {
            cancelTimer();
            showMissionDialog();
        }
    }

    public void showMissionDialog() {
        dialogMission = new Dialog(requireContext(), R.style.alert_dialog);
        dialogMission.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogMission.setCancelable(false);
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogView = inflater.inflate(R.layout.dialog_mission_view,
                null, false);
        dialogMission.setContentView(dialogView);

        /*onClick on dialog ok button*/
        Button btnOK = dialogView.findViewById(R.id.ok);
        btnOK.setOnClickListener(v -> {
            Util.vibrate(requireContext());
            if (!finishedFlag)
                startCountdown(requireActivity(), timerMilliseconds);
            if (dialogMission.isShowing())
                dialogMission.dismiss();
        });

        dialogMission.show();
        Animation animScaleUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up_long);
        dialogView.startAnimation(animScaleUp);
    }

    private void startCountdown(Activity activity, long countDown) {
        final int countDownInterval = 1000;
        countDownTimer = new CountDownTimer(countDown, countDownInterval) {
            public void onTick(long millisUntilFinished) {
                timerMilliseconds = millisUntilFinished;
                long auxVar = millisUntilFinished / countDownInterval;
                tvCountdown.setText(String.valueOf((int) auxVar));
                Log.d(TAG, "onTick: isOnTick");
            }

            public void onFinish() {
                finishedFlag = true;
                if (isVisible()) {
                    tvCountdown.setText("");
                    Util.hideView(null, llCountdownContainer);
                    showInterstitial(activity);
                    Log.d(TAG, "onFinish: countDownTimer Finished");
                }
            }
        };
        countDownTimer.start();
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            Log.d(TAG, "cancelTimer: countDownTimer Canceled");
        }
    }

    private void initInterstitialAd(final Context context) {
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(
                context, Constants.interstitial_ad_unit_id, adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        super.onAdLoaded(interstitialAd);
                        Log.d(TAG, "onAdLoaded: done!");
                        mInterstitialAd = interstitialAd;
                        setUpInterstitialAd();
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        super.onAdFailedToLoad(loadAdError);
                        mInterstitialAd = null;
                        Log.d(TAG, "onAdFailedToLoad: ErrorCode : " + loadAdError.getMessage());
                    }
                });
    }

    private void setUpInterstitialAd() {
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                // Called when fullscreen content is dismissed.
                Log.d(TAG, "The ad was dismissed.");
                Util.loadFragmentByUrl(requireActivity(),
                        R.id.action_adMobFragment_to_singleRecipeFragment,
                        url, tags);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(@NotNull AdError adError) {
                // Called when fullscreen content failed to show.
                Log.d(TAG, "The ad failed to show.");
            }

            @Override
            public void onAdShowedFullScreenContent() {
                // Called when fullscreen content is shown.
                // Make sure to set your reference to null so you don't
                // show it a second time.
                mInterstitialAd = null;
                Log.d(TAG, "The ad was shown.");
            }
        });
    }


    private void showInterstitial(Activity activity) {
        // Show the ad if it"s ready. Otherwise toast and reload the ad.
        if (mInterstitialAd != null) {
            mInterstitialAd.show(activity);
            Log.d(TAG, "showInterstitial: Showing Ad ready!");
        } else {
//            Toast.makeText(activity, R.string.ad_not_load, Toast.LENGTH_SHORT).show();
            MessageHelper.showWarningMessageOnMain(activity, getString(R.string.ad_not_load));
            Util.loadFragmentByUrl(requireActivity(),
                    R.id.action_adMobFragment_to_singleRecipeFragment,
                    url, tags);
//            Util.goBack(requireActivity());
            Log.d(TAG, "showInterstitial: Ad dit not load, is not ready");
        }
    }

    @Override
    public void onPause() {
        cancelTimer();
        if (dialogMission != null && dialogMission.isShowing()) {
            dialogMission.dismiss();
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!spHelper.getPremiumStatus()) {
            if (!finishedFlag)
                startCountdown(requireActivity(), timerMilliseconds);
        } else {
            Util.loadFragmentByUrl(requireActivity(),
                    R.id.action_adMobFragment_to_singleRecipeFragment,
                    url, tags);
        }

        Util.hideBottomBar(requireActivity());
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(SAVED_STATE_KEY, (savedState != null) ? savedState : saveState());
        Log.d(TAG, "onSaveInstanceState: outState = " + outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        savedState = saveState();
        Log.d(TAG, "onDestroyView: savedState = " + savedState);
    }

    private Bundle saveState() {
        Bundle state = new Bundle();
        if (args != null) {
            state.putBundle("args", args);
        }
        return state;
    }
}