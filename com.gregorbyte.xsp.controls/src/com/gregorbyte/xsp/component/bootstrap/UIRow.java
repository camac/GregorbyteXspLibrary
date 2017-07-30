package com.gregorbyte.xsp.component.bootstrap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.FacesException;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.gregorbyte.xsp.util.GregorbyteUtil;
import com.ibm.xsp.component.UIPanelEx;
import com.ibm.xsp.util.TypedUtil;

public class UIRow extends UIPanelEx {

	private Boolean autoColumn = false;

	public UIRow() {
		super();

	}

	@Override
	public String getStyleClass() {

		String parent = super.getStyleClass();

		return GregorbyteUtil.concatStyleClasses("row", parent);

	}

	@Override
	public void encodeBegin(FacesContext context) throws IOException {

		super.encodeBegin(context);

		calculateColumnSpans(context);
		
		if (autoColumn) {
			context.getResponseWriter().startElement("div", null);
			context.getResponseWriter().writeAttribute("class", "col-md-12", null);
		}

	}

	@Override
	public void encodeEnd(FacesContext context) throws IOException {

		if (autoColumn) {
			context.getResponseWriter().endElement("div");
		}

		super.encodeEnd(context);
	}

	public void calculateColumnSpans(FacesContext context) throws FacesException {

		List<UIComponent> kids = TypedUtil.getChildren(this);

		List<UIColumn> cols = new ArrayList<UIColumn>();

		int totalMediumSpecified = 0;

		for (UIComponent comp : kids) {
			if (comp instanceof UIColumn) {

				UIColumn col = (UIColumn) comp;

				if (col.getSpan() != null) {
					totalMediumSpecified += col.getSpan();
				} else {
					cols.add(col);
				}

			}
		}

		// If No Columns we will render
		autoColumn = (cols.size() == 0);

		if (autoColumn)
			return;

		int remaining = 12 - totalMediumSpecified;

		Integer colGuess = remaining / cols.size();

		if (colGuess != null) {
			for (UIColumn uiColumn : cols) {
				uiColumn.setGuessSpan(colGuess);
			}
		}

	}

	@Override
	public void restoreState(FacesContext context, Object state) {

		Object[] values = (Object[]) state;

		super.restoreState(context, values[0]);
		autoColumn = (Boolean) values[1];

	}

	@Override
	public Object saveState(FacesContext context) {

		Object[] values = new Object[2];

		values[0] = super.saveState(context);
		values[1] = autoColumn;

		return values;

	}

}