package com.erg.freecuisine.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.erg.freecuisine.R;
import com.erg.freecuisine.adapters.LoadingAdapter;
import com.erg.freecuisine.adapters.RecipesAdapter;
import com.erg.freecuisine.adapters.RecommendedRecipesAdapter;
import com.erg.freecuisine.adapters.TipsRecipesAdapter;
import com.erg.freecuisine.controller.network.AsyncDataLoad;
import com.erg.freecuisine.helpers.FireBaseHelper;
import com.erg.freecuisine.helpers.MessageHelper;
import com.erg.freecuisine.helpers.SharedPreferencesHelper;
import com.erg.freecuisine.helpers.StringHelper;
import com.erg.freecuisine.helpers.TimeHelper;
import com.erg.freecuisine.interfaces.OnFireBaseListenerDataStatus;
import com.erg.freecuisine.interfaces.OnRecipeListener;
import com.erg.freecuisine.models.LinkModel;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.TagModel;
import com.erg.freecuisine.util.Util;
import com.erg.freecuisine.views.CustomLineView;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;

import kotlinx.coroutines.Job;

import static com.erg.freecuisine.util.Constants.CONSEJOS_TAG;
import static com.erg.freecuisine.util.Constants.MAIN_RECETA_GRATIS_TAG;
import static com.erg.freecuisine.util.Constants.RECOMMENDED_RECIPE_KEY;
import static com.erg.freecuisine.util.Constants.SAVED_STATE_KEY;
import static com.erg.freecuisine.util.Constants.TAG_KEY;
import static com.erg.freecuisine.util.Constants.TIPS_RECIPE_KEY;
import static com.erg.freecuisine.util.Constants.URL_KEY;

public class HomeFragment extends Fragment implements OnRecipeListener,
        OnFireBaseListenerDataStatus, View.OnClickListener {

    private static final String TAG = "HomeFragment";

    private View rootView;
    private List<RecipeModel> recommendedRecipes;
    private List<RecipeModel> tipsRecipes;
    private RecommendedRecipesAdapter recommendedRecipesAdapter;
    private TipsRecipesAdapter tipsRecipesAdapter;
    private RecyclerView recyclerviewRecommendRecipe;
    private RecyclerView recyclerviewTipsRecipe;
    private LinearLayout userActivityContainer;
    private LinearLayout linearRecommendedEmptyContainer;
    private LinearLayout linearTipsEmptyContainer;
    private View lastReadingView, staticsGraphView;
    private SharedPreferencesHelper spHelper;
    private Context mContext;
    private Bundle savedState = null;
    private Job recommendLoaderJob;
    private Job tipsLoaderJob;
    private FireBaseHelper fireBaseHelper;
    private Animation scaleUP, scaleDown, scaleUpLong;
    private Handler handlerMessage;
    private Runnable runnableDelayMassage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        spHelper = new SharedPreferencesHelper(requireContext());
        handlerMessage = new Handler(Looper.myLooper());
        fireBaseHelper = new FireBaseHelper();

        runnableDelayMassage = () -> {
            if (isAdded() && isVisible()) {
                MessageHelper.showInfoMessageWarning(
                        requireActivity(),
                        getString(R.string.network_error),
                        rootView);
                stopLoading();
                refreshViewRecommendedView();
                refreshViewTipsView();
            }
        };

        if (savedInstanceState != null && savedState == null) {
            savedState = savedInstanceState.getBundle(SAVED_STATE_KEY);
            Log.d(TAG, "onCreate: SAVED INSTANCE = " + savedState);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        setUpView();
        return rootView;
    }

    private void setUpView() {
        recyclerviewRecommendRecipe = rootView.findViewById(R.id.recyclerviewRecommendRecipe);
        recyclerviewTipsRecipe = rootView.findViewById(R.id.recyclerviewTipsRecipe);
        userActivityContainer = rootView.findViewById(R.id.ll_activity_history_container);
        linearRecommendedEmptyContainer = rootView.findViewById(R.id.linear_layout_empty_container);
        linearTipsEmptyContainer = rootView.findViewById(R.id.linear_layout_empty_container_tips);
        linearRecommendedEmptyContainer.setOnClickListener(this);
        linearTipsEmptyContainer.setOnClickListener(this);

        scaleUP = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up);
        scaleUpLong = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up_long);
        scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);

        LinearLayoutManager layoutManagerRecommended = new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager layoutManagerTips = new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerviewRecommendRecipe.setLayoutManager(layoutManagerRecommended);
        recyclerviewTipsRecipe.setLayoutManager(layoutManagerTips);
        addUserActivityViews();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedState != null) {
            String recommendedRecipesJson = savedState.getString(RECOMMENDED_RECIPE_KEY);
            String tipsRecipesJson = savedState.getString(TIPS_RECIPE_KEY);

            recommendedRecipes = StringHelper.getRecipesFromStringJson(recommendedRecipesJson);
            tipsRecipes = StringHelper.getRecipesFromStringJson(tipsRecipesJson);

            restoreState();
        } else {
            LoadingAdapter loadingAdapter = new LoadingAdapter(
                    getLoadingList(), requireContext(),
                    R.layout.loading_item_recommend_recipe_card);
            recyclerviewRecommendRecipe.setAdapter(loadingAdapter);
            recyclerviewTipsRecipe.setAdapter(loadingAdapter);

            if (Util.isNetworkAvailable(requireActivity())) {
                fireBaseHelper.init(this);
            } else {
                MessageHelper.showInfoMessageWarning(
                        requireActivity(),
                        getString(R.string.network_error),
                        rootView);
                stopLoading();
                refreshViewRecommendedView();
                refreshViewTipsView();
            }
        }
        savedState = null;
    }

    private void stopLoading() {
        if (recommendLoaderJob != null && recommendLoaderJob.isActive()) {
            recommendLoaderJob.cancel(new CancellationException());
        }
        if (tipsLoaderJob != null && tipsLoaderJob.isActive()) {
            tipsLoaderJob.cancel(new CancellationException());
        }
    }

    private void addUserActivityViews() {
        lastReadingView = getLayoutInflater()
                .inflate(R.layout.user_activity_last_reading_view, null, false);
        staticsGraphView = getLayoutInflater()
                .inflate(R.layout.user_statics_graphic_view, null, false);

        setLastReading();
        setStatics();

        userActivityContainer.addView(lastReadingView);
        userActivityContainer.addView(staticsGraphView);
    }

    private void setLastReading() {

        RecipesAdapter.ViewHolder holder = new RecipesAdapter.ViewHolder(lastReadingView, this);
        RecipeModel recipe = spHelper.getLastRecipeRead();

        if (recipe != null) {
            Picasso.get()
                    .load(recipe.getImage().getUrl())
                    .error(R.drawable.ic_lunch_chef_mini)
                    .placeholder(R.drawable.ic_loading_icon)
                    .into(holder.recipeMainImg);
            holder.recipeTitle.setText(recipe.getTitle());
            holder.recipeDescription.setText(recipe.getDescription());

            if (recipe.getTags() != null && !recipe.getTags().isEmpty()) {
                List<TagModel> tags = recipe.getTags();
                if (tags.get(0) != null) {
                    TagModel firstTag = recipe.getTags().get(0);
                    holder.firstFilter.setText(firstTag.getText());
                } else {
                    Util.hideView(null, holder.firstFilter);
                }
            } else {
                Util.hideView(null, holder.firstFilter);
            }

            if (!recipe.getTime().isEmpty())
                holder.cockingTime.setText(recipe.getTime());
            else
                Util.hideView(null, holder.cockingTime);


            RelativeLayout typeContainer = lastReadingView
                    .findViewById(R.id.relative_recipes_type_container);
            if (!recipe.getType().isEmpty()) {
                Util.showView(null, typeContainer);
                holder.type.setText(recipe.getType());
            } else {
                Util.hideView(null, typeContainer);
            }

            if (!recipe.getDiners().isEmpty())
                holder.peopleAmount.setText(recipe.getDiners());
            else
                Util.hideView(null, holder.peopleAmount);

            ImageButton info = lastReadingView.findViewById(R.id.ib_info__last_reading);
            info.setOnClickListener(this);

            new Handler(Looper.getMainLooper()).postDelayed((Runnable) () -> {
                if (isVisible() && info.getVisibility() == View.VISIBLE) {
                    info.startAnimation(scaleUpLong);
                }
            }, TimeHelper.DELAY);

            LinearLayout linear_amounts_container = lastReadingView
                    .findViewById(R.id.linear_amounts_container);
            if (recipe.getDiners().isEmpty() && recipe.getType().isEmpty()
                    && recipe.getTime().isEmpty()) {
                Util.hideView(null, linear_amounts_container);
            } else {
                Util.showView(null, linear_amounts_container);
            }

        } else {
            Util.hideView(null, lastReadingView);
        }
    }

    private void setStatics() {

        ImageButton info = staticsGraphView.findViewById(R.id.ib_info__statics);
        info.setOnClickListener(this);
//        if (spHelper.isFirstLunch())
        new Handler(Looper.getMainLooper()).postDelayed((Runnable) () -> {
            if (isVisible() && info.getVisibility() == View.VISIBLE) {
                info.startAnimation(scaleUpLong);
            }
        }, TimeHelper.DELAY);

        ArrayList<Float> userActivity = spHelper.getUserActivity();
        ArrayList<String> weekDays = TimeHelper.getCurrentWeekDays();
        int[] colors = new int[]{
                getResources().getColor(R.color.red_default),
        };

        Log.d(TAG, " weekDays: " + weekDays.toString());
        Log.d(TAG, " userActivity: " + userActivity.toString());

        CustomLineView customLineView = staticsGraphView.findViewById(R.id.custom_line_view);
        customLineView.setDrawDotLine(false);
        customLineView.setShowPopup(CustomLineView.SHOW_POPUPS_MAX_MIN_ONLY);
        customLineView.setColorArray(colors);

        customLineView.setBottomTextList(weekDays);
        ArrayList<ArrayList<Float>> dataList = new ArrayList<>(Collections.singleton(userActivity));
        customLineView.setFloatDataList(dataList);
    }

    @Override
    public void onConnectionListener(boolean isConnected) {
        Log.d(TAG, "onConnectionListener: CONNECTED = " + isConnected);
        if (isConnected) {
            fireBaseHelper.getMainUrls(this);
        }

        showDelayDisconnectedMessage(isConnected);
        stopLoading();
    }

    private void showDelayDisconnectedMessage(boolean isConnected) {
        Log.d(TAG, "showDelayDisconnectedMessage: CONNECTED = " + isConnected);

        if (isConnected) {
            Log.d(TAG, "removeCallbacks");
            handlerMessage.removeCallbacks(runnableDelayMassage);
            return;
        }

        handlerMessage.postDelayed(runnableDelayMassage, TimeHelper.TIME_OUT / 2);
    }

    @Override
    public void onMainUrlsLoaded(List<LinkModel> links, List<String> keys) {
        Log.d(TAG, "onMainUrlLoaded: LINK = " + links.toString());
        stopLoading();
        if (isAdded()) {
            for (LinkModel link : links) {
                if (link.getTag().toLowerCase().contains(MAIN_RECETA_GRATIS_TAG)) {
                    recommendLoaderJob = new AsyncDataLoad()
                            .loadRecommendRecipesAsync(requireActivity(), this, link);
                }

                if (link.getTag().toLowerCase().contains(CONSEJOS_TAG)) {
                    tipsLoaderJob = new AsyncDataLoad()
                            .loadTipsRecipesAsync(requireActivity(), this, link);
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(SAVED_STATE_KEY, (savedState != null) ? savedState : saveState());
        Log.d(TAG, "onSaveInstanceState: outState = " + outState);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onRecipeClick(int position, View view) {
        Util.vibrate(requireContext());
        switch (view.getId()) {
            case R.id.last_reading_main_card_container:
                RecipeModel lastRecipeRead = spHelper.getLastRecipeRead();
                loadFragment(lastRecipeRead, view);
                break;
            case R.id.recommended_main_card_container:
                if (recommendedRecipes != null && !recommendedRecipes.isEmpty()) {
                    RecipeModel recipe = recommendedRecipesAdapter.getRecipes().get(position);
                    loadFragment(recipe, view);
                }
                break;
            case R.id.tips_main_card_container:
                if (tipsRecipes != null && !tipsRecipes.isEmpty()) {
                    RecipeModel recipe = tipsRecipesAdapter.getRecipes().get(position);
                    loadFragment(recipe, view);
                }
                break;
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        Util.vibrate(requireContext());
        switch (v.getId()) {
            case R.id.linear_layout_empty_container:
            case R.id.linear_layout_empty_container_tips:
                Util.refreshCurrentFragment(requireActivity());
                break;
            case R.id.ib_info__statics:
                if (isVisible())
                    MessageHelper.showInfoMessage(requireActivity(),
                            getString(R.string.statistics_info), rootView);
                break;
            case R.id.ib_info__last_reading:
                if (isVisible())
                    MessageHelper.showInfoMessage(requireActivity(),
                            getString(R.string.last_reading_info), rootView);
                break;
        }
    }


    private List<RecipeModel> getLoadingList() {
        ArrayList<RecipeModel> aux = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            aux.add(new RecipeModel());
        }
        return aux;
    }

    private void loadFragment(RecipeModel currentRecipe, View view) {

        NavController navController = Navigation
                .findNavController(requireActivity(), R.id.nav_host_fragment);

        Bundle args = new Bundle();
        args.putString(URL_KEY, currentRecipe.getUrl());
        ArrayList<TagModel> tagModels = new ArrayList<>(currentRecipe.getTags());
        args.putParcelableArrayList(TAG_KEY, tagModels);

        navController.navigate(R.id.action_navigation_home_to_singleRecipeFragment, args);

    }

    @Override
    public void onSingleRecipeLoaded(RecipeModel recipe) {
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
            showTimeOutMessage();
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onRecommendedRecipesLoaded(ArrayList<RecipeModel> recipes) {
        recommendedRecipes = recipes;
        if (mContext != null && isVisible()) {
            if (!recommendedRecipes.isEmpty()) {
                recommendedRecipesAdapter = new RecommendedRecipesAdapter(
                        recommendedRecipes, requireContext(), this);
                recyclerviewRecommendRecipe.setAdapter(recommendedRecipesAdapter);
            }
            refreshViewRecommendedView();
        }
    }

    @Override
    public void onTipsRecipesLoaded(ArrayList<RecipeModel> recipes) {
        tipsRecipes = recipes;
        if (mContext != null && isVisible()) {
            if (!tipsRecipes.isEmpty()) {
                tipsRecipesAdapter = new TipsRecipesAdapter(
                        tipsRecipes, requireContext(), this);
                recyclerviewTipsRecipe.setAdapter(tipsRecipesAdapter);
            }

            refreshViewTipsView();
        }
    }

    private void showTimeOutMessage() {
        if (isVisible()) {
            requireActivity().runOnUiThread(() -> {
                MessageHelper.showInfoMessageError(
                        requireActivity(), getString(R.string.network_error),
                        rootView);
            });
        } else {
            Toast.makeText(requireContext(),
                    getString(R.string.some_error), Toast.LENGTH_LONG).show();
        }
    }

    private void showErrorMessage() {
        if (isVisible()) {
            requireActivity().runOnUiThread(() -> {
                MessageHelper.showInfoMessageError(
                        requireActivity(), getString(R.string.some_error),
                        rootView);
            });
        } else {
            Toast.makeText(requireContext(),
                    getString(R.string.some_error), Toast.LENGTH_LONG).show();
        }
    }

    private void refreshViewRecommendedView() {

        if (recommendedRecipes != null && !recommendedRecipes.isEmpty()) {
            Util.showView(null, recyclerviewRecommendRecipe);
            Util.hideView(null, linearRecommendedEmptyContainer);
        } else {
            Util.hideView(null, recyclerviewRecommendRecipe);
            Util.showView(scaleUP, linearRecommendedEmptyContainer);
        }

    }

    private void refreshViewTipsView() {
        if (tipsRecipes != null && !tipsRecipes.isEmpty()) {
            Util.showView(null, recyclerviewTipsRecipe);
            Util.hideView(null, linearTipsEmptyContainer);
        } else {
            Util.hideView(null, recyclerviewTipsRecipe);
            Util.showView(scaleUP, linearTipsEmptyContainer);
        }
    }


    @Override
    public void onLinksLoaded(List<LinkModel> links, List<String> keys) {
        //Empty
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        savedState = saveState();
        Log.d(TAG, "onDestroyView: savedState = " + savedState);
    }

    private Bundle saveState() {
        Bundle state = new Bundle();
        if (recommendedRecipes != null && !recommendedRecipes.isEmpty()) {
            String recipesJson = new Gson().toJson(recommendedRecipes);
            state.putString(RECOMMENDED_RECIPE_KEY, recipesJson);
        }
        if (tipsRecipes != null && !tipsRecipes.isEmpty()) {
            String recipesJson = new Gson().toJson(tipsRecipes);
            state.putString(TIPS_RECIPE_KEY, recipesJson);
        }
        return state;
    }

    private void restoreState() {
        if (recommendedRecipes != null && !recommendedRecipes.isEmpty()) {
            recommendedRecipesAdapter = new RecommendedRecipesAdapter(
                    recommendedRecipes, requireContext(), this);
            recyclerviewRecommendRecipe.swapAdapter(recommendedRecipesAdapter, true);

        }

        if (tipsRecipes != null && !tipsRecipes.isEmpty()) {
            tipsRecipesAdapter = new TipsRecipesAdapter(
                    tipsRecipes, requireContext(), this);
            recyclerviewTipsRecipe.swapAdapter(tipsRecipesAdapter, true);
        }
        refreshViewTipsView();
        refreshViewRecommendedView();
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(requireContext());
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }
}