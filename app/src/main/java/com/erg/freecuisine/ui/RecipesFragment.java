
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.erg.freecuisine.R;
import com.erg.freecuisine.adapters.LoadingAdapter;
import com.erg.freecuisine.adapters.RecipesAdapter;
import com.erg.freecuisine.adapters.RecipesFilterAdapter;
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
import com.erg.freecuisine.util.Constants;
import com.erg.freecuisine.util.Util;
import com.erg.freecuisine.views.GridItemDecoration;
import com.google.gson.Gson;
import com.yalantis.filter.listener.FilterListener;
import com.yalantis.filter.widget.Filter;

import org.jetbrains.annotations.NotNull;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;

import kotlinx.coroutines.Job;

import static com.erg.freecuisine.util.Constants.FILTER_ID;
import static com.erg.freecuisine.util.Constants.LINK_KEY;
import static com.erg.freecuisine.util.Constants.QUERY_KEY;
import static com.erg.freecuisine.util.Constants.RECIPE_KEY;
import static com.erg.freecuisine.util.Constants.SAVED_STATE_KEY;
import static com.erg.freecuisine.util.Constants.TAG_KEY;

public class RecipesFragment extends Fragment implements
        FilterListener<TagModel>, OnRecipeListener, OnFireBaseListenerDataStatus,
        SearchView.OnQueryTextListener, SearchView.OnCloseListener, View.OnClickListener {

    private static final String TAG = "RecipesFragment";

    private View rootView;
    private RecyclerView recyclerViewRecipes;
    private SearchView searcher;
    private CoordinatorLayout filter_container;
    private Filter<TagModel> filter;
    private ImageButton btn_up;
    private Animation scaleUp, scaleDown, enterAnim, exitAnim;

    private List<RecipeModel> recipes;
    private List<TagModel> tags;
    private ArrayList<TagModel> tagsSelected;
    private List<LinkModel> links;
    private int[] colors;

    private AsyncDataLoad asyncDataLoad;
    private RecipesAdapter recipesAdapter;
    private Job recipesLoaderJob;
    private Context mContext;
    private Bundle savedState = null;
    private SharedPreferencesHelper spHelper;
    private LinearLayout linearEmptyContainer;
    private String currentSearchQuery = "";
    private FireBaseHelper fireBaseHelper;
    private Handler handlerMessage;
    private Handler handlerDelay;
    private Runnable runnableDelayMassage, runnableHideUpBtn;
    private RecyclerView.SmoothScroller smoothScroller;
    private StaggeredGridLayoutManager gridLayoutManager;
    private GridItemDecoration gridItemDecoration;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: savedInstanceState = " + savedInstanceState);

        fireBaseHelper = new FireBaseHelper();
        recipes = new ArrayList<>();
        tags = new ArrayList<>();
        tagsSelected = new ArrayList<>();
        asyncDataLoad = new AsyncDataLoad();
        spHelper = new SharedPreferencesHelper(requireContext());
        colors = getResources().getIntArray(R.array.colors);

        handlerMessage = new Handler(Looper.myLooper());
        handlerDelay = new Handler(Looper.getMainLooper());

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

        runnableHideUpBtn = () -> {
            if (isAdded() && isVisible()) {
                Util.hideView(exitAnim, btn_up);
            }
        };

        if (savedInstanceState != null && savedState == null) {
            savedState = savedInstanceState.getBundle(SAVED_STATE_KEY);
            Log.d(TAG, "onCreate: SAVED INSTANCE = " + savedState);
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: savedInstanceState = " + savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_recipes, container, false);
        setUpView();
        return rootView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(SAVED_STATE_KEY, (savedState != null) ? savedState : saveState());
        Log.d(TAG, "onSaveInstanceState: outState = " + outState);
    }

    public void setUpView() {
        Log.d(TAG, "setUpView: ");
        filter_container = rootView.findViewById(R.id.container_for_filter);
        if (filter_container.findViewById(FILTER_ID) == null) {
            filter = new Filter<>(rootView.getContext());
            filter.setId(Constants.FILTER_ID);
        }
        btn_up = rootView.findViewById(R.id.btn_up);
        searcher = rootView.findViewById(R.id.searcher);
        recyclerViewRecipes = rootView.findViewById(R.id.recycler_view_recipes);
        linearEmptyContainer = rootView.findViewById(R.id.linear_layout_empty_container);
        scaleUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);
        enterAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.custom_enter_anim);
        exitAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.custom_exit_anim);

        searcher.setSubmitButtonEnabled(true);
        searcher.setOnQueryTextListener(this);
        searcher.setOnCloseListener(this);
        linearEmptyContainer.setOnClickListener(this);
        btn_up.setOnClickListener(this);
//        searcher.setOnSearchClickListener(this);

//        setUpRecyclerView();

        if (savedState != null) {
            String recipesJson = savedState.getString(RECIPE_KEY);
            recipes = StringHelper.getRecipesFromStringJson(recipesJson);

            String linksJson = savedState.getString(LINK_KEY);
            links = StringHelper.getLinksFromStringJson(linksJson);

            String tagsSelectedJson = savedState.getString(TAG_KEY);
            tagsSelected = StringHelper.getSelectedTagsFromStringJson(tagsSelectedJson);

            currentSearchQuery = savedState.getString(QUERY_KEY, "");

            restoreState();

        } else {

            if (Util.isNetworkAvailable(requireActivity())) {

                LoadingAdapter loadingAdapter = new LoadingAdapter(
                        getLoadingList(), requireContext(),
                        R.layout.loading_item_recipe_card);
                setUpRecyclerView();
                recyclerViewRecipes.setAdapter(loadingAdapter);

                fireBaseHelper.init(this);
            } else {
                Log.d(TAG, "Not Internet");
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

    private List<RecipeModel> getLoadingList() {
        ArrayList<RecipeModel> aux = new ArrayList<>();
        for (int i = 0; i < 14; i++) {
            aux.add(new RecipeModel());
        }
        return aux;
    }

    @Override
    public void onConnectionListener(boolean isConnected) {
        Log.d(TAG, "onConnectionListener: CONNECTED = " + isConnected);
        if (isConnected) {
            fireBaseHelper.getLinks(this);
        }

        showDelayDisconnectedMessage(isConnected);
    }

    private void showDelayDisconnectedMessage(boolean isConnected) {
        Log.d(TAG, "showDelayDisconnectedMessage: CONNECTED = " + isConnected);

        if (isConnected) {
            Log.d(TAG, "removeCallbacks: ");
            handlerMessage.removeCallbacks(runnableDelayMassage);
            return;
        }

        handlerMessage.postDelayed(runnableDelayMassage, TimeHelper.TIME_OUT / 2);
    }

    @Override
    public void onLinksLoaded(List<LinkModel> links, List<String> keys) {
        Log.d(TAG, "onLinksLoaded: LINKS = " + links.toString());
        this.links = links;
        stopLoading();
        if (isAdded()) {
            recipesLoaderJob = asyncDataLoad.loadRecipesAsync(
                    requireActivity(), this, links);
        }
    }

    @Override
    public void onMainUrlsLoaded(List<LinkModel> links, List<String> keys) {
        /*Empty*/
    }

    @Override
    public void onRecipesLoaded(ArrayList<RecipeModel> recipes) {
        Log.d(TAG, "onRecipesLoaded: Recipes = " + recipes.toString());
        this.recipes = recipes;
        if (mContext != null && isVisible()) {

            if (!this.recipes.isEmpty()) {
                if (spHelper.getShuffleStatus()) {
                    Collections.shuffle(this.recipes);
                }
                setUpRecyclerView();
                recipesAdapter = new RecipesAdapter(this.recipes, requireContext(), this);
                recyclerViewRecipes.setAdapter(recipesAdapter);
                setUpFilterView(links);
            }
            refreshView();
        }
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
        /*Empty*/
    }

    @Override
    public void onTipsRecipesLoaded(ArrayList<RecipeModel> recipes) {
        /*Empty*/
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


    private void setUpRecyclerView() {

        int numberOfColumns = 2;
        gridLayoutManager = new StaggeredGridLayoutManager(numberOfColumns,
                StaggeredGridLayoutManager.VERTICAL);
        recyclerViewRecipes.setLayoutManager(gridLayoutManager);
        recyclerViewRecipes.setHasFixedSize(true);
        if (recyclerViewRecipes.getItemDecorationCount() > 0 &&
                gridItemDecoration != null) {
            recyclerViewRecipes.removeItemDecoration(gridItemDecoration);
        }
        gridItemDecoration = new GridItemDecoration(6, 6);
        recyclerViewRecipes.addItemDecoration(gridItemDecoration);

        if (spHelper.getScrollUpStatus()) {
            setUpScrollFlow();
        } else {
            Util.hideView(null, btn_up);
        }
    }

    private void setUpScrollFlow() {

        smoothScroller = new LinearSmoothScroller(requireContext()) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }
        };

        recyclerViewRecipes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Log.d(TAG, "onScrollStateChanged: STATE = " + newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE
                        && recyclerView.canScrollVertically(1)
                        && recyclerView.canScrollVertically(-1)) {
                    Util.showView(enterAnim, btn_up);
                    handlerDelay.removeCallbacks(runnableHideUpBtn);
                    handlerDelay.postDelayed(runnableHideUpBtn, TimeHelper.DELAY / 2);
                } else {
                    Util.hideView(exitAnim, btn_up);
                }
            }
        });

    }

    private void setUpFilterView(List<LinkModel> links) {
        if (links != null && !links.isEmpty()) {
            tags = getTags(links);
            RecipesFilterAdapter filterAdapter = new RecipesFilterAdapter(tags,
                    colors, requireActivity());

            CoordinatorLayout.LayoutParams params = new CoordinatorLayout.LayoutParams(
                    CoordinatorLayout.LayoutParams.MATCH_PARENT, CoordinatorLayout.LayoutParams.WRAP_CONTENT);
            filter.setLayoutParams(params);
            filter.setCollapsedBackground(getResources().getColor(R.color.custom_white_text_color));
            filter.setExpandedBackground(getResources().getColor(R.color.custom_white_text_color));
            filter.setAdapter(filterAdapter);
            filter.setListener(this);

            //the text to show when there's no selected items
            filter.setNoSelectedItemText(getString(R.string.str_all_selected));
            filter.build();
            filter_container.removeAllViews();
            filter_container.addView(filter);
            Util.showView(scaleUp, filter_container);
        }
    }

    public void refreshView() {
        if (recipes != null && !recipes.isEmpty()) {
            Util.hideView(scaleDown, linearEmptyContainer);
            Util.showView(null, recyclerViewRecipes);
            Util.showView(null, filter);
            Util.showView(null, searcher);
        } else {
            Util.showView(scaleUp, linearEmptyContainer);
            Util.hideView(null, recyclerViewRecipes);
            Util.hideView(null, filter);
            Util.hideView(null, searcher);
        }
    }

    private List<TagModel> getTags(List<LinkModel> links) {

        List<TagModel> tags = new ArrayList<>();
        //All categories as first item
        tags.add(new TagModel(getString(R.string.str_all_selected), colors[0]));
        // The rest
        for (int i = 0; i < links.size(); ++i) {
            String tag = links.get(i).getTag();
            tags.add(new TagModel(tag, colors[i]));
        }
        return tags;
    }

    @Override
    public void onFilterDeselected(TagModel tagModel) {
    }

    @Override
    public void onFilterSelected(TagModel tagModel) {
        Log.d(TAG, "onFilterSelected: ");
        if (!tags.isEmpty() && filter != null)
            if (tagModel.getText().contains(tags.get(0).getText())) {
                filter.deselectAll();
                filter.collapse();
                tagsSelected.clear();
            }
    }

    @Override
    public void onNothingSelected() {
        Log.d(TAG, "onNothingSelected: ");
        if (tagsSelected != null)
            tagsSelected.clear();
        if (recyclerViewRecipes != null && !recipes.isEmpty()) {
            if (!currentSearchQuery.isEmpty()) {
                searcher.setQuery(currentSearchQuery, false);
            } else if (recipesAdapter != null)
                recipesAdapter.refreshAdapter(recipes);
        }
    }

    @Override
    public void onFiltersSelected(@NotNull ArrayList<TagModel> tags) {
        Log.d(TAG, "onFiltersSelected: tags = " + tags);
        tagsSelected = tags;
        List<RecipeModel> oldRecipes = recipesAdapter.getRecipes();
        List<RecipeModel> newRecipes = Util.findByTags(tags, recipes);
        recipesAdapter.refreshAdapter(newRecipes);
        calculateDiff(oldRecipes, newRecipes);
    }

    private void calculateDiff(final List<RecipeModel> oldList,
                               final List<RecipeModel> newList) {

        DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldList.size();
            }

            @Override
            public int getNewListSize() {
                return newList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return oldList.get(oldItemPosition).equals(newList.get(newItemPosition));
            }
        }).dispatchUpdatesTo(recipesAdapter);
    }

    @Override
    public void onRecipeClick(int position, View view) {
        Util.vibrate(requireContext());
        RecipeModel currentRecipe = recipesAdapter.getRecipes().get(position);
        if (spHelper.showAdFirst() && !spHelper.getPremiumStatus()) {
            Util.loadFragment(requireActivity(),
                    R.id.action_navigation_recipes_to_adMobFragment,
                    currentRecipe);
        } else {
            Util.loadFragment(requireActivity(),
                    R.id.action_navigation_recipes_to_recipeFragment,
                    currentRecipe);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        Util.vibrate(requireContext());
        switch (v.getId()) {
            case R.id.linear_layout_empty_container:
                Util.refreshCurrentFragment(requireActivity());
                break;
            case R.id.btn_up:
                if (recyclerViewRecipes != null && smoothScroller != null
                        && gridLayoutManager != null) {
                    smoothScroller.setTargetPosition(0);
                    gridLayoutManager.startSmoothScroll(smoothScroller);
                }
                break;
        }
    }

    @Override
    public void onSingleRecipeLoaded(RecipeModel recipe) {
        //Empty
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        currentSearchQuery = query;
        if (query != null && !query.isEmpty()) {
            filter(query);
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        currentSearchQuery = newText;
        Log.d(TAG, "onQueryTextChange: newText =" + newText);
        if (newText != null) {
            if (!newText.isEmpty()) {
                filter(newText);
            } else {
                resetRecipeList();
            }
        }
        return false;
    }

    @Override
    public boolean onClose() {
        Log.d(TAG, "onClose: ");
        resetRecipeList();
        currentSearchQuery = "";
        return false;
    }

    private void resetRecipeList() {
        Log.d(TAG, "resetRecipeList: ");
        if (recipes != null && !recipes.isEmpty()) {
            if (tagsSelected != null && !tagsSelected.isEmpty()) {
                onFiltersSelected(tagsSelected);
            } else {
                recipesAdapter.refreshAdapter(recipes);
            }
        }
    }

    private void filter(@NotNull String query) {
        final String queryClarified = StringHelper.clarifyText(query.toLowerCase());
        List<RecipeModel> oldRecipes = recipes;
        ArrayList<RecipeModel> filteredVerseList = new ArrayList<>();
        for (RecipeModel recipe : oldRecipes) {
            final String title = StringHelper.clarifyText(recipe.getTitle().toLowerCase());
            final String description = StringHelper.clarifyText(recipe.getDescription()
                    .toLowerCase());
            final List<TagModel> tags = recipe.getTags();
            if (title.contains(queryClarified) || description.contains(queryClarified)
                    || tags.contains(new TagModel(queryClarified))) {
                filteredVerseList.add(recipe);
            }
        }
        recipesAdapter.refreshAdapter(filteredVerseList);
        calculateDiff(oldRecipes, filteredVerseList);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLoading();
    }

    private void stopLoading() {
        if (recipesLoaderJob != null && recipesLoaderJob.isActive()) {
            recipesLoaderJob.cancel(new CancellationException());
            Log.d(TAG, "onDestroyView: CANCELING JOB = "
                    + recipesLoaderJob.isCancelled());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        savedState = saveState();
        recyclerViewRecipes.setAdapter(null);
        Log.d(TAG, "onDestroyView: savedState = " + savedState);
    }

    private Bundle saveState() {
        Bundle state = new Bundle();
        if (recipes != null && !recipes.isEmpty()) {
            String recipesJson = new Gson().toJson(recipes);
            state.putString(RECIPE_KEY, recipesJson);
        }
        if (links != null && !links.isEmpty()) {
            String linksJson = new Gson().toJson(links);
            state.putString(LINK_KEY, linksJson);
        }
        if (tagsSelected != null && !tagsSelected.isEmpty()) {
            String tagsSelectedJson = new Gson().toJson(tagsSelected);
            state.putString(TAG_KEY, tagsSelectedJson);
        }

        if (!currentSearchQuery.isEmpty()) {
            state.putString(QUERY_KEY, currentSearchQuery);
        }

        return state;
    }

    private void restoreState() {
        Log.d(TAG, "restoreState: savedState = " + savedState);
        if (recipes != null && !recipes.isEmpty()) {
            setUpRecyclerView();
            recipesAdapter = new RecipesAdapter(this.recipes, requireContext(), this);
            recyclerViewRecipes.setAdapter(recipesAdapter);
            Log.d(TAG, "restoreState: RECIPES = " + recipes.toString());

            if (links != null && !links.isEmpty()) {
                setUpFilterView(links);
            }

            if (!currentSearchQuery.isEmpty()) {
                searcher.setQuery(currentSearchQuery, false);
            } else {
                Log.d(TAG, "restoreState: LINKS: " + links);
                if (tagsSelected != null && !tagsSelected.isEmpty()) {
                    onFiltersSelected(tagsSelected);
                    Log.d(TAG, "restoreState: TAGS SELECTED: " + tagsSelected.toString());
                }
            }
        }
    }

/*    private void resetSearchView() {
        if (searcher != null && searcher.getVisibility() == View.VISIBLE) {
            searcher.setQuery("", false);
            searcher.onActionViewCollapsed();
            searcher.clearFocus();
            searcher.setIconified(true);
        }
    }*/

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