package com.weooh.clair;

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
import java.util.stream.Stream;

import org.slf4j.LoggerFactory;

import com.weooh.clair.annotation.ClairIfNullGenerateValue;
import com.weooh.clair.annotation.Column;
import com.weooh.clair.annotation.Primary;
import com.weooh.clair.annotation.Table;
import com.weooh.clair.annotation.store.IFieldGenerateValue;
import com.weooh.clair.validation.IFieldValidation;
import com.weooh.clair.validation.Validator;
import com.weooh.clair.validation.annotation.ClairValidation;
import com.zaxxer.hikari.HikariDataSource;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import static java.util.stream.Collectors.toMap;

public abstract class AbstractDAO<T> {

	private HikariDataSource				dataSource;
	private Logger							logger	= (Logger) LoggerFactory.getLogger("test");
	private String							tableName;
	private Map<String, FieldInformation>	fields = new HashMap<String, FieldInformation>();
	private Map<String, FieldInformation>	validationsFields = new HashMap<String, FieldInformation>();
	private Map<String, FieldInformation>	generationsFields = new HashMap<String, FieldInformation>();
	private Class<?>						sourceClass = null;
	
	private final Object					locker	= new Object();
	
	public AbstractDAO(@org.jetbrains.annotations.NotNull ClairDataSource dataSource) throws SQLException
	{
		this.dataSource = dataSource.dataSource;
		this.logger.setLevel(Level.ERROR);
		if (this.sourceClass == null) {
			buildReflection();
		}
	}
	
	@SuppressWarnings("unchecked")
	private Class<?> classForT() {
		return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }
	
	private void buildReflection() throws SQLException {
		Class<?> c = classForT();
		
		if (c == null)
			return ;
		Annotation[] annots = c.getAnnotations();

		if (annots.length < 0)
			return ;
		this.sourceClass = c;
		this.tableName = Stream.of(c.getAnnotations())
				.filter((a) -> (a instanceof Table))
				.findFirst()
				.map((a) -> {
					Stream.of(c.getDeclaredFields()).forEach(this::addField);
					return ((Table)a).value();
				}).orElseThrow(() -> new SQLException("No annotation Table !"));
	}
	
	private void addField(Field field) {
		Map<Class<?>, Annotation> annotationMap = Stream.of(field.getAnnotations())
				.collect(toMap(Annotation::annotationType, x -> x));
		FieldInformation fieldInformation = new FieldInformation();

		if (!annotationMap.containsKey(Column.class)) {
			return ;
		}

		field.setAccessible(true);
		fieldInformation.field = field;
		fieldInformation.name = ((Column)annotationMap.get(Column.class)).value();
		//is primary key and is autoincremented key
		if (annotationMap.containsKey(Primary.class)) {
			fieldInformation.isPrimary = true;
			fieldInformation.autoIncrement = ((Primary)annotationMap.get(Primary.class)).autoIncrement();
		}
		this.fields.put(fieldInformation.name, fieldInformation);
		
		//validations
		if (annotationMap.containsKey(ClairValidation.class)) {
			this.validationsFields.put(fieldInformation.name, fieldInformation);
		}
		
		//generators
		if (annotationMap.containsKey(ClairIfNullGenerateValue.class)) {
			this.generationsFields.put(fieldInformation.name, fieldInformation);
		}
	}
	
	/**
	 * Get all Object from table
	 * @return
	 */
	public ArrayList<T> find() {
		ArrayList<T> array = new ArrayList<T>();
		Result result = null;
		try
		{
			result = getData("SELECT * FROM `" + tableName + "`");
			ResultSet RS = result.resultSet;
			while (RS.next())
			{
				T obj = this.newTInstance();
				
				for (Entry<String, FieldInformation> entry : this.fields.entrySet()) {
					entry.getValue().field.setAccessible(true);
					entry.getValue().field.set(obj, getValueFromResult(RS, entry.getKey(), entry.getValue()));
				}
				array.add(obj);
			}
		}
		catch (Exception e)
		{
			sendError("Error find() from " + this.tableName, e);
		}
		finally
		{
			close(result);
		}
		return array;
	}
	
	public ArrayList<T> find(String query) {
		ArrayList<T> array = new ArrayList<T>();
		Result result = null;
		try
		{
			result = getData("SELECT * FROM `" + tableName + "` " + query);
			ResultSet RS = result.resultSet;
			while (RS.next())
			{
				T obj = this.newTInstance();
				
				for (Entry<String, FieldInformation> entry : this.fields.entrySet()) {
					entry.getValue().field.setAccessible(true);
					entry.getValue().field.set(obj, getValueFromResult(RS, entry.getKey(), entry.getValue()));
				}
				array.add(obj);
			}
		}
		catch (Exception e)
		{
			sendError("Error find(String query) from " + this.tableName, e);
		}
		finally
		{
			close(result);
		}
		return array;
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
			result = getData("SELECT * FROM `" + tableName + "` " + query.trim());
			ResultSet RS = result.resultSet;
			if (RS.next())
			{
				obj = this.newTInstance();
				
				for (Entry<String, FieldInformation> entry : this.fields.entrySet()) {
					entry.getValue().field.setAccessible(true);
					entry.getValue().field.set(obj, getValueFromResult(RS, entry.getKey(), entry.getValue()));
				}
			}
		}
		catch (Exception e)
		{
			sendError("Error findOne(String query) from " + this.tableName, e);
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
			result = getData("SELECT * FROM `" + tableName + "` WHERE " + key + " = '" + value + "'");
			ResultSet RS = result.resultSet;
			if (RS.next())
			{
				obj = this.newTInstance();
				
				for (Entry<String, FieldInformation> entry : this.fields.entrySet()) {
					entry.getValue().field.setAccessible(true);
					entry.getValue().field.set(obj, getValueFromResult(RS, entry.getKey(), entry.getValue()));
				}
			}
		}
		catch (Exception e)
		{
			sendError("Error findOne(String key, Object value) from " + this.tableName, e);
		}
		finally
		{
			close(result);
		}
		return obj;
	}
	
	/**
	 * get global count
	 * @return
	 */
	public long count() {
		return count(null);
	}
	
	public long count(String query) {
		Result result = null;
		int count = 0;
		try
		{
			result = getDatanull("SELECT count(*) AS n FROM `" + this.tableName + "`" + (query != null ? " " + query : ""));
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
	
	public void delete(T entity) {
		PreparedStatement p = null;
		try
		{
			String where_primary = generateWherePrimary(entity); 
			p = getPreparedStatement("DELETE FROM `" + this.tableName + "` WHERE " + where_primary);
			execute(p);
		}
		catch (Exception e)
		{
			sendError("Error delete(T entity) from " + this.tableName, e);
		}
		finally
		{
			close(p);
		}
	}
	
	public void delete(String key, Object value) {
		PreparedStatement p = null;
		try
		{
			p = getPreparedStatement("DELETE FROM `" + this.tableName + "` WHERE " + key + " = '" + value + "'");
			execute(p);
		}
		catch (Exception e)
		{
			sendError("Error delete(String key, Object value) from " + this.tableName, e);
		}
		finally
		{
			close(p);
		}
	}
	
	/**
	 * Save object to database if is valid Object
	 * @param entity
	 * @param validate
	 * @return
	 */
	public ArrayList<String> save(T entity, boolean validate) {
		ArrayList<String> array = new ArrayList<String>();
		
		if (validate) {
			array.addAll(this.validate(entity));
		}
		if (array.size() == 0) {
			if (!this.save(entity)) {
				array.add("E-DB");//error insert code
			}
		}
		return array;
	}
	
	/**
	 * Save Object to database
	 * @param entity
	 * @return
	 */
	public boolean save(T entity) {
		boolean exists = exists(entity);
		
		PreparedStatement p = null;
		try
		{
			String numberOfValue = "";
			String columnsName = "";
			
			for (Entry<String, AbstractDAO<T>.FieldInformation> entry : this.fields.entrySet()) {
				if (!numberOfValue.isEmpty()) {
					numberOfValue += ",";
				}
				numberOfValue += "?";
				if (!columnsName.isEmpty()) {
					columnsName += ",";
				}
				columnsName += "`" + entry.getKey() + "`";
			}
			
			//generation of fields
			if (this.generationsFields.size() > 0) {
				this.generateValueFromObject(entity);
			}
			
			//create query :
			StringBuilder b = new StringBuilder();
			
			if (exists) {
				b.append("REPLACE INTO");
			} else {
				b.append("INSERT INTO");
			}
			b.append(" `").append(tableName).append("` ");
			b.append("(").append(columnsName).append(")").append(" VALUES ");
			b.append("(").append(numberOfValue).append(")");
			
			p = getPreparedStatement(b.toString());
			
			int parameterIndex = 1;
			for (FieldInformation field : this.fields.values()) {
				setValueToPreparedStatement(parameterIndex++, p, entity, field);
			}
			
			execute(p);
			
			if (!exists) {//update auto increment
				ResultSet result = p.getGeneratedKeys();
				int index = 1;
				for (Entry<String, FieldInformation> entry : this.fields.entrySet()) {
					if (!entry.getValue().isPrimary || !entry.getValue().autoIncrement) {
						continue ;
					}
					if (result.next()) {
						Object value = result.getObject(index++);
						entry.getValue().field.set(entity, value);
					}
				}
			}
		}
		catch (Exception e)
		{
			sendError("Error save(T entity) from " + this.tableName, e);
			return false;
		}
		finally
		{
			close(p);
		}
		
		return true;
	}
	
	/**
	 * check it object exists
	 * @param entity
	 * @return
	 */
	public boolean exists(T entity) {
		boolean exist = false;
		Result result = null;
		try
		{
			String where_primary = generateWherePrimary(entity);
			result = getData("SELECT * FROM `" + tableName + "` WHERE " + where_primary);
			ResultSet RS = result.resultSet;
			if (RS.next()) {
				exist = true;
			}
		}
		catch (Exception e)
		{
			sendError("Exists error", e);
		}
		finally
		{
			close(result);
		}
		return exist;
	}
	
	/**
	 * check if object exists
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean exists(String key, Object value) {
		boolean exist = false;
		Result result = null;
		try
		{
			result = getData("SELECT * FROM `" + tableName + "` WHERE " + key + " = '" + value
					+ "'");
			ResultSet RS = result.resultSet;
			if (RS.next()) {
				exist = true;
			}
		}
		catch (Exception e)
		{
			sendError("Exists error", e);
		}
		finally
		{
			close(result);
		}
		return exist;
	}
	
	/**
	 * Check ClairValidation annotation, and start Hibernate Validation<br>
	 * add all errors on ArrayList<String>
	 * 
	 * @param entity
	 * @return ArrayList<String>
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ArrayList<String> validate(T entity) {
		ArrayList<String> errors = Validator.validateObject(entity);
		
		for (Entry<String, FieldInformation> entry : this.validationsFields.entrySet()) {
			
			Annotation[] annots = entry.getValue().field.getAnnotations();
			Map<Class<?>, Annotation> annotationMap = new HashMap<Class<?>, Annotation>();
			
			for (Annotation a : annots) {
				annotationMap.put(a.annotationType(), a);
			}
			
			if (annotationMap.containsKey(ClairValidation.class)) {
				ClairValidation a = (ClairValidation)annotationMap.get(ClairValidation.class);
				
				try {
					Constructor<?> constructor = a.method().getConstructor();
					
					constructor.setAccessible(true);
					IFieldValidation validationClass = (IFieldValidation)constructor.newInstance();
					
					errors.addAll(validationClass.isValidField(entry.getValue().field.get(entity)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
		
		return errors;
	}
	
	private String generateWherePrimary(T entity) throws IllegalArgumentException, IllegalAccessException {
		String result = "";
		for (Entry<String, FieldInformation> entry : this.fields.entrySet()) {
			if (!entry.getValue().isPrimary) {
				continue ;
			}
			if (!result.isEmpty()) {
				result += " AND ";
			}
			result += entry.getKey() + " = '" + entry.getValue().field.get(entity) + "'";
		}
		return result;
	}
	
	private void generateValueFromObject(T entity) {
		for (Entry<String, FieldInformation> entry : this.generationsFields.entrySet()) {
			
			Annotation[] annots = entry.getValue().field.getAnnotations();
			Map<Class<?>, Annotation> annotationMap = new HashMap<Class<?>, Annotation>();
			
			for (Annotation a : annots) {
				annotationMap.put(a.annotationType(), a);
			}
			
			if (annotationMap.containsKey(ClairIfNullGenerateValue.class)) {
				ClairIfNullGenerateValue a = (ClairIfNullGenerateValue)annotationMap.get(ClairIfNullGenerateValue.class);
				
				try {
					Constructor<?> constructor = a.method().getConstructor();
					
					constructor.setAccessible(true);
					IFieldGenerateValue validationClass = (IFieldGenerateValue)constructor.newInstance();
					
					entry.getValue().field.set(entity, validationClass.generateValue());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		}
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
		Object result = RS.getObject(columName);
		if (result == null) {
			if (fieldInformation.field.getType() == boolean.class
					|| fieldInformation.field.getType() == int.class
					|| fieldInformation.field.getType() == double.class
					|| fieldInformation.field.getType() == long.class
					|| fieldInformation.field.getType() == float.class
					|| fieldInformation.field.getType() == byte.class)
				return 0;
		}
		return result;
	}
	
	private void setValueToPreparedStatement(int parameterIndex, PreparedStatement p, T entity, FieldInformation fieldInformation) throws SQLException, IllegalArgumentException, IllegalAccessException {
		Object result = fieldInformation.field.get(entity);

		p.setObject(parameterIndex, result);
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
	
    protected PreparedStatement getPreparedStatement(String query) throws SQLException {
    	try {
	       Connection connection = dataSource.getConnection();
	       return connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
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
		if (result == null)
			return ;
		if (result.resultSet != null)
			close(result.resultSet);
		if (result.connection != null)
			close(result.connection);
		logger.trace("Connection {} has been released", result.connection);
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
