package com.gregorbyte.xsp.component.bootstrap;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import com.gregorbyte.xsp.util.GregorbyteUtil;
import com.ibm.xsp.component.UIPanelEx;

public class UIContainer extends UIPanelEx {

	public Boolean fluid;

	public UIContainer() {

	}

	@Override
	public String getStyleClass() {

		String parent = super.getStyleClass();

		if (isFluid()) {
			return GregorbyteUtil.concatStyleClasses("container-fluid", parent);
		} else {
			return GregorbyteUtil.concatStyleClasses("container", parent);
		}

	}

	public Boolean isFluid() {

		if (this.fluid != null) {
			return this.fluid;
		}

		ValueBinding vb = getValueBinding("fluid");

		if (vb != null) {
			return (Boolean) vb.getValue(getFacesContext());
		}

		return false;

	}

	public void setFluid(Boolean fluid) {
		this.fluid = fluid;
	}

	@Override
	public void restoreState(FacesContext context, Object state) {

		Object[] values = (Object[]) state;

		super.restoreState(context, values[0]);
		fluid = (Boolean) values[1];

	}

	@Override
	public Object saveState(FacesContext context) {

		Object[] values = new Object[2];

		values[0] = super.saveState(context);
		values[1] = fluid;

		return values;

	}

}
