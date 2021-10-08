package tikfans.tikplus;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

import java.util.ArrayList;
import java.util.List;

import tikfans.tikplus.model.LikeCampaign;
import tikfans.tikplus.model.Message;
import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.FirebaseUtil;
import tikfans.tikplus.model.ChienDichCuaNguoiDungHienTai;
import tikfans.tikplus.model.SubCampaign;
import tikfans.tikplus.util.PreferenceUtil;
import tikfans.tikplus.util.RemoteConfigUtil;

import static com.unity3d.services.core.properties.ClientProperties.getApplicationContext;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChienDichFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChienDichFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChienDichFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    FirebaseCampaignsQueryAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private RelativeLayout mNoCampaignLayout;
    private ProgressDialog progressDialog;
    private ArrayList<String> mSubRunningCampaignList;
    private ArrayList<String> mLikeRunningCampaignList;
    private boolean isLoadedCampaign = false;


    public ChienDichFragment() {
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
    public static ChienDichFragment newInstance(String param1, String param2) {
        ChienDichFragment fragment = new ChienDichFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_sub_campaign, container, false);
        FloatingActionButton fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isLoadedCampaign) {
                    Toast.makeText(getActivity(), getString(R.string.please_wait_loading_campaign), Toast.LENGTH_SHORT).show();
                    return;
                }

                int limitCampaign = 5;
                if (MyChannelApplication.isVipAccount) {
                    limitCampaign = 10;
                }
                if (mAdapter != null && mAdapter.getItemCount() >= limitCampaign) {
                    try {
                        AppUtil.showAlertDialog(getContext(), getString(R.string.campaign_limitation), String.format(getString(R.string.campaign_limitation_details), limitCampaign));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
                try {
                    showChoseCampaignTypeDialog();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        mCreateMessageDialog = new Dialog(getContext());
        mCreateMessageDialog.setContentView(R.layout.dialog_create_message);
        mRecyclerView = rootView.findViewById(R.id.my_recycler_view);
        progressDialog = new ProgressDialog(getContext());
        mNoCampaignLayout = rootView.findViewById(R.id.no_campaign_layout);

        mChoseCampaignTypeDialog = new Dialog(getContext());
        mChoseCampaignTypeDialog.setContentView(R.layout.dialog_chose_account_layout);

        mSubRunningCampaignList = new ArrayList<>();
        mLikeRunningCampaignList = new ArrayList<>();

        queryDatabase();

        return rootView;
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
                                Toast.makeText(getApplicationContext(), getString(R.string.not_enough_coin), Toast.LENGTH_SHORT).show();
                                return;
                            }

                            if (cost > currentSubCoin) {
                                hideProgressDialog();

                                Toast.makeText(getApplicationContext(), getString(R.string.not_enough_coin), Toast.LENGTH_LONG).show();
                                Intent buyIntent = new Intent(getActivity(), MuaHangActivity.class);
                                buyIntent.putExtra(AppUtil.VIP_ACCOUNT_EXTRA, false);
                                startActivity(buyIntent);
                                return;
                            }
                            String mUserName = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_NAME, "NONE");
                            String mUserImg = PreferenceUtil.getStringPref(PreferenceUtil.TIKTOK_USER_PHOTO, "NONE");
                            Message message = new Message(user.getUid(), mUserName, mUserImg, messageText.getText().toString(), cost, ServerValue.TIMESTAMP);
                            FirebaseUtil.getChatRoomAllRef().push().setValue(message);
                            Toast.makeText(getActivity(), getString(R.string.send_message_success), Toast.LENGTH_SHORT).show();
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

    }


    private void queryDatabase() {
        showProgressDialog();
        FirebaseUtil.getCampaignCurrentUser()
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        mSubRunningCampaignList.clear();
                        mLikeRunningCampaignList.clear();
                        final List<Pair<String, ChienDichCuaNguoiDungHienTai>> campaignPaths = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            ChienDichCuaNguoiDungHienTai chienDichCuaNguoiDungHienTai = snapshot.getValue(ChienDichCuaNguoiDungHienTai.class);
                            if (chienDichCuaNguoiDungHienTai != null && !chienDichCuaNguoiDungHienTai.getKey().equals("") && !chienDichCuaNguoiDungHienTai.getKey().equals("wrongkey")) {
                                campaignPaths.add(new Pair(snapshot.getKey().toString(), chienDichCuaNguoiDungHienTai));
                            }
                        }

                        mAdapter = new FirebaseCampaignsQueryAdapter(campaignPaths,
                                new FirebaseCampaignsQueryAdapter.OnSetupViewListener() {
                                    @Override
                                    public void onSetupView(ChienDichViewHolder holder, SubCampaign subCampaign, LikeCampaign likeCampaign) {
                                        setupCampaign(holder, subCampaign, likeCampaign);
                                    }
                                });
                        mAdapter.setActivity(getActivity());
                        mRecyclerView.setAdapter(mAdapter);
                        if (mAdapter.getItemCount() == 0) {
                            mNoCampaignLayout.setVisibility(View.VISIBLE);
                        } else {
                            mNoCampaignLayout.setVisibility(View.GONE);
                        }
                        hideProgressDialog();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                isLoadedCampaign = true;
                            }
                        }, 500);
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {

                    }
                });
    }

    private void setupCampaign(ChienDichViewHolder holder, SubCampaign subCampaign, LikeCampaign likeCampaign) {
        if (subCampaign != null) {
            if (subCampaign.isIp()) {
                mSubRunningCampaignList.add(subCampaign.getUserName());
            }
            holder.setCampaignIcon(subCampaign.getUserImg(), 1, getContext());
//            holder.setCampaignVideoTitle(subCampaign.getChannelName());
            holder.setProgressInfo(subCampaign.getCurSub(), subCampaign.getOrder(), 1);
        } else if (likeCampaign != null) {
            if (likeCampaign.isIp()) {
                mLikeRunningCampaignList.add(likeCampaign.getVideoId());
            }
            String img_url = likeCampaign.getVideoThumb();
            holder.setCampaignIcon(img_url, 3, getContext());
//            holder.setCampaignVideoTitle(likeCampaign.getVideoTitle());
            holder.setProgressInfo(likeCampaign.getCurLike(), likeCampaign.getOrder(), 3);
        }
    }

    private Dialog mChoseCampaignTypeDialog;

    private void showChoseCampaignTypeDialog() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(mChoseCampaignTypeDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mChoseCampaignTypeDialog.show();
        mChoseCampaignTypeDialog.getWindow().setAttributes(lp);
        mChoseCampaignTypeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        LinearLayout subTypeLayout = mChoseCampaignTypeDialog.findViewById(R.id.sub_type);
        LinearLayout likeTypeLayout = mChoseCampaignTypeDialog.findViewById(R.id.like_type);
        LinearLayout messageTypeLayout = mChoseCampaignTypeDialog.findViewById(R.id.message_type);

        Button dialogButtonCancel = mChoseCampaignTypeDialog.findViewById(R.id.btnCancel);
        // if button is clicked, close the custom dialog

        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mChoseCampaignTypeDialog.dismiss();
            }
        });
        subTypeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChonVideoActivity.class);
                if (mSubRunningCampaignList != null) {
                    intent.putStringArrayListExtra(AppUtil.RUNNING_CAMPAIGN_PATH_STRING_EXTRA, mSubRunningCampaignList);
                    int numberCampaign = 0;
                    if (mAdapter!= null ) {
                        numberCampaign = mAdapter.getItemCount();
                    }
                    intent.putExtra(AppUtil.NUMBER_CAMPAIGN_PATH_STRING_EXTRA, numberCampaign);

                }
                intent.putExtra(AppUtil.CREATE_CAMPAIGN_TYPE, AppUtil.SUB_CAMPAIGN_TYPE);
                startActivity(intent);
                mChoseCampaignTypeDialog.dismiss();
            }
        });
        likeTypeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), ChonVideoActivity.class);
                if (mLikeRunningCampaignList != null) {
                    intent.putStringArrayListExtra(AppUtil.RUNNING_CAMPAIGN_PATH_STRING_EXTRA, mLikeRunningCampaignList);
                    int numberCampaign = 0;
                    if (mAdapter!= null ) {
                        numberCampaign = mAdapter.getItemCount();
                    }
                    intent.putExtra(AppUtil.NUMBER_CAMPAIGN_PATH_STRING_EXTRA, numberCampaign);
                }
                intent.putExtra(AppUtil.CREATE_CAMPAIGN_TYPE, AppUtil.LIKE_CAMPAIGN_TYPE);
                startActivity(intent);
                mChoseCampaignTypeDialog.dismiss();
            }
        });

        messageTypeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCreateMessageDialog();
            }
        });

    }


//    private void showChoseCampaignTypeDialog() {
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(mChoseCampaignTypeDialog.getWindow().getAttributes());
//        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
//        mChoseCampaignTypeDialog.show();
//        mChoseCampaignTypeDialog.getWindow().setAttributes(lp);
//        mChoseCampaignTypeDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
//        final RadioButton radioButtonSubCampaign = mChoseCampaignTypeDialog.findViewById(R.id.rb1);
//        final RadioButton radioButtonViewCampaign = mChoseCampaignTypeDialog.findViewById(R.id.rb2);
//        final RadioButton radioButtonLikeCampaign = mChoseCampaignTypeDialog.findViewById(R.id.rb3);
//        Button dialogButtonOK = mChoseCampaignTypeDialog.findViewById(R.id.btnOK);
//        Button dialogButtonCancel = mChoseCampaignTypeDialog.findViewById(R.id.btnCancel);
//        // if button is clicked, close the custom dialog
//        radioButtonSubCampaign.setChecked(true);
//        radioButtonViewCampaign.setChecked(false);
//        radioButtonLikeCampaign.setChecked(false);
//
//        dialogButtonCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mChoseCampaignTypeDialog.dismiss();
//            }
//        });
//        dialogButtonOK.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), ChonVideoActivity.class);
//                if (radioButtonSubCampaign.isChecked()) {
//                    if (mRunningCampaignList != null) {
//                        intent.putStringArrayListExtra(AppUtil.RUNNING_CAMPAIGN_PATH_STRING_EXTRA, mRunningCampaignList);
//
//                    }
//                    intent.putExtra(AppUtil.CREATE_CAMPAIGN_TYPE, AppUtil.SUB_CAMPAIGN_TYPE);
//                } else if (radioButtonViewCampaign.isChecked()){
//                    intent.putExtra(AppUtil.CREATE_CAMPAIGN_TYPE, AppUtil.VIEW_CAMPAIGN_TYPE);
//                } else {
//                    if (mRunningCampaignList != null) {
//                        intent.putStringArrayListExtra(AppUtil.RUNNING_CAMPAIGN_PATH_STRING_EXTRA, mRunningCampaignList);
//
//                    }
//                    intent.putExtra(AppUtil.CREATE_CAMPAIGN_TYPE, AppUtil.LIKE_CAMPAIGN_TYPE);
//                }
//                startActivity(intent);
//                mChoseCampaignTypeDialog.dismiss();
//            }
//        });
//
//        radioButtonSubCampaign.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                radioButtonViewCampaign.setChecked(false);
//                radioButtonSubCampaign.setChecked(true);
//                radioButtonLikeCampaign.setChecked(false);
//            }
//        });
//        radioButtonViewCampaign.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                radioButtonSubCampaign.setChecked(false);
//                radioButtonViewCampaign.setChecked(true);
//                radioButtonLikeCampaign.setChecked(false);
//            }
//        });
//        radioButtonLikeCampaign.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                radioButtonSubCampaign.setChecked(false);
//                radioButtonViewCampaign.setChecked(false);
//                radioButtonLikeCampaign.setChecked(true);
//            }
//        });
//
//    }

//    public final int REQUEST_CODE_CREATE_CAMPAIGN = 111;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_CODE_CREATE_CAMPAIGN && resultCode == Activity.RESULT_OK) {
//            queryDatabase();
//        }
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
