package com.widevision.dollarstar.model;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by mercury-one on 1/4/16.
 */
public class CustomButton extends Button {


    public CustomButton(Context context) {

        super(context);

    }

    public CustomButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(attrs);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        if (attrs != null) {
           /* Typeface myTypeface = Typeface.createFromAsset(getContext().getAssets(), "BauhausCTT.ttf");
            setTypeface(myTypeface);*/
        }
    }
}
