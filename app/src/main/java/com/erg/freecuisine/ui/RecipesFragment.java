
package com.erg.freecuisine.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.airbnb.lottie.LottieAnimationView;
import com.erg.freecuisine.R;
import com.erg.freecuisine.adapters.LoadingAdapter;
import com.erg.freecuisine.adapters.RecipesAdapter;
import com.erg.freecuisine.adapters.RecipesFilterAdapter;
import com.erg.freecuisine.controller.network.AsyncDataLoad;
import com.erg.freecuisine.controller.network.helpers.FireBaseHelper;
import com.erg.freecuisine.controller.network.helpers.StringHelper;
import com.erg.freecuisine.interfaces.OnFireBaseListenerDataStatus;
import com.erg.freecuisine.interfaces.OnRecipeListener;
import com.erg.freecuisine.models.LinkModel;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.TagModel;
import com.erg.freecuisine.util.Util;
import com.google.gson.Gson;
import com.yalantis.filter.listener.FilterListener;
import com.yalantis.filter.widget.Filter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;

import kotlinx.coroutines.Job;

import static com.erg.freecuisine.util.Constants.LINK_KEY;
import static com.erg.freecuisine.util.Constants.RECIPE_KEY;
import static com.erg.freecuisine.util.Constants.SAVED_STATE_KEY;
import static com.erg.freecuisine.util.Constants.TAG_KEY;
import static com.erg.freecuisine.util.Constants.URL_KEY;

public class RecipesFragment extends Fragment implements
        FilterListener<TagModel>, OnRecipeListener, OnFireBaseListenerDataStatus,
        SearchView.OnQueryTextListener {

    private static final String TAG = "RecipesFragment";

    private View rootView;
    private RecyclerView recyclerViewRecipes;
    private LottieAnimationView lottie_anim_empty;
    private SearchView searcher;
    private CoordinatorLayout filter_container;
    private Filter<TagModel> filter;
    private Animation scaleUP, scaleDown;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: savedInstanceState = " + savedInstanceState);

        recipes = new ArrayList<>();
        tags = new ArrayList<>();
        tagsSelected = new ArrayList<>();
        asyncDataLoad = new AsyncDataLoad();
        colors = getResources().getIntArray(R.array.colors);

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
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBundle(SAVED_STATE_KEY, (savedState != null) ? savedState : saveState());
        Log.d(TAG, "onSaveInstanceState: outState = " + outState);
    }

    public void setUpView() {

        filter_container = rootView.findViewById(R.id.container_for_filter);
        searcher = rootView.findViewById(R.id.searcher);
        recyclerViewRecipes = rootView.findViewById(R.id.recycler_view_recipes);
        lottie_anim_empty = rootView.findViewById(R.id.lottie_anim_empty);

        scaleUP = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);

        searcher.setSubmitButtonEnabled(true);
        searcher.setOnQueryTextListener(this);
//        searcher.setOnSearchClickListener(this);

        int numberOfColumns = 2;
        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(numberOfColumns, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewRecipes.setLayoutManager(gridLayoutManager);
        recyclerViewRecipes.setHasFixedSize(true);
        GridItemDecoration gridItemDecoration = new GridItemDecoration(12, 9);
        recyclerViewRecipes.addItemDecoration(gridItemDecoration);
        recyclerViewRecipes.setVerticalScrollBarEnabled(false); // disable temporally scroll bar

        if (savedState != null) {
            String recipesJson = savedState.getString(RECIPE_KEY);
            recipes = StringHelper.getRecipesFromStringJson(recipesJson);

            String linksJson = savedState.getString(LINK_KEY);
            links = StringHelper.getLinksFromStringJson(linksJson);

            String tagsSelectedJson = savedState.getString(TAG_KEY);
            tagsSelected = StringHelper.getSelectedTagsFromStringJson(tagsSelectedJson);

            restoreState();

        } else {
            LoadingAdapter loadingAdapter = new LoadingAdapter(
                    getLoadingList(), requireContext(),
                    R.layout.loading_item_recipe_card);
            recyclerViewRecipes.setNestedScrollingEnabled(false);
            recyclerViewRecipes.setAdapter(loadingAdapter);

            if (recipesLoaderJob != null && recipesLoaderJob.isActive()) {
                recipesLoaderJob.cancel(new CancellationException());
            }
            new FireBaseHelper().getLinks(this);
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
    public void onLinksLoaded(List<LinkModel> links, List<String> keys) {
        Log.d(TAG, "onLinksLoaded: LINKS = " + links.toString());
        this.links = links;
        recipesLoaderJob = asyncDataLoad.loadRecipesAsync(
                requireActivity(), this, links);
    }


    @Override
    public void onRecipesLoaded(ArrayList<RecipeModel> recipes) {
        Log.d(TAG, "onRecipesLoaded: Recipes = " + recipes.toString());
        this.recipes = recipes;
        if (mContext != null && isVisible() && !this.recipes.isEmpty()
                && isVisible()) {
            recipesAdapter = new RecipesAdapter(this.recipes, requireContext(), this);
            recyclerViewRecipes.setAdapter(recipesAdapter);
            recyclerViewRecipes.setNestedScrollingEnabled(true);
            recyclerViewRecipes.setVerticalScrollBarEnabled(true);
            setUpFilterView(links);
            refreshView();
        }
    }

    @Override
    public void onMainUrlLoaded(LinkModel link) {
        //Empty
    }

    private void setUpFilterView(List<LinkModel> links) {
        tags = getTags(links);
        RecipesFilterAdapter filterAdapter = new RecipesFilterAdapter(tags,
                colors, requireActivity());

        filter = new Filter<>(requireContext());
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
        filter_container.addView(filter);
        Util.showView(scaleUP, filter_container);
    }

    public void refreshView() {
        if (recipes != null && !recipes.isEmpty()) {
            Util.hideView(null, lottie_anim_empty);
            Util.showView(scaleUP, recyclerViewRecipes);
            Util.showView(scaleUP, filter);
            Util.showView(scaleUP, searcher);
        } else {
            Util.showView(scaleUP, lottie_anim_empty);
            Util.hideView(scaleDown, recyclerViewRecipes);
            Util.hideView(scaleDown, filter);
            Util.hideView(scaleUP, searcher);
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
            recipesAdapter.refreshAdapter(recipes);
        }
    }

    @Override
    public void onFiltersSelected(@NotNull ArrayList<TagModel> tags) {
        Log.d(TAG, "onFiltersSelected: ");
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
        loadFragment(position, view);
    }

    private void loadFragment(int position, View view) {
        Util.vibrateMin(requireContext());
        RecipeModel currentRecipe = recipesAdapter.getRecipes().get(position);
        Bundle args = new Bundle();
        args.putString(URL_KEY, currentRecipe.getLink());
        ArrayList<TagModel> tagModels = new ArrayList<>(currentRecipe.getTags());
        args.putParcelableArrayList(TAG_KEY, tagModels);

        NavController navController = Navigation
                .findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_navigation_recipes_to_recipeFragment, args);
    }

    @Override
    public void onSingleRecipeLoaded(RecipeModel recipe) {
        //Empty
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
//        currentSearchQuery = query;
        if (query != null && !query.isEmpty()) {
            filter(query);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
//        currentSearchQuery = newText;
        if (newText != null && !newText.isEmpty()) {
            filter(newText);
        }
        if (newText != null && newText.isEmpty()) {
            resetRecipeList();
        }
        return true;
    }

    private void resetRecipeList() {
        if (tagsSelected != null && tagsSelected.isEmpty()) {
            if (recipesAdapter != null)
                recipesAdapter.refreshAdapter(recipes);
        } else if (tagsSelected != null) {
            onFiltersSelected(tagsSelected);
        }
    }

    private void filter(@NotNull String query) {
        final String lowerCaseQuery = query.toLowerCase();
        List<RecipeModel> oldRecipes = recipes;
        ArrayList<RecipeModel> filteredVerseList = new ArrayList<>();
        for (RecipeModel recipe : oldRecipes) {
            final String title = recipe.getTitle().toLowerCase();
            final String description = recipe.getDescription().toLowerCase();
            final List<TagModel> tags = recipe.getTags();
            if (title.contains(lowerCaseQuery) || description.contains(lowerCaseQuery)
                    || tags.contains(new TagModel(query))) {
                filteredVerseList.add(recipe);
            }
        }
        recipesAdapter.refreshAdapter(filteredVerseList);
        calculateDiff(oldRecipes, filteredVerseList);
    }

    @Override
    public void onPause() {
        super.onPause();
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
        return state;
    }

    private void restoreState() {
        Log.d(TAG, "restoreState: savedState = " + savedState);
        if (recipes != null && !recipes.isEmpty()) {
            recipesAdapter = new RecipesAdapter(this.recipes, requireContext(), this);
            recyclerViewRecipes.swapAdapter(recipesAdapter, false);
            recyclerViewRecipes.setVerticalScrollBarEnabled(true);
            Log.d(TAG, "restoreState: RECIPES = " + recipes.toString());

            if (links != null && !links.isEmpty()) {
                setUpFilterView(links);
                Log.d(TAG, "restoreState: LINKS: " + links.toString());
            }

            if (tagsSelected != null && !tagsSelected.isEmpty()) {
                onFiltersSelected(tagsSelected);
                Log.d(TAG, "restoreState: TAGS SELECTED: " + tagsSelected.toString());
            }
        }
    }

    private void resetSearchView() {
        if (searcher != null && searcher.getVisibility() == View.VISIBLE) {
            searcher.setQuery("", false);
            searcher.onActionViewCollapsed();
            searcher.clearFocus();
            searcher.setIconified(true);
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