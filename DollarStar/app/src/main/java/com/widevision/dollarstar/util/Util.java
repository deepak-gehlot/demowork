package com.widevision.dollarstar.util;

import android.app.Activity;
import android.content.Intent;

import com.widevision.dollarstar.R;

/**
 * Created by mercury-five on 19/02/16.
 */
public class Util {


    public static void actionShare(Activity activity) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, activity.getResources().getString(R.string.app_name));
        String sAux = "";
        sAux = sAux + activity.getResources().getString(R.string.share_text) + "\n" + activity.getResources().getString(R.string.share_url) + "\n";
        i.putExtra(Intent.EXTRA_TEXT, sAux);
        activity.startActivity(Intent.createChooser(i, "Share using"));
    }

}
