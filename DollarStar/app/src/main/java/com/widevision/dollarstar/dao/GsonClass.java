package com.widevision.dollarstar.dao;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class GsonClass {

    public String success;
    public String id;
    public String message;
    public String code;
    public String phone;
    public String email;
    public String name;
    public String gender;
    public String country;
    public Data data;


    public class Data {
        /* "nickname": "android1",
        "first_name": "android",
        "last_name": "",
        "phone": "1234567890",
        "gender": "Male"*/

        public String nickname;
        public String first_name;
        public String last_name;
        public String gender;
        public String phone;
        public String profilePic;
        public String following;
        public String followers;
    }
}
