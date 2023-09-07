package tikfans.tikplus;


import static tikfans.tikplus.util.FirebaseUtil.TOKEN_REF;

import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.google.firebase.messaging.FirebaseMessaging;

import android.os.Handler;
import android.os.Looper;
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

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.FirebaseUtil;
import tikfans.tikplus.util.PreferenceUtil;
import tikfans.tikplus.util.SQLiteDatabaseHandler;

public class
ManHinhDangNhapActivity extends AppCompatActivity {

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
                try {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                    intent.setData(Uri.parse("https://sites.google.com/site/cptstudioprivacypolicy/privacy-policy"));
                    startActivity(intent);
                } catch (Exception e) {
                    FirebaseCrashlytics.getInstance().recordException(e);
                }
            }
        });

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
                PreferenceUtil.saveStringPref(PreferenceUtil.TIKTOK_USER_PHOTO_FOR_CAMPAIGN, img);

            }
            if (username != null && !username.isEmpty()) {
                updateValues.put(FirebaseUtil.TIKTOK_USER_NAME, username);
                PreferenceUtil.saveStringPref(PreferenceUtil.TIKTOK_USER_NAME, username);

                PreferenceUtil.saveStringPref(PreferenceUtil.TIKTOK_USER_NAME_FOR_CAMPAIGN, username);
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
                                        databaseReference.child(TOKEN_REF).setValue(token);
                                        Log.d("khang4", "update FCM token for: " + databaseReference + " Token: " + token);

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
        if (username.equals("cucphanthi3")) {
            signInWithFirebase("6858622697427026946", "https://p16-sign-sg.tiktokcdn.com/aweme/100x100/tiktok-obj/6891573429783232517.jpeg?x-expires=1676188800&x-signature=A1G3IduhbVObSAfTfcDDiLIQ1mc%3D");
        } else {
            getUserInfoByWebView();
        }

    }

    private int getDataFailedCount = 0;
    private void getUserInfoByWebView() {

        showProgressDialog();
        username = mEditTextUserName.getText().toString();
        if (username.charAt(0) == '@') {
            username = username.substring(1);
        }
        String link = AppUtil.TIKTOK_PREFIX_LINK + username;
        Log.e("khang", "getUserInfoByWebView: user link " + link);

        ExecutorService executor = Executors.newSingleThreadExecutor();
        long beginTime = System.currentTimeMillis();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(new Runnable() {
            @Override
            public void run() {

                //Background work here
                Document document = null;
                try {
                    document = Jsoup.connect(link).get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Document finalDocument = document;
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String toGetUidString = finalDocument.getElementById("SIGI_STATE").data();
                            String uid = "";
                            if (toGetUidString.contains("authorId")) {
                                uid = toGetUidString.substring(toGetUidString.indexOf("authorId") + 11, toGetUidString.indexOf("authorId") + 30);
                            } else if (toGetUidString.contains("\"id\":")){
                                uid = toGetUidString.substring(toGetUidString.indexOf("\"id\":") + 6, toGetUidString.indexOf("\"id\":") + 25);
                            }
                            Log.d("khang", "uid tiktok: " + uid);
                            if (uid.length() == 0 || uid.contains(":{")) {
                                mImgHelpGetUserName.setVisibility(View.VISIBLE);
//                        if (isFailToGetData) {
                                hideProgressDialog();
                                Toast.makeText(ManHinhDangNhapActivity.this, getString(R.string.username_not_found), Toast.LENGTH_SHORT).show();
//                        } else {
//                            isFailToGetData = true;
//                        }
                            } else {
                                String avatarUrl = finalDocument.getElementsByClass("tiktok-1zpj2q-ImgAvatar").get(0).attributes().get("src");
                                Log.d("Khang", "login: uid: " + uid + " / " + avatarUrl);
                                if (!isGetDataFromWebviewOrAPI) {
                                    Log.d("khang", "sign in with webview");
                                    signInWithFirebase(uid, avatarUrl);
                                }
                            }
                            getDataFailedCount = 0;
                        } catch (Exception e) {
                            getDataFailedCount++;
                            if (getDataFailedCount < 3) {
                                getUserInfoByWebView();
                            } else {
                                getDataFailedCount = 0;
                                mImgHelpGetUserName.setVisibility(View.VISIBLE);
                                Toast.makeText(ManHinhDangNhapActivity.this, getString(R.string.username_not_found), Toast.LENGTH_LONG).show();
                                hideProgressDialog();
                                e.printStackTrace();
                                FirebaseCrashlytics.getInstance().recordException(e);
                            }
                        }
                        //UI Thread work here
                        Log.d("khang", "loadingTime: " + (System.currentTimeMillis() - beginTime));
                    }
                });
            }
        });

//        long beginTime2 = System.currentTimeMillis();
//        tiktokWebClient.load(link);
//        tiktokWebClient.setListener(new TiktokWebClient.ClientListener() {
//            @Override
//            public void onLoading() {
//                Log.e("khang", "onLoading: ");
//                showProgressDialog();
//            }
//
//            @Override
//            public void onLoadFinish(Document document, String url) {
//                if (isLoaded[0]) return;
//                if (url.contains("notfound")) { //block ip from india so url is notfound
//                    //if (isFailToGetData) {
//                    hideProgressDialog();
//                    mImgHelpGetUserName.setVisibility(View.VISIBLE);
//                    Toast.makeText(ManHinhDangNhapActivity.this, getString(R.string.username_not_found), Toast.LENGTH_SHORT).show();
////                        } else {
////                            isFailToGetData = true;
////                        }
//                    return;
//                }
//
//                //get uid:
//                String toGetUidString = document.getElementById("SIGI_STATE").data();
//                String uid = "";
//                uid = toGetUidString.substring(toGetUidString.indexOf("{\"id\":\"") + 7, toGetUidString.indexOf("{\"id\":\"") + 26);
//                if (uid.length() == 0 || uid.contains(":{")) {
//                    mImgHelpGetUserName.setVisibility(View.VISIBLE);
////                        if (isFailToGetData) {
//                    hideProgressDialog();
//                    Toast.makeText(ManHinhDangNhapActivity.this, getString(R.string.username_not_found), Toast.LENGTH_SHORT).show();
////                        } else {
////                            isFailToGetData = true;
////                        }
//                } else {
//                    String avatarUrl = document.getElementsByClass("tiktok-1zpj2q-ImgAvatar").get(0).attributes().get("src");
//                    Log.d("Khang", "login: uid: " + uid + " / " + avatarUrl);
//                    if (!isGetDataFromWebviewOrAPI) {
//                        Log.d("khang", "sign in with webview");
//                        signInWithFirebase(uid, avatarUrl);
//                    }
//                }
//                isLoaded[0] = true;
//                mWebView.setVisibility(View.INVISIBLE);
//                Log.d("khang", "loadingTime2: " + (System.currentTimeMillis() - beginTime2));
//            }
//        });
    }

    // [END signin]


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
