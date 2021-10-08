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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import tikfans.tikplus.model.Message;
import tikfans.tikplus.util.AppUtil;

public class FirebaseChatQueryAdapter extends RecyclerView.Adapter<ChatViewHolder> {
    private final String TAG = "Khang";
    private ArrayList<Message> mMessageList;
    private OnSetupViewListener mOnSetupViewListener;
    private FragmentActivity mActivity;

    public FirebaseChatQueryAdapter(ArrayList<Message> mMessageList, OnSetupViewListener onSetupViewListener) {
        if (mMessageList == null || mMessageList.isEmpty()) {
            this.mMessageList = new ArrayList<>();
        } else {
            this.mMessageList = mMessageList;
        }
        mOnSetupViewListener = onSetupViewListener;
    }

    @Override
    public ChatViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_list_item, parent, false);
        return new ChatViewHolder(v);
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

    @Override
    public void onBindViewHolder(final ChatViewHolder holder, final int position) {
        mOnSetupViewListener.onSetupView(holder, mMessageList.get(position));
        holder.setItemClickListener(new ChatViewHolder.ItemClickListener() {
            @Override
            public void onClick(View view, int position) {
                visitTiktokProfiles(mMessageList.get(position).getuName());
            }

            @Override
            public void onMoreClick(View view, int position) {
                PopupMenu popup = new PopupMenu(mActivity, view);
                //Inflating the Popup using xml file
                popup.getMenuInflater().inflate(R.menu.chat_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        Toast.makeText(mActivity, mActivity.getString(R.string.cam_on_vi_bao_cao), Toast.LENGTH_SHORT).show();
                        return false;
                    }
                });
                popup.show();
            }
        });
    }

    @Override
    public void onViewRecycled(ChatViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public void setActivity(FragmentActivity activity) {
        this.mActivity = activity;
    }

    public interface OnSetupViewListener {
        void onSetupView(ChatViewHolder holder, Message message);
    }
}
