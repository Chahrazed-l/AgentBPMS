package com.agents;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.acl.Acl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.bpms.Struct;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.ControllerException;

public class UserAgent extends Agent {

	private String login;
	private String password;
	private String platform_URI;
	private String tenantId;
	private String tenantName;
	private Class<?> con;
	private String token;
	private String className;
	private int nbpage = 0;
	private int nbproc = 100;
	Object conn1;
	private long userId;
	private ArrayList<Long> listofPendingTasks;
	private long nbproca;
	private Method getNameMethod1;
	private long nbprocessActif;
	private String lock="lock";
	private int procexec = 0;

	// Initilize the agent
	public void setup() {
		Object[] args = getArguments();
		platform_URI = args[0].toString();
		login = args[1].toString();
		password = args[2].toString();
		tenantId = args[3].toString();
		className = args[4].toString();
		nbprocessActif = Long.parseLong(args[5].toString());
		tenantName = args[6].toString();
		registerUserAgent(tenantName);
		this.addBehaviour(new UserBehavior());
	}

	// A cyclic behavior
	public class UserBehavior extends Behaviour {
		int step = 1;
		MessageTemplate mt;
		long nbtaskact = 0;

		@Override
		public void action() {
			switch (step) {
			case 1:
				try {
					con = Class.forName(className);
					Object conn = con.newInstance();
					Method getNameMethod = conn.getClass().getMethod("getConnectionManager");
					PoolingHttpClientConnectionManager pool = (PoolingHttpClientConnectionManager) getNameMethod
							.invoke(conn);
					Constructor<?> myConstructor = con.getConstructor(CloseableHttpClient.class, String.class);
					conn1 = myConstructor.newInstance(HttpClients.custom().setConnectionManager(pool).build(),
							platform_URI);
					step = 2;

				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;

			case 2:
				// Connect to the portal
				Class<?>[] paramTypes = { String.class, String.class, String.class };
				try {
					getNameMethod1 = conn1.getClass().getMethod("doLogin", paramTypes);
					token = (String) getNameMethod1.invoke(conn1, login, password, tenantId);
					Class<?>[] paramTypess = { String.class, String.class };
					getNameMethod1 = conn1.getClass().getMethod("getactorID", paramTypess);
					userId = (Long) getNameMethod1.invoke(conn1, token, login);
				} catch (NoSuchMethodException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				} catch (SecurityException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Struct struct = new Struct(listofPendingTasks, nbproca);
				//synchronized Method
				try {
					execBehav(getNameMethod1, conn1, struct, nbprocessActif);
				} catch (NoSuchMethodException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SecurityException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalAccessException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IllegalArgumentException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (InvocationTargetException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
		
				if (procexec > 0 && (procexec % 10) == 0) {
					try {
						System.out.println("The user Agent " + myAgent.getLocalName() + " within the tenant "
								+ myAgent.getContainerController().getContainerName() + " achieved " + procexec);
					} catch (ControllerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				step = 3;
				break;

			case 3:
				mt = MessageTemplate.MatchAll();
				ACLMessage mg = myAgent.receive(mt);
				if (mg != null) {
					if (mg.getPerformative() == ACLMessage.REQUEST) {
						System.out.println("A message telling me to stop is receveid: " + myAgent.getLocalName());
						step = 4;
					}
				} else {
					step = 2;
				}
				break;
			case 4:
				mt = MessageTemplate.MatchAll();
				ACLMessage mg11 = myAgent.receive(mt);
				if (mg11 != null) {
					if (mg11.getPerformative() == ACLMessage.INFORM) {
						step = 1;
						platform_URI = mg11.getContent();
						System.out.println("A message telling me to start again is receveid: " + myAgent.getLocalName()
								+ " change to this BPMS URL " + platform_URI);
					}
				} else {
					block();
				}
				break;
			}
		}

		@Override
		public boolean done() {
			return step == -1;
		}

	}

	private AID userag(String tenantName) {
		AID useragent = null;
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("User" + tenantName + "M");
		template.addServices(sd);
		boolean found = false;
		try {
			do {

				DFAgentDescription[] resultList = DFService.search(this, template);
				if (resultList != null && resultList.length > 0) {
					useragent = resultList[0].getName();
					found = true;
				} // System.out.println("not found yet");
			} while (!found);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// System.out.println("L'id de l'agent synchro est " + agentsynchro);
		return useragent;
	}
	//
	public void execBehav(Method method, Object obj, Struct struct, long nb) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		synchronized (lock) {
			//retrieve Task 
			Class<?>[] paramType = { int.class, int.class, String.class, long.class};
			getNameMethod1 = conn1.getClass().getMethod("retreiveTask", paramType);
			struct = (Struct) getNameMethod1.invoke(conn1, nbpage, nbproc, token, userId);
			int k =struct.getPendingList().size();
			if(struct.getProccactif()>nb) {
			Random r = new Random();
			int p = r.nextInt(k);
			//Assign task
			Class<?>[] paramTypess = { long.class, long.class, String.class };
			getNameMethod1 = conn1.getClass().getMethod("autoAssign", paramTypess);
			getNameMethod1.invoke(conn1, struct.getPendingList().get(p), userId, token);
			//Execute Task
			Class<?>[] paramTyp = { long.class, long.class, String.class };
			getNameMethod1 = conn1.getClass().getMethod("executeTask", paramTyp);
			getNameMethod1.invoke(conn1, struct.getPendingList().get(p), userId, token);
			procexec=procexec+1;
			}
			else {
				System.out.println("Reached the threshold "+nb);
			}
		}
	}
	// Register the Technical Agent within the DF: Directory Facilitator
	private void registerUserAgent(String tenantName) {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(this.getLocalName());
		sd.setType("User" + tenantName);
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

}
