package com.weooh.clair.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.weooh.clair.annotation.store.IFieldGenerateValue;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ClairIfNullGenerateValue {
	public Class<? extends IFieldGenerateValue<?>> method();
}
