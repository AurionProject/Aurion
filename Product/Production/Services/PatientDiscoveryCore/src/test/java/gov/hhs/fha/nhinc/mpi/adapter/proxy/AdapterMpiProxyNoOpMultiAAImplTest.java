/**
 * 
 */
package gov.hhs.fha.nhinc.mpi.adapter.proxy;

import static org.junit.Assert.*;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;

import org.hl7.v3.PRPAIN201305UV02;
import org.hl7.v3.PRPAIN201306UV02;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This tests the AdapterMpiProxyNoOpMultiAAimpl class.  The test does some significant processing so to unmarshal an HL7 message so it
 * will not be turned on by default.  But can turned on if needed for debugging...
 * 
 * @author Les Westberg
 *
 */
@Ignore
public class AdapterMpiProxyNoOpMultiAAImplTest {

	/**
	 * Test method for {@link gov.hhs.fha.nhinc.mpi.adapter.proxy.AdapterMpiProxyNoOpMultiAAImpl#findCandidates(org.hl7.v3.PRPAIN201305UV02, gov.hhs.fha.nhinc.common.nhinccommon.AssertionType)}.
	 */
	@Test
	@Ignore
	public void testFindCandidates() {
		AdapterMpiProxyNoOpMultiAAImpl oAdapterMpiProxyNoOpMultiAA = new AdapterMpiProxyNoOpMultiAAImpl();
		PRPAIN201305UV02 oPDRequest = new PRPAIN201305UV02();
		AssertionType oAssertion = new AssertionType();
		
		PRPAIN201306UV02 oPDResponse = oAdapterMpiProxyNoOpMultiAA.findCandidates(oPDRequest, oAssertion);
		
		assertNotNull(oPDResponse);
		assertNotNull(oPDResponse.getControlActProcess());
		assertNotNull(oPDResponse.getControlActProcess().getSubject());
		assertEquals(3, oPDResponse.getControlActProcess().getSubject().size());
		
	}

}
