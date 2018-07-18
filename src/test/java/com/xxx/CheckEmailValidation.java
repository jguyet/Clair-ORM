package com.xxx;

import com.xxx.validation.IFieldValidation;

public class CheckEmailValidation implements IFieldValidation<String> {

	@Override
	public boolean isValidField(String obj) {
		if (!obj.contains("@"))
			return false;
		return true;
	}

}
