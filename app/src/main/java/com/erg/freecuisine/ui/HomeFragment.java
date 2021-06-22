package com.erg.freecuisine.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.erg.freecuisine.R;
import com.erg.freecuisine.adapters.LoadingAdapter;
import com.erg.freecuisine.adapters.RecommendedRecipesAdapter;
import com.erg.freecuisine.controller.network.AsyncDataLoad;
import com.erg.freecuisine.controller.network.helpers.FireBaseHelper;
import com.erg.freecuisine.interfaces.OnFireBaseListenerDataStatus;
import com.erg.freecuisine.interfaces.OnRecipeListener;
import com.erg.freecuisine.models.LinkModel;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.TagModel;
import com.erg.freecuisine.util.Util;

import java.util.ArrayList;
import java.util.List;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        recipes = new ArrayList<>();
        asyncDataLoad = new AsyncDataLoad();
        fireBaseHelper = new FireBaseHelper();

    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        rootView = inflater.inflate(R.layout.fragment_home, container, false);
        setUpView();
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fireBaseHelper.getMainUrl(this);
    }

    @Override
    public void onMainUrlLoaded(LinkModel link) {
        asyncDataLoad.loadRecommendRecipesAsync(requireActivity(), this, link);
    }

    private void setUpView() {
        recyclerviewRecommendRecipe = rootView.findViewById(R.id.recyclerviewRecommendRecipe);
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                requireContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerviewRecommendRecipe.setLayoutManager(layoutManager);

        LoadingAdapter loadingAdapter = new LoadingAdapter(
                getLoadingList(), requireContext(),
                R.layout.loading_item_recommend_recipe_card);
        recyclerviewRecommendRecipe.setAdapter(loadingAdapter);
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
        RecipeModel currentRecipe = adapter.getRecipes().get(position);
        Bundle args = new Bundle();
        args.putString(URL_KEY, currentRecipe.getLink());
        ArrayList<TagModel> tagModels = new ArrayList<>(currentRecipe.getTags());
        args.putParcelableArrayList(TAG_KEY, tagModels);

        NavController navController = Navigation
                .findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_navigation_home_to_singleRecipeFragment, args);
    }

    @Override
    public void onSingleRecipeLoaded(RecipeModel recipe) {
        //Empty
    }

    @Override
    public void onRecipesLoaded(ArrayList<RecipeModel> recipes) {
        this.recipes = recipes;
        adapter = new RecommendedRecipesAdapter(
                recipes, requireContext(), this);
        recyclerviewRecommendRecipe.setAdapter(adapter);
    }

    @Override
    public void onLinksLoaded(List<LinkModel> links, List<String> keys) {
        //Empty
    }

}