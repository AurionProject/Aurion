/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services. 
 * All rights reserved. 
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met: 
 *     * Redistributions of source code must retain the above 
 *       copyright notice, this list of conditions and the following disclaimer. 
 *     * Redistributions in binary form must reproduce the above copyright 
 *       notice, this list of conditions and the following disclaimer in the documentation 
 *       and/or other materials provided with the distribution. 
 *     * Neither the name of the United States Government nor the 
 *       names of its contributors may be used to endorse or promote products 
 *       derived from this software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY 
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package gov.hhs.fha.nhinc.hibernate;

import gov.hhs.fha.nhinc.auditrepository.AuditTestHelper;

import java.util.Date;
import java.util.List;

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
 * 
 * @author MFLYNN02
 * @author rhaslam  Enhancements
 */
// TODO: Move to an integration test
public class AuditRepositoryDAOTest {
    private AuditRepositoryDAO auditDAO = null;

    AuditRepositoryRecord record1 = null;
    AuditRepositoryRecord record2 = null;
    AuditRepositoryRecord record3 = null;
    AuditRepositoryRecord record4 = null;
    
    public AuditRepositoryDAOTest() {
    }

    protected AuditRepositoryDAO getAuditRepositoryDAO() {
    	auditDAO = (auditDAO != null ? auditDAO : new AuditRepositoryDAO());
    	return auditDAO;
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    	record1 = new AuditRepositoryRecord();
    	record2 = new AuditRepositoryRecord();
    	record3 = new AuditRepositoryRecord();
    	record4 = new AuditRepositoryRecord();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test the insert method on a list of entries
     * 
     * rbh - I doubt this ever happens in the real world!; but hey this was the original test in this file!
     */
    @Test
    @Ignore
    public void testInsertRecordList() {

    	auditDAO = getAuditRepositoryDAO();
        
    	// Read back all records to determine size of current table
        String queryAll = "select * from auditrepo.auditrepository";
        List<AuditRepositoryRecord> returnList = auditDAO.queryAuditRepository(queryAll);
        assertNotNull(returnList);
        int entries = returnList.size();
      	
    	List<AuditRepositoryRecord> recordList = AuditTestHelper.populateDummyRecords();
    	record1 = recordList.get(0);
    	record2 = recordList.get(1);
    	record3 = recordList.get(2);
    	
        assertNotNull(recordList);
        assertEquals(recordList.size(), 3);
        // Save the records to the DB
        boolean result = auditDAO.insertAuditRepository(recordList);
        assertTrue(result);
        
        // Read back all records
        returnList = auditDAO.queryAuditRepository(queryAll);
        assertNotNull(returnList);
        assertEquals(returnList.size(), entries + 3);
    }

    /**
     * Test of insertAuditRepository method, of class AuditRepositoryDAO.
     */
    @Test
    @Ignore
    public void testInsertAuditRecord() {
    	auditDAO = getAuditRepositoryDAO();

    	record4 = AuditTestHelper.populateDummyRecord();
    	
        Long result = auditDAO.insertAuditRepositoryRecord(record4);
        assertNotNull(result);
        assertTrue(result.longValue() > 0);
        
        // Query the DB for the new record
        AuditRepositoryRecord readBack = auditDAO.findById(result);
        assertEquals(record4.getCommunityId(), readBack.getCommunityId());
        assertEquals(record4.getEventId(), readBack.getEventId());
        assertEquals(record4.getMessageType(), readBack.getMessageType());
        assertEquals(record4.getParticipationIDTypeCode(), readBack.getParticipationIDTypeCode());
        assertEquals(record4.getParticipationTypeCodeRole(), readBack.getParticipationTypeCodeRole());
        assertEquals(record4.getPurposeOfUse(), readBack.getPurposeOfUse());
        assertEquals(record4.getReceiverPatientId(), readBack.getReceiverPatientId());
        assertEquals(record4.getSenderPatientId(), readBack.getSenderPatientId());
        assertEquals(record4.getUserId(), readBack.getUserId());

        try {
            long recordLength = record4.getMessage().length();
            long readBackLength = readBack.getMessage().length();
            assertEquals(recordLength, readBackLength);
            //byte[] recordBytes = record4.getMessage().getBytes(0L, (int)recordLength);
            //byte[] readBackBytes = readBack.getMessage().getBytes(0L, (int) readBackLength);
            //assertEquals(recordBytes, readBackBytes);
        }
        catch (Exception e){
        	System.out.println("Exception while processing dummy messaqes.\n");
        	e.printStackTrace();
        }
    }

    /**
     * Test of queryAuditRepositoryOnCriteria method, of class AuditRepositoryDAO.
     */
    @Test
    @Ignore
    public void testQueryAuditRepositoryOnCriteria() {
    	
    	auditDAO = getAuditRepositoryDAO();
    	// Populate and save a record
    	record4 = AuditTestHelper.populateDummyRecord();
        Long assignedId = auditDAO.insertAuditRepositoryRecord(record4);
        assertNotNull(assignedId);
        assertTrue(assignedId.longValue() > 0);
        
        String eUserId = "kscatterberg-zz-test-aa";
        String ePatientId = "77777^^^&1.1&ISO";
        Date startDate = null;
        Date endDate = null;
        List<AuditRepositoryRecord> result = auditDAO.queryAuditRepositoryOnCriteria(eUserId, ePatientId, startDate, endDate);
        assertNotNull(result);
        assertTrue(result.size() > 0);
        
    }

}
