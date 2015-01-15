package gov.hhs.fha.nhinc.transform.audit;

import com.services.nhinc.schema.auditmessage.CodedValueType;


/*
 * This record stores the defined values from a AuditMessage ParticipantObjectIdentification record.
 * 
 * There can be multiple such records in an AuditMessage. This object helps us store the various records
 * as we parse them all
 */
public class ParticipantRecord {

	private short participantTypeCode;
	private short participantRoleCode;
	private String participantId;
	private CodedValueType participantIdCodedValue;
	private String participantIdCVTasString;
	private String participantIdCode;
	private String participantName;
	private byte[] messageContent;
	
	public short getParticipantTypeCode() {
		return participantTypeCode;
	}
	public void setParticipantTypeCode(short participantTypeCode) {
		this.participantTypeCode = participantTypeCode;
	}
	public short getParticipantRoleCode() {
		return participantRoleCode;
	}
	public void setParticipantRoleCode(short participantRoleCode) {
		this.participantRoleCode = participantRoleCode;
	}

	public String getParticipantId() {
		return participantId;
	}
	public void setParticipantId(String participantId) {
		this.participantId = participantId;
	}

	public CodedValueType getParticipantIdCodedValue() {
		return participantIdCodedValue;
	}
	public void setParticipantIdCodedValue(CodedValueType participantIdCodedValue) {
		this.participantIdCodedValue = participantIdCodedValue;
	}
	
	// This supports an enhanced option for entering into baseAuditTable column participationIdTypeCode
	public String getParticipantIdCVTasString() {
		return AuditDataTransformHelper.formatCodedValue(this.participantIdCodedValue);
	}
	public void setParticipantIdCVTasString(String participantIdCVTasString) {
		this.participantIdCVTasString = participantIdCVTasString;
	}

	// This supports the legacy display of the baseAuditTable column participationIdTypeCode
	public String getParticipantIdCode() {
		return this.participantIdCodedValue.getCode();
	}
	public void setParticipantIdCode(String participantIdCode) {
		this.participantIdCode = participantIdCode;
	}
	
	public String getParticipantName() {
		return participantName;
	}
	public void setParticipantName(String participantName) {
		this.participantName = participantName;
	}
	
	public byte[] getMessageContent() {
		return messageContent;
	}
	public void setMessageContent(byte[] messageContent) {
		this.messageContent = messageContent;
	}	

}
