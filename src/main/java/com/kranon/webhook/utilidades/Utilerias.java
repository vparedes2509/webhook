package com.kranon.webhook.utilidades;

import java.io.FileReader;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

public class Utilerias {

	@SuppressWarnings("rawtypes")
	public boolean getProperties(String vsPathConf, Map<String, String> voMapConfi) {
		try {
			Properties p = new Properties();
			p.load(new FileReader(vsPathConf));
			for (Enumeration voEnum = p.keys(); voEnum.hasMoreElements();) {
				String vsProperty = String.valueOf(voEnum.nextElement());
				if (!vsProperty.contains("//")) {
					voMapConfi.put(vsProperty, p.getProperty(vsProperty));
				}
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

}
