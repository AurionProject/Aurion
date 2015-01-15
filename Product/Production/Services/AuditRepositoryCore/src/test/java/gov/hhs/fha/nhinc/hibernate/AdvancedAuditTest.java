/**
 * Test package for the Advanced Audit Table
 * auditrepo.advanced_audit
 */
package gov.hhs.fha.nhinc.hibernate;

import gov.hhs.fha.nhinc.auditrepository.AuditTestHelper;

import org.junit.Test;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
/**
 * @author rhaslam
 *
 */
public class AdvancedAuditTest {
	AuditRepositoryDAO auditDAO = null;
	
    protected AuditRepositoryDAO getAuditRepositoryDAO() {
    	auditDAO = (auditDAO != null ? auditDAO : new AuditRepositoryDAO());
    	return auditDAO;
    }
    
	@Test
	public void insertBaseAndAdvanced() {
		auditDAO = getAuditRepositoryDAO();
		
		// Populate and link the records
		AuditRepositoryRecord base = AuditTestHelper.populateDummyBaseRecord();
		AdvancedAuditRecord advRecord = AuditTestHelper.populateDummyAdvancedRecord();
		
		// Link the base and advanced records
		base.setAdvancedAuditRecord(advRecord);
		advRecord.setAuditRepositoryRecord(base);
		
		// Save both records via Hibernate
		Long id = auditDAO.insertAuditRepositoryRecord(base);
		assertNotNull(id);
		assertTrue(id.longValue() > 0L);
		
        // Query the DB for the new record
        AuditRepositoryRecord readBack = auditDAO.findById(id);

        // Validate base record
        assertEquals(base.getCommunityId(), readBack.getCommunityId());
        assertEquals(base.getEventId(), readBack.getEventId());
        assertEquals(base.getMessageType(), readBack.getMessageType());
        assertEquals(base.getParticipationIDTypeCode(), readBack.getParticipationIDTypeCode());
        assertEquals(base.getParticipationTypeCodeRole(), readBack.getParticipationTypeCodeRole());
        assertEquals(base.getPurposeOfUse(), readBack.getPurposeOfUse());
        assertEquals(base.getReceiverPatientId(), readBack.getReceiverPatientId());
        assertEquals(base.getSenderPatientId(), readBack.getSenderPatientId());
        assertEquals(base.getUserId(), readBack.getUserId());
		
        //Validate advanced record
        AdvancedAuditRecord readBackAdv = readBack.getAdvancedAuditRecord();
        assertEquals(advRecord.getMessageDirection(), readBackAdv.getMessageDirection());
        assertEquals(advRecord.getMessageId(), readBackAdv.getMessageId());
        assertEquals(advRecord.getServiceName(), readBackAdv.getServiceName());
        assertEquals(advRecord.getSourceCommunity(), readBackAdv.getSourceCommunity());
        assertEquals(advRecord.getSourceSystem(), readBackAdv.getSourceSystem());
        assertEquals(advRecord.getUserName(), readBackAdv.getUserName());
        assertEquals(advRecord.getUserRoles(), readBackAdv.getUserRoles());
				
	}
}
