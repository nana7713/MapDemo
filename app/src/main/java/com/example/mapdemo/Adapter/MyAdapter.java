package com.example.mapdemo.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mapdemo.Bean.NoteCard;
import com.example.mapdemo.Database.NoteDao;
import com.example.mapdemo.Database.User;
import com.example.mapdemo.MapApp;
import com.example.mapdemo.R;

import org.w3c.dom.Text;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<NoteCard> Mlist;
    private Context context;
    private LayoutInflater inflater;
    private int delete_position;
    NoteDao noteDao= MapApp.getAppDb().noteDao();
    FragmentManager fragmentManager;
    CountInterface countInterface;
    FragmentHelper fragmentHelper;

    public MyAdapter(List<NoteCard> mlist, Context context,CountInterface countInterface,FragmentHelper fragmentHelper) {
        Mlist = mlist;
        this.context = context;
        inflater= LayoutInflater.from(context);
        this.countInterface=countInterface;
        this.fragmentHelper=fragmentHelper;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.item_layout,parent,false);
        return new MyViewHolder(view);
    }

    @NonNull
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.position.setText(Mlist.get(position).getLongitude() +" "+ Mlist.get(position).getLatitude());
        holder.userName.setText(Mlist.get(position).getUser_name());
        holder.slogan.setText(Mlist.get(position).getUser_slogan());
        holder.title.setText(Mlist.get(position).getNote_title());
        holder.content.setText(Mlist.get(position).getNote_content());
        Glide.with(context).load(Mlist.get(position).getUser_avatar()).into(holder.avatar);
        //Glide.with(context).load(Mlist.get(position).getCover()).into(holder.cover);
        if(Mlist.get(position).getCover()!=null){
            InputStream inputStream= null;
            try {

                inputStream = context.getContentResolver().openInputStream(Uri.parse(Mlist.get(position).getCover()));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
            holder.cover.setImageBitmap(bitmap);
        }

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
        LinearLayout itemCard;
        TextView position;



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
        }
    }
    public interface CountInterface{
        public void Count(int count);

    }
    public interface FragmentHelper{
        public void Helper(String title,String content,long id);
    }
}
