package gov.hhs.fha.nhinc.callback;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.log4j.Logger;

public class MustUnderStandSoapHeaderHandler implements SOAPHandler<SOAPMessageContext> {

	private static final Logger LOG = Logger.getLogger(MustUnderStandSoapHeaderHandler.class);
    private static final String WSA_NS = "http://www.w3.org/2005/08/addressing";
    private static final String ACTION_ELEMENT_NAME = "Action";
    private static final String MUST_UNDERSTAND_ATTRIBUTE_NAME = "mustUnderstand";
    private static final String MUST_UNDERSTAND_VALUE_TRUE = "true";

    @Override
	public boolean handleMessage(SOAPMessageContext messageContext) {
        LOG.info("Entering MustUnderStandSoapHeaderHandler.handleMessage");
        Boolean isOutboundMessage = (Boolean) messageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        try {
            SOAPHeader soapHeader = messageContext.getMessage().getSOAPHeader();

            if (isOutboundMessage.booleanValue()) {
                addMustUnderstand(soapHeader);
            } else {
                LOG.debug("Will not adjust messageID on inbound request");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
        LOG.debug("Leave MustUnderStandSoapHeaderHandler.handleMessage");
		return true;
	}

	private void addMustUnderstand(SOAPHeader oHeader) throws SOAPException {
        SOAPElement actionElement = getFirstChild(oHeader, ACTION_ELEMENT_NAME, WSA_NS);
        if (actionElement != null) {
            LOG.debug("Found the Action header element - checking for existing mustUnderstand attribute");
            if(actionElement.hasAttribute(MUST_UNDERSTAND_ATTRIBUTE_NAME)) {
            	LOG.debug("mustUnderstand already present - no action performed");
            } else {
            	LOG.debug("Did not find mustUnderstand attribute - adding");
            	QName mustUnderstandQName = new QName(MUST_UNDERSTAND_ATTRIBUTE_NAME);
            	actionElement.addAttribute(mustUnderstandQName, MUST_UNDERSTAND_VALUE_TRUE);
            }
        } else {
            LOG.debug("Did not find the Action header element - no action performed");
        }
	}

    /**
     * Returns a header object with a particular local name and namespace.
     * @param header The header object from the message
     * @param name The local name of the element being searched for
     * @param ns The namespace of the object being searched for
     * @return The first instance that matches the localname and namespace or return null
     */
    @SuppressWarnings("rawtypes")
	private SOAPElement getFirstChild(SOAPHeader header, String name, String ns) {
        SOAPElement result = null;
        if (header == null || !header.hasChildNodes()) {
            return result;
        }

        QName qname = new QName(ns, name);
        Iterator iter = header.getChildElements(qname);
        if (iter.hasNext()) {
            result = (SOAPElement) iter.next();
        }
        return result;
    }

	@Override
	public boolean handleFault(SOAPMessageContext context) {
        LOG.warn("MustUnderStandSoapHeaderHandler.handleFault");
        return true;
	}

	@Override
	public void close(MessageContext context) {
        LOG.debug("MustUnderStandSoapHeaderHandler.close");
	}

	@Override
	public Set<QName> getHeaders() {
        LOG.debug("MustUnderStandSoapHeaderHandler.getHeaders");
        return Collections.emptySet();
	}

}
