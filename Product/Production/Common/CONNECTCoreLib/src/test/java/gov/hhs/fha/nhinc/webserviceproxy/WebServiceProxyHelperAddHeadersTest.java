package gov.hhs.fha.nhinc.webserviceproxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import gov.hhs.fha.nhinc.properties.IPropertyAcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;

import org.apache.cxf.headers.Header;
import org.apache.cxf.jaxb.JAXBDataBinding;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the "addHeaders" code of the WebServiceProxyHelper class.
 * 
 * @author Greg Gurr
 */
public class WebServiceProxyHelperAddHeadersTest {
	private final int RETRY_ATTEMPTS = 2;
	private final int RETRY_DELAY_MS = 1000;
	private final String RETRY_EXCEPTION_TEXT = "SocketTimeoutException";
	
	private WebServiceProxyHelper testSubject;
	private IPropertyAcessor mockPropertyAccessor;
	private BindingProvider mockBindingProvider;
	private Map<String, Object> requestContext;
		
	@Before
	public void setUp() throws Exception {
		mockPropertyAccessor = mock(IPropertyAcessor.class);
		mockBindingProvider = mock(BindingProvider.class);		
		requestContext = new HashMap<String, Object>();	
		
		testSubject = new WebServiceProxyHelper(mockPropertyAccessor) {
			
			@Override
			public int getRetryAttempts() {
				return RETRY_ATTEMPTS;				
			}
			
			@Override
			public int getRetryDelay() {
				return RETRY_DELAY_MS;
			}
			
			@Override
			public String getExceptionText() {
				return RETRY_EXCEPTION_TEXT;
			}						
		};		
	}

	@After
	public void tearDown() throws Exception {
		testSubject = null;	
		mockPropertyAccessor = null;
		mockBindingProvider = null;
	}
	
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddHeaders_WithNullPort() throws Exception {
		BindingProvider port = null;
		List<Header> headers = null;
		
		testSubject.addHeaders(port, headers);	
	}	
		
	
	@Test
	public void testAddHeaders_WithInvalidHeaders() {
		List<Header> headers = null;
		
		try {
			testSubject.addHeaders(mockBindingProvider, headers);
			
			fail("Should have thrown an IllegalArugmentException but did not.");
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}	
		
		// Try an empty list
		headers = new ArrayList<Header>();
		
		try {
			testSubject.addHeaders(mockBindingProvider, headers);
			
			fail("Should have thrown an IllegalArugmentException but did not.");
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
	}		
		
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddHeaders_WithNullRequestContext() throws Exception {
		List<Header> headers = createSampleHeaderList();
		
		when(mockBindingProvider.getRequestContext()).thenReturn(null);
		
		testSubject.addHeaders(mockBindingProvider, headers);	
		
		verify(mockBindingProvider).getRequestContext();
	}	
	
	
	@Test
	public void testAddHeaders_HappyPath() throws Exception {
		List<Header> headers = createSampleHeaderList();
				
		when(mockBindingProvider.getRequestContext()).thenReturn(requestContext);
		
		testSubject.addHeaders(mockBindingProvider, headers);	
		
		verify(mockBindingProvider, times(2)).getRequestContext();
		
		assertNotNull(mockBindingProvider.getRequestContext());
		assertNotNull(mockBindingProvider.getRequestContext().get(Header.HEADER_LIST));
		assertEquals(1, mockBindingProvider.getRequestContext().size());
		
	}	
	
	
	
	/**
	 * Create a sample "Header" list.
	 * 
	 * @return
	 * 		Returns a list of "Header" objects.
	 * @throws JAXBException
	 */
	private List<Header> createSampleHeaderList() throws JAXBException {
		List<Header> headers = new ArrayList<Header>();
		Header dummyHeader = new Header(new QName("uri:org.apache.cxf", "dummy"), "decapitated",
                new JAXBDataBinding(String.class));	
		
		headers.add(dummyHeader);		
		
		return headers;
	}
	
	
}
