package com.ds.utils;

public class StringUtil {

	public static boolean isEmpty(String str) {
		if (null==str || str.equals("")) {
			return true;
		}

		return false;
	}
	public static String bytesToHexString(byte[] src) {
		StringBuilder sb = new StringBuilder(src.length * 2);

		final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };

		for (int i = 0; i < src.length; i++) {
			int value = src[i] & 0xff;
			sb.append(HEX[value / 16]).append(HEX[value % 16]);
		}

		return sb.toString();
	}
	public static String bytesToHexString(byte[] src,int length) {

		int len = src.length>length?length:src.length;

		StringBuilder sb = new StringBuilder(len * 2);

		final char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };

		for (int i = 0; i < src.length; i++) {
			int value = src[i] & 0xff;
			sb.append(HEX[value / 16]).append(HEX[value % 16]);
		}

		return sb.toString();
	}
}
