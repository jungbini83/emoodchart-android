package embedded.korea.ac.kr.emoodchart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 앱에서 활용되는 전반적인 메세지들을 전부 받아 처리한다
 */
public class EMCReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Intent service = new Intent(context, EMCService.class);
        if (action != null) {
            service.setAction(intent.getAction());
        }

        context.startService(service);
    }
}
