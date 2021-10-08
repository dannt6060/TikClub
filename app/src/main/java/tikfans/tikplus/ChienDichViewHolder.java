/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tikfans.tikplus;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import tikfans.tikplus.util.CircleTransform;

public class ChienDichViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final View mView;

    private ImageView mCampaignThumb;
//    private TextView mVideoTitleTextView;
    private TextView mStatusProgressTextView;
    private ImageView mMoreIcon, mCampaignType;
    private ProgressBar mProgressBar;
    private ItemClickListener itemClickListener;
//    private ImageView mImageViewStatus;

    public DatabaseReference mCampaignRef;
    public ValueEventListener mCampaignRefListener;

    public ChienDichViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mCampaignThumb = itemView.findViewById(R.id.unfollow_user_photo);
//        mVideoTitleTextView = itemView.findViewById(R.id.txtVideoTitle);
        mStatusProgressTextView = itemView.findViewById(R.id.txtStatusProgress);
//        mImageViewStatus = itemView.findViewById(R.id.imageViewStatus);
        mProgressBar = itemView.findViewById(R.id.campaign_progress);
        mMoreIcon = itemView.findViewById(R.id.campaign_more_ic);
        mCampaignType = itemView.findViewById(R.id.img_campaign_type);
        itemView.setOnClickListener(this);
        mMoreIcon.setOnClickListener(this);
    }

    public void setCampaignIcon(String channelThumb, int campaignType, Context context) {
        if (channelThumb != null && !channelThumb.equals("")) {
            Picasso.get().setLoggingEnabled(true);
            Picasso.get()
                    .load(channelThumb)
                    .transform(new CircleTransform())
                    .into(mCampaignThumb);
        }
        switch (campaignType) {
            case 1:
                mCampaignType.setImageResource(R.drawable.ic_subscriber);
                break;
            case 2:
                mCampaignType.setImageResource(R.drawable.ic_play);
                break;
            case 3:
                mCampaignType.setImageResource(R.drawable.ic_thumb_up_button);
                break;
        }

    }

//    public void setCampaignVideoTitle(String videoTitle) {
//        mVideoTitleTextView.setText(videoTitle);
//    }

    public void setProgressInfo(int currentLiked, int totalOrderLike, int campaignType) {
        switch (campaignType) {
            case 1:
                mStatusProgressTextView.setText("" + currentLiked + "/" + totalOrderLike + " Subs");
                break;
            case 2:
                mStatusProgressTextView.setText("" + currentLiked + "/" + totalOrderLike + " View");
                break;
            case 3:
                mStatusProgressTextView.setText("" + currentLiked + "/" + totalOrderLike + " Like");
                break;
        }

        int progress = currentLiked * 100 / totalOrderLike;
        mProgressBar.setProgress(progress);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.campaign_more_ic) {
            itemClickListener.onMoreClick(v,getAdapterPosition());
        } else {
            itemClickListener.onClick(v, getAdapterPosition());
        }
    }

    public void setItemClickListener(ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onClick(View view, int position);
        void onMoreClick(View view, int position);
    }
}