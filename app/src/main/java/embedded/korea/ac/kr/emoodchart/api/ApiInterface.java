package embedded.korea.ac.kr.emoodchart.api;

import embedded.korea.ac.kr.emoodchart.UserInfo;
import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * Created by Skais on 2016-08-09.
 */
public class ApiInterface {
    private ApiService service;
    public ApiInterface() {
        this.service = new Retrofit.Builder()
                .baseUrl("https://")
                .build()
                .create(ApiService.class);
    }

    public Call<ApiResponse> checkAuth(UserInfo info) {
        return service.checkAuth(info.getInstId(), info.getProjId(), info.getUserId(), info.getHash());
    }

    public static String genSurveyUrl(UserInfo user) {
        return null;
    }
}
