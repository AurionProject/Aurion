/**
 * 
 */
package gov.hhs.fha.nhinc.messaging.client;

import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.messaging.service.ServiceEndpoint;
import gov.hhs.fha.nhinc.messaging.service.decorator.MTOMServiceEndpointDecorator;
import gov.hhs.fha.nhinc.messaging.service.decorator.cxf.WsAddressingServiceEndpointDecorator;
import gov.hhs.fha.nhinc.messaging.service.port.CXFServicePortBuilder;
import gov.hhs.fha.nhinc.messaging.service.port.ServicePortBuilder;
import gov.hhs.fha.nhinc.messaging.service.port.ServicePortDescriptor;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.properties.PropertyAccessException;
import gov.hhs.fha.nhinc.properties.PropertyAccessor;

/**
 * @author bhumphrey
 * 
 */
public abstract class CONNECTCXFClient<T> extends CONNECTBaseClient<T> {

    protected ServiceEndpoint<T> serviceEndpoint = null;

    CONNECTCXFClient(ServicePortDescriptor<T> portDescriptor, String url, AssertionType assertion) {
        this(portDescriptor, url, assertion, new CXFServicePortBuilder<T>(portDescriptor));
    }

    CONNECTCXFClient(ServicePortDescriptor<T> portDescriptor, String url, AssertionType assertion,
            ServicePortBuilder<T> portBuilder) {
        serviceEndpoint = super.configureBasePort(portBuilder.createPort(), url);
    }

    CONNECTCXFClient(ServicePortDescriptor<T> portDescriptor, String url, AssertionType assertion,
            ServicePortBuilder<T> portBuilder, String subscriptionId) {
        serviceEndpoint = super.configureBasePort(portBuilder.createPort(), subscriptionId);
    }

    public T getPort() {
        return serviceEndpoint.getPort();
    }
    
    public void enableMtom() {
        serviceEndpoint = new MTOMServiceEndpointDecorator<T>(serviceEndpoint);
        serviceEndpoint.configure();
    }

    public void enableWSA(AssertionType assertion, String wsAddressingTo, String wsAddressingActionId) {
        serviceEndpoint = new WsAddressingServiceEndpointDecorator<T>(serviceEndpoint, wsAddressingTo, wsAddressingActionId, assertion);
        serviceEndpoint.configure();
    }
    
    // This method was added to allow the setting of timeout values on a per endpooint basis
    // Override Default Timeouts
    public boolean overrideDefaultTimeouts(String connectTOTag, String responseTOTag){
    	if (NullChecker.isNotNullish(connectTOTag) && NullChecker.isNotNullish(responseTOTag)) {
            // Get the HTTPClientPolicyObject
    		HTTPClientPolicy httpPolicy = serviceEndpoint.getHTTPClientPolicy();
            long lConnectionTimeout;
            long lResponseTimeout;            
    		try {
	    		// Lookup values from the gateway.properties file for TIMEOUTS
	        	String sConnectTimeout  = PropertyAccessor.getInstance().getProperty(NhincConstants.GATEWAY_PROPERTY_FILE, connectTOTag);
	            String sResponseTimeout = PropertyAccessor.getInstance().getProperty(NhincConstants.GATEWAY_PROPERTY_FILE, responseTOTag);


            	lConnectionTimeout = Long.parseLong(sConnectTimeout);
            	lResponseTimeout = Long.parseLong(sResponseTimeout);
            } catch (NumberFormatException e) {
            	//LOG.error("Failed to convert " + connectTOTag + " or " responseToTag + " from gateway.properties to long values. Check gateway.properties");
            	return false;
            } catch (PropertyAccessException e) {
            	//LOG.error("Failed to locate " + connectTOTag + " or " responseToTag + " from gateway.properties to long values. Check gateway.properties");
            	return false;
            }
            // Set the customized timeout values
            httpPolicy.setConnectionTimeout(lConnectionTimeout);
            httpPolicy.setReceiveTimeout(lResponseTimeout);
            return true;
    	}
    	return false;
    }
}
