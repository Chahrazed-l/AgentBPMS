package com.start;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;
import java.util.regex.MatchResult;

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

	public static void main(String[] args) {
		String bpms_name = args[0].toString();
		String uri = args[1].toString();
		String filename = args[2].toString();
		long NbprocessActif = Long.parseLong(args[3].toString());
		//long actiTime = Long.parseLong(args[4].toString());
		rt = emptyPlatform(containerList);
		String className = classNameToinstantiate(bpms_name);
		createSecondContainers(rt, uri, bpms_name, filename, className, NbprocessActif);

	}

	private static Runtime emptyPlatform(HashMap<String, ContainerController> containerList) {

		Runtime rt = Runtime.instance();

		// 1) create a platform (main container+DF+AMS)
		Profile pMain = new ProfileImpl(null, 8888, null);
		System.out.println("Launching a main-container..." + pMain);
		AgentContainer mainContainerRef = rt.createMainContainer(pMain); // DF and AMS are included
		System.out.println("Plaform ok");
		return rt;

	}

	private static String classNameToinstantiate(String bmpsName) {
		String className;
		if (bmpsName.equals("bonita")) {
			className = "BonitaPlatform";
			return className;
		} else if (bmpsName.equals("camunda")) {
			return null;
		} else {
			return null;
		}

	}

		
	private static void createSecondContainers(Runtime runtime, String uRi, String bmps_name, String filename,
			String className, long nbprocessActif) {
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
		while (s.findInLine("([A-Z][0-9]{1,4});([0-9]{1,4});([0-9]{1,4})") != null) {
			MatchResult match = s.match();
			String tenantName = match.group(1);
			String tenantId = match.group(2);
			Integer userNumber = Integer.parseInt(match.group(3));
			Profile profile = new ProfileImpl();
			profile.setParameter(Profile.CONTAINER_NAME, tenantName);
			profile.setParameter(Profile.MAIN_HOST, "localhost");
			// create a non-main agent container
			System.out.println("Launchig the user Agents of Tenant "+tenantName);
			ContainerController container = runtime.createAgentContainer(profile);
			for (int i = 1; i <= userNumber; i++) {
				try {
					AgentController ag = container.createNewAgent("user" + i+tenantName, "com.agents.UserAgent",
							new Object[] { uRi, "user" + i, "user" + i, tenantId, "com.bpms." + className, nbprocessActif});// arguments
					ag.start();

				} catch (StaleProxyException e) {
					e.printStackTrace();
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

}
