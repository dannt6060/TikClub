package tikfans.tikplus;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.BuildConfig;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;
import com.google.firebase.dynamiclinks.ShortDynamicLink;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.PreferenceUtil;
import tikfans.tikplus.util.RemoteConfigUtil;

import static tikfans.tikplus.util.PreferenceUtil.IS_FTU;

public class ManHinhDauTienActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private static final long CONFIG_EXPIRE_SECOND = 12 * 3600;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        mFirebaseAuth = FirebaseAuth.getInstance();

//Get instance của remote config
        final FirebaseRemoteConfig config = FirebaseRemoteConfig.getInstance();
        //Vứt nó vào 1 class singleton dùng cho tiện
        //Setting chế độ debug
        FirebaseRemoteConfigSettings settings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG).build();
        config.setConfigSettings(settings);

        //FirebaseRemoteConfig sử dụng các giá trị defaule trong file R.xml.default_config nếu không lấy được giá trị
        config.setDefaults(R.xml.remote_config_defaults);

        //Vì chúng ta đang trong debug mode nên cần config được fetch và active ngay lập tức sau khi thay đổi trên console
        long expireTime = config.getInfo().getConfigSettings().isDeveloperModeEnabled() ? 0 : CONFIG_EXPIRE_SECOND;
        Log.d("Khang", "remote config fetch:" + expireTime);
        //Mỗi lần khởi chạy app sẽ fetch config về và nếu thành công thì sẽ active config vừa lấy về
        config.fetch(expireTime)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d("Khang", "remote config fetch:" + task.toString());
                        if (task.isSuccessful()) {
                            config.activateFetched();
                        }
                    }
                });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            String link = "https://tikfans.tikplus/?invitedby=" + uid;
            FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(Uri.parse(link))
                    .setDynamicLinkDomain("tikclub.page.link")
                    .setAndroidParameters(
                            new DynamicLink.AndroidParameters.Builder("tikfans.tikplus")
                                    .setMinimumVersion(20)
                                    .build())
                    .setIosParameters(
                            new DynamicLink.IosParameters.Builder("com.channelpromoter.view4view")
                                    .setAppStoreId("1445118375")
                                    .setMinimumVersion("1.0.1")
                                    .build())
                    .buildShortDynamicLink()
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Khang", "dynamic link failed: " + e.toString());
                        }
                    })
                    .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                        @Override
                        public void onSuccess(ShortDynamicLink shortDynamicLink) {
                            Log.d("Khang", "dynamic link success: " + shortDynamicLink.toString());
                            MyChannelApplication.invitationUrl = shortDynamicLink.getShortLink();
                        }
                    });
        }
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                        // Get deep link from result (may be null if no link is found)
                        Uri deepLink = null;
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.getLink();
                        }
                        //
                        // If the user isn't signed in and the pending Dynamic Link is
                        // an invitation, sign in the user anonymously, and record the
                        // referrer's UID.
                        //
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        if (user == null
                                && deepLink != null
                                && deepLink.getBooleanQueryParameter("invitedby", false)) {
                            String referrerUid = deepLink.getQueryParameter("invitedby");
                            PreferenceUtil.saveStringPref(PreferenceUtil.REFERRED_BY, referrerUid);
                            Log.d("Khang", "getDynamicLink by: " + referrerUid);
                        }
                    }
                });


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!AppUtil.isNetworkAvailable(getApplicationContext())) {
                    AppUtil.showAlertDialog(ManHinhDauTienActivity.this, getString(R.string.khong_ket_noi), getString(R.string.khong_ket_noi_chi_tiet));
                } else {
                    try {
                        int versionCode = ManHinhDauTienActivity.this.getPackageManager().getPackageInfo(ManHinhDauTienActivity.this.getPackageName(), 0).versionCode;
                        if (versionCode < FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_VERSION)) {
                            AlertDialog alertDialog = new AlertDialog.Builder(ManHinhDauTienActivity.this).setTitle(getString(R.string.cap_nhat_app_tieu_deu)).
                                    setMessage(getString(R.string.cap_nhat_app_chi_tiet)).
                                    setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            finish();
                                        }
                                    }).
                                    setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            updateApp(FirebaseRemoteConfig.getInstance().getString(RemoteConfigUtil.TIKFANS_PACKAGE_APP));
                                            finish();
                                        }
                                    }).create();
                            alertDialog.show();
                        } else {
                            boolean isFTU = PreferenceUtil.getBooleanPref(IS_FTU, true);
                            if (isFTU) {
                                startActivity(new Intent(getApplication(), ManHinhFTUActivity.class));
                                finish();
                            } else {
                                if (mFirebaseAuth.getCurrentUser() != null) {
                                    startActivity(new Intent(getApplication(), MainActivity.class));
                                    finish();
                                } else {
                                    startActivity(new Intent(getApplication(), ManHinhDangNhapActivity.class));
                                    finish();
                                }
                            }
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        boolean isFTU = PreferenceUtil.getBooleanPref(IS_FTU, true);
                        if (isFTU) {
                            startActivity(new Intent(getApplication(), ManHinhFTUActivity.class));
                            finish();
                        } else {
                            if (mFirebaseAuth.getCurrentUser() != null) {
                                startActivity(new Intent(getApplication(), MainActivity.class));
                                finish();
                            } else {
                                startActivity(new Intent(getApplication(), ManHinhDangNhapActivity.class));
                                finish();
                            }
                        }
                    }
                }
            }
        }, 1000);
    }

    private void updateApp(String url) {
        Uri uri = Uri.parse("market://details?id=" + url);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "http://play.google.com/store/apps/details?id=" +
                            url)));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

}
