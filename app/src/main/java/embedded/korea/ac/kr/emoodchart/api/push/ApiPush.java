/*
 * Push 서비스 등록을 위한 클래스
 * 1. sendToMobileServer: 사용자 정보와 함께 Firebase에서 발급된 토큰을 emoodchart DB에 저장
 * (저장된 토큰을 이용하여 사용자별로 Push 메시지를 보낼 수 있음)
 *
 * 2. getResult: 토큰 저장 후 결과 메시지를 리턴해주는 get 함수
 */

package embedded.korea.ac.kr.emoodchart.api.push;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;

import embedded.korea.ac.kr.emoodchart.UserInfo;

public class ApiPush {

    // 디바이스를 등록하기 위한 테스트 서버 주소
    public static String QUERY_PATID_URL  = "http://jungbini.com:3000/process/queryPatId";
    private static String PUSH_URL         = "http://jungbini.com:3000/process/adddevice";
    private static String result;
    private static String patID;

    private Context currentContext;
    private JsonParser jsonParser;

    public ApiPush(Context context) {
        currentContext = context;
        jsonParser = new JsonParser();
    }

    public void sendToMobileServer(final UserInfo userInfo, final String regId, final String URL) {

        StringRequest request = new StringRequest(Request.Method.POST, URL,
                new Response.Listener<String>() {
                    public void onResponse(String response) {
                        try {
                            System.out.println("onResponse() 호출됨 : " + response);
                            JsonObject jsonObject = (JsonObject) jsonParser.parse(response);

                            // request 결과를 result 변수에 저장
                            // QUERY_PATID_URL이라면 피험자의 ID가 저장
                            result = jsonObject.get("message").toString();

                            if (URL == QUERY_PATID_URL)
                                sendToMobileServer(userInfo, regId, PUSH_URL);
                            else
                                viewInfoDialog("푸시 서비스 등록", result);

                        } catch (Exception e) {
                            e.printStackTrace();
                            result = e.getMessage();

                            if (URL == QUERY_PATID_URL)
                                viewInfoDialog("피험자 ID 조회", result);
                            else
                                viewInfoDialog("푸시 서비스 등록", result);
                        }
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        result = error.getMessage();

                        if(URL == QUERY_PATID_URL)
                            viewInfoDialog("피험자 ID 조회", "서버와의 통신이 불안정합니다.\n다시 시도해 보세요:" + result);
                        else
                            viewInfoDialog("푸시 서비스 등록", "서버와의 통신이 불안정합니다.\n다시 시도해 보세요:" + result);
                    }
                }
        ) {
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                if (URL == QUERY_PATID_URL) {
                    params.put("userId", String.valueOf(userInfo.getUserId()));
                    params.put("projId", String.valueOf(userInfo.getProjId()));
                    params.put("instId", String.valueOf(userInfo.getInstId()));
                } else {
                    params.put("patId", result);
                    params.put("regId", regId);
                }

                return params;
            }
        };

        request.setShouldCache(false);
        Volley.newRequestQueue(currentContext).add(request);
    }

    public void viewInfoDialog(String title, String message) {
        AlertDialog.Builder alert = new AlertDialog.Builder(currentContext);
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
