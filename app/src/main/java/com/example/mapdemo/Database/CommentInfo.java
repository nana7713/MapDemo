package com.example.mapdemo.Database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "comments",
        indices = {@Index(value = {"post_id"}),
                @Index(value = {"user_id"})},
        foreignKeys = {
            @ForeignKey(
                        entity = NoteEntity.class,
                        parentColumns = "id",
                        childColumns = "post_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = User.class,
                        parentColumns = "uid",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                )
        }

)
public class CommentInfo {
    //@PrimaryKey(autoGenerate = true)
    @PrimaryKey
    private long comment_id;//评论id
    private long post_id;//帖子id
    private int user_id;//评论的用户id
    private String username;//评论的用户名
    private long timestamp;//评论的时间戳
    private Long parentcomment_id;//父评论的id
    private boolean isSynced;//是否同步到服务器
    private String comment_content;//评论的内容
    private String avatar; // 新增头像字段
    public CommentInfo()
    {

    }
    public CommentInfo(long post_id, String comment_content, int user_id) {
        this.post_id = post_id;
        this.comment_content = comment_content;
        this.user_id = user_id;
        this.timestamp = System.currentTimeMillis(); // 设置当前时间戳
        this.isSynced = false; // 默认未同步
    }
   public CommentInfo(long comment_id, long post_id, int user_id, String username, long timestamp, Long parentcomment_id, boolean isSynced, String comment_content) {
        this.comment_id = comment_id;
        this.post_id = post_id;
        this.user_id = user_id; // 初始化 user_id
        this.username = username;
        this.timestamp = timestamp;
        this.parentcomment_id = parentcomment_id;
        this.isSynced = isSynced;
        this.comment_content = comment_content;
    }
    public long getComment_id() {
        return comment_id;
    }
    public long getPost_id() {
        return post_id;
    }
    public String getUsername() {
        return username;
    }
    public String getComment_content() {
        return comment_content;
    }
    public int getUser_id() {
        return user_id;
    }
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getParentcomment_id() {
        return parentcomment_id;
    }
    public void setParentcomment_id(Long parentcomment_id) {
        this.parentcomment_id = parentcomment_id;
    }
    public boolean isSynced() {
        return isSynced;
    }
    public void setSynced(boolean isSynced) {
        this.isSynced = isSynced;
    }
    public void setComment_id(long comment_id) {
        this.comment_id = comment_id;
    }
    public void setPost_id(long post_id) {
        this.post_id = post_id;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setComment_content(String comment_content) {
        this.comment_content = comment_content;
    }

    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "CommentInfo{" +
                "comment_id=" + comment_id +
                ", post_id=" + post_id +
                ", user_id=" + user_id +
                ", username='" + username + '\'' +
                ", timestamp=" + timestamp +
                ", parentcomment_id=" + parentcomment_id +
                ", isSynced=" + isSynced +
                ", comment_content='" + comment_content + '\'' +
                '}';
    }

}
