package com.messaging.rcs.domain;

import javax.persistence.*;

/**
 * Created by sbsingh on Nov/15/2021.
 */
@Entity
@Table(name = "message_types")
public class MessageTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long messageId;
    @Column(columnDefinition = "varchar(255)")
    private String messageType;
    @Column(columnDefinition = "varchar(255)")
    private String description;

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
