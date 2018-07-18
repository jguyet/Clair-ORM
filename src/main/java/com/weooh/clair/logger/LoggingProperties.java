package com.weooh.clair.logger;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class LoggingProperties {
	
	public static Level	HIBERNATE_LOGS_LEVEL = Level.OFF;
	
	public static Level	HIKARI_LOGS_LEVEL = Level.OFF;

	public static void load() {
		/**
		 * SET LEVEL LOGS org.hibernate
		 */
		Logger log = (Logger) LoggerFactory.getLogger("org.hibernate.validator.util.Version");
		log.setLevel(LoggingProperties.HIBERNATE_LOGS_LEVEL);
		
		log = (Logger) LoggerFactory.getLogger("org.hibernate.validator.engine.resolver.DefaultTraversableResolver");
		log.setLevel(LoggingProperties.HIBERNATE_LOGS_LEVEL);
		
		log = (Logger) LoggerFactory.getLogger("org.hibernate.validator.xml.ValidationXmlParser");
		log.setLevel(LoggingProperties.HIBERNATE_LOGS_LEVEL);
		
		log = (Logger) LoggerFactory.getLogger("org.hibernate");
		log.setLevel(LoggingProperties.HIBERNATE_LOGS_LEVEL);
		
		/**
		 * SET LEVEL LOGS com.zaxxer.hikari
		 */
		log	= (Logger) LoggerFactory.getLogger("com.zaxxer.hikari");
		log.setLevel(LoggingProperties.HIKARI_LOGS_LEVEL);
	}
}
