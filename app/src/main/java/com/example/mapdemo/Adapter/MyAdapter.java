package com.example.mapdemo.Adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.baidu.mapapi.clusterutil.ui.Comment;
import com.bumptech.glide.Glide;
import com.example.mapdemo.Bean.NoteCard;
import com.example.mapdemo.Database.AppDatabase;
import com.example.mapdemo.Database.CommentInfo;
import com.example.mapdemo.Database.CommentDao;
import com.example.mapdemo.Database.NoteDao;
import com.example.mapdemo.Database.NoteEntity;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.Database.UserDao;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<NoteCard> Mlist;
    private Context context;
    private LayoutInflater inflater;
    private int delete_position;
    NoteDao noteDao= MapApp.getAppDb().noteDao();
    FragmentManager fragmentManager;
    CountInterface countInterface;
    FragmentHelper fragmentHelper;
    // 新增：评论数量刷新回调接口
    public interface CommentCountRefresher {
        void refreshCommentCount(long noteId);
    }
    private CommentCountRefresher commentCountRefresher;

    public MyAdapter(List<NoteCard> mlist, Context context,CountInterface countInterface,FragmentHelper fragmentHelper) {
        Mlist = mlist;
        this.context = context;
        inflater= LayoutInflater.from(context);
        this.countInterface=countInterface;
        this.fragmentHelper=fragmentHelper;
        // 默认实现为空
        this.commentCountRefresher = null;
    }
    // 新增：支持外部设置刷新回调
    public void setCommentCountRefresher(CommentCountRefresher refresher) {
        this.commentCountRefresher = refresher;
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
        holder.slogan.setText(Mlist.get(position).getUser_slogan());
        holder.title.setText(Mlist.get(position).getNote_title());
        holder.content.setText(Mlist.get(position).getNote_content());
        Glide.with(context).load(Mlist.get(position).getUser_avatar()).into(holder.avatar);
        //Glide.with(context).load(Mlist.get(position).getCover()).into(holder.cover);
//        InputStream inputStream= null;
//        try {
//            inputStream = context.getContentResolver().openInputStream(Uri.parse(Mlist.get(position).getCover()));
//        } catch (FileNotFoundException e) {
//            throw new RuntimeException(e);
//        }
//        Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
        // 处理封面图片加载，添加异常捕获
        String coverUri = Mlist.get(position).getCover();
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
//            Glide.with(context)
//                    .load(Uri.parse(coverUri))
//                    .into(holder.cover);
        }
        //holder.cover.setImageBitmap(bitmap);
        holder.createTime.setText(Mlist.get(position).getCreate_time());
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context).setTitle("是否确定删除？").setPositiveButton("yes", (dialogInterface, i) -> {
                    delete_position=holder.getLayoutPosition();//手动获取最新position
                    deleteNote();

                }).setNegativeButton("no", (dialogInterface, i) -> {

                }).show();
            }
        });
        holder.itemCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                long id=Mlist.get(position).getCardID();
                fragmentHelper.Helper(holder.title.getText().toString(),holder.content.getText().toString(),id);
            }
        });
        holder.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(context, "评论", Toast.LENGTH_SHORT).show();
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    NoteCard note = Mlist.get(position);
                    showCommentsBottomSheet(note.getCardID());
                }
            }
        });
        // 显示所有评论总数
        final long cardId = Mlist.get(position).getCardID();
        holder.tvCommentCount.setText("..."); // 先占位
        new Thread(() -> {
            int count = getTotalCommentCount(cardId);
            ((Activity) context).runOnUiThread(() -> {
                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION &&
                        Mlist.get(currentPos).getCardID() == cardId) {
                    holder.tvCommentCount.setText(count + "条评论");
                }
            });
        }).start();
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
                        this
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
                newComment.setAvatar(user.getAvatar()); // 关键：插入时赋值avatar
                db.commentDao().insertComment(newComment);
                List<CommentInfo> commentInfos = db.commentDao().getCommentsByPostId(noteId);
                List<Comment> comments = buildCommentHierarchy(commentInfos, db.userDao());
                activity.runOnUiThread(() -> {
                    CommentsAdapter adapter = new CommentsAdapter(
                            comments,
                            noteId,
                            this::buildCommentHierarchy,
                            ()-> refreshCommentCountOnMainThread(noteId),
                            this
                    );
                    rvComments.setAdapter(adapter);
                    etInput.setText("");
                    refreshCommentCountOnMainThread(noteId); // 新增：实时刷新评论数量
                });
            }).start();
        });
        bottomSheet.show();
    }
    // Helper 方法：构建层级评论结构
    private List<Comment> buildCommentHierarchy(List<CommentInfo> entities, UserDao userDao) {
        Map<Long, List<CommentInfo>> repliesMap = new HashMap<>();
        Map<Integer, String> userIdToName = new HashMap<>();

        // 预加载用户数据（避免多次查询）
        List<User> users = userDao.getAll();
        for (User user : users) {
            userIdToName.put(user.getUid(), user.getName());
        }

        // 按父评论ID分组
        for (CommentInfo entity : entities) {
            Long parentId = entity.getParentcomment_id();
            if (parentId != null) {
                if (!repliesMap.containsKey(parentId)) {
                    repliesMap.put(parentId, new ArrayList<>());
                }
                repliesMap.get(parentId).add(entity);
            }
        }

        // 构建层级结构
        List<Comment> rootComments = new ArrayList<>();
        for (CommentInfo entity : entities) {
            if (entity.getParentcomment_id() == null) {
                // 优先用CommentInfo的username字段
                String userName = entity.getUsername();
                if (userName == null || userName.isEmpty()) {
                    userName = userIdToName.getOrDefault(entity.getUser_id(), "未知用户");
                }
                Log.d("CommentDebug", "Comment ID: " + entity.getComment_id() +
                        ", User ID: " + entity.getUser_id() +
                        ", UserName: " + userName);
                Comment comment = Comment.fromEntity(entity, userName);
                loadRepliesRecursive(comment, repliesMap, userIdToName);
                rootComments.add(comment);
            }
        }
        return rootComments;
    }

    // Helper 方法：递归加载子回复
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
                loadRepliesRecursive(childComment, repliesMap, userIdToName); // 递归处理嵌套回复
            }
        }
    }
    private void deleteNote() {
        noteDao.deleteById(Mlist.get(delete_position).getCardID());
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
        Button btnComment;
        LinearLayout itemCard;
        TextView position;
        TextView tvCommentCount;


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
            cover=itemView.findViewById(R.id.cover);
            position=itemView.findViewById(R.id.position);
            btnComment=itemView.findViewById(R.id.btn_comment);
            tvCommentCount=itemView.findViewById(R.id.tv_comment_count);
        }

    }
    public interface CountInterface{
        public void Count(int count);

    }
    public interface FragmentHelper{
        public void Helper(String title,String content,long id);
    }
    private int getCurrentUserId() {
        // SharedPreferences prefs = context.getSharedPreferences("spRecord", Context.MODE_PRIVATE);
        // return prefs.getInt("uid", -1); // -1 表示未登录
        int userId = MapApp.getUserID();
        Log.d("评论userId", "userId=" + userId);
        return userId;
    }

    // 新增：统计所有评论（含子评论）总数
    private int getTotalCommentCount(long noteId) {
        List<CommentInfo> allComments = MapApp.getAppDb().commentDao().getCommentsByPostId(noteId);
        return allComments.size();
    }

    // 主线程安全刷新评论数量方法，改为统计所有评论
    public void refreshCommentCountOnMainThread(long noteId) {
        new Thread(() -> {
            int count = getTotalCommentCount(noteId);
            ((Activity) context).runOnUiThread(() -> {
                for (int i = 0; i < Mlist.size(); i++) {
                    if (Mlist.get(i).getCardID() == noteId) {
                        notifyItemChanged(i);
                        break;
                    }
                }
            });
        }).start();
    }

    // 新增：递归删除评论及其所有子评论
    public void deleteCommentAndChildren(long commentId) {
        CommentDao commentDao = MapApp.getAppDb().commentDao();
        List<CommentInfo> children = commentDao.getChildComments(commentId);
        for (CommentInfo child : children) {
            deleteCommentAndChildren(child.getComment_id());
        }
        commentDao.deleteCommentById(commentId);
    }
}
