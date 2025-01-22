package com.messaging.rcs.email.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;


@Entity
@Table(name = "email_template")
@EntityListeners(AuditingEntityListener.class)
public class EmailTemplate implements Serializable{

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@NotBlank
	@Column(name="event_key")
    private String eventKey;

	@NotBlank
	@Column(name="from_email")
    private String fromEmail;

	@NotBlank
	@Column(name="to_email", length = 2000)
	private String toEmail;

	@NotBlank
	@Column(name="subject")
    private String subject;

	@NotBlank
	@Column(name="email_body", length = 2000)
    private String emailBody;

	@NotBlank
	@Column(name="expression_key")
    private String expressionKey; //Not in use

	@NotBlank
	@Column(name="is_active")
    private String isActive;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEventKey() {
		return eventKey;
	}

	public void setEventKey(String eventKey) {
		this.eventKey = eventKey;
	}

	public String getFromEmail() {
		return fromEmail;
	}

	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getEmailBody() {
		return emailBody;
	}

	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}

	public String getExpressionKey() {
		return expressionKey;
	}

	public void setExpressionKey(String expressionKey) {
		this.expressionKey = expressionKey;
	}

	public String getIsActive() {
		return isActive;
	}

	public void setIsActive(String isActive) {
		this.isActive = isActive;
	}

	public String getToEmail() {
		return toEmail;
	}

	public void setToEmail(String toEmail) {
		this.toEmail = toEmail;
	}

	@Override
	public String toString() {
		return "EmailTemplate{" +
				"id=" + id +
				", eventKey='" + eventKey + '\'' +
				", fromEmail='" + fromEmail + '\'' +
				", toEmail='" + toEmail + '\'' +
				", subject='" + subject + '\'' +
				", emailBody='" + emailBody + '\'' +
				", expressionKey='" + expressionKey + '\'' +
				", isActive='" + isActive + '\'' +
				'}';
	}
}
