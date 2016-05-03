package gov.hhs.fha.nhinc.docquery.entity;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.hl7.v3.II;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import gov.hhs.fha.nhinc.nhinclib.NullChecker;

/**
 * This class contains all junit tests for the class "DocQueryCorrelationFilter".
 * 
 * @author Greg Gurr
 */
public class DocQueryCorrelationFilterTest {

	private final String AAID_1 = "1.1";
	private final String AAID_2 = "2.2";
	private final String AAID_3 = "3.3";
	private final String AAID_4 = "4.4";
	
	private final String PATID_1 = "H111";
	private final String PATID_2 = "D456";
	private final String PATID_3 = "H777";
	private final String PATID_4 = "D500";
	
	private final int SAMPLE_CORRELATION_SET_SIZE = 4;
	
	private DocQueryCorrelationFilter testSubject;
	
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testFilteringNotEnabled_NullCorrelations() {
		Set<II> correlations = null;
		boolean filterEnabled = false;
		
		testSubject = createTestSubject(filterEnabled);
		
		Set<II> results = testSubject.filterDuplicateCorrelationsByAssigningAuthority(correlations);
		
		assertNull(results);
	}
	
	@Test
	public void testFilteringNotEnabled_NonNullCorrelations() {
		Set<II> correlations = createSampleCorrelationsWithOutDuplicate();
		boolean filterEnabled = false;
		
		testSubject = createTestSubject(filterEnabled);
		
		Set<II> results = testSubject.filterDuplicateCorrelationsByAssigningAuthority(correlations);
		
		assertNotNull(results);
		assertEquals(SAMPLE_CORRELATION_SET_SIZE, results.size());
		assertCorrelations(correlations, results);	
	}	
	
	@Test
	public void testFilteringEnabled_NullCorrelations() {
		Set<II> correlations = null;
		boolean filterEnabled = true;
		
		testSubject = createTestSubject(filterEnabled);
		
		Set<II> results = testSubject.filterDuplicateCorrelationsByAssigningAuthority(correlations);
		
		assertNotNull(results);
		assertEquals(0, results.size());
	}		
	
	@Test
	public void testFilteringEnabled_CorrelationsExistNoDuplicates() {
		Set<II> correlations = createSampleCorrelationsWithOutDuplicate();
		boolean filterEnabled = true;
		
		testSubject = createTestSubject(filterEnabled);
		
		Set<II> results = testSubject.filterDuplicateCorrelationsByAssigningAuthority(correlations);
		
		assertNotNull(results);
		assertEquals(SAMPLE_CORRELATION_SET_SIZE, results.size());
		assertCorrelations(correlations, results);	
	}		
	
	@Test
	public void testFilteringEnabled_CorrelationsExistWithDuplicate() {
		Set<II> correlations = createSampleCorrelationsWithDuplicate();
		boolean filterEnabled = true;
		
		testSubject = createTestSubject(filterEnabled);
		
		Set<II> results = testSubject.filterDuplicateCorrelationsByAssigningAuthority(correlations);
		
		assertNotNull(results);
		assertEquals(SAMPLE_CORRELATION_SET_SIZE - 1, results.size());
		assertCorrelations(correlations, results);	
	}			
	
	@Test
	public void testFilteringEnabled_CorrelationsExistNoDuplicatesAndNullCorrelationItem() {
		Set<II> correlations = createSampleCorrelationsWithOutDuplicate();
		boolean filterEnabled = true;
		
		correlations.add(null);
		
		testSubject = createTestSubject(filterEnabled);
		
		Set<II> results = testSubject.filterDuplicateCorrelationsByAssigningAuthority(correlations);
		
		assertNotNull(results);
		assertEquals(SAMPLE_CORRELATION_SET_SIZE, results.size());
		assertCorrelations(correlations, results);	
	}		
	
	@Test
	public void testFilteringEnabled_EmptyCorrelations() {
		Set<II> correlations = new HashSet<II>();
		boolean filterEnabled = true;
		
		testSubject = createTestSubject(filterEnabled);
		
		Set<II> results = testSubject.filterDuplicateCorrelationsByAssigningAuthority(correlations);
		
		assertNotNull(results);
		assertEquals(0, results.size());
	}	
	


	//-------------------------------------------------------------------------------------------------------
	// Private methods
	//-------------------------------------------------------------------------------------------------------
	
	private void assertCorrelations(Set<II> expected, Set<II> actual) {
		if ((expected != null) && (actual == null)) {
			fail("Expected correlations are not null and actual correlation are null.");
		}
		
		if ((expected == null) && (actual != null)) {
			fail("Expected correlations are null and actual correlation are not null.");
		}
		
		if ((expected != null) && (actual != null)) {
			
			assertTrue(expected.containsAll(actual));			
			assertNoDuplicateResults(actual);
		}
	}	
	
	private void assertNoDuplicateResults(Set<II> resultCorrelations) {
		Set<String> encounteredAssigningAuthoritIds = new HashSet<String>();
		
		for (II correlationItem : resultCorrelations) {
			if ((correlationItem != null) && (NullChecker.isNotNullish(correlationItem.getRoot()))) {
				if (encounteredAssigningAuthoritIds.contains(correlationItem.getRoot())) {
					fail("Correlation results contained duplicate assigning authority id: " + correlationItem.getRoot());					
				} else {
					encounteredAssigningAuthoritIds.add(correlationItem.getRoot());
				}				
			}
		}		
	}

	/**
	 * Create a sample correlation set.
	 * 
	 * @return
	 * 		Returns a sample correlation set.
	 */
	private Set<II> createSampleCorrelationsWithDuplicate() {
		Set<II> sampleCorrelations = new HashSet<II>();
		II correlation1 = new II();
		II correlation2 = new II();
		II correlation3 = new II();
		II correlation4 = new II();

		correlation1.setRoot(AAID_1);
		correlation1.setExtension(PATID_1);
		
		correlation2.setRoot(AAID_2);
		correlation2.setExtension(PATID_2);		
		
		correlation3.setRoot(AAID_1);
		correlation3.setExtension(PATID_3);			
		
		correlation4.setRoot(AAID_3);
		correlation4.setExtension(PATID_4);	
		
		sampleCorrelations.add(correlation1);
		sampleCorrelations.add(correlation2);
		sampleCorrelations.add(correlation3);
		sampleCorrelations.add(correlation4);
		
		return sampleCorrelations;
	}

	/**
	 * Create a sample correlation set.
	 * 
	 * @return
	 * 		Returns a sample correlation set.
	 */
	private Set<II> createSampleCorrelationsWithOutDuplicate() {
		Set<II> sampleCorrelations = new HashSet<II>();
		II correlation1 = new II();
		II correlation2 = new II();
		II correlation3 = new II();
		II correlation4 = new II();

		correlation1.setRoot(AAID_1);
		correlation1.setExtension(PATID_1);
		
		correlation2.setRoot(AAID_2);
		correlation2.setExtension(PATID_2);		
		
		correlation3.setRoot(AAID_3);
		correlation3.setExtension(PATID_3);			
		
		correlation4.setRoot(AAID_4);
		correlation4.setExtension(PATID_4);	
		
		sampleCorrelations.add(correlation1);
		sampleCorrelations.add(correlation2);
		sampleCorrelations.add(correlation3);
		sampleCorrelations.add(correlation4);
		
		return sampleCorrelations;
	}	
		
	/**
	 * Create a testSubject instance.
	 * 
	 * @param filterEnabled
	 * 		True if filtering is "enabled", false otherwise.
	 * @return
	 * 		Returns an instance of a "DocQueryCorrelationFilter" object.
	 */
	private DocQueryCorrelationFilter createTestSubject(boolean filterEnabled) {		
		if (filterEnabled) {
			
			return new DocQueryCorrelationFilter() {
				@Override
				protected boolean filterEnabled() {
					return true;
				}
			};					
		} else {
			
			return new DocQueryCorrelationFilter() {
				@Override
				protected boolean filterEnabled() {
					return false;
				}
			};	
		}				
	}
	

}

