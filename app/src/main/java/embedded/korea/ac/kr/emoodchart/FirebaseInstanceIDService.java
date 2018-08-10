package embedded.korea.ac.kr.emoodchart;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "MyFirebaseIDService";

    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
    }
}
