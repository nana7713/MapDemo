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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.widget.EditText;
import android.view.WindowManager;
import android.os.Handler;
import android.os.Looper;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.widget.Toast;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

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

    public PoiAdapter(List<NoteEntity> mlist, Context context,CountInterface countInterface,FragmentHelper fragmentHelper) {
        Mlist = mlist;
        this.context = context;
        inflater= LayoutInflater.from(context);
        this.countInterface=countInterface;
        this.fragmentHelper=fragmentHelper;
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
        String coverUri = Mlist.get(position).getNote_image_uri();
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
        // 设置评论数量
        int commentCount = getTotalCommentCount(Mlist.get(position).getId());
        holder.tvCommentCount.setText(commentCount + "条评论");
        // 评论按钮点击事件
        holder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCommentsBottomSheet(Mlist.get(position).getId());
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
        new Thread(() -> {
            AppDatabase db = MapApp.getAppDb();
            List<CommentInfo> commentInfos = db.commentDao().getCommentsByPostId(noteId);
            List<Comment> comments = buildCommentHierarchy(commentInfos, db.userDao());
            activity.runOnUiThread(() -> {
                CommentsAdapter adapter = new CommentsAdapter(
                        comments,
                        noteId,
                        this::buildCommentHierarchy,
                        () -> refreshCommentCountOnMainThread(noteId),
                        null
                );
                rvComments.setAdapter(adapter);
            });
        }).start();
        btnSend.setOnClickListener(v -> {
            String content = etInput.getText().toString().trim();
            if (content.isEmpty()) return;
            int userId = MapApp.getUserID();
            new Thread(() -> {
                AppDatabase db = MapApp.getAppDb();
                User user = db.userDao().findById(userId);
                if (user == null) return;
                CommentInfo newComment = new CommentInfo(noteId, content, userId);
                newComment.setUsername(user.getName() != null && !user.getName().isEmpty() ? user.getName() : user.getAccount());
                newComment.setAvatar(user.getAvatar()); // 插入时赋值avatar
                db.commentDao().insertComment(newComment);
                List<CommentInfo> commentInfos = db.commentDao().getCommentsByPostId(noteId);
                List<Comment> comments = buildCommentHierarchy(commentInfos, db.userDao());
                activity.runOnUiThread(() -> {
                    CommentsAdapter adapter = new CommentsAdapter(
                            comments,
                            noteId,
                            this::buildCommentHierarchy,
                            ()-> refreshCommentCountOnMainThread(noteId),
                            null
                    );
                    rvComments.setAdapter(adapter);
                    etInput.setText("");
                    refreshCommentCountOnMainThread(noteId); // 实时刷新评论数量
                });
            }).start();
        });
        bottomSheet.show();
    }

    // 构建评论树
    private List<Comment> buildCommentHierarchy(List<CommentInfo> entities, UserDao userDao) {
        Map<Long, List<CommentInfo>> repliesMap = new HashMap<>();
        Map<Integer, String> userIdToName = new HashMap<>();
        List<User> users = userDao.getAll();
        for (User user : users) {
            userIdToName.put(user.getUid(), user.getName());
        }
        for (CommentInfo entity : entities) {
            Long parentId = entity.getParentcomment_id();
            if (parentId != null) {
                if (!repliesMap.containsKey(parentId)) {
                    repliesMap.put(parentId, new ArrayList<>());
                }
                repliesMap.get(parentId).add(entity);
            }
        }
        List<Comment> rootComments = new ArrayList<>();
        for (CommentInfo entity : entities) {
            if (entity.getParentcomment_id() == null) {
                String userName = entity.getUsername();
                if (userName == null || userName.isEmpty()) {
                    userName = userIdToName.getOrDefault(entity.getUser_id(), "未知用户");
                }
                Comment comment = Comment.fromEntity(entity, userName);
                loadRepliesRecursive(comment, repliesMap, userIdToName);
                rootComments.add(comment);
            }
        }
        return rootComments;
    }
    private void loadRepliesRecursive(Comment parent, Map<Long, List<CommentInfo>> repliesMap, Map<Integer, String> userIdToName) {
        List<CommentInfo> childEntities = repliesMap.get(parent.getCommentId());
        if (childEntities != null) {
            for (CommentInfo childEntity : childEntities) {
                String userName = childEntity.getUsername();
                if (userName == null || userName.isEmpty()) {
                    userName = userIdToName.getOrDefault(childEntity.getUser_id(), "未知用户");
                }
                Comment childComment = Comment.fromEntity(childEntity, userName);
                parent.getReplies().add(childComment);
                loadRepliesRecursive(childComment, repliesMap, userIdToName);
            }
        }
    }
    // 刷新评论数量
    private void refreshCommentCountOnMainThread(long noteId) {
        new Handler(Looper.getMainLooper()).post(() -> {
            notifyDataSetChanged();
        });
    }
}
