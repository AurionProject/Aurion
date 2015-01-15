package gov.hhs.fha.nhinc.webserviceproxy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import gov.hhs.fha.nhinc.properties.IPropertyAcessor;

import java.net.SocketTimeoutException;

import javax.xml.ws.WebServiceException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This class tests the retry "Socketimeout" code of the WebServiceProxyHelper class.
 * 
 * @author Greg Gurr
 */
public class WebServiceProxyHelperSocktimeoutTest {
	private final int RETRY_ATTEMPTS = 2;
	private final int RETRY_DELAY_MS = 1000;
	private final String RETRY_EXCEPTION_TEXT = "SocketTimeoutException";
	private final String UNSUPPORTED_ERROR_MESSAGE = "Unsupported error message";
	private final String SOCKET_TIMEOUT_ERROR_MESSAGE = "Supported socketTimeout error message";
	
	private WebServiceProxyHelper testSubject;
	private IPropertyAcessor mockPropertyAccessor;
		
	@Before
	public void setUp() throws Exception {
		mockPropertyAccessor = mock(IPropertyAcessor.class);
		
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
	}

	/**
	 * Test the retry logic when we have a "SocketTimeoutException". This method will attempt to retry invoking the port for the
	 * specified retry attempts. After that an "InvocationTargetException" should be thrown.
	 */
	@SuppressWarnings("unused")
	@Test
	public void testInvokePort_RetrySocketTimeoutException() {		
		try {
			Integer response = (Integer) testSubject.invokePort(this, this.getClass(), "supportedExceptionMethod", new Integer(100));
			fail("A Exception should have been thrown but was not.");
		} catch (Exception e) {			
			assertTrue("Thrown exception should be InvocationTargetException but was not", e instanceof WebServiceException);
			assertEquals(SOCKET_TIMEOUT_ERROR_MESSAGE, e.getMessage());
		}			
	}

	
	/**
	 * Test the retry logic when we have a "SocketTimeoutException". This method will attempt to retry invoking the port for the
	 * specified retry attempts. After that an "InvocationTargetException" should be thrown.
	 */
	@SuppressWarnings("unused")
	@Test
	public void testInvokePort_RetryUnSupportedException() {		
		try {
			Integer response = (Integer) testSubject.invokePort(this, this.getClass(), "unSupportedExceptionMethod", new Integer(100));	
			fail("A Exception should have been thrown but was not.");	
		} catch (Exception e) {			
			assertTrue("Thrown exception should be InvocationTargetException but was not", e instanceof WebServiceException);
			assertEquals(UNSUPPORTED_ERROR_MESSAGE, e.getMessage());
		}			
	}	
	
	
	/**
	 * Test the retry logic when we have no Exceptions. This method will invoke and return a valid value..
	 */
	@Test
	public void testInvokePort_RetryHappyPath() {		
		try {
			Integer response = (Integer) testSubject.invokePort(this, this.getClass(), "helperMethod", new Integer(100));	
			
			assertNotNull(response);
			assertEquals(100, response.intValue());			
		} catch (Exception e) {	
			fail("Should not have thrown an Exception but did");
		}			
	}		
	
	
	
    /**
     * This method is used to test out some of the dynamic invocation methods.
     * 
     * @param x 
	 *		Contains some Integer param.
	 *
	 * @return
	 * 		Returns the param that was passed in.
     */
    public Integer helperMethod(Integer x) {
        return x;
    }	
	
    
    /**
     * This method is used to test out some of the dynamic invocation methods. It is used to test
     * when a "invokePort" call throws a non-SocketTimeoutException.
     * 
     * @param x 
	 *		Contains some Integer param.
	 *
	 * @return
	 * 		Returns the param that was passed in.
     * @throws Fault 
	 * @exception
	 * 		Throws a Fault.
     */    
    public Integer unSupportedExceptionMethod(Integer x) throws WebServiceException {
    	IllegalArgumentException illegalArgumentException = new IllegalArgumentException(UNSUPPORTED_ERROR_MESSAGE);   	
    	
        throw new WebServiceException(UNSUPPORTED_ERROR_MESSAGE, illegalArgumentException);
    }
    
    
    /**
     * This method is used to test out some of the dynamic invocation methods. It is used to test
     * when a "invokePort" call throws a SocketTimeoutException.
     * 
     * @param x 
	 *		Contains some Integer param.
	 *
	 * @return
	 * 		Returns the param that was passed in.
     * @throws Fault 
	 * @exception
	 * 		Throws a Fault.
     */     
    public Integer supportedExceptionMethod(Integer x) throws WebServiceException {
    	SocketTimeoutException socketTimeoutException = new SocketTimeoutException(SOCKET_TIMEOUT_ERROR_MESSAGE);   	
    	
        throw new WebServiceException(SOCKET_TIMEOUT_ERROR_MESSAGE, socketTimeoutException);
    }    
    
	
	
}
