package embedded.korea.ac.kr.emoodchart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class EMCScreenEventReceiver extends BroadcastReceiver {
    public EMCScreenEventReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action.equals(Intent.ACTION_SCREEN_ON)){
            Intent service = new Intent(context, EMCService.class);
            context.startService(service);
        }
    }
}
