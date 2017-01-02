package com.widevision.dollarstar.util;

import android.app.Activity;
import android.graphics.Color;

import com.widevision.dollarstar.SweetAlert.SweetAlertDialog;


/**
 * Created by mercury-five on 20/01/16.
 */
public class ProgressLoaderHelper {

    private static volatile ProgressLoaderHelper instance = null;

    private SweetAlertDialog dialog;

    // private constructor
    private ProgressLoaderHelper() {
    }

    public static ProgressLoaderHelper getInstance() {
        if (instance == null) {
            synchronized (ProgressLoaderHelper.class) {
                // Double check
                if (instance == null) {
                    instance = new ProgressLoaderHelper();

                }
            }
        }
        return instance;
    }

    public void showProgress(Activity activity) {
        dialog = new SweetAlertDialog(activity, SweetAlertDialog.PROGRESS_TYPE);
        dialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
        dialog.setTitleText("Loading...");
        dialog.show();
    }

    public void showWarning(Activity activity, String message) {
        dialog = new SweetAlertDialog(activity, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Opps...")
                .setContentText(message)
                .setCancelText("Ok")
                .showCancelButton(true)
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        dialog.dismissWithAnimation();
                    }
                });
        dialog.show();
    }

    public void dismissProgress() {
        if (dialog != null) {
            dialog.cancel();
        }
    }
}
