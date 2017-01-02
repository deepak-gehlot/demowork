package com.widevision.dollarstar.dao;

import java.util.ArrayList;

public class SearchGsonClass {

    public String success;
    public String id;
    public String message;
    public ArrayList<Data> data;

    public class Data {
        public String user_id;
        public String frist_name;
        public String profilePic;
    }
}
