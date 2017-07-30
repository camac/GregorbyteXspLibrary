package com.gregorbyte.xsp.config;

public abstract class GregorbytePluginConfig {

	public String[] getXspConfigFiles(String[] files) {
		return files;
	}

	public String[] getFacesConfigFiles(String[] files) {
		return files;
	}

	public static String[] concat(String[] s1, String[] s2) {
		String[] s = new String[s1.length + s2.length];
		System.arraycopy(s1, 0, s, 0, s1.length);
		System.arraycopy(s2, 0, s, s1.length, s2.length);
		return s;
	}

}
