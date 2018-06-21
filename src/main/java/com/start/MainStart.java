package com.start;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.bpms.BonitaPlatform;
import com.bpms.Struct;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

public class MainStart {
	// container's name and reference
	private static HashMap<String, ContainerController> containerList = new HashMap<String, ContainerController>();
	// private static List<AgentController> agentList;// agents's ref
	private static Runtime rt;
	public static Object lock = new Object();
	private static ArrayList<String> tenantList = new ArrayList<String>();
	private static ArrayList<ContainerController> containers = new ArrayList<ContainerController>();
	private static Struct tenantcontainer = new Struct(tenantList, containers);
	protected CloseableHttpClient httpClient;
	protected HttpContext httpContext;
	public static String uri;
	public static String username = "platformAdmin";
	public static String password = "platform";
	public static PoolingHttpClientConnectionManager conMan;
	public static MainStart con;
	public static String token;

	public MainStart() {

	}

	public MainStart(CloseableHttpClient client, String platformURI) {
		this.httpClient = client;
		this.uri = platformURI;
	}

	public static void main(String[] args) throws IOException {
		String bpms_name = args[0].toString();
		uri = args[1].toString();
		String filename = args[2].toString();
		rt = emptyPlatform(containerList);
		String className = classNameToinstantiate(bpms_name);
		boolean available = false;
		int i = 1;
		while (!available) {
			int status = getStatus(uri, i);
			if (status == 200) {
				available = true;
			}
			i++;
		}
		System.out.println("The Name of the BPMS is " + bpms_name);

		boolean ok = false;
		while (!ok) {
			conMan = MainStart.getConnectionManager();
			con = new MainStart(HttpClients.custom().setConnectionManager(conMan).build(), MainStart.uri);
			token = con.doLoginPlatform(username, password);
			if (token != null) {
				System.out.println("Platform Admin is In");
				ok = true;
			} else {
				conMan.close();
			}
		}
		createSecondContainers(rt, uri, bpms_name, filename, className, token);

	}

	private static Runtime emptyPlatform(HashMap<String, ContainerController> containerList) {

		Runtime rt = Runtime.instance();

		// 1) create a platform (main container+DF+AMS)
		Profile pMain = new ProfileImpl(null, 1090, null);
		System.out.println("Launching a main-container..." + pMain);
		AgentContainer mainContainerRef = rt.createMainContainer(pMain); // DF and AMS are included
		// createMonitoringAgents(mainContainerRef);
		System.out.println("Plaform ok");
		return rt;
	}

	private static String classNameToinstantiate(String bmpsName) {
		String className;
		if (bmpsName.equals("bonita")) {
			className = "BonitaPlatform";
			return className;
		} else if (bmpsName.equals("camunda")) {
			System.out.println("Not yet supported");
			return null;
		} else {
			System.out.println("Not yet supported");
			return null;
		}

	}

	private static void createMonitoringAgents(ContainerController mc) {

		System.out.println("Launching the rma agent on the main container ...");
		AgentController rma;

		try {
			rma = mc.createNewAgent("rma", "jade.tools.rma.rma", new Object[0]);
			rma.start();
		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.out.println("Launching of rma agent failed");
		}

		System.out.println("Launching  Sniffer agent on the main container...");
		AgentController snif = null;

		try {
			snif = mc.createNewAgent("sniffeur", "jade.tools.sniffer.Sniffer", new Object[0]);
			snif.start();

		} catch (StaleProxyException e) {
			e.printStackTrace();
			System.out.println("launching of sniffer agent failed");

		}

	}

	private static void createSecondContainers(Runtime runtime, String uRi, String bmps_name, String filename,
			String className, String token) {
		// Read from file information about the tenants and the agents number
		FileReader fileReader = null;
		String tenantId = null;
		try {
			fileReader = new FileReader(filename);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		BufferedReader bufferedReader = new BufferedReader(fileReader);
		Scanner s = new Scanner(bufferedReader);
		while (s.hasNext("\r|\n"))
			s.next("\r|\n");
		while (s.findInLine("(.*);(.*);(.*);([0-9]{1,4});([0-9]{1,9})") != null) {
			MatchResult match = s.match();
			String tenantName = match.group(1);
			String userName = match.group(2);
			String password = match.group(3);
			Integer userNumber = Integer.parseInt(match.group(4));
			Long nbprocessActif = Long.parseLong(match.group(5));
			boolean found = false;
			while (!found) {
				tenantId = con.GetTenantId(token, tenantName);
				if (tenantId != null) {
					
					found = true;
				}

			}
			// tester si le tenant deja existe ou pas
			if (!checkTenant(tenantName, tenantcontainer)) {
				// not found : create another second container with the tenant Name
				Profile profile = new ProfileImpl();
				profile.setParameter(Profile.CONTAINER_NAME, tenantName);
				profile.setParameter(Profile.MAIN_HOST, "localhost");
				ContainerController container = null;
				container = runtime.createAgentContainer(profile);
				tenantcontainer.getTenantList().add(tenantName);
				System.out.println("Not found " + container);
				tenantcontainer.getContainers().add(container);
				try {

					AgentController ag = container.createNewAgent("logger",
							"com.agents.LogGenerator", new Object[] {tenantName});// arguments
					ag.start();
				} catch (StaleProxyException e) {
					e.printStackTrace();
				}
				for (int i = 1; i <= userNumber; i++) {
					try {

						AgentController ag = container.createNewAgent(userName + i + tenantName, "com.agents.UserAgent",
								new Object[] { uRi, userName, password, tenantId, "com.bpms." + className,
										tenantName });// arguments
						ag.start();
						System.out
								.println("The agent " + userName + i + tenantName + " is created within " + tenantName);
					} catch (StaleProxyException e) {
						e.printStackTrace();
					}

				}
				try {

					AgentController ag = container.createNewAgent(userName + "synchro" + tenantName,
							"com.agents.Synchronizer", new Object[] { uRi, userName, password, tenantId,
									"com.bpms." + className, nbprocessActif, tenantName });// arguments
					ag.start();
					System.out.println("The agent " + userName + "synchro" + tenantName
							+ " is created succefully within " + tenantName);
				} catch (StaleProxyException e) {
					e.printStackTrace();
				}

			} else {
				// Add the agents to the existing container having the name of the Tenant
				ContainerController containerController = container(tenantcontainer, tenantName);

				for (int i = 1; i <= userNumber; i++) {
					try {
						AgentController ag = containerController.createNewAgent(userName + i + tenantName,
								"com.agents.UserAgent", new Object[] { uRi, userName, password, tenantId,
										"com.bpms." + className, tenantName });// arguments
						ag.start();
						System.out.println(
								"The agent " + userName + i + tenantName + " is created within tenant" + tenantName);

					} catch (StaleProxyException e) {
						e.printStackTrace();
					}

				}
				try {

					AgentController ag = containerController.createNewAgent(userName + "synchro" + tenantName,
							"com.agents.Synchronizer", new Object[] { uRi, userName, password, tenantId,
									"com.bpms." + className, nbprocessActif, tenantName });// arguments
					ag.start();
					System.out.println("The agent " + userName + "synchro" + tenantName
							+ " is created succefully within tenant " + tenantName);
				} catch (StaleProxyException e) {
					e.printStackTrace();
				}

			}

			if (s.hasNextLine())
				s.nextLine();
		}
		try {
			bufferedReader.close();
			conMan.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	// Return the container with a given Name

	private static ContainerController container(Struct list, String tenantName) {

		ContainerController c = null;
		boolean found = false;
		int i = 0;

		while (!found && i < list.getTenantList().size()) {
			if (list.getTenantList().get(i).equals(tenantName)) {
				c = list.getContainers().get(i);
				found = true;
			} else {
				i++;
			}

		}

		return c;
	}

	// check if the Tenant container already exits or not
	private static boolean checkTenant(String tenant, Struct list) {
		int i = 0;
		boolean found = false;
		while (!found && i < list.getTenantList().size()) {

			if (list.getTenantList().get(i).equals(tenant)) {
				found = true;
			} else {
				i++;
			}

		}
		return found;
	}

	// Se connecter en tant que plqtform Admin to get tenant ID
	public String GetTenantId(String token, String tenantName) {
		String id = null;
		String tenantUrl = "/API/platform/tenant?f=name=";
		HttpResponse response = executeGetRequest(tenantUrl + tenantName, token);
		String actorJson = null;
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
			id = (String) json.get("id");
		}
		//System.out.println("The id of the tenant is " + id);
		if(id!=null) {
			System.out.println(actorJson);
		}
		return id;
	}

	public static PoolingHttpClientConnectionManager getConnectionManager() {
		{
			// TODO Auto-generated method stub
			PoolingHttpClientConnectionManager conMan = new PoolingHttpClientConnectionManager();
			conMan.setMaxTotal(1000);
			conMan.setDefaultMaxPerRoute(1000);
			return conMan;
		}
	}

	public HttpResponse executeGetRequest(String apiURI, String tokencsrf) {
		// TODO Auto-generated method stub
		try {
			HttpGet getrequest = new HttpGet(uri + apiURI);
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

	public String doLoginPlatform(String username, String password) {
		String loginUrl = "/platformloginservice";
		try {
			CookieStore cookieStore = new BasicCookieStore();
			httpContext = new BasicHttpContext();
			httpContext.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
			List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
			urlParameters.add(new BasicNameValuePair("username", username));
			urlParameters.add(new BasicNameValuePair("password", password));
			urlParameters.add(new BasicNameValuePair("redirect", "false"));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(urlParameters, "utf-8");
			executePostRequest(loginUrl, entity);
			return getCookieValue(cookieStore, "X-Bonita-API-Token");
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

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

	public void executePostRequest(String url, UrlEncodedFormEntity entity) {
		// TODO Auto-generated method stub
		HttpPost postRequest = new HttpPost(uri + url);
		postRequest.setEntity(entity);
		HttpResponse response = null;
		try {
			response = httpClient.execute(postRequest, httpContext);
			if (response.getStatusLine().getStatusCode() != 201 && response.getStatusLine().getStatusCode() != 200) {
				System.out.println("Platform ADMIN logged IN Bonita");
			}
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static int getStatus(String url, int i) throws IOException {

		int code = 0;
		try {
			URL siteURL = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
			connection.setRequestMethod("GET");
			connection.connect();
			code = connection.getResponseCode();
			if (code == 200) {
				System.out.println("-> THE BPMS IS READY <- ");
			}
		} catch (Exception e) {
			if (i == 1) {
				System.out.println("-> THE BPMS IS NOT AVAILABLE TRY TO RECONNECT AGAIN ... <- ");
			}
		}
		return code;
	}

}
