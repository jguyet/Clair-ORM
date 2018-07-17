package com.xxx;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.xxx.annotation.Column;
import com.xxx.annotation.Primary;
import com.xxx.annotation.Table;

public abstract class AbstractDAO<T> {

	private String							tableName;
	private Map<String, FieldInformation>	columns = new HashMap<String, FieldInformation>();
	private Class<?>						sourceClass = null;
	
	public AbstractDAO() {
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
		return null;
	}
	
	public ArrayList<T> find(String query) {
		return null;
	}
	
	public T findOne(String query) {
		return null;
	}
	
	public T findOne(String key, Object value) {
		return null;
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
	
	private void hhh() {
//		try {
//			Constructor<?> co = table.clazz.getConstructor();
//			
//			System.out.println(co.toString());
//			
//			co.setAccessible(true);
//			T obj = (T) co.newInstance();
//			
//			
////			Field field1 = obj.getClass().get.getDeclaredField("id");
////	        field1.setAccessible(true);
////	        field1.set(obj, "IDENTIFIANT");
////	        System.out.println(field1.get(obj));
//	        
//	        for (Field f : table.columns.values()) {
//	        	f.set(obj, "TEST");
//	        	
//	        	System.out.println(f.get(obj));
//	        }
//			System.out.println(obj.toString());
//			
//			
//			
//		} catch (NoSuchMethodException | SecurityException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvocationTargetException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	static class FieldInformation {
		public String name = null;
		public Field field = null;
		public boolean isPrimary = false;
		public boolean autoIncrement = false;
	}
	
}
