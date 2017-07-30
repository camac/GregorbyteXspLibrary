package com.gregorbyte.xsp.component;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import com.ibm.xsp.component.xp.XspTable;

public class UIPanelGrid extends XspTable {

	public static final String STYLEKIT_FAMILY = "Panel.Grid";
	
	private Integer columns = null;

	private String headerClass = null;
	private String footerClass = null;

	private String columnClasses = null;
	private String rowClasses = null;

	private String bgcolor = null;
	private String frame = null;

	public UIPanelGrid() {
		super();
		setRendererType("com.gregorbyte.PanelGrid");

	}

	@Override
	public String getStyleKitFamily() {
		return STYLEKIT_FAMILY;
	}
	
	public Integer getColumns() {

		if (this.columns != null) {
			return this.columns;
		}

		ValueBinding vb = getValueBinding("columns");

		if (vb != null) {
			return (Integer) vb.getValue(getFacesContext());
		}

		return null;

	}

	public void setColumns(Integer columns) {
		this.columns = columns;
	}
	
	public String getHeaderClass() {

		if (this.headerClass != null) {
			return this.headerClass;
		}

		ValueBinding vb = getValueBinding("headerClass");

		if (vb != null) {
			return (String) vb.getValue(getFacesContext());
		}

		return null;

	}

	public void setHeaderClass(String headerClass) {
		this.headerClass = headerClass;
	}
	
	public String getFooterClass() {

		if (this.footerClass != null) {
			return this.footerClass;
		}

		ValueBinding vb = getValueBinding("footerClass");

		if (vb != null) {
			return (String) vb.getValue(getFacesContext());
		}

		return null;

	}

	public void setFooterClass(String footerClass) {
		this.footerClass = footerClass;
	}
	
	public String getBgcolor() {

		if (this.bgcolor != null) {
			return this.bgcolor;
		}

		ValueBinding vb = getValueBinding("bgcolor");

		if (vb != null) {
			return (String) vb.getValue(getFacesContext());
		}

		return null;

	}

	public void setBgcolor(String bgcolor) {
		this.bgcolor = bgcolor;
	}
	
	public String getColumnClasses() {

		if (this.columnClasses != null) {
			return this.columnClasses;
		}

		ValueBinding vb = getValueBinding("columnClasses");

		if (vb != null) {
			return (String) vb.getValue(getFacesContext());
		}

		return null;

	}

	public void setColumnClasses(String columnClasses) {
		this.columnClasses = columnClasses;
	}
	
	public String getRowClasses() {

		if (this.rowClasses != null) {
			return this.rowClasses;
		}

		ValueBinding vb = getValueBinding("rowClasses");

		if (vb != null) {
			return (String) vb.getValue(getFacesContext());
		}

		return null;

	}

	public void setRowClasses(String rowClasses) {
		this.rowClasses = rowClasses;
	}
	
	public String getFrame() {

		if (this.frame != null) {
			return this.frame;
		}

		ValueBinding vb = getValueBinding("frame");

		if (vb != null) {
			return (String) vb.getValue(getFacesContext());
		}

		return null;

	}

	public void setFrame(String frame) {
		this.frame = frame;
	}

	@Override
	public void restoreState(FacesContext context, Object object) {

		Object[] values = (Object[])object;
		
		super.restoreState(context, values[0]);
		this.bgcolor = (String)values[1];
		this.columnClasses = (String)values[2];
		this.columns = (Integer)values[3];
		this.footerClass = (String)values[4];
		this.frame = (String)values[5];
		this.headerClass = (String)values[6];
		this.rowClasses = (String)values[7];
		
	}

	@Override
	public Object saveState(FacesContext context) {

		Object[] values = new Object[8];
		
		values[0] = super.saveState(context);
		values[1] = this.bgcolor;
		values[2] = this.columnClasses;
		values[3] = this.columns;
		values[4] = this.footerClass;
		values[5] = this.frame;
		values[6] = this.headerClass;
		values[7] = this.rowClasses;
		
		return values;
		
		
	}
	
	
	
}
