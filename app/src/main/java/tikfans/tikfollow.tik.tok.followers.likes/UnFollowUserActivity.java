package tikfans.tikfollow.tik.tok.followers.likes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.Button;

import java.util.ArrayList;

import tikfans.tikfollow.tik.tok.followers.likes.model.UnFollowUSer;
import tikfans.tikfollow.tik.tok.followers.likes.util.AppUtil;

public class UnFollowUserActivity extends AppCompatActivity {

    private Button mBtnDone;
    private UnFollowUserAdapter unFollowUserAdapter;
    private ArrayList<UnFollowUSer> unFollowUSerArrayList;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow_reported_user);
//        mBtnDone = findViewById(R.id.btn_done_follow_report_user);
        mRecyclerView = findViewById(R.id.unfollow_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(layoutManager);
        unFollowUSerArrayList = new ArrayList<>();
        try {
            unFollowUSerArrayList = (ArrayList<UnFollowUSer>) getIntent().getExtras().getSerializable(AppUtil.UNFOLLOW_USER_LIST_EXTRA);
        } catch (Exception e) {
            e.printStackTrace();
        }
        unFollowUserAdapter = new UnFollowUserAdapter(this, unFollowUSerArrayList, new UnFollowUserAdapter.OnSetupViewListener() {
            @Override
            public void onSetupView(UnfollowUserViewHolder holder, UnFollowUSer unFollowUSer) {
                if (unFollowUSer!= null) {
                    holder.setInfo(unFollowUSer);
                }
            }
        });
        mRecyclerView.setAdapter(unFollowUserAdapter);
    }
}