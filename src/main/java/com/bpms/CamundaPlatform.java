package com.bpms;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class CamundaPlatform extends Bpms {
	protected String PlatformURL="http://localhost:8080/engine-rest";

	@Override
	public String doLogin(String username, String password, String tenantId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Struct retreiveTask(int numberPag, int numberproc, String token, long userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void autoAssign(long taskId, long userId, String token) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void executeTask(long activityId, long userId, String token) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dologout(String token) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getTaskInfo(long activityId, String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PoolingHttpClientConnectionManager getConnectionManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HttpResponse executePostRequest(String apiUri, String payloadAsString, String tokenCSRF) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int executePostRequest(String uri, UrlEncodedFormEntity entity) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getCookieValue(CookieStore cookieStore, String cookieName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int ensureStatusOk(HttpResponse response) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public HttpResponse executeGetRequest(String apiURI, String tokencsrf) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getactorID(String tokencsrf, String userName) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public HttpResponse executePutRequest(String apiURI, String payloadString, String tokenCSRF) {
		// TODO Auto-generated method stub
		return null;
	}

}
