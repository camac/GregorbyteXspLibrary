package com.gregorbyte.xsp.config;

public class CoreConfig extends GregorbytePluginConfig {

	@Override
	public String[] getFacesConfigFiles(String[] files) {
		return concat(
				files,
				new String[] { "com/gregorbyte/xsp/config/gregorbyte-core-faces-config.xml", // $NON-NLS-1$
				});

	}

}
