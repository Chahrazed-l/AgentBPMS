package com.agents;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

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
	Object conn1;
	private long userId;
	private int nbprocexec=0;

	private Method getNameMethod1;

	// Initilize the agent
	public void setup() {
		Object[] args = getArguments();
		platform_URI = args[0].toString();
		login = args[1].toString();
		password = args[2].toString();
		tenantId = args[3].toString();
		className = args[4].toString();
		tenantName = args[5].toString();
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
				break;

			case 2:
				ACLMessage mgk = myAgent.receive(mt);
				if (mgk != null) {
					if (mgk.getPerformative() == ACLMessage.REQUEST_WHEN) {
						try {
							// Auto Assign
							long taskid = (Long) mgk.getContentObject();
							Class<?>[] paramTypes1 = { long.class, String.class };
							getNameMethod1 = conn1.getClass().getMethod("getTaskInfo", paramTypes1);
							if((Boolean) getNameMethod1.invoke(conn1, taskid, token)==false) {
								Class<?>[] paramTypess = { long.class, long.class, String.class };
								getNameMethod1 = conn1.getClass().getMethod("autoAssign", paramTypess);
								getNameMethod1.invoke(conn1, taskid, userId, token);
								// Execute
								Class<?>[] paramTyp = { long.class, long.class, String.class };
								getNameMethod1 = conn1.getClass().getMethod("executeTask", paramTyp);
								getNameMethod1.invoke(conn1, taskid, userId, token);
								nbprocexec=nbprocexec+1;
							}
							else {
								System.out.println("The task Id is taken "+taskid);
							}
						} catch (UnreadableException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
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
						if (nbprocexec> 0 && (nbprocexec % 50) == 0) {
							
								try {
									System.out.println("The user Agent " + myAgent.getLocalName() + " within the tenant "
											+ myAgent.getContainerController().getContainerName() + " achieved " + nbprocexec);
								} catch (ControllerException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
						}
						step=2;
					} else 
					if (mgk.getPerformative() == ACLMessage.REQUEST) {
						
						System.out.println("A message telling me to stop is received: " + myAgent.getLocalName());
						step = 3;

					}
				}
				else {
					block();
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
		}

		@Override
		public boolean done() {
			return step == -1;
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
