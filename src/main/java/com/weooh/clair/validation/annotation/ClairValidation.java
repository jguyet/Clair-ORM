package com.weooh.clair.validation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.weooh.clair.validation.IFieldValidation;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ClairValidation {
	public Class<? extends IFieldValidation<?>> method();
}
