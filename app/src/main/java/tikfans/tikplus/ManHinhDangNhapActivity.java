package tikfans.tikplus;


import static tikfans.tikplus.util.FirebaseUtil.TOKEN_REF;

import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.messaging.FirebaseMessaging;

import android.util.Log;
import android.app.ProgressDialog;

import android.content.Intent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tikfans.tikplus.model.ItemVideo;
import tikfans.tikplus.model.ResultUser;
import tikfans.tikplus.model.ResultVideo;
import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.FirebaseUtil;
import tikfans.tikplus.util.PreferenceUtil;
import tikfans.tikplus.util.SQLiteDatabaseHandler;

public class ManHinhDangNhapActivity extends AppCompatActivity {

    // [START declare_auth]
    private FirebaseAuth mAuth;
    private ProgressDialog progressDialog;
    private EditText mEditTextUserName;

    private Button mLoginButton;
    //    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private TextView mTextViewPrivacyPolicy;
    private String username;
    private SQLiteDatabaseHandler db;
    private boolean isGetDataFromWebviewOrAPI = false;
    private boolean isFailToGetData = false;
    private ImageView mImgHelpGetUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_login);

        db = new SQLiteDatabaseHandler(this);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        mLoginButton = findViewById(R.id.google_sign_in_button);
        mEditTextUserName = findViewById(R.id.login_edit_text_user_name);
        mImgHelpGetUserName = findViewById(R.id.help_get_user_name);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        mTextViewPrivacyPolicy = findViewById(R.id.txt_privacy_policy);
        mTextViewPrivacyPolicy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse("https://sites.google.com/site/cptstudioprivacypolicy/privacy-policy"));
                startActivity(intent);
            }
        });
        //for checking follow
        mWebView = findViewById(R.id.webView);
        mWebView.setVisibility(View.INVISIBLE);
        tiktokWebClient = new TiktokWebClient(getApplicationContext(), mWebView);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if mFirebaseUser is signed in (non-null) and update UI accordingly.
        hideProgressDialog();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser, null);
    }


    private void updateUI(FirebaseUser user, String img) {
        hideProgressDialog();
        if (user != null) {


            String countryCodeValue = "all";
            countryCodeValue = getApplicationContext().getResources().getConfiguration().locale.getCountry();
            final DatabaseReference currentUserRef = FirebaseUtil.getAccountRef().child(user.getUid());

            Map<String, Object> updateValues = new HashMap<>();
            String referredByID = PreferenceUtil.getStringPref(PreferenceUtil.REFERRED_BY, "");
            boolean isReferReward = PreferenceUtil.getBooleanPref(PreferenceUtil.REFERRED_REWARDED, false);
            if (!referredByID.equals("") && !referredByID.equals(user.getUid()) && !isReferReward) {
                updateValues.put(FirebaseUtil.REFERRED_BY, referredByID);
                Log.d("Khang", "updated referred by: " + referredByID);
                PreferenceUtil.saveStringPref(PreferenceUtil.REFERRED_BY, "");
                PreferenceUtil.saveBooleanPref(PreferenceUtil.REFERRED_REWARDED, true);
            }
            updateValues.put(FirebaseUtil.USUB_LAST_SIGN_IN_AT, ServerValue.TIMESTAMP);
            updateValues.put(FirebaseUtil.COUNTRY_CODE_REF, countryCodeValue);
            if (img != null && !img.equals("")) {
                updateValues.put(FirebaseUtil.PHOTO_REF, img);
                PreferenceUtil.saveStringPref(PreferenceUtil.TIKTOK_USER_PHOTO, img);

            }
            if (username != null && !username.isEmpty()) {
                updateValues.put(FirebaseUtil.TIKTOK_USER_NAME, username);
                PreferenceUtil.saveStringPref(PreferenceUtil.TIKTOK_USER_NAME, username);
            }
            currentUserRef.updateChildren(updateValues, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    //for update token
                    if (databaseReference != null) {
                        Log.d("khang4", "update FCM token for: " + databaseReference);
                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(new OnCompleteListener<String>() {
                                    @Override
                                    public void onComplete(@NonNull Task<String> task) {
                                        if (!task.isSuccessful()) {
                                            Log.w("Khang", "Fetching FCM registration token failed", task.getException());
                                            return;
                                        }
                                        // Get new FCM registration token
                                        String token = task.getResult();
                                        databaseReference.child(TOKEN_REF).setValue(token);Log.d("khang4", "update FCM token for: " + databaseReference + " Token: " + token);

                                    }
                                });
                    }
                }
            });


            //for first purchase promotion
            long firstSignInTime = PreferenceUtil.getLongPref(PreferenceUtil.FIRST_SIGN_IN_TIME + user.getUid(), 0);
            if (firstSignInTime == 0) {
                PreferenceUtil.saveLongPref(PreferenceUtil.FIRST_SIGN_IN_TIME + user.getUid(), System.currentTimeMillis());
            }

            Intent intent = new Intent(this, MainActivity
                    .class);
            startActivity(intent);
            finish();
        } else {
        }
    }


    // [START signin]
    private void signIn() {
//        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
//        startActivityForResult(signInIntent, RC_SIGN_IN);
        username = mEditTextUserName.getText().toString();
        if (username.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.please_input_user_name), Toast.LENGTH_SHORT).show();
            mImgHelpGetUserName.setVisibility(View.VISIBLE);
            return;
        }
        if (username.charAt(0) == '@') {
            username = username.substring(1);
        }
        String link = AppUtil.TIKTOK_PREFIX_LINK + username;
        ItemURLTiktok url = new ItemURLTiktok(link, listener);
        url.getUserInfo();
        url.getListVideoFromUser();
        getUserInfoByWebView();

    }

    private WebView mWebView;
    private TiktokWebClient tiktokWebClient;

    private void getUserInfoByWebView() {
        final boolean[] isLoaded = {false}; // haom onloadFinish bi goi 2 lan, chi xu ly o lan goi dau tien
        showProgressDialog();
        username = mEditTextUserName.getText().toString();
        if (username.charAt(0) == '@') {
            username = username.substring(1);
        }
        String link = AppUtil.TIKTOK_PREFIX_LINK + username;
        ItemURLTiktok url = new ItemURLTiktok(link, null);
        if (url.getTYPE_URL() == ItemURLTiktok.URL_TYPE_USER) {
            Log.e("khang", "getUserInfoByWebView: user link " + url.getBaseUrl());
            tiktokWebClient.load(url.getBaseUrl());
            tiktokWebClient.setListener(new TiktokWebClient.ClientListener() {
                @Override
                public void onLoading() {
                    Log.e("khang", "onLoading: ");
                    showProgressDialog();
                }

                @Override
                public void onLoadFinish(Document document, String url) {
                    if (isLoaded[0]) return;
                    if (url.contains("notfound")) { //block ip from india so url is notfound
                        //if (isFailToGetData) {
                            hideProgressDialog();
                            mImgHelpGetUserName.setVisibility(View.VISIBLE);
                            Toast.makeText(ManHinhDangNhapActivity.this, "Login error, Please try again later", Toast.LENGTH_SHORT).show();
//                        } else {
//                            isFailToGetData = true;
//                        }
                        return;
                    }

                    //get uid:
                    String toGetUidString = document.getElementById("SIGI_STATE").data();
                    String uid = "";
                    uid = toGetUidString.substring(toGetUidString.indexOf("{\"id\":\"") + 7, toGetUidString.indexOf("{\"id\":\"") + 26);
                    if (uid.length() == 0 || uid.contains(":{")) {
                        mImgHelpGetUserName.setVisibility(View.VISIBLE);
//                        if (isFailToGetData) {
                            hideProgressDialog();
                            Toast.makeText(ManHinhDangNhapActivity.this, "Login error, Please try again later", Toast.LENGTH_SHORT).show();
//                        } else {
//                            isFailToGetData = true;
//                        }
                    } else {
                        String avatarUrl = document.getElementsByClass("tiktok-1zpj2q-ImgAvatar").get(0).attributes().get("src");
                        Log.d("Khang", "login: uid: " + uid + " / " + avatarUrl);
                        if (!isGetDataFromWebviewOrAPI) {
                            Log.d("khang", "sign in with webview");
                            signInWithFirebase(uid, avatarUrl);
                        }
                    }
                    isLoaded[0] = true;
                    mWebView.setVisibility(View.INVISIBLE);

                    /*
                    Log.e("khang", "getUserInfoByWebView onLoadFinish: " + url);
                    Elements listElement = document.getElementsByTag("meta");
                    String uid = "";
                    for (Element e : listElement) {
                        String content = e.attributes().get("content");
                        if (content.contains("user/profile/")) {
                            uid = content.substring(content.lastIndexOf("profile/") + 8, content.lastIndexOf("?"));
                            Log.d("khang", "getUID: " + uid);
                            break;
                        }
                    }

                    Elements user = document.getElementsByClass("user-page");
                    if (user.size() > 0) {
                        try {
                            Element avatar = user.get(0).getElementsByClass("user-profile-avatar").get(0).getElementsByTag("img").get(0);
                            String avatarUrl = avatar.attributes().get("src");
                            if (uid == null || uid.length() == 0) {
                                mImgHelpGetUserName.setVisibility(View.VISIBLE);
                                if (isFailToGetData) {
                                    hideProgressDialog();
                                    Toast.makeText(ManHinhDangNhapActivity.this, "Login error, Please try again later", Toast.LENGTH_SHORT).show();
                                } else {
                                    isFailToGetData = true;
                                }
                            } else {
                                if (!isGetDataFromWebviewOrAPI) {
                                    Log.d("khang", "sign in with webview");
                                    signInWithFirebase(uid, avatarUrl);
                                }
                            }
                            Log.e("khang", "onLoadFinish: Avatar Url: " + avatarUrl);
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (isFailToGetData) {
                                mImgHelpGetUserName.setVisibility(View.VISIBLE);
                                hideProgressDialog();
                                Toast.makeText(ManHinhDangNhapActivity.this, "Username is not correct, please check your tiktok username again", Toast.LENGTH_SHORT).show();
                            } else {
                                isFailToGetData = true;
                            }
                        }
                    }
                    isLoaded[0] = true;
                    mWebView.setVisibility(View.INVISIBLE);
                     */
                }
            });
        }
    }

    // [END signin]

    ItemURLTiktok.ClientTikTokListener listener = new ItemURLTiktok.ClientTikTokListener() {
        @Override
        public void onLoading() {
            showProgressDialog();
        }

        @Override
        public void onReceivedListVideo(ResultVideo result) {

            List<ItemVideo> itemVideoList = result.getResult();
            if (itemVideoList != null && itemVideoList.size() > 0) {
                for (int i = 0; i < itemVideoList.size(); i++) {
                    ItemVideo itemVideo = itemVideoList.get(i);
                    db.addItemVideo(itemVideo);
                }
            }
        }

        @Override
        public void onReceivedUserInfo(final ResultUser result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String img = "";
                    if (result.getResult() != null && result.getResult().getCovers() != null) {
                        img = result.getResult().getCovers().get(0);
                    }
                    if (img.equals("") && result.getResult() != null && result.getResult().getCoversMedium() != null) {
                        img = result.getResult().getCoversMedium().get(0);
                    }

                    if (!isGetDataFromWebviewOrAPI && result != null && result.getResult() != null && !img.equals("")) {
                        Log.d("khang", "sign in with API");
                        signInWithFirebase(result.getResult().getUserId(), img);
                    } else {
                        if (isFailToGetData) {
//                            Toast.makeText(ManHinhDangNhapActivity.this, "Login error, Please try again later", Toast.LENGTH_SHORT).show();
                        } else {
                            isFailToGetData = true;
                        }
                    }
                }
            });
        }


        @Override
        public void onError(int code, String mess) {
            mImgHelpGetUserName.setVisibility(View.VISIBLE);
            if (isFailToGetData) {
                hideProgressDialog();
                Toast.makeText(ManHinhDangNhapActivity.this, "Login error, Please try again later", Toast.LENGTH_SHORT).show();
            } else {
                isFailToGetData = true;
            }
        }

        @Override
        public void onInvalidLink() {
            mImgHelpGetUserName.setVisibility(View.VISIBLE);
            if (isFailToGetData) {
                hideProgressDialog();
                Toast.makeText(ManHinhDangNhapActivity.this, "Username is not correct, please check your tiktok username again", Toast.LENGTH_SHORT).show();
            } else {
                isFailToGetData = true;
            }
        }

        @Override
        public void onCheckLinkDone() {
        }
    };

    private void signInWithFirebase(String uid, String img) {

        isGetDataFromWebviewOrAPI = true;
        PreferenceUtil.saveStringPref(PreferenceUtil.TIKTOK_USER_NAME, username);
        mAuth.signInWithEmailAndPassword(uid + "@gmail.com", "password")
                .addOnCompleteListener(ManHinhDangNhapActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("Khang", "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user, img);
                        } else {
                            mAuth.createUserWithEmailAndPassword(uid + "@gmail.com", "password")
                                    .addOnCompleteListener(ManHinhDangNhapActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                // Sign in success, update UI with the signed-in user's information
                                                Log.d("Khang", "signInWithEmail:success");
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                updateUI(user, img);
                                            } else {
                                                // If sign in fails, display a message to the user.
                                                Log.w("Khang", "signInWithEmail:failure", task.getException().getCause());
                                                Toast.makeText(ManHinhDangNhapActivity.this, "Authentication failed.",
                                                        Toast.LENGTH_SHORT).show();
                                                updateUI(null, null);
                                                // ...
                                            }

                                            // ...
                                        }
                                    });
                            // ...
                        }

                        // ...
                    }
                });
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
                progressDialog.setTitle(R.string.please_wait_for_login);
                progressDialog.setCancelable(false);
                progressDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
