package com.messaging.rcs.domain;

import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.util.Date;

public class BaseEntity {


	@Temporal( TemporalType.TIMESTAMP)
	@Column(name="CREATED_DATE", nullable=false)
	protected Date createdDate;

	@Column(name="CREATED_BY", nullable=false)
	protected Long createdBy;		
		
    @Temporal( TemporalType.TIMESTAMP)
	@Column(name="LAST_MODIFIED_DATE", nullable=false)
	protected Date lastModifiedDate;

	@Column(name="LAST_MODIFIED_BY", nullable=false)
	protected Long lastModifiedBy;   
	
	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public Long getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(Long createdBy) {
		this.createdBy = createdBy;
	}

	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

	public Long getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(Long lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

}
