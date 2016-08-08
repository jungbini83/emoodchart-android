package embedded.korea.ac.kr.emoodchart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RestartReceiver extends BroadcastReceiver {
    public RestartReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, InitService.class);
        if (intent != null) service.setAction(intent.getAction());
        context.startService(service);
    }
}
