package com.widevision.dollarstar.dao;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.widevision.dollarstar.util.Constant;

import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by widevision on 5/2/15.
 */
public class SearchDao extends QueryManager<SearchGsonClass> {

    String user_id, search;

    public SearchDao(String user_id, String search) {

        this.user_id = user_id;
        this.search = search;
        /*tag:search_people
                (user_id, search)*/
    }

    @Override
    protected Request.Builder buildRequest() {

        RequestBody formBody = new FormBody.Builder()
                .add("tag", "search_people")
                .add("user_id", user_id)
                .add("search", search)
                .build();
        Request.Builder request = new Request.Builder();
        request.url(Constant.URL).post(formBody).build();

        return request;
    }

    @Override
    protected SearchGsonClass parseResponse(String jsonResponse) {

        Log.e("", "responce --- " + jsonResponse);

        SearchGsonClass agents = null;
        try {
            Gson gson = new GsonBuilder().create();
            agents = gson.fromJson(jsonResponse, SearchGsonClass.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return agents;
    }
}