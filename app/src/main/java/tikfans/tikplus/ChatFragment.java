package tikfans.tikplus;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;

import tikfans.tikplus.model.Message;
import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.FirebaseUtil;
import tikfans.tikplus.util.PreferenceUtil;
import tikfans.tikplus.util.RemoteConfigUtil;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChatFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    FirebaseChatQueryAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private ProgressDialog progressDialog;
    private ArrayList<Message> messageArrayList;
    private boolean isLoadedCampaign = false;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private long lastRefreshTime = 0L;


    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChienDichFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatFragment newInstance(String param1, String param2) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_chat, container, false);
        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    showCreateMessageDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mRecyclerView = rootView.findViewById(R.id.my_recycler_view);
        progressDialog = new ProgressDialog(getContext());

        mCreateMessageDialog = new Dialog(getContext());
        mCreateMessageDialog.setContentView(R.layout.dialog_create_message);

        messageArrayList = new ArrayList<>();

        queryDatabase();

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

    }


    private void queryDatabase() {
        lastRefreshTime = System.currentTimeMillis();
        showProgressDialog();
        mSwipeRefreshLayout.setRefreshing(true);
        Query query = FirebaseUtil.getChatRoomAllRef()
                .orderByKey()
                .limitToLast(5);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                messageArrayList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    messageArrayList.add(0, snapshot.getValue(Message.class));
                }

                mAdapter = new FirebaseChatQueryAdapter(messageArrayList,
                        new FirebaseChatQueryAdapter.OnSetupViewListener() {
                            @Override
                            public void
                            onSetupView(ChatViewHolder holder, Message message) {
                                setupChatItem(holder, message);
                            }
                        });
                mAdapter.setActivity(getActivity());
                mRecyclerView.setAdapter(mAdapter);

                mRecyclerView.scrollToPosition(messageArrayList.size() - 1);
                hideProgressDialog();
                mSwipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                hideProgressDialog();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void setupChatItem(ChatViewHolder holder, Message message) {
        if (message != null) {
            holder.setInfo(message);
        }
    }

    private Dialog mCreateMessageDialog;

    private void showCreateMessageDialog() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(mCreateMessageDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mCreateMessageDialog.show();
        mCreateMessageDialog.getWindow().setAttributes(lp);
        mCreateMessageDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        final EditText messageText = mCreateMessageDialog.findViewById(R.id.message_text);
        TextView messageCost = mCreateMessageDialog.findViewById(R.id.message_total_coin);
        Button btnCancel = mCreateMessageDialog.findViewById(R.id.btnCancel);
        Button btnSend = mCreateMessageDialog.findViewById(R.id.btnSend);
        final long cost = FirebaseRemoteConfig.getInstance().getLong(RemoteConfigUtil.TIKFANS_MESSAGE_COST);
        messageCost.setText(String.format(getString(R.string.message_cost), cost));

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCreateMessageDialog.dismiss();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {

                    FirebaseUtil.getCoinCurrentAccountRef().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            long currentSubCoin = (long) dataSnapshot.getValue();
                            if (currentSubCoin <= 0) {
                                hideProgressDialog();
                                Toast.makeText(getActivity(), getString(R.string.not_enough_coin), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (cost > currentSubCoin) {
                                hideProgressDialog();
                                Toast.makeText(getActivity(), getString(R.string.not_enough_coin), Toast.LENGTH_LONG).show();
                                Intent buyIntent = new Intent(getActivity(), MuaHangActivity.class);
                                buyIntent.putExtra(AppUtil.VIP_ACCOUNT_EXTRA, false);
                                startActivity(buyIntent);
                                return;
                            }

                            String mUserName = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_NAME, "NONE");
                            String mUserImg = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_PHOTO, "NONE");
                            Message message = new Message(user.getUid(), mUserName, mUserImg, messageText.getText().toString(), cost, ServerValue.TIMESTAMP);
                            FirebaseUtil.getChatRoomAllRef().push().setValue(message);
                            mCreateMessageDialog.dismiss();
                            queryDatabase();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onRefresh() {
        if (System.currentTimeMillis() - lastRefreshTime > 10000) {
            queryDatabase();
        } else {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    private void hideProgressDialog() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void showProgressDialog() {
        progressDialog.show();
    }


}
