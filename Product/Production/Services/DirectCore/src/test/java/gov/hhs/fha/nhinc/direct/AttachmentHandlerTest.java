package gov.hhs.fha.nhinc.direct;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.nhindirect.xd.common.DirectDocument2;
import org.nhindirect.xd.common.DirectDocument2.Metadata;
import org.nhindirect.xd.common.DirectDocuments;

/**
 * Contain all junit tests for the class "AttachmentHandler".
 * 
 * @author Greg Gurr
 */
@RunWith(MockitoJUnitRunner.class)
public class AttachmentHandlerTest extends DirectBaseTest {	
	private static final String MESSAGE_ID = "111-000";
	private static final String MOCK_FILE_NAME = "MockFile";
	private static final String MOCK_FILE_PATH = "/some/file/path/" + MOCK_FILE_NAME;
	private static final String ABSOLUTE_MOCK_FILE_PATH = "C:/some/file/path/" + MOCK_FILE_NAME;
	
	private String SAMPLE_DOCUMENT = "PENsaW5pY2FsRG9jdW1lbnQ+DQoJPHNlY3Rpb24+VGVzdCBzZWN0aW9uPC9zZWN0aW9uPg0KCTxjb21tZW50PlRoaXMgaXMgYSB0ZXN0IGNvbW1lbnQ8L2NvbW1lbnQ+DQo8L0NsaW5pY2FsRG9jdW1lbnQ+";
	
	@Mock
	private File mockFile;
	@Mock
	private BufferedOutputStream mockBufferedOutputStream;
	@Mock
	private FileOutputStream mockFileOutputStream;

	@Before
	public void setUp() throws Exception {	
		when(mockFile.getName()).thenReturn(MOCK_FILE_NAME);
		when(mockFile.getPath()).thenReturn(MOCK_FILE_PATH);
		when(mockFile.getAbsolutePath()).thenReturn(MOCK_FILE_PATH);		
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddAttachments_WithNullDirectDocuments() throws Exception {
		AttachmentHandler testSubject = createTestSubject(false);

		DirectDocuments directDocuments = null;
		Address recipient = new InternetAddress();
		
		@SuppressWarnings("unused")
		List<MimeBodyPart> attachmentParts = testSubject.createDirectAttachments(directDocuments, MESSAGE_ID, recipient);
	}	
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddAttachments_WithNullDirectDocumentList() throws Exception {
		AttachmentHandler testSubject = createTestSubject(false);

		DirectDocuments directDocuments = new DirectDocuments();
		Address recipient = new InternetAddress();
		
		@SuppressWarnings("unused")
		List<MimeBodyPart> attachmentParts = testSubject.createDirectAttachments(directDocuments, MESSAGE_ID, recipient);
	}	
	
	@Test(expected=IllegalArgumentException.class)
	public void testAddAttachments_WithNullMessageId() throws Exception {
		AttachmentHandler testSubject = createTestSubject(false);

		DirectDocuments directDocuments = createDirectDocumentsWithNullData();
		Address recipient = new InternetAddress();
		
		@SuppressWarnings("unused")
		List<MimeBodyPart> attachmentParts = testSubject.createDirectAttachments(directDocuments, null, recipient);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testAddAttachments_WithBlankMessageId() throws Exception {
		AttachmentHandler testSubject = createTestSubject(false);

		DirectDocuments directDocuments = createDirectDocumentsWithNullData();
		Address recipient = new InternetAddress();
		
		@SuppressWarnings("unused")
		List<MimeBodyPart> attachmentParts = testSubject.createDirectAttachments(directDocuments, "", recipient);
	}
	
	@Test
	public void testAddAttachments_WithNullDocumentData() throws Exception {
		DirectDocuments directDocuments = createDirectDocumentsWithNullData();
		Address recipient = createRecipient("johh.smith@intermountain.org");
		
		boolean persistAttachment = false;
		AttachmentHandler testSubject = createTestSubject(persistAttachment);
		
		List<MimeBodyPart> attachmentParts = testSubject.createDirectAttachments(directDocuments, MESSAGE_ID, recipient);
		
		assertNotNull(attachmentParts);
		assertEquals(0, attachmentParts.size());	
		
		verifyNoMoreInteractions(mockFile);
	}	
	
	@Test
	public void testAddAttachments_WithXMLAttachmentDocumentDataAndNotPersistingData() throws Exception {
		DirectDocuments directDocuments = createDirectDocumentsWithData(AttachmentHandler.DirectMimeType.TEXT_XML.getType());
		Address recipient = createRecipient("johh.smith@intermountain.org");
		
		boolean persistAttachment = false;
		AttachmentHandler testSubject = createTestSubject(persistAttachment);

		List<MimeBodyPart> attachmentParts = testSubject.createDirectAttachments(directDocuments, MESSAGE_ID, recipient);
		
		assertNotNull(attachmentParts);
		assertEquals(1, attachmentParts.size());	
		assertEquals(1, attachmentParts.get(0).getHeader("Content-Type").length);
		assertEquals(AttachmentHandler.DirectMimeType.TEXT_XML.getType(), attachmentParts.get(0).getHeader("Content-Type")[0]);
		
		verifyNoMoreInteractions(mockFile);
	}		
	
	@Test
	public void testAddAttachments_WithXMLAttachmentAndPersistingDocument() throws Exception {
		DirectDocuments directDocuments = createDirectDocumentsWithData(AttachmentHandler.DirectMimeType.TEXT_XML.getType());
		Address recipient = createRecipient("johh.smith@intermountain.org");
		
		boolean persistAttachment = true;
		AttachmentHandler testSubject = createTestSubject(persistAttachment);

		List<MimeBodyPart> attachmentParts = testSubject.createDirectAttachments(directDocuments, MESSAGE_ID, recipient);
		
		assertNotNull(attachmentParts);
		assertEquals(1, attachmentParts.size());	
		assertEquals(1, attachmentParts.get(0).getHeader("Content-Type").length);
		assertEquals(AttachmentHandler.DirectMimeType.TEXT_XML.getType(), attachmentParts.get(0).getHeader("Content-Type")[0]);
		
		verify(mockBufferedOutputStream).write(SAMPLE_DOCUMENT.getBytes());
		verify(mockBufferedOutputStream).flush();
		verify(mockBufferedOutputStream).close();	
		verifyNoMoreInteractions(mockFile, mockBufferedOutputStream); 	
	}		
	
	@Test
	public void testAddAttachments_WithZipAttachmentAndPersistingDocument() throws Exception {
		DirectDocuments directDocuments = createDirectDocumentsWithData(AttachmentHandler.DirectMimeType.APPLICATION_ZIP.getType());
		Address recipient = createRecipient("johh.smith@intermountain.org");
		
		boolean persistAttachment = true;
		AttachmentHandler testSubject = createTestSubject(persistAttachment);

		List<MimeBodyPart> attachmentParts = testSubject.createDirectAttachments(directDocuments, MESSAGE_ID, recipient);
		
		assertNotNull(attachmentParts);
		assertEquals(1, attachmentParts.size());	
		assertEquals(1, attachmentParts.get(0).getHeader("Content-Type").length);
		assertEquals(AttachmentHandler.DirectMimeType.APPLICATION_ZIP.getType(), attachmentParts.get(0).getHeader("Content-Type")[0]);
		
		verify(mockBufferedOutputStream).write(SAMPLE_DOCUMENT.getBytes());
		verify(mockBufferedOutputStream).flush();
		verify(mockBufferedOutputStream).close();	
		verifyNoMoreInteractions(mockFile, mockBufferedOutputStream); 	
	}	
	
	@Test(expected=DirectException.class)
	public void testAddAttachments_WithXMLAttachmentAndThrowingErrorOnBufferedStreamWrite() throws Exception {
		DirectDocuments directDocuments = createDirectDocumentsWithData(AttachmentHandler.DirectMimeType.TEXT_XML.getType());
		Address recipient = createRecipient("johh.smith@intermountain.org");
		
		boolean persistAttachment = true;
		AttachmentHandler testSubject = createTestSubject(persistAttachment);
			
		doThrow(new DirectException("*** Ignored, thrown for unit test ***")).when(mockBufferedOutputStream).write(SAMPLE_DOCUMENT.getBytes());

		@SuppressWarnings("unused")
		List<MimeBodyPart> attachmentParts = testSubject.createDirectAttachments(directDocuments, MESSAGE_ID, recipient);
	}		
	
	@Test
	public void testAddAttachments_WithXMLAttachmentAndThrowingErrorOnBufferedStreamClose() throws Exception {
		DirectDocuments directDocuments = createDirectDocumentsWithData(AttachmentHandler.DirectMimeType.TEXT_XML.getType());
		Address recipient = createRecipient("johh.smith@intermountain.org");
		
		boolean persistAttachment = true;
		AttachmentHandler testSubject = createTestSubject(persistAttachment);
					
		doThrow(new IOException("*** Ignored, thrown for unit test ***")).when(mockBufferedOutputStream).close();

		List<MimeBodyPart> attachmentParts = testSubject.createDirectAttachments(directDocuments, MESSAGE_ID, recipient);
		
		assertNotNull(attachmentParts);
		assertEquals(1, attachmentParts.size());	
		assertEquals(1, attachmentParts.get(0).getHeader("Content-Type").length);
		assertEquals(AttachmentHandler.DirectMimeType.TEXT_XML.getType(), attachmentParts.get(0).getHeader("Content-Type")[0]);
		
		verify(mockBufferedOutputStream).write(SAMPLE_DOCUMENT.getBytes());
		verify(mockBufferedOutputStream).flush();
		verify(mockBufferedOutputStream).close();		
		verifyNoMoreInteractions(mockFile, mockBufferedOutputStream);		
	}		
	
	
	
	//------------------------------------------------------------------------------------------------------------------------------
	// Private methods
	//------------------------------------------------------------------------------------------------------------------------------
	
	
	/**
	 * Create a "testSubject" object and override methods that access external resources (files, database, etc.).
	 * 
	 * @param persistDirectAttachment
	 * 		True if the Direct attachment is supposed to be persisted, false otherwise.
	 * @return
	 * 		Returns a "AttachmentHandler" class with some methods overridden.
	 */
	private AttachmentHandler createTestSubject(final boolean persistDirectAttachment) {
		AttachmentHandler testItem = new AttachmentHandler() {
			@Override
			protected boolean persistDirectAttachmentsToFile() {
				return persistDirectAttachment;
			}
			
			@Override
			protected File generateXdmPackageFile(DirectDocuments directDocuments, String formattedMessageId) {	
				return mockFile;
			}
			
			@Override
			protected File getAttachmentFile(String fileName) {
				return mockFile;
			}
			
			@Override
			protected String getAbsoluteAttachmentFilePath(String attachmentFileName) {
				return ABSOLUTE_MOCK_FILE_PATH;
			}			
			
			@Override
			protected BufferedOutputStream getBufferedOutputStream(FileOutputStream fos) {	
				return mockBufferedOutputStream;
			}
			
			@Override
			protected FileOutputStream getFileOutputStream(File attachmentFile) throws FileNotFoundException {
				return mockFileOutputStream;
			}
			
		};
		
		return testItem;
	}
	
	private Address createRecipient(String emailAddress) throws AddressException {
		InternetAddress recipient = new InternetAddress(emailAddress);
		
		return recipient;
	}

	private DirectDocuments createDirectDocumentsWithNullData() {
		DirectDocuments retVal = new DirectDocuments();
		DirectDocument2 doc2 = new DirectDocument2();
		List<DirectDocument2> docList = new ArrayList<DirectDocument2>();
		
		docList.add(doc2);
		
		retVal.setDocuments(docList);
	
		return retVal;
	}	
	
	private DirectDocuments createDirectDocumentsWithData(String dataMimeType) {
		DirectDocuments retVal = new DirectDocuments();
		DirectDocument2 doc2 = new DirectDocument2();
		List<DirectDocument2> docList = new ArrayList<DirectDocument2>();
		
		doc2.setMetadata(new Metadata());
		doc2.getMetadata().setMimeType(dataMimeType);
		
		doc2.setData(SAMPLE_DOCUMENT.getBytes());
		
		docList.add(doc2);
		
		retVal.setDocuments(docList);
	
		return retVal;
	}	

	
}
