package embedded.korea.ac.kr.emoodchart.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 화면이 켜지는 이벤트를 받아 EMCService에 넘긴다
 */
public class EMCScreenEventReceiver extends BroadcastReceiver {
    public EMCScreenEventReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action.equals(Intent.ACTION_SCREEN_ON)){
            Intent service = new Intent(context, EMCService.class);
            service.setAction(EMCService.ACT_SCREEN_ON);
            context.startService(service);
        }
    }
}
