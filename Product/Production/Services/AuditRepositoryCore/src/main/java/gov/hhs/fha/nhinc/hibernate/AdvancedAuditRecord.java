package gov.hhs.fha.nhinc.hibernate;

import java.sql.Clob;


/**
 * 
 * AdvancedAuditRecord.java
 * 
 * This the the model file for the database known as: auditrepo.advanced_audit
 * 
 * This table enhances the information stored in auditrepo.auditrepository
 * 
 * @author rhaslam
 */

public class AdvancedAuditRecord { 

	private Long   id = null;
	private String serviceName = null;
	private String subsystem = null;
	private String messageDirection = null;
	private String messageId = null;
	private String userName = null;
	private String userRoles = null;
	private String sourceSystem = null;
	private String sourceCommunity = null;
	private Clob queryParams = null;
	private Clob messageAudited = null;
	private AuditRepositoryRecord auditRepositoryRecord = null;
	
	// Auto generated "getters" and "setters"
	public Long getId() {
		return id;
	}
	public void setId(Long auditId) {
		this.id = auditId;
	}

	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getSubsystem() {
		return subsystem;
	}
	public void setSubsystem(String subsystem) {
		this.subsystem = subsystem;
	}
	
	public String getMessageDirection() {
		return messageDirection;
	}
	public void setMessageDirection(String messageDirection) {
		this.messageDirection = messageDirection;
	}
	
	public String getMessageId() { 
		return messageId;
	}
	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getUserRoles() {
		return userRoles;
	}
	public void setUserRoles(String userRoles) {
		this.userRoles = userRoles;
	}
	
	public String getSourceSystem() {
		return sourceSystem;
	}
	public void setSourceSystem(String sourceSystem) {
		this.sourceSystem = sourceSystem;
	}
	
	public void setSourceCommunity(String sourceCommunity) {
		this.sourceCommunity = sourceCommunity;
	}
	public String getSourceCommunity() {
		return sourceCommunity;
	}

	public Clob getQueryParams() {
		return queryParams;
	}
	public void setQueryParams(Clob queryParams) {
		this.queryParams = queryParams;
	}
	public Clob getMessageAudited() {
		return messageAudited;
	}
	public void setMessageAudited(Clob messageAudited) {
		this.messageAudited = messageAudited;
	}

	public AuditRepositoryRecord getAuditRepositoryRecord() {
		return auditRepositoryRecord;
	}
	public void setAuditRepositoryRecord(AuditRepositoryRecord auditRepositoryRecord) {
		this.auditRepositoryRecord = auditRepositoryRecord;
	}

}
