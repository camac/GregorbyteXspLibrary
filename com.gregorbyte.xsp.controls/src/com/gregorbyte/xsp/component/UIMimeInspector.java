package com.gregorbyte.xsp.component;


import java.io.IOException;
import java.util.Vector;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import lotus.domino.MIMEEntity;
import lotus.domino.MIMEHeader;
import lotus.domino.NotesException;

import com.gregorbyte.xsp.util.MimeUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.component.UIOutputEx;
import com.ibm.xsp.model.domino.wrapped.DominoRichTextItem;
import com.ibm.xsp.util.HtmlUtil;

public class UIMimeInspector extends UIOutputEx {

	private static final String LIST = "ul";
	private static final String LISTITEM = "li";

	private Boolean showHeaders = null;
	private Boolean showContent = null;

	public UIMimeInspector() {

	}

	@Override
	public void encodeBegin(FacesContext context) throws IOException {

	}

	@Override
	public void encodeChildren(FacesContext context) throws IOException {
		super.encodeChildren(context);
	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException {

		if (!isRendered()) {
			return;
		}

		ResponseWriter rw = context.getResponseWriter();

		Object o = getValue();

		if (o == null) {
			rw.writeText("Value is Null", null);
			return;
		}

		if (!(o instanceof DominoRichTextItem)) {
			rw.writeText("ERR: Expecting to Debug DominoRichTextItem, not a - "
					+ o.getClass().getName(), null);
		}

		DominoRichTextItem drti = (DominoRichTextItem) o;

		writeStatus(context, rw, drti);

		writeMimeTree(rw, drti);

	}

	private void writeStatus(FacesContext context, ResponseWriter rw,
			DominoRichTextItem drti) throws IOException {

		rw.startElement("table", null);

		writeTableRowBoolean(rw, "Has Embbeded Images to Save",
				drti.hasEmbeddedImagesToSave());
		writeTableRowBoolean(rw, "Has Attachments to Save",
				drti.hasAttachmentsToSave());

		writeTableRowBoolean(rw, "Is Mime", drti.isMime());
		writeTableRowBoolean(rw, "Is Discarded", drti.isDiscarded());

		rw.endElement("table");

	}

	private void writeSpan(ResponseWriter rw, String label, String value)
			throws IOException {

		rw.startElement(HtmlUtil.SPAN, null);
		rw.writeAttribute(HtmlUtil.STYLE, "color: gray; margin-left: 3px;",
				null);

		rw.writeText(label, null);
		rw.writeText(HtmlUtil.COLON, null);
		rw.writeText(value, null);

		rw.endElement(HtmlUtil.SPAN);

	}

	private void writeTableRowBoolean(ResponseWriter rw, String label,
			Boolean test) throws IOException {

		if (test) {
			writeTableRowString(rw, label, "Yes");
		} else {
			writeTableRowString(rw, label, "No");
		}

	}

	private void writeTableRowString(ResponseWriter rw, String label,
			String string) throws IOException {

		rw.startElement("tr", null);
		rw.startElement("td", null);

		rw.writeText(label, null);

		rw.endElement("td");
		rw.startElement("td", null);

		rw.writeText(string, null);

		rw.endElement("td");

		rw.endElement("tr");

	}

	private void writeMimeTree(ResponseWriter rw, DominoRichTextItem drti)
			throws IOException {

		try {
			MIMEEntity mime = drti.getMIMEEntity();

			rw.startElement(LIST, null);

			while (mime != null) {

				writeMimeEntity(rw, mime);
				writeMimeChildren(rw, mime);

				mime = mime.getNextSibling();

			}

			rw.endElement(LIST);

		} catch (NotesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void writeMimeChildren(ResponseWriter rw, MIMEEntity parent)
			throws IOException, NotesException {

		MIMEEntity mime = parent.getFirstChildEntity();
		if (mime == null)
			return;

		rw.startElement(LIST, null);

		while (mime != null) {

			writeMimeEntity(rw, mime);
			writeMimeChildren(rw, mime);

			mime = mime.getNextSibling();

		}

		rw.endElement(LIST);

	}

	private void writeEncoding(ResponseWriter rw, MIMEEntity mime)
			throws NotesException, IOException {

		int encoding = mime.getEncoding();
		String encStr = null;

		switch (encoding) {
		case MIMEEntity.ENC_BASE64:
			encStr = "base64";
			break;

		case MIMEEntity.ENC_EXTENSION:
			encStr = "user-defined";
			break;

		case MIMEEntity.ENC_IDENTITY_7BIT:
			encStr = "7bit";
			break;

		case MIMEEntity.ENC_IDENTITY_8BIT:
			encStr = "8bit";
			break;

		case MIMEEntity.ENC_IDENTITY_BINARY:
			encStr = "binary";
			break;

		case MIMEEntity.ENC_NONE:
			encStr = "no encoding header";
			break;

		case MIMEEntity.ENC_QUOTED_PRINTABLE:
			encStr = "quoted-printable";
			break;

		default:
			break;
		}

		writeSpan(rw, "Encoding", encStr);

	}

	@SuppressWarnings("unchecked")
	private void writeHeaders(ResponseWriter rw, MIMEEntity mime)
			throws IOException, NotesException {

		rw.startElement("br", null);
		rw.endElement("br");

		rw.startElement("p", null);
		rw.writeAttribute("style",
				"border: 1px solid gray; background-color: #FFC;", null);

		Vector<MIMEHeader> headers = mime.getHeaderObjects();

		rw.startElement("table", null);

		for (MIMEHeader header : headers) {

			// writeTableRowString(rw, header.getHeaderName(),
			// header.getHeaderVal());

			writeTableRowString(rw, header.getHeaderName(),
					header.getHeaderValAndParams());

		}

		rw.endElement("table");

		rw.endElement("p");

	}

	private void writeContent(ResponseWriter rw, MIMEEntity mime)
			throws IOException, NotesException {

		String content = mime.getContentAsText();

		if (StringUtil.isEmpty(content))
			return;

		rw.startElement("br", null);
		rw.endElement("br");

		rw.startElement("p", null);
		rw.writeAttribute("style",
				"border: 1px solid gray; background-color: #EEE;", null);

		rw.writeText(content, null);

		rw.startElement(HtmlUtil.SPAN, null);
		rw.writeAttribute(HtmlUtil.STYLE, "color: blue;", null);
		rw.writeText("=END=", null);
		rw.endElement(HtmlUtil.SPAN);

		rw.endElement("p");

	}

	private void writeMimeEntity(ResponseWriter rw, MIMEEntity mime)
			throws IOException, NotesException {

		rw.startElement(LISTITEM, null);

		String contentType = mime.getContentType();
		String contentSubType = mime.getContentSubType();

		String disposition = MimeUtil.getDisposition(mime);

		if (StringUtil.isNotEmpty(contentType)) {
			rw.writeText(contentType, null);
		} else {
			rw.writeText("???", null);
		}
		rw.writeText("/", null);

		if (StringUtil.isNotEmpty(contentSubType)) {
			rw.writeText(contentSubType, null);
		} else {
			rw.writeText("???", null);
		}

		if (StringUtil.isNotEmpty(mime.getCharset())) {

			writeSpan(rw, "Charset", mime.getCharset());

		}

		if (StringUtil.isNotEmpty(disposition)) {
			writeSpan(rw, "Disposition", disposition);
		}

		writeEncoding(rw, mime);

		if (getShowHeaders()) {
			writeHeaders(rw, mime);
		}

		if (getShowContent()) {
			writeContent(rw, mime);
		}

		rw.endElement(LISTITEM);

	}

	public Boolean getShowContent() {

		if (this.showContent != null) {
			return this.showContent;
		}

		ValueBinding vb = getValueBinding("showContent");

		if (vb != null) {
			return (Boolean) vb.getValue(getFacesContext());
		}

		return false;

	}

	public void setShowContent(Boolean showContent) {
		this.showContent = showContent;
	}

	public Boolean getShowHeaders() {

		if (this.showHeaders != null) {
			return this.showHeaders;
		}

		ValueBinding vb = getValueBinding("showHeaders");

		if (vb != null) {
			return (Boolean) vb.getValue(getFacesContext());
		}

		return false;

	}

	public void setShowHeaders(Boolean showHeaders) {
		this.showHeaders = showHeaders;
	}

	@Override
	public Object saveState(FacesContext context) {

		Object state[] = new Object[3];

		state[0] = super.saveState(context);
		state[1] = this.showContent;
		state[2] = this.showHeaders;

		return state;

	}

	@Override
	public void restoreState(FacesContext context, Object object) {

		Object state[] = (Object[]) object;

		super.restoreState(context, state[0]);

		this.showContent = (Boolean) state[1];
		this.showHeaders = (Boolean) state[2];

	}

}
