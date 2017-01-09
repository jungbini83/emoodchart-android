package embedded.korea.ac.kr.emoodchart.light;

import android.content.Context;
import android.content.Intent;
import embedded.korea.ac.kr.emoodchart.UserInfo;
import embedded.korea.ac.kr.emoodchart.api.APIHelper;
import embedded.korea.ac.kr.emoodchart.api.ApiClient;
import embedded.korea.ac.kr.emoodchart.api.response.ApiResponse;
import embedded.korea.ac.kr.emoodchart.api.response.NotificationResponse;
import embedded.korea.ac.kr.emoodchart.services.EMCService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 새로운 데이터가 추가될 때마다 새롭게 생성하며 데이터의 업로드의 성공, 실패에 따라 데이터베이스를 조작하는 역할을 수행함
 */
public class LightUploader implements Callback<ApiResponse<NotificationResponse>> {
    private Date mKey;
    private Context ctx;
    private float mValue;
    private SimpleDateFormat sdf;
    private LightDBHelper mDB;
    private ApiClient api = APIHelper.createClient();

    private final static String DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";


    public LightUploader(Context ctx, float mValue) {
        this.ctx = ctx;
        this.mKey = Calendar.getInstance().getTime();
        this.mValue = mValue;
        this.sdf = new SimpleDateFormat(DATETIME_FORMAT);
        this.sdf.setTimeZone(TimeZone.getDefault()); // 재부팅 없이도 시스템 세팅이 변함에 따라 자동으로 가져올 수 있도록
        this.mDB = new LightDBHelper(ctx);
    }

    public void start(UserInfo user) {
        Map<String,Float> data = mDB.getData(mKey);
        data.put(sdf.format(mKey), mValue);
        APIHelper.uploadLight(api, user, data).enqueue(this);
    }

    @Override
    public void onResponse(Call<ApiResponse<NotificationResponse>> call, Response<ApiResponse<NotificationResponse>> response) {
        // 성공했을 경우에는 데이터베이스에 존재했던 데이터들은 삭제하고 마무리한다
        mDB.clearUploadedData(mKey);
        mDB.close();

        NotificationResponse res = response.body().getResult();
        if (res.getHasNotice()) {
            Intent intent = new Intent(ctx, EMCService.class);
            intent.setAction(EMCService.ACT_NOTIFY_PREDICTION);
            ctx.startService(intent);
        }
    }

    @Override
    public void onFailure(Call<ApiResponse<NotificationResponse>> call, Throwable t) {
        mDB.storeUploadFailed(sdf, mKey, mValue);
        mDB.close();
    }
}
