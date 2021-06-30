package com.erg.freecuisine.ui;

import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;

import com.airbnb.lottie.LottieAnimationView;
import com.erg.freecuisine.R;
import com.erg.freecuisine.controller.network.AsyncDataLoad;
import com.erg.freecuisine.controller.network.helpers.SharedPreferencesHelper;
import com.erg.freecuisine.controller.network.helpers.StringHelper;
import com.erg.freecuisine.controller.network.helpers.TimeHelper;
import com.erg.freecuisine.interfaces.OnRecipeListener;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.StepModel;
import com.erg.freecuisine.models.TagModel;
import com.erg.freecuisine.models.VideoModel;
import com.erg.freecuisine.util.Util;
import com.google.android.material.imageview.ShapeableImageView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.CancellationException;

import kotlinx.coroutines.Job;

import static com.erg.freecuisine.util.Constants.RECIPE_KEY;
import static com.erg.freecuisine.util.Constants.TAG_KEY;
import static com.erg.freecuisine.util.Constants.URL_KEY;


public class SingleRecipeFragment extends Fragment implements OnRecipeListener {

    public static final String TAG = "RecipeFragment";
    private String url;
    private View rootView;
    private RelativeLayout relative_container_recipe_main_info;
    private LinearLayout linear_layout_empty_container;
    private LottieAnimationView lottie_anim_loading;
    private Animation scaleUp, scaleDown;
    private ArrayList<TagModel> tags;
    private AsyncDataLoad asyncDataLoad;
    private YouTubePlayerView videoView;
    private RecipeModel recipe;

    private SharedPreferencesHelper spHelper;
    private Job recipeLoaderJob;

    public SingleRecipeFragment() {
        // Required empty public constructor
    }

    public static SingleRecipeFragment newInstance() {
        return new SingleRecipeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Transition transition = TransitionInflater.from(requireContext()).inflateTransition(
                android.R.transition.move);
        setSharedElementEnterTransition(transition);
        setSharedElementReturnTransition(transition);

        Bundle args = getArguments();
        if (args != null && !args.isEmpty()) {
            url = args.getString(URL_KEY);
            if (args.getParcelableArrayList(TAG_KEY) != null) {
                tags = args.getParcelableArrayList(TAG_KEY);
            }
        }

        spHelper = new SharedPreferencesHelper(requireContext());
        asyncDataLoad = new AsyncDataLoad();
        scaleUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_single_recipe, container, false);

        linear_layout_empty_container = rootView
                .findViewById(R.id.linear_layout_empty_container);
        relative_container_recipe_main_info = rootView
                .findViewById(R.id.relative_container_recipe_main_info);
        lottie_anim_loading = rootView
                .findViewById(R.id.lottie_anim_loading);
        Util.showView(scaleUp, lottie_anim_loading);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        stopLoading();
        recipeLoaderJob = asyncDataLoad.loadSingleRecipeAsync(requireActivity(),
                this, url, tags);
    }

    private void stopLoading() {
        if (recipeLoaderJob != null && recipeLoaderJob.isActive()) {
            recipeLoaderJob.cancel(new CancellationException());
            Log.d(TAG, "onDestroyView: CANCELING JOB = "
                    + recipeLoaderJob.isCancelled());
        }
    }

    @Override
    public void onSingleRecipeLoaded(RecipeModel recipe) {
        setUpView(recipe);
    }

    public void setUpView(RecipeModel recipe) {

        Util.hideView(null, lottie_anim_loading);
        if (recipe != null && !recipe.getTitle().isEmpty()) {
            this.recipe = recipe;

            ShapeableImageView recipeImg = rootView.findViewById(R.id.recipe_main_image);
            AppCompatTextView type = rootView.findViewById(R.id.tv_type);
            TextView recipeTitle = rootView.findViewById(R.id.recipe_title);
            TextView recipeDescription = rootView.findViewById(R.id.recipe_description);
            LinearLayout linearColumn1 = rootView.findViewById(R.id.linear_column_1);
            LinearLayout linearColumn2 = rootView.findViewById(R.id.linear_column_2);
            RelativeLayout typeContainer = rootView
                    .findViewById(R.id.relative_recipes_type_container);


            if (tags != null && !tags.isEmpty())
                recipe.setTags(tags); // From Bundle Args
            recipeTitle.setText(recipe.getTitle());
            recipeDescription.setText(recipe.getDescription());

            if (!recipe.getType().isEmpty()) {
                Util.showView(null, typeContainer);
                type.setText(recipe.getType());
            } else {
                Util.hideView(null, typeContainer);
            }

            String[] ingredients = Util.extractIngredients(recipe.getIngredients());
            for (int i = 0; i < ingredients.length; i++) {
                String ingredient = ingredients[i];
                View view = getLayoutInflater().inflate(R.layout.item_ingredint, null);
                CheckBox checkBox = view.findViewById(R.id.checkBox);
                TextView textView = view.findViewById(R.id.textViewIngredient);
                textView.setText(ingredient);
                view.setId(i);
                if (i <= ingredients.length / 2) {
                    linearColumn1.addView(view);
                } else {
                    linearColumn2.addView(view);
                }
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked)
                        textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    else
                        textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                });
            }


            Picasso.get()
                    .load(recipe.getImage().getUrl())
                    .error(R.drawable.ic_lunch_chef_mini)
                    .placeholder(R.drawable.ic_loading_icon)
                    .into(recipeImg);

            LinearLayout linear_preparation_container = rootView.findViewById(R.id.linear_preparation_container);
            for (StepModel step : recipe.getSteps()) {
                View view = getLayoutInflater().inflate(R.layout.item_recipe_preparation_step_img, null);

                if (step.getImage() != null && !step.getImage().getUrl().isEmpty()) {
                    ShapeableImageView iv_step_preparation_image = view.findViewById(R.id.iv_step_preparation_image);
                    Picasso.get().load(step.getImage().getUrl()).into(iv_step_preparation_image);
                }

                if (step.getVideo() != null && !step.getVideo().getId().isEmpty()) {
                    view = getLayoutInflater().inflate(R.layout.item_recipe_preparation_step_video, null);
                    videoView = view.findViewById(R.id.youtube_player_view);
                    getViewLifecycleOwner().getLifecycle().addObserver(videoView);
                    setUpYouPlayerView(videoView, step.getVideo());
                }

                TextView tv_preparation_number_order = view.findViewById(R.id.tv_preparation_number_order);
                TextView tv_preparation_description = view.findViewById(R.id.tv_preparation_description);
//                TextView tv_tips = view.findViewById(R.id.tv_tips);

                tv_preparation_number_order.setText(String.valueOf(step.getStepNumber()));
                tv_preparation_description.setText(step.getPreparation());
                linear_preparation_container.addView(view);
            }

            Util.showView(null, relative_container_recipe_main_info);
            Util.hideView(null, linear_layout_empty_container);
        } else {
            Util.showView(scaleUp, linear_layout_empty_container);
            Util.hideView(scaleDown, relative_container_recipe_main_info);
        }
    }

    private void setUpYouPlayerView(YouTubePlayerView videoView, VideoModel video) {
        videoView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.cueVideo(video.getId(), 0);
            }
        });

    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: Usage = " + spHelper.getUsageOpenTime());
        super.onStart();
        spHelper.saveUsageOpenTime(System.currentTimeMillis());
    }

    @Override
    public void onPause() {
        super.onPause();
        if (recipe != null) {
            saveUsageByWeekDay();
            spHelper.saveLastRecipeRead(recipe);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (videoView != null)
            videoView.release();
    }

    private void saveUsageByWeekDay() {

        String dayCodeKey;
        String[] dayCodes = getResources().getStringArray(R.array.day_codes);
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        int day = calendar.get(Calendar.DAY_OF_WEEK);

        long currentTime = System.currentTimeMillis();
        long openTimeUsage = spHelper.getUsageOpenTime();
        long diff = TimeHelper.getDifferenceInMinutes(currentTime, openTimeUsage);
        float usageScore = Util.calculateUsageScore(diff);

        Log.d(TAG, "saveUsageScoreByWeekDay: Memorizing difference: " + diff);
        Log.d(TAG, "saveUsageScoreByWeekDay: UsageScore: " + usageScore);

        switch (day) {
            case Calendar.MONDAY:
                dayCodeKey = dayCodes[0] + 1;
                spHelper.increasesUsageValue(dayCodeKey, usageScore);
                break;
            case Calendar.TUESDAY:
                dayCodeKey = dayCodes[1] + 2;
                spHelper.increasesUsageValue(dayCodeKey, usageScore);
                break;
            case Calendar.WEDNESDAY:
                dayCodeKey = dayCodes[2] + 3;
                spHelper.increasesUsageValue(dayCodeKey, usageScore);
                break;
            case Calendar.THURSDAY:
                dayCodeKey = dayCodes[3] + 4;
                spHelper.increasesUsageValue(dayCodeKey, usageScore);
                break;
            case Calendar.FRIDAY:
                dayCodeKey = dayCodes[4] + 5;
                spHelper.increasesUsageValue(dayCodeKey, usageScore);
                break;
            case Calendar.SATURDAY:
                dayCodeKey = dayCodes[5] + 6;
                spHelper.increasesUsageValue(dayCodeKey, usageScore);
                break;
            case Calendar.SUNDAY:
                dayCodeKey = dayCodes[6] + 7;
                spHelper.increasesUsageValue(dayCodeKey, usageScore);
                break;
        }
    }

    @Override
    public void onRecipeClick(int position, View view) {
        //Empty
    }

    @Override
    public void onRecipesLoaded(ArrayList<RecipeModel> recipes) {
        //Empty
    }
}