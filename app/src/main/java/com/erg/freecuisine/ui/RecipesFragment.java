package com.erg.freecuisine.ui;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
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
import com.erg.freecuisine.controller.network.helpers.SharedPreferencesHelper;
import com.erg.freecuisine.controller.network.helpers.TimeHelper;
import com.erg.freecuisine.interfaces.OnFireBaseListenerDataStatus;
import com.erg.freecuisine.interfaces.OnRecipeListener;
import com.erg.freecuisine.models.LinkModel;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.TagModel;
import com.erg.freecuisine.util.Util;
import com.yalantis.filter.listener.FilterListener;
import com.yalantis.filter.widget.Filter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import kotlinx.coroutines.Job;

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
    private final String currentSearchQuery = "";
    private Animation scaleUP, scaleDown;

    private List<RecipeModel> recipes;
    private List<TagModel> tags;
    private ArrayList<TagModel> tagsSelected;
    private List<LinkModel> links;
    private int[] colors;

    private AsyncDataLoad asyncDataLoad;
    private Filter<TagModel> filter;
    private RecipesFilterAdapter filterAdapter;
    private RecipesAdapter recipesAdapter;
    private Job recipesLoaderJob;
    private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");

        recipes = new ArrayList<>();
        tags = new ArrayList<>();
        tagsSelected = new ArrayList<>();
        asyncDataLoad = new AsyncDataLoad();
        colors = getResources().getIntArray(R.array.colors);

        if (recipesLoaderJob != null && recipesLoaderJob.isActive()) {
            recipesLoaderJob.cancel(recipesLoaderJob.getCancellationException());
        }
        new FireBaseHelper().getLinks(this);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        rootView = inflater.inflate(R.layout.fragment_recipes, container, false);
        setUpView();

        return rootView;
    }

    public void setUpView() {

        filter = rootView.findViewById(R.id.filter);
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

        LoadingAdapter loadingAdapter = new LoadingAdapter(
                getLoadingList(), requireContext(),
                R.layout.loading_item_recipe_card);
        recyclerViewRecipes.setAdapter(loadingAdapter);
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
        this.links = links;
        setUpFilterView(links);
        recipesLoaderJob = asyncDataLoad.loadRecipesAsync(
                requireActivity(), this, links);
    }


    @Override
    public void onRecipesLoaded(ArrayList<RecipeModel> recipes) {
        this.recipes = recipes;
        if (mContext != null && isVisible()) {
            recipesAdapter = new RecipesAdapter(this.recipes, requireContext(), this);
            recyclerViewRecipes.setAdapter(recipesAdapter);
            refreshView();
        }
    }

    @Override
    public void onMainUrlLoaded(LinkModel link) {
        //Empty
    }

    private void setUpFilterView(List<LinkModel> links) {
        tags.clear();
        tags = getTags(links);

        filterAdapter = new RecipesFilterAdapter(tags,
                colors, requireContext());
        filter.setAdapter(filterAdapter);
        filter.setListener(this);

        //the text to show when there's no selected items
        filter.setNoSelectedItemText(getString(R.string.str_all_selected));
        filter.build();
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
        if (!tags.isEmpty())
            if (tagModel.getText().contains(tags.get(0).getText())) {
                filter.deselectAll();
                filter.collapse();
                tagsSelected.clear();
            }
    }

    @Override
    public void onNothingSelected() {
        tagsSelected.clear();
        if (recyclerViewRecipes != null && !recipes.isEmpty()) {
            recipesAdapter.refreshAdapter(recipes);
        }
    }

    @Override
    public void onFiltersSelected(@NotNull ArrayList<TagModel> tags) {
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
        if (tagsSelected.isEmpty()) {
            if (recipesAdapter != null)
                recipesAdapter.refreshAdapter(recipes);
        } else {
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
    public void onResume() {
        super.onResume();
        restoreState();
        Log.d(TAG, "onResume: ");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
    }

    private void restoreState() {
        if (recipes != null && !recipes.isEmpty()) {
            recipesAdapter = new RecipesAdapter(this.recipes, requireContext(), this);
            recyclerViewRecipes.swapAdapter(recipesAdapter, true);

            if (links != null && !links.isEmpty() && isVisible() && isResumed()) {
                setUpFilterView(links);
                Log.d(TAG, "restoreState: LINKS: " + links.toString());
            }
        }

        if (tagsSelected != null && !tagsSelected.isEmpty()) {
            onFiltersSelected(tagsSelected);
            Log.d(TAG, "restoreState: TAGS SELECTED: " + tagsSelected.toString());
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