package embedded.korea.ac.kr.emoodchart.api.response;

import com.google.gson.JsonObject;

/**
 * Created by T22mo on 2016-08-30.
 */
public class VersionResponse extends ApiResponse<JsonObject> {
    private String version;

    public String getVersion() {
        return version;
    }
}
