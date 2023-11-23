package tikfans.tikfollow.tik.tok.followers.likes;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.ShortDynamicLink;

import java.util.ArrayList;

import tikfans.tikfollow.tik.tok.followers.likes.util.FirebaseUtil;
import tikfans.tikfollow.tik.tok.followers.likes.util.PreferenceUtil;

public class MoiBanBeActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    MoiBanBeAdapter mMoiBanBeAdapter;
    ArrayList<String> referredToList;

    TextView mTxtLink;
    TextView mTxtInviteTotal;
    Button mBtnCopyLink, mBtnInvite;
    private ImageView imgToobarBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friend);

        Toolbar toolbar = findViewById(R.id.toolbar);
        imgToobarBack = findViewById(R.id.toolbar_back);
        setSupportActionBar(toolbar);

        imgToobarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTxtLink = findViewById(R.id.txt_invite_link);
        mTxtInviteTotal = findViewById(R.id.txt_invite_total);
        mBtnCopyLink = findViewById(R.id.btn_copy_link);
        mBtnInvite = findViewById(R.id.btn_invite);
        mRecyclerView = findViewById(R.id.invite_friend_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        referredToList = new ArrayList<>();

        if (MyChannelApplication.invitationUrl != null) {
            mTxtLink.setText(MyChannelApplication.invitationUrl.toString());
        }


        mBtnInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inviteUser();
            }
        });

        mBtnCopyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MyChannelApplication.invitationUrl != null) {
                    mTxtLink.setText(MyChannelApplication.invitationUrl.toString());
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", MyChannelApplication.invitationUrl.toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getApplicationContext(), getString(R.string.copy_to_clipboard), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.get_invite_link_error), Toast.LENGTH_SHORT).show();
                }
            }
        });

        createDynamicLink();

        FirebaseUtil.getReferredToCurrentUser()
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            referredToList.add(snapshot.getValue().toString());
                        }
                        mMoiBanBeAdapter = new MoiBanBeAdapter(getApplicationContext(), new MoiBanBeAdapter.OnSetupNameListener() {
                            @Override
                            public void onSetupName(MoiBanBeAdapter.UserViewHolder holder, String name) {
                                holder.setName(name);

                            }
                        }, new MoiBanBeAdapter.OnSetupPhotoListener() {
                            @Override
                            public void onSetupPhoto(MoiBanBeAdapter.UserViewHolder holder, String photo) {
                                holder.setPhoto(photo);
                            }
                        });
                        mMoiBanBeAdapter.setReferredToList(referredToList);
                        mRecyclerView.setAdapter(mMoiBanBeAdapter);
                        if (referredToList.size() > 0) {
                            int bonusCoin = referredToList.size() * 1000;
                            mTxtInviteTotal.setText(String.format(getString(R.string.you_earned_by_referr), bonusCoin));
                        } else {
                            mTxtInviteTotal.setText(getString(R.string.you_dont_have_referral));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {

                    }
                });
    }


    private void inviteUser() {

        if (MyChannelApplication.invitationUrl == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.invite_failed), Toast.LENGTH_SHORT).show();
            return;
        }


        //send the invitations
        String referrerName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        String subject = "";
        try {
            subject = String.format(getString(R.string.invite_subject), referrerName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String invitationLink = MyChannelApplication.invitationUrl.toString();
        String msg = getString(R.string.invite_message)
                + invitationLink;
        String msgHtml = String.format("<p>Let's install SubChat together! Both you and I will get 2000 coin bonus when you install this app! Use my "
                + "<a href=\"%s\">referrer link: </a>!</p>", invitationLink);

        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, msg);
        intent.putExtra(Intent.EXTRA_HTML_TEXT, msgHtml);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void createDynamicLink() {
        String savedLink = PreferenceUtil.getStringPref(PreferenceUtil.INVITE_FRIEND_LINK, "");
        if (savedLink != null && savedLink.length() > 5) {
            MyChannelApplication.invitationUrl  = Uri.parse(savedLink);
            mTxtLink.setText(MyChannelApplication.invitationUrl.toString());
            return;
        }
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
                    .addOnSuccessListener(new OnSuccessListener<ShortDynamicLink>() {
                        @Override
                        public void onSuccess(ShortDynamicLink shortDynamicLink) {
                            MyChannelApplication.invitationUrl = shortDynamicLink.getShortLink();
                            if (MyChannelApplication.invitationUrl != null) {
                                mTxtLink.setText(MyChannelApplication.invitationUrl.toString());
                                PreferenceUtil.saveStringPref(PreferenceUtil.INVITE_FRIEND_LINK, MyChannelApplication.invitationUrl.toString() );
                            }
                        }
                    });
        }
    }
}
