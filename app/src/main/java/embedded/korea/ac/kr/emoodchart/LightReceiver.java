package embedded.korea.ac.kr.emoodchart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LightReceiver extends BroadcastReceiver {
    public LightReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if(action.equals(Intent.ACTION_SCREEN_ON)){
            Intent service = new Intent(context, LightUploadService.class);
            service.setAction("ACTION.GET_LIGHT");
            context.startService(service);
        }
    }
}
