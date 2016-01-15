package com.gregorbyte.xsp.converter.phone;

import java.util.HashSet;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.ConverterException;
import javax.faces.el.ValueBinding;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.gregorbyte.xsp.component.phone.UIPhoneNumber;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.converter.AbstractConverter;

public class PhoneNumberConverter extends AbstractConverter {

	private String defaultCountryCode = null;

	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {

		if (StringUtil.isEmpty(value))
			return value;

		PhoneNumberUtil util = PhoneNumberUtil.getInstance();

		String defCountryCode = null;

		if (component instanceof UIPhoneNumber) {
			System.out.println("Using UIPhoneNumber defCountryCode");
			defCountryCode = ((UIPhoneNumber) component).getDefCountryCode();
		} else {
			defCountryCode = getDefaultCountryCode();
		}

		try {

			PhoneNumber number = util.parse(value, defCountryCode);

			if (!util.isValidNumber(number)) {

				FacesMessage fm = new FacesMessage();
				fm.setSummary("Phone Number is not valid for country code "
						+ defCountryCode);
				fm
						.setDetail("The supplied Phone Number is not valid to the country code "
								+ defCountryCode);
				fm.setSeverity(FacesMessage.SEVERITY_ERROR);

				throw new ConverterException(fm);

			} else {

				if (component instanceof UIPhoneNumber
						&& number.hasCountryCode()) {

					String regCode = null;
					if (number.getCountryCode() == 1) {
						regCode = getNanpaRegion(number, util);
					}

					if (regCode == null) {
						regCode = util.getRegionCodeForCountryCode(number
								.getCountryCode());
					}

					((UIPhoneNumber) component).setRegionCode(regCode);

				}

				return util.format(number, PhoneNumberFormat.INTERNATIONAL);
			}

		} catch (NumberParseException e) {

			FacesMessage fm = new FacesMessage();
			fm
					.setSummary("The Phone Number entered is not in a valid format for "
							+ defCountryCode);
			fm.setDetail(e.getMessage());
			fm.setSeverity(FacesMessage.SEVERITY_ERROR);

			throw new ConverterException(fm);
		}
	}

	public String getAsString(FacesContext context, UIComponent component,
			Object value) {

		if (value == null)
			return null;
		return value.toString();

	}

	public String getDefaultCountryCode() {

		if (this.defaultCountryCode != null) {
			return this.defaultCountryCode;
		}

		ValueBinding vb = getValueBinding("defaultCountryCode");
		if (vb != null) {
			return (String) vb.getValue(getFacesContext());
		}

		return "AU";
	}

	public void setDefaultCountryCode(String defaultCountryCode) {
		this.defaultCountryCode = defaultCountryCode;
	}

	public void restoreState(FacesContext context, Object object) {

		Object[] state = (Object[]) object;

		super.restoreState(context, state[0]);
		this.defaultCountryCode = (String) state[1];

	}

	public Object saveState(FacesContext arg0) {

		Object[] state = new Object[2];

		state[0] = super.saveState(arg0);
		state[1] = this.defaultCountryCode;

		return state;
	}

	public Set<String> getNanpaRegions(PhoneNumberUtil util) {

		Set<String> regset = new HashSet<String>();

		for (String region : util.getSupportedRegions()) {

			if (util.isNANPACountry(region)) {
				regset.add(region);
			}

		}

		return regset;

	}

	public String getNanpaRegion(PhoneNumber num, PhoneNumberUtil util) {

		for (String region : getNanpaRegions(util)) {
			if (util.isValidNumberForRegion(num, region)) {
				return region;
			}
		}
		return null;

	}

}
