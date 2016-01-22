package com.gregorbyte.xsp.config;

public class ControlsConfig extends GregorbytePluginConfig {

	@Override
	public String[] getXspConfigFiles(String[] files) {
		return concat(files, new String[] {
				"com/gregorbyte/xsp/config/gregorbyte-textdiff.xsp-config", // $NON-NLS-1$
				"com/gregorbyte/xsp/config/gregorbyte-mimeinspector.xsp-config", // $NON-NLS-1$
				"com/gregorbyte/xsp/config/gregorbyte-panelgrid.xsp-config", // $NON-NLS-1$
				"com/gregorbyte/xsp/config/gregorbyte-phonenumber.xsp-config", // $NON-NLS-1$
				"com/gregorbyte/xsp/config/gregorbyte-markdown.xsp-config", // $NON-NLS-1$
		});
	}

	@Override
	public String[] getFacesConfigFiles(String[] files) {
		return concat(
				files,
				new String[] {
						"com/gregorbyte/xsp/config/gregorbyte-textdiff-faces-config.xml", // $NON-NLS-1$
						"com/gregorbyte/xsp/config/gregorbyte-panelgrid-faces-config.xml", // $NON-NLS-1$
						"com/gregorbyte/xsp/config/gregorbyte-phonenumber-faces-config.xml", // $NON-NLS-1$
						"com/gregorbyte/xsp/config/gregorbyte-markdown-faces-config.xml", // $NON-NLS-1$
				});

	}

}
