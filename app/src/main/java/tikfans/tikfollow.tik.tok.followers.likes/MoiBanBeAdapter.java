package tikfans.tikfollow.tik.tok.followers.likes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import tikfans.tikfollow.tik.tok.followers.likes.util.CircleTransform;
import tikfans.tikfollow.tik.tok.followers.likes.util.FirebaseUtil;

public class MoiBanBeAdapter extends RecyclerView.Adapter<MoiBanBeAdapter.UserViewHolder> {
    private Context mContext;
    private ArrayList<String> referredToList;
    private OnSetupNameListener mOnSetupNameListener;
    private OnSetupPhotoListener mOnSetupPhotoListener;

    public MoiBanBeAdapter(Context context, OnSetupNameListener onSetupNameListener, OnSetupPhotoListener onSetupPhotoListener) {
        mContext = context;
        mOnSetupNameListener = onSetupNameListener;
        mOnSetupPhotoListener = onSetupPhotoListener;
    }


    @Override
    public MoiBanBeAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(mContext);

        View itemView1 = li.inflate(R.layout.invite_referral_item, parent, false);
        return new UserViewHolder(itemView1);

    }

    @Override
    public void onViewRecycled(@NonNull UserViewHolder holder) {
        super.onViewRecycled(holder);
        holder.mPhotoRef.removeEventListener(holder.mPhotoRefListener);
        holder.mNameRef.removeEventListener(holder.mNameRefListener);
    }

    @Override
    public void onBindViewHolder(final MoiBanBeAdapter.UserViewHolder holder, int position) {
        ValueEventListener nameRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.getValue(String.class);
                mOnSetupNameListener.onSetupName(holder, name);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };
        DatabaseReference userNameRef = FirebaseUtil.getAccountRef().child(referredToList.get(position)).child(FirebaseUtil.TIKTOK_USER_NAME);
        userNameRef.addListenerForSingleValueEvent(nameRefListener);

        holder.mNameRef = userNameRef;
        holder.mNameRefListener = nameRefListener;

        ValueEventListener photoRefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String photo = dataSnapshot.getValue(String.class);
                mOnSetupPhotoListener.onSetupPhoto(holder, photo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };

        DatabaseReference userPhotoRef = FirebaseUtil.getAccountRef().child(referredToList.get(position)).child(FirebaseUtil.PHOTO_REF);
        userPhotoRef.addListenerForSingleValueEvent(photoRefListener);

        holder.mPhotoRef = userPhotoRef;
        holder.mPhotoRefListener = photoRefListener;
    }

    @Override
    public int getItemCount() {
        if (referredToList == null) return 0;

        return referredToList.size();
    }

    public void setReferredToList(ArrayList<String> referredToList) {
        this.referredToList = referredToList;
    }



    public class UserViewHolder extends RecyclerView.ViewHolder {
        private TextView txtName;
        private ImageView profilePictureView;
        public DatabaseReference mNameRef;
        public ValueEventListener mNameRefListener;

        public DatabaseReference mPhotoRef;
        public ValueEventListener mPhotoRefListener;

        public UserViewHolder(View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtUserName);
            profilePictureView = itemView.findViewById(R.id.author_icon);
        }

        public void setName(String name) {
            txtName.setText(name);
        }

        public void setPhoto(String photo) {
            Picasso.get().load(photo).transform(new CircleTransform())
                    .into(profilePictureView);
        }

    }

    public interface OnSetupNameListener {
        void onSetupName(UserViewHolder holder, String name);
    }

    public interface OnSetupPhotoListener {
        void onSetupPhoto(UserViewHolder holder, String photo);
    }


}
