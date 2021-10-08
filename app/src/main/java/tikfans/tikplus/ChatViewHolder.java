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

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import tikfans.tikplus.model.Message;
import tikfans.tikplus.util.CircleTransform;

public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final View mView;

    private ImageView mVideoThumb;
    private TextView mMessageText;
    private ImageView mMoreIcon;
    private TextView mUserrName;
    private ItemClickListener itemClickListener;
    public DatabaseReference mCampaignRef;
    public ValueEventListener mCampaignRefListener;

    public ChatViewHolder(View itemView) {
        super(itemView);
        mView = itemView;
        mVideoThumb = itemView.findViewById(R.id.message_item_video_thumb);
        mUserrName = itemView.findViewById(R.id.message_item_name);
        mMessageText = itemView.findViewById(R.id.txt_item_message);
        mMoreIcon = itemView.findViewById(R.id.campaign_more_ic);
        itemView.setOnClickListener(this);
        mMoreIcon.setOnClickListener(this);
    }

    public void setInfo(Message message) {
        String img_url = message.getuImg();
        Picasso.get().setLoggingEnabled(true);
        Picasso.get()
                .load(img_url)
                .transform(new CircleTransform())
                .into(mVideoThumb);
        mMessageText.setText(message.getMess());
        mUserrName.setText("@"+message.getuName());
    }



    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.campaign_more_ic) {
            itemClickListener.onMoreClick(v, getAdapterPosition());
        } else {
            itemClickListener.onClick(v, getAdapterPosition());
        }
    }

    public void setItemClickListener(ChatViewHolder.ItemClickListener itemClickListener)
    {
        this.itemClickListener = itemClickListener;
    }


    public interface ItemClickListener {
        void onClick(View view, int position);

        void onMoreClick(View view, int position);
    }
}