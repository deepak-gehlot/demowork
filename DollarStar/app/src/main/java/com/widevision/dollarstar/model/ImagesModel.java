package com.widevision.dollarstar.model;

import android.graphics.Bitmap;

/**
 * Created by mercury-five on 01/07/16.
 */
public class ImagesModel {

    public String lat;
    public String lng;
    public Bitmap bitmap;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }
}
