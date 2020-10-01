package com.taiyiyun.passport.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.Id;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;



public final class ReflectUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(ReflectUtil.class);
	private ReflectUtil() {
		
	}

	public static boolean isInteger(Class<?> clazz) {
		return (null == clazz) ? false : (clazz == Integer.class || clazz == int.class);
	}
	
	public static boolean isLong(Class<?> clazz) {
		return (null == clazz) ? false : (clazz == Long.class || clazz == long.class);
	}
	
	public static boolean isFloat(Class<?> clazz) {
		return (null == clazz) ? false : (clazz == Float.class || clazz == float.class);
	}
	
	public static boolean isDouble(Class<?> clazz) {
		return (null == clazz) ? false : (clazz == Double.class || clazz == double.class);
	}
	
	public static boolean isBigDecimal(Class<?> clazz) {
		return (null == clazz) ? false : (clazz == BigDecimal.class);
	}
	
	public static boolean isByte(Class<?> clazz) {
		return (null == clazz) ? false : (clazz == Byte.class || clazz == byte.class);
	}
	
	public static boolean isShort(Class<?> clazz) {
		return (null == clazz) ? false : (clazz == Short.class || clazz == short.class);
	}
	
	public static boolean isChar(Class<?> clazz) {
		return (null == clazz) ? false : (clazz == Character.class || clazz == char.class);
	}
	
	public static boolean isBoolean(Class<?> clazz) {
		return (null == clazz) ? false : (clazz == Boolean.class || clazz == boolean.class);
	}
	
	public static boolean isDate(Class<?> clazz) {
		return (null == clazz) ? false : (clazz == Date.class);
	}
	
	/**
	 * 方法调用
	 * @param clazz
	 * @param obj
	 * @param mthName
	 * @param params
	 * @return
	 */
	public static Object tryInvoke(Class<?> clazz, Object obj, String mthName, Object ... params) {
		try {
			return invoke(clazz, obj, mthName, params);
		} catch (Exception e) {
			logger.error("Invoke method error. " + e.getMessage(), e);
			return null;
		}
	}
	
	public static Object invoke(Class<?> clazz, Object obj, String mthName, Object ... params) throws Exception {
		Method mth = null;
		Object retObj = null;
		
		if (null == clazz && null == obj) {
			logger.debug("Invoke Class and Object is null, return null.");
			return null;
		}
		
		// 如果类型传入的为空，则使用obj的类型
		if (null == clazz) {
			logger.debug("Invoke Class is null, use object's type, Class is: " + obj.getClass());
			clazz = obj.getClass();
		}
		// 如果传入的Object为空，则创建一个新的
		if (null == obj) {
			logger.debug("Invoke object is null, create a new instance.");
			obj = clazz.newInstance();
		}
		
		// 如果参数为空，则直接获取方法
		if (null == params) {
			mth = clazz.getMethod(mthName);
		} else {
			// 如果所有的参数都有值，则取得其对应的类，获取方法
			Class<?>[] clazzs = new Class<?>[params.length];
			boolean isParamsNull = false;
			for (int i = 0; i < params.length; ++i) {
				if (null == params[i]) {
					isParamsNull = true;
					break;
				}
				clazzs[i] = params[i].getClass();
			}
			boolean isMatch = false;
			if (!isParamsNull) {
				try {
					mth = clazz.getMethod(mthName, clazzs);
					isMatch = true;
				} catch (NoSuchMethodException e) {
					isMatch = false;
				}
			} 
			if (!isMatch) {
				// 获取最接近的方法
				Method[] allMethods = clazz.getMethods();
				for (Method m : allMethods) {
					if (mthName.equals(m.getName())) {
						clazzs = m.getParameterTypes();
						if (null != clazzs && clazzs.length == params.length) {
							boolean isMth = true;
							for (int i = 0; i < params.length; ++i) {
								if (null != params[i] && !params[i].getClass().getName().equals(clazzs[i].getName())) {
									isMth = false;
									break;
								}
							}
							if (isMth) {
								mth = m;
								break;
							}
							
							// 尝试强制转换参数为方法对应的类型
							try {
								Object[] paramObj = new Object[clazzs.length];
								for (int i = 0; i < clazzs.length; ++i) {
									paramObj[i] = toType(clazzs[i], params[i]);
								}
								logger.debug("Invoke obj = " + obj + ", method = " + m.getName() + ", paramObj = " + getParamsStr(paramObj));
								retObj = m.invoke(obj, paramObj);
								logger.debug("Invoke return: " + retObj);
								return retObj;
							} catch (Exception e) {
								logger.error("尝试强制转换参数使其匹配异常。", e);
							}
						}
					}
				}
			}
		}
		
		if (mth != null) {
			logger.debug("Invoke obj = " + obj + ", method = " + mth.getName() + ", params = " + getParamsStr(params));
			retObj = mth.invoke(obj, params);
		} else {
			logger.error("Invoke method: " + clazz.getName() + "." + mthName + " is not found, failed to invoke.");
		}
		logger.debug("Invoke return: " + retObj);
		return retObj;
	}
	
	/**
	 * 根据类名称获取Class对象
	 * @param clazzName -类完全名称
	 * @return
	 */
	public static Class<?> getClass(String clazzName) {
		try {
			return Class.forName(clazzName);
		} catch (Exception e) {
			logger.error("getClass method error, return null. " + e.getMessage(), e);
			return null;
		}
	}
	
	public static Object getInstance(String clazzName) {
		Class<?> clazz = getClass(clazzName);
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			logger.error("getInstance method error, return null. " + e.getMessage(), e);
			return null;
		}
	}
	
	/**
	 * 获取泛型的类型
	 * @param clazz 获取的类
	 * @param index  第几个类型
	 * @return
	 */
	public static Class<?> getGenericType(Class<?> clazz, int index) {
		if (null == clazz) {
			throw new NullPointerException("paramter clazz is null.");
		}
		Type genType = clazz.getGenericSuperclass();
		if (!(genType instanceof ParameterizedType)) {
			return Object.class;
		}
		Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
		if (index >= params.length || index < 0) {
			throw new RuntimeException("Index outof bounds: " + index);
		}
		if (!(params[index] instanceof Class)) {
			return Object.class;
		}
		return (Class<?>) params[index];
	}
	
	/**
	 * Object转换为 {@link Map}对象
	 * @param instance CmsPo
	 * @return
	 * @throws Exception
	 */
	public static Map<String, String> toMap(Object instance) throws Exception {
		Class<?> clazz = instance.getClass();
		Method[] methods = clazz.getDeclaredMethods();
		Map<String, String> result = new HashMap<String, String>();
		
		Object val = null;
		for (Method method : methods) {
			if (isGet(method)) {
				val = method.invoke(instance);
				result.put(StringUtil.firstLower(method.getName().substring(3)), StringUtil.toStr(val));
			}
		}
		
		return result;
	}
	
	/**
	 * Object转换为 {@link Map}对象
	 * @param instance CmsPo
	 * @return
	 * @throws Exception
	 */
	public static Map<String, Object> toObjectMap(Object instance) throws Exception {
		Class<?> clazz = instance.getClass();
		Method[] methods = clazz.getDeclaredMethods();
		Map<String, Object> result = new HashMap<String, Object>();
		
		for (Method method : methods) {
			if (isGet(method)) {
				result.put(StringUtil.firstLower(method.getName().substring(3)), method.invoke(instance));
			}
		}
		
		return result;
	}
	
	/**
	 * List转换为Map，属性对应的值用特定的符号隔开
	 * @param list
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes" })
	public static Map<String, String> listId2Map(List list, String split) throws Exception {
		Map<String, String> result = new HashMap<String, String>();
		if (null != list && list.size() > 0) {
			Object val = null;
			String key = null;
			String keyVal = null;
			String strVal = null;
			Class clazz = list.get(0).getClass();
			Field[] fields = clazz.getDeclaredFields();
			for (int i = 0; i < list.size(); ++i) {
				for (int j = 0; j < fields.length; ++j) {
					Id id = fields[j].getAnnotation(Id.class);
					if (null != id) {
						key = fields[j].getName();
						val = tryInvoke(clazz, list.get(i), "get" + StringUtil.firstUpper(key));
						strVal = StringUtil.toStr(val);
						keyVal = result.get(key);
						if (null != keyVal) {
							keyVal += split + (StringUtil.isEmptyOrBlank(strVal) ? " " : strVal);
						} else {
							keyVal = StringUtil.isEmptyOrBlank(strVal) ? null : strVal;
						}
						result.put(key, keyVal);
					}
				}
			}
		}
		return result;
	}
	
	/**
	 * 是否是getXx方法
	 * @param method
	 * @return
	 */
	public static boolean isGet(Method method) {
		if (null != method) {
			String methodName = method.getName();
			if (methodName.length() > 3 && methodName.startsWith("get")) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * 获取声明的属性名称
	 * @param ins
	 * @return
	 * @throws Exception
	 */
	public static List<String> getFields(Object ins, MethodType type) throws Exception {
		List<String> fields = new ArrayList<String>();
		Set<String> getMth = getMthName(ins, "get");
		Set<String> setMth = getMthName(ins, "set");
		if (null != ins) {
			Field[] fieldArr = ins.getClass().getDeclaredFields();
			for (Field f : fieldArr) {
				if (MethodType.GET.equals(type)) {
					// 验证get方法
					if (getMth.contains(StringUtil.firstUpper(f.getName()))) {
						fields.add(f.getName());
					}
				} else if (MethodType.SET.equals(type)) {
					// 验证set方法
					if (setMth.contains(StringUtil.firstUpper(f.getName()))) {
						fields.add(f.getName());
					}
				} else {
					fields.add(f.getName());
				}
			}
		}
		return fields;
	}
	
	/**
	 * 获取方法
	 * @param ins
	 * @param pref 方法前缀
	 * @return
	 * @throws Exception
	 */
	public static List<Method> getMths(Object ins, String pref) throws Exception {
		List<Method> mths = new ArrayList<Method>();
		if (null != ins && null != pref && pref.trim().length() > 0) {
			Method[] mthArr = ins.getClass().getDeclaredMethods();
			for (Method m : mthArr) {
				if (m.getName().length() > pref.length() && m.getName().startsWith(pref)) {
					mths.add(m);
				}
			}
		}
		return mths;
	}
	
	/**
	 * 获取和 {@link Field}对应的GET/SET/...方法的集合
	 * @param ins
	 * @param pref 方法前缀
	 * @return
	 * @throws Exception
	 */
	private static Set<String> getMthName(Object ins, String pref) throws Exception {
		Set<String> mths = new HashSet<String>();
		if (null != ins && null != pref && pref.trim().length() > 0) {
			Method[] mthArr = ins.getClass().getDeclaredMethods();
			for (Method m : mthArr) {
				if (m.getName().length() > pref.length() && m.getName().startsWith(pref)) {
					mths.add(m.getName().substring(pref.length()));
				}
			}
		}
		return mths;
	}
	
	/**
	 * 验证类是否有字段
	 * @param clazz
	 * @param fieldNames
	 * @return
	 */
	public static boolean hasFields(Class<?> clazz, List<String> fieldNames) {
		try {
			Set<String> fields = new HashSet<String>();
			fields.addAll(getFields(clazz.newInstance(), MethodType.ALL));
			for (String field : fieldNames) {
				if (!fields.contains(field)) {
					logger.error("can't find field[" + field + "] in " + clazz.getName());
					return false;
				}
			}
		} catch (Exception e) {
			logger.error("验证字段异常。", e);
			return false;
		}
		return true;
	}
	
	public static Object getField(String clazzName, String fieldName) {
		Class<?> clazz = getClass(clazzName);
		if (null == clazz) {
			return null;
		}
		try {
			Field filed = clazz.getDeclaredField(fieldName);
			filed.setAccessible(true);
			return filed.get(clazz);
		} catch (Exception e) {
			logger.error("获取字段信息异常，类名：" + clazzName + "，字段名：" + fieldName, e);
		}
		return null;
	}
	
	public static boolean isAnnotation(Class<?> clazz, String fieldName, Class<?> annotation) {
		try {
			Field field = clazz.getDeclaredField(fieldName);
			if (null != field) {
				Annotation[] annotations = field.getAnnotations();
				if (null != annotations) {
					for (Annotation a : annotations) {
						if (a.annotationType() == annotation) {
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			
		}
		return false;
	}
	
	private static String getParamsStr(Object[] objs) {
		if (null == objs || objs.length == 0) {
			return "[void]";
		}
		StringBuffer buf = new StringBuffer("[");
		for (int i = 0; i < objs.length; ++i) {
			if (i > 0) {
				buf.append(", ");
			}
			buf.append(objs[i]);
		}
		buf.append("]");
		return buf.toString();
	}
	
	@SuppressWarnings({ "rawtypes" })
	public static Object toType(Class clazz, Object val) throws Exception {
		if (null == clazz || null == val) {
			return null;
		}

		Object obj = null;
		if (String.class == clazz) { // String
			obj = String.valueOf(val);
		} else if (Integer.class == clazz) { // int
			obj = Integer.valueOf(String.valueOf(val));
		} else if (int.class == clazz) {
			obj = (int) Integer.valueOf(String.valueOf(val));
		} else if (Double.class == clazz) { // double
			obj = Double.valueOf(String.valueOf(val));
		} else if (double.class == clazz) {
			obj = (double) Double.valueOf(String.valueOf(val));
		} else if (Float.class == clazz) { // float
			obj = Float.valueOf(String.valueOf(val));
		} else if (float.class == clazz) {
			obj = (float) Float.valueOf(String.valueOf(val));
		} else if (Long.class == clazz) { // long
			obj = Long.valueOf(String.valueOf(val));
		} else if (long.class == clazz) {
			obj = (long) Long.valueOf(String.valueOf(val));
		} else if (Byte.class == clazz) { // byte
			obj = Byte.valueOf(String.valueOf(val));
		} else if (byte.class == clazz) {
			obj = (byte) Byte.valueOf(String.valueOf(val));
		} else if (Date.class == clazz) { // Date
			if (val instanceof Date) {
				obj = (Date) val;
			} else if (val instanceof java.sql.Date) {
				obj = new Date(((java.sql.Date)val).getTime());
			} else {
				obj = DateUtil.fromString(String.valueOf(val));
			}
		} else if (boolean.class == clazz) {
			String str = String.valueOf(val).trim().toUpperCase();
			obj = ("TRUE".equals(str) || "1".equals(str) || "YES".equals(str) || "ON".equals(str)) ? true : false;
		} else if (Boolean.class == clazz) {
			String str = String.valueOf(val).trim().toUpperCase();
			obj = ("TRUE".equals(str) || "1".equals(str) || "YES".equals(str) || "ON".equals(str)) ? Boolean.TRUE : Boolean.FALSE;
		} else if (BigDecimal.class == clazz) {
			String str = String.valueOf(val).trim().toUpperCase();
			if (NumberUtil.isNumber(str)) {
				obj = new BigDecimal(str);
			}
		} else {
			obj = val;
		}

		return obj;
	}
	
	public static enum MethodType {
		GET, SET, ALL
	}
	
}
