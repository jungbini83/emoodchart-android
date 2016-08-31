package embedded.korea.ac.kr.emoodchart.api;

import embedded.korea.ac.kr.emoodchart.UserInfo;
import embedded.korea.ac.kr.emoodchart.api.response.ApiResponse;
import embedded.korea.ac.kr.emoodchart.api.response.CodeResponse;
import embedded.korea.ac.kr.emoodchart.api.response.FitbitResponse;
import embedded.korea.ac.kr.emoodchart.api.response.VersionResponse;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.Map;

/**
 * Created by Skais on 2016-08-09.
 */
public class ApiService {
    public ApiDefinition service;
    private static final String URL_BASE = "http://52.78.135.214:4000";
    private static final String URL_API = URL_BASE + ":4000/api/v2/";
    public static final String URL_APK = URL_BASE + "/apk";

    public ApiService() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        this.service = new Retrofit.Builder()
                .baseUrl(ApiService.URL_API)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiDefinition.class);
    }

    public Call<Void> checkAuth(UserInfo info) {
        return service.checkAuth(info.getInstId(), info.getProjId(), info.getUserId(), info.getHash());
    }

    public Call<ApiResponse> uploadLight(UserInfo info, Map<String, Float> body) {
        return service.uploadLight(info.getInstId(), info.getProjId(), info.getUserId(), info.getHash(), body);
    }

    public Call<ApiResponse<FitbitResponse>> loginWithFitbit() {
        return service.loginWithFitbit();
    }

    public Call<ApiResponse<VersionResponse>> checkApkUpdate() {
        return service.checkUpdate();
    }

    public Call<ApiResponse<CodeResponse>> authenticate(int code) {
        return service.authenticate(code);
    }

    public static String genSurveyUrl(UserInfo user) {
        return URL_BASE + "/inst/"+user.getInstId()+"/proj/"+user.getProjId()+"/user/"+user.getUserId()+"/survey?hash="+user.getHash();
    }
}
