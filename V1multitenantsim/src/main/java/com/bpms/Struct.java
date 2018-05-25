package com.bpms;

import java.util.ArrayList;

public class Struct {
	private ArrayList<Long> pendingList;
	private long proccactif;

	public Struct(ArrayList<Long> pendingList, long proccactif) {
		this.pendingList = pendingList;
		this.proccactif = proccactif;
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
