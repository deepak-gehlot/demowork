package com.widevision.dollarstar.dao;

import java.io.Serializable;
import java.util.ArrayList;

public class PostGsonClass {

    public String success;
    public String id;
    public String message;
    public String code;
    public String phone;
    public String email;
    public String name;
    public String gender;
    public String country;
    public ArrayList<Data> data;


    public static class Data implements Serializable {
        /*  "ID": "5",
            "post_author": "2",
            "post_date": "2016-06-20 09:27:46",
            "post_content": "test1",
            "post_title": "test1content",
            "post_excerpt": "",
            "post_status": "publish",
            "comment_status": "open",
            "comment_count": "0"*/
        public String ID;
        public String post_author;
        public String post_date;
        public String post_content;
        public String post_title;
        public String post_excerpt;
        public String post_status;
        public String comment_status;
        public String comment_count;
        public String profilePic;
        public String post_like;
        public String first_name;
        public String lat;
        public String lng;


        /*  "comment_ID": "5",
            "comment_post_ID": "5",
            "comment_author": "android",
            "comment_author_email": "",
            "comment_author_url": "",
            "comment_author_IP": "",
            "comment_date": "2016-06-21 08:38:19",
            "comment_date_gmt": "0000-00-00 00:00:00",
            "comment_content": "gd",
            "comment_karma": "0",
            "comment_approved": "1",
            "comment_agent": "",
            "comment_type": "",
            "comment_parent": "0",
            "user_id": "4"*/
        public String comment_ID;
        public String comment_post_ID;
        public String comment_author;
        public String comment_author_email;
        public String comment_author_url;
        public String comment_author_IP;
        public String comment_date;
        public String comment_date_gmt;
        public String comment_content;
        public String comment_karma;
        public String comment_approved;
        public String comment_type;
        public String user_id;
        public boolean isSelected = false;
    }
}
