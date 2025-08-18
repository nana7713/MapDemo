package com.example.mapdemo.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStoreOwner;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.mapapi.clusterutil.ui.Comment;
import com.example.mapdemo.ApiService;
import com.example.mapdemo.Database.AppDatabase;
import com.example.mapdemo.Database.CommentDao;
import com.example.mapdemo.Database.CommentInfo;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.Database.UserDao;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;
import com.example.mapdemo.RetrofitClient;
import com.example.mapdemo.ViewModel.MyViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.ViewHolder>{
    private List<Comment> comments;
    private long noteId;
    private java.util.function.BiFunction<List<com.example.mapdemo.Database.CommentInfo>, com.example.mapdemo.Database.UserDao, List<com.baidu.mapapi.clusterutil.ui.Comment>> buildHierarchyFunc;
    private Runnable onCommentChanged;
    private MyAdapter myAdapter;
    private ApiService apiService;
    private MyViewModel viewModel;

    public CommentsAdapter(List<Comment> comments, long noteId, BiFunction<List<CommentInfo>, UserDao, List<Comment>> buildHierarchyFunc, Runnable onCommentChanged, MyAdapter myAdapter,MyViewModel viewModel) {
        this.comments = comments;
        this.noteId = noteId;
        this.buildHierarchyFunc = buildHierarchyFunc;
        this.onCommentChanged = onCommentChanged;
        this.myAdapter = myAdapter;
        apiService = RetrofitClient.getClient().create(ApiService.class);
//        if (MapApp.getAppDb() != null) {
//            viewModel = new ViewModelProvider((ViewModelStoreOwner) MapApp.getAppDb()).get(MyViewModel.class);
//        }
        this.viewModel=viewModel;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        // 加载头像
        if (comment.getAvatar() != null && !comment.getAvatar().isEmpty()) {
            // 如果有Glide依赖，优先用Glide加载网络图片
            try {
                com.bumptech.glide.Glide.with(holder.itemView.getContext())
                        .load(comment.getAvatar())
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(holder.ivAvatar);
            } catch (Exception e) {
                holder.ivAvatar.setImageResource(R.drawable.default_avatar);
            }
        } else {
            holder.ivAvatar.setImageResource(R.drawable.default_avatar);
        }
        // 确保同时设置用户名和内容
        holder.tvUserName.setText(comment.getUsername() + ":");
        holder.tvContent.setText(comment.getContent());

        // 子回复：点击评论内容弹出BottomSheet
        holder.tvContent.setOnClickListener(v -> {
            showReplyBottomSheet(holder.itemView.getContext(), comment.getCommentId());
        });
        // 使用新的绑定方法更新嵌套适配器
        holder.bindReplies(
                comment.getReplies(),
                noteId,
                buildHierarchyFunc,
                onCommentChanged,
                myAdapter
        );

//        // 关键修复：正确处理嵌套RecyclerView的Adapter和可见性
//        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
//            holder.rvReplies.setVisibility(View.VISIBLE);
//            CommentsAdapter repliesAdapter = new CommentsAdapter(
//                comment.getReplies(), noteId, buildHierarchyFunc, onCommentChanged, myAdapter
//            );
//            holder.rvReplies.setAdapter(repliesAdapter);
//        } else {
//            holder.rvReplies.setVisibility(View.GONE);
//            holder.rvReplies.setAdapter(null); // 关键：必须清空Adapter，防止视图复用时显示旧数据
//        }
//        // 更新嵌套适配器的数据
//        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
//            holder.rvReplies.setVisibility(View.VISIBLE);
//            holder.repliesAdapter.updateComments(comment.getReplies());
//        } else {
//            holder.rvReplies.setVisibility(View.GONE);
//            holder.repliesAdapter.updateComments(new ArrayList<>());
//        }

        // 删除按钮逻辑
        holder.btnDelete.setVisibility(View.GONE);
        int currentUserId = com.example.mapdemo.MapApp.getUserID();
        if (comment.getUser_id() == currentUserId) {
            holder.btnDelete.setVisibility(View.VISIBLE);
        }

//        holder.btnDelete.setOnClickListener(v -> {
//            new Thread(() -> {
//                // 递归删除评论及其所有子评论
//                if (myAdapter != null) {
//                    myAdapter.deleteCommentAndChildren(comment.getCommentId());
//                } else {
//                    com.example.mapdemo.Database.AppDatabase db = com.example.mapdemo.MapApp.getAppDb();
//                    db.commentDao().deleteCommentById(comment.getCommentId());
//                }
//
////                // 重新查询并刷新评论树
//                com.example.mapdemo.Database.AppDatabase db = com.example.mapdemo.MapApp.getAppDb();
//                List<com.example.mapdemo.Database.CommentInfo> newEntities = db.commentDao().getCommentsByPostId(noteId);
//                List<com.baidu.mapapi.clusterutil.ui.Comment> newComments = buildHierarchyFunc.apply(newEntities, db.userDao());
//                //((android.app.Activity) holder.itemView.getContext()).runOnUiThread(() -> {
//                ((android.app.Activity) holder.itemView.getContext()).runOnUiThread(() -> {
//                updateComments(newComments);
//                    if (onCommentChanged != null) onCommentChanged.run();
//                });
//            }).start();
//        });


//        holder.btnDelete.setOnClickListener(v -> {
//            int currentPosition = holder.getAdapterPosition();
//            if (currentPosition == RecyclerView.NO_POSITION) return;
//
//            Comment commentToDelete = comments.get(currentPosition);
//
//            new Thread(() -> {
//                // 递归删除评论及其所有子评论
//                if (myAdapter != null) {
//                    myAdapter.deleteCommentAndChildren(commentToDelete.getCommentId());
//                } else {
//                    AppDatabase db = MapApp.getAppDb();
//                    db.commentDao().deleteCommentById(commentToDelete.getCommentId());
//                }
//
//                // 在后台重新加载数据
//                AppDatabase db = MapApp.getAppDb();
//                List<CommentInfo> newEntities = db.commentDao().getCommentsByPostId(noteId);
//                List<Comment> newComments = buildHierarchyFunc.apply(newEntities, db.userDao());
//
//                // 更新UI
//                ((android.app.Activity) holder.itemView.getContext()).runOnUiThread(() -> {
//                    // 使用优化的更新方法
//                    updateComments(newComments);
//
//                    if (onCommentChanged != null) {
//                        onCommentChanged.run();
//                    }
//                });
//            }).start();
//        });
        holder.btnDelete.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            Comment commentToDelete = comments.get(currentPosition);
            // 显示确认对话框
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("删除评论")
                    .setMessage("确定要删除这条评论吗？")
                    .setPositiveButton("删除", (dialog, which) -> {
                        // 直接调用ViewModel的删除方法
                        deleteCommentAndChildren(commentToDelete, position,holder.itemView.getContext());
                    })
                    .setNegativeButton("取消", null)
                    .show();
//            // 保存被删除评论的ID和位置
//            long deletedCommentId = commentToDelete.getCommentId();
//            int deletedPosition = currentPosition;
//
//            new Thread(() -> {
//                // 递归删除评论及其所有子评论
//                deleteCommentAndChildren(commentToDelete.getCommentId(),holder, deletedPosition);
//
//                // 直接更新当前适配器的数据，而不是重新加载整个树
//                ((Activity) holder.itemView.getContext()).runOnUiThread(() -> {
//                    // 从当前列表中移除被删除的评论
//                    comments.remove(deletedPosition);
//
//                    // 使用更精确的更新方法
//                    notifyItemRemoved(deletedPosition);
//
//                    // 如果删除后列表为空，更新可见性
//                    if (comments.isEmpty()) {
//                        // 尝试找到父ViewHolder
//                        View parent = (View) holder.itemView.getParent();
//                        if (parent != null) {
//                            RecyclerView rvReplies = parent.findViewById(R.id.rv_replies);
//                            if (rvReplies != null) {
//                                rvReplies.setVisibility(View.GONE);
//                            }
//                        }
//                    }
//
//                    if (onCommentChanged != null) {
//                        onCommentChanged.run();
//                    }
//                    // 新增：强制刷新评论数量
//                    if (myAdapter != null) {
//                        myAdapter.refreshCommentCountOnMainThread(noteId);
//                    }
//                });
//            }).start();
        });

    }
    private int findCommentPosition(long commentId) {
        for (int i = 0; i < comments.size(); i++) {
            if (comments.get(i).getCommentId() == commentId) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return comments.size();
    }
    @Override
    public int getItemViewType(int position) {
        // 使用评论ID作为视图类型，确保唯一性
        return (int) comments.get(position).getCommentId();
    }
    class ViewHolder extends RecyclerView.ViewHolder {
        public CommentsAdapter repliesAdapter;
        TextView tvUserName, tvContent;
        RecyclerView rvReplies;
        android.widget.ImageButton btnDelete;
        ImageView ivAvatar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvContent = itemView.findViewById(R.id.tv_content);
            rvReplies = itemView.findViewById(R.id.rv_replies);
            btnDelete = itemView.findViewById(R.id.btn_delete_comment);
            ivAvatar = itemView.findViewById(R.id.iv_avatar);
            // LayoutManager只应被创建一次
            rvReplies.setLayoutManager(new LinearLayoutManager(itemView.getContext()));
            rvReplies.setNestedScrollingEnabled(false);
            rvReplies.setItemViewCacheSize(0); // 禁用缓存
        }
        // 添加绑定子评论的方法
        public void bindReplies(List<Comment> replies, long noteId,
                                java.util.function.BiFunction<List<com.example.mapdemo.Database.CommentInfo>,
                                        com.example.mapdemo.Database.UserDao,
                                        List<com.baidu.mapapi.clusterutil.ui.Comment>> buildHierarchyFunc,
                                Runnable onCommentChanged,
                                MyAdapter myAdapter) {

            if (repliesAdapter == null) {
                repliesAdapter = new CommentsAdapter(
                        new ArrayList<>(),
                        noteId,
                        buildHierarchyFunc,
                        onCommentChanged,
                        myAdapter,
                        viewModel
                );
                rvReplies.setAdapter(repliesAdapter);
            }

            // 更新嵌套适配器的参数
            repliesAdapter.noteId = noteId;
            repliesAdapter.buildHierarchyFunc = buildHierarchyFunc;
            repliesAdapter.onCommentChanged = onCommentChanged;
            repliesAdapter.myAdapter = myAdapter;

            // 直接更新嵌套适配器的数据
            repliesAdapter.updateCommentsDirect(replies);

            rvReplies.setVisibility(replies != null && !replies.isEmpty() ?
                    View.VISIBLE : View.GONE);
        }

    }
    public void updateCommentsDirect(List<Comment> newComments) {
        this.comments = new ArrayList<>(newComments);
        notifyDataSetChanged();
    }
    public void updateComments(List<Comment> newComments) {
        //this.comments = newComments;
//        this.comments = new ArrayList<>(newComments);
//        notifyDataSetChanged(); // 触发 RecyclerView 刷新
        CommentDiffCallback diffCallback = new CommentDiffCallback(this.comments, newComments);
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(diffCallback);

        this.comments.clear();
        this.comments.addAll(newComments);

        diffResult.dispatchUpdatesTo(this);
    }
    // 添加 DiffUtil 回调类
    static class CommentDiffCallback extends DiffUtil.Callback {
        private final List<Comment> oldComments;
        private final List<Comment> newComments;

        public CommentDiffCallback(List<Comment> oldComments, List<Comment> newComments) {
            this.oldComments = oldComments;
            this.newComments = newComments;
        }

        @Override
        public int getOldListSize() {
            return oldComments.size();
        }

        @Override
        public int getNewListSize() {
            return newComments.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldComments.get(oldItemPosition).getCommentId() ==
                    newComments.get(newItemPosition).getCommentId();
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Comment oldComment = oldComments.get(oldItemPosition);
            Comment newComment = newComments.get(newItemPosition);

            return oldComment.getContent().equals(newComment.getContent()) &&
                    oldComment.getUsername().equals(newComment.getUsername()) &&
                    oldComment.getReplies().size() == newComment.getReplies().size();
        }
    }

    private void showReplyBottomSheet(Context context, Long parentCommentId) {
        Activity activity = (Activity) context;
        BottomSheetDialog bottomSheet = new BottomSheetDialog(activity);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.bottom_sheet_comments, null);
        bottomSheet.setContentView(dialogView);
        EditText etInput = dialogView.findViewById(R.id.et_comment_input);
        Button btnSend = dialogView.findViewById(R.id.btn_send);
        // 隐藏评论列表，只显示输入框
        RecyclerView rv = dialogView.findViewById(R.id.rv_comment);
        rv.setVisibility(View.GONE);
        btnSend.setOnClickListener(v -> {
            String content = etInput.getText().toString().trim();
            if (content.isEmpty()) return;
            int userId = MapApp.getUserID();
            new Thread(() -> {
                AppDatabase db = MapApp.getAppDb();
                User user = db.userDao().findById(userId);
                if (user == null) return;
                CommentInfo reply = new CommentInfo();
                reply.setPost_id(noteId);
                reply.setUser_id(userId);
                reply.setComment_content(content);
                reply.setTimestamp(System.currentTimeMillis());
                // 修正parentcomment_id赋值，防止为0导致孤儿评论
                if (parentCommentId != null && parentCommentId != 0) {
                    reply.setParentcomment_id(parentCommentId);
                } else {
                    reply.setParentcomment_id(null);
                }
                reply.setSynced(false);
                reply.setUsername(user.getName() != null && !user.getName().isEmpty() ? user.getName() : user.getAccount());
                reply.setAvatar(user.getAvatar()); // 新增：设置头像字段
                if (parentCommentId != null && parentCommentId != 0) {
                    CommentInfo parentComment = db.commentDao().getCommentById(parentCommentId);
                    if (parentComment == null) {
                        activity.runOnUiThread(() ->
                                Toast.makeText(activity, "回复的评论不存在", Toast.LENGTH_SHORT).show());
                        return;
                    }
                }
                db.commentDao().insertComment(reply);
                if (viewModel != null) {
                    viewModel.syncComment(reply, () -> {
                        // 刷新评论树
                        List<CommentInfo> newEntities = db.commentDao().getCommentsByPostId(noteId);
                        List<Comment> newComments = buildHierarchyFunc.apply(newEntities, db.userDao());
                        activity.runOnUiThread(() -> {
                            updateComments(newComments);
                            bottomSheet.dismiss();
                            // 新增：回调外层刷新评论数量
                            if (onCommentChanged != null) onCommentChanged.run();
                        });
                    });
                } else {
                    // 如果viewModel不可用，使用全局同步
                    MapApp.getInstance().syncUnsyncedComments();
                }
//                // 重新查询并刷新评论树
//                List<com.example.mapdemo.Database.CommentInfo> newEntities = db.commentDao().getCommentsByPostId(noteId);
//                List<com.baidu.mapapi.clusterutil.ui.Comment> newComments = buildHierarchyFunc.apply(newEntities, db.userDao());
//                activity.runOnUiThread(() -> {
//                    updateComments(newComments);
//                    bottomSheet.dismiss();
//                    // 新增：回调外层刷新评论数量
//                    if (onCommentChanged != null) onCommentChanged.run();
//                });
//            }).start();
//        });
//        bottomSheet.show();
                // 创建新的评论对象
                Comment newComment = Comment.fromEntity(reply, reply.getUsername());

                activity.runOnUiThread(() -> {
                    // 找到父评论的位置
                    int parentPosition = findCommentPosition(parentCommentId);

                    if (parentPosition != -1) {
                        // 获取父评论
                        Comment parentComment = comments.get(parentPosition);

                        // 添加新回复
                        if (parentComment.getReplies() == null) {
                            parentComment.setReplies(new ArrayList<>());
                        }
                        parentComment.getReplies().add(newComment);

                        // 更新嵌套适配器
                        notifyItemChanged(parentPosition);
                    }

                    bottomSheet.dismiss();
                    // 新增：回调外层刷新评论数量
                    if (onCommentChanged != null) onCommentChanged.run();
                });
            }).start();
        });
        bottomSheet.show();
    }
    // 新增：递归删除评论及其所有子评论
//    public void deleteCommentAndChildren(long commentId) {
//        CommentDao commentDao = MapApp.getAppDb().commentDao();
//        List<CommentInfo> children = commentDao.getChildComments(commentId);
//        for (CommentInfo child : children) {
//            deleteCommentAndChildren(child.getComment_id());
//        }
//        commentDao.deleteCommentById(commentId);
//    }
    private void deleteCommentAndChildren(Comment comment, int position,Context context) {
        // 1. 收集所有需要删除的评论ID（包括子评论）
        List<Long> commentIds = new ArrayList<>();
        collectCommentIds(comment, commentIds);

        // 2. 从UI中立即移除
        comments.remove(position);
        notifyItemRemoved(position);

        // 3. 在后台删除本地和服务器数据
        new Thread(() -> {
            // 删除本地数据
            CommentDao commentDao = MapApp.getAppDb().commentDao();
            for (long id : commentIds) {
                commentDao.deleteCommentById(id);
            }

            // 删除服务器数据
            deleteCommentsOnServer(commentIds);

            // 刷新评论数量
            if (onCommentChanged != null) {
                ((Activity) context).runOnUiThread(onCommentChanged);
            }
        }).start();
    }

    // 递归收集所有需要删除的评论ID（包括子评论）
    private void collectCommentIds(Comment comment, List<Long> ids) {
        ids.add(comment.getCommentId());
        for (Comment reply : comment.getReplies()) {
            collectCommentIds(reply, ids);
        }
    }

    // 同步删除服务器上的评论
    private void deleteCommentsOnServer(List<Long> commentIds) {
        // 使用ViewModel批量删除（如果可用）
        if (viewModel != null) {
            viewModel.deleteCommentsBatch(commentIds);
        } else {
            // 直接API调用
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            for (long commentId : commentIds) {
                try {
                    // 同步执行删除请求
                    Response<Void> response = apiService.deleteComment(commentId).execute();
                    if (response.isSuccessful()) {
                        Log.d("DELETE_COMMENT", "评论删除成功: " + commentId);
                    } else {
                        Log.e("DELETE_COMMENT", "评论删除失败: " + response.code());
                    }
                } catch (IOException e) {
                    Log.e("DELETE_COMMENT", "网络错误: " + e.getMessage());
                }
            }
        }
    }

}
