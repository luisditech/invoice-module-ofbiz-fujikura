package com.fae.EDI.util;

/**
 * Class with Numeric utilities (is numeric, parse double, ...)
 *
 */
public class FAENumericUtil {
	
	private FAENumericUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Enter a String number, a defaultValue; convert number to Long. If string
	 * is null or empty, return defaultValue.
	 * 
	 * @param number
	 * @param defaultValue
	 * @return Long
	 */
	public static Long parseLong(String number, Long defaultValue) {
		Long result = null;
		if (number != null && !number.isEmpty()) {
			result = Long.parseLong(number);
		} else {
			result = defaultValue;
		}
		return result;
	}	

}
