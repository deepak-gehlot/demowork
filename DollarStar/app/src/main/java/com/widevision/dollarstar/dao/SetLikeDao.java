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
public class SetLikeDao extends QueryManager<UploadGsonClass> {

    String user_id, post_id;

    public SetLikeDao(String user_id, String post_id) {

        this.user_id = user_id;
        this.post_id = post_id;

/*tag:set_like
(user_id,post_id)*/
    }

    @Override
    protected Request.Builder buildRequest() {


        RequestBody formBody = new FormBody.Builder()
                .add("tag", "set_like")
                .add("user_id", user_id)
                .add("post_id", post_id)
                .build();
        Request.Builder request = new Request.Builder();
        request.url(Constant.URL).post(formBody).build();

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
