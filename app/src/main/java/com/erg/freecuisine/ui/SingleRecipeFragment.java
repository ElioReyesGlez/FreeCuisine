package com.erg.freecuisine.ui;

import android.graphics.Paint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;
import androidx.transition.Transition;
import androidx.transition.TransitionInflater;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.erg.freecuisine.R;
import com.erg.freecuisine.controller.network.AsyncDataLoad;
import com.erg.freecuisine.models.RecipeModel;
import com.erg.freecuisine.models.StepModel;
import com.erg.freecuisine.models.TagModel;
import com.erg.freecuisine.util.Util;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import static com.erg.freecuisine.util.Constants.TAG_KEY;
import static com.erg.freecuisine.util.Constants.URL_KEY;


public class SingleRecipeFragment extends Fragment {

    public static final String TAG = "RecipeFragment";
    private String link;
    private View rootView;
    public RelativeLayout relative_container_recipe_main_info;
    public LinearLayout linear_layout_empty_container;
    public LottieAnimationView lottie_anim_loading;
    public Animation scaleUp, scaleDown;
    private ArrayList<TagModel> tags;
    private AsyncDataLoad asyncDataLoad;


    public SingleRecipeFragment() {
        // Required empty public constructor
    }

    public static SingleRecipeFragment newInstance() {
        return new SingleRecipeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Transition transition = TransitionInflater.from(requireContext()).inflateTransition(
                android.R.transition.move);
        setSharedElementEnterTransition(transition);
        setSharedElementReturnTransition(transition);

        Bundle args = getArguments();
        if (args != null && !args.isEmpty()) {
            link = args.getString(URL_KEY);
            if (args.getParcelableArrayList(TAG_KEY) != null) {
                tags = args.getParcelableArrayList(TAG_KEY);
            }
        }

        asyncDataLoad = new AsyncDataLoad();
        scaleUp = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_up);
        scaleDown = AnimationUtils.loadAnimation(requireContext(), R.anim.scale_down);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_single_recipe, container, false);

        linear_layout_empty_container = rootView
                .findViewById(R.id.linear_layout_empty_container);
        relative_container_recipe_main_info  = rootView
                .findViewById(R.id.relative_container_recipe_main_info);
        lottie_anim_loading  = rootView
                .findViewById(R.id.lottie_anim_loading);
        Util.showView(scaleUp, lottie_anim_loading);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        asyncDataLoad.loadSingleRecipeAsync(requireActivity(), this, link, tags);
    }

    public void setUpView(RecipeModel recipe) {

        Util.hideView(null, lottie_anim_loading);
        if (recipe !=  null && !recipe.getTitle().isEmpty()) {

            ShapeableImageView recipeImg = rootView.findViewById(R.id.recipe_main_image);
            AppCompatTextView type = rootView.findViewById(R.id.tv_type);
            TextView recipeTitle = rootView.findViewById(R.id.recipe_title);
            TextView recipeDescription = rootView.findViewById(R.id.recipe_description);
            LinearLayout linearColumn1 = rootView.findViewById(R.id.linear_column_1);
            LinearLayout linearColumn2 = rootView.findViewById(R.id.linear_column_2);
            RelativeLayout typeContainer = rootView
                    .findViewById(R.id.relative_recipes_type_container);


            recipe.setTags(tags); // From Bundle Args
            recipeTitle.setText(recipe.getTitle());
            recipeDescription.setText(recipe.getDescription());

            if (!recipe.getType().isEmpty()) {
                Util.showView(null, typeContainer);
                type.setText(recipe.getType());
            } else {
                Util.hideView(null, typeContainer);
            }

            String[] ingredients = Util.extractIngredients(recipe.getIngredients());
            for (int i = 0; i < ingredients.length; i++) {
                String ingredient = ingredients[i];
                View view = getLayoutInflater().inflate(R.layout.item_ingredint, null);
                CheckBox checkBox = view.findViewById(R.id.checkBox);
                TextView textView = view.findViewById(R.id.textViewIngredient);
                textView.setText(ingredient);
                view.setId(i);
                if (i < ingredients.length / 2) {
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


            Picasso.get()
                    .load(recipe.getImage().getUrl())
                    .error(R.drawable.ic_lunch_chef)
                    .placeholder(R.drawable.ic_loading)
                    .into(recipeImg);
            LinearLayout linear_preparation_container = rootView.findViewById(R.id.linear_preparation_container);
            for (StepModel step : recipe.getSteps()) {
                View view = getLayoutInflater().inflate(R.layout.item_recipe_preparation_step, null);
                TextView tv_preparation_number_order = view.findViewById(R.id.tv_preparation_number_order);
                TextView tv_preparation_description = view.findViewById(R.id.tv_preparation_description);
//                TextView tv_tips = view.findViewById(R.id.tv_tips);
                ShapeableImageView iv_step_preparation_image = view.findViewById(R.id.iv_step_preparation_image);

                tv_preparation_number_order.setText(String.valueOf(step.getStepNumber()));
                tv_preparation_description.setText(step.getPreparation());
                Picasso.get().load(step.getImage().getUrl()).into(iv_step_preparation_image);
                linear_preparation_container.addView(view);
            }

            Util.showView(scaleUp, relative_container_recipe_main_info);
            Util.hideView(null, linear_layout_empty_container);
        } else {
            Util.showView(scaleUp, linear_layout_empty_container);
            Util.hideView(scaleDown, relative_container_recipe_main_info);
        }
    }
}