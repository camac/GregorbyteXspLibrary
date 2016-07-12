package com.gregorbyte.xsp.sqlsync;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.ibm.commons.util.StringUtil;

public class SqlView implements Serializable {

	private static final long serialVersionUID = 1L;

	private String viewName = null;

	private String baseClassPath = null;
	private String sqlQuery = null;
	private String filename = null;

	private boolean viewExists = false;

	public void clearExistenceChecks() {
		this.viewExists = false;
	}

	public static SqlView fromSqlFile(String viewName) {
		SqlView view = new SqlView(viewName);

		return view;
	}

	public static SqlView fromSqlFile(String viewName, String fileName) {
		SqlView view = new SqlView(viewName);
		view.filename = fileName;
		return view;
	}

	public static SqlView fromClasspath(String viewName, String pluginId, String baseClassPath) {

		SqlView view = new SqlView(viewName);
		view.baseClassPath = baseClassPath;

		String path = baseClassPath + view.getEffectiveFileName();
		Bundle bundle = Platform.getBundle(pluginId);
		URL url = getResourceURL(bundle, path);

		if (url == null) {
			return view;
		}

		InputStream is;

		try {
			is = url.openStream();
			view.sqlQuery = AbstractSqlRepository.getStringFromInputStream(is);
			is.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return view;

	}

	public static SqlView fromSql(String viewName, String sql) {
		SqlView view = new SqlView(viewName);
		view.sqlQuery = sql;
		return view;
	}

	public SqlView(String viewName) {
		this.viewName = viewName;
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewname) {
		this.viewName = viewname;
	}

	public String getSqlQuery() {
		return sqlQuery;
	}

	public void setSqlQuery(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}

	public String getEffectiveFileName() {
		if (StringUtil.isEmpty(filename)) {
			return String.format("createview_%s.sql", viewName);
		}
		return filename;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getBaseClassPath() {
		return baseClassPath;
	}

	public void setBaseClassPath(String baseClassPath) {
		this.baseClassPath = baseClassPath;
	}

	public static URL getResourceURL(Bundle bundle, String path) {

		int fileNameIndex = path.lastIndexOf('/');
		String fileName = path.substring(fileNameIndex + 1);
		path = path.substring(0, fileNameIndex + 1);

		// see http://www.osgi.org/javadoc/r4v42/org/osgi/framework/Bundle.html
		// #findEntries%28java.lang.String,%20java.lang.String,%20boolean%29
		Enumeration<?> urls = bundle.findEntries(path, fileName, false/* recursive */);
		if (null != urls && urls.hasMoreElements()) {
			URL url = (URL) urls.nextElement();
			if (null != url) {
				return url;
			}
		}
		return null; // no match, 404 not found.
	}

	public boolean isViewExists() {
		return viewExists;
	}

	public void setViewExists(boolean viewExists) {
		this.viewExists = viewExists;
	}

}
