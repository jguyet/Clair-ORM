package com.xxx;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.LoggerFactory;

import com.xxx.annotation.Column;
import com.xxx.annotation.Primary;
import com.xxx.annotation.Table;
import com.zaxxer.hikari.HikariDataSource;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public abstract class AbstractDAO<T> {

	protected HikariDataSource	dataSource;
	protected Logger			logger	= (Logger) LoggerFactory.getLogger("test");
	protected final Object		locker	= new Object();
	
	private String							tableName;
	private Map<String, FieldInformation>	columns = new HashMap<String, FieldInformation>();
	private Class<?>						sourceClass = null;
	
	public AbstractDAO(HikariDataSource dataSource)
	{
		System.out.println("COUCOU -> " + dataSource);
		this.dataSource = dataSource;
		this.logger.setLevel(Level.ERROR);
		if (sourceClass == null) {
			buildReflection();
		}
	}
	
	@SuppressWarnings("unchecked")
	private Class<?> classForT() {
		return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
	
	private void buildReflection() {
		Class<?> c = classForT();
		
		if (c == null)
			return ;
		Annotation[] annots = c.getAnnotations();
		
		if (annots.length < 0)
			return ;
		this.sourceClass = c;
		for (Annotation a : annots) {
			if (!(a instanceof Table))
				continue ;
			
			Field[] fields = c.getDeclaredFields();
			
			for (int i = 0; i < fields.length; i++) {
				this.addField(fields[i]);
			}
			this.tableName = ((Table)a).value();
			break ;
		}
	}
	
	private void addField(Field field) {
		Annotation[] annots = field.getAnnotations();
		Map<String, Annotation> annotationMap = new HashMap<String, Annotation>();
		FieldInformation fieldInformation = new FieldInformation();
		
		for (Annotation a : annots) {
			annotationMap.put(a.annotationType().getSimpleName(), a);
		}
		
		if (!annotationMap.containsKey("Column")) {
			return ;
		}
		
		field.setAccessible(true);
		fieldInformation.field = field;
		fieldInformation.name = ((Column)annotationMap.get("Column")).value();
		//is primary key and is autoincremented key
		if (annotationMap.containsKey("Primary")) {
			fieldInformation.isPrimary = true;
			fieldInformation.autoIncrement = ((Primary)annotationMap.get("Primary")).autoIncrement();
		}
		this.columns.put(fieldInformation.name, fieldInformation);
	}
	
	public ArrayList<T> find() {
		ArrayList<T> array = new ArrayList<T>();
		Result result = null;
		try
		{
			result = getData("SELECT * FROM " + tableName + "");
			ResultSet RS = result.resultSet;
			while (RS.next())
			{
				T obj = this.newTInstance();
				
				for (Entry<String, FieldInformation> entry : this.columns.entrySet()) {
					entry.getValue().field.setAccessible(true);
					entry.getValue().field.set(obj, getValueFromResult(RS, entry.getKey(), entry.getValue()));
				}
				array.add(obj);
			}
		}
		catch (Exception e)
		{
			this.sendError(this.tableName + " error find", e);
		}
		finally
		{
			close(result);
		}
		return array;
	}
	
	public ArrayList<T> find(String query) {
		return null;
	}
	
	/**
	 * Example :<br>
	 * findOne("WHERE id=1")
	 * @param query
	 * @return
	 */
	public T findOne(String query) {
		T obj = null;
		Result result = null;
		try
		{
			result = getData("SELECT * FROM " + tableName + " " + query.trim());
			ResultSet RS = result.resultSet;
			if (RS.next())
			{
				obj = this.newTInstance();
				
				for (Entry<String, FieldInformation> entry : this.columns.entrySet()) {
					entry.getValue().field.setAccessible(true);
					entry.getValue().field.set(obj, getValueFromResult(RS, entry.getKey(), entry.getValue()));
				}
			}
		}
		catch (Exception e)
		{
			this.sendError(this.tableName + " error findOne", e);
		}
		finally
		{
			close(result);
		}
		return obj;
	}
	
	/**
	 * Example :<br>
	 * .findOne("id", 1);
	 * @param key
	 * @param value
	 * @return 
	 */
	public T findOne(String key, Object value) {
		T obj = null;
		Result result = null;
		try
		{
			result = getData("SELECT * FROM " + tableName + " WHERE " + key + " = '" + value + "'");
			ResultSet RS = result.resultSet;
			if (RS.next())
			{
				obj = this.newTInstance();
				
				for (Entry<String, FieldInformation> entry : this.columns.entrySet()) {
					entry.getValue().field.setAccessible(true);
					entry.getValue().field.set(obj, getValueFromResult(RS, entry.getKey(), entry.getValue()));
				}
			}
		}
		catch (Exception e)
		{
			this.sendError(this.tableName + " error findOne", e);
		}
		finally
		{
			close(result);
		}
		return obj;
	}
	
	public long count() {
		return 0L;
	}
	
	public long count(String query) {
		return 0L;
	}
	
	public void delete(T entity) {
		
	}
	
	public void delete(String key, Object value) {
		
	}
	
	public void save(T entity) {
		
	}
	
	public void update(T entity) {
		
	}
	
	public boolean exists(String key, Object value) {
		return false;
	}
	
	@SuppressWarnings("unchecked")
	private T newTInstance() {
		try {
			Constructor<?> co = this.sourceClass.getConstructor();
			
			co.setAccessible(true);
			return (T) co.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Object getValueFromResult(ResultSet RS, String columName, FieldInformation fieldInformation) throws SQLException {
		if (fieldInformation.field.getType() == Integer.class) {
			return RS.getInt(columName);
		}
		if (fieldInformation.field.getType() == String.class) {
			return RS.getString(columName);
		}
		return null;
	}
	
	protected class FieldInformation {
		public String name = null;
		public Field field = null;
		public boolean isPrimary = false;
		public boolean autoIncrement = false;
	}
	
	protected class Result
	{
		public final Connection	connection;
		public final ResultSet	resultSet;

		protected Result(Connection connection, ResultSet resultSet)
		{
			this.connection = connection;
			this.resultSet = resultSet;
		}
	}
	
	public boolean existTable()
	{
		Result result = null;
		try
		{
			result = getDatanull("SELECT * from " + tableName + " LIMIT 0,1");
			ResultSet RS = result.resultSet;
			if (RS.next())
				return true;
			else
				return true;
		}catch (Exception e)
		{
			
		}
		finally
		{
			close(result);
		}
		return false;
	}

	protected void execute(String query)
	{
		if (dataSource.isClosed())
			return ;
		synchronized(locker)
		{
			Connection connection = null;
			Statement statement = null;
			try
			{
				connection = dataSource.getConnection();
				statement = connection.createStatement();
				statement.execute(query);
				logger.debug("SQL request executed successfully {}", query);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				logger.error("Can't execute SQL Request :" + query, e);
				try
				{
					if (connection != null)
					{
						connection.setAutoCommit(false);
						connection.rollback();
						connection.setAutoCommit(true);
					}
				}
				catch (Exception e1)
				{
					logger.error("Can't rollback", e1);
				}
			}
			finally
			{
				close(statement);
				close(connection);
			}
		}
	}

	protected void execute(PreparedStatement statement)
	{
		if (dataSource.isClosed())
			return ;
		synchronized(locker)
		{
			Connection connection = null;
			try
			{
				if (statement != null && !statement.isClosed())
				{
					connection = statement.getConnection();
					if (connection != null && !connection.isClosed())
					{
						statement.setQueryTimeout(1);
						statement.execute();
						logger.debug("SQL request executed successfully {}", statement.toString());
					}
					else
					{
						close(statement);
						return;
					}
				}
			}
			catch (SQLException e)
			{
				logger.error("Can't execute SQL Request :"
						+ statement.toString(), e);
				try
				{
					if (statement != null && !statement.isClosed())
						close(statement);
					if (connection != null && !connection.isClosed())
						close(connection);
					return ;
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
			finally
			{
				close(statement);
				close(connection);
			}
		}
	}

	protected Result getData(String query)
	{
		if (dataSource.isClosed())
			return null;
		synchronized(locker)
		{
			Connection connection = null;
			Statement statement = null;
			try
			{
				if (query == null)
					return null;
				if (!query.endsWith(";"))
					query = query + ";";
				connection = dataSource.getConnection();
				statement = connection.createStatement();
				Result result = new Result(connection, statement.executeQuery(query));
				logger.debug("SQL request executed successfully {}", query);
				return result;
			}
			catch (SQLException e)
			{
				logger.error("Can't execute SQL Request :" + query, e);
				e.printStackTrace();
				try
				{
					if (statement != null)
					{
						close(statement);
					}
					if (connection != null)
					{
						logger.error("Connection rollback");
						connection.setAutoCommit(false);
						connection.rollback();
						connection.setAutoCommit(true);
					}
				}
				catch (Exception e1)
				{
					logger.error("Can't rollback", e1);
				}
			}
			return null;
		}
	}
	
	protected Result getDatanull(String query)
	{
		if (dataSource.isClosed())
			return null;
		synchronized(locker)
		{
			Connection connection = null;
			Statement statement = null;
			try
			{
				if (query == null)
					return null;
				if (!query.endsWith(";"))
					query = query + ";";
				connection = dataSource.getConnection();
				statement = connection.createStatement();
				Result result = new Result(connection, statement.executeQuery(query));
				logger.debug("SQL request executed successfully {}", query);
				return result;
			}
			catch (SQLException e)
			{
				
			}
			return null;
		}
	}
	
    protected PreparedStatement getPreparedStatement(String query) throws SQLException {//shaaf
    	try {
	       Connection connection = dataSource.getConnection();
	       return connection.prepareStatement(query);
    	}catch(SQLException e)
    	{
    		logger.error("Erreur SQL : " + query, e);
    		Connection connection = dataSource.getConnection();
 	       return connection.prepareStatement(query);
    	}
    }

	protected void close(PreparedStatement statement)
	{
		if (statement == null)
			return;
		try
		{
			close(statement.getConnection());
			statement.clearParameters();
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Can't close statement", e);
		}
	}

	protected void close(Connection connection)
	{
		if (connection == null)
			return;
		try
		{
			connection.close();
			logger.trace("{} released", connection);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Can't close connection", e);
		}
	}

	protected void close(Statement statement)
	{
		if (statement == null)
			return;
		try
		{
			statement.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Can't close statement", e);
		}
	}

	protected void close(ResultSet resultSet)
	{
		if (resultSet == null)
			return;
		try
		{
			resultSet.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.error("Can't close resultSet", e);
		}
	}

	protected void close(Result result)
	{
		if (result != null)
		{
			if (result.resultSet != null)
				close(result.resultSet);
			if (result.connection != null)
				close(result.connection);
			logger.trace("Connection {} has been released", result.connection);
		}
	}

	protected void sendError(String msg, Exception e)
	{
		e.printStackTrace();
		logger.error("Erreur Sql {} : {}", msg, e.getMessage());
	}
	
	protected int getCount()
	{
		Result result = null;
		int count = 0;
		try
		{
			result = getDatanull("SELECT count(*) AS n FROM `" + this.tableName + "`");
			ResultSet RS = result.resultSet;

			boolean found = RS.first();

			if (found)
				count = RS.getInt("n");
		}
		catch (SQLException e)
		{
			return (0);
		}
		finally
		{
			close(result);
		}
		return count;
	}
	
}
