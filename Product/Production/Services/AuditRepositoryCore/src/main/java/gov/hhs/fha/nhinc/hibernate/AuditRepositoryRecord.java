/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services. 
 * All rights reserved. 
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met: 
 *     * Redistributions of source code must retain the above 
 *       copyright notice, this list of conditions and the following disclaimer. 
 *     * Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the documentation 
 *       and/or other materials provided with the distribution. 
 *     * Neither the name of the United States Government nor the 
 *       names of its contributors may be used to endorse or promote products 
 *       derived from this software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY 
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package gov.hhs.fha.nhinc.hibernate;

import java.util.Date;
import java.sql.Blob;

/**
 * 
 * @author MFLYNN02
 */
public class AuditRepositoryRecord {
    private long id = 0;
    private Date timeStamp = null;
    private long eventId = 0;
    private String userId = null;
    private short participationTypeCode = 0;
    private short participationTypeCodeRole = 0;
    private String participationIDTypeCode = null;
    private String receiverPatientId = null;
    private String senderPatientId = null;
    private String communityId = null;
    private String messageType = null;
    private String purposeOfUse = null;
    private Blob message = null;
    private AdvancedAuditRecord advancedAuditRecord = null;

    public AuditRepositoryRecord() {
    }

    public long getEventId() {
        return eventId;
    }

    public void setEventId(long eventId) {
        this.eventId = eventId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Blob getMessage() {
        return message;
    }

    public void setMessage(Blob message) {
        this.message = message;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getParticipationIDTypeCode() {
        return participationIDTypeCode;
    }

    public void setParticipationIDTypeCode(String participationIDTypeCode) {
        this.participationIDTypeCode = participationIDTypeCode;
    }

    public short getParticipationTypeCode() {
        return participationTypeCode;
    }

    public void setParticipationTypeCode(short participationTypeCode) {
        this.participationTypeCode = participationTypeCode;
    }

    public short getParticipationTypeCodeRole() {
        return participationTypeCodeRole;
    }

    public void setParticipationTypeCodeRole(short participationTypeCodeRole) {
        this.participationTypeCodeRole = participationTypeCodeRole;
    }

    public String getCommunityId() {
        return communityId;
    }

    public void setCommunityId(String communityId) {
        this.communityId = communityId;
    }

    public String getReceiverPatientId() {
        return receiverPatientId;
    }

    public void setReceiverPatientId(String receiverPatientId) {
        this.receiverPatientId = receiverPatientId;
    }

    public String getSenderPatientId() {
        return senderPatientId;
    }

    public void setSenderPatientId(String senderPatientId) {
        this.senderPatientId = senderPatientId;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPurposeOfUse() {
        return purposeOfUse;
    }

    public void setPurposeOfUse(String purposeOfUse) {
        this.purposeOfUse = purposeOfUse;
    }

	public AdvancedAuditRecord getAdvancedAuditRecord() {
		return advancedAuditRecord;
	}

	public void setAdvancedAuditRecord(AdvancedAuditRecord advancedAuditRecord) {
		this.advancedAuditRecord = advancedAuditRecord;
	}
}
