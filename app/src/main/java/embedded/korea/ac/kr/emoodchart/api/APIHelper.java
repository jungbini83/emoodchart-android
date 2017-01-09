package embedded.korea.ac.kr.emoodchart.api;

import embedded.korea.ac.kr.emoodchart.BuildConfig;
import embedded.korea.ac.kr.emoodchart.R;
import embedded.korea.ac.kr.emoodchart.UserInfo;
import embedded.korea.ac.kr.emoodchart.api.response.*;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.Map;

/**
 * Created by skais on 1/5/17.
 */
public class APIHelper {
    private static final String URL_API = BuildConfig.API_URL + "/api/v2/";
    public static final String URL_APK = BuildConfig.WEB_URL + "/apk";

    public static ApiClient createClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        return new Retrofit.Builder()
                .baseUrl(URL_API)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiClient.class);
    }

    public static Call<Void> checkAuth(ApiClient service, UserInfo info) {
        return service.checkAuth(info.getInstId(), info.getProjId(), info.getUserId(), info.getHash());
    }

    public static Call<ApiResponse<NotificationResponse>> uploadLight(ApiClient service, UserInfo info, Map<String, Float> body) {
        return service.uploadLight(info.getInstId(), info.getProjId(), info.getUserId(), info.getHash(), body);
    }

    public static String genSurveyUrl(UserInfo user) {
        return BuildConfig.WEB_URL + "/inst/"+user.getInstId()+"/proj/"+user.getProjId()+"/user/"+user.getUserId()+"/survey?hash="+user.getHash();
    }

    public static String genDashbardUrl(UserInfo user) {
        return BuildConfig.WEB_URL + "/inst/"+user.getInstId()+"/proj/"+user.getProjId()+"/user/"+user.getUserId()+"/?hash="+user.getHash();
    }
}
