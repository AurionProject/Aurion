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
package gov.hhs.fha.nhinc.transform.audit;

/**
 * 
 * @author MFLYNN02
 * @author rhaslam - Updates for the 2013 Message Audit Specifications
 */
public class AuditDataTransformConstants {

	// EVENT ACTION CODES
    public static final String EVENT_ACTION_CODE_CREATE = "C";
    public static final String EVENT_ACTION_CODE_READ = "R";
    public static final String EVENT_ACTION_CODE_UPDATE = "U";
    public static final String EVENT_ACTION_CODE_DELETE = "D";
    public static final String EVENT_ACTION_CODE_EXECUTE = "E";
    
    // EVENT OUTCOME CODES
    public static final Integer EVENT_OUTCOME_INDICATOR_SUCCESS = 0;
    public static final Integer EVENT_OUTCOME_INDICATOR_MINOR_FAILURE = 4;
    public static final Integer EVENT_OUTCOME_INDICATOR_SERIOUS_FAILURE = 8;
    public static final Integer EVENT_OUTCOME_INDICATOR_MAJOR_FAILURE = 12;
    
    // AUDIT SOURCE CODES - Network Access Point
    public static final Short NETWORK_ACCESS_POINT_TYPE_CODE_DNS = 1;
    public static final Short NETWORK_ACCESS_POINT_TYPE_CODE_IP = 2;

    
    // Active Participant Constants HUMAN
    public static final String DR_HUMAN_USER_IS_REQUESTOR = "true";

    public static final String EVENT_ID_CODE_SYS_NAME_SDN = "SDN";
    public static final String EVENT_ID_CODE_SYS_NAME_SDR = "SDR";
    public static final String EVENT_ID_CODE_SYS_NAME_SDD = "SDD";
    public static final String EVENT_ID_CODE_SYS_NAME_SD = "ESD";
    public static final String EVENT_ID_CODE_SYS_NAME_SR = "ESR";
    public static final String EVENT_ID_CODE_SYS_NAME_ACK = "ACK";
    public static final String EVENT_ID_CODE_SYS_NAME_ADQ = "ADQ";
    public static final String EVENT_ID_CODE_SYS_NAME_SRI = "SRI";
    public static final String EVENT_ID_CODE_SYS_NAME_SRO = "SRO";
    public static final String EVENT_ID_CODE_SYS_NAME_SUB = "SUB";
    public static final String EVENT_ID_CODE_SYS_NAME_UNS = "UNS";
    public static final String EVENT_ID_CODE_SYS_NAME_NOT = "NOT";
    
    
    // PARTICIPANT OBJECT Constants
    // Used to identify the type of entity represented by a ParticipantObjectIdentification record
    public static final Short PARTICIPANT_OBJECT_TYPE_CODE_PERSON = 1;
    public static final Short PARTICIPANT_OBJECT_TYPE_CODE_SYSTEM = 2;
    
    // Used to identify the role within the typeCode (PERSON or SYSTEM) filled by the data
    // Used for the Human Patient
    public static final short PARTICIPANT_OBJECT_TYPE_CODE_ROLE_PATIENT = 1;    
    // Used for System Objects
    // Used by DocQuery
    public static final short PARTICIPANT_OBJECT_TYPE_CODE_ROLE_REPORT = 3;
    
    // Used to return QUERY Parameters in various messages
    public static final short PARTICIPANT_OBJECT_TYPE_CODE_ROLE_QUERY = 24;
    
    // JOB is used for XDR
    public static final short PARTICIPANT_OBJECT_TYPE_CODE_ROLE_JOB = 20;
    
    // Not in any specification - Used to carry message being audited to the Audit Log subsystem
    public static final short PARTICIPANT_OBJECT_TYPE_CODE_ROLE_DATA_TRANSPORT=64;
    
    // Not is any specification - Used to carry sending communityId to the Audit Log subsystem
    public static final short PARTICIPANT_OBJECT_TYPE_CODE_ROLE_COMMUNITY = 96;
    
    // The parameters for the CodedValueType for Participant == Patient
    public static final String PARTICIPANT_PATIENT_ID_CODE = "2";
    public static final String PARTICIPANT_PATIENT_ID_SYSTEM = "RFC-3881";
    public static final String PARTICIPANT_PATIENT_ID_SYSTEM_NAME = "RFC-3881";
    public static final String PARTICIPANT_PATIENT_ID_SYSTEM_DISPLAY_NAME = "Patient Number";

    // The parameters for the CodedValueType for Participant == "Document"
    public static final String PARTICIPANT_DOCUMENT_ID_CODE = "9";
    public static final String PARTICIPANT_DOCUMENT_ID_SYSTEM = "RFC-3881";
    public static final String PARTICIPANT_DOCUMENT_ID_SYSTEM_NAME = "RFC-3881";
    public static final String PARTICIPANT_DOCUMENT_ID_SYSTEM_DISPLAY_NAME = "Report Number";

    // The parameters for the CodedValueType for Participant == Document Submission
    public static final String PARTICIPANT_SUBMISSION_SET_ID_CODE = "urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd";
    public static final String PARTICIPANT_SUBMISSION_SET_ID_SYSTEM = "IHE XDS Metadata";
    public static final String PARTICIPANT_SUBMISSION_SET_ID_SYSTEM_NAME = "IHE XDS Metadata";
    public static final String PARTICIPANT_SUBMISSION_SET_ID_SYSTEM_DISPLAY_NAME = "submission set classificationNode";

    // The parameters for the CodedValueType for Participant == Query Parameters
    public static final String PARTICIPANT_QUERY_SET_ID_CODE = "urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd";
    public static final String PARTICIPANT_QUERY_SET_ID_SYSTEM = "IHE XDS Metadata";
    public static final String PARTICIPANT_QUERY_SET_ID_SYSTEM_NAME = "IHE XDS Metadata";
    public static final String PARTICIPANT_QUERY_SET_ID_SYSTEM_DISPLAY_NAME = "submission set classificationNode";
    
    /*
     *  Not in any specification - CodedValueType parameters for the ParticipantObjectIdentification object that carries
     *  the message being audited to the Audit Logging Subsystem
     *  
     *  The parameters for the CodedValueType for the Participant == Data Transport
     */
    public static final String PARTICIPANT_DATA_OBJECT_ID_CODE="NwHIN Participant Object";
    public static final String PARTICIPANT_DATA_OBJECT_ID_CODE_SYSTEM="NwHIN";
    public static final String PARTICIPANT_DATA_OBJECT_ID_CODE_SYSTEM_NAME="Nationwide Health Information Network";
    public static final String PARTICIPANT_DATA_OBJECT_ID_SYSTEM_DISPLAY_NAME="Message being audited";
    
    // The parameters for the CodedValueType for Participant == Community
    public static final String PARTICIPANT_COMMUNITY_CODE = "NwHIN Participant Object";
    public static final String PARTICIPANT_COMMUNITY_SYSTEM = "NwHIN";
    public static final String PARTICIPANT_COMMUNITY_SYSTEM_NAME = "Nationwide Health Information Network";
    public static final String PARTICIPANT_COMMUNITYSYSTEM_DISPLAY_NAME = "Community Participant";
    
    // ========================== Active Participant SOURCE RoleIdCode ===================================================
    
    public static final String AP_SOURCE_TYPE_CODE = "110153";
    public static final String AP_SOURCE_TYPE_CODE_SYSTEM = "DCM";
    public static final String AP_SOURCE_TYPE_CODE_SYSTEM_NAME = "DCM";
    public static final String AP_SOURCE_TYPE_CODE_SYSTEM_DISPLAY_NAME = "Source";

    // ========================== Active Participant Destination RoleIdCode ===================================================
    public static final String AP_DESTINATION_TYPE_CODE = "110152";
    public static final String AP_DESTINATION_TYPE_CODE_SYSTEM = "DCM";
    public static final String AP_DESTINATION_TYPE_CODE_SYSTEM_NAME = "DCM";
    public static final String AP_DESTINATION_TYPE_CODE_SYSTEM_DISPLAY_NAME = "Destination";
    
    // =========================  PATIENT DISCOVERY AUDIT SPECIFICATION ===================================================
    
    // Event Identification (Patient Discovery Request)
    public static final String PD_REQUEST_EVENT_ID_CODE = "110112"; 
    public static final String PD_REQUEST_EVENT_ID_CODE_SYSTEM = "DCM";
    public static final String PD_REQUEST_EVENT_ID_CODE_SYSTEM_NAME = "DCM";
    public static final String PD_REQUEST_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME = "Query";
    public static final String PD_REQUEST_EVENT_ACTION_CODE ="E"; 
    
    // Event Identification (Patient Discovery Response)
    public static final String PD_RESPONSE_EVENT_ID_CODE = "110112"; 
    public static final String PD_RESPONSE_EVENT_ID_CODE_SYSTEM = "DCM";
    public static final String PD_RESPONSE_EVENT_ID_CODE_SYSTEM_NAME = "DCM";
    public static final String PD_RESPONSE_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME = "Query";
    public static final String PD_RESPONSE_EVENT_ACTION_CODE ="E"; 
    
    // Event Identification EventTypeCode
    public static final String PD_EVENT_TYPE_CODE = "ITI-55";
    public static final String PD_EVENT_TYPE_CODE_SYSTEM = "IHE Transactions";
    public static final String PD_EVENT_TYPE_CODE_SYSTEM_NAME =  "IHE Transactions";
    public static final String PD_EVENT_TYPE_CODE_SYSTEM_DISPLAY_NAME = "Cross Gateway Patient Discovery";
    
    // Active Participant Constants SOURCE SYSTEM
    public static final String PD_SOURCE_USER_IS_REQUESTOR = "false";
    public static final String PD_SOURCE_TYPE_CODE = "110153";
    public static final String PD_SOURCE_TYPE_CODE_SYSTEM = "DCM";
    public static final String PD_SOURCE_TYPE_CODE_SYSTEM_NAME = "DCM";
    public static final String PD_SOURCE_TYPE_CODE_SYSTEM_DISPLAYNAME = "Source";
     
    // Active Participant Constants DESTINATION SYSTEM
    public static final String PD_DESTINATION_USER_IS_REQUESTOR = "true";
    public static final String PD_DESTINATION_TYPE_CODE = "110152";
    public static final String PD_DESTINATION_TYPE_CODE_SYSTEM = "DCM";
    public static final String PD_DESTINATION_TYPE_CODE_SYSTEM_NAME = "DCM";
    public static final String PD_DESTINATION_TYPE_CODE_SYSTEM_DISPLAYNAME = "Destination";
    
    // USed for PD PARTICIPANT OBJECT (Query Parameters)
    public static final String PD_PARTICIPANT_ID_CODE_QUERY_QUERY_PARAMS = "ITI-55";
    public static final String PD_PARTICIPANT_ID_CODE_SYSTEM_QUERY_PARAMS = "IHE Transactions";
    public static final String PD_PARTICIPANT_ID_CODE_SYSTEM_NAME_QUERY_PARAMS = "IHE Transactions";
    public static final String PD_PARTICIPANT_ID_CODE_SYSTEM_DISPLAY_NAME_QUERY_PARAMS = "Cross Gateway Patient Discovery";

    // ========================================  DOCUMENT QUERY AUDIT SPECIFICATIONS  ======================================================
    // Event Identification (Document Query Request)
    public static final String DQ_REQUEST_EVENT_ID_CODE = "110112"; 
    public static final String DQ_REQUEST_EVENT_ID_CODE_SYSTEM = "DCM";
    public static final String DQ_REQUEST_EVENT_ID_CODE_SYSTEM_NAME = "DCM";
    public static final String DQ_REQUEST_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME = "Query";
    public static final String DQ_REQUEST_EVENT_ACTION_CODE ="E"; 
    
    // Event Identification (Document Query Response)
    public static final String DQ_RESPONSE_EVENT_ID_CODE = "110112"; 
    public static final String DQ_RESPONSE_EVENT_ID_CODE_SYSTEM = "DCM";
    public static final String DQ_RESPONSE_EVENT_ID_CODE_SYSTEM_NAME = "DCM";
    public static final String DQ_RESPONSE_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME = "Query";
    public static final String DQ_RESPONE_EVENT_ACTION_CODE ="E"; 

    // Event Identification EventTypeCode Document Query
    public static final String DQ_EVENT_TYPE_CODE = "ITI-18";
    public static final String DQ_EVENT_TYPE_CODE_SYSTEM = "IHE Transactions";
    public static final String DQ_EVENT_TYPE_CODE_SYSTEM_NAME =  "IHE Transactions";
    public static final String DQ_EVENT_TYPE_CODE_SYSTEM_DISPLAY_NAME = "Registry Stored Query";
    
    // Active Participant Constants SOURCE SYSTEM
    public static final String DQ_SOURCE_USER_IS_REQUESTOR = "true";
    public static final String DQ_SOURCE_TYPE_CODE = "110153";
    public static final String DQ_SOURCE_TYPE_CODE_SYSTEM = "DCM";
    public static final String DQ_SOURCE_TYPE_CODE_SYSTEM_NAME = "DCM";
    public static final String DQ_SOURCE_TYPE_CODE_SYSTEM_DISPLAYNAME = "Source";
     
    // Active Participant Constants DESTINATION SYSTEM
    public static final String DQ_DESTINATION_USER_IS_REQUESTOR = "false";
    
    // USed for DQ PARTICIPANT OBJECT (Query Parameters)
    public static final String DQ_PARTICIPANT_ID_CODE_QUERY_PARAMS = "“ITI-18”";
    public static final String DQ_PARTICIPANT_ID_SYSTEM_QUERY_PARAMS = "IHE Transactions";
    public static final String DQ_PARTICIPANT_ID_SYSTEM_NAME_QUERY_PARAMS = "IHE Transactions";
    public static final String DQ_PARTICIPANT_ID_SYSTEM_DISPLAY_NAME_QUERY_PARAMS = "Registry Stored Query”)";

    
    // ========================================    DOCUMENT RETRIEVE AUDIT SPECIFICATIONS  =========================================
    // Event Identification (Document Retrieve Request)
    public static final String DR_REQUEST_EVENT_ID_CODE = "110107"; 
    public static final String DR_REQUEST_EVENT_ID_CODE_SYSTEM = "DCM";
    public static final String DR_REQUEST_EVENT_ID_CODE_SYSTEM_NAME = "DCM";
    public static final String DR_REQUEST_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME = "Import";
    public static final String DR_REQUEST_EVENT_ACTION_CODE ="C"; 
    
    // Event Identification (Document Retrieve Response)
    public static final String DR_RESPONSE_EVENT_ID_CODE = "110106"; 
    public static final String DR_RESPONSE_EVENT_ID_CODE_SYSTEM = "DCM";
    public static final String DR_RESPONSE_EVENT_ID_CODE_SYSTEM_NAME = "DCM";
    public static final String DR_RESPONSE_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME = "Export";
    public static final String DR_RESPONSE_EVENT_ACTION_CODE ="R";
    
    // Event Identification EventTypeCode Document Retrieve
    public static final String DR_EVENT_TYPE_CODE = "ITI-43";
    public static final String DR_EVENT_TYPE_CODE_SYSTEM = "IHE Transactions";
    public static final String DR_EVENT_TYPE_CODE_SYSTEM_NAME =  "IHE Transactions";
    public static final String DR_EVENT_TYPE_CODE_SYSTEM_DISPLAY_NAME = "Retrieve Document Set";
    
    // Active Participant Constants SOURCE SYSTEM
    public static final String DR_SOURCE_USER_IS_REQUESTOR = "false";
    public static final String DR_SOURCE_TYPE_CODE = "110153";
    public static final String DR_SOURCE_TYPE_CODE_SYSTEM = "DCM";
    public static final String DR_SOURCE_TYPE_CODE_SYSTEM_NAME = "DCM";
    public static final String DR_SOURCE_TYPE_CODE_SYSTEM_DISPLAYNAME = "Source";
     
    // Active Participant Constants DESTINATION SYSTEM
    public static final String DR_DESTINATION_USER_IS_REQUESTOR = "true";
    public static final String DR_DESTINATION_TYPE_CODE = "110152";
    public static final String DR_DESTINATION_TYPE_CODE_SYSTEM = "DCM";
    public static final String DR_DESTINATION_TYPE_CODE_SYSTEM_NAME = "DCM";
    public static final String DR_DESTINATION_TYPE_CODE_SYSTEM_DISPLAYNAME = "Destination";

    // USed for DR PARTICIPANT OBJECT (Query Parameters)
    public static final Short DR_PARTICIPANT_TYPE_CODE_ROLE = 24;
    
    public static final String DR_PARTICIPANT_ID_CODE = "“ITI-43”";
    public static final String DR_PARTICIPANT_ID_SYSTEM = "IHE Transactions";
    public static final String DR_PARTICIPANT_ID_SYSTEM_NAME = "IHE Transactions";
    public static final String DR_PARTICIPANT_ID_SYSTEM_DISPLAY_NAME = "Retrieve Document Set”)";

    
    // ==============================================   DOCUMENT SUBMISSION AUDIT SPECIFICATIONS    ======================================================
    // Event Identification (Document Submission Request)
    public static final String XDR_REQUEST_EVENT_ID_CODE = "110106"; 
    public static final String XDR_REQUEST_EVENT_ID_CODE_SYSTEM = "DCM";
    public static final String XDR_REQUEST_EVENT_ID_CODE_SYSTEM_NAME = "DCM";
    public static final String XDR_REQUEST_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME = "Export";
    public static final String XDR_REQUEST_EVENT_ACTION_CODE ="R";
    
    // Event Identification (Document Submission Response)
    public static final String XDR_RESPONSE_EVENT_ID_CODE = "110107"; 
    public static final String XDR_RESPONSE_EVENT_ID_CODE_SYSTEM = "DCM";
    public static final String XDR_RESPONSE_EVENT_ID_CODE_SYSTEM_NAME = "DCM";
    public static final String XDR_RESPONSE_EVENT_ID_CODE_SYSTEM_DISPLAY_NAME = "Import";
    public static final String XDR_RESPONSE_EVENT_ACTION_CODE ="C";
    
    // Event Identification EventTypeCode Document Submission
    public static final String XDR_EVENT_TYPE_CODE = "ITI-41";
    public static final String XDR_EVENT_TYPE_CODE_SYSTEM = "IHE Transactions";
    public static final String XDR_EVENT_TYPE_CODE_SYSTEM_NAME =  "IHE Transactions";
    public static final String XDR_EVENT_TYPE_CODE_SYSTEM_DISPLAY_NAME = "Provide and Register Document Set-b";
    
    // Active Participant Constants Document Submission SOURCE SYSTEM
    public static final String XDR_SOURCE_USER_IS_REQUESTOR = "true";
    public static final String XDR_SOURCE_TYPE_CODE = "110153";
    public static final String XDR_SOURCE_TYPE_CODE_SYSTEM = "DCM";
    public static final String XDR_SOURCE_TYPE_CODE_SYSTEM_NAME = "DCM";
    public static final String XDR_SOURCE_TYPE_CODE_SYSTEM_DISPLAY_NAME = "Source";
     
    // Active Participant Constants Document Submission DESTINATION SYSTEM
    public static final String XDR_DESTINATION_USER_IS_REQUESTOR = "false";
    public static final String XDR_DESTINATION_TYPE_CODE = "110152";
    public static final String XDR_DESTINATION_TYPE_CODE_SYSTEM = "DCM";
    public static final String XDR_DESTINATION_TYPE_CODE_SYSTEM_NAME = "DCM";
    public static final String XDR_DESTINATION_TYPE_CODE_SYSTEM_DISPLAYNAME = "Destination";
    
    // Used for XDR Participant Object
    public static final Short XDR_PARTICIPANT_SET_TYPE_CODE_SYSTEM = 2;
    public static final Short XDR_PARTICIPANT_SET_TYPE_CODE_ROLE = 20;
    
    public static final String XDR_PARTICIPANT_SET_ID_CODE = "“urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd”";
    public static final String XDR_PARTICIPANT_SET_ID_SYSTEM = "IHE XDS Metadata";
    public static final String XDR_PARTICIPANT_SET_ID_SYSTEM_NAME = "IHE XDS Metadata";
    public static final String XDR_PARTICIPANT_SET_ID_SYSTEM_DISPLAY_NAME = "submission set classificationNode”)";
    
    
    // ==================================  UN VALIDATED ADMIN DISTRIBUTION, Subscribe, UnSubscribe, Notify =============================================
    
    public static final String EVENT_ID_CODE_SYS_NAME_T63 = "T63";
    public static final String EVENT_ID_DISPLAY_NAME_ADMIN_DIST = "Administrative Distribution";

    public static final String EVENT_ID_DISPLAY_NAME_SUBSCRIBE = "Subscribe";
    public static final String EVENT_ID_DISPLAY_NAME_UNSUBSCRIBE = "Unsubscribe";
    public static final String EVENT_ID_DISPLAY_NAME_NOTIFY = "Notify";

}
