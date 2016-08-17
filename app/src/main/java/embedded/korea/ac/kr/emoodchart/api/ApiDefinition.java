package embedded.korea.ac.kr.emoodchart.api;

import embedded.korea.ac.kr.emoodchart.api.response.ApiResponse;
import embedded.korea.ac.kr.emoodchart.api.response.CodeResponse;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface ApiDefinition {
    /**
     * 수집된 조도 데이터를 서버로 전송한다.<br>
     * 500 - 서버 자체 에러<br>
     * 400 - iid, pid, ident 중 하나라도 존재하지 않는 아이디일 경우, 혹은 전송된 데이터의 포맷이 이상할 경우<br>
     * 401 - hash가 올바른 값이 아닐 경우<br>
     * 200 - 성공적으로 처리됨<br>
     * @param iid   기관 아이디
     * @param pid   프로젝트 아이디
     * @param ident 프로젝트 내에서의 사용자 고유 번호
     * @param hash  사용자의 비밀값
     * @param body  전송할 조도값이며 (시간,조도값) 형식으로 되어 있음
     * @return ApiResponse를 참고
     * @see ApiResponse
     */
    @POST("inst/{iid}/proj/{pid}/user/{ident}/light")
    Call<ApiResponse> uploadLight(@Path("iid") int iid, @Path("pid") int pid, @Path("ident") int ident, @Query("hash") String hash, @Body Map<String, Float> body);

    /**
     * Fitbit을 이용하여 로그인을 시도한다.<br>
     * 500 - 서버 자체 에러<br>
     * 200 - 성공적으로 처리됨<br>
     * @return {<br>
     *     url: 이 url로 접속하면 fitbit을 이용한 로그인을 시작할 수 있음<br>
     *     code: fitbit에서 돌아온 이후 이 코드를 {@link #authenticate}의 path parameter로 넘겨줘야 함<br>
     * }
     * @see #authenticate
     * @see ApiResponse
     */
    @POST("auth/fitbit")
    Call<ApiResponse> loginWithFitbit();

    /**
     * 부여받은 authentication code를 이용하여 로그인을 시도함
     * @param code {@link #loginWithFitbit}으로 스스로 발급하거나 관리자에게 전달받은 코드
     * @return {<br>
     *     inst_id, proj_id, identifier, hash<br>
     * }
     * @see ApiResponse
     */
    @GET("auth/{code}")
    @Headers("x-access-by: application")
    Call<CodeResponse> authenticate(@Path("code") int code);


    @HEAD("apk")
    Call<ApiResponse> checkUpdate(@Query("version") String version);

    /**
     * 주어진 정보가 유효하고 실제로 존재하는 인증 정보인지 확인
     * 500 - 서버 자체 에러<br>
     * 400 - iid, pid, ident 중 하나 이상이 존재하지 않음
     * 301 - 잘못된 hash 정보
     * 200 - 인증 가능한 정보임을 의미함
     * @param iid   기관 아이디
     * @param pid   프로젝트 아이디
     * @param ident 프로젝트 내에서의 사용자 고유 번호
     * @param hash  사용자의 비밀값
     * @return ApiResponse를 참고
     * @see ApiResponse
     */
    @HEAD("inst/{iid}/proj/{pid}/user/{ident}")
    Call<ApiResponse> checkAuth(@Path("iid") int iid, @Path("pid") int pid, @Path("ident") int ident, @Query("hash") String hash);

    @GET("insts")
    Call<ApiResponse> getInsts();
}