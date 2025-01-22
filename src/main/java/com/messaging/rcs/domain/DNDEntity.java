
package com.messaging.rcs.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "dnd")
public class DNDEntity {

	@Id

	@GeneratedValue(strategy = GenerationType.AUTO)

	@Column(name = "id", nullable = false)
	private Long id;

	private int f1;
	private long f2;
	private String f3;
	private String f4;
	private int prefix;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public int getF1() {
		return f1;
	}

	public void setF1(int f1) {
		this.f1 = f1;
	}

	public long getF2() {
		return f2;
	}

	public void setF2(long f2) {
		this.f2 = f2;
	}

	public String getF3() {
		return f3;
	}

	public void setF3(String f3) {
		this.f3 = f3;
	}

	public String getF4() {
		return f4;
	}

	public void setF4(String f4) {
		this.f4 = f4;
	}

	public int getPrefix() {
		return prefix;
	}

	public void setPrefix(int prefix) {
		this.prefix = prefix;
	}

	@Override
	public String toString() {
		return "DNDEntity [id=" + id + ", f1=" + f1 + ", f2=" + f2 + ", f3=" + f3 + ", f4=" + f4 + ", prefix=" + prefix
				+ "]";
	}

}
