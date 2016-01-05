/**
 * 
 */
package gov.hhs.fha.nhinc.messaging.client;

import java.util.List;

import com.sun.xml.ws.api.message.Header;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.messaging.service.ServiceEndpoint;
import gov.hhs.fha.nhinc.messaging.service.decorator.MTOMServiceEndpointDecorator;
import gov.hhs.fha.nhinc.messaging.service.decorator.cxf.WsAddressingServiceEndpointDecorator;
import gov.hhs.fha.nhinc.messaging.service.port.CXFServicePortBuilder;
import gov.hhs.fha.nhinc.messaging.service.port.ServicePortBuilder;
import gov.hhs.fha.nhinc.messaging.service.port.ServicePortDescriptor;

/**
 * @author bhumphrey
 * 
 */
public abstract class CONNECTCXFClient<T> extends CONNECTBaseClient<T> {

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
    	((com.sun.xml.ws.developer.WSBindingProvider)getPort()).setOutboundHeaders(outboundHeaders);
    }

}
