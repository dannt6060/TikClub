package tikfans.tikplus.subviewlike;

import static com.unity3d.scar.adapter.common.Utils.runOnUiThread;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import tikfans.tikplus.R;
import tikfans.tikplus.model.UnFollowUSer;
import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.CircleTransform;
import tikfans.tikplus.util.FirebaseUtil;
import tikfans.tikplus.util.SecureDate;
import tikfans.tikplus.model.LogSub;
import tikfans.tikplus.model.SubCampaign;


public class SubChiTietChienDichAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private Activity mActivity;
    private SubCampaign campaign;
    private ArrayList<LogSub> logSubArrayList;
    private SimpleDateFormat sfd;
    private CampaignDetailHeaderViewHolder campaignDetailHeaderViewHolder;

    public SubChiTietChienDichAdapter(Context context, Activity activity) {
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
            View itemView0 = li.inflate(R.layout.sub_campaign_detail_header, parent, false);
            return new CampaignDetailHeaderViewHolder(itemView0);
        } else {
            View itemView1 = li.inflate(R.layout.campaign_detail_user_item, parent, false);
            return new UserViewHolder(itemView1);
        }

    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        if (position == 0) {
            campaignDetailHeaderViewHolder = (CampaignDetailHeaderViewHolder) holder;
//            textViewHolder.tvText.setText(mAuthorLiked.get(position).toString());
            if (campaign != null) {
                Long createTimeStamp = (Long) campaign.getCreTime();
                Long finishTimeStamp = (Long) campaign.getFinTime();
                Picasso.get()
                        .load(campaign.getUserImg())
                        .transform(new CircleTransform())
                        .into(campaignDetailHeaderViewHolder.imgChannelThumb);
                if (campaign.getUserImg() != null && !campaign.getUserImg().equals("") && !campaign.getUserImg().equals("NONE")) {
                    try {
                        Picasso.get().load(campaign.getUserImg()).transform(new CircleTransform())
                                .into(campaignDetailHeaderViewHolder.imgChannelThumb);
                    } catch (Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                    String img = campaign.getUserImg();
                    try {
                        String expiredTime = img.substring(img.lastIndexOf("expires=") + 8, img.lastIndexOf("expires=") + 18);
                        Log.d("khang", "expiredTime: " + expiredTime + " / " + System.currentTimeMillis());

                        long l = Long.parseLong(expiredTime);
                        if (l < System.currentTimeMillis() / 1000) {
                            Log.d("khang", "da het han" + expiredTime);
                        } else {
                            Log.d("khang", "chua het han" + expiredTime);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                }


                campaignDetailHeaderViewHolder.txtChannelName.setText(campaign.getUserName());
                campaignDetailHeaderViewHolder.txtCreateTimeStamp.setText(sfd.format(new Date
                        (createTimeStamp)));
                if (finishTimeStamp == -1) {
                    campaignDetailHeaderViewHolder.txtFinishTime.setText(mContext.getString(R
                            .string.in_progress));
                    campaignDetailHeaderViewHolder.txtFinishTimeStamp.setText("");
                } else {
                    campaignDetailHeaderViewHolder.txtFinishTimeStamp.setText(sfd.format(new Date(finishTimeStamp)));
                }
                campaignDetailHeaderViewHolder.txtStatusProgress.setText(
                        String.format(mContext.getString(R.string.progress_subs), campaign.getCurSub(), campaign.getOrder()));

                if (logSubArrayList == null) {
                } else {
                    if (logSubArrayList.size() == 100) {
                        campaignDetailHeaderViewHolder.txtPeopleViewedTitle.setText(mContext.getString(R.string.people_subscriber_limitation));
                    } else {
                        campaignDetailHeaderViewHolder.txtPeopleViewedTitle.setText(mContext.getString(R.string.people_subscriber));
                    }
                }

                int progress = campaign.getCurSub() * 100 / campaign
                        .getOrder();
                campaignDetailHeaderViewHolder.progressBar.setProgress(progress);
                if (campaign.getCurSub() >= campaign.getOrder()) {
                    campaignDetailHeaderViewHolder.imgProgress.setImageResource(R.drawable.ic_done);
                } else {
                    campaignDetailHeaderViewHolder.imgProgress.setImageResource(R.drawable.ic_in_progress);
                }
            }
        } else {
            UserViewHolder userViewHolder = (UserViewHolder) holder;
            LogSub logSub = logSubArrayList.get(position - 1);
            Long timeStamp = (Long) logSub.getTime();
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
                    visitTiktokProfiles(logSubArrayList.get(position - 1).getSubUName());
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
                                        .setTitle(mContext.getResources().getString(R.string.bao_cao_nguoi_dung))
                                        .setMessage(mContext.getResources().getString(R.string.bao_cao_nguoi_dung_chi_tiet))
                                        .setNegativeButton(mContext.getResources().getString(R.string.huy_bo), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .setPositiveButton(mContext.getResources().getString(R.string.bao_cao), new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                try {
                                                    DatabaseReference reportRef = FirebaseUtil.getReportSubRef(logSubArrayList.get(position - 1).getSubId());
                                                    reportRef.child(campaign.getUserName()).setValue(new UnFollowUSer(campaign.getUserName(), campaign.getUserImg(), campaign.getVideoId()));
                                                    Toast.makeText(mContext, mContext.getString(R.string.cam_on_vi_bao_cao), Toast.LENGTH_SHORT).show();
                                                    dialog.dismiss();
                                                } catch (Exception e) {
                                                    FirebaseCrashlytics.getInstance().recordException(e);
                                                }
                                            }
                                        })
                                        .show();
                            } else if (item.getItemId() == R.id.popup_menu_visit_tiktok_profiles) {
                                visitTiktokProfiles(logSubArrayList.get(position - 1).getSubUName());
                            }
                            return true;
                        }
                    });

                    popup.show();//showing popup menu
                }
            });

            userViewHolder.txtName.setText(logSub.getSubUName());
            userViewHolder.imgMoreIcon.setVisibility(View.VISIBLE);
            try {
                Picasso.get().load(logSub.getSubPhoto()).transform(new CircleTransform())
                        .into(userViewHolder.profilePictureView);
            } catch (Exception e) {
                FirebaseCrashlytics.getInstance().recordException(e);
            }

            if (logSub.getSubUName() == null || logSub.getSubUName().length()<1) {
                DatabaseReference userNameRef = FirebaseUtil.getUserNameByUID(logSub.getSubId());
                userNameRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            logSub.setSubUName(snapshot.getValue(String.class));
                            userViewHolder.txtName.setText("@" + logSub.getSubUName());
                            logSubArrayList.set(position - 1, logSub);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else {
                userViewHolder.txtName.setText("@" + logSub.getSubUName());
            }

            if (logSub.getSubPhoto() == null || logSub.getSubPhoto().equals("")) {
                DatabaseReference userPhotoRef = FirebaseUtil.getUserPhotoByUID(logSub.getSubId());
                userPhotoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        try {
                            Picasso.get().load(snapshot.getValue(String.class)).transform(new CircleTransform())
                                    .into(userViewHolder.profilePictureView);
                            logSub.setSubPhoto(snapshot.getValue(String.class));
                            logSubArrayList.set(position - 1, logSub);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else {
                try {
                    Picasso.get().load(logSub.getSubPhoto()).transform(new CircleTransform())
                            .into(userViewHolder.profilePictureView);
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        }
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
        } catch (ActivityNotFoundException e) {
            try {
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage("com.ss.android.ugc.trill");
                intent.setData(Uri.parse(videoWebUrl));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(intent);
            } catch (ActivityNotFoundException e2) {
                try {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setPackage("com.zhiliaoapp.musically.go");
                    intent.setData(Uri.parse(videoWebUrl));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mActivity.startActivity(intent);
                } catch (ActivityNotFoundException e3) {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(videoWebUrl));
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mActivity.startActivity(intent);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if (logSubArrayList == null) return 1;

        return logSubArrayList.size() + 1;
    }

    public void setCampaign(SubCampaign campaign) {
        this.campaign = campaign;
    }

    public void setLogSubArray(ArrayList<LogSub> logSubArrayList) {
        this.logSubArrayList = logSubArrayList;
    }

    public interface ItemClickListener {
        void onClick(View view, int position);
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
            txtName = itemView.findViewById(R.id.txtUserName);
            txtViewedTimeStamp = itemView.findViewById(R.id.txtUserTimeStamp);
            profilePictureView = itemView.findViewById(R.id.author_icon);
            imgMoreIcon = itemView.findViewById(R.id.sub_log_more_ic);
            imgMoreIcon.setOnClickListener(this);
            txtName.setOnClickListener(this);
            txtViewedTimeStamp.setOnClickListener(this);
            profilePictureView.setOnClickListener(this);
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
        private TextView txtChannelName;
        private ImageView imgChannelThumb;
        private TextView txtCreateTimeStamp;
        private TextView txtFinishTimeStamp;
        private TextView txtFinishTime;
        private TextView txtStatusProgress;
        private TextView txtPeopleViewedTitle;
        private ProgressBar progressBar;
        private ImageView imgProgress;

        public CampaignDetailHeaderViewHolder(View itemView) {
            super(itemView);
            txtChannelName = itemView.findViewById(R.id.txt_channel_name);
            imgChannelThumb = itemView.findViewById(R.id.unfollow_user_photo);
            txtCreateTimeStamp = itemView.findViewById(R.id.create_timestamp);
            txtFinishTime = itemView.findViewById(R.id.finished_time_textView);
            txtFinishTimeStamp = itemView.findViewById(R.id.finish_timestamp);
            txtPeopleViewedTitle = itemView.findViewById(R.id.txt_people_viewed_title);
            txtStatusProgress = itemView.findViewById(R.id.txtStatusProgress);
            progressBar = itemView.findViewById(R.id.campaign_progress);
            imgProgress = itemView.findViewById(R.id.imageViewStatus);
        }
    }

}
