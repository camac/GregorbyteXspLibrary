package com.gregorbyte.xsp.validator.phone;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.gregorbyte.xsp.component.phone.CountryCode;
import com.gregorbyte.xsp.component.phone.UIPhoneNumber;
import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.util.StateHolderUtil;
import com.ibm.xsp.validator.AbstractValidator;

public class PhoneNumberValidator extends AbstractValidator {

	public PhoneNumberValidator() {

	}

	public List<CountryCode> countryCodes;

	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {

		// If there is no value then we just return
		if (value == null)
			return;

		List<CountryCode> validCodes = null;
		
		if (component instanceof UIPhoneNumber) {
			System.out.println("Using UIPhoneNumber CountryCodes");
			validCodes = ((UIPhoneNumber)component).getCountryCodes();
		} else {
			validCodes = getCountryCodes();
		}

		// If no countryCodes have been set on this validator then we will
		// just say anything is valid
		if (validCodes == null || validCodes.isEmpty())
			return;

		// Convert to String
		String stringValue = value.toString();

		// Get the PhoneNumberUtil instance
		PhoneNumberUtil util = PhoneNumberUtil.getInstance();

		try {

			// Create a Phone Number from the String
			PhoneNumber number = util.parse(stringValue, null);

			// Get the Region Code
			String regionCode = util.getRegionCodeForNumber(number);

			if (StringUtil.isEmpty(regionCode)) {
				throw createValidatorEx("Region Code could not be determined from the supplied Phone Number");
			}

			for (CountryCode countryCode : validCodes) {
				if (StringUtil.equals(countryCode.getCode(), regionCode)) {
					
					if (component instanceof UIPhoneNumber) {
						((UIPhoneNumber)component).setDefCountryCode(regionCode);
					}
					
					return;
				}
			}

			throw createValidatorEx("Region Code " + regionCode
					+ " is not in the list of valid Region Codes");

		} catch (NumberParseException e) {
			e.printStackTrace();
			throw createValidatorEx("Supplied value was not in a valid phone Number format");
		}

	}

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

		Object state[] = new Object[2];

		state[0] = super.saveState(context);
		state[1] = StateHolderUtil.saveList(context, countryCodes, false);

		return state;

	}

	@Override
	public void restoreState(FacesContext context, Object object) {

		Object state[] = (Object[]) object;

		super.restoreState(context, state[0]);
		this.countryCodes = StateHolderUtil.restoreList(context,
				getComponent(), state[1]);

	}

}
