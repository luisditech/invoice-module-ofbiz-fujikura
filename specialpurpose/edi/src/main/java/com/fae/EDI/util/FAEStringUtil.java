package com.fae.EDI.util;

/**
 * Class with String utilities (get subtring, isNull, ...)
 *
 */
public class FAEStringUtil {


	private FAEStringUtil() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * Return true if s == null or is empty ""
	 * 
	 * @param s
	 * @return boolean
	 */
	public static boolean isNullOrBlank(String s) {
		return (s == null || s.trim().equals(""));
	}


}
