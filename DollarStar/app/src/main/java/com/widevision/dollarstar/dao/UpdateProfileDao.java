package com.widevision.dollarstar.dao;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.widevision.dollarstar.util.Constant;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

public class UpdateProfileDao extends QueryManager<GsonClass> {

    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    RequestBody requestBody;

    public UpdateProfileDao(String user_id, String name, String gender, String contact, File image) {

     /*   tag:updateProfile
                (user_id ,firstName,lastName, phone, gender, profilePic)*/

        requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("tag", "updateProfile")
                .addFormDataPart("user_id", user_id)
                .addFormDataPart("first_name", name)
                .addFormDataPart("last_name", "")
                .addFormDataPart("gender", gender)
                .addFormDataPart("phone", contact)
                .addFormDataPart("profilePic", "profile_pic.png", RequestBody.create(MEDIA_TYPE_PNG, image))
                .build();
    }

    public UpdateProfileDao(String user_id, String name, String gender, String contact) {

     /*   tag:updateProfile
                (firstName,lastName, phone, gender, profilePic)*/

        requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("tag", "updateProfile")
                .addFormDataPart("user_id", user_id)
                .addFormDataPart("firstName", name)
                .addFormDataPart("lastName", "")
                .addFormDataPart("gender", gender)
                .addFormDataPart("phone", contact)
                .build();
    }

    @Override
    protected Request.Builder buildRequest() {

        Request.Builder request = new Request.Builder();
        request.url(Constant.URL).post(requestBody).build();
        return request;
    }

    @Override
    protected GsonClass parseResponse(String jsonResponse) {

        GsonClass agents = null;
        try {
            Gson gson = new GsonBuilder().create();
            agents = gson.fromJson(jsonResponse, GsonClass.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return agents;
    }
}