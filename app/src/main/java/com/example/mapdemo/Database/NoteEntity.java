package com.example.mapdemo.Database;

import androidx.room.*;

@Entity(tableName = "note",indices = {@Index(value = {"user_name"})},foreignKeys = @ForeignKey(
        entity = User.class, parentColumns = "uid", childColumns = "userId",onDelete =ForeignKey.CASCADE
        ))
public class NoteEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    @ColumnInfo(name = "user_name")
    public String user_name;
    public int userId;
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

    public NoteEntity(String user_name, int userId, String slogan, String content, String title, String note_image_uri, String create_time, String avatar_uri) {
        this.user_name = user_name;
        this.userId = userId;
        this.slogan = slogan;
        this.content = content;
        this.title = title;
        this.note_image_uri = note_image_uri;
        this.create_time = create_time;
        this.avatar_uri = avatar_uri;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
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

    @Override
    public String toString() {
        return "NoteEntity{" +
                "id=" + id +
                ", user_name='" + user_name + '\'' +
                ", slogan='" + slogan + '\'' +
                ", content='" + content + '\'' +
                ", title='" + title + '\'' +
                ", note_image_uri='" + note_image_uri + '\'' +
                ", create_time='" + create_time + '\'' +
                ", avatar_uri='" + avatar_uri + '\'' +
                '}';
    }
    @ColumnInfo(name = "latitude")
    private Double latitude;
    @ColumnInfo(name = "longitude")
    private Double longitude;

    // Getter和Setter方法
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}