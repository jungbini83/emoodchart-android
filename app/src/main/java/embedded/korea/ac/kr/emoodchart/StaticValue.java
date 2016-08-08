package embedded.korea.ac.kr.emoodchart;

/**
 * 기본적인 url을 가지고 있는 클래스
 */
public class StaticValue {
    private static final String url = "http://52.68.83.209:4000";

    public static String checkUserUrl(int id,int iid,int pid,String hash) {
        return url + "/api/inst/"+iid+"/project/"+pid+"/user/" + id+"?hash="+hash;
    }

    public static String createUserUrl() {
        return url + "/user/";
    }
    
    public static String getProjectURL(int iid)
    {
    	return url+"/api/inst/"+iid+"/projects/";
    }
    
    public static String getInstURL()
    {
    	return url+"/api/inst/";
    }
    public static String getAvailIdURL(int iid,int pid)
    {
    	return url+"/api/inst/"+iid+"/project/"+pid+"/users/";
    }
    public static String requestPermission(int iid,int pid, int uid)
    {
    	return url+"/api/inst/"+iid+"/project/"+pid+"/user/"+uid;
    }
    public static String checkUpdate()
    {
    	return url+"/apk/update?version=3";
    }

    public static String uploadLightUrl(int id,int iid,int pid,String hash) { return url + "/api/inst/"+iid+"/project/"+pid+"/user/" + id + "/light?hash="+hash; }
}