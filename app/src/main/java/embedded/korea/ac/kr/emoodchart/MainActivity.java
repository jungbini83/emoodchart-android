package embedded.korea.ac.kr.emoodchart;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import embedded.korea.ac.kr.emoodchart.api.*;
import embedded.korea.ac.kr.emoodchart.api.response.ApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 앱을 시작하면 기존에 설정된 아이디가 있는지 확인하고, 해당 아이디가 유효한지를 서버를 통해 확인함
 * 아니라면 새로운 아이디를 할당받기 위해서는 두 가지 프로세스가 존재함
 * - fitbit 아이디를 통한 확인
 * - 관리자 화면에서 특정 환자의 요청을 수락하면 이를 결합하도록 함
 */
public class MainActivity extends Activity {
    private ApiService api = new ApiService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UserInfo info = new UserInfo(this);

        if (info.isValid()) {
			api.checkAuth(info).enqueue(new Callback<ApiResponse>() {
				@Override
				public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    //TODO: 성공여부 확인 후 다음 창으로 넘어감
                    // 실패일 경우에는 failure 상태일 때의 행동으로 넘어감
                    onAuthorized();
				}

				@Override
				public void onFailure(Call<ApiResponse> call, Throwable t) {
                    //TODO: 기존 방법으로 로그인 실패, 어떤 방식으로 로그인 할 것인가 정하도록 함
                    // 1. 관리자가 발급한 issue ID를 통한 로그인
                    // 2. fitbit 계정을 통한 로그인
                    setLoginLayout();
				}
			});
        } else {
            setLoginLayout();
        }
    }

    private void setLoginLayout() {

        Button fitbitLoginBtn, coordLoginBtn;
        setContentView(R.layout.login);

        fitbitLoginBtn  = (Button)findViewById(R.id.btnLoginWithFitbit);
        coordLoginBtn   = (Button)findViewById(R.id.btnLoginWithCoord);

        fitbitLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.naver.com"));
                startActivity(browserIntent);
                openCodeInputActivity();

            }
        });

        coordLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCodeInputActivity();
            }
        });

        //TODO: 버튼 이벤트 연동
    }
    private void onAuthorized() {
        Intent intent = new Intent(this, StatusActivity.class);
        startActivity(intent);
        finish();
    }
    private void openCodeInputActivity()
    {
        Intent intent = new Intent(this, CodeInputActivity.class);
        startActivity(intent);
    }
}