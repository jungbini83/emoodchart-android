package embedded.korea.ac.kr.emoodchart;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

import embedded.korea.ac.kr.emoodchart.api.APIHelper;
import embedded.korea.ac.kr.emoodchart.api.ApiClient;
import embedded.korea.ac.kr.emoodchart.api.push.ApiPush;
import embedded.korea.ac.kr.emoodchart.services.EMCService;

import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class StatusActivity extends Activity {
    private ApiClient mApi;
    private ApiPush mPush;

    @Override
    public void onBackPressed() {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.status);

        mApi = APIHelper.createClient();
        mPush = new ApiPush(StatusActivity.this);

        FirebaseMessaging.getInstance().subscribeToTopic("notice");

        // 조명 사용 가능 여부 확인
        SensorManager sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        boolean light_available = (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null);

        TextView availLight = (TextView)findViewById(R.id.avail_light);
        if(light_available){
            availLight.setText("가능");
        } else {
            availLight.setText("불가능");
        }

        // 재부팅 시 자동 실행 가능 여부 확인
        TextView availReboot = (TextView)findViewById(R.id.avail_reboot);
        SharedPreferences pf = getSharedPreferences("appstatus", MODE_PRIVATE);
        if (pf.getBoolean("rebootable", true)) {
            availReboot.setText("가능");
        } else {
            availReboot.setText("불가능 혹은 확인필요");
        }

        // 서베이 이동 버튼 링크
        findViewById(R.id.button_opensurvey).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
            Uri uri = Uri.parse(APIHelper.genSurveyUrl(new UserInfo(getBaseContext())));
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(browserIntent);
			}
		});

        // 업데이트 확인 버튼 링크
        findViewById(R.id.btn_update).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(APIHelper.URL_APK));
                startActivity(browserIntent);
			}
		});

        // 푸시 서버에 등록하는 버튼 링크
        findViewById(R.id.btn_register).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String regId = FirebaseInstanceId.getInstance().getToken();                                     // Firebase에서 기기에 할당된 Token 받아오기
                mPush.sendToMobileServer(new UserInfo(getBaseContext()), regId, ApiPush.QUERY_PATID_URL);        // Token을 emoodchart 서버에 등록
            }
        });

        // 수집 서비스 시작
        Intent service = new Intent(this, EMCService.class);
        startService(service);
    }

    protected void onNewIntent(Intent intent) {

        if (intent != null && intent.getExtras() != null)
            processIntent(intent);

        super.onNewIntent(intent);
    }

    private void processIntent(Intent intent) {
        String message = intent.getStringExtra("data");
        viewInfoDialog("설문 조사 알람", message);
    }

    public void viewInfoDialog(String title, String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(StatusActivity.this);
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.setTitle(title);
        alert.setMessage(message);
        alert.show();
    }
}