package com.example.fireblog;

public class BlogModel {
    String title,body,image,username,date,user_photo,uid;
    public BlogModel(){}

    public BlogModel(String uid,String title, String body, String image,String username,String date,String user_photo) {
        this.title = title;
        this.body = body;
        this.image = image;
        this.username = username;
        this.date = date;
        this.user_photo = user_photo;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUser_photo() {
        return user_photo;
    }

    public void setUser_photo(String user_photo) {
        this.user_photo = user_photo;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
