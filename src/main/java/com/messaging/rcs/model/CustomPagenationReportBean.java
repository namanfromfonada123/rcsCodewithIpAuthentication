package com.messaging.rcs.model;

import org.springframework.data.domain.Sort.Direction;

public class CustomPagenationReportBean {

	private String startDate;
	private String endDate;
	private String cLid;
	private Integer page;
	private Integer size;
	private Direction direction;
	private int clientId;
	private String sortingColumn;
	private Integer start;
    private String flag;
    
	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public String getSortingColumn() {
		return sortingColumn;
	}

	public void setSortingColumn(String sortingColumn) {
		this.sortingColumn = sortingColumn;
	}

	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getcLid() {
		return cLid;
	}

	public void setcLid(String cLid) {
		this.cLid = cLid;
	}

	public int getClientId() {
		return clientId;
	}

	public void setClientId(int clientId) {
		this.clientId = clientId;
	}

	@Override
	public String toString() {
		return "CustomCdrReportBean [startDate=" + startDate + ", endDate=" + endDate + ", cLid=" + cLid + ", page="
				+ page + ", size=" + size + ", direction=" + direction + ", sortingColumn=" + sortingColumn + "]";
	}

}
