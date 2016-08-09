package embedded.korea.ac.kr.emoodchart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
