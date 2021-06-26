package com.erg.freecuisine.adapters;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.erg.freecuisine.models.TagModel;
import com.yalantis.filter.adapter.FilterAdapter;
import com.yalantis.filter.widget.FilterItem;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RecipesFilterAdapter extends FilterAdapter<TagModel> {

    private int[] colors;
    private Context context;

    public RecipesFilterAdapter(@NotNull List<? extends TagModel> items,
                                int[] colors, Context context) {
        super(items);
        this.colors = colors;
        this.context = context;
    }

    RecipesFilterAdapter(@NotNull List<? extends TagModel> items) {
        super(items);
    }

    @NotNull
    @Override
    public FilterItem createView(int position, TagModel item) {
        FilterItem filterItem = new FilterItem(context);

        filterItem.setStrokeColor(colors[0]);
        filterItem.setTextColor(colors[0]);
        filterItem.setCheckedTextColor(ContextCompat.getColor(context, android.R.color.white));
        filterItem.setColor(ContextCompat.getColor(context, android.R.color.white));
        filterItem.setCheckedColor(colors[position]);
        filterItem.setText(item.getText());
        filterItem.deselect();

        return filterItem;
    }
}
