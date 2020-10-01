package com.taiyiyun.passport.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class TypeRef<T> {
	
	private final Type type;

	public TypeRef() {
		ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
		type = parameterizedType.getActualTypeArguments()[0];
	}

	public Type getType() {
		return type;
	}
	
}
