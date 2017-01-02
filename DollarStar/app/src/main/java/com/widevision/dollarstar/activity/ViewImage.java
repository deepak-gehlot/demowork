package com.widevision.dollarstar.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.androidquery.AQuery;
import com.widevision.dollarstar.R;
import com.widevision.dollarstar.model.TouchImageView;

/**
 * Created by mercury-five on 30/06/16.
 */
public class ViewImage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_image_layout);
        Bundle b = getIntent().getExtras();
        String imageUrl = b.getString("image_url");

        TouchImageView subsamplingScaleImageView = (TouchImageView) findViewById(R.id.imageView);
        AQuery aQuery = new AQuery(this);
        aQuery.id(subsamplingScaleImageView).image(imageUrl, true, true, 0, R.drawable.placeholder);

        findViewById(R.id.action_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
