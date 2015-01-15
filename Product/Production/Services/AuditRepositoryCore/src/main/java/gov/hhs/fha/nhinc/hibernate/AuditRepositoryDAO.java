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

import gov.hhs.fha.nhinc.hibernate.util.HibernateUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Expression;

/**
 * AuditRepositoryDAO Class provides methods to query and update Audit Data to/from MySQL Database using Hibernate
 * 
 * @author svalluripalli
 */
public class AuditRepositoryDAO {
    // Log4j logging initiated
    private static final Logger LOG = Logger.getLogger(AuditRepositoryDAO.class);
    public static String JAVA_IO_TMPDIR = "java.io.tmpdir";

    /**
     * Constructor
     */
    public AuditRepositoryDAO() {
        LOG.info("AuditRepositoryDAO - Initialized");
    }

    /**
     * 
     * @param query
     * @param whereClause
     * @return List<AuditRepositoryRecord>
     */
    public List<AuditRepositoryRecord> queryAuditRepository(String query) {
        LOG.debug("Entering..." + this.getClass().getName() + ".queryAuditRepository()");

        Session session = null;

        List<AuditRepositoryRecord> queryList = null;
        try {
            SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
            session = sessionFactory.openSession();
            LOG.info("Getting Record");
            queryList = session.createSQLQuery(query).addEntity("queryAuditRepository()", AuditRepositoryRecord.class).list();
        } 
    	catch(Exception e) {
    		LOG.error(this.getClass().getName() + ".queryAuditRepository()" + "Exception thrown");
    		LOG.error(e.getMessage());
    	}
        finally {
            if (session != null) {
                try {
                	session.close();
                } 
                catch (Throwable t) {
                    LOG.warn(this.getClass().getName() + ".queryAuditRepository(): Failed to close session: " + t.getMessage(), t);
                }
            }
        }

        LOG.debug("Exiting...." + this.getClass().getName() + ".queryAuditRepository()");
        return queryList;
    }

    /**
     * 
     * @param auditRepositoryRecord
     * @return record id for the new record as a Long
     */
    public Long insertAuditRepositoryRecord(AuditRepositoryRecord auditRec) {
        Session session = null;
        Transaction  tx = null;
        Long id = null;
        Serializable saveRet= null;
        
        LOG.debug("Entering..." + this.getClass().getName() + ".insertAuditRepositoryRecord()");
        if (auditRec == null) {
        	return id;
        }
        try {
            SessionFactory oSessionFactory = HibernateUtil.getSessionFactory();
            if (oSessionFactory != null) {
            	session = oSessionFactory.openSession();
            }
            else {
            	 LOG.error(this.getClass().getName() + "..insertAuditRepositoryRecord(): Failed to obtain a session from the sessionFactory");
            }
            if (session != null) {
               	tx = session.beginTransaction();
            }
            else {
			   	LOG.error(this.getClass().getName() + "..insertAuditRepositoryRecord(): Failed to obtain a session factory");
	        }
            auditRec.setTimeStamp(new Date());
            AdvancedAuditRecord adv = auditRec.getAdvancedAuditRecord();

            saveRet = session.save(auditRec);
            Class c = saveRet.getClass();
            if (c.getName().equals("java.lang.Long")) {
                id = (Long) saveRet;
            }
            tx.commit();
        } 
        catch (Exception e) {
            if (tx != null) {
            	id = null;
                tx.rollback();
            }
            LOG.error(this.getClass().getName() + ".insertAuditRepositoryRecord():Error during audit record persistence caused by :" + e.getMessage());
        } 
        finally {
            // Actual event_log insertion will happen at this step
            if (session != null) {
                session.close();
            }
        }
        LOG.debug("Exiting..." + this.getClass().getName() + ".insertAuditRepositoryRecord()");
        return id;
    }
    
    /**
     * Save a list of auditRepositoryRecords
     * 
     * this never happens in the real world (I guess I should say where list size > 1), 
     * but hey this is the way it was always done until 2013!
     * 
     * @param auditList
     * @return boolean
     */
    public boolean insertAuditRepository(List<AuditRepositoryRecord> auditList) {
        Session session = null;
        Transaction  tx = null;
        boolean result = true;

        LOG.debug("Entering..." + this.getClass().getName() + ".insertAuditRepository()");
        if (auditList != null && auditList.size() > 0) {
            int size = auditList.size();
            AuditRepositoryRecord auditRecord = null;

        try {
            SessionFactory oSessionFactory = HibernateUtil.getSessionFactory();
            if (oSessionFactory != null) {
            	session = oSessionFactory.openSession();
            }
            else {
            	LOG.error(this.getClass().getName() + "..insertAuditRepository(): Failed to obtain a session from the sessionFactory");
            }
            if (session != null) {
               	tx = session.beginTransaction();
            }
            else {
            	LOG.error(this.getClass().getName() + "..insertAuditRepository(): Failed to obtain a session factory");
            }
            // Loop through the list and save each record.
            for (int i = 0; i < size; i++) {
                auditRecord = (AuditRepositoryRecord) auditList.get(i);
                auditRecord.setTimeStamp(new Date());
                session.save(auditRecord);
            }
            tx.commit();
        } 
        catch (Exception e) {
            result = false;
            if (tx != null) {
                tx.rollback();
            }
            LOG.error(this.getClass().getName() + ".insertAuditRepository():Error during audit record persistence caused by :" + e.getMessage());
        } 
        finally {
                // Actual event_log insertion will happen at this step
                if (session != null) {
                    session.close();
                }
            }
        }
        LOG.debug("Exiting..." + this.getClass().getName() + ".insertAuditRepository()");
        return result;
    }

    /**
     * 
     */
    public List<AuditRepositoryRecord> queryOnLikeUserId(String userId){
    	List<AuditRepositoryRecord> results = null;
    	Session session = null;
    	
    	LOG.debug("Entering..." + this.getClass().getName() + ".queryOnLikeUserId");
    	if (userId == null || userId.isEmpty()) {
    		// Impossible to match
    		return results;
    	}
        try {
            SessionFactory oSessionFactory = HibernateUtil.getSessionFactory();
            if (oSessionFactory != null) {
            	session = oSessionFactory.openSession();
                if (session == null) {
				  LOG.error(this.getClass().getName() + "..queryOnLikeUserId(): Failed to obtain a session from the sessionFactory");
                }
            } 
            else {
            	LOG.error(this.getClass().getName() + "..queryOnLikeUserId(): Failed to obtain a session factory");
            }
			Criteria aCriteria = session.createCriteria(AuditRepositoryRecord.class);
			aCriteria.add(Expression.ilike(userId, "test"));
			results = aCriteria.list();
        } 
    	catch(Exception e) {
    		LOG.error(this.getClass().getName() + ".queryOnLikeUserId()" + "Exception thrown");
    		LOG.error(e.getMessage());
    	}
        finally {
            if (session != null) {
                try {
                	session.close();
                } 
                catch (Throwable t) {
                    LOG.warn(this.getClass().getName() + ".queryOnLikeUserId(): Failed to close session: " + t.getMessage(), t);
                }
            }
        }
        return results;
    }
    /**
     * This method does a query to database to get the Audit Log Messages based on user id and/or patient id and/or
     * community id and/or timeframe
     * 
     * @param eUserId
     * @param ePatientId
     * @param startDate
     * @param endDate
     * @return List
     */
    public List<AuditRepositoryRecord> queryAuditRepositoryOnCriteria(String eUserId, String ePatientId, Date startDate, Date endDate) {
        LOG.debug("AuditRepositoryDAO.getAuditRepositoryOnCriteria() Begin");

        if (eUserId == null && ePatientId == null && startDate == null) {
            LOG.info("-- No - Input Parameters found for Audit Query --");
            LOG.debug("AuditRepositoryDAO.getAuditRepositoryOnCriteria() End");
            return null;
        }

        Session session = null;
        List<AuditRepositoryRecord> queryList = null;
        try {
            SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
            session = sessionFactory.openSession();
            LOG.info("Getting Record");

            // Build the criteria
            Criteria aCriteria = session.createCriteria(AuditRepositoryRecord.class);
            if (eUserId != null && !eUserId.equals("")) {
                aCriteria.add(Expression.eq("userId", eUserId));
            }
            if (ePatientId != null && !ePatientId.equals("")) {
                aCriteria.add(Expression.eq("receiverPatientId", ePatientId));
            }

            if (startDate != null && endDate != null) {
                aCriteria.add(Expression.between("timeStamp", new Date(startDate.getTime()),
                        new Date(endDate.getTime())));
            } else if (startDate != null && endDate == null) {
                aCriteria.add(Expression.ge("timeStamp", new Date(startDate.getTime())));
            }
            queryList = aCriteria.list();
        }
        catch(Exception e) {
        		LOG.error(this.getClass().getName() + ".queryOnLikeUserId()" + "Exception thrown");
        		LOG.error(e.getMessage());
        }
        finally {
            if (session != null) {
                try {
                	session.close();
                } 
                catch (Throwable t) {
                    LOG.warn(this.getClass().getName() + ".queryOnLikeUserId(): Failed to close session: " + t.getMessage(), t);
                }
            }
        }
        LOG.debug("AuditRepositoryDAO.getAuditRepositoryOnCriteria() End");
        return queryList;
    }
    
    public AuditRepositoryRecord findById(Long id) {

        AuditRepositoryRecord auditRecord = null;
        Session oSession = null;

        LOG.debug("Begin..." + this.getClass().getName() + ".findById()");

        try {
            SessionFactory oSessionFactory = HibernateUtil.getSessionFactory();
            if (oSessionFactory != null) {
                oSession = oSessionFactory.openSession();
                if (oSession != null) {
                    auditRecord = (AuditRepositoryRecord) oSession.get(AuditRepositoryRecord.class, id);
                } else {
                    LOG.error(this.getClass().getName() + ".findById()" + "Failed to obtain a session from the sessionFactory");
                }
            } else {
                LOG.error(this.getClass().getName() + ".findById()" + "Session factory was null");
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug(this.getClass().getName() + ".findById()" + "Completed AuditRepositoryRecord retrieve by id (" + id + "). Result was " + ((auditRecord == null) ? "not " : "") + "found");
            }
        } 
        catch (Exception e) {
        	LOG.error(this.getClass().getName() + ".findById()" + "General exception thrown while processing.");
        	e.printStackTrace();
        }
        finally {
            if (oSession != null) {
                try {
                    oSession.close();
                } catch (Throwable t) {
                    LOG.warn(this.getClass().getName() + ".findById()" + "Failed to close session: " + t.getMessage(), t);
                }
            }
        }
        LOG.debug("Exiting..." + this.getClass().getName() + ".findById()");
        return auditRecord;
    }
}
