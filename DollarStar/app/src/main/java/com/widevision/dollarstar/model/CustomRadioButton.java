package com.widevision.dollarstar.model;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.RadioButton;

/**
 * Created by mercury-one on 1/4/16.
 */
public class CustomRadioButton extends RadioButton {


    public CustomRadioButton(Context context) {

        super(context);

    }

    public CustomRadioButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(attrs);
    }

    public CustomRadioButton(Context context, AttributeSet attrs) {
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
