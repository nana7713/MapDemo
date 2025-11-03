package com.example.mapdemo.Adapter;

import static com.google.android.material.internal.ContextUtils.getActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.mapdemo.ApiService;
import com.example.mapdemo.Bean.NoteCard;
import com.example.mapdemo.Database.NoteDao;
import com.example.mapdemo.Database.NoteEntity;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;
import com.example.mapdemo.RetrofitClient;
import com.example.mapdemo.Database.AppDatabase;
import com.example.mapdemo.Database.CommentDao;
import com.example.mapdemo.Database.CommentInfo;
import com.example.mapdemo.Database.UserDao;
import com.example.mapdemo.Adapter.CommentsAdapter;
import com.baidu.mapapi.clusterutil.ui.Comment;
import com.example.mapdemo.ViewModel.MyViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.EditText;
import android.view.WindowManager;
import android.os.Handler;
import android.os.Looper;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.widget.Toast;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ArrayList;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PoiAdapter extends RecyclerView.Adapter<PoiAdapter.MyViewHolder> {
    private List<NoteEntity> Mlist;
    private Context context;
    private LayoutInflater inflater;
    private int delete_position;
    NoteDao noteDao= MapApp.getAppDb().noteDao();
    FragmentManager fragmentManager;
    CountInterface countInterface;
    FragmentHelper fragmentHelper;
    private MyViewModel viewModel;
    private Map<Long, Integer> commentCountMap = new HashMap<>(); // 存储笔记ID与评论数量的映射
    private Set<Long> requestedNoteIds = new HashSet<>(); // 避免重复请求

    public PoiAdapter(List<NoteEntity> mlist, Context context,CountInterface countInterface,FragmentHelper fragmentHelper,MyViewModel viewModel) {
        Collections.reverse(mlist);
        Mlist = mlist;
        this.context = context;
        inflater= LayoutInflater.from(context);
        this.countInterface=countInterface;
        this.fragmentHelper=fragmentHelper;
        this.viewModel = viewModel;
    }

    public PoiAdapter(List<NoteEntity> mList, FragmentActivity activity, CountInterface countInterface, MyAdapter.FragmentHelper fragmentHelper) {
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.item_layout,parent,false);
        return new MyViewHolder(view);
    }

    @NonNull
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.position.setText(Mlist.get(position).getLongitude() +" "+ Mlist.get(position).getLatitude());
        holder.userName.setText(Mlist.get(position).getUser_name());
        holder.slogan.setText(Mlist.get(position).getSlogan());
        holder.title.setText(Mlist.get(position).getTitle());
        holder.content.setText(Mlist.get(position).getContent());
        Glide.with(holder.itemView.getContext()).load(Mlist.get(position).getAvatar_uri()).into(holder.avatar);
        //Glide.with(context).load(Mlist.get(position).getCover()).into(holder.cover);
//        InputStream inputStream= null;
//        try {
//            inputStream = context.getContentResolver().openInputStream(Uri.parse(Mlist.get(position).getCover()));
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
        // 处理封面图片加载，添加异常捕获
        String coverUri=Mlist.get(position).getNote_image_thumbnail_uri();
        if (coverUri == null ||coverUri.isEmpty())
        {
            coverUri = Mlist.get(position).getNote_image_uri();
        }
        if (coverUri != null && !coverUri.isEmpty()) {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(Uri.parse(coverUri));
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                holder.cover.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Glide.with(context)
                    .load(Uri.parse(coverUri))
                    .override(800, 800) // 限制分辨率
                    .format(DecodeFormat.PREFER_RGB_565) // 内存减半
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE) // 只缓存处理后的图
                    .into(holder.cover);
        } else {

            return;
        }
        //holder.cover.setImageBitmap(bitmap);
        holder.createTime.setText(Mlist.get(position).getCreate_time());
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MapApp.getUserID()==Mlist.get(position).getUser_id()){
                    new AlertDialog.Builder(context).setTitle("是否确定删除？").setPositiveButton("yes", (dialogInterface, i) -> {
                        delete_position=holder.getLayoutPosition();//手动获取最新position
                        deleteNote();

                    }).setNegativeButton("no", (dialogInterface, i) -> {

                    }).show();
                }
                else{
                    Toast.makeText(context, "您没有删除此笔记的权限！", Toast.LENGTH_LONG).show();
                }

            }
        });
        holder.itemCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long id=Mlist.get(position).getId();
                fragmentHelper.Helper(holder.title.getText().toString(),holder.content.getText().toString(),id);
                /*if (MapApp.getUserID()==Mlist.get(position).getUser_id()){
                    fragmentHelper.Helper(holder.title.getText().toString(),holder.content.getText().toString(),id);
                }
                else{
                    Toast.makeText(context, "您没有编辑此笔记的权限！", Toast.LENGTH_LONG).show();
                }*/

            }
        });
        NoteEntity note = Mlist.get(position);
        long noteId = note.getId();
        // 设置评论数量
        if (commentCountMap.containsKey(noteId)) {
            int count = commentCountMap.get(noteId);
            holder.tvCommentCount.setText(count + "条评论");
        } else {
            holder.tvCommentCount.setText("加载中...");
            fetchCommentCount(noteId); // 发起网络请求获取评论数量
        }
        // 评论按钮点击事件
        holder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommentsBottomSheet(Mlist.get(position).getId());
            }
        });
    }

    private void fetchCommentCount(long noteId) {
        if (requestedNoteIds.contains(noteId)) return;
        requestedNoteIds.add(noteId);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getCommentCount(noteId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                requestedNoteIds.remove(noteId);
                if (response.isSuccessful() && response.body() != null) {
                    int count = response.body();
                    commentCountMap.put(noteId, count);
                    // 更新对应位置的评论数量显示
                    for (int i = 0; i < Mlist.size(); i++) {
                        if (Mlist.get(i).getId() == noteId) {
                            notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                requestedNoteIds.remove(noteId);
                Log.e("CommentCount", "获取评论数量失败: " + t.getMessage());
            }
        });
    }

    private void deleteNote() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.deleteNote(Mlist.get(delete_position).getId()).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("DELETE", "删除成功");
                    // 可以在这里更新 UI，比如刷新列表
                } else {
                    Log.e("DELETE", "删除失败: " + response.code());
                }

            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("DELETE", "请求失败: " + t.getMessage());

            }
        });
        noteDao.deleteById(Mlist.get(delete_position).getId());
        notifyItemRemoved(delete_position); //该方法不会重置position，因此如果不手动更新会导致越界访问
        Mlist.remove(delete_position);
        countInterface.Count(Mlist.size());

    }


    @Override
    public int getItemCount() {
        return Mlist.size();
    }
    class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView avatar;
        TextView userName;
        TextView slogan;
        TextView title;
        TextView content;
        TextView createTime;
        ImageView cover;
        Button delete;
        LinearLayout itemCard;
        TextView position;
        TextView tvCommentCount;
        Button btnComment;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar=itemView.findViewById(R.id.avatar);
            userName=itemView.findViewById(R.id.userName);
            slogan=itemView.findViewById(R.id.slogan);
            title=itemView.findViewById(R.id.title);
            content=itemView.findViewById(R.id.content);
            createTime=itemView.findViewById(R.id.createTime);
            cover=itemView.findViewById(R.id.cover);
            delete=itemView.findViewById(R.id.deleteButton);
            itemCard=itemView.findViewById(R.id.item_card);
            position=itemView.findViewById(R.id.position);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
            btnComment = itemView.findViewById(R.id.btn_comment);
        }
    }
    public interface CountInterface{
        public void Count(int count);

    }
    public interface FragmentHelper{
        public void Helper(String title,String content,long id);
    }

    // 统计所有评论数量（包括子评论）
    private int getTotalCommentCount(long noteId) {
        List<CommentInfo> commentInfos = MapApp.getAppDb().commentDao().getCommentsByPostId(noteId);
        return commentInfos == null ? 0 : commentInfos.size();
    }

    private void showCommentsBottomSheet(long noteId) {
        if (context == null || !(context instanceof Activity)) {
            return;
        }
        Activity activity = (Activity) context;
        BottomSheetDialog bottomSheet = new BottomSheetDialog(activity);
        View dialogView = LayoutInflater.from(activity)
                .inflate(R.layout.bottom_sheet_comments, null);
        bottomSheet.setContentView(dialogView);
        RecyclerView rvComments = dialogView.findViewById(R.id.rv_comment);
        EditText etInput = dialogView.findViewById(R.id.et_comment_input);
        Button btnSend = dialogView.findViewById(R.id.btn_send);
        rvComments.setLayoutManager(new LinearLayoutManager(activity));
        // 加载评论内容
        // 1. 从服务器加载评论
        loadCommentsFromServer(noteId, rvComments, activity);

        btnSend.setOnClickListener(v -> {
            String content = etInput.getText().toString().trim();
            if (content.isEmpty()) return;

            // 2. 发送评论到服务器
            sendCommentToServer(noteId, content, rvComments, activity);
            etInput.setText("");
            new Thread(() -> {
                // 插入评论后，重新加载评论
                loadCommentsFromServer(noteId, rvComments, activity);

            }).start();
        });


        bottomSheet.show();
    }
    private void loadCommentsFromServer(long noteId, RecyclerView rvComments, Activity activity) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getCommentsByPostId(noteId).enqueue(new Callback<List<CommentInfo>>() {
            @Override
            public void onResponse(Call<List<CommentInfo>> call, Response<List<CommentInfo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 在后台线程构建评论树
                    List<CommentInfo> commentsFromServer = response.body();
                    Log.d("CommentLoad", "Received " + commentsFromServer.size() + " comments");
                    new Thread(() -> {
                        List<Comment> comments = buildCommentHierarchy(response.body(), MapApp.getAppDb().userDao());
                        activity.runOnUiThread(() -> {
                            Log.d("CommentLoad", "Building comment tree with " + comments.size() + " root comments");
                            CommentsAdapter adapter = new CommentsAdapter(
                                    comments,
                                    noteId,
                                    PoiAdapter.this::buildCommentHierarchy,
                                    () -> refreshCommentCountOnMainThread(noteId),
                                    null,
                                    viewModel
                            );
                            rvComments.setAdapter(adapter);
                        });
                    }).start();
                } else {
                    Log.e("CommentLoad", "Failed to load comments: " + response.code());
                    Toast.makeText(activity, "加载评论失败: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<CommentInfo>> call, Throwable t) {
                Toast.makeText(activity, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendCommentToServer(long noteId, String content, RecyclerView rvComments, Activity activity) {
        int userId = MapApp.getUserID();

        new Thread(() -> {
            AppDatabase db = MapApp.getAppDb();
            User user = db.userDao().findById(userId);
            if (user == null) return;

            CommentInfo newComment = new CommentInfo(noteId, content, userId);
            newComment.setUsername(user.getName() != null && !user.getName().isEmpty()
                    ? user.getName() : user.getAccount());
            newComment.setAvatar(user.getAvatar());

            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            apiService.createComment(newComment).enqueue(new Callback<CommentInfo>() {
                @Override
                public void onResponse(Call<CommentInfo> call, Response<CommentInfo> response) {
                    if (response.isSuccessful()) {
                        // 评论发送成功，重新加载评论
                        fetchCommentCount(noteId);
                        loadCommentsFromServer(noteId, rvComments, activity);
                        //refreshCommentCountOnMainThread(noteId);
                    } else {
                        Toast.makeText(activity, "发送失败: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<CommentInfo> call, Throwable t) {
                    Toast.makeText(activity, "网络错误: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
    // 构建评论树
    private List<Comment> buildCommentHierarchy(List<CommentInfo> entities, UserDao userDao) {
        Map<Long, List<CommentInfo>> repliesMap = new HashMap<>();
//        Map<Integer, String> userIdToName = new HashMap<>();
//        List<User> users = userDao.getAll();
//        for (User user : users) {
//            userIdToName.put(user.getUid(), user.getName());
//        }
        for (CommentInfo entity : entities) {
            Long parentId = entity.getParentcomment_id();
            Log.d("CommentLoad", "Comment ID: " + entity.getComment_id() +
                    ", Parent ID: " + parentId);
            if (parentId != null&& parentId > 0) {
                if (!repliesMap.containsKey(parentId)) {
                    repliesMap.put(parentId, new ArrayList<>());
                }
                repliesMap.get(parentId).add(entity);
            }
        }
        List<Comment> rootComments = new ArrayList<>();
        for (CommentInfo entity : entities) {
            Long parentId = entity.getParentcomment_id();

            // 根评论条件：没有父评论或父评论为0/null
            if (parentId == null || parentId == 0) {
                // 确保用户名不为空
                String userName = entity.getUsername();
                if (userName == null || userName.isEmpty()) {
                    // 如果服务器没有提供用户名，尝试从本地获取
                    User user = MapApp.getAppDb().userDao().findById(entity.getUser_id());
                    userName = (user != null && user.getName() != null) ?
                            user.getName() : "未知用户";
                    Log.d("CommentLoad", "Fetched username from local: " + userName);
                }

                Comment comment = Comment.fromEntity(entity, userName);
                loadRepliesRecursive(comment, repliesMap);
                rootComments.add(comment);
                Log.d("CommentLoad", "Added root comment: " + comment.getContent());
            }
        }

        Log.d("CommentLoad", "Built " + rootComments.size() + " root comments");
        return rootComments;
    }
    private void loadRepliesRecursive(Comment parent, Map<Long, List<CommentInfo>> repliesMap) {
        List<CommentInfo> childEntities = repliesMap.get(parent.getCommentId());
        if (childEntities != null) {
            Log.d("CommentLoad", "Found " + childEntities.size() +
                    " replies for comment " + parent.getCommentId());

            for (CommentInfo childEntity : childEntities) {
                // 确保用户名不为空
                String userName = childEntity.getUsername();
                if (userName == null || userName.isEmpty()) {
                    // 如果服务器没有提供用户名，尝试从本地获取
                    User user = MapApp.getAppDb().userDao().findById(childEntity.getUser_id());
                    userName = (user != null && user.getName() != null) ?
                            user.getName() : "未知用户";
                }

                Comment childComment = Comment.fromEntity(childEntity, userName);
                parent.getReplies().add(childComment);
                Log.d("CommentTree", "Added reply: " + childComment.getContent() +
                        " to comment " + parent.getCommentId());

                // 递归加载子回复
                loadRepliesRecursive(childComment, repliesMap);
            }
        } else {
            Log.d("CommentTree", "No replies found for comment " + parent.getCommentId());
        }
    }
    // 刷新评论数量
    private void refreshCommentCountOnMainThread(long noteId) {
        // 从服务器获取最新评论数
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getCommentCount(noteId).enqueue(new Callback<Integer>() {
            @Override
            public void onResponse(Call<Integer> call, Response<Integer> response) {
                if (response.isSuccessful() && response.body() != null) {
                    int count = response.body();
                    commentCountMap.put(noteId, count);

                    new Handler(Looper.getMainLooper()).post(() -> {
                        for (int i = 0; i < Mlist.size(); i++) {
                            if (Mlist.get(i).getId() == noteId) {
                                notifyItemChanged(i);
                                break;
                            }
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call<Integer> call, Throwable t) {
                Log.e("REFRESH_COUNT", "更新评论数失败", t);
            }
        });
    }
}