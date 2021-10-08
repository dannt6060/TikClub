package tikfans.tikplus.model;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tikfans.tikplus.R;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.ViewHolder> {
    private ArrayList<ItemVideo> list;
    private Activity context;
    private AdapterView.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public VideoAdapter(ArrayList<ItemVideo> list, Activity context) {
        this.list = list;
        this.context = context;
    }

    public ArrayList<ItemVideo> getList() {
        return list;
    }

    public void setList(ArrayList<ItemVideo> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_video_recyclerview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Picasso.get()
                .load(list.get(position).getImageUrl())
                .into(holder.imgThumb);
    }

    @Override
    public int getItemCount() {
        return list == null ? 0 : list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumb;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumb = itemView.findViewById(R.id.img_thumb);
            imgThumb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(context, PlayVideoActivity.class);
//                    intent.putExtra("video", list.get(getLayoutPosition()).getVideo().getPlayAddr());
//                    //https://www.tiktok.com/@palomagraciely/video/6817171941347036422
//                    String url = "https://www.tiktok.com/@"+list.get(getLayoutPosition()).getAuthor().getUniqueId()+"/video/"+list.get(getLayoutPosition()).getId();
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(null, itemView, getLayoutPosition(), getItemId());
                    }
//                    context.startActivity(intent);
//                    context.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                }
            });
        }
    }
}
