package gov.hhs.fha.nhinc.patientlocationquery;

import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.QualifiedSubjectIdentifierType;
import gov.hhs.fha.nhinc.common.patientcorrelationfacade.RetrievePatientCorrelationsRequestType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerCommunityMapping;
import gov.hhs.fha.nhinc.nhinclib.NullChecker;
import gov.hhs.fha.nhinc.patientcorrelation.nhinc.parsers.PRPAIN201309UV.PixRetrieveBuilder;
import gov.hhs.fha.nhinc.patientcorrelation.nhinc.proxy.PatientCorrelationProxy;
import gov.hhs.fha.nhinc.patientcorrelation.nhinc.proxy.PatientCorrelationProxyObjectFactory;
import gov.hhs.fha.nhinc.patientdiscovery.PatientDiscoveryException;
import gov.hhs.healthit.nhin.PatientLocationQueryFaultType;
import ihe.iti.xcpd._2009.PatientLocationQueryFault;
import ihe.iti.xcpd._2009.PatientLocationQueryRequestType;
import ihe.iti.xcpd._2009.PatientLocationQueryResponseType;
import ihe.iti.xcpd._2009.PatientLocationQueryResponseType.PatientLocationResponse;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hl7.v3.II;
import org.hl7.v3.PRPAIN201309UV02;
import org.hl7.v3.RetrievePatientCorrelationsResponseType;

/**
 * Class processes a PatientLocationQuery (ITI-56) request and returns a
 * PatientLocationQuery response.
 * 
 * @author Cindy Atherton
 * 
 */
public class PatientLocationQueryProcessor {

	private Log log = null;

	public PatientLocationQueryProcessor() {
		log = createLogger();
	}

	protected Log createLogger() {
		return ((log != null) ? log : LogFactory.getLog(getClass()));
	}

	/**
	 * Method to orchestrate the processing of the PatientLocationQueryRequest
	 * message (ITI-56) and the return of the Response message.
	 * 
	 * @param PatientLocationQueryRequestType
	 *            message
	 * @param assertion
	 * @returns PatientLocationQueryResponseType message
	 */
	public PatientLocationQueryResponseType processPatientLocationQuery(
			PatientLocationQueryRequestType request, AssertionType assertion)
			throws PatientLocationQueryFault, PatientDiscoveryException  {
		log.debug("Entering method processPatientLocationQuery...");

		PatientLocationQueryResponseType response = new PatientLocationQueryResponseType();
		
		if (request == null){
            log.error("Error: Request is null, returning empty response.");
            return response;
        }

		List<QualifiedSubjectIdentifierType> correlationsList = retrieveAllCorrelations(request, assertion);

		// populate the response message
		populateResponse(request.getRequestedPatientId(), response,
				correlationsList);

		log.debug("Returning "
				+ response.getPatientLocationResponse().size()
				+ " results in the PatientLocationQueryResponse from method processPatientLocationQuery...");
		return response;
	}

	/**
	 * Retrieves a list of patient correlations matching the RequestedPatientID
	 * from the PatientLocationQueryRequest message.
	 * 
	 * @param requestedPatientId
	 *            in HL7 II format
	 * @param assertion
	 * @return list of correlations, including HCID and corresponding patientId
	 * @throws PatientDiscoveryException
	 */
	protected List<QualifiedSubjectIdentifierType> retrieveAllCorrelations(
			PatientLocationQueryRequestType body, AssertionType assertion)
			throws PatientDiscoveryException, PatientLocationQueryFault {
		log.debug("Entering method retrieveCorrelations...");

		// Initialize the final list that contains that data to be returned
		List<QualifiedSubjectIdentifierType> finalList = new ArrayList<QualifiedSubjectIdentifierType>();
		
		// transform the HL7 II RequestedPatientId into a
		// QualifiedSubjectIdentifier

		if ((body != null) 
				&& (body.getRequestedPatientId() != null)
				&& (assertion != null)
				&& (NullChecker.isNotNullish(body.getRequestedPatientId().getRoot()))
				&& (NullChecker.isNotNullish(body.getRequestedPatientId().getExtension()))) 
		{
			II requestedII = body.getRequestedPatientId();
			if (requestedII != null && (! requestedII.getRoot().isEmpty()) &&
					(! requestedII.getExtension().isEmpty()))
			{
				String requestAaId = requestedII.getRoot();
				String requestPatId = requestedII.getExtension();
				
				PRPAIN201309UV02 correlationRequest = createCorrelationsRequest(
						requestAaId, requestPatId, assertion);
				
				List<QualifiedSubjectIdentifierType> localMatchList = 
					ProcessPatientCorrelationRequest(correlationRequest, assertion);
				
				if (localMatchList.size() == 0)
				{
					// Return a PatientLocationQuery Fault
		            log.info("Error: Could not find any correlations for RequestedPatientId. Returning PatientLocationQuery Fault.");
					PatientLocationQueryFaultType type = new PatientLocationQueryFaultType();
					type.setErrorCode("Sender");
					type.setReason("No Correlations are available");
		            PatientLocationQueryFault fault = new PatientLocationQueryFault(type.getReason(), type);
		            
		            // We are done
		            throw fault;
				}
				
				if (localMatchList.size() > 1)
				{
					log.error("The PLQ message containing aaId = " +
							requestAaId + " and patId = " +
							requestPatId + " resulted in multiple local matches");
					log.info("Processing the first match only");
				}

				//Process the local correlation just received
				QualifiedSubjectIdentifierType localMatch = localMatchList.get(0);
				finalList.add(localMatch);
				String localAaId = localMatch.getAssigningAuthorityIdentifier();
				String localPatId = localMatch.getSubjectIdentifier();
				correlationRequest = createCorrelationsRequest(localAaId, localPatId, assertion);
				localMatchList = ProcessPatientCorrelationRequest(correlationRequest, assertion);
				
				Iterator<QualifiedSubjectIdentifierType> listIter = localMatchList.iterator();
				while (listIter.hasNext())
				{
					QualifiedSubjectIdentifierType match = listIter.next();
					// Remove the match on the request id pair or it will be double counted
					if (match.getAssigningAuthorityIdentifier().equalsIgnoreCase(requestAaId)
							&& match.getSubjectIdentifier().equalsIgnoreCase(requestPatId))
					{
						listIter.remove();
					}
					else
					{
						finalList.add(match);						
					}
					
					log.debug("Adding external match: aaId = " + match.getAssigningAuthorityIdentifier()
							+ " and patId = " + match.getSubjectIdentifier());
				}
				log.debug("The search for other matches using the local id pair returned " + localMatchList.size()  + " mataches.");
			} 
			else 
			{
				log.error("The PatientLocationQueryRequest was not well-formed. Cannot process request.");
			}
		}
		log.debug("Returning " + finalList.size()
				+ " correlations from method retrieveCorrelations...");
		return finalList;
	}
	
/**
 * Method: processPatientCorrelationRequest
 * 
 * Makes a single call to the CorrelationService
 * 
 * @param correlationRequest - contains the aaId and patId for which correlations are requested
 * @param assertion - assertings from the original message
 * 
 * @return List<QualifiedSubjectIdentifierType> containing correlations found.
 */
	protected List<QualifiedSubjectIdentifierType> ProcessPatientCorrelationRequest(PRPAIN201309UV02 correlationRequest, AssertionType assertion)
	{
		PatientCorrelationProxy patientCorrelationProxy = getPatientCorrelationProxy();
		RetrievePatientCorrelationsResponseType results = patientCorrelationProxy
				.retrievePatientCorrelations(correlationRequest, assertion);
	
		List<QualifiedSubjectIdentifierType> matchList = new ArrayList<QualifiedSubjectIdentifierType>();
		
		if ((results != null)
				&& (results.getPRPAIN201310UV02() != null)
				&& (NullChecker.isNotNullish(results.getPRPAIN201310UV02()
						.getControlActProcess().getSubject()))
				&& (results.getPRPAIN201310UV02().getControlActProcess()
						.getSubject().get(0) != null)
				&& (results.getPRPAIN201310UV02().getControlActProcess()
						.getSubject().get(0).getRegistrationEvent() != null)
				&& (results.getPRPAIN201310UV02().getControlActProcess()
						.getSubject().get(0).getRegistrationEvent()
						.getSubject1() != null)
				&& (results.getPRPAIN201310UV02().getControlActProcess()
						.getSubject().get(0).getRegistrationEvent()
						.getSubject1().getPatient() != null)
				&& (NullChecker.isNotNullish(results.getPRPAIN201310UV02()
						.getControlActProcess().getSubject().get(0)
						.getRegistrationEvent().getSubject1().getPatient()
						.getId()))) 
		{
			List<II> IIList = results.getPRPAIN201310UV02()
					.getControlActProcess().getSubject().get(0)
					.getRegistrationEvent().getSubject1().getPatient()
					.getId();

			Iterator<II> iiIter = IIList.iterator();
			while (iiIter.hasNext())
			{
				II correlatedIds = iiIter.next();
				QualifiedSubjectIdentifierType subjectId = new QualifiedSubjectIdentifierType();
				subjectId.setAssigningAuthorityIdentifier(correlatedIds.getRoot());
				subjectId.setSubjectIdentifier(correlatedIds.getExtension());
				matchList.add(subjectId);
				log.debug("Found correlation. aaId = " 
					+ subjectId.getAssigningAuthorityIdentifier() 
					+ " and patId = " + subjectId.getSubjectIdentifier());
			}
		}
		return matchList;
	}
	/**
	 * Generates a Correlations Request using the HL7 II formatted Id and the
	 * assertion.
	 * 
	 * @param HL7
	 *            II formatted id
	 * @param assertion
	 * @return Patients Correlation Request
	 */
	private PRPAIN201309UV02 createCorrelationsRequest(String aaId,
			String patId, AssertionType assertion) 
	{
		log.debug("Entering method createCorrelationsRequest...");

		PRPAIN201309UV02 correlationsRequest = null;
		RetrievePatientCorrelationsRequestType patientCorrelationReq = new RetrievePatientCorrelationsRequestType();
		QualifiedSubjectIdentifierType subjectId = new QualifiedSubjectIdentifierType();
		subjectId.setSubjectIdentifier(patId);
		subjectId.setAssigningAuthorityIdentifier(aaId);
		patientCorrelationReq.setQualifiedPatientIdentifier(subjectId);
		correlationsRequest = new PixRetrieveBuilder()
				.createPixRetrieve(patientCorrelationReq);

		log.debug("Returning the Correlations Request from method createCorrelationsRequest.");
		return correlationsRequest;
	}

	/**
	 * Returns an instance of the PatientCorrelationProxy.
	 */
	protected PatientCorrelationProxy getPatientCorrelationProxy() {
		return new PatientCorrelationProxyObjectFactory()
				.getPatientCorrelationProxy();
	}

	/**
	 * Generates the PatientLocationQueryResponse message with the date from
	 * the Correlation Table. The List<QualifiedSubjectIdentifierType> only
	 * contains matches to the requestedCompositePatientId (AA & PatientId)
	 * 
	 * @param requestedPatientIds
	 *            in HL7 II format
	 * @param PatientLocationQuery
	 *            response message
	 * @param list
	 *            of patient correlations already found to match the request
	 */
	private void populateResponse(II requestedPatientIds,
			PatientLocationQueryResponseType response,
			List<QualifiedSubjectIdentifierType> correlationsList) 
	{
		log.debug("The search for correlated identifiers returned " + correlationsList.size() 
				+ " matches for AA = " + requestedPatientIds.getRoot() + " and patientId = " + requestedPatientIds.getExtension());
		
		// Create the response
		if ((correlationsList != null) && !(correlationsList.isEmpty())) 
		{
			for (QualifiedSubjectIdentifierType identifier : correlationsList) 
			{
				if ((identifier != null)
						&& (identifier.getAssigningAuthorityIdentifier() != null)) 
				{
					// We have a match; make a response
					String matchedAA = identifier.getAssigningAuthorityIdentifier();
					String matchedPatientId = identifier.getSubjectIdentifier();
					
					PatientLocationResponse matchRecord = new PatientLocationResponse();
					matchRecord.setHomeCommunityId(getOidFromAssigningAuthority(matchedAA));
	
					// Create the II record
					II correlatedIds = new II();
					correlatedIds.setRoot(matchedAA);
					correlatedIds.setExtension(matchedPatientId);
					
					// Fill in matchRecord with the new II record (correspondingCompositePatientId
					matchRecord.setCorrespondingPatientId(correlatedIds);
					
					// Fill in matchRecord with the REQUESTED II (requestedCompositePatientId)
					matchRecord.setRequestedPatientId(requestedPatientIds);
					
					// Add match record to the response
					response.getPatientLocationResponse().add(matchRecord);
				}
			}
		}
	}

	/**
	 * Returns the HCID that matches the assigning authority Id
	 * 
	 * @param assigningAuthorityId
	 * @return the HCID that corresponds to the assigning authority Id
	 */
	protected String getOidFromAssigningAuthority(String assigningAuthorityId) {
		HomeCommunityType homeCommunity = getHomeCommunityFromAA(assigningAuthorityId);

		if (homeCommunity != null) {
			return homeCommunity.getHomeCommunityId();
		} else {
			log.error("The homeCommunity value is null, so returning null.");
		}

		return null;
	}

	/**
	 * Uses the connection manager to find the HCID based on the submitted AA
	 * ID.
	 * 
	 * @param assigningAuthorityId
	 * @return the home community Id
	 */
	protected HomeCommunityType getHomeCommunityFromAA(
			String assigningAuthorityId) {
		return new ConnectionManagerCommunityMapping()
				.getHomeCommunityByAssigningAuthority(assigningAuthorityId);
	}
}