package com.widevision.dollarstar.dao;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.widevision.dollarstar.util.Constant;

import java.io.File;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by widevision on 5/2/15.
 */
public class SetPostDao extends QueryManager<UploadGsonClass> {
    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/jpg");
    String user_id, lat, lng, address, friendStr;
    File post_imgage;

    public SetPostDao(String user_id, File post_imgage, String lat, String lng, String address, String friendStr) {

        this.user_id = user_id;
        this.post_imgage = post_imgage;
        this.lat = lat;
        this.lng = lng;
        this.address = address;
        this.friendStr = friendStr;

/* tag:set_post
(user_id,post_imgage, lat,lng,address, tag_user)*/
    }

    @Override
    protected Request.Builder buildRequest() {


        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("tag", "set_post")
                .addFormDataPart("user_id", user_id)
                .addFormDataPart("lat", lat)
                .addFormDataPart("lng", lng)
                .addFormDataPart("address", address)
                .addFormDataPart("tag_user", friendStr)
                .addFormDataPart("post_imgage", "post_imgage.jpg", RequestBody.create(MEDIA_TYPE_PNG, post_imgage))
                .build();


        Request.Builder request = new Request.Builder();
        request.url(Constant.URL).post(requestBody).build();

        return request;
    }

    @Override
    protected UploadGsonClass parseResponse(String jsonResponse) {

        Log.e("", "responce --- " + jsonResponse);

        UploadGsonClass agents = null;
        try {
            Gson gson = new GsonBuilder().create();
            agents = gson.fromJson(jsonResponse, UploadGsonClass.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return agents;
    }

}
