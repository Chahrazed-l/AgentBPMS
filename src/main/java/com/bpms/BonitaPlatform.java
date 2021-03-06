package com.bpms;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.DateUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class BonitaPlatform extends Bpms {
	protected String loginUrl = "/loginservice";
	protected String logoutUrl = "/logoutservice";
	protected String userUrl = "/API/identity/user?f=userName=";
	protected CloseableHttpClient httpClient;
	protected HttpContext httpContext;
	protected String bonitaURI;
	protected String urlTaskRetrive = "/API/bpm/humanTask?p=0&c=100&f=state%3dready&f=user_id%3d";
	protected String urlTask = "/API/bpm/humanTask/";
	protected String urlstateTask = "/API/bpm/activity/";

	public BonitaPlatform() {
		// TODO Auto-generated constructor stub

	}

	public BonitaPlatform(CloseableHttpClient client, String platformURI) {
		this.httpClient = client;
		this.bonitaURI = platformURI;
	}

	@Override
	public String doLogin(String username, String password, String tenantId) {
		// TODO Auto-generated method stub
		try {
			CookieStore cookieStore = new BasicCookieStore();
			httpContext = new BasicHttpContext();
			httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			urlParameters.add(new BasicNameValuePair("username", username));
			urlParameters.add(new BasicNameValuePair("password", password));
			urlParameters.add(new BasicNameValuePair("tenant", tenantId));
			urlParameters.add(new BasicNameValuePair("redirect", "false"));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(urlParameters, "utf-8");
			executePostRequest(loginUrl, entity);
			return getCookieValue(cookieStore, "X-Bonita-API-Token");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}

	}
	
	@Override
	public Struct retreiveTask(int numberPag, int numberproc, String token, long userId) {
		// TODO Auto-generated method stu
		ArrayList<Long> listOfPendingTasks = new ArrayList<Long>();
		ArrayList<String> listOfPendingcase = new ArrayList<String>();
		ArrayList<Timestamp> dateready = new ArrayList<Timestamp>();
		long nbprocactif = 0;
		Struct struct = new Struct(listOfPendingTasks,listOfPendingcase, nbprocactif);
		HttpResponse response = executeGetRequest(
				"/API/bpm/humanTask?p=0&c=" + numberproc + "&f=state%3dready&f=user_id%3d" + userId, token);
		// ensureStatusOk(response,"retreiveTask" );
		String Data;
		try {
			Data = EntityUtils.toString(response.getEntity());
			// System.out.println("The value of the data!!"+Data);
			JSONArray array = (JSONArray) new JSONParser().parse(Data);
			// System.out.println("The size of the array !!"+array.size() );
			for (int i = 0; i < array.size(); i++) {
				JSONObject json = null;
				json = (JSONObject) array.get(i);
				String id = (String) json.get("id");
				long id1 = Long.valueOf(Long.parseLong(id, 10));
				String caseId= (String) json.get("parentCaseId");
				//Timestamp dateread= Timestamp.valueOf((String) json.get("reached_state_date"));
				//System.out.println("The id of the assigned Task is " + id1+" caseId "+caseId);
				if (!getTaskInfo(id1, token)) {
					listOfPendingTasks.add(id1);
					listOfPendingcase.add(caseId);
					//dateready.add(dateread);
				}
				// System.out.println("The id of the assigned Task is " + id);
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		nbprocactif = getInstanceLength(response);
		struct = new Struct(listOfPendingTasks,listOfPendingcase, nbprocactif);
		return struct;

	}

	public static long getInstanceLength(HttpResponse response) {
		long length = -1;
		if (response != null) {
			Header[] range = response.getHeaders("Content-Range");
			if (range.length > 0) {
				// Get the header value
				String value = range[0].getValue();

				// Split the value
				String[] section = value.split("/");

				try {
					// Parse for the instance length
					length = Long.parseLong(section[1]);
				} catch (NumberFormatException e) {
					// The server returned an unknown "*" or invalid instance-length
				}
			}
		}
		return length;
	}

	@Override
	public void autoAssign(long taskId, long userId, String token) {
		// TODO Auto-generated method stub
		String payloadAsString = "{\"assigned_id\":\"" + userId + "\"}";
		HttpResponse response = executePutRequest(urlTask + taskId, payloadAsString, token);
		// ensureStatusOk(response, "autoAssign");
	}

	@Override
	public void executeTask(long activityId, long userId, String token) {
		String payloadAsString = "{\"state\":\"completed\"}";
		HttpResponse response = executePutRequest(urlstateTask + activityId, payloadAsString, token);
		// ensureStatusOk(response, "executeTask");

	}

	@Override
	public String getCookieValue(CookieStore cookieStore, String cookieName) {
		// TODO Auto-generated method stub
		String value = null;
		for (Cookie cookie : cookieStore.getCookies()) {
			if (cookie.getName().equals(cookieName)) {
				value = cookie.getValue();
				break;
			}
		}
		return value;
	}

	@Override
	public int ensureStatusOk(HttpResponse response) {
		// TODO Auto-generated method stub
		if (response.getStatusLine().getStatusCode() != 201 && response.getStatusLine().getStatusCode() != 200) {
			System.out.println("Failed: Http error code : " + response.getStatusLine().getStatusCode() + " : "
					+ response.getStatusLine().getReasonPhrase() + " in the method ");
		}
		return response.getStatusLine().getStatusCode();
	}

	@Override
	public HttpResponse executeGetRequest(String apiURI, String tokencsrf) {
		// TODO Auto-generated method stub
		try {
			HttpGet getrequest = new HttpGet(bonitaURI + apiURI);
			getrequest.addHeader("X-Bonita-API-Token", tokencsrf);
			HttpResponse response = httpClient.execute(getrequest, httpContext);
			// ensureStatusOk(response, "executeGetRequest");
			return response;

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	@Override
	public long getactorID(String tokencsrf, String userName) {
		// TODO Auto-generated method stub
		long id1 = 0;
		HttpResponse response = executeGetRequest(userUrl + userName, tokencsrf);
		String actorJson;
		JSONArray array = null;
		try {
			actorJson = EntityUtils.toString(response.getEntity());
			array = (JSONArray) new JSONParser().parse(actorJson);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (org.json.simple.parser.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// System.out.println("The size of the array !!"+array.size() );
		for (int i = 0; i < array.size(); i++) {
			JSONObject json = null;
			json = (JSONObject) array.get(i);
			String id = (String) json.get("id");
			id1 = Long.valueOf(Long.parseLong(id, 10));
		}
		return id1;
	}

	@Override
	public HttpResponse executePutRequest(String apiURI, String payloadString, String tokenCSRF) {
		// TODO Auto-generated method stub
		HttpPut putRequest = new HttpPut(bonitaURI + apiURI);
		putRequest.addHeader("Accept", "application/json");
		putRequest.setHeader("Content-Type", "application/json");
		putRequest.addHeader("X-Bonita-API-Token", tokenCSRF);
		StringEntity input;
		HttpResponse response = null;
		try {
			input = new StringEntity(payloadString);
			putRequest.setEntity(input);
			response = httpClient.execute(putRequest, httpContext);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
			System.out.println("error");
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// ensureStatusOk(response,"executePutRequest");
		return response;

	}

	@Override
	public void dologout(String token) {
		// TODO Auto-generated method stub
		HttpResponse response = executeGetRequest(logoutUrl, token);
		ensureStatusOk(response);

	}

	@Override
	public HttpResponse executePostRequest(String apiUri, String payloadAsString, String tokenCSRF) {
		// TODO Auto-generated method stub
		try {
			HttpPost postRequest = new HttpPost(bonitaURI + apiUri);
			StringEntity input = new StringEntity(payloadAsString);
			postRequest.setEntity(input);
			postRequest.addHeader("Accept", "application/json");
			postRequest.setHeader("Content-Type", "application/json");
			postRequest.addHeader("X-Bonita-API-Token", tokenCSRF);
			HttpResponse response = httpClient.execute(postRequest, httpContext);
			// ensureStatusOk(response, "executePostRequest");
			return response;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new RuntimeException();
		}

	}

	@Override
	public void executePostRequest(String uri, UrlEncodedFormEntity entity) {
		// TODO Auto-generated method stub
		HttpPost postRequest = new HttpPost(bonitaURI + uri);
		postRequest.setEntity(entity);
		HttpResponse response = null;
		try {
			response = httpClient.execute(postRequest, httpContext);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//return ensureStatusOk(response);
	}

	@Override
	public  PoolingHttpClientConnectionManager getConnectionManager() {
		{
			// TODO Auto-generated method stub
			PoolingHttpClientConnectionManager conMan = new PoolingHttpClientConnectionManager();
			conMan.setMaxTotal(10000);
			conMan.setDefaultMaxPerRoute(10000);
			return conMan;
		}
	}

	@Override
	public boolean getTaskInfo(long activityId, String token) {
		// TODO Auto-generated method stubString assignid = "";
		String assignid = "";
		String actorJson = null;
		boolean assign = false;
		HttpResponse response = executeGetRequest("/API/bpm/humanTask/" + activityId, token);
		if (response.getStatusLine().getStatusCode() == 200) {
			try {
				actorJson = EntityUtils.toString(response.getEntity());
				JSONParser parser = new JSONParser();
				Object jsonObj = parser.parse(actorJson);
				JSONObject jsonObject = (JSONObject) jsonObj;
				assignid = (String) jsonObject.get("assigned_id");
				assignid = assignid.trim();
				
				// id1= Long.parseLong(assignid);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (org.json.simple.parser.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (assignid.equals("")) {
			assign = false;
		} else {
			assign = true;
		}
		//System.out.println(activityId+" " +assign+"  "+assignid);
		return assign;

	}
}
