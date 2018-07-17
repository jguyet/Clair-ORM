package com.xxx;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

import com.xxx.annotation.Column;
import com.xxx.annotation.Table;

public abstract class AbstractDAO<T> {

	private String		tableName;
	private TableModel	tableModel;
	
	
	public AbstractDAO() {
		buildReflection();
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
		this.tableModel = new TableModel();
		this.tableModel.clazz = c;
		for (Annotation a : annots) {
			if (!(a instanceof Table))
				continue ;
			
			Field[] fields = c.getDeclaredFields();
			
			for (int i = 0; i < fields.length; i++) {
				this.tableModel.addField(fields[i]);
			}
			this.tableName = ((Table)a).value();
			break ;
		}
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
	
	public class TableModel {
		
		public Map<String, Field> columns = new HashMap<String, Field>();
		public Class<?> clazz = null;
		
		public void addField(Field field) {
			Annotation[] annots = field.getAnnotations();
			Map<String, Annotation> annotationMap = new HashMap<String, Annotation>();
			
			for (Annotation a : annots) {
				annotationMap.put(a.annotationType().getSimpleName(), a);
			}
			
			if (annotationMap.containsKey("Column")) {
				field.setAccessible(true);
				this.columns.put(((Column)annotationMap.get("Column")).value(), field);
			}
		}
	}
	
}
