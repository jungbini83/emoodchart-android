package embedded.korea.ac.kr.emoodchart.api.response;

import com.google.gson.JsonObject;

/**
 * Created by Skais on 2016-08-17.
 */
public class CodeResponse {
    private int inst_id;
    private int proj_id;
    private String hash;
    private int identifier;


    public int getInst_id() {
        return inst_id;
    }

    public int getProj_id() {
        return proj_id;
    }

    public String getHash() {
        return hash;
    }

    public int getIdentifier() {
        return identifier;
    }
}
