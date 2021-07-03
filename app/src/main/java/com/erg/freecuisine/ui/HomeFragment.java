package com.erg.freecuisine.ui;

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
import com.erg.freecuisine.controller.network.AsyncDataLoad;
import com.erg.freecuisine.controller.network.helpers.FireBaseHelper;
import com.erg.freecuisine.controller.network.helpers.MessageHelper;
import com.erg.freecuisine.controller.network.helpers.RealmHelper;
import com.erg.freecuisine.controller.network.helpers.SharedPreferencesHelper;
import com.erg.freecuisine.controller.network.helpers.StringHelper;
import com.erg.freecuisine.controller.network.helpers.TimeHelper;
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

import static com.erg.freecuisine.util.Constants.CONSEJOS_DE_COSINA_TAG;
import static com.erg.freecuisine.util.Constants.MAIN_RECETA_GRATIS_TAG;
import static com.erg.freecuisine.util.Constants.RECIPE_KEY;
import static com.erg.freecuisine.util.Constants.SAVED_STATE_KEY;
import static com.erg.freecuisine.util.Constants.TAG_KEY;
import static com.erg.freecuisine.util.Constants.URL_KEY;

public class HomeFragment extends Fragment implements OnRecipeListener,
        OnFireBaseListenerDataStatus, View.OnClickListener {

    private static final String TAG = "HomeFragment";

    private View rootView;
    private List<RecipeModel> recipes;
    private RecommendedRecipesAdapter adapter;
    private RecyclerView recyclerviewRecommendRecipe;
    private LinearLayout userActivityContainer;
    private LinearLayout linearEmptyContainer;
    private View lastReadingView, staticsGraphView;
    private SharedPreferencesHelper spHelper;
    private Context mContext;
    private Bundle savedState = null;
    private Job recommendLoaderJob;
    private Job tipsLoaderJob;
    private FireBaseHelper fireBaseHelper;
    private Animation scaleUP, scaleDown;
    private Handler handlerMessage;
    private Runnable runnableDelayMassage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        recipes = new ArrayList<>();
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
                refreshView();
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
        userActivityContainer = rootView.findViewById(R.id.ll_activity_history_container);
        linearEmptyContainer = rootView.findViewById(R.id.linear_layout_empty_container);
        linearEmptyContainer.setOnClickListener(this);

        scaleUP = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerviewRecommendRecipe.setLayoutManager(layoutManager);
        addUserActivityViews();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (savedState != null) {
            String recipesJson = savedState.getString(RECIPE_KEY);
            recipes = StringHelper.getRecipesFromStringJson(recipesJson);

            restoreState();
        } else {
            LoadingAdapter loadingAdapter = new LoadingAdapter(
                    getLoadingList(), requireContext(),
                    R.layout.loading_item_recommend_recipe_card);
            recyclerviewRecommendRecipe.setAdapter(loadingAdapter);

            if (Util.isNetworkAvailable(requireActivity())) {

                fireBaseHelper.init(this);
            } else {
                MessageHelper.showInfoMessageWarning(
                        requireActivity(),
                        getString(R.string.network_error),
                        rootView);
                stopLoading();
                refreshView();
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
            holder.cockingTime.setText(recipe.getTime());

            RelativeLayout typeContainer = lastReadingView
                    .findViewById(R.id.relative_recipes_type_container);
            if (!recipe.getType().isEmpty()) {
                Util.showView(null, typeContainer);
                holder.type.setText(recipe.getType());
            } else {
                Util.hideView(null, typeContainer);
            }

            if (recipe.getTags() != null && !recipe.getTags().isEmpty()) {
                List<TagModel> tags = recipe.getTags();
                if (tags.get(0) != null) {
                    TagModel firstTag = recipe.getTags().get(0);
                    holder.firstFilter.setText(firstTag.getText());
                }
            }
        } else {
            Util.hideView(null, lastReadingView);
        }
    }

    private void setStatics() {

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
                if (link.getTag().toLowerCase().equalsIgnoreCase(MAIN_RECETA_GRATIS_TAG.toLowerCase())) {
                    recommendLoaderJob = new AsyncDataLoad()
                            .loadRecommendRecipesAsync(requireActivity(), this, link);
                }
                if (link.getTag().toLowerCase().equalsIgnoreCase(CONSEJOS_DE_COSINA_TAG.toLowerCase())) {
                    tipsLoaderJob = new AsyncDataLoad()
                            .loadTipRecipesAsync(requireActivity(), this, link);
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

    @Override
    public void onRecipeClick(int position, View view) {
        Util.vibrate(requireContext());
        if (view.getId() == R.id.last_reading_main_card_container) {
            loadFragment(position, view);
        } else if (recipes != null && !recipes.isEmpty()) {
            loadFragment(position, view);
        }
    }

    @Override
    public void onClick(View v) {
        Util.vibrate(requireContext());
        if (v.getId() == R.id.linear_layout_empty_container) {
            Util.refreshCurrentFragment(requireActivity());
        }
    }


    private List<RecipeModel> getLoadingList() {
        ArrayList<RecipeModel> aux = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            aux.add(new RecipeModel());
        }
        return aux;
    }

    private void loadFragment(int position, View view) {

        NavController navController = Navigation
                .findNavController(requireActivity(), R.id.nav_host_fragment);

        Bundle args;
        if (view.getId() == R.id.last_reading_main_card_container) {
            args = new Bundle();
            RecipeModel lastRecipeRead = spHelper.getLastRecipeRead();
            args.putString(URL_KEY, lastRecipeRead.getUrl());
            ArrayList<TagModel> tagModels = new ArrayList<>(lastRecipeRead.getTags());
            args.putParcelableArrayList(TAG_KEY, tagModels);
        } else {
            args = new Bundle();
            RecipeModel currentRecipe = adapter.getRecipes().get(position);
            args.putString(URL_KEY, currentRecipe.getUrl());
            ArrayList<TagModel> tagModels = new ArrayList<>(currentRecipe.getTags());
            args.putParcelableArrayList(TAG_KEY, tagModels);
        }

        navController.navigate(R.id.action_navigation_home_to_singleRecipeFragment, args);

    }

    @Override
    public void onSingleRecipeLoaded(RecipeModel recipe) {
        //Empty
    }

    @Override
    public void onRecipesLoaded(ArrayList<RecipeModel> recipes) {
        this.recipes = recipes;
        if (mContext != null && isVisible()) {
            if (!this.recipes.isEmpty()) {
                adapter = new RecommendedRecipesAdapter(
                        this.recipes, requireContext(), this);
                recyclerviewRecommendRecipe.setAdapter(adapter);
            }
            refreshView();
        }
    }

    @Override
    public void onLoaderFailed(ArrayList<RecipeModel> recipes, Exception e) {
        Log.d(TAG, "onRecipesLoadedFailed: ERROR = " + e.toString());
        if (e instanceof SocketTimeoutException) {
            showTimeOutMessage();
        } else {
            showErrorMessage();
        }
    }

    private void showTimeOutMessage() {
        if (isVisible()) {
            requireActivity().runOnUiThread(() -> {
                MessageHelper.showInfoMessageError(
                        requireActivity(), getString(R.string.network_error),
                        rootView);
                refreshView();
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
                refreshView();
            });
        } else {
            Toast.makeText(requireContext(),
                    getString(R.string.some_error), Toast.LENGTH_LONG).show();
        }
    }

    private void refreshView() {
        if (recipes != null && !recipes.isEmpty()) {
            Util.showView(null, recyclerviewRecommendRecipe);
            Util.hideView(scaleDown, linearEmptyContainer);
        } else {
            Util.hideView(null, recyclerviewRecommendRecipe);
            Util.showView(scaleUP, linearEmptyContainer);
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
        if (recipes != null && !recipes.isEmpty()) {
            String recipesJson = new Gson().toJson(recipes);
            state.putString(RECIPE_KEY, recipesJson);
        }
        return state;
    }

    private void restoreState() {
        if (recipes != null && !recipes.isEmpty()) {
            adapter = new RecommendedRecipesAdapter(
                    recipes, requireContext(), this);
            recyclerviewRecommendRecipe.swapAdapter(adapter, true);
        }

        refreshView();
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