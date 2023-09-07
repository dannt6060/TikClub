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

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import tikfans.tikplus.model.ItemVideo;
import tikfans.tikplus.subviewlike.LikeChiTietChienDichActivity;
import tikfans.tikplus.model.LikeCampaign;
import tikfans.tikplus.subviewlike.SubChiTietChienDichActivity;
import tikfans.tikplus.util.FirebaseUtil;
import tikfans.tikplus.model.ChienDichCuaNguoiDungHienTai;
import tikfans.tikplus.model.SubCampaign;
import tikfans.tikplus.util.PreferenceUtil;
import tikfans.tikplus.util.SQLiteDatabaseHandler;

import static tikfans.tikplus.util.AppUtil.CAMPAIGN_PATH_STRING_EXTRA;

public class FirebaseCampaignsQueryAdapter extends RecyclerView.Adapter<ChienDichViewHolder> {
    private final String TAG = "Khang";
    private List<Pair<String, ChienDichCuaNguoiDungHienTai>> mCampaignPaths;
    private OnSetupViewListener mOnSetupViewListener;
    private FragmentActivity mActivity;

    public FirebaseCampaignsQueryAdapter(List<Pair<String, ChienDichCuaNguoiDungHienTai>> campaignPaths, OnSetupViewListener onSetupViewListener) {
        if (campaignPaths == null || campaignPaths.isEmpty()) {
            mCampaignPaths = new ArrayList<>();
        } else {
            mCampaignPaths = campaignPaths;
        }
        mOnSetupViewListener = onSetupViewListener;
    }

    @Override
    public ChienDichViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.campaign_list_item, parent, false);
        return new ChienDichViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ChienDichViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        if (mCampaignPaths.get(position).second.getType().equals(FirebaseUtil.SUB_CAMPAIGNS)) {
            //for sub campaign
            DatabaseReference ref = FirebaseUtil.getSubCampaignsRef().child(mCampaignPaths.get(position).second.getKey());
            // TODO: Fix this so async event won't bind the wrong view post recycle.
            ValueEventListener campaignListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    SubCampaign subCampaign = dataSnapshot.getValue(SubCampaign.class);
                    Log.d(TAG, "subCampaign key: " + dataSnapshot.getRef());
                    if (subCampaign == null) {
                        Log.d(TAG, "delete: " + dataSnapshot.getRef() + " / " + mCampaignPaths.get(position).second.getKey());
//                        FirebaseUtil.getCampaignCurrentUser().child(mCampaignPaths.get(position).first).removeValue();
                        return;
                    }
                    // to update image
                    try {
                        String img = subCampaign.getUserImg();
                        String expiredTime = img.substring(img.lastIndexOf("expires=") + 8, img.lastIndexOf("expires=") + 18);
                        Log.d("khang", "expiredTime: " + expiredTime + " / " + System.currentTimeMillis());

                        long l = Long.parseLong(expiredTime);
                        if (l < System.currentTimeMillis() / 1000) {
                            Log.d("khang", "da het han" + expiredTime);
                            String mUserNameForCampaign = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_NAME_FOR_CAMPAIGN, "NONE");
                            String mUserImgForCampaign = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_PHOTO_FOR_CAMPAIGN, "NONE");
                            if (subCampaign.getUserName().equals(mUserNameForCampaign)) {
                                subCampaign.setUserImg(mUserImgForCampaign);
                                dataSnapshot.getRef().child("userImg").setValue(mUserImgForCampaign);
                            }
                        } else {
                            Log.d("khang", "chua het han" + expiredTime);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mOnSetupViewListener.onSetupView(holder, subCampaign, null);
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                    Log.e(TAG, "Error occurred: " + firebaseError.getMessage());
                }
            };
            ref.addListenerForSingleValueEvent(campaignListener);
            holder.mCampaignRef = ref;
            holder.mCampaignRefListener = campaignListener;
            holder.setItemClickListener(new ChienDichViewHolder.ItemClickListener() {
                @Override
                public void onClick(View view, int position) {
                    if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                        Intent intent = new Intent(mActivity, SubChiTietChienDichActivity.class);
                        intent.putExtra(CAMPAIGN_PATH_STRING_EXTRA, mCampaignPaths.get(position).second.getKey());
                        mActivity.startActivity(intent);
                    } else {
                        // only for gingerbread and newer versions
                        Log.d("Khang", "do not support android version: " + android.os.Build.VERSION.SDK_INT);
                    }
                }

                @Override
                public void onMoreClick(View view, final int position) {
                    PopupMenu popup = new PopupMenu(mActivity, view);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            new AlertDialog.Builder(new ContextThemeWrapper(mActivity, R.style.myDialog))
                                    .setTitle(mActivity.getResources().getString(R.string.xoa_chien_dich))
                                    .setMessage(mActivity.getResources().getString(R.string.xoa_chien_dich_explain))
                                    .setNegativeButton(mActivity.getResources().getString(R.string.huy_bo), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setPositiveButton(mActivity.getResources().getString(R.string.xoa), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DatabaseReference campaignInUserRef = FirebaseUtil.getCampaignCurrentUser().child(mCampaignPaths.get(position).first);
                                            campaignInUserRef.removeValue();

                                            DatabaseReference currentCampaignRef = FirebaseUtil.getSubCampaignsRef().child(mCampaignPaths.get(position).second.getKey());
                                            currentCampaignRef.removeValue();

                                            DatabaseReference currentCampaignLogSubRef = FirebaseUtil.getLogSubRef().child(mCampaignPaths.get(position).second.getKey());
                                            currentCampaignLogSubRef.removeValue();
//                                            mCampaignPaths.remove(position - mViewCampaignPaths.size());
//                                            notifyDataSetChanged();
                                        }
                                    })
                                    .show();
                            return true;
                        }
                    });

                    popup.show();//showing popup menu
                }
            });
        } else if (mCampaignPaths.get(position).second.getType().equals(FirebaseUtil.LIKE_CAMPAIGNS)) {
            //for sub campaign
            DatabaseReference ref = FirebaseUtil.getLikeCampaignsRef().child(mCampaignPaths.get(position).second.getKey());
            // TODO: Fix this so async event won't bind the wrong view post recycle.
            ValueEventListener campaignListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    LikeCampaign likeCampaign = dataSnapshot.getValue(LikeCampaign.class);
                    Log.d(TAG, "likeCampaign key: " + dataSnapshot.getRef());
                    if (likeCampaign == null) {
                        Log.d(TAG, "delete: " + dataSnapshot.getRef() + " / " + mCampaignPaths.get(position).second.getKey());
//                        FirebaseUtil.getCampaignCurrentUser().child(mCampaignPaths.get(position).first).removeValue();
                        return;
                    }

                    //update campaign img
                    try {
                        String img = likeCampaign.getVideoThumb();
                        String expiredTime = "0";
                        if (img != null && img.length() > 50) {
                            expiredTime = img.substring(img.lastIndexOf("expires=") + 8, img.lastIndexOf("expires=") + 18);
                        }
                        Log.d("khangcheckfollow", "expiredTime: " + expiredTime + " / " + System.currentTimeMillis());

                        long l = Long.parseLong(expiredTime);
                        if (l < System.currentTimeMillis() / 1000) {
                            if (itemVideoArrayList != null && itemVideoArrayList.size() > 0) {
                                for (int i = 0; i < itemVideoArrayList.size(); i++) {
                                    if (likeCampaign.getVideoId().equals(itemVideoArrayList.get(i).getId())) {
                                        likeCampaign.setVideoThumb(itemVideoArrayList.get(i).getImageUrl());
                                        dataSnapshot.getRef().child("videoThumb").setValue(itemVideoArrayList.get(i).getImageUrl());
                                    }
                                }
                            }

                        }
                    } catch (Exception e) {
                        FirebaseCrashlytics.getInstance().recordException(e);
                    }
                    mOnSetupViewListener.onSetupView(holder, null, likeCampaign);
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                    Log.e(TAG, "Error occurred: " + firebaseError.getMessage());
                }
            };
            ref.addListenerForSingleValueEvent(campaignListener);
            holder.mCampaignRef = ref;
            holder.mCampaignRefListener = campaignListener;
            holder.setItemClickListener(new ChienDichViewHolder.ItemClickListener() {
                @Override
                public void onClick(View view, int position) {
                    if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                        Intent intent = new Intent(mActivity, LikeChiTietChienDichActivity.class);
                        intent.putExtra(CAMPAIGN_PATH_STRING_EXTRA, mCampaignPaths.get(position).second.getKey());
                        mActivity.startActivity(intent);
                    } else {
                        // only for gingerbread and newer versions
                        Log.d("Khang", "do not support android version: " + android.os.Build.VERSION.SDK_INT);
                    }
                }

                @Override
                public void onMoreClick(View view, final int position) {
                    PopupMenu popup = new PopupMenu(mActivity, view);
                    //Inflating the Popup using xml file
                    popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());

                    //registering popup with OnMenuItemClickListener
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {
                            new AlertDialog.Builder(new ContextThemeWrapper(mActivity, R.style.myDialog))
                                    .setTitle(mActivity.getResources().getString(R.string.xoa_chien_dich))
                                    .setMessage(mActivity.getResources().getString(R.string.xoa_chien_dich_explain))
                                    .setNegativeButton(mActivity.getResources().getString(R.string.huy_bo), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setPositiveButton(mActivity.getResources().getString(R.string.xoa), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            DatabaseReference campaignInUserRef = FirebaseUtil.getCampaignCurrentUser().child(mCampaignPaths.get(position).first);
                                            campaignInUserRef.removeValue();

                                            DatabaseReference currentCampaignRef = FirebaseUtil.getLikeCampaignsRef().child(mCampaignPaths.get(position).second.getKey());
                                            currentCampaignRef.removeValue();

                                            DatabaseReference currentCampaignLogLikeRef = FirebaseUtil.getLogLikeRef().child(mCampaignPaths.get(position).second.getKey());
                                            currentCampaignLogLikeRef.removeValue();
                                        }
                                    })
                                    .show();
                            return true;
                        }
                    });

                    popup.show();//showing popup menu
                }
            });
        }
    }

    @Override
    public void onViewRecycled(ChienDichViewHolder holder) {
        super.onViewRecycled(holder);
        holder.mCampaignRef.removeEventListener(holder.mCampaignRefListener);
    }

    @Override
    public int getItemCount() {
        return mCampaignPaths.size();
    }

    private SQLiteDatabaseHandler db;
    ArrayList<ItemVideo> itemVideoArrayList;
    public void setActivity(FragmentActivity activity) {
        this.mActivity = activity;

        db = new SQLiteDatabaseHandler(mActivity);
        try {
            itemVideoArrayList = db.getAllItemVideo();
        } catch (Exception e) {
            FirebaseCrashlytics.getInstance().recordException(e);
        }

    }

    public interface OnSetupViewListener {
        void onSetupView(tikfans.tikplus.ChienDichViewHolder holder, SubCampaign subCampaign, LikeCampaign likeCampaign);
    }
}
