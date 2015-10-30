package com.gregorbyte.xsp.component;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import name.fraser.neil.plaintext.DiffMatchPatch.Diff;
import name.fraser.neil.plaintext.DiffMatchPatch.Operation;

import com.ibm.xsp.component.UIOutputEx;

public class UITextDiff extends UIOutputEx {

	public static final String STYLEKIT_FAMILY = "Text.Diff";

	private String from = null;
	private String to = null;

	// Styles when Rendering
	private String insertStyle = null;
	private String insertStyleClass = null;
	private static final String DEFAULT_INSERTSTYLE = "color: green;";

	private String deleteStyle = null;
	private String deleteStyleClass = null;
	private static final String DEFAULT_DELETESTYLE = "color: red; text-decoration: line-through";

	private String equalStyle = null;
	private String equalStyleClass = null;

	private String cleanup = null;
	public static final String CLEANUP_SEMANTIC = "semantic";
	public static final String CLEANUP_EFFICIENCY = "efficiency";
	public static final String CLEANUP_NOCLEANUP = "none";
	private static final String DEFAULT_CLEANUP = CLEANUP_NOCLEANUP;

	private Short editCost = null;
	private Float timeout = null;

	public UITextDiff() {
		super();
		setRendererType("com.gregorbyte.TextDiff");
	}

	@Override
	public String getStyleKitFamily() {
		return STYLEKIT_FAMILY;
	}

	// Properties
	public String getFrom() {

		if (from != null) {
			return from;
		}

		ValueBinding vb = getValueBinding("from");

		if (vb != null) {
			return (String) vb.getValue(FacesContext.getCurrentInstance());
		}

		return null;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {

		if (this.to != null) {
			return this.to;
		}

		ValueBinding vb = getValueBinding("to");

		if (vb != null) {
			return (String) vb.getValue(getFacesContext());
		}

		return null;

	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getStyle(Diff diff) {

		if (diff == null)
			return null;

		return getStyle(diff.operation);

	}

	public String getStyle(Operation operation) {

		if (operation == null)
			return null;

		if (operation.equals(Operation.INSERT)) {
			return getInsertStyle();
		} else if (operation.equals(Operation.DELETE)) {
			return getDeleteStyle();
		} else if (operation.equals(Operation.EQUAL)) {
			return getEqualStyle();
		}

		return null;

	}

	public String getStyleClass(Diff diff) {

		if (diff == null)
			return null;

		return getStyleClass(diff.operation);

	}

	public String getStyleClass(Operation operation) {

		if (operation == null)
			return null;

		if (operation.equals(Operation.INSERT)) {
			return getInsertStyleClass();
		} else if (operation.equals(Operation.DELETE)) {
			return getDeleteStyleClass();
		} else if (operation.equals(Operation.EQUAL)) {
			return getEqualStyleClass();
		}

		return null;

	}

	// Styles Below

	public String getInsertStyle() {

		if (this.insertStyle != null) {
			return this.insertStyle;
		}

		ValueBinding vb = getValueBinding("insertStyle");

		if (vb != null) {
			return (String) vb.getValue(getFacesContext());
		}

		return DEFAULT_INSERTSTYLE;

	}

	public void setInsertStyle(String insertStyle) {
		this.insertStyle = insertStyle;
	}

	public String getInsertStyleClass() {

		if (this.insertStyleClass != null) {
			return this.insertStyleClass;
		}

		ValueBinding vb = getValueBinding("insertStyleClass");

		if (vb != null) {
			return (String) vb.getValue(getFacesContext());
		}

		return null;

	}

	public void setInsertStyleClass(String insertStyleClass) {
		this.insertStyleClass = insertStyleClass;
	}

	public String getDeleteStyle() {

		if (this.deleteStyle != null) {
			return this.deleteStyle;
		}

		ValueBinding vb = getValueBinding("deleteStyle");

		if (vb != null) {
			return (String) vb.getValue(getFacesContext());
		}

		return DEFAULT_DELETESTYLE;

	}

	public void setDeleteStyle(String deleteStyle) {
		this.deleteStyle = deleteStyle;
	}

	public String getDeleteStyleClass() {

		if (this.deleteStyleClass != null) {
			return this.deleteStyleClass;
		}

		ValueBinding vb = getValueBinding("deleteStyleClass");

		if (vb != null) {
			return (String) vb.getValue(getFacesContext());
		}

		return null;

	}

	public void setDeleteStyleClass(String deleteStyleClass) {
		this.deleteStyleClass = deleteStyleClass;
	}

	public String getEqualStyle() {

		if (this.equalStyle != null) {
			return this.equalStyle;
		}

		ValueBinding vb = getValueBinding("equalStyle");

		if (vb != null) {
			return (String) vb.getValue(getFacesContext());
		}

		return null;

	}

	public void setEqualStyle(String equalStyle) {
		this.equalStyle = equalStyle;
	}

	public String getEqualStyleClass() {

		if (this.equalStyleClass != null) {
			return this.equalStyleClass;
		}

		ValueBinding vb = getValueBinding("equalStyleClass");

		if (vb != null) {
			return (String) vb.getValue(getFacesContext());
		}

		return null;

	}

	public void setEqualStyleClass(String equalStyleClass) {
		this.equalStyleClass = equalStyleClass;
	}

	public String getCleanup() {

		if (this.cleanup != null) {
			return this.cleanup;
		}

		ValueBinding vb = getValueBinding("cleanup");

		if (vb != null) {
			return (String) vb.getValue(getFacesContext());
		}

		return DEFAULT_CLEANUP;

	}

	public void setCleanup(String cleanup) {
		this.cleanup = cleanup;
	}

	public Float getTimeout() {

		if (this.timeout != null) {
			return this.timeout;
		}

		ValueBinding vb = getValueBinding("timeout");

		if (vb != null) {
			return (Float) vb.getValue(getFacesContext());
		}

		return null;

	}

	public void setTimeout(Float timeout) {
		this.timeout = timeout;
	}

	public Short getEditCost() {

		if (this.editCost != null) {
			return this.editCost;
		}

		ValueBinding vb = getValueBinding("editCost");

		if (vb != null) {
			return (Short) vb.getValue(getFacesContext());
		}

		return null;

	}

	public void setEditCost(Short editCost) {
		this.editCost = editCost;
	}

	@Override
	public Object saveState(FacesContext context) {

		Object state[] = new Object[12];

		state[0] = super.saveState(context);
		state[1] = this.from;
		state[2] = this.to;

		state[3] = this.insertStyle;
		state[4] = this.insertStyleClass;
		state[5] = this.equalStyle;
		state[6] = this.equalStyleClass;
		state[7] = this.deleteStyle;
		state[8] = this.deleteStyleClass;

		state[9] = this.cleanup;

		state[10] = this.timeout;
		state[11] = this.editCost;

		return state;

	}

	@Override
	public void restoreState(FacesContext context, Object object) {

		Object state[] = (Object[]) object;

		super.restoreState(context, state[0]);

		this.from = (String) state[1];
		this.to = (String) state[2];

		this.insertStyle = (String) state[3];
		this.insertStyleClass = (String) state[4];
		this.equalStyle = (String) state[5];
		this.equalStyleClass = (String) state[6];
		this.deleteStyle = (String) state[7];
		this.deleteStyleClass = (String) state[8];

		this.cleanup = (String) state[9];

		this.timeout = (Float) state[10];
		this.editCost = (Short) state[11];

	}

}
