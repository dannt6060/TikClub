package tikfans.tikplus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import com.google.firebase.auth.FirebaseAuth;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import tikfans.tikplus.model.ItemVideo;
import tikfans.tikplus.model.ResultUser;
import tikfans.tikplus.model.ResultVideo;
import tikfans.tikplus.model.VideoAdapter;
import tikfans.tikplus.subviewlike.LikeTaoChienDichActivity;
import tikfans.tikplus.subviewlike.SubTaoChienDichActivity;
import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.PreferenceUtil;
import tikfans.tikplus.util.SQLiteDatabaseHandler;

import static tikfans.tikplus.util.AppUtil.SELECTED_USER_IMG_FOR_CAMPAIGN_EXTRA;
import static tikfans.tikplus.util.AppUtil.SELECTED_USER_NAME_FOR_CAMPAIGN_EXTRA;
import static tikfans.tikplus.util.AppUtil.SELECTED_VIDEO_ID_STRING_EXTRA;
import static tikfans.tikplus.util.AppUtil.SELECTED_VIDEO_THUMBNAIL_STRING_EXTRA;
import static tikfans.tikplus.util.AppUtil.SELECTED_VIDEO_WEB_LINK_STRING_EXTRA;
import static tikfans.tikplus.util.AppUtil.convertStringToInteger;

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
    private ArrayList<ItemVideo> list = new ArrayList<>();
    private SQLiteDatabaseHandler db;
    private int mNumberOfCampaign = 0;
    int limitCampaign = 5;
    private String mSelectedVideoId = "NONE";
    private String mUserTikToklink = "";
    private EditText mEditTextYTURL;
    private Button mBtnAdd;
    String mUserNameForCampaign;
    String mUserImgForCampaign;
    boolean isNeedShowAddVideoLayout = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        db = new SQLiteDatabaseHandler(this);
        list = db.getAllItemVideo();
        mRunningCampaignList = intent.getStringArrayListExtra(AppUtil.RUNNING_CAMPAIGN_PATH_STRING_EXTRA);
        mCampaignType = intent.getIntExtra(AppUtil.CREATE_CAMPAIGN_TYPE, AppUtil.SUB_CAMPAIGN_TYPE);
        mNumberOfCampaign = intent.getIntExtra(AppUtil.NUMBER_CAMPAIGN_PATH_STRING_EXTRA, 0);
        setContentView(R.layout.activity_video_selector);

        mWebView = findViewById(R.id.webView);
        mWebView.setVisibility(View.INVISIBLE);
        tiktokWebClient = new TiktokWebClient(ChonVideoActivity.this, mWebView);

        mUserNameForCampaign = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_NAME, "NONE");
        mUserImgForCampaign = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_PHOTO, "NONE");

        mEditTextYTURL = findViewById(R.id.edittext_yt_url);
        mBtnAdd = findViewById(R.id.btn_add);
        mAddVideoLayout = findViewById(R.id.add_video_layout);
        mBtnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ItemURLTiktokForWebView url = new ItemURLTiktokForWebView(mEditTextYTURL.getText().toString(), null);
                if (url.getTYPE_URL() == ItemURLTiktokForWebView.URL_TYPE_VIDEO) {
                    tiktokWebClient.load(url.getBaseUrl());
                    tiktokWebClient.setListener(new TiktokWebClient.ClientListener() {
                        @Override
                        public void onLoading() {
                            showProgressDialog();
                        }

                        @Override
                        public void onLoadFinish(Document document, String url) {
                            Log.e("khang", "onLoadFinish: " + url);
                            hideProgressDialog();

                            try {
                                String imageUrl = "";
                                String baseUrl = "";
                                Elements findImgUrlElementList = document.getElementsByTag("meta");
                                for (Element e:findImgUrlElementList) {
                                    String content = e.attributes().get("content");
                                    if (content.contains("expires=")) {
                                        imageUrl = content;
                                    }
                                    if (content.contains("/video/")) {
                                        baseUrl = content;
                                        break;
                                    }
                                }
                                mUserImgForCampaign = imageUrl;
                                mUserNameForCampaign = baseUrl.substring(baseUrl.lastIndexOf("@") + 1, baseUrl.lastIndexOf("/video/"));
                                String videoId = baseUrl.substring(baseUrl.lastIndexOf("/") + 1, baseUrl.lastIndexOf("/") + 20);

                                Log.d("khang", "userName: " + mUserNameForCampaign + " / " + mUserImgForCampaign + " / " + videoId + " / " + baseUrl + " / " + imageUrl);

                                ItemVideo item = new ItemVideo();
                                item.setWebVideoUrl(baseUrl);
                                item.setId(videoId);
                                item.setImageUrl(imageUrl);
                                startCampaignWithSelectedVideo(item);


                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(ChonVideoActivity.this, getString(R.string.wrong_video_link), Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                } else {
                    Toast.makeText(ChonVideoActivity.this, getString(R.string.wrong_video_link), Toast.LENGTH_SHORT).show();
                }
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
        if (list.size() == 0) {
            mNoVideoLayout.setVisibility(View.VISIBLE);
        }
        mAdapter = new VideoAdapter(list, ChonVideoActivity.this);
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
        String userImg = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_PHOTO, "NONE");
        mUserTikToklink = AppUtil.TIKTOK_PREFIX_LINK + username;
        ItemURLTiktok url = new ItemURLTiktok(mUserTikToklink, listener);
        if (!userImg.equals("NONE")) {
            try {
                String expiredTime = userImg.substring(userImg.lastIndexOf("expires=") + 8, userImg.lastIndexOf("expires=") + 18);
                Log.d("khang", "expiredTime: " + expiredTime + " / " + System.currentTimeMillis());
                long l = Long.parseLong(expiredTime);
                if (l < System.currentTimeMillis() / 1000) {
                    Log.d("khang", "da het han" + expiredTime);
                    url.getUserInfo();
                } else {
                    Log.d("khang", "chua het han" + expiredTime);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            url.getUserInfo();
        }
        url.getListVideoFromUser();
        getVideoListByWebview();
        showProgressDialog();


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
                startActivityForResult(i, AppUtil.INTENT_REQUEST_CODE_FOR_CREATE_CAMPAIGN);
            }
        }
    }

    private TiktokWebClient tiktokWebClient;
    final String regex = "(?:\\(['\"]?)(.*?)(?:['\"]?\\))"; //lấy link ảnh từ background url
    final Pattern pattern = Pattern.compile(regex);
    private WebView mWebView;

    private void getVideoListByWebview() {
        ItemURLTiktok url = new ItemURLTiktok(mUserTikToklink, null);
        if (url.getTYPE_URL() == ItemURLTiktok.URL_TYPE_USER) {
            Log.e("khang", "onCreate: link " + url.getBaseUrl());

            tiktokWebClient.load(url.getBaseUrl());
            tiktokWebClient.setListener(new TiktokWebClient.ClientListener() {
                @Override
                public void onLoading() {
                    Log.e("khang", "onLoading: ");
                    showProgressDialog();
                }

                @Override
                public void onLoadFinish(Document document, String url) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgressDialog();
                            Log.e("khang", "onLoadFinish: " + url);


                            Elements listVideoElement = document.getElementsByClass("tiktok-a3te33-AVideoContainer e18clhtl2");
                            if (listVideoElement.size() > 0 && list.size() == 0) {
                                Log.e("khang", "onLoadFinish: video size: " + listVideoElement.size());
                                for (Element video : listVideoElement) {
                                    ItemVideo item = new ItemVideo();
                                    try {
                                        String videoUrl = video.attributes().get("href");
                                        String[] splitUrl = videoUrl.split("/");
                                        String id = splitUrl[splitUrl.length-1];
                                        item.setId(id);
                                        item.setWebVideoUrl(videoUrl);

                                        String style = video.attributes().get("style");
                                        style = style.replaceAll(Pattern.quote("&quot;"), "");
                                        final Matcher matcher = pattern.matcher(style);
                                        if (matcher.find()) {
                                            String imageUrl = matcher.group(0);
                                            imageUrl = imageUrl.replaceAll(Pattern.quote("(\""), "");
                                            imageUrl = imageUrl.replaceAll(Pattern.quote("\")"), "");
                                            item.setImageUrl(imageUrl);
                                        }


                                        Elements playCountE = video.getElementsByClass("tiktok-1g6bqj2-SpanPlayCount");
                                        if (playCountE.size() > 0) {
                                            String playCount = playCountE.get(0).text();
                                            item.setDiggCount(convertStringToInteger(playCount));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    list.add(item);
                                }


                                if (list != null && list.size() > 0) {
                                    db.removeAll();
                                    for (int i = 0; i < list.size(); i++) {
                                        ItemVideo itemVideo = list.get(i);
                                        db.addItemVideo(itemVideo);
                                    }
                                    mNoVideoLayout.setVisibility(View.INVISIBLE);
                                    mRecyclerView.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mAdapter.setList(list);
                                            mRecyclerView.setAdapter(mAdapter);
                                        }
                                    });
                                    mAddVideoLayout.setVisibility(View.GONE);
                                    isNeedShowAddVideoLayout = false;
                                } else {
                                    if (isNeedShowAddVideoLayout) {
                                        mAddVideoLayout.setVisibility(View.VISIBLE);
                                    } else {
                                        isNeedShowAddVideoLayout = true;
                                    }
                                }

                                mWebView.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mWebView.setVisibility(View.INVISIBLE);
                                    }
                                });

                            } else if (listVideoElement.size() == 0) {
                                Log.e("khang", "onLoadFinish: empty video");
                                if (isNeedShowAddVideoLayout) {
                                    mAddVideoLayout.setVisibility(View.VISIBLE);
                                } else {
                                    isNeedShowAddVideoLayout = true;
                                }
                            } else {
                                Log.e("khang", "onLoadFinish: list video exist");
                            }
                        }
                    });

                }
            });
        }
    }

    ItemURLTiktok.ClientTikTokListener listener = new ItemURLTiktok.ClientTikTokListener() {
        @Override
        public void onLoading() {
            showProgressDialog();
        }

        @Override
        public void onReceivedListVideo(ResultVideo result) {
            List<ItemVideo> itemVideoList = result.getResult();
            if (itemVideoList != null && itemVideoList.size() > 0) {
                hideProgressDialog();
                db.removeAll();
                for (int i = 0; i < itemVideoList.size(); i++) {
                    ItemVideo itemVideo = itemVideoList.get(i);
                    Log.d("khang", "byAPI: " + itemVideo.getWebVideoUrl());
                    db.addItemVideo(itemVideo);
                }
                mNoVideoLayout.setVisibility(View.INVISIBLE);
                mAdapter.getList().clear();
                mAdapter.getList().addAll(result.getResult());
                mAdapter.notifyDataSetChanged();
                mAddVideoLayout.setVisibility(View.GONE);
                isNeedShowAddVideoLayout = false;
            } else {
                if (isNeedShowAddVideoLayout) {
                    mAddVideoLayout.setVisibility(View.VISIBLE);
                } else {
                    isNeedShowAddVideoLayout = true;
                }
            }
        }

        @Override
        public void onReceivedUserInfo(final ResultUser result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (result != null) {
                        String img = "";
                        if (result.getResult() != null && result.getResult().getCovers() != null) {
                            img = result.getResult().getCovers().get(0);
                        }
                        if (img.equals("") && result.getResult() != null && result.getResult().getCoversMedium() != null) {
                            img = result.getResult().getCoversMedium().get(0);
                        }
                        if (!img.equals("")) {
                            PreferenceUtil.saveStringPref(PreferenceUtil.TIKTOK_USER_PHOTO, img);
                        }
                    }
                }
            });
            hideProgressDialog();
        }

        @Override
        public void onError(int code, String mess) {
            hideProgressDialog();
            Toast.makeText(ChonVideoActivity.this, mess, Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onInvalidLink() {
            hideProgressDialog();
            Toast.makeText(ChonVideoActivity.this, "onInvalidLink", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCheckLinkDone() {
            hideProgressDialog();
        }
    };

    private void signOut() {
        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseAuth.signOut();
        startActivity(new Intent(getApplication(), ManHinhDangNhapActivity.class));
        finish();
    }

    public java.lang.String extractYTId(String url) throws MalformedURLException {
        if (url.contains("youtu.be")) {
            java.lang.String[] param = url.split("/");
            java.lang.String id = null;
            if (param.length == 4) {
                id = param[3];
            }
            return id;
        } else {
            java.lang.String query = new URL(url).getQuery();
            java.lang.String[] param = query.split("&");
            java.lang.String id = null;
            for (java.lang.String row : param) {
                java.lang.String[] param1 = row.split("=");
                if (param1[0].equals("v")) {
                    id = param1[1];
                }
            }
            return id;
        }
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
