package tikfans.tikplus.subviewlike;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import tikfans.tikplus.R;
import tikfans.tikplus.model.LikeCampaign;
import tikfans.tikplus.model.LogLike;
import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.FirebaseUtil;

public class LikeChiTietChienDichActivity extends AppCompatActivity {

    RecyclerView mRecyclerView;
    LikeChiTietChienDichAdapter mLikeChiTietChienDichAdapter;
    String mCampaignPath;
    private ImageView imgToobarBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mCampaignPath = intent.getStringExtra(AppUtil.CAMPAIGN_PATH_STRING_EXTRA);
        setContentView(R.layout.activity_view_campaign_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        imgToobarBack = findViewById(R.id.toolbar_back);
        setSupportActionBar(toolbar);

        imgToobarBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.campaign_detail_recycler_view);
        mLikeChiTietChienDichAdapter = new LikeChiTietChienDichAdapter(getApplicationContext(), this);
        mRecyclerView.setAdapter(mLikeChiTietChienDichAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference campaignRef = FirebaseUtil.getLikeCampaignsRef().child(mCampaignPath);
        // TODO: Fix this so async event won't bind the wrong view post recycle.
        ValueEventListener campaignListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LikeCampaign campaign = dataSnapshot.getValue(LikeCampaign.class);
                if (campaign == null) return;
                mLikeChiTietChienDichAdapter.setCampaign(campaign);
                mLikeChiTietChienDichAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
            }
        };
        campaignRef.addListenerForSingleValueEvent(campaignListener);


        Query query = FirebaseUtil.getLogLikeRef().child(mCampaignPath)
                .limitToLast(100);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    ArrayList<LogLike> logLikeArrayList = new ArrayList<>();
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        logLikeArrayList.add(0,snapshot.getValue(LogLike.class));
                    }

                    mLikeChiTietChienDichAdapter.setLogViewArray(logLikeArrayList);
                    mLikeChiTietChienDichAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
