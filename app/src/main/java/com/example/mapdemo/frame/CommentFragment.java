package com.example.mapdemo.frame;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.clusterutil.ui.Comment;
import com.example.mapdemo.Adapter.CommentsAdapter;
import com.example.mapdemo.ApiService;
import com.example.mapdemo.Database.CommentDao;
import com.example.mapdemo.Database.CommentInfo;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.Database.UserDao;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;
import com.example.mapdemo.RetrofitClient;
import com.example.mapdemo.ViewModel.MyViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentFragment extends Fragment {
    private CommentDao commentDao;
    private long noteId;
    private EditText etComment;
    private Button btnSubmit;
    private RecyclerView rvComments;
    private CommentsAdapter adapter;
    private ApiService apiService;
    private MyViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //初始化api服务
        apiService = RetrofitClient.getClient().create(ApiService.class);
        viewModel = new ViewModelProvider(requireActivity()).get(MyViewModel.class);

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
                null,// 传递null，CommentFragment不需要递归删除
                viewModel
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

        viewModel.getCommentLiveData().observe(getViewLifecycleOwner(), new Observer<List<CommentInfo>>() {
            @Override
            public void onChanged(List<CommentInfo> commentInfos) {
                if (commentInfos != null) {
                    // 将CommentInfo转换为UI展示用的Comment对象
                    List<Comment> uiComments = convertToUiModel(commentInfos);
                    adapter.updateComments(uiComments);
                }
            }
        });
        // 观察用户数据变化
        viewModel.getUserLiveData().observe(getViewLifecycleOwner(), new Observer<User>() {
            @Override
            public void onChanged(User user) {
                // 当用户信息更新时，刷新评论列表
                loadComments();
            }
        });

        loadComments();
        checkAndSyncUnsyncedComments();
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
            viewModel.getCommentsByPostId(noteId);
            // 设置超时处理
            new android.os.Handler().postDelayed(() -> {
                // 如果5秒后还没有数据，从本地数据库加载
                if (viewModel.getCommentLiveData().getValue() == null) {
                    List<CommentInfo> commentInfos = commentDao.getCommentsByPostId(noteId);

                    // 2. 转换为UI展示用的Comment对象
                    List<Comment> uiComments = convertToUiModel(commentInfos);

                    requireActivity().runOnUiThread(() -> {
                        viewModel.getCommentLiveData().setValue(commentInfos);
                        // 3. 更新UI
                        if (adapter != null) {
                            adapter.updateComments(uiComments);
                        }
                    });
                }
            }, 5000);
            // 1. 从数据库获取CommentInfo实体

        }).start();
    }
    private void checkAndSyncUnsyncedComments() {
        new Thread(() -> {
            // 获取所有未同步的评论
            List<CommentInfo> unsyncedComments = commentDao.getUnsyncedComments();

            if (!unsyncedComments.isEmpty()) {
                Log.d("CommentSync", "发现未同步评论: " + unsyncedComments.size() + "条");

                // 批量同步评论到服务器
                Call<List<CommentInfo>> call = apiService.syncCommentsBatch(unsyncedComments);
                call.enqueue(new Callback<List<CommentInfo>>() {
                    @Override
                    public void onResponse(Call<List<CommentInfo>> call, Response<List<CommentInfo>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            new Thread(() -> {
                                // 标记已同步的评论
                                for (CommentInfo comment : response.body()) {
                                    commentDao.markAsSynced(comment.getComment_id());
                                }
                                Log.d("CommentSync", "批量同步成功");
                            }).start();
                        } else {
                            Log.e("CommentSync", "批量同步失败: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<List<CommentInfo>> call, Throwable t) {
                        Log.e("CommentSync", "批量同步网络错误: " + t.getMessage());

                    }
                });
            }
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
            User user = getCurrentUser();  // 使用全局用户

            CommentInfo newComment = new CommentInfo(noteId, content, user.getUid());
            newComment.setUsername(user.getName());
            newComment.setAvatar(user.getAvatar());

            long commentId = commentDao.insertComment(newComment);
            newComment.setComment_id(commentId);

            // 添加到当前列表
            List<CommentInfo> currentComments = new ArrayList<>();
            if (viewModel.getCommentLiveData().getValue() != null) {
                currentComments.addAll(viewModel.getCommentLiveData().getValue());
            }
            currentComments.add(newComment);

            requireActivity().runOnUiThread(() -> {
                viewModel.getCommentLiveData().setValue(currentComments);
                adapter.updateComments(convertToUiModel(currentComments));
            });

            // 异步上传到服务器
            Call<CommentInfo> call = apiService.createComment(newComment);
            call.enqueue(new Callback<CommentInfo>() {
                @Override
                public void onResponse(Call<CommentInfo> call, Response<CommentInfo> response) {
                    if (response.isSuccessful()) {
                        new Thread(() -> {
                            commentDao.markAsSynced(newComment.getComment_id());
                        }).start();
                    }
                }

                @Override
                public void onFailure(Call<CommentInfo> call, Throwable t) {
                    // 失败处理
                }
            });
        }).start();

    }
    private User getCurrentUser() {
        // 1. 尝试从ViewModel获取用户信息（来自服务器）
        User user = viewModel.getUserLiveData().getValue();
        if (user != null) {
            return user;
        }

        // 2. 尝试从本地数据库获取
        UserDao userDao = MapApp.getAppDb().userDao();
        user = userDao.findById(MapApp.getUserID());
        if (user != null) {
            return user;
        }

        // 3. 创建默认用户（当服务器和本地都不可用时）
        User defaultUser = new User("用户" + MapApp.getUserID(),  // account 参数
                "" );
        defaultUser.setUid(MapApp.getUserID());
        defaultUser.setAccount("用户" + MapApp.getUserID());
        defaultUser.setName("用户" + MapApp.getUserID());
        return defaultUser;
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