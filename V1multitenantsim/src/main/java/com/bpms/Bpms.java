package com.bpms;

import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public abstract class Bpms {
	public abstract String doLogin(String username, String password, String tenantId);
	// Retrieve the tasks 
	public abstract Struct retreiveTask(int numberPag, int numberproc, String token, long userId);
	// Auto assign the tasks
	public abstract void autoAssign(long taskId, long userId, String token);
	//Execute the Task
	public abstract void executeTask(long activityId,long userId, String token);
	//logout the platform
	public abstract void dologout(String token);
	//Create a closable Http Client
	public abstract String getTaskInfo(long activityId,String token);
	public abstract PoolingHttpClientConnectionManager getConnectionManager() ;
	// Execute a Post Request
	public abstract HttpResponse executePostRequest(String apiUri, String payloadAsString, String tokenCSRF);
	public abstract int executePostRequest(String uri, UrlEncodedFormEntity entity);
	// Get the cookie values from the response
	public abstract String getCookieValue(CookieStore cookieStore, String cookieName);
	// check the response value: OK or Not 
	public abstract int ensureStatusOk(HttpResponse response);
	//Execute a get request 
	public abstract HttpResponse executeGetRequest(String apiURI, String tokencsrf);
	// Get the actor Id 
	public abstract long getactorID(String tokencsrf, String userName);
	//Execute Put Request 
	public abstract HttpResponse executePutRequest(String apiURI, String payloadString, String tokenCSRF);
}
