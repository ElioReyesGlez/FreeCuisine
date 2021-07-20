package com.erg.freecuisine.helpers;

import android.app.Activity;
import android.view.View;

import com.erg.freecuisine.R;
import com.google.android.material.snackbar.Snackbar;

public class MessageHelper {

    public static void showSuccessMessage(Activity context, String msg, View placeSnackBar) {
        if (!context.isFinishing()) {
            Snackbar snackBar = Snackbar.make(placeSnackBar, msg, Snackbar.LENGTH_LONG);
            snackBar.setBackgroundTint(context.getResources().getColor(R.color.md_green_200));
            if (!snackBar.isShown())
                snackBar.show();
        }
    }

    public static void showInfoMessage(Activity context, String msg, View placeSnackBar) {
        if (!context.isFinishing()) {
            Snackbar snackBar = Snackbar.make(placeSnackBar, msg, Snackbar.LENGTH_LONG);
            snackBar.setBackgroundTint(context.getResources().getColor(R.color.custom_white_text_color));
            snackBar.setTextColor(context.getResources().getColor(R.color.text_second_color));
            if (!snackBar.isShown())
                snackBar.show();
        }
    }

    public static void showInfoMessageWarning(Activity context, String msg, View placeSnackBar) {
        if (!context.isFinishing()) {
            Snackbar snackBar = Snackbar.make(placeSnackBar, msg, Snackbar.LENGTH_LONG);
            snackBar.setBackgroundTint(context.getResources().getColor(R.color.yellow_bg_color_light));
            snackBar.setTextColor(context.getResources().getColor(R.color.text_second_color));
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

    public static void showSuccessMessageOnMain(Activity context, String msg) {
        if (!context.isFinishing()) {
            Snackbar snackBar = Snackbar.make(context.findViewById(R.id.nav_host_fragment),
                    msg, Snackbar.LENGTH_LONG);
            snackBar.setBackgroundTint(context.getResources().getColor(R.color.md_green_200));
            snackBar.setTextColor(context.getResources().getColor(R.color.text_second_color));
            if (!snackBar.isShown())
                snackBar.show();
        }
    }

    public static void showErrorMessageOnMain(Activity context, String msg) {
        if (!context.isFinishing()) {
            Snackbar snackBar = Snackbar.make(context.findViewById(R.id.nav_host_fragment),
                    msg, Snackbar.LENGTH_LONG);
            snackBar.setBackgroundTint(context.getResources().getColor(R.color.red_btn_bg_color));
            if (!snackBar.isShown())
                snackBar.show();
        }
    }

    public static void showWarningMessageOnMain(Activity context, String msg) {
        if (!context.isFinishing()) {
            Snackbar snackBar = Snackbar.make(context.findViewById(R.id.nav_host_fragment),
                    msg, Snackbar.LENGTH_LONG);
            snackBar.setBackgroundTint(context.getResources().getColor(R.color.yellow_bg_color_light));
            snackBar.setTextColor(context.getResources().getColor(R.color.text_second_color));
            if (!snackBar.isShown())
                snackBar.show();
        }
    }


    public static void showInfoMessageWarningOnMain(Activity context, String msg) {
        if (!context.isFinishing()) {
            Snackbar snackBar = Snackbar.make(context.findViewById(R.id.nav_host_fragment),
                    msg, Snackbar.LENGTH_LONG);
            snackBar.setBackgroundTint(context.getResources().getColor(R.color.yellow_bg_color_light));
            snackBar.setTextColor(context.getResources().getColor(R.color.text_second_color));
            if (!snackBar.isShown())
                snackBar.show();
        }
    }
}
