package tikfans.tikplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import tikfans.tikplus.model.UnFollowUSer;
import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.FirebaseUtil;


public class UnFollowUserAdapter extends RecyclerView.Adapter<UnfollowUserViewHolder> {
    private Activity mActivity;
    private ArrayList<UnFollowUSer> unFollowUSerArrayList;
    private OnSetupViewListener mOnSetupViewListener;

    public UnFollowUserAdapter(Activity applicationContext, ArrayList<UnFollowUSer> unFollowUSerArrayList, OnSetupViewListener onSetupViewListener) {
        this.mActivity = applicationContext;
        if (unFollowUSerArrayList == null || unFollowUSerArrayList.isEmpty()) {
            this.unFollowUSerArrayList = new ArrayList<>();
        } else {
            this.unFollowUSerArrayList = unFollowUSerArrayList;
        }
        mOnSetupViewListener = onSetupViewListener;
    }

    @NonNull
    @Override
    public UnfollowUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.unfollow_user_item, parent, false);
        return new UnfollowUserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UnfollowUserViewHolder holder, int position) {
        mOnSetupViewListener.onSetupView(holder, unFollowUSerArrayList.get(position));
        holder.setItemClickListener(new UnfollowUserViewHolder.ItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                visitTiktokProfiles(unFollowUSerArrayList.get(position).getUserName());
                try {
                    FirebaseUtil.getUnSubscribedListRef().child(unFollowUSerArrayList.get(position).getUserName()).removeValue();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                unFollowUSerArrayList.remove(position);
                if (unFollowUSerArrayList.size() == 0) {
                    mActivity.finish();
                }
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d("Khang", "getItemCount:" + unFollowUSerArrayList.size());
        return unFollowUSerArrayList.size();
    }

    @Override
    public void onViewRecycled(UnfollowUserViewHolder holder) {
        super.onViewRecycled(holder);
    }

    public interface OnSetupViewListener {
        void onSetupView(UnfollowUserViewHolder holder, UnFollowUSer unFollowUSer);
    }

    private void visitTiktokProfiles(String userName) {
        if (userName == null || userName.equals("")) return;
        String videoWebUrl = AppUtil.TIKTOK_PREFIX_LINK + userName;
        Intent intent;
        try {
            intent = new Intent(Intent.ACTION_VIEW);
            intent.setPackage("com.zhiliaoapp.musically");
            intent.setData(Uri.parse(videoWebUrl));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mActivity.startActivity(intent);
        } catch (Exception e) {
            try {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.ss.android.ugc.trill");
                intent.setData(Uri.parse(videoWebUrl));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(intent);
            } catch (Exception e2) {
                try {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("com.zhiliaoapp.musically.go");
                    intent.setData(Uri.parse(videoWebUrl));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mActivity.startActivity(intent);
                } catch (Exception e3) {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(videoWebUrl));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mActivity.startActivity(intent);
                }
            }
        }
    }
}
