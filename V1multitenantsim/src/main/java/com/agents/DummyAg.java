package com.agents;

import java.util.ArrayList;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

public class DummyAg extends Agent {
	private String messageType;
	private String tenantName;
	private String url;
	private ArrayList<AID> usersID = new ArrayList<AID>();

	public void setup() {
		Object[] args = getArguments();
		registerdummy();
		if (args.length == 2) {
			messageType = args[0].toString();
			tenantName = args[1].toString();
			addBehaviour(new OneShotBehaviour() {
				@Override
				public void action() {
					// TODO Auto-generated method stub
					if (messageType.equals("stop")) {
						usersID = usersId(tenantName);
						ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
						for (int i = 0; i < usersID.size(); i++) {
							msg.addReceiver(usersID.get(i));
						}
						send(msg);
					} else {
						System.out.println("The correct message name is : stop not what u have written");
					}
					
				     kill();
				}
			});
		}
		if (args.length == 3) {
			messageType = args[0].toString();
			tenantName = args[1].toString();
			url = args[2].toString();
			addBehaviour(new OneShotBehaviour() {
				@Override
				public void action() {
					// TODO Auto-generated method stub
					if (messageType.equals("start")) {
						usersID = usersId(tenantName);
						ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
						for (int i = 0; i < usersID.size(); i++) {
							msg.addReceiver(usersID.get(i));
						}
						msg.setContent(url);
						send(msg);
					} else {
						System.out.println("The correct message name is : start not what u have written");
					}
					
                    kill();

				}
			});
		} 
	}

	// Register Agent
	private void registerdummy() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(this.getLocalName());
		sd.setType("Dummy");
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("The Dummy is registred");
	}

	private void deregisterdummy() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(this.getLocalName());
		sd.setType("Dummy");
		dfd.addServices(sd);

		try {
			DFService.deregister(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		System.out.println("The Dummy is deregistred");
	}
    //kill the agent and container 
	private void kill() {
		// Kill the agent as well as the container
				try {
					String localname = dummyId();
					deregisterdummy();
					getContainerController().getAgent(localname).kill();
					getContainerController().kill();
				} catch (StaleProxyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ControllerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}
	
	// Get the ids of the users who need to be started or stoped
	private ArrayList<AID> usersId(String tenantName) {
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
						useragents.add(resultList[i].getName());
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

	private String dummyId() {
		String agentsdummy = null;
		DFAgentDescription template = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setType("Dummy");
		template.addServices(sd);
		boolean found = false;
		try {
			do {
				DFAgentDescription[] resultList = DFService.search(this, template);
				if (resultList != null && resultList.length > 0) {
					agentsdummy = resultList[0].getName().getLocalName();
					found = true;
				}
			} while (!found);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		return agentsdummy;
	}

}
