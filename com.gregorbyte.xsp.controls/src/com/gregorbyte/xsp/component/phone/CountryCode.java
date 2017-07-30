package com.gregorbyte.xsp.component.phone;

import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import com.ibm.xsp.complex.ValueBindingObjectImpl;

public class CountryCode extends ValueBindingObjectImpl implements Serializable {

	private static final long serialVersionUID = -8018499989974540760L;

	private String code;

	public CountryCode() {};
	
	public CountryCode(String code) {
		this.code = code;
	}
	
	public String getCode() {

		if (this.code != null) {
			return this.code;
		}

		ValueBinding vb = getValueBinding("code");

		if (vb != null) {
			return (String) vb.getValue(getFacesContext());
		}

		return null;

	}

	public void setCode(String code) {
		this.code = code;
	}

	@Override
	public void restoreState(FacesContext context, Object object) {
		
		Object[] state = (Object[])object;
		
		super.restoreState(context, state[0]);
		this.code = (String)state[1];
		
	}

	@Override
	public Object saveState(FacesContext context) {

		Object[] state = new Object[2];
		
		state[0] = super.saveState(context);
		state[1] = this.code;
		
		return state;
		
	}

}
