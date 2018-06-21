package com.bpms;

import java.sql.Timestamp;
import java.util.ArrayList;

import jade.wrapper.ContainerController;

public class Struct {
	private ArrayList<Long> pendingList;
	private ArrayList<String> pendingcaseId;
	private ArrayList<Timestamp> dateready;
	private long proccactif;
	private ArrayList<String> tenantList;
	private ArrayList<ContainerController> containers;
	private long taskid;
	public Struct(ArrayList<Long> pendingList,ArrayList<String> pendingcaseId,ArrayList<Timestamp> dateready, long proccactif ){
		this.pendingList = pendingList;
		this.pendingcaseId=pendingcaseId;
		this.dateready=dateready;
		this.proccactif = proccactif;
	
	}

	public Struct(ArrayList<String> tenantList, ArrayList<ContainerController> containers) {
		this.tenantList = tenantList;
		this.containers = containers;
	}

	public ArrayList<Timestamp> getDateready() {
		return dateready;
	}

	public void setDateready(ArrayList<Timestamp> dateready) {
		this.dateready = dateready;
	}

	public ArrayList<String> getTenantList() {
		return tenantList;
	}
	

	public long getTaskid() {
		return taskid;
	}

	public void setTaskid(long taskid) {
		this.taskid = taskid;
	}

	public void setTenantList(ArrayList<String> tenantList) {
		this.tenantList = tenantList;
	}

	public ArrayList<ContainerController> getContainers() {
		return containers;
	}

	public void setContainers(ArrayList<ContainerController> containers) {
		this.containers = containers;
	}

	public ArrayList<Long> getPendingList() {
		return pendingList;
	}

	public void setPendingList(ArrayList<Long> pendingList) {
		this.pendingList = pendingList;
	}

	public long getProccactif() {
		return proccactif;
	}

	public void setProccactif(long proccactif) {
		this.proccactif = proccactif;
	}

	public ArrayList<String> getPendingcaseId() {
		return pendingcaseId;
	}

	public void setPendingcaseId(ArrayList<String> pendingcaseId) {
		this.pendingcaseId = pendingcaseId;
	}
	

}
