package tikfans.tikplus.service;

import android.util.Log;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import tikfans.tikplus.util.FirebaseUtil;

/**
 * Created by sev_user on 12/24/2016.
 */

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("Khang", "token:  " + token);
        try {
            DatabaseReference tokenRef = FirebaseUtil.getCurrentUserRef().child("token");
            tokenRef.setValue(token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
