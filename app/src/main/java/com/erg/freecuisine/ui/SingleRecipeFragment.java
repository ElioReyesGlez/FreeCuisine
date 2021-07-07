package com.erg.freecuisine.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;

import com.airbnb.lottie.LottieAnimationView;
import com.erg.freecuisine.R;
import com.erg.freecuisine.controller.network.AsyncDataLoad;
import com.erg.freecuisine.controller.network.helpers.MessageHelper;
import com.erg.freecuisine.controller.network.helpers.RealmHelper;
import com.erg.freecuisine.controller.network.helpers.SharedPreferencesHelper;
import com.erg.freecuisine.controller.network.helpers.StringHelper;
import com.erg.freecuisine.controller.network.helpers.TimeHelper;
import com.erg.freecuisine.interfaces.OnRecipeListener;
import com.erg.freecuisine.models.RealmRecipeModel;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.StepModel;
import com.erg.freecuisine.models.TagModel;
import com.erg.freecuisine.models.VideoModel;
import com.erg.freecuisine.util.Util;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.squareup.picasso.Picasso;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.CancellationException;

import kotlinx.coroutines.Job;

import static com.erg.freecuisine.util.Constants.BOOKMARK_FLAG_KEY;
import static com.erg.freecuisine.util.Constants.JSON_RECIPE_KEY;
import static com.erg.freecuisine.util.Constants.SAVED_STATE_KEY;
import static com.erg.freecuisine.util.Constants.TAG_KEY;
import static com.erg.freecuisine.util.Constants.URL_KEY;


public class SingleRecipeFragment extends Fragment implements OnRecipeListener, View.OnClickListener {

    public static final String TAG = "SingleRecipeFragment";
    private String url;
    private View rootView;
    private RelativeLayout relative_container_recipe_main_info;
    private LinearLayout linear_layout_empty_container;
    private LinearLayout linear_amounts_container;
    private LinearLayout table_recipe_ingredients_container;
    private LottieAnimationView lottie_anim_loading;
    private ImageButton ibBookmark;
    private Animation scaleUp, scaleDown;
    private ArrayList<TagModel> tags;
    private AsyncDataLoad asyncDataLoad;
    private YouTubePlayerView videoView;
    private RecipeModel recipe;

    private SharedPreferencesHelper spHelper;
    private RealmHelper realmHelper;
    private Job recipeLoaderJob;
    private Bundle savedState = null;
    private Bundle args;
    private boolean isBookmarkChecked;
    private boolean isBookmarkRecipe;

    public SingleRecipeFragment() {
        // Required empty public constructor
    }

    public static SingleRecipeFragment newInstance() {
        return new SingleRecipeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        Transition transition = TransitionInflater.from(requireContext()).inflateTransition(
                android.R.transition.move);
        setSharedElementEnterTransition(transition);
        setSharedElementReturnTransition(transition);
        tags = new ArrayList<>();

        if (savedInstanceState != null && savedState == null) {
            savedState = savedInstanceState.getBundle(SAVED_STATE_KEY);
            Log.d(TAG, "onCreate: SAVED INSTANCE = " + savedState);
        }

        if (savedState != null) {
            isBookmarkChecked = savedState.getBoolean("isSaved", false);
            args = savedState.getBundle("args");
        } else {
            args = getArguments();
        }

        if (args != null && !args.isEmpty()) {
            isBookmarkRecipe = args.getBoolean(BOOKMARK_FLAG_KEY, false);
            if (isBookmarkRecipe) {
                String jsonRecipe = args.getString(JSON_RECIPE_KEY, "");
                if (!jsonRecipe.isEmpty())
                    recipe = StringHelper.getSingleRecipeFromJson(jsonRecipe);
            } else {
                url = args.getString(URL_KEY);
                if (args.getParcelableArrayList(TAG_KEY) != null) {
                    tags = args.getParcelableArrayList(TAG_KEY);
                }
            }
        }

        realmHelper = new RealmHelper();
        spHelper = new SharedPreferencesHelper(requireContext());
        asyncDataLoad = new AsyncDataLoad();
        scaleUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.fragment_single_recipe, container, false);

        linear_layout_empty_container = rootView
                .findViewById(R.id.linear_layout_empty_container);
        relative_container_recipe_main_info = rootView
                .findViewById(R.id.relative_container_recipe_main_info);
        linear_amounts_container = rootView
                .findViewById(R.id.linear_amounts_container);
        table_recipe_ingredients_container = rootView
                .findViewById(R.id.table_recipe_ingredients_container);
        lottie_anim_loading = rootView
                .findViewById(R.id.lottie_anim_loading);

        Util.showView(scaleUp, lottie_anim_loading);

        linear_layout_empty_container.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");
        if (isBookmarkRecipe) {
            setUpView(recipe);
        } else {
            stopLoading();
            recipeLoaderJob = asyncDataLoad.loadSingleRecipeAsync(requireActivity(),
                    this, url, tags);
        }
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
            ibBookmark = rootView.findViewById(R.id.ib_bookmark);
            TextView recipeTitle = rootView.findViewById(R.id.recipe_title);
            TextView recipeDescription = rootView.findViewById(R.id.recipe_description);
            AppCompatTextView type = rootView.findViewById(R.id.tv_type);
            AppCompatTextView cockingTime = rootView.findViewById(R.id.tv_cocking_time);
            AppCompatTextView peopleAmount = rootView.findViewById(R.id.tv_people_amount);

            RelativeLayout typeContainer = rootView
                    .findViewById(R.id.relative_recipes_type_container);
            RelativeLayout timeContainer = rootView
                    .findViewById(R.id.recipes_cooking_time_container);
            RelativeLayout peopleContainer = rootView
                    .findViewById(R.id.recipes_amount_container);

            LinearLayout linearColumn1 = rootView.findViewById(R.id.linear_column_1);
            LinearLayout linearColumn2 = rootView.findViewById(R.id.linear_column_2);
            ibBookmark.setOnClickListener(this);

            checkBookmark(realmHelper.exists(recipe));

            if (tags != null && !tags.isEmpty())
                recipe.setTags(tags); // From Bundle Args

            recipeTitle.setText(recipe.getTitle());
            recipeDescription.setText(recipe.getDescription());
            recipeDescription.setOnClickListener(v -> showDescriptionDialog(recipe.getDescription()));

            if (!recipe.getTime().isEmpty()) {
                Util.showView(null, timeContainer);
                cockingTime.setText(recipe.getTime());
            } else
                Util.hideView(null, timeContainer);

            if (!recipe.getType().isEmpty()) {
                Util.showView(null, typeContainer);
                type.setText(recipe.getType());
            } else
                Util.hideView(null, typeContainer);

            if (!recipe.getDiners().isEmpty()) {
                Util.showView(null, peopleContainer);
                peopleAmount.setText(recipe.getDiners());
            } else
                Util.hideView(null, peopleContainer);

            if (recipe.getDiners().isEmpty() && recipe.getType().isEmpty()
                    && recipe.getTime().isEmpty()) {
                Util.hideView(null, linear_amounts_container);
            } else {
                Util.showView(null, linear_amounts_container);
            }

            String ingredientsStr = recipe.getIngredients();

            if (!ingredientsStr.isEmpty()) {
                String[] ingredients = Util.extractIngredients(ingredientsStr);
                for (int i = 0; i < ingredients.length; i++) {
                    String ingredient = ingredients[i];
                    View view = getLayoutInflater().inflate(R.layout.item_ingredint, null);
                    CheckBox checkBox = view.findViewById(R.id.checkBox);
                    TextView textView = view.findViewById(R.id.textViewIngredient);
                    textView.setText(ingredient);
                    view.setId(i);
                    if (i <= (ingredients.length - 1) / 2) {
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
                Util.showView(null, table_recipe_ingredients_container);
            } else {
                Util.hideView(null, linear_amounts_container);
                Util.hideView(null, table_recipe_ingredients_container);
            }

            Picasso.get()
                    .load(recipe.getImage().getUrl())
                    .error(R.drawable.ic_lunch_chef_mini)
                    .placeholder(R.drawable.ic_loading_icon)
                    .into(recipeImg);

            LinearLayout linear_preparation_container = rootView.findViewById(R.id.linear_preparation_container);
            if (!recipe.getSteps().isEmpty()) {
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

                    tv_preparation_number_order.setText(step.getStepNumber());
                    tv_preparation_description.setText(step.getPreparation());

                    if (!step.getStepLink().isEmpty()) {
                        view.setBackgroundResource(R.drawable.selector_white);
                        view.setClickable(true);
                        view.setFocusable(true);
                        view.setOnClickListener(v -> {
                            loadFragment(step.getStepLink(), tags);
                        });
                        Log.d(TAG, "setUpView: STEP = " + step.toString());
                    }

                    linear_preparation_container.addView(view);
                }
                Util.showView(null, linear_preparation_container);
            } else {
                Util.hideView(null, linear_preparation_container);
            }

            Util.showView(null, relative_container_recipe_main_info);
            Util.hideView(null, linear_layout_empty_container);
        } else {
            Util.showView(scaleUp, linear_layout_empty_container);
            Util.hideView(null, relative_container_recipe_main_info);
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

    private void checkBookmark(boolean flag) {
        if (flag) {
            isBookmarkChecked = true;
            ibBookmark.setImageResource(R.drawable.ic_bookmark);
        } else {
            isBookmarkChecked = false;
            ibBookmark.setImageResource(R.drawable.ic_bookmark_outline);
        }
        ibBookmark.startAnimation(scaleUp);
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

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        Util.vibrate(requireContext());
        switch (v.getId()) {
            case R.id.linear_layout_empty_container:
                Util.refreshCurrentFragmentWithArgs(requireActivity(), args);
                break;
            case R.id.ib_bookmark:

                String strJsonRecipe = new Gson().toJson(recipe);
                RealmRecipeModel realmRecipeModel = new RealmRecipeModel(recipe.getId(), strJsonRecipe);
                if (!isBookmarkChecked) {
                    isBookmarkChecked = true;
                    checkBookmark(true);
                    realmHelper.insertOrUpdate(realmRecipeModel);
                    if (isVisible())
                        MessageHelper.showInfoMessage(requireActivity(),
                                getString(R.string.saved), rootView);
                } else {
                    isBookmarkChecked = false;
                    checkBookmark(false);
                    realmHelper.deleteRecipe(realmRecipeModel);
                    if (isVisible())
                        MessageHelper.showInfoMessage(requireActivity(),
                                getString(R.string.removed), rootView);
                }
                break;
        }
    }

    private void loadFragment(String url, ArrayList<TagModel> tags) {
        NavController navController = Navigation
                .findNavController(requireActivity(), R.id.nav_host_fragment);
        Bundle args = new Bundle();
        args.putString(URL_KEY, url);
        args.putParcelableArrayList(TAG_KEY, tags);
        navController.navigate(R.id.action_singleRecipeFragment_self, args);
    }

    private void showDescriptionDialog(String text) {

        final Dialog dialog = new Dialog(requireContext(), R.style.alert_dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogView = inflater.inflate(R.layout.dialog_description_text, null, false);
        TextView msg = dialogView.findViewById(R.id.tv_description);

        msg.setText(text);
        dialog.setContentView(dialogView);

        Button cancelBtn = dialog.findViewById(R.id.ok);
        cancelBtn.setOnClickListener(v -> {
            Util.vibrate(requireContext());
            if (dialog.isShowing())
                dialog.dismiss();
        });

        dialog.show();
        dialogView.startAnimation(scaleUp);

    }

    @Override
    public void onRecipeClick(int position, View view) {
        //Empty
    }

    @Override
    public void onRecipesLoaded(ArrayList<RecipeModel> recipes) {
        //Empty
    }

    @Override
    public void onLoaderFailed(ArrayList<RecipeModel> recipes, Exception e) {
        Log.d(TAG, "onRecipesLoadedFailed: ERROR = " + e.toString());
        if (e instanceof SocketTimeoutException) {
            showNetworkErrorMessage();
        }
        if (e instanceof SocketException) {
            showNetworkErrorMessage();
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onRecommendedRecipesLoaded(ArrayList<RecipeModel> recipes) {
        /*Empty*/
    }

    @Override
    public void onTipsRecipesLoaded(ArrayList<RecipeModel> recipes) {
        /*Empty*/
    }

    private void showNetworkErrorMessage() {
        if (isVisible()) {
            requireActivity().runOnUiThread(() -> {
                MessageHelper.showInfoMessageWarning(
                        requireActivity(), getString(R.string.network_error),
                        rootView);
                refreshView();
            });
        } else {
            Toast.makeText(requireContext(),
                    getString(R.string.network_error), Toast.LENGTH_LONG).show();
        }
    }

    private void showErrorMessage() {
        if (isVisible()) {
            requireActivity().runOnUiThread(() -> {
                MessageHelper.showInfoMessageError(
                        requireActivity(), getString(R.string.some_error),
                        rootView);
                refreshView();
            });
        } else {
            Toast.makeText(requireContext(),
                    getString(R.string.some_error), Toast.LENGTH_LONG).show();
        }
    }

    public void refreshView() {
        if (recipe != null && !recipe.getId().isEmpty()) {
            Util.showView(scaleUp, linear_layout_empty_container);
            Util.hideView(null, relative_container_recipe_main_info);
        } else {
            Util.showView(scaleUp, relative_container_recipe_main_info);
            Util.hideView(null, linear_layout_empty_container);
        }
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
        state.putBoolean("isSaved", isBookmarkChecked);
        if (args != null) {
            state.putBundle("args", args);
        }
        return state;
    }
}