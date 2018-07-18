package com.xxx;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class Clair {

	private HikariDataSource		dataSource;
	private HikariConfig			hikariConfig;
	
	private static Logger			logger	= (Logger) LoggerFactory.getLogger(Clair.class);
	
	public void buildCredentials() {
		//TODO by properties files
	}
	
	public void buildCredentials(HikariConfig config) {
		this.hikariConfig = config;
	}
	
	public void buildCredentials(String hostDB, int portDB, String dataBaseName, String userDB, String passwordDB) {
		
		HikariConfig config = new HikariConfig();
		config.setDataSourceClassName("org.mariadb.jdbc.MySQLDataSource");
		config.addDataSourceProperty("serverName", hostDB);
		config.addDataSourceProperty("port", portDB);
		config.addDataSourceProperty("databaseName", dataBaseName);
		config.addDataSourceProperty("user", userDB);
		config.addDataSourceProperty("password", passwordDB);
		config.setAutoCommit(true);// AutoCommit, control les request
		config.setConnectionTimeout(3000);//timeout
		config.setMaximumPoolSize(1000);//nombre de request simultane
		config.setMinimumIdle(1);
		config.setIdleTimeout(1000);//temp de request normal a value
		config.setConnectionTimeout(251);
		//config.setMaxLifetime();//a voir si a utilise
		this.hikariConfig = config;
	}
	
	public boolean initializeConnection()
	{
		try
		{
			logger.setLevel(Level.OFF);
			logger.trace("Reading database config");
			this.dataSource = new HikariDataSource(this.hikariConfig);
			if (!testConnection(dataSource))
			{
				logger.error("Pleaz check your username and password and database connection");
				return false;
			}
			logger.info("Database connection established");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Erreur sql : " + e.getMessage());
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T buildDAO(Class<T> c) {
		Annotation[] annots = c.getAnnotations();
		Map<String, Class<?>> an = new HashMap<String, Class<?>>();
		
		if (c.getSuperclass() != AbstractDAO.class) {
			logger.error("Class DAO {} is not a instanceof {}", c.getSimpleName(), AbstractDAO.class.getSimpleName());
			return null;
		}
		if (annots.length < 0) {
			logger.error("Annotation not found in Class DAO {}", c.getSimpleName());
			return null;
		}
		for (Annotation a : annots) {
			an.put(a.annotationType().getSimpleName(), c);
		}
		if (!an.containsKey("ObjectDAO")) {
			logger.error("Annotation ObjectDAO not found in Class DAO {}", c.getSimpleName());
			return null;
		}
		try {
			Constructor<?> constructor = c.getDeclaredConstructor(HikariDataSource.class);
			
			constructor.setAccessible(true);
			
			return (T)constructor.newInstance(this.dataSource);
			
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Exception error in Class DAO {}", c.getSimpleName());
		}
		return null;
	}
	
	private boolean testConnection(HikariDataSource dataSource)
	{
		try
		{
			Connection connection = dataSource.getConnection();
			connection.close();
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}
}
