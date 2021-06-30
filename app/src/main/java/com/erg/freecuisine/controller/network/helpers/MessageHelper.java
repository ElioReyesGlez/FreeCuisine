package com.erg.freecuisine.controller.network.helpers;

import android.app.Activity;
import android.view.View;

import com.erg.freecuisine.R;
import com.google.android.material.snackbar.Snackbar;

public class MessageHelper {
    public static void showInfoMessage(Activity context, String msg, View placeSnackBar) {
        if (!context.isFinishing()) {
            Snackbar snackBar = Snackbar.make(placeSnackBar, msg, Snackbar.LENGTH_LONG);
            snackBar.setBackgroundTint(context.getResources().getColor(R.color.colorPrimary));
            if (!snackBar.isShown())
                snackBar.show();
        }
    }

    public static void showInfoMessageWarning(Activity context, String msg, View placeSnackBar) {
        if (!context.isFinishing()) {
            Snackbar snackBar = Snackbar.make(placeSnackBar, msg, Snackbar.LENGTH_LONG);
            snackBar.setBackgroundTint(context.getResources().getColor(R.color.yellow_bg_color_light));
            snackBar.setTextColor(context.getResources().getColor(R.color.dark_gray_btn_bg_color));
            if (!snackBar.isShown())
                snackBar.show();
        }
    }

    public static void showInfoMessageError(Activity context, String msg, View placeSnackBar) {
        if (!context.isFinishing()) {

            Snackbar snackBar = Snackbar.make(placeSnackBar, msg, Snackbar.LENGTH_LONG);
            snackBar.setBackgroundTint(context.getResources().getColor(R.color.red_btn_bg_color));
            if (!snackBar.isShown())
                snackBar.show();
        }
    }
}
