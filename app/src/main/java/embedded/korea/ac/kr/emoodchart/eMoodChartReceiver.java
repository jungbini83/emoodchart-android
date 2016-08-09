package embedded.korea.ac.kr.emoodchart;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class eMoodChartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Intent service = new Intent(context, eMoodChartService.class);
        if (action != null && action.equals(Intent.ACTION_SCREEN_ON)) {
            service.setAction(eMoodChartService.LIGHT);
        } else {
            service.setAction(intent.getAction());
        }

        context.startService(service);
    }
}
