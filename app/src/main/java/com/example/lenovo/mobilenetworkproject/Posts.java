package com.example.lenovo.mobilenetworkproject;

public class Posts
{
    public String uid, time, date, postimage, descrption, profileImage, fullname;

    public Posts()
    {

    }

    public Posts(String uid, String time, String date, String postimage, String descrption, String profileImage, String fullname) {
        this.uid = uid;
        this.time = time;
        this.date = date;
        this.postimage = postimage;
        this.descrption = descrption;
        this.profileImage = profileImage;
        this.fullname = fullname;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getPostimage() {
        return postimage;
    }

    public void setPostimage(String postimage) {
        this.postimage = postimage;
    }

    public String getDescrption() {
        return descrption;
    }

    public void setDescrption(String descrption) {
        this.descrption = descrption;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
}
