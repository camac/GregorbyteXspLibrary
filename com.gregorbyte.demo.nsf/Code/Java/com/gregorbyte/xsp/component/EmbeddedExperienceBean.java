package com.gregorbyte.xsp.component;

import java.io.Serializable;

import lotus.domino.Document;
import lotus.domino.MIMEEntity;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.Stream;

import com.ibm.xsp.model.domino.DominoUtils;

public class EmbeddedExperienceBean implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7319072749049673294L;

	public EmbeddedExperienceBean() {

	}

	public static void updateUrl(Document doc) {

		try {

			MIMEEntity mime = doc.getMIMEEntity();

			if (mime == null) {
				System.out.println("no mime entity");
				return;
			}

			MIMEEntity child = mime.createChildEntity();

			StringBuilder sb = new StringBuilder();

			sb.append("<embed>");
			sb.append("<url>");

			sb.append("http://ausdata.jord.com.au/Jord/JobHub/e9000.nsf/ActionActiveActivities.xsp");

			sb.append("</url>");
			sb.append("</embed>");

			Session s = DominoUtils.getCurrentSession();

			Stream stream = s.createStream();
			stream.writeText(sb.toString());
			child.setContentFromText(stream, "application/embed+xml;charset=UTF-8", 0);
			child.encodeContent(MIMEEntity.ENC_BASE64);

			stream.recycle();
			child.recycle();
			
			doc.save();
			
		} catch (NotesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
