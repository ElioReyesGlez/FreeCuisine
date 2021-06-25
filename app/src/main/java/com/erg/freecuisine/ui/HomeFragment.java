package com.erg.freecuisine.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlinx.coroutines.Job;

import static com.erg.freecuisine.util.Constants.LINK_KEY;
import static com.erg.freecuisine.util.Constants.RECIPE_KEY;
import static com.erg.freecuisine.util.Constants.SAVED_STATE_KEY;
import static com.erg.freecuisine.util.Constants.TAG_KEY;
import static com.erg.freecuisine.util.Constants.URL_KEY;

public class HomeFragment extends Fragment implements OnRecipeListener,
        OnFireBaseListenerDataStatus {

    private static final String TAG = "HomeFragment";

    private View rootView;
    private AsyncDataLoad asyncDataLoad;
    private FireBaseHelper fireBaseHelper;
    private List<RecipeModel> recipes;
    private RecommendedRecipesAdapter adapter;
    private RecyclerView recyclerviewRecommendRecipe;
    private LinearLayout userActivityContainer;
    private View lastReadingView, staticsGraphView;
    private SharedPreferencesHelper spHelper;
    private ViewGroup container;
    private Context mContext;
    private Bundle savedState = null;
    private Job recommendLoaderJob;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        recipes = new ArrayList<>();
        spHelper = new SharedPreferencesHelper(requireContext());

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

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerviewRecommendRecipe.setLayoutManager(layoutManager);

        if (savedState != null) {
            String recipesJson = savedState.getString(RECIPE_KEY);
            recipes = StringHelper.getRecipesFromStringJson(recipesJson);

            restoreState();
        } else {
            LoadingAdapter loadingAdapter = new LoadingAdapter(
                    getLoadingList(), requireContext(),
                    R.layout.loading_item_recommend_recipe_card);
            recyclerviewRecommendRecipe.setAdapter(loadingAdapter);

            if (recommendLoaderJob != null && recommendLoaderJob.isActive()) {
                recommendLoaderJob.cancel(recommendLoaderJob.getCancellationException());
            }
            new FireBaseHelper().getMainUrl(this);
        }
        addUserActivityViews();
        savedState = null;
    }

    private void addUserActivityViews() {
        lastReadingView = getLayoutInflater()
                .inflate(R.layout.user_activity_last_reading_view, container, false);
        staticsGraphView = getLayoutInflater()
                .inflate(R.layout.user_statics_graphic_view, container, false);

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
                    .error(R.drawable.ic_lunch_chef)
                    .placeholder(R.drawable.ic_loading_icon)
                    .into(holder.recipeMainImg);
            holder.recipeTitle.setText(recipe.getTitle());
            holder.recipeDescription.setText(recipe.getDescription());
            holder.cockingTime.setText(recipe.getTime());
            holder.peopleAmount.setText(String.valueOf(recipe.getDiners()));


            if (recipe.getTags() != null && !recipe.getTags().isEmpty()) {
                List<TagModel> tags = recipe.getTags();
                if (tags.get(0) != null) {
                    TagModel firstTag = recipe.getTags().get(0);
                    holder.firstFilter.setText(firstTag.getText());
                }

                if (tags.size() > 1 && tags.get(1) != null) {
                    TagModel secondTag = recipe.getTags().get(1);
                    holder.secondFilter.setText(secondTag.getText());
                } else {
                    holder.secondFilter.setVisibility(View.INVISIBLE);
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
    public void onMainUrlLoaded(LinkModel link) {
        Log.d(TAG, "onMainUrlLoaded: LINK = " + link.getUrl());
        recommendLoaderJob = new AsyncDataLoad()
                .loadRecommendRecipesAsync(requireActivity(), this, link);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(SAVED_STATE_KEY, (savedState != null) ? savedState : saveState());
        Log.d(TAG, "onSaveInstanceState: outState = " + outState);
    }

    @Override
    public void onRecipeClick(int position, View view) {
        Util.vibrateMin(requireContext());
        if (recipes != null && !recipes.isEmpty()) {
            loadFragment(position, view);
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
        if (view.getId() == R.id.last_reading_main_card_container) {
            RecipeModel currentRecipe = spHelper.getLastRecipeRead();
            Bundle args = new Bundle();
            args.putString(URL_KEY, currentRecipe.getLink());
            ArrayList<TagModel> tagModels = new ArrayList<>(currentRecipe.getTags());
            args.putParcelableArrayList(TAG_KEY, tagModels);

            NavController navController = Navigation
                    .findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_navigation_home_to_singleRecipeFragment, args);

        } else {
            RecipeModel currentRecipe = adapter.getRecipes().get(position);
            Bundle args = new Bundle();
            args.putString(URL_KEY, currentRecipe.getLink());
            ArrayList<TagModel> tagModels = new ArrayList<>(currentRecipe.getTags());
            args.putParcelableArrayList(TAG_KEY, tagModels);

            NavController navController = Navigation
                    .findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_navigation_home_to_singleRecipeFragment, args);
        }
    }

    @Override
    public void onSingleRecipeLoaded(RecipeModel recipe) {
        //Empty
    }

    @Override
    public void onRecipesLoaded(ArrayList<RecipeModel> recipes) {
        this.recipes = recipes;
        if (mContext != null && isVisible() && !this.recipes.isEmpty()) {
            adapter = new RecommendedRecipesAdapter(
                    this.recipes, requireContext(), this);
            recyclerviewRecommendRecipe.setAdapter(adapter);
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