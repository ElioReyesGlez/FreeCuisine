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

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.erg.freecuisine.R;
import com.erg.freecuisine.adapters.RecipesAdapter;
import com.erg.freecuisine.helpers.RealmHelper;
import com.erg.freecuisine.helpers.SharedPreferencesHelper;
import com.erg.freecuisine.helpers.StringHelper;
import com.erg.freecuisine.helpers.TimeHelper;
import com.erg.freecuisine.interfaces.OnRecipeListener;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.TagModel;
import com.erg.freecuisine.util.Util;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.erg.freecuisine.util.Constants.BOOKMARK_FLAG_KEY;
import static com.erg.freecuisine.util.Constants.JSON_RECIPE_KEY;
import static com.erg.freecuisine.util.Constants.QUERY_KEY;
import static com.erg.freecuisine.util.Constants.RECIPE_KEY;

public class BookmarksFragment extends Fragment implements View.OnClickListener,
        SearchView.OnQueryTextListener, SearchView.OnCloseListener, OnRecipeListener {

    private static final String TAG = "BookmarksFragment";

    private View rootView;
    private RecyclerView recyclerViewRecipes;
    private RecyclerView.SmoothScroller smoothScroller;
    private GridItemDecoration gridItemDecoration;
    private StaggeredGridLayoutManager gridLayoutManager;
    private SearchView searcher;
    private ImageButton btn_up;
    private LinearLayout linearEmptyContainer;
    private Animation scaleUP, scaleDown,enterAnim, exitAnim;

    private List<RecipeModel> recipes;
    private RecipesAdapter recipesAdapter;
    private String currentSearchQuery = "";
    private Runnable runnableHideUpBtn;
    private Handler handlerDelay;
    private SharedPreferencesHelper spHelper;

    private Bundle savedState = null;

    public BookmarksFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RealmHelper realmHelper = new RealmHelper();
        recipes = StringHelper.getRecipesFromStringJsonList(realmHelper.getRecipes());
        spHelper = new SharedPreferencesHelper(requireContext());
        handlerDelay = new Handler(Looper.getMainLooper());
        runnableHideUpBtn = () -> {
            if (isAdded() && isVisible()) {
                Util.hideView(exitAnim, btn_up);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        setUpView();
        return rootView;
    }

    public void setUpView() {
        Log.d(TAG, "setUpView: ");

        searcher = rootView.findViewById(R.id.searcher);
        recyclerViewRecipes = rootView.findViewById(R.id.recycler_view_recipes);
        linearEmptyContainer = rootView.findViewById(R.id.linear_layout_empty_container);
        scaleUP = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);
        enterAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.custom_enter_anim);
        exitAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.custom_exit_anim);
        btn_up = rootView.findViewById(R.id.btn_up);

        searcher.setSubmitButtonEnabled(true);
        searcher.setOnQueryTextListener(this);
        searcher.setOnCloseListener(this);
        btn_up.setOnClickListener(this);
        linearEmptyContainer.setOnClickListener(this);

        if (savedState != null) {
            String recipesJson = savedState.getString(RECIPE_KEY);
            recipes = StringHelper.getRecipesFromStringJson(recipesJson);
            currentSearchQuery = savedState.getString(QUERY_KEY, "");

            restoreState();

        } else {
            setUpRecyclerView();
            recipesAdapter = new RecipesAdapter(this.recipes, requireContext(), this);
            recyclerViewRecipes.setAdapter(recipesAdapter);
            refreshView();
        }
        savedState = null;
    }

    public void refreshView() {
        if (recipes != null && !recipes.isEmpty()) {
            Util.hideView(scaleDown, linearEmptyContainer);
            Util.showView(null, recyclerViewRecipes);
            Util.showView(null, searcher);
        } else {
            Util.showView(scaleUP, linearEmptyContainer);
            Util.hideView(null, recyclerViewRecipes);
            Util.hideView(null, searcher);
        }
    }

    @Override
    public void onRecipeClick(int position, View view) {
        Util.vibrate(requireContext());
        RecipeModel currentRecipe = recipesAdapter.getRecipes().get(position);
        SharedPreferencesHelper spHelper = new SharedPreferencesHelper(requireContext());
        if (spHelper.showAdFirst()) {
            loadFragment(currentRecipe,
                    R.id.action_bookmarksFragment_to_adMobFragment);
        } else {
            loadFragment(currentRecipe,
                    R.id.action_bookmarksFragment_to_singleRecipeFragment);
        }
    }

    @Override
    public void onSingleRecipeLoaded(RecipeModel recipe) {
        /*Empty*/
    }

    @Override
    public void onRecipesLoaded(ArrayList<RecipeModel> recipes) {
        /*Empty*/
    }

    @Override
    public void onLoaderFailed(String url, Exception e) {
        /*Empty*/
    }

    @Override
    public void onRecommendedRecipesLoaded(ArrayList<RecipeModel> recipes) {
        /*Empty*/
    }

    @Override
    public void onTipsRecipesLoaded(ArrayList<RecipeModel> recipes) {
        /*Empty*/
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
        }
    }

    private void loadFragment(RecipeModel currentRecipe, int idAction) {
        Bundle args = new Bundle();
        args.putBoolean(BOOKMARK_FLAG_KEY, true);
        String jsonRecipe = new Gson().toJson(currentRecipe);
        args.putString(JSON_RECIPE_KEY, jsonRecipe);
        NavController navController = Navigation
                .findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(idAction, args);
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

    private void setUpRecyclerView() {

        int numberOfColumns = 2;
        gridLayoutManager = new StaggeredGridLayoutManager(numberOfColumns, StaggeredGridLayoutManager.VERTICAL);
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
                        && recyclerView.canScrollVertically(Integer.MAX_VALUE)) {
                    Util.showView(enterAnim, btn_up);
                    handlerDelay.removeCallbacks(runnableHideUpBtn);
                    handlerDelay.postDelayed(runnableHideUpBtn, TimeHelper.DELAY / 2);
                } else {
                    Util.hideView(exitAnim, btn_up);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!recyclerView.canScrollVertically(Integer.MAX_VALUE)) {
                    Util.hideView(exitAnim, btn_up);
                }
            }
        });

    }

    private void resetRecipeList() {
        Log.d(TAG, "resetRecipeList: ");
        if (recipes != null && !recipes.isEmpty()) {
            recipesAdapter.refreshAdapter(recipes);
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

            if (!currentSearchQuery.isEmpty()) {
                searcher.setQuery(currentSearchQuery, false);
            }
        }
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(requireContext());
    }
}