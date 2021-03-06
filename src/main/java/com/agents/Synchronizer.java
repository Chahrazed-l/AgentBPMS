package com.agents;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

import com.bpms.LogObject;
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
	private Timestamp reqtime;
	private Timestamp resptime;
	private int nbpage = 0;
	private int nbproc = 10;
	private long attente;
	Object conn1;
	private long userId;
	private ArrayList<Long> listofPendingTasks;
	private ArrayList<String> listofPendingcase;
	private ArrayList<Timestamp> listofdateready;
	private long nbproca;
	private Method getNameMethod1;
	private long nbprocessActif;
	private ArrayList<AID> userID;
	private ArrayList<Long> assigned = new ArrayList<Long>();
	private ArrayList<String> cases = new ArrayList<String>();

	public void setup() {
		Object[] args = getArguments();
		platform_URI = args[0].toString();
		login = args[1].toString();
		password = args[2].toString();
		tenantId = args[3].toString();
		className = args[4].toString();
		nbprocessActif = Long.parseLong(args[5].toString());
		tenantName = args[6].toString();
		attente = Long.parseLong(args[7].toString());
		// Register
		registerSynchoAgent(tenantName);
		userID = usersId(tenantName, this.getLocalName());
		System.out.println(this.getLocalName() + " is Starting its behaviour ...");
		this.addBehaviour(new synchroBehav());

	}

	public class synchroBehav extends Behaviour {
		MessageTemplate mt;
		int step = 1;
		boolean found = true;
		PoolingHttpClientConnectionManager pool;

		@Override
		public void action() {
			switch (step) {
			case 1:
				try {
					con = Class.forName(className);
					Object conn = con.newInstance();
					Method getNameMethod = conn.getClass().getMethod("getConnectionManager");
					pool = (PoolingHttpClientConnectionManager) getNameMethod.invoke(conn);
					Constructor<?> myConstructor = con.getConstructor(CloseableHttpClient.class, String.class);
					conn1 = myConstructor.newInstance(HttpClients.custom().setConnectionManager(pool).build(),
							platform_URI);
					Class<?>[] paramTypes = { String.class, String.class, String.class };
					getNameMethod1 = conn1.getClass().getMethod("doLogin", paramTypes);
					token = (String) getNameMethod1.invoke(conn1, login, password, tenantId);
					if (token != null) {
						Class<?>[] paramTypess = { String.class, String.class };
						getNameMethod1 = conn1.getClass().getMethod("getactorID", paramTypess);
						userId = (Long) getNameMethod1.invoke(conn1, token, login);
						step = 2;
					} else {
						pool.close();
						step = 1;
					}
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
				MessageTemplate mt1 = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage mgk = myAgent.receive(mt1);
				Struct struct = new Struct(listofPendingTasks, listofPendingcase, nbproca);
				try {
					nbproc = userID.size() * 2;
					Class<?>[] paramType = { int.class, int.class, String.class, long.class };
					getNameMethod1 = conn1.getClass().getMethod("retreiveTask", paramType);
					reqtime = new Timestamp(System.currentTimeMillis());
					struct = (Struct) getNameMethod1.invoke(conn1, nbpage, nbproc, token, userId);
					resptime = new Timestamp(System.currentTimeMillis());
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
				long nbpr = struct.getProccactif() - assigned.size();
				//System.out.println(nbpr+"   "+struct.getProccactif());
				Iterator<Long> iter = struct.getPendingList().iterator();
				Iterator<String> iter1 = struct.getPendingcaseId().iterator();
				while (iter.hasNext() && iter1.hasNext()) {
					long in = iter.next();
					String in1 = iter1.next();
					if (exist(in, assigned)) {
						iter.remove();
						iter1.remove();
					}
				}
				assigned = new ArrayList<Long>();
				cases = new ArrayList<String>();
				if (nbpr > nbprocessActif) {
					System.out.println("Timestamp:  " + reqtime + " :Request retrieve To Bonita");
					System.out.println("Timestamp:  " + resptime
							+ " :Response retrieve from Bonita Received number of active tasks not assigned is  "
							+ nbpr);			
					// send to the agents the tasks to be executed
					if (nbpr - nbprocessActif >= userID.size()) {
						if (userID.size() <= struct.getPendingList().size()) {
							System.out.println("Timestamp:  " + new Timestamp(System.currentTimeMillis())
									+ " :The size of the case list " + struct.getPendingcaseId().size()
									+ " and  the size of task list " + struct.getPendingList().size()
									+ " --> Number of Active BPM Agents is " + userID.size());
							for (int i = 0; i < userID.size(); i++) {
								ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
								msg.addReceiver(userID.get(i));
								try {
									LogObject obj = new LogObject(struct.getPendingcaseId().get(i),
											struct.getPendingList().get(i), reqtime, resptime, reqtime, resptime,
											reqtime, resptime);
									msg.setContentObject(obj);
									assigned.add(struct.getPendingList().get(i));
									cases.add(struct.getPendingcaseId().get(i));
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								send(msg);
							}
						} else {
							System.out.println("Timestamp:  " + new Timestamp(System.currentTimeMillis())
									+ " :The size of the case list " + struct.getPendingcaseId().size()
									+ " and  the size of task list " + struct.getPendingList().size()
									+ " --> Number of Active BPM Agents is " + struct.getPendingList().size());
							for (int i = 0; i < struct.getPendingList().size(); i++) {
								ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
								msg.addReceiver(userID.get(i));
								try {
									LogObject obj = new LogObject(struct.getPendingcaseId().get(i),
											struct.getPendingList().get(i), reqtime, resptime, reqtime, resptime,
											reqtime, resptime);
									msg.setContentObject(obj);
									assigned.add(struct.getPendingList().get(i));
									cases.add(struct.getPendingcaseId().get(i));

								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								send(msg);
							}
						}
					} else {
						if (nbpr - nbprocessActif >= struct.getPendingList().size()
								&& struct.getPendingList().size() > 0) {
							System.out.println("Timestamp:  " + new Timestamp(System.currentTimeMillis())
									+ " :The size of the case list " + struct.getPendingcaseId().size()
									+ " and  the size of task list " + struct.getPendingList().size()
									+ " --> Number of Active BPM Agents is " + struct.getPendingList().size());

							for (int i = 0; i < struct.getPendingList().size(); i++) {
								ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
								msg.addReceiver(userID.get(i));
								try {

									LogObject obj = new LogObject(struct.getPendingcaseId().get(i),
											struct.getPendingList().get(i), reqtime, resptime, reqtime, resptime,
											reqtime, resptime);
									msg.setContentObject(obj);
									assigned.add(struct.getPendingList().get(i));
									cases.add(struct.getPendingcaseId().get(i));

								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								send(msg);
							}
						} else {
							if (struct.getPendingList().size() > 0 && nbpr - nbprocessActif > 0) {
								System.out.println("Timestamp:  " + new Timestamp(System.currentTimeMillis())
										+ " :The size of the case list " + struct.getPendingcaseId().size()
										+ " and  the size of task list " + struct.getPendingList().size()
										+ " --> Number of Active BPM Agents is  " + (nbpr - nbprocessActif));
								for (int i = 0; i < nbpr - nbprocessActif; i++) {
									ACLMessage msg = new ACLMessage(ACLMessage.REQUEST_WHEN);
									msg.addReceiver(userID.get(i));
									try {

										LogObject obj = new LogObject(struct.getPendingcaseId().get(i),
												struct.getPendingList().get(i), reqtime, resptime, reqtime, resptime,
												reqtime, resptime);
										msg.setContentObject(obj);
										assigned.add(struct.getPendingList().get(i));
										cases.add(struct.getPendingcaseId().get(i));
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									send(msg);
								}
							}
						}
					}
					found = true;
				}

				else {
					if (found) {
						if(struct.getProccactif()==nbprocessActif) {
						System.out.println("Threshold  " + nbprocessActif + " within the tenant " + tenantName
								+ " is reached Stop the execution of tasks : Timestamp ---  "
								+ new Timestamp(System.currentTimeMillis()));
						found = false;
						}
						else if(struct.getProccactif()>nbprocessActif) {
							System.out.println("Too much active Tasks  " + struct.getProccactif() + " within the tenant " + tenantName
									+ "  : Timestamp ---  "
									+ new Timestamp(System.currentTimeMillis()));
							found = false;
						}
						else {
							System.out.println("Too few active Tasks  " + struct.getProccactif() + " within the tenant " + tenantName
									+ "  : Timestamp ---  "
									+ new Timestamp(System.currentTimeMillis()));
							found = false;
						}
					}
				}
				if (mgk != null) {
					step = 3;
					System.out.println("A message telling me to STOP is recived !");
				} else {

					step = 2;
					try {
						Thread.sleep(attente);
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
			return step == -1;
		}
	}
	// Function exist

	public boolean exist(long it, ArrayList<Long> list) {
		boolean found = false;
		int i = 0;
		while (i < list.size() && !found) {
			if (list.get(i) == it) {
				found = true;
			}
			i++;
		}
		return found;
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
