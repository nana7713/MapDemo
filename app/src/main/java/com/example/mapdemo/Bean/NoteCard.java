package com.example.mapdemo.Bean;

import android.widget.ImageView;

public class NoteCard {
    long cardID;
    String user_name;
    String user_slogan;
    String note_title;
    String note_content;
    String user_avatar;
    String cover;
    String create_time;
    double longitude,latitude;
    boolean isDirect;

    public NoteCard(long cardID, String user_name, String user_slogan, String note_title, String note_content, String user_avatar, String cover,String create_time,double longitude,double latitude,boolean isDirect) {
        this.cardID = cardID;
        this.user_name = user_name;
        this.user_slogan = user_slogan;
        this.note_title = note_title;
        this.note_content = note_content;
        this.user_avatar = user_avatar;
        this.cover = cover;
        this.create_time=create_time;
        this.longitude = longitude;
        this.latitude = latitude;
        this.isDirect=isDirect;
    }

    public long getCardID() {
        return cardID;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public boolean isDirect() {
        return isDirect;
    }

    public void setDirect(boolean direct) {
        isDirect = direct;
    }

    public void setCardID(long cardID) {
        this.cardID = cardID;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_slogan() {
        return user_slogan;
    }

    public void setUser_slogan(String user_slogan) {
        this.user_slogan = user_slogan;
    }

    public String getNote_title() {
        return note_title;
    }

    public void setNote_title(String note_title) {
        this.note_title = note_title;
    }

    public String getNote_content() {
        return note_content;
    }

    public void setNote_content(String note_content) {
        this.note_content = note_content;
    }

    public String getUser_avatar() {
        return user_avatar;
    }

    public void setUser_avatar(String user_avatar) {
        this.user_avatar = user_avatar;
    }

    public String getCover() {
        return cover;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    @Override
    public String toString() {
        return "NoteCard{" +
                "cardID=" + cardID +
                ", user_name='" + user_name + '\'' +
                ", user_slogan='" + user_slogan + '\'' +
                ", note_title='" + note_title + '\'' +
                ", note_content='" + note_content + '\'' +
                ", user_avatar='" + user_avatar + '\'' +
                ", cover='" + cover + '\'' +
                ", create_time='" + create_time + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", isDirect=" + isDirect +
                '}';
    }
}
