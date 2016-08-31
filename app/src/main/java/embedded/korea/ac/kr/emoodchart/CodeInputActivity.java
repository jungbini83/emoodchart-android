package embedded.korea.ac.kr.emoodchart;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import embedded.korea.ac.kr.emoodchart.api.ApiService;
import embedded.korea.ac.kr.emoodchart.api.response.ApiResponse;
import embedded.korea.ac.kr.emoodchart.api.response.CodeResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CodeInputActivity extends AppCompatActivity implements Callback<ApiResponse<CodeResponse>> {
    EditText codeEdit;
    Button codeConfirmBtn;
    Button codeCancelBtn;
    ApiService api = new ApiService();

    static final String VIA_FITBIT = "action.fitbit";
    static final String VIA_CODE = "action.code";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_input);

        codeEdit        = (EditText)    findViewById(R.id.code_edit);
        codeConfirmBtn  = (Button)      findViewById(R.id.code_confirm);
        codeCancelBtn   = (Button)      findViewById(R.id.code_cancel);

        String type = getIntent().getAction();
        if (type.equals(VIA_FITBIT)) {
            String code = getIntent().getStringExtra("code");
            String url = getIntent().getStringExtra("url");
            codeEdit.setText(code);

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        } else if (type.equals(VIA_CODE)) {
            // Do nothing. NOW.
        }

        codeConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int code = Integer.parseInt(codeEdit.getText().toString());
                sendCodeToServer(code);
            }
        });

        codeCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }
    private void sendCodeToServer(int code)
    {
        api.authenticate(code).enqueue(this);
    }

    @Override
    public void onResponse(Call<ApiResponse<CodeResponse>> call, Response<ApiResponse<CodeResponse>> response) {
        switch(response.code()) {
            case 200: Log.v("teemo",response.body().toString() ); break;
            default: return;
        }


        int userId = response.body().getResult().getIdentifier();
        int projId = response.body().getResult().getProj_id();
        int instId = response.body().getResult().getInst_id();
        String hash = response.body().getResult().getHash();
        Log.v("teemo", Integer.toString(userId) );
        Log.v("teemo", Integer.toString(projId) );
        Log.v("teemo", Integer.toString(instId) );
        Log.v("teemo", hash );

        UserInfo.set(this, instId, projId, userId, hash);

        Intent intent = new Intent(this , StatusActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onFailure(Call<ApiResponse<CodeResponse>> call, Throwable t) {
        //Fail
        Toast.makeText(CodeInputActivity.this.getBaseContext()  ,"서버 연결에 실패하였습니다.",Toast.LENGTH_LONG).show();
    }
}
