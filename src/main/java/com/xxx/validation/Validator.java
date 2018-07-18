package com.xxx.validation;

import java.util.Set;

import javax.validation.Configuration;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.bootstrap.ProviderSpecificBootstrap;

import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;

public class Validator {
	private static ProviderSpecificBootstrap<HibernateValidatorConfiguration> psb = Validation.byProvider(HibernateValidator.class);
	private static Configuration<?> configuration = psb.configure();
	
	private static ValidatorFactory factory = configuration.buildValidatorFactory();//Validation.buildDefaultValidatorFactory();
	private static javax.validation.Validator validator = factory.getValidator();
	
	private static Logger logger = (Logger) LoggerFactory.getLogger(Validator.class);

	public static boolean validateObject(Object e) {
		
		Set<ConstraintViolation<Object>> constraintViolations = validator.validate(e);
		 
		if (constraintViolations.size() > 0 ) {
			logger.error("{");
			logger.error("	Impossible de valider les donnees du bean <" + e.getClass().getName() + "> : Error information -> ");
			for (ConstraintViolation<Object> contraintes : constraintViolations) {
				logger.error("	" + contraintes.getPropertyPath() + " " + contraintes.getMessage() + "");
			}
			logger.error("}");
			return false;
		}
		logger.debug("{Les donnees du bean <{}> sont valides}", e.getClass().getName());
		return true;
	}
}
