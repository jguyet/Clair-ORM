package com.weooh.clair.validation;

import java.util.ArrayList;

public interface IFieldValidation<T> {

	public ArrayList<String> isValidField(T obj);
}
