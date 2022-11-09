package tikfans.tikplus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.ServerValue;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;


import tikfans.tikplus.util.AppUtil;
import tikfans.tikplus.util.FirebaseUtil;
import tikfans.tikplus.util.RemoteConfigUtil;
import tikfans.tikplus.model.LogAdsReward;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link KiemCoinFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link KiemCoinFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KiemCoinFragment extends Fragment implements View.OnClickListener{
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Context mContext;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public KiemCoinFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment KiemCoinFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KiemCoinFragment newInstance(String param1, String param2) {
        KiemCoinFragment fragment = new KiemCoinFragment();
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
        mContext = getActivity().getApplicationContext();

        //create invitation link
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_earn_coin, container, false);
        rootView.findViewById(R.id.invite_user).setOnClickListener(this);
        rootView.findViewById(R.id.watchVideo).setOnClickListener(this);
        rootView.findViewById(R.id.buyCredits).setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void onClick(View view) {
        view.startAnimation(AnimationUtils.loadAnimation(getActivity().getApplicationContext(), R.anim.click_anim));
        if (AppUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            int viewId = view.getId();
            if (viewId == R.id.buyCredits) {
                startActivity(new Intent(getActivity().getApplicationContext(), MuaHangActivity.class));
                return;
            } else if (viewId == R.id.invite_user) {
//                dailyBonusChecking();
                startActivity(new Intent(getActivity().getApplicationContext(), MoiBanBeActivity.class));
                return;
            } else if (viewId == R.id.watchVideo) {
                startActivity(new Intent(getActivity().getApplicationContext(), XemQuangCaoActivity.class));
                return;
            } else {
                return;
            }
        } else {
            AppUtil.showAlertDialog(getActivity().getApplicationContext(), getString(R.string.khong_ket_noi), getString(R.string.khong_ket_noi_chi_tiet));
        }
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
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


}
