package com.agents;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.ArrayList;

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

public class Synchronizer extends Agent {
	private String login;
	private String password;
	private String platform_URI;
	private String tenantId;
	private String tenantName;
	private Class<?> con;
	private String token;
	private String className;
	private int nbpage = 0;
	private int nbproc = 10;
	Object conn1;
	private long userId;
	private ArrayList<Long> listofPendingTasks;
	private long nbproca;
	private Method getNameMethod1;
	private long nbprocessActif;
	private ArrayList<AID> userID;
	private ArrayList<Long> assigned = new ArrayList<Long>();

	public void setup() {
		Object[] args = getArguments();
		platform_URI = args[0].toString();
		login = args[1].toString();
		password = args[2].toString();
		tenantId = args[3].toString();
		className = args[4].toString();
		nbprocessActif = Long.parseLong(args[5].toString());
		tenantName = args[6].toString();
		// Register
		registerSynchoAgent(tenantName);
		userID = usersId(tenantName, this.getLocalName());
		this.addBehaviour(new synchroBehav());

	}

	public class synchroBehav extends Behaviour {
		MessageTemplate mt;
		int step = 1;
		boolean found = true;

		@Override
		public void action() {
			switch (step) {
			case 1:
				System.out.println(myAgent.getLocalName()+" is Starting its behaviour ...");
				try {
					con = Class.forName(className);
					Object conn = con.newInstance();
					Method getNameMethod = conn.getClass().getMethod("getConnectionManager");
					PoolingHttpClientConnectionManager pool = (PoolingHttpClientConnectionManager) getNameMethod
							.invoke(conn);
					Constructor<?> myConstructor = con.getConstructor(CloseableHttpClient.class, String.class);
					conn1 = myConstructor.newInstance(HttpClients.custom().setConnectionManager(pool).build(),
							platform_URI);
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
				step = 2;
				break;
			case 2:
				MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage mgk = myAgent.receive(mt1);
				Struct struct = new Struct(listofPendingTasks, nbproca);
				try {
					nbproc = 100;
					Class<?>[] paramType = { int.class, int.class, String.class, long.class };
					getNameMethod1 = conn1.getClass().getMethod("retreiveTask", paramType);
					struct = (Struct) getNameMethod1.invoke(conn1, nbpage, nbproc, token, userId);
				} catch (NoSuchMethodException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
				struct.getPendingList().removeAll(assigned);
				assigned = new ArrayList<Long>();
				if (struct.getProccactif() > nbprocessActif) {
					// send to the agents the tasks to be executed
					if (struct.getProccactif() - nbprocessActif >= userID.size()) {
						if (userID.size() <= struct.getPendingList().size()) {
							for (int i = 0; i < userID.size(); i++) {
								ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
								msg.addReceiver(userID.get(i));
								try {
									msg.setContentObject(struct.getPendingList().get(i));
									assigned.add(struct.getPendingList().get(i));
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								send(msg);
							}
						} else {
							for (int i = 0; i < struct.getPendingList().size(); i++) {
								ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
								msg.addReceiver(userID.get(i));
								try {
									msg.setContentObject(struct.getPendingList().get(i));
									assigned.add(struct.getPendingList().get(i));
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								send(msg);
							}
						}
					} else {
						if(struct.getProccactif() - nbprocessActif<=struct.getPendingList().size() && struct.getPendingList().size()>0) {
							System.out.println("the size of pending is "+struct.getPendingList().size());
							for (int i = 0; i < struct.getProccactif() - nbprocessActif; i++) {
								ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
								msg.addReceiver(userID.get(i));
								try {
									msg.setContentObject(struct.getPendingList().get(i));
									assigned.add(struct.getPendingList().get(i));
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								send(msg);
							}
						}
						else {
							for (int i = 0; i < struct.getPendingList().get(i); i++) {
								ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
								msg.addReceiver(userID.get(i));
								try {
									msg.setContentObject(struct.getPendingList().get(i));
									assigned.add(struct.getPendingList().get(i));
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								send(msg);
							}
						}
					}
					found = true;
				}

				else {
					if (found) {
						System.out.println("Threshold  " + nbprocessActif + " within the tenant " + tenantName
								+ " is reached Stop the execution of tasks : System Time Zone --- "
								+ ZonedDateTime.now());
						found = false;
					}
				}
				if (mgk != null) {
					step = 3;
					System.out.println("A message telling me to STOP is recived !");
				} else {
					step = 2;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				break;
			case 3:
				mt = MessageTemplate.MatchAll();

				ACLMessage mg11 = myAgent.receive(mt);
				if (mg11 != null) {
					if (mg11.getPerformative() == ACLMessage.INFORM) {
						step = 1;
						platform_URI = mg11.getContent();
						System.out.println("A message telling me to start again is received: " + myAgent.getLocalName()
								+ " change to this BPMS URL " + platform_URI);
					}
				} else {
					block();
				}
				break;
			}
			// TODO Auto-generated method stub

		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return false;
		}
	}

	private ArrayList<AID> usersId(String tenantName, String name) {
		ArrayList<AID> useragents = new ArrayList<AID>();
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("User" + tenantName);
		template.addServices(sd);
		boolean found = false;
		try {
			do {

				DFAgentDescription[] resultList = DFService.search(this, template);
				if (resultList != null && resultList.length > 0) {
					for (int i = 0; i < resultList.length; i++) {
						if (!resultList[i].getName().getLocalName().equals(name)) {
							useragents.add(resultList[i].getName());
						}
					}
					found = true;
				} // System.out.println("not found yet");
			} while (!found);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		// System.out.println("L'id de l'agent synchro est " + agentsynchro);
		return useragents;
	}

	// Register synchronizer
	// Register the Technical Agent within the DF: Directory Facilitator
	private void registerSynchoAgent(String tenantName) {
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
