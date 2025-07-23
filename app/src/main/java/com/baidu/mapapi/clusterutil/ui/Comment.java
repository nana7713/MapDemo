package com.baidu.mapapi.clusterutil.ui;

import com.example.mapdemo.Database.CommentInfo;
import com.example.mapdemo.Database.UserDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Comment {
    private long commentId;
    private String content;
    private String username;
    private long timestamp;//评论的时间戳
    private int user_id;
    private List<Comment> replies=new ArrayList<>();
    private String avatar; // 新增头像字段
    public static Comment fromEntity(CommentInfo commentInfo,String username){
        Comment comment=new Comment();
        comment.setCommentId(commentInfo.getComment_id());
        comment.setContent(commentInfo.getComment_content());
        comment.setUsername(username);
        comment.setTimestamp(commentInfo.getTimestamp());
        comment.setUser_id(commentInfo.getUser_id()); // 新增
        comment.setAvatar(commentInfo.getAvatar()); // 新增
        return comment;
    }
    public static Comment buildHierarchy(//
            CommentInfo entity,
            String userName,
            Map<Long, List<CommentInfo>> repliesMap,
            Map<Integer, String> userIdToNameMap // 预加载的用户ID到用户名的映射
    ) {
        Comment comment = fromEntity(entity, userName);
        List<CommentInfo> childEntities = repliesMap.get(entity.getComment_id());
        if (childEntities != null) {
            for (CommentInfo childEntity : childEntities) {
                // 从预加载的Map中直接获取用户名
                String childUserName = userIdToNameMap.get(childEntity.getUser_id());
                comment.getReplies().add(
                        buildHierarchy(childEntity, childUserName, repliesMap, userIdToNameMap)
                );
            }
        }
        return comment;
    }
    public long getCommentId() {
        return commentId;
    }
    public String getContent() {
        return content;
    }
    public void setContent(String commentContent) {
        this.content = commentContent;
    }

    public void setCommentId(long commentId) {
        this.commentId = commentId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public List<Comment> getReplies() {
        return replies;
    }
    public void setReplies(List<Comment> replies) {
        this.replies = replies;
    }
    // 新增user_id getter/setter
    public int getUser_id() {
        return user_id;
    }
    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getAvatar() {
        return avatar;
    }
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
