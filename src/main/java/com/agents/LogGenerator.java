package com.agents;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.bpms.LogObject;

import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.UnreadableException;

public class LogGenerator extends Agent {
	private String tenantName;
	private static final String COMMA_DELIMITER = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	private static final String FILE_HEADER = "caseId,taskId,Ready_Time,Originator,Req_retrieve_Time,Resp_retrieve_Time,Req_Assign_Tim,Resp_Assign_Time,Req_exec_Time,Resp_exec_Time,Retrive_Time,Assign_Time,Exec_Time,Retrive_Exec,Treatment,Ready_Exec";
	private FileWriter fileWriter = null;
	private File file;

	public void setup() {
		Object[] args = getArguments();
		tenantName = args[0].toString();
		registerUserAgent(tenantName);
		try {
			file = new File("result.csv");
			fileWriter = new FileWriter(file);
			fileWriter.append(FILE_HEADER.toString());
			System.out.println("CSV file was created successfully !!!");
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.addBehaviour(new generateBehav());
	}

	public class generateBehav extends Behaviour {
		int step = 1;

		@Override
		public void action() {
			// TODO Auto-generated method stub
			switch (step) {
			case 1:
				ACLMessage mgk = myAgent.receive();
				if (mgk != null) {
					if (mgk.getPerformative() == ACLMessage.INFORM) {
						try {
							fileWriter = new FileWriter(file, true);
							LogObject obj = (LogObject) mgk.getContentObject();
							fileWriter.append(NEW_LINE_SEPARATOR);
							fileWriter.append(obj.getCaseId());
							fileWriter.append(COMMA_DELIMITER);
							fileWriter.append(String.valueOf(obj.getTaskId()));
							fileWriter.append(COMMA_DELIMITER);
							fileWriter.append(String.valueOf(obj.getDateready()));
							fileWriter.append(COMMA_DELIMITER);
							fileWriter.append(mgk.getSender().getLocalName());
							fileWriter.append(COMMA_DELIMITER);
							fileWriter.append(String.valueOf(obj.getReq_retrive()));
							fileWriter.append(COMMA_DELIMITER);
							fileWriter.append(String.valueOf(obj.getResp_retrive()));
							fileWriter.append(COMMA_DELIMITER);
							fileWriter.append(String.valueOf(obj.getReq_assign()));
							fileWriter.append(COMMA_DELIMITER);
							fileWriter.append(String.valueOf(obj.getResp_assign()));
							fileWriter.append(COMMA_DELIMITER);
							fileWriter.append(String.valueOf(obj.getReq_exec()));
							fileWriter.append(COMMA_DELIMITER);
							fileWriter.append(String.valueOf(obj.getResp_exec()));
							fileWriter.append(COMMA_DELIMITER);
							long reTime=obj.getResp_retrive().getTime()-obj.getReq_retrive().getTime();
							fileWriter.append(String.valueOf(reTime));
							fileWriter.append(COMMA_DELIMITER);
							long auTime=obj.getResp_assign().getTime()-obj.getReq_assign().getTime();
							fileWriter.append(String.valueOf(auTime));
							fileWriter.append(COMMA_DELIMITER);
							long exTime=obj.getResp_exec().getTime()-obj.getReq_exec().getTime();
							fileWriter.append(String.valueOf(exTime));
							fileWriter.append(COMMA_DELIMITER);
							long diff = obj.getResp_exec().getTime() - obj.getReq_retrive().getTime();
							fileWriter.append(String.valueOf(diff));
							fileWriter.append(COMMA_DELIMITER);
							long diff1 = diff-(exTime+auTime+reTime);
							fileWriter.append(String.valueOf(diff1));
							fileWriter.append(COMMA_DELIMITER);
							long readyToexec = obj.getResp_exec().getTime()-obj.getDateready().getTime();
							fileWriter.append(String.valueOf(readyToexec));
						} catch (UnreadableException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						try {
							fileWriter.flush();
							fileWriter.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

				}

				step = 1;
				break;
			}

		}

		@Override
		public boolean done() {
			// TODO Auto-generated method stub
			return step == -1;
		}

	}

	private void registerUserAgent(String tenantName) {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName(this.getLocalName());
		sd.setType("Generate" + tenantName);
		dfd.addServices(sd);

		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}
}
