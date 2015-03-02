package gov.hhs.fha.nhinc.patientdiscovery.nhin.proxy.service;

import gov.hhs.fha.nhinc.messaging.service.port.SOAP12ServicePortDescriptor;
import ihe.iti.xcpd._2009.RespondingGatewayPortType;

public class RespondingGatewayServicePLQPortDescriptor extends SOAP12ServicePortDescriptor<RespondingGatewayPortType> {

	/*
     * (non-Javadoc)
     * 
     * @see gov.hhs.fha.nhinc.messaging.service.port.ServicePortDescriptor#getWSAddressingAction()
     */
    @Override
    public String getWSAddressingAction() {
        return "urn:ihe:iti:2009:PatientLocationQuery";
    }

    /*
     * (non-Javadoc)
     * 
     * @see gov.hhs.fha.nhinc.messaging.service.port.ServicePortDescriptor#getPortClass()
     */
    @Override
    public Class<RespondingGatewayPortType> getPortClass() {
        return RespondingGatewayPortType.class;
    }
}
