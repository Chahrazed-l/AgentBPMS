package com.bpms;

import java.util.ArrayList;

import jade.wrapper.ContainerController;

public class Struct {
	private ArrayList<Long> pendingList;
	private long proccactif;
	private ArrayList<String> tenantList;
	private ArrayList<ContainerController> containers;

	public Struct(ArrayList<Long> pendingList, long proccactif) {
		this.pendingList = pendingList;
		this.proccactif = proccactif;
	}

	public Struct(ArrayList<String> tenantList, ArrayList<ContainerController> containers) {
		this.tenantList = tenantList;
		this.containers = containers;
	}

	public ArrayList<String> getTenantList() {
		return tenantList;
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

}
