package embedded.korea.ac.kr.emoodchart;

import embedded.korea.ac.kr.emoodchart.api.ApiClient;
import embedded.korea.ac.kr.emoodchart.api.response.*;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface AdminApiClient {

    @POST("{inst}/login")
    Call<ApiResponse> login(@Path("inst") int iid, @Body Map<String, String> params);

    @POST("proj/{proj}/user/{user}")
    Call<ApiResponse> createuser(@Path("proj") int pid, @Path("user") int uid);

    @POST("proj/{proj}/user/{user}/activate")
    Call<ApiResponse> activate(@Path("proj") int pid, @Path("user") int uid);

    @POST("proj/{proj}/user/{user}/deactivate")
    Call<ApiResponse> deactivate(@Path("proj") int pid, @Path("user") int uid);
}