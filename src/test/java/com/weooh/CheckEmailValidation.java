package com.weooh;

import java.util.ArrayList;

import com.weooh.clair.validation.IFieldValidation;

public class CheckEmailValidation implements IFieldValidation<String> {

	@Override
	public ArrayList<String> isValidField(String obj) {
		ArrayList<String> a = new ArrayList<String>();
		
		if (!obj.contains("@"))
			a.add("Is not a email format");
		return a;
	}

}
