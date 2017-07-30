package com.gregorbyte.xsp.component.phone;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.convert.ConverterException;

import com.gregorbyte.xsp.converter.phone.PhoneNumberConverter;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.component.xp.XspInputText;
import com.ibm.xsp.renderkit.ReadOnlyAdapterRenderer;
import com.ibm.xsp.util.StateHolderUtil;

public class UIPhoneNumber extends XspInputText {

	private static final String FAMILY = "com.gregorbyte.xsp.phonenumber";

	public List<CountryCode> countryCodes;
	private String defCountryCode = null;

	private String regionCode = null;

	public UIPhoneNumber() {
		super();
		setRendererType("com.gregorbyte.PhoneNumber");
		setConverter(new PhoneNumberConverter());

	}

	public boolean isReadOnly(FacesContext context) {
		return ReadOnlyAdapterRenderer.isReadOnly(context, this);
	}

	@SuppressWarnings("unused")
	private String getCountryCode() {
		if (getValueAsString().contains("!")) {

			String[] bits = getValueAsString().split("!");
			return bits[0];

		}
		return null;
	}

	private String getNumber() {
		if (getValueAsString().contains("!")) {

			System.out.println("This is the value" + getValueAsString());

			String[] bits = getValueAsString().split("!");

			if (bits.length > 1)
				return bits[1];
			else
				return "";

		}
		return getValueAsString();
	}

	private void renderInputBox(FacesContext context) throws IOException {

		ResponseWriter rw = context.getResponseWriter();

		rw.startElement("input", this);
		rw.writeAttribute("id", getClientId(context), null);
		rw.writeAttribute("name", getClientId(context), null);

		rw.writeAttribute("value", getNumber(), null);

		rw.endElement("input");

	}

	private void renderComboBox(FacesContext context) throws IOException {

		ResponseWriter rw = context.getResponseWriter();

		List<String> countries = new ArrayList<String>();

		for (CountryCode countryCode : getCountryCodes()) {
			countries.add(countryCode.getCode());
		}

		rw.startElement("select", this);

		String id = getCountryComboId(context);

		String selectedCountry = getDefCountryCode();

		rw.writeAttribute("id", id, null);
		rw.writeAttribute("name", id, null);

		System.out.println("At this Point getValue is: " + getValueAsString());

		for (String string : countries) {

			rw.startElement("option", null);
			rw.writeAttribute("value", string, null);

			if (StringUtil.equals(selectedCountry, string)) {
				rw.writeAttribute("selected", "true", null);
			}

			rw.write(string);
			rw.endElement("option");

		}

		rw.endElement("select");

	}

	private String getCountryComboId(FacesContext context) {
		return getClientId(context) + "_countryCode";
	}

	@Override
	public void decode(FacesContext context) {

		ExternalContext external = context.getExternalContext();

		String numberId = getClientId(context);
		String countryId = getCountryComboId(context);

		String numberValue = (String) external.getRequestParameterMap().get(
				numberId);
		String countryValue = (String) external.getRequestParameterMap().get(
				countryId);

		if (StringUtil.isNotEmpty(numberValue)
				|| StringUtil.isNotEmpty(countryValue)) {

			setSubmittedValue(numberValue);
			setDefCountryCode(countryValue);
			setValid(true);
			System.out.println("Submitted " + numberValue + " " + countryValue);

		}

	}

	@Override
	public void updateModel(FacesContext arg0) {

		System.out.println("Updating the Model Values");
		super.updateModel(arg0);
	}

	@Override
	protected Object getConvertedValue(FacesContext arg0, Object arg1)
			throws ConverterException {

		System.out.println("Getting Converted Value");

		if (arg1 != null) {
			System.out.println(arg1.toString());
		}

		Object o = super.getConvertedValue(arg0, arg1);

		if (o != null) {
			System.out.println(o.toString());
		}

		return o;
	}

	@Override
	public boolean isValid() {

		boolean valid = super.isValid();

		if (valid) {
			System.out.println(" I am Valid ");
		} else {
			System.out.println(" I am Not Valid");
		}

		return valid;
	}

	@Override
	public void setValid(boolean valid) {

		if (valid) {
			System.out.println(" Setting as Valid ");
		} else {
			System.out.println(" Setting as not Valid");
		}

		super.setValid(valid);
	}

	// @Override
	// public String getFamily() {
	// return FAMILY;
	// }

	public void addCountryCode(CountryCode countryCode) {

		if (this.countryCodes == null) {
			this.countryCodes = new ArrayList<CountryCode>();
		}
		this.countryCodes.add(countryCode);

	}

	public List<CountryCode> getCountryCodes() {
		return countryCodes;
	}

	public void setCountryCodes(List<CountryCode> countryCodes) {
		this.countryCodes = countryCodes;
	}

	@Override
	public Object saveState(FacesContext context) {

		Object state[] = new Object[3];

		state[0] = super.saveState(context);
		state[1] = StateHolderUtil.saveList(context, countryCodes, false);
		state[2] = this.defCountryCode;

		return state;

	}

	@Override
	public void restoreState(FacesContext context, Object object) {

		Object state[] = (Object[]) object;

		super.restoreState(context, state[0]);
		this.countryCodes = StateHolderUtil
				.restoreList(context, this, state[1]);
		this.defCountryCode = (String) state[2];

	}

	public void setDefCountryCode(String defCountryCode) {
		this.defCountryCode = defCountryCode;
	}

	public String getDefCountryCode() {
		return defCountryCode;
	}

	public void setRegionCode(String regionCode) {
		this.regionCode = regionCode;
	}

	public String getRegionCode() {
		return regionCode;
	}

}
