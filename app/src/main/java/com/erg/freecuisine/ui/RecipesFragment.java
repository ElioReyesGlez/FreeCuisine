package com.erg.freecuisine.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.airbnb.lottie.LottieAnimationView;
import com.erg.freecuisine.R;
import com.erg.freecuisine.adapters.RecipesAdapter;
import com.erg.freecuisine.adapters.RecipesFilterAdapter;
import com.erg.freecuisine.controller.network.AsyncDataLoad;
import com.erg.freecuisine.controller.network.helpers.FireBaseHelper;
import com.erg.freecuisine.interfaces.OnFireBaseListenerDataStatus;
import com.erg.freecuisine.interfaces.OnRecipeListener;
import com.erg.freecuisine.models.LinkModel;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.TagModel;
import com.erg.freecuisine.util.Util;
import com.yalantis.filter.animator.FiltersListItemAnimator;
import com.yalantis.filter.listener.FilterListener;
import com.yalantis.filter.widget.Filter;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.erg.freecuisine.util.Constants.TAG_KEY;
import static com.erg.freecuisine.util.Constants.URL_KEY;

public class RecipesFragment extends Fragment implements
        FilterListener<TagModel>, OnRecipeListener, OnFireBaseListenerDataStatus,
        SearchView.OnQueryTextListener {

    private static final String TAG = "RecipesFragment";

    private View rootView;
    private RecyclerView recyclerViewRecipes;
    private LottieAnimationView lottie_anim_empty, lottie_anim_loading;
    private Animation scaleUP, scaleDown;

    public List<RecipeModel> recipes;
    public List<TagModel> tags;
    private int[] colors;

    private AsyncDataLoad asyncDataLoad;
    private Filter<TagModel> filter;
    public RecipesAdapter recipesAdapter;
    private RecipesFilterAdapter filterAdapter;

    FireBaseHelper fireBaseHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);

        recipes = new ArrayList<>();
        tags = new ArrayList<>();
        asyncDataLoad = new AsyncDataLoad();
        colors = getResources().getIntArray(R.array.colors);
        fireBaseHelper = new FireBaseHelper();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_recipes, container, false);
        lottie_anim_loading = rootView.findViewById(R.id.lottie_anim_loading);
        lottie_anim_empty = rootView.findViewById(R.id.lottie_anim_empty);
        Util.showView(null, lottie_anim_loading);
        Util.hideView(null, lottie_anim_empty);

        setUpView();
        Log.d(TAG, "onCreateView: ");

        return rootView;
    }

    public void setUpView() {

        filter = rootView.findViewById(R.id.filter);

        recyclerViewRecipes = rootView.findViewById(R.id.recycler_view_recipes);
        scaleUP = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);

        int numberOfColumns = 2;
        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(numberOfColumns, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewRecipes.setLayoutManager(gridLayoutManager);
        recyclerViewRecipes.setHasFixedSize(true);
        GridItemDecoration gridItemDecoration = new GridItemDecoration(12, 9);
        recyclerViewRecipes.addItemDecoration(gridItemDecoration);
//        recyclerViewRecipes.setItemAnimator(new FiltersListItemAnimator());

        recipesAdapter = new RecipesAdapter(recipes, requireContext(), this);
        recyclerViewRecipes.setAdapter(recipesAdapter);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fireBaseHelper.getLinks(this);
        Log.d(TAG, "onViewCreated: ");
    }

    @Override
    public void onRecipesLoaded(ArrayList<RecipeModel> auxList) {
        recipes.addAll(auxList);
        recipesAdapter.refreshAdapter(recipes);
        refreshView();
    }


    @Override
    public void onLinksLoaded(List<LinkModel> links, List<String> keys) {
        setUpFilterView(links);
        for (LinkModel link : links) {
            asyncDataLoad.loadRecipesAsync(requireActivity(), this, link);
        }
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
        Util.hideView(null, lottie_anim_loading);
        if (recipes != null && !recipes.isEmpty()) {
            Util.hideView(null, lottie_anim_empty);
            Util.showView(scaleUP, recyclerViewRecipes);
            Util.showView(scaleUP, filter);
        } else {
            Util.showView(scaleUP, lottie_anim_empty);
            Util.hideView(scaleDown, recyclerViewRecipes);
            Util.hideView(scaleDown, filter);
        }
    }

    private List<TagModel> getTags(List<LinkModel> links) {

        List<TagModel> tags = new ArrayList<>();
        //All categories as first item
        tags.add(new TagModel(getString(R.string.str_all_selected), colors[0]));
        // The rest
        for (int i = 0; i < links.size(); ++i) {
            String tag = links.get(i).getTag();
//            tag = tag.replace('_', '&');  ToDo
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
            }
    }

    @Override
    public void onNothingSelected() {
        if (recyclerViewRecipes != null && !recipes.isEmpty()) {
            recipesAdapter.refreshAdapter(recipes);
        }
    }

    @Override
    public void onFiltersSelected(@NotNull ArrayList<TagModel> tags) {
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
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.menu_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        searchView.setSubmitButtonEnabled(true);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        if (query != null && !query.isEmpty()) {
            filter(query);
        }
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        filter(newText);
        return true;
    }

    private void filter(@NotNull String query) {
        final String lowerCaseQuery = query.toLowerCase();
        ArrayList<RecipeModel> filteredVerseList = new ArrayList<>();
        for (RecipeModel recipe : recipes) {
            final String title = recipe.getTitle().toLowerCase();
            final String description = recipe.getDescription().toLowerCase();
            final List<TagModel> tags = recipe.getTags();
            if (title.contains(lowerCaseQuery) || description.contains(lowerCaseQuery)
                    || tags.contains(new TagModel(query))) {
                filteredVerseList.add(recipe);
            }
        }
        recipesAdapter.refreshAdapter(filteredVerseList);
    }
}