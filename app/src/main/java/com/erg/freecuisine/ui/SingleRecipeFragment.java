package com.erg.freecuisine.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.Fragment;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;

import com.airbnb.lottie.LottieAnimationView;
import com.erg.freecuisine.R;
import com.erg.freecuisine.adapters.LinksAdapter;
import com.erg.freecuisine.controller.network.AsyncDataLoad;
import com.erg.freecuisine.helpers.MessageHelper;
import com.erg.freecuisine.helpers.RealmHelper;
import com.erg.freecuisine.helpers.SharedPreferencesHelper;
import com.erg.freecuisine.helpers.StringHelper;
import com.erg.freecuisine.helpers.TimeHelper;
import com.erg.freecuisine.interfaces.OnRecipeListener;
import com.erg.freecuisine.models.LinkModel;
import com.erg.freecuisine.models.RealmRecipeModel;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.StepModel;
import com.erg.freecuisine.models.TagModel;
import com.erg.freecuisine.models.VideoModel;
import com.erg.freecuisine.util.Util;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.squareup.picasso.Picasso;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.CancellationException;

import kotlinx.coroutines.Job;

import static com.erg.freecuisine.util.Constants.BOOKMARK_FLAG_KEY;
import static com.erg.freecuisine.util.Constants.JSON_RECIPE_KEY;
import static com.erg.freecuisine.util.Constants.MAIN_RECIPE_SERVER;
import static com.erg.freecuisine.util.Constants.SAVED_STATE_KEY;
import static com.erg.freecuisine.util.Constants.TAG_KEY;
import static com.erg.freecuisine.util.Constants.URL_KEY;


public class SingleRecipeFragment extends Fragment implements OnRecipeListener,
        View.OnClickListener, ViewTreeObserver.OnScrollChangedListener {

    public static final String TAG = "SingleRecipeFragment";
    private String url;
    private View rootView;
    private RelativeLayout relative_container_recipe_main_info;
    private RelativeLayout relative_top_menu;
    private ScrollView main_scroll_view;
    private LinearLayout linear_layout_empty_container;
    private LinearLayout linear_amounts_container;
    private LinearLayout table_recipe_ingredients_container;
    private LottieAnimationView lottie_anim_loading;
    private ImageButton ibBookmark;
    private ImageButton btn_up;
    private Animation scaleUp, scaleDown;
    private Animation enter;
    private Animation exit;
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
    private Handler handlerDelay;
    private Runnable runnableHideUpBtn;

    public SingleRecipeFragment() {
        // Required empty public constructor
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
                if (!jsonRecipe.isEmpty()) {
                    recipe = StringHelper.getSingleRecipeFromJson(jsonRecipe);
                    url = recipe.getUrl();
                }
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
        enter = AnimationUtils.loadAnimation(requireContext(), R.anim.custom_enter_anim);
        exit = AnimationUtils.loadAnimation(requireContext(), R.anim.custom_exit_anim);
        handlerDelay = new Handler(Looper.getMainLooper());
        runnableHideUpBtn = () -> {
            if (isAdded() && isVisible()) {
                Util.hideViewInvisibleWay(exit, btn_up);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        rootView = inflater.inflate(R.layout.fragment_single_recipe, container, false);

        linear_layout_empty_container = rootView
                .findViewById(R.id.linear_layout_empty_container);
        main_scroll_view = rootView
                .findViewById(R.id.main_scroll_view);
        relative_container_recipe_main_info = rootView
                .findViewById(R.id.relative_container_recipe_main_info);
        linear_amounts_container = rootView
                .findViewById(R.id.linear_amounts_container);
        table_recipe_ingredients_container = rootView
                .findViewById(R.id.table_recipe_ingredients_container);
        lottie_anim_loading = rootView
                .findViewById(R.id.lottie_anim_loading);
        relative_top_menu = rootView
                .findViewById(R.id.relative_top_menu);

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

    @SuppressLint("InflateParams")
    public void setUpView(RecipeModel recipe) {

        Util.hideView(null, lottie_anim_loading);
        if (recipe != null && !recipe.getTitle().isEmpty()) {
            Log.d(TAG, "setUpView: RECIPE = " + recipe.toString());
            this.recipe = recipe;

            ShapeableImageView recipeImg = rootView.findViewById(R.id.recipe_main_image);
            ibBookmark = rootView.findViewById(R.id.ib_bookmark);
            TextView recipeTitle = rootView.findViewById(R.id.recipe_title);
            TextView recipeDescription = rootView.findViewById(R.id.recipe_description);
            AppCompatTextView type = rootView.findViewById(R.id.tv_type);
            AppCompatTextView cockingTime = rootView.findViewById(R.id.tv_cocking_time);
            AppCompatTextView peopleAmount = rootView.findViewById(R.id.tv_people_amount);
            ImageButton shareButton = rootView.findViewById(R.id.ib_sharing);
            btn_up = rootView.findViewById(R.id.btn_up);

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
            shareButton.setOnClickListener(this);
            btn_up.setOnClickListener(this);
            main_scroll_view.getViewTreeObserver().addOnScrollChangedListener(this);

            if (tags != null && !tags.isEmpty())
                recipe.setTags(tags); // From Bundle Args

            recipeTitle.setText(recipe.getTitle());
            recipeDescription.setText(recipe.getDescription());
            recipeDescription.setOnClickListener(this);

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
                    @SuppressLint("InflateParams")
                    View view = getLayoutInflater().inflate(R.layout.item_recipe_preparation_step, null);

                    if (step.getImage() != null && !step.getImage().getUrl().isEmpty()) {
                        view = getLayoutInflater().inflate(R.layout.item_recipe_preparation_step_img, null);
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

                    ImageButton iButtonLinks = view.findViewById(R.id.ib_link_container);
                    if (step.getStepLinks() != null && !step.getStepLinks().isEmpty()) {
                        Util.showView(null, iButtonLinks);
                        view.setBackgroundResource(R.drawable.selector_white);
                        view.setClickable(true);
                        view.setFocusable(true);
                        view.setOnClickListener(v -> {
                            Util.vibrate(requireContext());
                            showLinksDialog(step.getStepLinks());
                        });
                        iButtonLinks.setOnClickListener(v -> {
                            Util.vibrate(requireContext());
                            showLinksDialog(step.getStepLinks());
                        });
                        Log.d(TAG, "setUpView: STEP = " + step.toString());
                    } else {
                        Util.hideView(null, iButtonLinks);
                    }

                    linear_preparation_container.addView(view);
                }
                Util.showView(null, linear_preparation_container);
            } else {
                Util.hideView(null, linear_preparation_container);
            }
            if (!spHelper.getScrollUpStatus()) {
                Util.hideView(null, btn_up);
            }
            Util.showView(null, relative_top_menu);
            Util.showView(null, relative_container_recipe_main_info);
            Util.hideView(null, linear_layout_empty_container);
        } else {
            Util.showView(scaleUp, linear_layout_empty_container);
            Util.hideView(null, relative_container_recipe_main_info);
            Util.hideView(null, relative_top_menu);
        }
    }

    @Override
    public void onScrollChanged() {
        if (main_scroll_view != null) {
            Log.d(TAG, "onScrollChanged: ScrollY = " + main_scroll_view.getScrollY());

            if (main_scroll_view.getScrollY() <= 0) {
                Util.showView(enter, relative_top_menu);
            } else {
                Util.hideViewInvisibleWay(exit, relative_top_menu);
            }

            if (spHelper.getScrollUpStatus()) {
                if (main_scroll_view.getScrollY() > 0) {
                    Util.showView(enter, btn_up);
                    handlerDelay.removeCallbacks(runnableHideUpBtn);
                    handlerDelay.postDelayed(runnableHideUpBtn, TimeHelper.DELAY / 2);
                } else {
                    Util.hideViewInvisibleWay(exit, btn_up);
                }
            }
        }
    }

    private void showLinksDialog(ArrayList<LinkModel> links) {
        Dialog dialog = new Dialog(requireContext(), R.style.alert_dialog);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LayoutInflater inflater = getLayoutInflater();
        @SuppressLint("InflateParams")
        View dialogView = inflater.inflate(R.layout.dialog_view_links_choice, null, false);
        ListView listView = dialogView.findViewById(R.id.list_view_links);

        LinksAdapter linksAdapter = new LinksAdapter(requireContext(), R.layout.item_step_link, links);
        listView.setAdapter(linksAdapter);
        dialog.setContentView(dialogView);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            Util.vibrate(requireContext());
            handleLink(links.get(position));
            if (dialog.isShowing())
                dialog.dismiss();
        });
        dialog.show();
        dialogView.startAnimation(scaleUp);
    }

    private void showGoToBrowserDialog(String url) {

        Handler handlerMessage = new Handler(Looper.getMainLooper());

        Runnable runnableDelayMassage = () -> {

           /* if (isAdded() && isVisible()) {
                Dialog dialog = new Dialog(requireContext(), R.style.alert_dialog);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                LayoutInflater inflater = getLayoutInflater();
                @SuppressLint("InflateParams")
                View dialogView = inflater.inflate(R.layout.dialog_view_error_loading, null,
                        false);

                TextView tv_url = dialogView.findViewById(R.id.url_link);
                Button ok = dialogView.findViewById(R.id.ok);
                Button cancel = dialogView.findViewById(R.id.cancel);

                tv_url.setText(url);

                ok.setOnClickListener(v -> {
                    Util.vibrate(requireContext());
                    goToBrowser(url);
                    if (dialog.isShowing())
                        dialog.dismiss();
                });

                cancel.setOnClickListener(v -> {
                    Util.vibrate(requireContext());
                    if (dialog.isShowing())
                        dialog.dismiss();
                });

                dialog.show();
                dialogView.startAnimation(scaleUp);
            }*/

            if (isAdded() && isVisible()) {
                String msg = getString(R.string.aks_go_to_bowser);
                Snackbar snackBar = Snackbar.make(rootView, msg, Snackbar.LENGTH_SHORT);
                snackBar.setBackgroundTint(getResources().getColor(R.color.md_green_50));
                snackBar.setTextColor(getResources().getColor(R.color.dark_gray_btn_bg_color));
                snackBar.setDuration(Snackbar.LENGTH_INDEFINITE);

                snackBar.setAction(getString(R.string.ok), v -> Util.goToBrowser(requireActivity(), url));

                if (!snackBar.isShown())
                    snackBar.show();
            }

        };

        handlerMessage.postDelayed(runnableDelayMassage, TimeHelper.DIALOG_DELAY);
    }

    private void handleLink(LinkModel link) {
        if (link.getUrl().contains(MAIN_RECIPE_SERVER))
            Util.loadSingleRecipeFragmentSelf(requireActivity(), link.getUrl(), tags);
        else {
            Util.goToBrowser(requireActivity(), link.getUrl());
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
        super.onStart();
        Log.d(TAG, "onStart: Usage = " + spHelper.getUsageOpenTime());
        spHelper.saveUsageOpenTime(System.currentTimeMillis());
        spHelper.increasesAdCounter();
        Util.hideBottomBar(requireActivity(), scaleDown);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (recipe != null) {
            Util.saveUsageByWeekDay(requireContext());
            spHelper.saveLastRecipeRead(recipe);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (videoView != null)
            videoView.release();
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
                        MessageHelper.showSuccessMessageOnMain(requireActivity(),
                                getString(R.string.saved));
                } else {
                    isBookmarkChecked = false;
                    checkBookmark(false);
                    realmHelper.deleteRecipe(realmRecipeModel);
                    if (isVisible())
                        MessageHelper.showSuccessMessageOnMain(requireActivity(),
                                getString(R.string.removed));
                }
                break;
            case R.id.recipe_description:
                showDescriptionDialog(recipe.getDescription());
                break;
            case R.id.ib_sharing:
                if (url != null && !url.isEmpty())
                    Util.share(requireActivity(), url);
                break;
            case R.id.btn_up:
                if (main_scroll_view != null) {
                    main_scroll_view.smoothScrollTo(0, 0);
                }
                break;
        }
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
    public void onLoaderFailed(String url, Exception e) {
        Log.d(TAG, "onRecipesLoadedFailed: ERROR = " + e.toString());
        if (e instanceof SocketTimeoutException) {
            showNetworkErrorMessage();
        }
        if (e instanceof SocketException) {
            showNetworkErrorMessage();
        } else {
            showErrorMessage(url);
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
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(),
                        getString(R.string.network_error), Toast.LENGTH_LONG)
                        .show();
                if (isVisible())
                    refreshView();
            });
        }
    }

    private void showErrorMessage(String url) {
        if (isVisible()) {
            requireActivity().runOnUiThread(() -> {
                MessageHelper.showInfoMessageWarning(
                        requireActivity(), getString(R.string.some_error),
                        rootView);
                showGoToBrowserDialog(url);
                if (isVisible())
                    refreshView();
            });
        } else {
            requireActivity().runOnUiThread(() -> {
                Toast.makeText(requireContext(),
                        getString(R.string.some_error), Toast.LENGTH_LONG)
                        .show();
                showGoToBrowserDialog(url);
                if (isVisible())
                    refreshView();
            });
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