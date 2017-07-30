package com.gregorbyte.xsp.component.bootstrap;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import com.gregorbyte.xsp.util.GregorbyteUtil;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.component.UIPanelEx;

public class UIColumn extends UIPanelEx {

	private Integer guessSpan = null;

	// Defaults for Medium Columns, use styleClass for specific layouts
	private Integer span = null;

	private String align = null;

	public UIColumn() {
		super();
	}

	private Integer getEffectiveSpan() {
		if (getSpan() != null) {
			return getSpan();
		} else if (getGuessSpan() != null) {
			return getGuessSpan();
		}

		return 12;
	}

	@Override
	public String getStyleClass() {

		String styleClass = super.getStyleClass();

		styleClass = GregorbyteUtil.concatStyleClasses(styleClass, "col-md-" + getEffectiveSpan());

		String align = getAlign();

		if (StringUtil.equals(align, "left")) {
			styleClass = GregorbyteUtil.concatStyleClasses(styleClass, "text-left");
		} else if (StringUtil.equals(align, "right")) {
			styleClass = GregorbyteUtil.concatStyleClasses(styleClass, "text-right");
		} else if (StringUtil.equals(align, "center")) {
			styleClass = GregorbyteUtil.concatStyleClasses(styleClass, "text-center");
		}

		return styleClass;

	}

	public Integer getGuessSpan() {
		return guessSpan;
	}

	public void setGuessSpan(Integer guessSpan) {
		this.guessSpan = guessSpan;
	}

	public Integer getSpan() {

		if (this.span != null) {
			return this.span;
		}

		ValueBinding vb = getValueBinding("span");

		if (vb != null) {
			return (Integer) vb.getValue(getFacesContext());
		}

		return null;

	}

	public void setSpan(Integer span) {
		this.span = span;
	}

	public String getAlign() {

		if (this.align != null) {
			return this.align;
		}

		ValueBinding vb = getValueBinding("align");

		if (vb != null) {
			return (String) vb.getValue(getFacesContext());
		}

		return null;

	}

	public void setAlign(String align) {
		this.align = align;
	}

	@Override
	public void restoreState(FacesContext context, Object state) {

		Object[] values = (Object[]) state;

		super.restoreState(context, values[0]);

		guessSpan = (Integer) values[1];
		span = (Integer) values[2];
		align = (String) values[3];

	}

	@Override
	public Object saveState(FacesContext context) {

		Object[] values = new Object[4];

		values[0] = super.saveState(context);
		values[1] = guessSpan;
		values[2] = span;
		values[3] = align;

		return values;

	}

}