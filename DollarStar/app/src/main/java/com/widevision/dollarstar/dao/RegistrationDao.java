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

public class RegistrationDao extends QueryManager<GsonClass> {

    private static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    private File profilePic;
    RequestBody requestBody;


    public RegistrationDao(String name, String username, String email, String gender, String password, String contact) {

        /*
tag:registration
(email,username,password,firstName,lastName,phone,gender)*/

        requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("tag", "registration")
                .addFormDataPart("email", email)
                .addFormDataPart("firstName", name)
                .addFormDataPart("lastName", "")
                .addFormDataPart("username", username)
                .addFormDataPart("gender", gender)
                .addFormDataPart("phone", contact)
                .addFormDataPart("password", password)
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