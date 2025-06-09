package com.example.mapdemo.Database;

import androidx.room.*;

@Entity(tableName = "note",indices = {@Index(value = {"user_name"}),            // 为userId列添加索引
        @Index(value = {"user_id"}) },foreignKeys = @ForeignKey(
        entity = User.class, parentColumns = "uid", childColumns = "user_id",onDelete =ForeignKey.CASCADE
        ))
public class NoteEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "user_name")
    public String user_name;
    @ColumnInfo(name = "user_id")
    public int user_id;
    @ColumnInfo(name = "slogan")
    public String slogan;
    @ColumnInfo(name = "content")
    public String content;
    @ColumnInfo(name = "title")
    public String title;
    @ColumnInfo(name = "note_image_uri")
    public String note_image_uri;
    @ColumnInfo(name = "create_time")
    public String create_time;
    @ColumnInfo(name = "avatar_uri")
    public String avatar_uri;
    @ColumnInfo(name = "longitude")
    public double longitude;
    @ColumnInfo(name = "latitude")
    public double latitude;
    @ColumnInfo(name="isDirect")
    public boolean isDirect;
    @ColumnInfo(name="poi_id")
    public String poi_id;

    public NoteEntity(String user_name, int user_id, String slogan, String content, String title, String note_image_uri, String create_time, String avatar_uri, double longitude, double latitude, boolean isDirect, String poi_id) {
        this.user_name = user_name;
        this.user_id = user_id;
        this.slogan = slogan;
        this.content = content;
        this.title = title;
        this.note_image_uri = note_image_uri;
        this.create_time = create_time;
        this.avatar_uri = avatar_uri;
        this.longitude = longitude;
        this.latitude = latitude;
        this.isDirect = isDirect;
        this.poi_id= poi_id;
    }

    public String getPoi_id() {
        return poi_id;
    }

    @Override
    public String toString() {
        return "NoteEntity{" +
                "id=" + id +
                ", user_name='" + user_name + '\'' +
                ", userId=" + user_id +
                ", slogan='" + slogan + '\'' +
                ", content='" + content + '\'' +
                ", title='" + title + '\'' +
                ", note_image_uri='" + note_image_uri + '\'' +
                ", create_time='" + create_time + '\'' +
                ", avatar_uri='" + avatar_uri + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", isDirect=" + isDirect +
                ", PoiId='" + poi_id + '\'' +
                '}';
    }

    public void setPoi_id(String poiId) {
        poi_id = poiId;
    }

    public boolean isDirect() {
        return isDirect;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public void setDirect(boolean direct) {
        isDirect = direct;
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

    public void setUserId(int user_id) {
        this.user_id = user_id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getSlogan() {
        return slogan;
    }

    public void setSlogan(String slogan) {
        this.slogan = slogan;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote_image_uri() {
        return note_image_uri;
    }

    public void setNote_image_uri(String note_image_uri) {
        this.note_image_uri = note_image_uri;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public String getAvatar_uri() {
        return avatar_uri;
    }

    public void setAvatar_uri(String avatar_uri) {
        this.avatar_uri = avatar_uri;
    }

}