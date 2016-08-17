package embedded.korea.ac.kr.emoodchart.api.response;

import com.google.gson.JsonObject;

/**
 * Created by Skais on 2016-08-17.
 */
public class CodeResponse extends ApiResponse<JsonObject> {
    private String code;

    public String getCode() { return code; }
}
