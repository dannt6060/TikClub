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

package tikfans.tikfollow.tik.tok.followers.likes;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import tikfans.tikfollow.tik.tok.followers.likes.model.UnFollowUSer;
import tikfans.tikfollow.tik.tok.followers.likes.util.CircleTransform;

public class
UnfollowUserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ImageView mUserPhoto;
    private TextView mUserName;
    private ItemClickListener itemClickListener;

    public UnfollowUserViewHolder(View itemView) {
        super(itemView);
        mUserPhoto = itemView.findViewById(R.id.unfollow_user_photo);
        mUserName = itemView.findViewById(R.id.txt_unfollow_user_name);
        itemView.findViewById(R.id.btn_follow_again).setOnClickListener(this);
        itemView.setOnClickListener(this);
    }

    public void setInfo(UnFollowUSer message) {
        String img_url = message.getUserImg();
        if (img_url != null) {
            Picasso.get().setLoggingEnabled(true);
            Picasso.get()
                    .load(img_url)
                    .transform(new CircleTransform())
                    .into(mUserPhoto);
        }
        if (message.getUserName() != null) {
            mUserName.setText("@" + message.getUserName());
        }
    }


    @Override
    public void onClick(View v) {
        itemClickListener.onClick(v, getAdapterPosition());
    }

    public void setItemClickListener(UnfollowUserViewHolder.ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


    public interface ItemClickListener {
        void onClick(View view, int position);

    }
}