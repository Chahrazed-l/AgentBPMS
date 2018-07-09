package com.bpms;

import java.io.Serializable;
import java.sql.Timestamp;

public class LogObject implements Serializable {
	String caseId;
	long taskId;
	Timestamp req_retrive;
	Timestamp resp_retrive;
	Timestamp req_assign;
	Timestamp resp_assign;
	Timestamp req_exec;
	Timestamp resp_exec;

	public LogObject(String caseId, long taskId, Timestamp req_retrive, Timestamp resp_retrive, Timestamp req_assign,
			Timestamp resp_assign, Timestamp req_exec, Timestamp resp_exec) {
		this.caseId = caseId;
		this.taskId = taskId;
		this.req_retrive = req_retrive;
		this.resp_retrive = resp_retrive;
		this.req_assign = req_assign;
		this.resp_assign = resp_assign;
		this.req_exec = req_exec;
		this.resp_exec = resp_exec;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public Timestamp getReq_retrive() {
		return req_retrive;
	}

	public void setReq_retrive(Timestamp req_retrive) {
		this.req_retrive = req_retrive;
	}

	public Timestamp getResp_retrive() {
		return resp_retrive;
	}

	public void setResp_retrive(Timestamp resp_retrive) {
		this.resp_retrive = resp_retrive;
	}

	public Timestamp getReq_assign() {
		return req_assign;
	}

	public void setReq_assign(Timestamp req_assign) {
		this.req_assign = req_assign;
	}

	public Timestamp getResp_assign() {
		return resp_assign;
	}

	public void setResp_assign(Timestamp resp_assign) {
		this.resp_assign = resp_assign;
	}

	public Timestamp getReq_exec() {
		return req_exec;
	}

	public void setReq_exec(Timestamp req_exec) {
		this.req_exec = req_exec;
	}

	public Timestamp getResp_exec() {
		return resp_exec;
	}

	public void setResp_exec(Timestamp resp_exec) {
		this.resp_exec = resp_exec;
	}

	

}
