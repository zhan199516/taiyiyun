package com.taiyiyun.passport.transfer;

import java.io.Serializable;
import java.math.BigDecimal;

import com.alibaba.fastjson.annotation.JSONField;
import com.taiyiyun.passport.util.NumberUtil;

/**
 * 数值型返回结果
 * @author LiuQing
 */
public class NumberResult implements Serializable {

	@JSONField(serialize = false)
	private static final long serialVersionUID = 7515356286637889943L;

	@JSONField(name = "stringValue")
	private String stringValue; // 字符串结果

	@JSONField(name = "decimalValue")
	private BigDecimal decimalValue; // BigDecimal结果

	@JSONField(name = "intValue")
	private Integer intValue; // Integer结果

	@JSONField(name = "longValue")
	private Long longValue; // Long结果

	@JSONField(name = "doubleValue")
	private Double doubleValue; // Double结果

	@JSONField(name = "floatValue")
	private Float floatValue; // Float结果

	public String getStringValue() {
		return stringValue;
	}

	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
		if (NumberUtil.isNumber(stringValue)) {
			setDecimalValue(new BigDecimal(stringValue));
		}
	}

	public BigDecimal getDecimalValue() {
		return decimalValue;
	}

	public void setDecimalValue(BigDecimal decimalValue) {
		this.decimalValue = decimalValue;
		if (null != decimalValue) {
			this.doubleValue = decimalValue.doubleValue();
			this.floatValue = decimalValue.floatValue();
			this.intValue = decimalValue.intValue();
			this.longValue = decimalValue.longValue();
			this.stringValue = String.format("%.8f", decimalValue);
		}
	}

	public Integer getIntValue() {
		return intValue;
	}

	public void setIntValue(Integer intValue) {
		this.intValue = intValue;
		if (null != intValue) {
			setDecimalValue(new BigDecimal(intValue));
		}
	}

	public Long getLongValue() {
		return longValue;
	}

	public void setLongValue(Long longValue) {
		this.longValue = longValue;
		if (null != longValue) {
			setDecimalValue(new BigDecimal(longValue));
		}
	}

	public Double getDoubleValue() {
		return doubleValue;
	}

	public void setDoubleValue(Double doubleValue) {
		this.doubleValue = doubleValue;
		if (null != doubleValue) {
			setDecimalValue(new BigDecimal(doubleValue));
		}
	}

	public Float getFloatValue() {
		return floatValue;
	}

	public void setFloatValue(Float floatValue) {
		this.floatValue = floatValue;
		if (null != floatValue) {
			setDecimalValue(new BigDecimal(floatValue));
		}
	}

}
