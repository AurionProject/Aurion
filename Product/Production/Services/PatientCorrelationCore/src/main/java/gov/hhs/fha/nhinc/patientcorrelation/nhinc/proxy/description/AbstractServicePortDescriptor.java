/**
 * 
 */
package gov.hhs.fha.nhinc.patientcorrelation.nhinc.proxy.description;

import gov.hhs.fha.nhinc.messaging.service.port.SOAP12ServicePortDescriptor;
import gov.hhs.fha.nhinc.nhinccomponentpatientcorrelation.PatientCorrelationPortType;

/**
 * @author mweaver
 * 
 */
public abstract class AbstractServicePortDescriptor extends SOAP12ServicePortDescriptor<PatientCorrelationPortType> {

    /*
     * (non-Javadoc)
     * 
     * @see gov.hhs.fha.nhinc.messaging.service.port.ServicePortDescriptor#getPortClass()
     */
    @Override
    public Class<PatientCorrelationPortType> getPortClass() {
        return PatientCorrelationPortType.class;
    }
}
