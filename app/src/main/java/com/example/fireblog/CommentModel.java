package com.example.fireblog;

public class CommentModel {
    public CommentModel(){}
    String comment;
    String date;
    String username;
    String user_photo;
    String postid;

    public CommentModel(String comment, String date, String username, String user_photo,String postid) {
        this.comment = comment;
        this.date = date;
        this.username = username;
        this.user_photo = user_photo;
        this.postid = postid;
    }

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
    }


    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public String getUser_photo() {
        return user_photo;
    }

    public void setUser_photo(String user_photo) {
        this.user_photo = user_photo;
    }
}
