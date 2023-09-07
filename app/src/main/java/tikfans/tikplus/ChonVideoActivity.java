package tikfans.tikplus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import tikfans.tikplus.model.ItemVideo;
import tikfans.tikplus.model.VideoAdapter;
import tikfans.tikplus.subviewlike.LikeTaoChienDichActivity;
import tikfans.tikplus.subviewlike.SubTaoChienDichActivity;
import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.CircleTransform;
import tikfans.tikplus.util.PreferenceUtil;
import tikfans.tikplus.util.SQLiteDatabaseHandler;

import static tikfans.tikplus.util.AppUtil.SELECTED_USER_IMG_FOR_CAMPAIGN_EXTRA;
import static tikfans.tikplus.util.AppUtil.SELECTED_USER_NAME_FOR_CAMPAIGN_EXTRA;
import static tikfans.tikplus.util.AppUtil.SELECTED_VIDEO_ID_STRING_EXTRA;
import static tikfans.tikplus.util.AppUtil.SELECTED_VIDEO_THUMBNAIL_STRING_EXTRA;
import static tikfans.tikplus.util.AppUtil.SELECTED_VIDEO_WEB_LINK_STRING_EXTRA;
import static tikfans.tikplus.util.AppUtil.convertStringToInteger;
import static tikfans.tikplus.util.FirebaseUtil.getSubCampaignsRef;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ChonVideoActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private RecyclerView mRecyclerView;
    private VideoAdapter mAdapter;
    private ArrayList<String> mRunningCampaignList;
    private RelativeLayout mNoVideoLayout, mAddVideoLayout;
    private int mCampaignType;
    private ImageView imgToobarBack;
    private ArrayList<ItemVideo> itemVideoArrayList = new ArrayList<>();
    private SQLiteDatabaseHandler db;
    private int mNumberOfCampaign = 0;
    int limitCampaign = 5;
    private String mSelectedVideoId = "NONE";
    private String mUserTikToklink = "";
    private EditText mEditTextYTURL;
    private Button mBtnAdd;
    String mUserNameForCampaign;
    String mUserImgForCampaign;
    String mInputUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        db = new SQLiteDatabaseHandler(this);
        itemVideoArrayList = db.getAllItemVideo();
        mRunningCampaignList = intent.getStringArrayListExtra(AppUtil.RUNNING_CAMPAIGN_PATH_STRING_EXTRA);
        mCampaignType = intent.getIntExtra(AppUtil.CREATE_CAMPAIGN_TYPE, AppUtil.SUB_CAMPAIGN_TYPE);
        mNumberOfCampaign = intent.getIntExtra(AppUtil.NUMBER_CAMPAIGN_PATH_STRING_EXTRA, 0);
        setContentView(R.layout.activity_video_selector);

        mUserNameForCampaign = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_NAME_FOR_CAMPAIGN, "NONE");
        mUserImgForCampaign = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_PHOTO_FOR_CAMPAIGN, "NONE");
        mInputUserName = mUserNameForCampaign;
        boolean isListVideoFromCurrentUser = PreferenceUtil.getBooleanPref(PreferenceUtil.IS_LIST_VIDEO_FROM_CURRENT_USER, true);

        mEditTextYTURL = findViewById(R.id.edittext_yt_url);
        mBtnAdd = findViewById(R.id.btn_add);
        mAddVideoLayout = findViewById(R.id.add_video_layout);
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mInputUserName = mEditTextYTURL.getText().toString();
                if (mInputUserName.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.please_input_user_name), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mInputUserName.charAt(0) == '@') {
                    mInputUserName = mInputUserName.substring(1);
                }
                mUserTikToklink = AppUtil.TIKTOK_PREFIX_LINK + mInputUserName;
                getVideoListByWebview();
            }
        });

        Toolbar toolbar = findViewById(R.id
                .toolbar);
        imgToobarBack = findViewById(R.id.toolbar_back);
        setSupportActionBar(toolbar);

        imgToobarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        limitCampaign = 5;
        if (MyChannelApplication.isVipAccount) {
            limitCampaign = 10;
        }

        mRecyclerView = findViewById(R.id.recycle_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(ChonVideoActivity.this, 3));
        mNoVideoLayout = findViewById(R.id.no_campaign_layout);
        mAdapter = new VideoAdapter(itemVideoArrayList, ChonVideoActivity.this);
        mAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mNumberOfCampaign >= limitCampaign) {
                    try {
                        AppUtil.showAlertDialog(ChonVideoActivity.this, getString(R.string.campaign_limitation), String.format(getString(R.string.campaign_limitation_details), limitCampaign));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
                startCampaignWithSelectedVideo(mAdapter.getList().get(position));
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        progressDialog = new ProgressDialog(this);
        String username = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_NAME, "");
        mUserTikToklink = AppUtil.TIKTOK_PREFIX_LINK + username;
        if (itemVideoArrayList.size() == 0) {
            getVideoListByWebview();
        }

    }

    private void startCampaignWithSelectedVideo(ItemVideo selectedVideo) {
        if (mCampaignType == AppUtil.SUB_CAMPAIGN_TYPE) {
            boolean isRunningCampaign = false;
            if (mRunningCampaignList != null && mRunningCampaignList.size() > 0) {
                String mUserName = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_NAME, "NONE");
                for (int i = 0; i < mRunningCampaignList.size(); i++) {
                    if (mRunningCampaignList.get(i).equals(mUserName)) {
                        isRunningCampaign = true;
                    }
                }
            }
            if (isRunningCampaign) {
                AlertDialog alertDialog = new AlertDialog.Builder(ChonVideoActivity.this).setTitle(getString(R.string.create_campaign_failed)).setMessage(getString(R.string.create_campaign_dupplicated)).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
                alertDialog.show();
            } else {
                Intent i = new Intent(ChonVideoActivity.this, SubTaoChienDichActivity.class);
                i.putExtra(SELECTED_VIDEO_WEB_LINK_STRING_EXTRA, selectedVideo.getWebVideoUrl());
                i.putExtra(SELECTED_VIDEO_ID_STRING_EXTRA, selectedVideo.getId());
                i.putExtra(SELECTED_USER_NAME_FOR_CAMPAIGN_EXTRA, mUserNameForCampaign);
                i.putExtra(SELECTED_USER_IMG_FOR_CAMPAIGN_EXTRA, mUserImgForCampaign);
                startActivityForResult(i, AppUtil.INTENT_REQUEST_CODE_FOR_CREATE_CAMPAIGN);
            }
        } else {
            boolean isRunningCampaign = false;
            if (mRunningCampaignList != null && mRunningCampaignList.size() > 0) {
                for (int i = 0; i < mRunningCampaignList.size(); i++) {
                    if (mRunningCampaignList.get(i).equals(selectedVideo.getId())) {
                        isRunningCampaign = true;
                    }
                }
            }
            if (isRunningCampaign) {
                AlertDialog alertDialog = new AlertDialog.Builder(ChonVideoActivity.this).setTitle(getString(R.string.create_campaign_failed)).setMessage(getString(R.string.create_campaign_dupplicated)).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
                alertDialog.show();
            } else {
                mSelectedVideoId = selectedVideo.getId();
                Intent i = new Intent(ChonVideoActivity.this, LikeTaoChienDichActivity.class);
                i.putExtra(SELECTED_VIDEO_THUMBNAIL_STRING_EXTRA, selectedVideo.getImageUrl());
                i.putExtra(SELECTED_VIDEO_WEB_LINK_STRING_EXTRA, selectedVideo.getWebVideoUrl());
                i.putExtra(SELECTED_VIDEO_ID_STRING_EXTRA, selectedVideo.getId());
                i.putExtra(SELECTED_USER_NAME_FOR_CAMPAIGN_EXTRA, mUserNameForCampaign);
                startActivityForResult(i, AppUtil.INTENT_REQUEST_CODE_FOR_CREATE_CAMPAIGN);
            }
        }
    }

    final String regex = "(?:\\(['\"]?)(.*?)(?:['\"]?\\))"; //lấy link ảnh từ background url
    final Pattern pattern = Pattern.compile(regex);

    private int getDataFailedCount = 0;

    private void getVideoListByWebview() {
        showProgressDialog();
        Log.e("khang", "onCreate: link " + mUserTikToklink);
        //newGetData
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {

                //Background work here
                Document document = null;
                try {
                    document = Jsoup.connect(mUserTikToklink).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Document finalDocument1 = document;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressDialog();
                        try {
                            JSONObject listVideoJSONObject = new JSONObject(finalDocument1.getElementById("SIGI_STATE").data()).getJSONObject("ItemModule");
                            Iterator<String> keys = listVideoJSONObject.keys();
                            ArrayList<ItemVideo> tmpList = new ArrayList<>();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                JSONObject object = listVideoJSONObject.getJSONObject(key);
                                // Now you can access the data within each individual JSONObject
                                ItemVideo item = new ItemVideo();
                                String id = object.getString("id");
                                item.setId(id);
                                item.setImageUrl(object.getJSONObject("video").getString("cover"));
                                tmpList.add(item);
                                if (tmpList.size() > 0) {
                                    itemVideoArrayList = tmpList;
                                }
                                if (itemVideoArrayList.size() > 0) {
                                    try {
                                        String avatarUrl = finalDocument1.getElementsByClass("tiktok-1zpj2q-ImgAvatar").get(0).attributes().get("src");
                                        if (avatarUrl != null && !avatarUrl.equals("")) {
                                            mUserNameForCampaign = mInputUserName;
                                            mUserImgForCampaign = avatarUrl;
                                            PreferenceUtil.saveStringPref(PreferenceUtil.TIKTOK_USER_PHOTO_FOR_CAMPAIGN, avatarUrl);
                                            PreferenceUtil.saveStringPref(PreferenceUtil.TIKTOK_USER_NAME_FOR_CAMPAIGN, mInputUserName);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    PreferenceUtil.saveBooleanPref(PreferenceUtil.IS_LIST_VIDEO_FROM_CURRENT_USER, false);

                                    db.removeAll();
                                    for (int i = 0; i < itemVideoArrayList.size(); i++) {
                                        ItemVideo itemVideo = itemVideoArrayList.get(i);
                                        db.addItemVideo(itemVideo);
                                    }
                                    mNoVideoLayout.setVisibility(View.INVISIBLE);
                                    mRecyclerView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mAdapter.setList(itemVideoArrayList);
                                            mRecyclerView.setAdapter(mAdapter);
                                            mRecyclerView.invalidate();
                                        }
                                    });
//                                    mAddVideoLayout.setVisibility(View.GONE);
                                } else {
//                                    mAddVideoLayout.setVisibility(View.VISIBLE);
//                                if (itemVideoArrayList.size() == 0 || !isListVideoFromCurrentUser) {
                                    mNoVideoLayout.setVisibility(View.VISIBLE);
                                    mAddVideoLayout.setVisibility(View.VISIBLE);
//                                }
                                }
                                // Use the extracted data as needed
                            }

                            if (itemVideoArrayList.size() == 0) {
                                Log.e("khang", "onLoadFinish: empty video");
//                                mAddVideoLayout.setVisibility(View.VISIBLE);
                                mNoVideoLayout.setVisibility(View.VISIBLE);
                                mAddVideoLayout.setVisibility(View.VISIBLE);
                            } else {
                                Log.e("khang", "onLoadFinish: list video exist");
                                mNoVideoLayout.setVisibility(View.GONE);
                                mAddVideoLayout.setVisibility(View.GONE);

                            }
                            getDataFailedCount = 0;
                        } catch (Exception e) {
                            getDataFailedCount++;
                            if (getDataFailedCount < 3) {
                                getVideoListByWebview();
                            } else {
                                getDataFailedCount = 0;
                                Toast.makeText(ChonVideoActivity.this, getString(R.string.username_not_found), Toast.LENGTH_LONG).show();
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }

                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AppUtil.INTENT_REQUEST_CODE_FOR_CREATE_CAMPAIGN && resultCode == Activity.RESULT_OK) {
            mNumberOfCampaign++;
            if (mCampaignType == AppUtil.SUB_CAMPAIGN_TYPE) {
                String mUserName = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_NAME, "NONE");
                mRunningCampaignList.add(mUserName);
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                if (mNumberOfCampaign >= limitCampaign) {
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    if (!mSelectedVideoId.equals("NONE")) {
                        mRunningCampaignList.add(mSelectedVideoId);
                    }
                }
            }
        }
    }


    private void hideProgressDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showProgressDialog() {
        try {
            if (progressDialog != null && !progressDialog.isShowing()) {
                progressDialog.setTitle(R.string.wait_a_moment);
                progressDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
