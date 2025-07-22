package com.example.mapdemo.frame;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.clusterutil.ui.Comment;
import com.example.mapdemo.Adapter.CommentsAdapter;
import com.example.mapdemo.Database.CommentDao;
import com.example.mapdemo.Database.CommentInfo;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.Database.UserDao;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentFragment extends Fragment {
    private CommentDao commentDao;
    private long noteId;
    private EditText etComment;
    private Button btnSubmit;
    private RecyclerView rvComments;
    private CommentsAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etComment = view.findViewById(R.id.et_comment_input);
        btnSubmit = view.findViewById(R.id.btn_send);
        rvComments = view.findViewById(R.id.rv_comment);

        rvComments.setLayoutManager(new LinearLayoutManager(getContext()));
        Runnable refreshCallback = () -> {
            requireActivity().runOnUiThread(() -> {
                loadComments();
            });
        };

        adapter = new com.example.mapdemo.Adapter.CommentsAdapter(
                new ArrayList<>(),
                noteId,
                (entities, userDao) -> {
                    Map<Long, List<CommentInfo>> repliesMap = new HashMap<>();
                    Map<Integer, String> userIdToName = new HashMap<>();
                    List<User> users = userDao.getAll();
                    for (User user : users) {
                        userIdToName.put(user.getUid(), user.getName());
                    }
                    for (CommentInfo entity : entities) {
                        Long parentId = entity.getParentcomment_id();
                        if (parentId != null) {
                            repliesMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(entity);
                        }
                    }
                    List<Comment> rootComments = new ArrayList<>();
                    for (CommentInfo entity : entities) {
                        if (entity.getParentcomment_id() == null) {
                            String userName = userIdToName.getOrDefault(entity.getUser_id(), "未知用户");
                            Comment comment = Comment.buildHierarchy(entity, userName, repliesMap, userIdToName);
                            rootComments.add(comment);
                        }
                    }
                    return rootComments;
                },
                //() -> {},
                refreshCallback, // 传递刷新回调
                null // 传递null，CommentFragment不需要递归删除
        );
        rvComments.setAdapter(adapter);
        rvComments.setItemAnimator(null);
        btnSubmit.setOnClickListener(v -> {
            String content = etComment.getText().toString().trim();
            if (!content.isEmpty()) {
                addComment(content);
                etComment.setText("");
            }
        });

        loadComments();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        commentDao = MapApp.getAppDb().commentDao();

        if (getArguments() != null) {
            noteId = getArguments().getLong("note_id", -1);
        }
    }

    private void loadComments() {
        new Thread(() -> {
            // 1. 从数据库获取CommentInfo实体
            List<CommentInfo> commentInfos = commentDao.getCommentsByPostId(noteId);

            // 2. 转换为UI展示用的Comment对象
            List<Comment> uiComments = convertToUiModel(commentInfos);

            requireActivity().runOnUiThread(() -> {
                // 3. 更新UI
                if (adapter != null) {
                    adapter.updateComments(uiComments);
                }
            });
        }).start();
    }

    private List<Comment> convertToUiModel(List<CommentInfo> commentInfos) {
        // 预加载用户ID到用户名的映射
        Map<Integer, String> userIdToNameMap = preloadUserNames();

        // 构建回复映射（父评论ID -> 子评论列表）
        Map<Long, List<CommentInfo>> repliesMap = new HashMap<>();
        for (CommentInfo comment : commentInfos) {
            Long parentId = comment.getParentcomment_id();
            if (parentId != null) {
                repliesMap.computeIfAbsent(parentId, k -> new ArrayList<>()).add(comment);
            }
        }

        // 构建顶级评论（无父评论）
        List<Comment> topLevelComments = new ArrayList<>();
        for (CommentInfo comment : commentInfos) {
            if (comment.getParentcomment_id() == null) {
                String userName = userIdToNameMap.getOrDefault(comment.getUser_id(), "未知用户");
                Comment uiComment = Comment.buildHierarchy(
                        comment,
                        userName,
                        repliesMap,
                        userIdToNameMap
                );
                topLevelComments.add(uiComment);
            }
        }
        return topLevelComments;
    }

    private Map<Integer, String> preloadUserNames() {
        // 动态查询用户表，获取所有用户id和name
        Map<Integer, String> userMap = new HashMap<>();
        UserDao userDao = MapApp.getAppDb().userDao();
        List<User> users = userDao.getAll();
        for (User user : users) {
            if (user.getName() != null && !user.getName().isEmpty()) {
                userMap.put(user.getUid(), user.getName());
            } else if (user.getAccount() != null) {
                userMap.put(user.getUid(), user.getAccount());
            }
        }
        return userMap;
    }

    private void addComment(String content) {
        new Thread(() -> {
            int userId = MapApp.getUserID();

            // 1. 创建数据库实体CommentInfo
            CommentInfo newComment = new CommentInfo(noteId, content, userId);
            newComment.setUsername(getCurrentUserName()); // 设置用户名

            // 2. 保存到数据库
            commentDao.insertComment(newComment);

            requireActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), "评论添加成功", Toast.LENGTH_SHORT).show();
                loadComments(); // 刷新评论列表
            });
        }).start();
    }

    private String getCurrentUserName() {
        // 查询当前用户信息
        UserDao userDao = MapApp.getAppDb().userDao();
        User user = userDao.findById(MapApp.getUserID());
        if (user != null && user.getName() != null && !user.getName().isEmpty()) {
            return user.getName();
        } else if (user != null && user.getAccount() != null) {
            return user.getAccount();
        } else {
            return "未知用户";
        }
    }
    //Button btnTestMode = findViewById(R.id.btn_comment);
}