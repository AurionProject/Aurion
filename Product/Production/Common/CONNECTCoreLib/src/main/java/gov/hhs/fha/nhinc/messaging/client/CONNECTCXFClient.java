/**
 * 
 */
package gov.hhs.fha.nhinc.messaging.client;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.BindingProvider;

import org.apache.cxf.headers.Header;
import org.apache.log4j.Logger;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.messaging.service.ServiceEndpoint;
import gov.hhs.fha.nhinc.messaging.service.decorator.MTOMServiceEndpointDecorator;
import gov.hhs.fha.nhinc.messaging.service.decorator.cxf.WsAddressingServiceEndpointDecorator;
import gov.hhs.fha.nhinc.messaging.service.port.CXFServicePortBuilder;
import gov.hhs.fha.nhinc.messaging.service.port.ServicePortBuilder;
import gov.hhs.fha.nhinc.messaging.service.port.ServicePortDescriptor;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;

/**
 * @author bhumphrey
 * 
 */
public abstract class CONNECTCXFClient<T> extends CONNECTBaseClient<T> {

	private static final Logger log = Logger.getLogger(CONNECTCXFClient.class);
	
	protected ServiceEndpoint<T> serviceEndpoint = null;

    protected CONNECTCXFClient(ServicePortDescriptor<T> portDescriptor, String url, AssertionType assertion) {
        this(portDescriptor, url, assertion, new CXFServicePortBuilder<T>(portDescriptor));
    }

    protected CONNECTCXFClient(ServicePortDescriptor<T> portDescriptor, String url, AssertionType assertion,
            ServicePortBuilder<T> portBuilder) {
        serviceEndpoint = super.configureBasePort(portBuilder.createPort(), url);
    }

    protected CONNECTCXFClient(ServicePortDescriptor<T> portDescriptor, String url, AssertionType assertion,
            ServicePortBuilder<T> portBuilder, String subscriptionId) {
        serviceEndpoint = super.configureBasePort(portBuilder.createPort(), subscriptionId);
    }
   

    @Override
    public T getPort() {
        return serviceEndpoint.getPort();
    }
    
    @Override
    public void enableMtom() {
        serviceEndpoint = new MTOMServiceEndpointDecorator<T>(serviceEndpoint);
        serviceEndpoint.configure();
    }

    @Override
    public void enableWSA(AssertionType assertion, String wsAddressingTo, String wsAddressingActionId) {
        serviceEndpoint = new WsAddressingServiceEndpointDecorator<T>(serviceEndpoint, wsAddressingTo, wsAddressingActionId, assertion);
        serviceEndpoint.configure();
    }
    
    @Override
    public void setOutboundHeaders(List<Header> outboundHeaders) {
    	log.debug("Start CONNECTCXFClient - setOutboundHeaders(...)");
    	if(NullChecker.isNotNullish(outboundHeaders)) {
    		BindingProvider bindingProviderPort = (BindingProvider)serviceEndpoint.getPort();
    		List<Header> headerList = new ArrayList<Header>();
    		Object existingHeaders = bindingProviderPort.getRequestContext().get(Header.HEADER_LIST);
    		if(existingHeaders != null) {
    			log.debug("Existing header object was not null. Type: " + existingHeaders.getClass().getName());
    			if(List.class.isAssignableFrom(existingHeaders.getClass())) {
    				log.debug("Existing header object was a list");
    				@SuppressWarnings("rawtypes")
					List existingHeadersList = (List)existingHeaders;
    				for (Object existingHeaderObject : existingHeadersList) {
    					log.debug("Looking at an existing header item of type: " + existingHeaderObject.getClass().getName());
    					if(Header.class.isAssignableFrom(existingHeaderObject.getClass())) {
    						log.debug("Existing header object was of type Header - adding to the header list");
    						headerList.add((Header)existingHeaderObject);
    					}
    				}
    			}
    		}
			headerList.addAll(outboundHeaders);
    		bindingProviderPort.getRequestContext().put(Header.HEADER_LIST, outboundHeaders);
    	} else {
    		log.debug("No outbound headers to add");
    	}
    	log.debug("End CONNECTCXFClient - setOutboundHeaders(...)");
    }

}
