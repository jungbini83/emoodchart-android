package embedded.korea.ac.kr.emoodchart.api.response;

import com.google.gson.JsonObject;

/**
 * Created by T22mo on 2016-08-17.
 */
public class FitbitResponse extends ApiResponse<JsonObject> {
    private String code;
    private String url;

    public String getCode() {
        return  code;
    }
    public String getUrl() {
        return url;
    }
}
