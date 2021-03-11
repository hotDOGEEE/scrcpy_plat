/**
 * 
 */
package com.example.devices.utils;

import java.io.File;

/**
 * @author hui.qian qianhui@58.com
 * @date 2015年8月17日 下午12:02:25
 */
public class Constant {
	private static final String ROOT = System.getProperty("user.dir");

	public static File getMinicap() {
		return new File(ROOT, "minicap");
	}

	public static File getMinicapBin() {
		return new File(ROOT, "minicap/bin");
	}

	public static File getMinicapSo() {
		return new File(ROOT, "minicap/shared");
	}

	public static File getMiniTouchBin() {
		return new File(ROOT, "minitouch");
	}

	public static File getMinicapAdb() {
		return new File(ROOT, "platform-tools");
	}

}
