package com.example.mapdemo.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
@Dao
public interface CommentDao {
    @Insert
    long insertComment(CommentInfo comment);
    @Query("SELECT * FROM comments WHERE post_id= :post_id ORDER BY timestamp DESC")
    List<CommentInfo> getCommentsByPostId(long post_id);//根据帖子id获取评论列表
    @Query("UPDATE comments SET isSynced = 1 WHERE comment_id = :commentId")
    void markAsSynced(long commentId);//
    @Query("SELECT * FROM comments WHERE isSynced = 0")
    List<CommentInfo> getUnsyncedComments();//
    @Query("DELETE FROM comments WHERE comment_id = :commentId")
    void deleteCommentById(long commentId);
    @Query("SELECT COUNT(*) FROM comments WHERE post_id = :post_id")
    int getCommentCountByPostId(long post_id);
    @Query("SELECT * FROM comments WHERE parentcomment_id = :parentId")
    List<CommentInfo> getChildComments(long parentId);
}
