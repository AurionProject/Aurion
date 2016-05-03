package gov.hhs.fha.nhinc.docquery.entity;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hl7.v3.II;

import gov.hhs.fha.nhinc.properties.PropertyAccessor;

/**
 * This class optionally gets called to filter document query correlation results BEFORE executing the targets for a document query.
 * It filters out entries that have the same "assigning authority id".
 * 
 * @author Greg Gurr
 */
public class DocQueryCorrelationFilter {
    private static final String PROPERTIES_FILE_GATEWAY = "gateway";
    private static final String PROPERTY_KEY_DQ_CORR_FILTER = "documentQueryOutboundCorrelationFilterEnabled";
    private static final Logger LOG = Logger.getLogger(DocQueryCorrelationFilter.class);	
	private Set<String> correlatedAssigningAuthorityIds;
	

    /**
     * Determines if document query filtering is enabled or not.
     * 
     * @return
     * 		Returns "true" if document query filtering is enabled, "false" otherwise.
     */
    protected boolean filterEnabled() {
        boolean filterEnabled = false;
        
        try {
            filterEnabled = PropertyAccessor.getInstance().getPropertyBoolean(PROPERTIES_FILE_GATEWAY,  PROPERTY_KEY_DQ_CORR_FILTER);                  
        } catch(Exception e) {
        	LOG.error("Error encountered looking up " +
                    PROPERTY_KEY_DQ_CORR_FILTER + " property in the " +
                    PROPERTIES_FILE_GATEWAY + " properties file: " +
                    e.getMessage(), e);
        }
        
        return filterEnabled;
    }	
	
	/**
	 * Filters the passed in correlations by assigning authority id. If a duplicate is found, the first item encountered will be used.
	 * 
	 * @param correlations
	 * 		Contains the correlations to be filtered.
	 * @return
	 * 		Returns a "Set" of "II" objects having duplicates removed.
	 */
	public Set<II> filterDuplicateCorrelationsByAssigningAuthority(Set<II> correlations) {	
		
        if(!filterEnabled()) {
            return correlations;
        }		
		
        Set<II> filteredCorrelations = new HashSet<II>();
        
    	logCorrelations(correlations, "\nBEFORE filterDuplicateCorrelationsByAssigningAuthority:\n");
        
        if (correlationsExists(correlations)) {
        	correlatedAssigningAuthorityIds = new HashSet<String>();
        	
        	for (II correlationItem : correlations) {
        		if (correlationItem != null) {
            		String assignAuthorityId = correlationItem.getRoot();
            		
            		if (!assigningAuthorityIdAlreadyEncountered(assignAuthorityId)) {
            			filteredCorrelations.add(correlationItem);
            			
            			correlatedAssigningAuthorityIds.add(assignAuthorityId);					
    				}				
				}
			}			
		}
        
    	logCorrelations(filteredCorrelations, "\nAFTER filterDuplicateCorrelationsByAssigningAuthority:\n");
        
		return filteredCorrelations;
	}
	


	/**
	 * Determines whether we have already encounters the passed in assigning authority id or not.
	 * 
	 * @param assignAuthorityId
	 * 		Contains an assigning authority id.
	 * @return
	 * 		Returns true if we have already encountered this assigning authority if, false otherwise.
	 */
	private boolean assigningAuthorityIdAlreadyEncountered(String assignAuthorityId) {
		return correlatedAssigningAuthorityIds.contains(assignAuthorityId);
	}

	/**
	 * Determines if correlations exist in the passed in set.
	 * 
	 * @param correlations
	 * 		Contains correlations.
	 * @return
	 * 		Returns true if correlations exist, false otherwise.
	 */
	private boolean correlationsExists(Set<II> correlations) {
		return ((correlations != null) && (correlations.size() > 0));
	}

	private void logCorrelations(Set<II> correlations, String titleText) {
		StringBuilder buf = new StringBuilder(titleText);

		if (correlations != null && correlations.size() > 0) {
			for (II ii : correlations) {
				if (ii != null) {
					buf.append("\tAAId: ").append(ii.getRoot()).append("  PatId: ").append(ii.getExtension()).append("\n");	
				} else {
					buf.append("\tCorrelation item is NULL\n");	
				}			
			}			
		} else {
			buf.append("\tCorrelations list was null or empty\n");
		}
		
		LOG.debug(buf.toString());
	}
	
	
	
}
