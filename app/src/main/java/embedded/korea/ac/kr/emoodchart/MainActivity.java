package embedded.korea.ac.kr.emoodchart;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemSelectedListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 사용자 고유 아이디를 서버로 할당받고, 사용되는 서비스들을 초기화함
 */
public class MainActivity extends Activity implements OnClickListener, Response.ErrorListener, Response.Listener<String>, Runnable {
    SharedPreferences pfSetting;
    RequestQueue rq;
    Handler handler = new Handler();
    Spinner prjSpinner, instSpinner, availIdSpinner;
    Button startBtn;
    EditText idTxt;
    ArrayList<String> projectList, instList, availList;
    ArrayList<Integer> projectIdList, instIdList, availIdList;
    
    ArrayAdapter iAdapter , pAdapter, aAdapter;
    ProgressBar loadingBar;
    
    int instId,prjId,userId;
    String hash;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pfSetting	= getSharedPreferences("setting", MODE_PRIVATE);
        userId		= pfSetting.getInt("userId", 0);
        instId		= pfSetting.getInt("instId", 0);
        prjId		= pfSetting.getInt("projectId", 0);
        hash		= pfSetting.getString("hash","");
        rq = Volley.newRequestQueue(this);
        
        if (userId != 0) {
            checkUserId();
            return;
        }

        setContentView(R.layout.setting);
        
      
        
        startBtn		= (Button)		findViewById(R.id.start);
        idTxt			= (EditText)	findViewById(R.id.userId);
        loadingBar		= (ProgressBar)	findViewById(R.id.prgBar);
        instSpinner		= (Spinner)		findViewById(R.id.main_inst_spinner);
        prjSpinner		= (Spinner)		findViewById(R.id.main_project_spinner);
        availIdSpinner	= (Spinner)		findViewById(R.id.main_availid_spinner);
       
        
        projectList		= new ArrayList<String>();
        projectIdList	= new ArrayList<Integer>();
        instList		= new ArrayList<String>();
        instIdList		= new ArrayList<Integer>();
        availList		= new ArrayList<String>();
        availIdList		= new ArrayList<Integer>();
        
        iAdapter		= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item , instList);
        pAdapter		= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, projectList);
        aAdapter		= new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, availList);
        
        startBtn.setOnClickListener(this);
        

        
        instSpinner.setAdapter(iAdapter);
        prjSpinner.setAdapter(pAdapter);
        availIdSpinner.setAdapter(aAdapter);
        
        
        setInstList();
        
        instSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
	
			
				instId = instIdList.get(position);	
				setProjectList(instId);
				
				startBtn.setVisibility(View.INVISIBLE);
				idTxt.setVisibility(View.INVISIBLE);
				availIdSpinner.setVisibility(View.INVISIBLE);
				
				pAdapter.clear();
				projectIdList.clear();
				projectList.clear();
				prjSpinner.setVisibility(View.VISIBLE);
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
        
        prjSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				
				prjId = projectIdList.get(position);
				setAvailableIdList(instId, prjId);
				
				startBtn.setVisibility(View.INVISIBLE);
				idTxt.setVisibility(View.INVISIBLE);
				
				aAdapter.clear();
				availIdList.clear();
				availList.clear();
				availIdSpinner.setVisibility(View.VISIBLE);
				
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
        
        availIdSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				userId = availIdList.get(position);
				startBtn.setVisibility(View.VISIBLE);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
        
        
    }
    private void setInstList()
    {
    	RequestQueue queue = Volley.newRequestQueue(this);
    	String url = StaticValue.getInstURL();

		JSONObject params = new JSONObject();
	
		JsonObjectRequest jReq = new JsonObjectRequest(Request.Method.GET , url, params, 
					new Response.Listener<JSONObject>() {

						@Override
						public void onResponse(JSONObject jObj) {
						
							try {
								
								JSONObject jResObj = jObj.getJSONObject("result");
								Iterator keyList = jResObj.keys();
								instList.clear();
								instIdList.clear();
								
								while(keyList.hasNext())
								{
									String k = (String) keyList.next();
									Log.v("teemo",jResObj.getJSONObject(k).toString());
									String id = jResObj.getJSONObject(k).getString("id");
									String name = jResObj.getJSONObject(k).getString("name");
											
									instList.add(name);
									instIdList.add( Integer.parseInt(id));
								}
								if(instIdList.size()<=0)
								{
									Toast.makeText(getBaseContext(), "가능한 기관이 존재하지 않습니다.",Toast.LENGTH_LONG).show();
								}
								else
								{																	
									instId = instIdList.get(0);
									iAdapter.notifyDataSetChanged();
									instSpinner.setVisibility(View.VISIBLE);
									loadingBar.setVisibility(View.INVISIBLE);
								}
								
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}							
						}
					}, 
					new Response.ErrorListener() {

						@Override
						public void onErrorResponse(VolleyError vError) {
							Log.v("teemo","Get inst list volley error: "+ vError.toString());		
							Toast.makeText(getBaseContext(), "목록을 불러오지 못하였습니다.",Toast.LENGTH_LONG).show();
							loadingBar.setVisibility(View.INVISIBLE);
							finish();
						}
					}
				);
				
		
		queue.add(jReq);
    }
    private void setProjectList(int iid)
    {
    	RequestQueue queue = Volley.newRequestQueue(this);
    	String url = StaticValue.getProjectURL(iid);
    	
		
		JSONObject params = new JSONObject();
		
	
		JsonObjectRequest jReq = new JsonObjectRequest(Request.Method.GET , url, params, 
					new Response.Listener<JSONObject>() {

						@Override
						public void onResponse(JSONObject jObj) {
							
							Log.v("teemo",jObj.toString());
							try {
								
								JSONArray jArr = jObj.getJSONArray("result");
								projectList.clear();
								projectIdList.clear();
								
								for(int i=0 ; i<jArr.length() ; i++)
								{
									JSONObject j = jArr.getJSONObject(i);
									
									projectList.add(j.getString("name"));
									projectIdList.add(j.getInt("id"));
								}
								if(projectIdList.size()<=0)
								{
									Toast.makeText(getBaseContext(), "가능한 프로젝트가 존재하지 않습니다.",Toast.LENGTH_LONG).show();
								}
								else
								{
									prjId = projectIdList.get(0);
									pAdapter.notifyDataSetChanged();
									prjSpinner.setVisibility(View.VISIBLE);
								}
								
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}							
						}
					}, 
					new Response.ErrorListener() {

						@Override
						public void onErrorResponse(VolleyError vError) {
							Log.v("Internet-Error", vError.toString());		
							Toast.makeText(getBaseContext(), "목록을 불러오지 못하였습니다.",Toast.LENGTH_LONG).show();	
						}
					}
				);
				
		
		queue.add(jReq);
    }
    public void setAvailableIdList(int iid, int pid)
    {
    	RequestQueue queue = Volley.newRequestQueue(this);
    	String url = StaticValue.getAvailIdURL(iid,pid);
    	
		JSONObject params = new JSONObject();
		
	
		JsonObjectRequest jReq = new JsonObjectRequest(Request.Method.GET , url, params, 
					new Response.Listener<JSONObject>() {

						@Override
						public void onResponse(JSONObject jObj) {
							
							Log.v("teemo",jObj.toString());

							try {
								
								JSONArray jArr = jObj.getJSONObject("result").getJSONArray("users");
								availList.clear();
								availIdList.clear();
								
								for(int i=0 ; i<jArr.length() ; i++)
								{
									JSONObject j = jArr.getJSONObject(i);
									
									availList.add(j.getString("name"));
									availIdList.add(j.getInt("identifier"));
								}
								
								if( availIdList.size()<=0)
								{
									Toast.makeText(getBaseContext(), "사용 가능한 ID가 없습니다.",Toast.LENGTH_LONG).show();
								}
								else
								{
									userId = availIdList.get(0);
									aAdapter.notifyDataSetChanged();
									availIdSpinner.setVisibility(View.VISIBLE);									
								}
								
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}							
						}
					}, 
					new Response.ErrorListener() {

						@Override
						public void onErrorResponse(VolleyError vError) {
							Log.v("teemo","Get available id list volley error: "+ vError.toString());						
							Toast.makeText(getBaseContext(), "ID목록을 불러오지 못하였습니다.",Toast.LENGTH_LONG).show();
							availIdSpinner.setVisibility(View.INVISIBLE);
						}
					}
				);
				
		
		queue.add(jReq);
    }
    private void createUserId() {
        String url = StaticValue.requestPermission(instId,prjId,userId);
        Log.v("teemo",url);
     
    	JSONObject params = new JSONObject();
    	
        JsonObjectRequest jReq = new JsonObjectRequest(Request.Method.POST , url, params, 
				new Response.Listener<JSONObject>() {

					@Override
					public void onResponse(JSONObject jObj) {
					
						try {
							
							String hash = jObj.getJSONObject("result").getString("hash");
							Log.v("teemo","test = "+jObj.toString());
							

							pfSetting.edit().putInt("userId", userId).apply();
							pfSetting.edit().putInt("projectId", prjId).apply();
							pfSetting.edit().putInt("instId", instId).apply();
							pfSetting.edit().putString("hash", hash).apply();
							init();
							
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							Log.v("teemo","JSON error");
							e.printStackTrace();
						}							
					}
				}, 
				new Response.ErrorListener() {

					@Override
					public void onErrorResponse(VolleyError vError) {
						Log.v("Internet-Error", vError.toString());		

						Toast.makeText(getBaseContext(), "ID확인을 하지 못하였습니다.",Toast.LENGTH_LONG).show();
					}
				}
			);
        rq.add(jReq);
    }

    private void checkUserId() {
    	RequestQueue queue = Volley.newRequestQueue(this);
    	String url = StaticValue.checkUserUrl(instId,prjId,userId,hash);
        Log.v("teemo",url);
        
		JSONObject params = new JSONObject();

		JsonObjectRequest jReq = new JsonObjectRequest(Request.Method.GET , url, params, 
					new Response.Listener<JSONObject>() {
						@Override
						public void onResponse(JSONObject jObj) {
							try {
								if(jObj.getString("success").equals("true") )
								{
							/*		Log.v("teemo","res = "+jObj.toString());
									JSONObject res = jObj.getJSONObject("result");
									String hash = res.getString("hash");
									int id = res.getInt("id");
									
									pfSetting.edit().putInt("userId", id).apply();
									pfSetting.edit().putInt("projectId", prjId).apply();
									pfSetting.edit().putInt("instId", instId).apply();
									pfSetting.edit().putString("hash", hash).apply();*/
									init();
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								Toast.makeText(getBaseContext(), "JSON 에러",Toast.LENGTH_LONG).show();
								e.printStackTrace();
							}			
						}
					}, 
					new Response.ErrorListener() {

						@Override
						public void onErrorResponse(VolleyError vError) {
							Log.v("teemo","Create user ID volley error: "+ vError.toString());
							Toast.makeText(getBaseContext(), "ID 확인에 실패하였습니다.",Toast.LENGTH_LONG).show();
						}
					}
				);
				
		
		queue.add(jReq);
    }
  

    @Override
    public void run() {
        Intent intent = new Intent(this, TestActivity.class);
        startActivity(intent);
        finish();
    }

    private void init() {
        handler.post(this);
    }

    @Override
    public void onClick(View v) {
        //Only R.id.start
      /*  EditText txt = (EditText)findViewById(R.id.userId);

        if (txt.getText().length() == 0) {
            createUserId();
        } else {
            try {
                int userId = Integer.parseInt(txt.getText().toString());
                chkUserId(userId);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "입력값이 잘못되었습니다.", Toast.LENGTH_SHORT).show();
            }
        }*/
    	
    	createUserId();
    	
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Log.v("Internet-Error", error.toString());
        Toast.makeText(this, "인터넷에 연결할 수 없어 정보를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();

        if (pfSetting.contains("userId")) init();
    }

    @Override
    public void onResponse(String response) {
        JSONObject res, body;

        try {
            res = new JSONObject(response);
            
            if (res.has("success") && res.getBoolean("success") ) {
            	
                init();
            } else {
                Toast.makeText(this, "존재하지 않는 환자 아이디거나 새로운 아이디 발급에 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Toast.makeText(this, "서버에 문제가 발생했습니다. 관리자에게 문의 바랍니다.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}