package com.gregorbyte.xsp.sqlsync;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import com.ibm.commons.util.StringUtil;

public class SqlProc implements Serializable {

	private static final long serialVersionUID = 1L;

	private String procName = null;

	private String baseClassPath = null;
	private String sqlQuery = null;
	private String filename = null;

	private boolean procExists = false;

	public void clearExistenceChecks() {
		this.procExists = false;
	}

	public static SqlProc fromSqlFile(String viewName) {
		SqlProc view = new SqlProc(viewName);

		return view;
	}

	public static SqlProc fromSqlFile(String viewName, String fileName) {
		SqlProc view = new SqlProc(viewName);
		view.filename = fileName;
		return view;
	}

	public static SqlProc fromClasspath(String viewName, String pluginId,
			String baseClassPath) {

		SqlProc view = new SqlProc(viewName);
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

	public static SqlProc fromSql(String viewName, String sql) {
		SqlProc view = new SqlProc(viewName);
		view.sqlQuery = sql;
		return view;
	}

	public SqlProc(String viewName) {
		this.procName = viewName;
	}

	public String getProcName() {
		return procName;
	}

	public void setProcName(String viewname) {
		this.procName = viewname;
	}

	public String getSqlQuery() {
		return sqlQuery;
	}

	public void setSqlQuery(String sqlQuery) {
		this.sqlQuery = sqlQuery;
	}

	public String getEffectiveFileName() {
		if (StringUtil.isEmpty(filename)) {
			return String.format("createproc_%s.sql", procName);
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
		Enumeration<?> urls = bundle
				.findEntries(path, fileName, false/* recursive */);
		if (null != urls && urls.hasMoreElements()) {
			URL url = (URL) urls.nextElement();
			if (null != url) {
				return url;
			}
		}
		return null; // no match, 404 not found.
	}

	public boolean isProcExists() {
		return procExists;
	}

	public void setProcExists(boolean viewExists) {
		this.procExists = viewExists;
	}

}
