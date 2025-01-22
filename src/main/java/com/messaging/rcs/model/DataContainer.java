package com.messaging.rcs.model;

public class DataContainer {
	private String msg;
	private Object Data;
	private Integer status;
	private Integer total;
	private String request_status;

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public String getRequest_status() {
		return request_status;
	}

	public void setRequest_status(String request_status) {
		this.request_status = request_status;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Object getData() {
		return Data;
	}

	public void setData(Object data) {
		Data = data;
	}

	@Override
	public String toString() {
		return "DataContainer [msg=" + msg + ", Data=" + Data + ", status=" + status + ", total=" + total
				+ ", request_status=" + request_status + "]";
	}

}
