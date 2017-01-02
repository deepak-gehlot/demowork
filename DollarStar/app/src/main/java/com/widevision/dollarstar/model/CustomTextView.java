package com.widevision.dollarstar.model;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by mercury-one on 1/4/16.
 */
public class CustomTextView extends TextView {


    public CustomTextView(Context context) {
        super(context);
        init();
    }

    public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init();
    }

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
       /* Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "BauhausCTT.ttf");
        setTypeface(myTypeface);*/

    }
}
