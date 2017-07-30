package com.gregorbyte.xsp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lotus.domino.Document;
import lotus.domino.EmbeddedObject;
import lotus.domino.Item;
import lotus.domino.MIMEEntity;
import lotus.domino.MIMEHeader;
import lotus.domino.NotesException;
import lotus.domino.RichTextItem;
import lotus.domino.Stream;

import com.gregorbyte.xsp.log.GregorbyteLogger;
import com.ibm.commons.log.LogMgr;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.model.domino.DominoUtils;

public class MimeUtil {

	private static final LogMgr logger = GregorbyteLogger.MIME;

	public enum HtmlEditMode {
		APPEND, INSERT, REPLACE
	};

	public enum ContentType {

		MULTIPART_MIXED("multipart", "mixed"), MULTIPART_RELATED("multipart",
				"related"), TEXT_HTML("text", "html"), MULTIPART_ALTERNATIVE(
				"multipart", "alternative"), TEXT_PLAIN("text", "plain"), APPLICATION_EMBEDJSON(
				"application", "embed+json"), APPLICATION_EMBEDXML(
				"application", "embed+xml");

		public final String stringVal;
		public final String type;
		public final String subType;

		String getString() {
			return stringVal;
		}

		ContentType(String type, String subType) {
			this.type = type;
			this.subType = subType;
			this.stringVal = type + "/" + subType;
		}

	}

	private static final String MH_CONTENTDISPOSITION = "Content-Disposition";
	public static final String MH_INLINE = "inline";
	private static final String MH_ATTACHMENT = "attachment";
	private static final String MH_ATTACHMENT_ACONEXBUG = "2"; // Aconex uses
																// content-dispositon
																// 2 for some
																// reason
	public static final String MH_CD_PARAM_FILENAME = "filename";
	public static final String MH_CONTENTTYPE = "Content-Type";
	public static final String MH_CONTENT_ID = "Content-ID";

	public static final String CT_IMAGE = "image";

	public static final String REGEX_MATCH_SRCCID = "src=\"cid:([\\S]+)\"";
	public static final String REGEX_REPLACE_SRCCID = "src=cid:$1";

	// private static final String MH_CT_PARAM_NAME = "name";

	public static boolean isContentType(MIMEEntity mime, String contentType,
			String contentSubType) throws NotesException {

		if (mime.getContentType().equalsIgnoreCase(contentType)
				&& mime.getContentSubType().equals(contentSubType))
			return true;

		return false;
	}

	public static void copyAttachmentsFromDocToDoc(Document targetDoc,
			String targetField, Document sourceDoc, String sourceField)
			throws NotesException, IOException, RuntimeException {
		copyAttachmentsFromDocToDoc(targetDoc, targetField, sourceDoc,
				sourceField, null);
	}

	public static void copyAttachmentsFromDocToDoc(Document targetDoc,
			String targetField, Document sourceDoc, String sourceField,
			List<String> fileNames) throws NotesException, IOException,
			RuntimeException {

		// if (fileNames == null || fileNames.isEmpty())
		// return;

		Item item = sourceDoc.getFirstItem(sourceField);

		if (item == null)
			throw new RuntimeException(
					"Source Item to copy attachments could not be found "
							+ sourceField);

		if (item.getType() == Item.RICHTEXT)
			copyAttachmentsUsingRichTextMethods(targetDoc, targetField,
					sourceDoc, sourceField, fileNames);
		else if (item.getType() == Item.MIME_PART)
			copyAttachmentsFromMime(targetDoc, targetField, sourceDoc,
					sourceField, fileNames);
		else
			throw new RuntimeException(
					"Source Field was not an appropriate Type");

	}

	private static void copyAttachmentsUsingRichTextMethods(Document targetDoc,
			String targetField, Document sourceDoc, String sourceField,
			List<String> fileNames) throws NotesException, IOException {

		RichTextItem rti = (RichTextItem) sourceDoc.getFirstItem(sourceField);

		MIMEEntity mime = targetDoc.getMIMEEntity(targetField);

		for (Object o : rti.getEmbeddedObjects()) {

			EmbeddedObject eo = (EmbeddedObject) o;

			if (fileNames == null) {
				addEmbeddedObjectToMime(eo, mime);
			} else {
				for (String attFilename : fileNames) {
					if (attFilename.equalsIgnoreCase(eo.getName()))
						addEmbeddedObjectToMime(eo, mime);

				}
			}

			eo.recycle();

		}

	}

	public static boolean isTextHtml(MIMEEntity mime) throws NotesException {
		return isContentType(mime, "text", "html");
	}

	public static boolean isTextPlain(MIMEEntity mime) throws NotesException {
		return isContentType(mime, "text", "plain");
	}

	public static boolean isApplicationEmbedJson(MIMEEntity mime)
			throws NotesException {
		return isContentType(mime, "application", "embed+json");
	}

	public static boolean isApplicationEmbedXml(MIMEEntity mime)
			throws NotesException {
		return isContentType(mime, "application", "embed+xml");
	}

	public static boolean isRfc822(MIMEEntity mime) throws NotesException {
		return isContentType(mime, "message", "rfc822");
	}

	public static boolean isAttachment(MIMEEntity mime) throws NotesException {

		MIMEHeader header = mime.getNthHeader(MH_CONTENTDISPOSITION);
		if (header == null)
			return false;
		return header.getHeaderVal().equals(MH_ATTACHMENT)
				|| header.getHeaderVal().equals(MH_ATTACHMENT_ACONEXBUG);

	}

	public static boolean isInline(MIMEEntity mime) throws NotesException {

		MIMEHeader header = mime.getNthHeader(MH_CONTENTDISPOSITION);
		if (header == null)
			return false;
		return header.getHeaderVal().equals(MH_INLINE);

	}

	public static boolean isProbablyInline(MIMEEntity mime)
			throws NotesException {

		MIMEHeader header = mime.getNthHeader(MH_CONTENT_ID);
		if (header != null) {
			return StringUtil.equalsIgnoreCase(mime.getContentType(), CT_IMAGE);
		}
		return false;

	}

	private static String getAttachmentFileName(MIMEEntity mime)
			throws NotesException {

		MIMEHeader header = mime.getNthHeader(MH_CONTENTDISPOSITION);
		if (header == null)
			return null;

		String fileName = header.getParamVal(MH_CD_PARAM_FILENAME);

		if (fileName != null) {
			fileName = fileName.trim();
		}

		fileName = fileName.replaceAll("^\"|\"$", "");

		if (mime instanceof lotus.domino.local.MIMEEntity) {
			String weirdone = ((lotus.domino.local.MIMEEntity) mime)
					.getAttachName();
			System.out.println("weirdone " + weirdone);
			fileName = weirdone;
		}

		// String fileName = header.getParamVal(MH_CD_PARAM_FILENAME);
		header.recycle();

		// Strip Leading and Trailing Double Quotes

		System.out.println("File Name " + fileName);

		return fileName;

	}

	private static void copyAttachmentsFromMime(Document targetDoc,
			String targetField, Document sourceDoc, String sourceField,
			List<String> fileNames) throws NotesException {

		MIMEEntity mime = sourceDoc.getMIMEEntity(sourceField);
		MIMEEntity mimeTarget = targetDoc.getMIMEEntity(targetField);

		while (mime != null) {

			if (isAttachment(mime)) {

				if (fileNames == null) {
					copyMIMEEntityAttachment(mimeTarget, mime);
				} else {
					String filename = getAttachmentFileName(mime);

					for (String attFilename : fileNames) {

						if (attFilename.equalsIgnoreCase(filename))
							copyMIMEEntityAttachment(mimeTarget, mime);

					}
				}

			}

			mime = mime.getNextEntity();
		}
	}

	
	private static void addEmbeddedObjectToMime(EmbeddedObject eo,
			MIMEEntity mimeParent) throws NotesException, IOException {

		if (eo == null)
			throw new NullPointerException("EmbeddedObject eo cannot be null");
		if (mimeParent == null)
			throw new NullPointerException("mimeParent cannot be null");

		MIMEEntity mime = mimeParent.createChildEntity();

		InputStream is = null;

		try {

			String name = eo.getName();

			MIMEHeader emailHeader = mime.createHeader("Content-Disposition");
			emailHeader.setHeaderVal("attachment; filename=\"" + name + "\"");

			// emailHeader = mime.createHeader("Content-ID");
			// emailHeader.setHeaderVal("<" + cid + ">");

			// Custom Header to Like to this ProjDocId
			// emailHeader = mime.createHeader("Jord-ProjDoc-ID");
			// emailHeader.setHeaderVal(docWithOriginal.getItemValueString("docId"));
			// emailHeader.setParamVal("ProjectDocId",
			// docWithOriginal.getItemValueString("docId"));

			is = eo.getInputStream();
			Stream stream = DominoUtils.getCurrentSession().createStream();
			stream.setContents(is);
			mime.setContentFromBytes(stream, "application/octet-stream",
					MIMEEntity.ENC_IDENTITY_BINARY);

		} finally {
			if (null != is) {
				is.close();
				is = null;
			}
		}

	}

	private static void copyMIMEEntityAttachment(MIMEEntity mimeTargetParent,
			MIMEEntity mimeSource) throws NotesException {

		MIMEEntity mimeTarget = mimeTargetParent.createChildEntity();

		// Get the ContentType
		MIMEHeader mimeHeader = mimeSource.getNthHeader(MH_CONTENTTYPE);
		String contentType = mimeHeader.getHeaderVal();

		String filename = getAttachmentFileName(mimeSource);

		// Create the Content-Disposition Header
		MIMEHeader emailHeader = mimeTarget.createHeader(MH_CONTENTDISPOSITION);
		emailHeader.setHeaderVal("attachment; filename=\"" + filename + "\"");

		int sourceEncoding = mimeSource.getEncoding();

		Stream stream = DominoUtils.getCurrentSession().createStream();
		mimeSource.getContentAsBytes(stream);
		mimeTarget.setContentFromBytes(stream, contentType, sourceEncoding);
	}

	public static void editTextHtml(MIMEEntity mimeRoot, String html,
			HtmlEditMode mode) throws NotesException {

		if (mimeRoot == null)
			throw new NullPointerException("mimeRoot cannot be null");

		MIMEEntity mime = mimeRoot;

		while (mime != null) {

			if (isTextHtml(mime)) {

				Stream stream = DominoUtils.getCurrentSession().createStream();

				if (mode.equals(HtmlEditMode.APPEND)) {
					mime.getContentAsText(stream, true);
				}

				stream.writeText(html);

				if (mode.equals(HtmlEditMode.INSERT)) {
					mime.getContentAsText(stream, true);
				}

				mime.setContentFromText(stream, "text/html;charset=UTF-8",
						MIMEEntity.ENC_NONE);

				stream.close();
				stream.recycle();

				return;
			}

			mime = mime.getNextEntity();

		}

		// If we get here, then we didn't find a text/html mime entity
		MIMEEntity mimeNew = mimeRoot.createChildEntity();
		MimeUtil.setContentType(mimeNew, ContentType.TEXT_HTML);
		Stream stream = DominoUtils.getCurrentSession().createStream();
		stream.writeText(html);
		mimeNew.setContentFromText(stream, "text/html;charset=UTF-8",
				MIMEEntity.ENC_NONE);
		stream.close();
		stream.recycle();

	}

	public static void printMIME(MIMEEntity mime, StringBuilder sb)
			throws NotesException {

		sb.append(mime.getContentType() + "/" + mime.getContentSubType()
				+ "<br/>");

		MIMEEntity mimeChild = mime.getFirstChildEntity();

		if (mimeChild != null) {
			sb.append("Child > ");
			printMIME(mimeChild, sb);
		}

		MIMEEntity mimeSibling = mime.getNextSibling();

		if (mimeSibling != null) {
			sb.append("Sibling >");
			printMIME(mimeSibling, sb);
		}

	}

	public static String getMimeReport(Document document) throws NotesException {

		StringBuilder sb = new StringBuilder();

		MIMEEntity mime = document.getMIMEEntity("Body");

		if (mime == null)
			return "Mime is null for Body";

		sb.append("+ Parent");

		printMIME(mime, sb);

		return sb.toString();

	}

	public static void removeAttachments(MIMEEntity mime) throws NotesException {

		if (mime == null)
			return;

		while (mime != null) {

			MIMEEntity nextMime = mime.getNextEntity(MIMEEntity.SEARCH_DEPTH);

			removeAttachments(nextMime);

			if (nextMime != null)
				nextMime.recycle();
			nextMime = null;

			if (isAttachment(mime))
				mime.remove();

		}

	}

	public static void insertTextHtml(MIMEEntity mime, String html)
			throws NotesException {

		MIMEEntity mimeTarget = findHTMLEntity(mime);

		Stream htmlStream = DominoUtils.getCurrentSession().createStream();

		htmlStream.writeText(html);
		mimeTarget.getContentAsText(htmlStream, true);

		mimeTarget.setContentFromText(htmlStream, "text/html;charset=UTF-8",
				MIMEEntity.ENC_NONE);

		htmlStream.close();
		htmlStream.recycle();

	}

	public static void addAttachmentFromFilesystem(MIMEEntity mimeRoot,
			File fileToAdd, String fileNameToUse) throws NotesException,
			IOException {

		if (fileToAdd == null)
			throw new NullPointerException("fileToAdd cannot be null");
		if (fileNameToUse == null)
			throw new NullPointerException("FileNameToUse cannot be null");
		if (!fileToAdd.exists())
			throw new FileNotFoundException("FileToAdd not found");

		MIMEEntity mimeParent = mimeRoot;

		if (mimeParent == null)
			throw new NullPointerException("mimeParent cannot be null");

		MIMEEntity mime = mimeParent.createChildEntity();

		InputStream is = null;

		try {

			MIMEHeader emailHeader = mime.createHeader("Content-Disposition");
			emailHeader.setHeaderVal("attachment; filename=\"" + fileNameToUse
					+ "\"");

			// emailHeader = mime.createHeader("Content-ID");
			// emailHeader.setHeaderVal("<" + cid + ">");

			is = new FileInputStream(fileToAdd);
			Stream stream = DominoUtils.getCurrentSession().createStream();
			stream.setContents(is);
			mime.setContentFromBytes(stream, "application/octet-stream",
					MIMEEntity.ENC_IDENTITY_BINARY);

		} finally {
			if (null != is) {
				is.close();
				is = null;
			}
		}

	}

	public static void addAttachmentFromInputStream(MIMEEntity mimeRoot,
			InputStream is, String fileNameToUse) throws NotesException,
			IOException {

		if (is == null)
			throw new NullPointerException("inputStream cannot be null");
		if (fileNameToUse == null)
			throw new NullPointerException("FileNameToUse cannot be null");

		MIMEEntity mimeParent = mimeRoot;

		if (mimeParent == null)
			throw new NullPointerException("mimeParent cannot be null");

		MIMEEntity mime = mimeParent.createChildEntity();

		try {

			MIMEHeader emailHeader = mime.createHeader("Content-Disposition");
			emailHeader.setHeaderVal("attachment; filename=\"" + fileNameToUse
					+ "\"");

			// emailHeader = mime.createHeader("Content-ID");
			// emailHeader.setHeaderVal("<" + cid + ">");

			Stream stream = DominoUtils.getCurrentSession().createStream();
			stream.setContents(is);
			mime.setContentFromBytes(stream, "application/octet-stream",
					MIMEEntity.ENC_IDENTITY_BINARY);

		} finally {
			if (null != is) {
				is.close();
				is = null;
			}
		}

	}

	public static void addEmbeddedImageFromResource(MIMEEntity mimeRoot,
			String resourcePath, String cid) throws NotesException,
			RuntimeException, IOException {

		MIMEEntity mimeHtml = findHTMLEntity(mimeRoot);
		MIMEEntity mimeParent = getParentMimeRelated(mimeHtml, true);

		InputStream is = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(resourcePath);

		MIMEEntity mimeImage = mimeParent.createChildEntity();

		String contentType = "image/jpeg";

		String bracketedCid = "<" + cid + ">";

		// Set the CID
		MIMEHeader cidHeader = mimeImage.createHeader(MH_CONTENT_ID);
		cidHeader.setHeaderVal(bracketedCid);

		Stream stream = DominoUtils.getCurrentSession().createStream();
		stream.setContents(is);
		mimeImage.setContentFromBytes(stream, contentType,
				MIMEEntity.ENC_IDENTITY_BINARY);

		is.close();
		stream.close();

	}

	public static boolean isMimeMultipartRelated(MIMEEntity mime)
			throws NotesException {

		return isContentType(mime, "multipart", "related");

	}

	public static boolean isMimeMultipartAlternative(MIMEEntity mime)
			throws NotesException {

		return isContentType(mime, "multipart", "alternative");

	}

	public static boolean isMimeMultipartMixed(MIMEEntity mime)
			throws NotesException {
		return isContentType(mime, "multipart", "mixed");
	}

	public static MIMEEntity findRfc822Entity(MIMEEntity mimeRoot)
			throws NotesException {

		MIMEEntity mime = mimeRoot;

		while (mime != null) {

			if (isRfc822(mime))
				return mime;

			MIMEEntity nextMime = mime.getNextEntity();
			mime.recycle();
			mime = nextMime;

		}

		return null;

	}

	public static MIMEEntity findMultipartAlternative(MIMEEntity mimeRoot)
			throws NotesException {

		MIMEEntity mime = mimeRoot;

		while (mime != null) {

			if (isContentType(mime, "multipart", "alternative")) {
				return mime;
			}

			MIMEEntity nextMime = mime.getNextEntity();
			if (mime != mimeRoot) {
				mime.recycle();
			}
			mime = nextMime;

		}

		return null;

	}

	public static MIMEEntity findMultipartMixed(MIMEEntity mimeRoot)
			throws NotesException {

		MIMEEntity mime = mimeRoot;

		while (mime != null) {

			if (isContentType(mime, "multipart", "mixed")) {
				return mime;
			}

			MIMEEntity nextMime = mime.getNextEntity();
			mime.recycle();
			mime = nextMime;

		}

		return null;

	}

	public static MIMEEntity findHTMLEntity(MIMEEntity mimeRoot)
			throws NotesException {

		MIMEEntity mime = mimeRoot;

		while (mime != null) {

			if (isTextHtml(mime))
				return mime;

			MIMEEntity nextMime = mime.getNextEntity();
			mime.recycle();
			mime = nextMime;

		}

		return null;

	}

	public static MIMEEntity getParentMimeRelated(MIMEEntity mimeChild)
			throws NotesException, RuntimeException {
		return getParentMimeRelated(mimeChild, false);
	}

	/**
	 * if immediate parent is null, creates a new multipart/related entity and
	 * returns it otherwise, searches throuht parents of parents for first
	 * occurance of multipart/related
	 * 
	 * @param mimeChild
	 * @param createIfNull
	 * @return
	 * @throws NotesException
	 * @throws RuntimeException
	 */
	public static MIMEEntity getParentMimeRelated(MIMEEntity mimeChild,
			boolean createIfNull) throws NotesException, RuntimeException {

		if (!isTextHtml(mimeChild))
			throw new IllegalArgumentException(
					"mimeChild must be text/html for getParentMimeRelated");

		MIMEEntity mimeParent = mimeChild.getParentEntity();

		if (mimeParent == null) {

			mimeParent = mimeChild.createParentEntity();

			MIMEHeader mimeHeader = mimeParent.getNthHeader(MH_CONTENTTYPE);
			mimeHeader.setHeaderVal("multipart/related");
			// mimeHeader.setHeaderVal("multipart/related; boundary=\"superboundaryman\"");

			return mimeParent;

		}

		while (mimeParent != null) {

			if (isMimeMultipartRelated(mimeParent))
				return mimeParent;

			mimeParent = mimeParent.getParentEntity();

		}

		return null;
	}

	public static void copyEmbeddedImages(MIMEEntity mimeSource,
			MIMEEntity mimeTarget) throws NotesException {

		MIMEEntity mime = mimeSource.getFirstChildEntity();

		while (mime != null) {

			if (mime.getContentType().equals("image"))
				copyEmbeddedImage(mime, mimeTarget);

			mime = mime.getNextSibling();
		}

	}

	public static void copyEmbeddedImage(MIMEEntity mimeSource,
			MIMEEntity mimeTargetParent) throws NotesException {

		MIMEEntity mimeTarget = mimeTargetParent.createChildEntity();

		// Get the ContentType and Encoding
		MIMEHeader mimeHeader = mimeSource.getNthHeader(MH_CONTENTTYPE);
		String contentType = mimeHeader.getHeaderVal();
		int sourceEncoding = mimeSource.getEncoding();

		// Get the CID
		MIMEHeader cidHeader = mimeSource.getNthHeader(MH_CONTENT_ID);
		String cid = cidHeader.getHeaderVal();

		// Set the CID
		cidHeader = mimeTarget.createHeader(MH_CONTENT_ID);
		cidHeader.setHeaderVal(cid);

		Stream stream = DominoUtils.getCurrentSession().createStream();
		mimeSource.getContentAsBytes(stream, false);
		mimeTarget.setContentFromBytes(stream, contentType, sourceEncoding);

	}

	public static boolean fixCIDsNeeded(String html) {

		if (html == null)
			return false;
		Pattern pattern = Pattern.compile(REGEX_MATCH_SRCCID);
		Matcher matcher = pattern.matcher(html);
		return matcher.find();

	}

	public static String fixCIDs(String html) {

		if (html == null)
			return null;

		Pattern pattern = Pattern.compile(REGEX_MATCH_SRCCID);
		Matcher matcher = pattern.matcher(html);
		return matcher.replaceAll(REGEX_REPLACE_SRCCID);

	}

	public static boolean processExternalMimeNeeded(MIMEEntity mimeRoot)
			throws NotesException {

		MIMEEntity mimeHtml = findHTMLEntity(mimeRoot);

		if (mimeHtml == null)
			return false;

		mimeHtml.decodeContent();
		String origHtml = mimeHtml.getContentAsText();

		if (origHtml == null)
			return false;

		return fixCIDsNeeded(origHtml);

	}

	public static void processExternalMime(MIMEEntity mimeRoot)
			throws NotesException {

		MIMEEntity mimeHtml = findHTMLEntity(mimeRoot);

		if (mimeHtml == null)
			return;

		mimeHtml.decodeContent();

		String origHtml = mimeHtml.getContentAsText();

		if (origHtml == null)
			return;

		String html = fixCIDs(origHtml);

		Stream stream = DominoUtils.getCurrentSession().createStream();
		stream.writeText(html);
		mimeHtml.setContentFromText(stream, "text/html;charset=UTF-8",
				MIMEEntity.ENC_NONE);
		mimeHtml = null;

	}

	public static void setContentType(MIMEEntity mime, ContentType contentType)
			throws NotesException {

		if (mime == null)
			throw new NullPointerException(
					"supplied MIMEEntity must not be null");

		MIMEHeader header = mime.getNthHeader(MH_CONTENTTYPE);
		if (header != null)
			throw new IllegalArgumentException(
					"Supplied MIMEEntity must not have existing MIMEHeader for "
							+ MH_CONTENTTYPE);

		header = mime.createHeader(MH_CONTENTTYPE);
		header.setHeaderVal(contentType.getString());

		header.recycle();
		header = null;

	}

	public static void removeHeader(MIMEEntity mime, String headerName)
			throws NotesException {

		MIMEHeader header = mime.getNthHeader(headerName);

		if (header != null) {
			header.remove();
			header.recycle();
		}

	}

	public static MIMEEntity setupStandardMimeStructure(Document doc,
			String itemName) throws NotesException {

		if (doc == null)
			throw new NullPointerException("Supplied document must not be null");

		if (doc.hasItem(itemName))
			throw new IllegalArgumentException(
					"Document already has item with name " + itemName);

		// Add multipart/mixed
		MIMEEntity mimeRoot = doc.createMIMEEntity(itemName);
		setContentType(mimeRoot, ContentType.MULTIPART_MIXED);

		// Add child multipart/related
		MIMEEntity mimeRelated = mimeRoot.createChildEntity();
		setContentType(mimeRelated, ContentType.MULTIPART_RELATED);

		// Add Child text/html
		MIMEEntity mimeHtml = mimeRelated.createChildEntity();
		setContentType(mimeHtml, ContentType.TEXT_HTML);

		return mimeRoot;

	}

	public static MIMEEntity setupStandardMimeStructure2(Document doc,
			String itemName) throws NotesException {

		if (doc == null)
			throw new NullPointerException("Supplied document must not be null");

		if (doc.hasItem(itemName))
			throw new IllegalArgumentException(
					"Document already has item with name " + itemName);

		// Add multipart/mixed
		MIMEEntity mimeRoot = doc.createMIMEEntity(itemName);
		setContentType(mimeRoot, ContentType.MULTIPART_MIXED);

		// Add child multipart/alternative
		MIMEEntity mimeAlternative = mimeRoot.createChildEntity();
		setContentType(mimeAlternative, ContentType.MULTIPART_ALTERNATIVE);

		// Add child multipart/related
		MIMEEntity mimeRelated = mimeAlternative.createChildEntity();
		setContentType(mimeRelated, ContentType.MULTIPART_RELATED);

		// Add Child text/html
		MIMEEntity mimeHtml = mimeRelated.createChildEntity();
		setContentType(mimeHtml, ContentType.TEXT_HTML);

		return mimeRoot;

	}

	public static String getDisposition(MIMEEntity mime) throws NotesException {

		MIMEHeader header = mime.getNthHeader(MH_CONTENTDISPOSITION);

		if (header != null) {
			return header.getHeaderValAndParams(false, true);
		}

		return null;

	}

	public static void setDisposition(MIMEEntity mime, String disposition)
			throws NotesException {

		if (mime == null)
			throw new NullPointerException(
					"supplied MIMEEntity must not be null");

		MIMEHeader ctheader = mime.getNthHeader(MH_CONTENTTYPE);
		String filename = null;

		if (ctheader != null) {
			filename = ctheader.getParamVal("name");
		}

		MIMEHeader header = mime.getNthHeader(MH_CONTENTDISPOSITION);
		if (header == null) {
			header = mime.createHeader(MH_CONTENTDISPOSITION);
		}

		header.setHeaderVal(disposition);
		if (StringUtil.isNotEmpty(filename)) {
			header.setParamVal("filename", filename);
		}

		header.recycle();
		header = null;

	}

	public static void copyAsChild(MIMEEntity parent, MIMEEntity source)
			throws NotesException {

		MIMEEntity child = parent.createChildEntity();
		copyMIMEEntity(source, child);

	}

	public static void copyMIMEEntity(MIMEEntity source, MIMEEntity target)
			throws NotesException {

		// Copy the Mime Headers
		@SuppressWarnings("rawtypes")
		Vector headers = source.getHeaderObjects();

		for (Object o : headers) {

			if (o instanceof MIMEHeader) {

				MIMEHeader sourceHeader = (MIMEHeader) o;

				String headerName = sourceHeader.getHeaderName();

				logTrace("Copying Header {0}", headerName);

				// Create the New header
				MIMEHeader targetHeader = target.createHeader(headerName);

				String hvp = sourceHeader.getHeaderValAndParams();

				logTrace("  Setting HeaderValand Params: {0}", hvp);

				targetHeader.setHeaderValAndParams(hvp);

			}

		}

		// Get the ContentType
		MIMEHeader mimeHeader = source.getNthHeader(MH_CONTENTTYPE);
		String contentType = mimeHeader.getHeaderValAndParams(); // With Params
																	// so
																	// charset
																	// is there

		/*
		 * String filename = getAttachmentFileName(source);
		 * 
		 * // Create the Content-Disposition Header MIMEHeader emailHeader =
		 * target.createHeader(MH_CONTENTDISPOSITION);
		 * emailHeader.setHeaderVal("attachment; filename=\"" + filename +
		 * "\"");
		 */

		int sourceEncoding = source.getEncoding();

		Stream stream = DominoUtils.getCurrentSession().createStream();
		source.getContentAsBytes(stream);

		if (stream.getBytes() != 0) {
			logTrace("Copying {0} with encoding {1}", contentType,
					translateEncoding(sourceEncoding));
			target.setContentFromBytes(stream, contentType, sourceEncoding);
		} else {
			logTrace("No Content in Stream");
		}

		stream.close();

	}

	private static void logTrace(String message) {

		if (logger.isTraceDebugEnabled()) {
			logger.traceDebug(message);
		}

	}

	private static void logTrace(String message, Object... args) {

		if (logger.isTraceDebugEnabled()) {
			logger.traceDebug(message, args);
		}

	}

	private static String translateEncoding(int encoding) {

		switch (encoding) {
		case MIMEEntity.ENC_BASE64:
			return "base64";
		case MIMEEntity.ENC_EXTENSION:
			return "EXTENSION";
		case MIMEEntity.ENC_IDENTITY_7BIT:
			return "7bit";
		case MIMEEntity.ENC_IDENTITY_8BIT:
			return "8bit";
		case MIMEEntity.ENC_IDENTITY_BINARY:
			return "binary";
		case MIMEEntity.ENC_NONE:
			return "NONE";
		case MIMEEntity.ENC_QUOTED_PRINTABLE:
			return "QUOTED-PRINTABLE";

		default:
			return "";
		}

	}

	public static void addApplicationJson(MIMEEntity mime, String json)
			throws NotesException {

		MIMEEntity parent = findMultipartAlternative(mime);

		if (parent == null) {
			parent = mime;
		}

		MIMEEntity mimeJson = parent.createChildEntity();

		Stream stream = DominoUtils.getCurrentSession().createStream();

		stream.writeText(json);

		mimeJson.setContentFromText(stream,
				"application/embed+json;charset=UTF-8", 0);
		mimeJson.encodeContent(MIMEEntity.ENC_BASE64);

		stream.close();
		stream.recycle();
		mimeJson.recycle();

	}

	public static void addApplicationXml(MIMEEntity mime, String xml)
			throws NotesException {

		MIMEEntity parent = findMultipartAlternative(mime);

		if (parent == null) {
			parent = mime;
		}

		MIMEEntity mimeJson = parent.createChildEntity();

		Stream stream = DominoUtils.getCurrentSession().createStream();

		stream.writeText(xml);

		mimeJson.setContentFromText(stream,
				"application/embed+xml;charset=UTF-8", 0);
		mimeJson.encodeContent(MIMEEntity.ENC_BASE64);

		stream.close();
		stream.recycle();
		mimeJson.recycle();

	}

}
