package tikfans.tikplus.subviewlike;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tikfans.tikplus.ItemURLTiktok;
import tikfans.tikplus.R;
import tikfans.tikplus.model.ItemVideo;
import tikfans.tikplus.model.LikeCampaign;
import tikfans.tikplus.model.LogLike;
import tikfans.tikplus.model.ResultUser;
import tikfans.tikplus.model.ResultVideo;
import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.CircleTransform;
import tikfans.tikplus.util.FirebaseUtil;
import tikfans.tikplus.util.SecureDate;

import static com.unity3d.services.core.misc.Utilities.runOnUiThread;

public class LikeChiTietChienDichAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private Activity mActivity;
    private LikeCampaign campaign;
    private ArrayList<LogLike> logLikeArrayList;
    private SimpleDateFormat sfd;
    private CampaignDetailHeaderViewHolder campaignDetailHeaderViewHolder;

    public LikeChiTietChienDichAdapter(Context context, Activity activity) {
        mContext = context;
        mActivity = activity;
        sfd = new SimpleDateFormat(mContext.getResources().getString(R
                .string.simple_date_format));
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 0;
        return 1;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(mContext);

        if (viewType == 0) {
            View itemView0 = li.inflate(R.layout.view_campaign_detail_header, parent, false);
            return new CampaignDetailHeaderViewHolder(itemView0);
        } else {
            View itemView1 = li.inflate(R.layout.campaign_detail_user_item, parent, false);
            return new UserViewHolder(itemView1);
        }

    }

    ItemURLTiktok.ClientTikTokListener listener = new ItemURLTiktok.ClientTikTokListener() {
        @Override
        public void onLoading() {
        }

        @Override
        public void onReceivedListVideo(ResultVideo result) {
            List<ItemVideo> itemVideoList = result.getResult();
            if (itemVideoList != null && itemVideoList.size() > 0) {
                for (int i = 0; i < itemVideoList.size(); i++) {
                    ItemVideo itemVideo = itemVideoList.get(i);

                    if (campaign != null && campaign.getVideoId() != null && campaign.getVideoId().equals(itemVideo.getId())) {
                        String img = itemVideo.getImageUrl();
                        if (campaignDetailHeaderViewHolder != null) {
                            Picasso.get().load(img).transform(new CircleTransform())
                                    .into(campaignDetailHeaderViewHolder.imgVideoThumb);
                        }
                        final DatabaseReference campaignCurrentRef = FirebaseUtil.getLikeCampaignsRef().child(campaign.getKey());
                        campaignCurrentRef.runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                LikeCampaign currentCampaign = mutableData.getValue(LikeCampaign.class);
                                if (currentCampaign == null) {
                                    return Transaction.success(mutableData);
                                }
                                currentCampaign.setVideoThumb(img);
                                // Set value and report transaction success
                                mutableData.setValue(currentCampaign);
                                return Transaction.success(mutableData);
                            }

                            @Override
                            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                            }
                        });
                    }
                }
            }
        }

        @Override
        public void onReceivedUserInfo(final ResultUser result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onError(int code, String mess) {
        }

        @Override
        public void onInvalidLink() {
        }

        @Override
        public void onCheckLinkDone() {
        }
    };

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            campaignDetailHeaderViewHolder = (CampaignDetailHeaderViewHolder) holder;
//            textViewHolder.tvText.setText(mAuthorLiked.get(position).toString());
            if (campaign != null) {
                Long createTimeStamp = (Long) campaign.getCreTime();
                Long finishTimeStamp = (Long) campaign.getFinTime();
                Picasso.get().load(campaign.getVideoThumb()).into(campaignDetailHeaderViewHolder.imgVideoThumb);

                if (campaign.getUserName() != null) {
                    if (campaign.getVideoThumb() != null && !campaign.getVideoThumb().equals("") && !campaign.getVideoThumb().equals("NONE")) {
                        String img = campaign.getVideoThumb();
                        try {
                            String expiredTime = img.substring(img.lastIndexOf("expires=") + 8, img.lastIndexOf("expires=") + 18);
                            Log.d("khang", "expiredTime: " + expiredTime + " / " + System.currentTimeMillis());

                            long l = Long.parseLong(expiredTime);
                            if (l < System.currentTimeMillis() / 1000) {
                                Log.d("khang", "da het han" + expiredTime);
                                String link = AppUtil.TIKTOK_PREFIX_LINK + campaign.getUserName();
                                ItemURLTiktok url = new ItemURLTiktok(link, listener);
                                url.getListVideoFromUser();
                            } else {
                                Log.d("khang", "chua het han" + expiredTime);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        String link = AppUtil.TIKTOK_PREFIX_LINK + campaign.getUserName();
                        ItemURLTiktok url = new ItemURLTiktok(link, listener);
                        url.getListVideoFromUser();
                    }
                } else {
                    Log.d("khang", "mUserName is null");
                    final DatabaseReference mUserNameRef = FirebaseUtil.getUserNameRef(campaign.ownId);
                    mUserNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                String link = AppUtil.TIKTOK_PREFIX_LINK + snapshot.getValue(String.class);
                                ItemURLTiktok url = new ItemURLTiktok(link, listener);
                                url.getListVideoFromUser();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                }

                campaignDetailHeaderViewHolder.txtCreateTimeStamp.setText(sfd.format(new Date
                        (createTimeStamp)));
                if (finishTimeStamp == -1) {
                    campaignDetailHeaderViewHolder.txtFinishTime.setText(mContext.getString(R
                            .string.in_progress));
                    campaignDetailHeaderViewHolder.txtFinishTimeStamp.setText("");
                } else {
                    campaignDetailHeaderViewHolder.txtFinishTimeStamp.setText(sfd.format(new Date(finishTimeStamp)));
                }
                if (campaign.getCurLike() <= 1) {
                    campaignDetailHeaderViewHolder.txtStatusProgress.setText(
                            String.format(mContext.getString(R.string.progress_like), campaign.getCurLike(), campaign.getOrder()));
                } else {
                    campaignDetailHeaderViewHolder.txtStatusProgress.setText(
                            String.format(mContext.getString(R.string.progress_likes), campaign.getCurLike(), campaign.getOrder()));
                }

                campaignDetailHeaderViewHolder.txtPeopleLikedTitle.setText(mContext.getString(R.string.people_liked));

                int progress = (int) (campaign.getCurLike() * 100 / campaign
                        .getOrder());
                campaignDetailHeaderViewHolder.progressBar.setProgress(progress);
                if (campaign.getCurLike() >= campaign.getOrder()) {
                    campaignDetailHeaderViewHolder.imgProgress.setImageResource(R.drawable.ic_done);
                } else {
                    campaignDetailHeaderViewHolder.imgProgress.setImageResource(R.drawable.ic_in_progress);
                }
            }
        } else {
            UserViewHolder userViewHolder = (UserViewHolder) holder;
            LogLike logLike = logLikeArrayList.get(position - 1);
            Long timeStamp = (Long) logLike.getTime();
            Long currentTime = SecureDate.getInstance().getDate().getTime();
            Long diffTime = (currentTime - timeStamp) / 1000;
            if (diffTime < 60) {
                userViewHolder.txtViewedTimeStamp.setText(String.format(mContext.getString(R.string.seconds_ago), diffTime));
            } else if (diffTime < 60 * 60) {
                userViewHolder.txtViewedTimeStamp.setText(String.format(mContext.getString(R.string.minutes_ago), diffTime / 60));
            } else if (diffTime < 60 * 60 * 24) {
                userViewHolder.txtViewedTimeStamp.setText(String.format(mContext.getString(R.string.hours_ago), diffTime / (60 * 60)));
            } else {
                userViewHolder.txtViewedTimeStamp.setText(sfd.format(new Date(timeStamp)));
            }
            userViewHolder.setItemClickListener(new ItemClickListener() {
                @Override
                public void onClick(View view, int position) {
                    Log.d("Khang", "item click");
                    visitTiktokProfiles(logLikeArrayList.get(position - 1).getLikeUName());
                }
            });
            userViewHolder.setMoreItemClickListener(new MoreItemClickListener() {
                @Override
                public void onClick(View view, final int position) {
                    Log.d("Khang", "more item click");
                    PopupMenu popup = new PopupMenu(mActivity, view);
                    popup.getMenuInflater().inflate(R.menu.sub_lop_popup_menu, popup.getMenu());

                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            if (item.getItemId() == R.id.popup_menu_report_user) {
                                new AlertDialog.Builder(new ContextThemeWrapper(mActivity, R.style.myDialog))
                                        .setTitle(mActivity.getResources().getString(R.string.bao_cao_nguoi_dung))
                                        .setMessage(mActivity.getResources().getString(R.string.bao_cao_nguoi_dung_chi_tiet))
                                        .setNegativeButton(mActivity.getResources().getString(R.string.huy_bo), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .setPositiveButton(mActivity.getResources().getString(R.string.bao_cao), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                Toast.makeText(mActivity, mActivity.getString(R.string.cam_on_vi_bao_cao), Toast.LENGTH_SHORT).show();
                                                dialog.dismiss();
                                            }
                                        })
                                        .show();
                            } else if (item.getItemId() == R.id.popup_menu_visit_tiktok_profiles) {
                                visitTiktokProfiles(logLikeArrayList.get(position - 1).getLikeUName());
                            }
                            return true;
                        }
                    });

                    popup.show();//showing popup menu
                }
            });

            userViewHolder.txtName.setText("@"+ logLike.getLikeUName());
            Picasso.get().load(logLike.getLikePhoto()).transform(new CircleTransform())
                    .into(userViewHolder.profilePictureView);
        }
    }

    public interface ItemClickListener {
        void onClick(View view, int position);
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
            mContext.startActivity(intent);
        } catch (Exception e) {
            try {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.ss.android.ugc.trill");
                intent.setData(Uri.parse(videoWebUrl));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            } catch (Exception e2) {
                try {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("com.zhiliaoapp.musically.go");
                    intent.setData(Uri.parse(videoWebUrl));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                } catch (Exception e3) {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(videoWebUrl));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if (logLikeArrayList == null) return 1;

        return logLikeArrayList.size() + 1;
    }

    public void setCampaign(LikeCampaign campaign) {
        this.campaign = campaign;
    }

    public void setLogViewArray(ArrayList<LogLike> logLikeArrayList) {
        this.logLikeArrayList = logLikeArrayList;
    }

    public interface MoreItemClickListener {
        void onClick(View view, int position);
    }


    public class UserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView txtName;
        private TextView txtViewedTimeStamp;
        private ImageView profilePictureView;
        private ImageView imgMoreIcon;
        private ItemClickListener itemClickListener;
        private MoreItemClickListener moreItemClickListener;

        public UserViewHolder(View itemView) {
            super(itemView);
            txtName = (TextView) itemView.findViewById(R.id.txtUserName);
            txtViewedTimeStamp = (TextView) itemView.findViewById(R.id.txtUserTimeStamp);
            profilePictureView = (ImageView) itemView.findViewById(R.id.author_icon);
            imgMoreIcon = itemView.findViewById(R.id.sub_log_more_ic);
            imgMoreIcon.setOnClickListener(this);
            itemView.setOnClickListener(this);
        }

        public void setItemClickListener(ItemClickListener itemClickListener) {
            this.itemClickListener = itemClickListener;
        }

        public void setMoreItemClickListener(MoreItemClickListener moreItemClickListener) {
            this.moreItemClickListener = moreItemClickListener;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.sub_log_more_ic) {
                moreItemClickListener.onClick(v, getAdapterPosition());
            } else {
                itemClickListener.onClick(v, getAdapterPosition());
            }
        }

    }

    public class CampaignDetailHeaderViewHolder extends RecyclerView.ViewHolder {
        private TextView txtVideoTitle;
        private ImageView imgVideoThumb;
        private TextView txtCreateTimeStamp;
        private TextView txtFinishTimeStamp;
        private TextView txtFinishTime;
        private TextView txtStatusProgress;
        private TextView txtPeopleLikedTitle;
        private ProgressBar progressBar;
        private ImageView imgProgress;

        public CampaignDetailHeaderViewHolder(View itemView) {
            super(itemView);
            txtVideoTitle = (TextView) itemView.findViewById(R.id.txtVideoTitle);
            imgVideoThumb = (ImageView) itemView.findViewById(R.id.imgVideoThumb);
            txtCreateTimeStamp = (TextView) itemView.findViewById(R.id.create_timestamp);
            txtFinishTime = (TextView) itemView.findViewById(R.id.finished_time_textView);
            txtFinishTimeStamp = (TextView) itemView.findViewById(R.id.finish_timestamp);
            txtPeopleLikedTitle = (TextView) itemView.findViewById(R.id.txt_people_viewed_title);
            txtStatusProgress = (TextView) itemView.findViewById(R.id.txtStatusProgress);
            progressBar = (ProgressBar) itemView.findViewById(R.id.campaign_progress);
            imgProgress = (ImageView) itemView.findViewById(R.id.imageViewStatus);
        }
    }

}
