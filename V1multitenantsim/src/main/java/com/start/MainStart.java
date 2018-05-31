package com.start;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.MatchResult;

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
	private static ArrayList<String> tenantList = new ArrayList<String>();
	private static ArrayList<ContainerController> containers = new ArrayList<ContainerController>();
	private static Struct tenantcontainer = new Struct(tenantList, containers);

	public static void main(String[] args) {
		String bpms_name = args[0].toString();
		String uri = args[1].toString();
		String filename = args[2].toString();
		rt = emptyPlatform(containerList);
		String className = classNameToinstantiate(bpms_name);
		createSecondContainers(rt, uri, bpms_name, filename, className);

	}

	private static Runtime emptyPlatform(HashMap<String, ContainerController> containerList) {

		Runtime rt = Runtime.instance();

		// 1) create a platform (main container+DF+AMS)
		Profile pMain = new ProfileImpl(null, 1090, null);
		System.out.println("Launching a main-container..." + pMain);
		AgentContainer mainContainerRef = rt.createMainContainer(pMain); // DF and AMS are included
		//createMonitoringAgents(mainContainerRef);
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
			String className) {
		// Read from file information about the tenants and the agents number
		FileReader fileReader = null;
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
		while (s.findInLine("([A-Z][0-9]{1,4});([0-9]{1,4});(.*);(.*);([0-9]{1,4});([0-9]{1,9})") != null) {
			MatchResult match = s.match();
			String tenantName = match.group(1);
			String tenantId = match.group(2);
			String userName = match.group(3);
			String password = match.group(4);
			Integer userNumber = Integer.parseInt(match.group(5));
			Long nbprocessActif = Long.parseLong(match.group(6));
			// tester si le tenant deja existe ou pas
			if (!checkTenant(tenantName, tenantcontainer)) {
				// not found : create another second container with the tenant Name
				Profile profile = new ProfileImpl();
				profile.setParameter(Profile.CONTAINER_NAME, tenantName);
				profile.setParameter(Profile.MAIN_HOST, "localhost");
				// create a non-main agent container
				System.out.println(
						"Launchig the user Agents of Tenant " + tenantName + " number of users is " + userNumber);
				ContainerController container = null;
				container = runtime.createAgentContainer(profile);
				tenantcontainer.getTenantList().add(tenantName);
				System.out.println("Not found " + container);
				tenantcontainer.getContainers().add(container);
				for (int i = 1; i <= userNumber; i++) {
					try {

						AgentController ag = container.createNewAgent(userName + i + tenantName, "com.agents.UserAgent",
								new Object[] { uRi, userName, password, tenantId, "com.bpms." + className,
										nbprocessActif, tenantName });// arguments
						ag.start();

					} catch (StaleProxyException e) {
						e.printStackTrace();
					}

				}

			} else {
				// Add the agents to the existing container having the name of the Tenant
				ContainerController containerController = container(tenantcontainer, tenantName);
				System.out.println(
						"Launchig the user Agents of Tenant " + tenantName + " number of users is " + userNumber);
				for (int i = 1; i <= userNumber; i++) {
					try {
						AgentController ag = containerController.createNewAgent(userName + i + tenantName,
								"com.agents.UserAgent", new Object[] { uRi, userName, password, tenantId,
										"com.bpms." + className, nbprocessActif, tenantName });// arguments
						ag.start();

					} catch (StaleProxyException e) {
						e.printStackTrace();
					}

				}

			}

			if (s.hasNextLine())
				s.nextLine();
		}
		try {
			bufferedReader.close();
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

}
