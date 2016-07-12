package com.gregorbyte.xsp.sqlsync;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.context.FacesContext;
import javax.naming.NameNotFoundException;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.relational.util.JdbcUtil;
import com.ibm.xsp.util.FacesUtil;

public abstract class AbstractSqlRepository implements Serializable {

	private static final long serialVersionUID = 1L;

	private String connectionName = null;
	private String connectionUrl = null;

	private boolean initialised = false;
	private List<SqlTableSync> syncList = new ArrayList<SqlTableSync>();

	private List<SqlView> views = new ArrayList<SqlView>();

	private List<SqlProc> procs = new ArrayList<SqlProc>();

	protected DeletionSync delSync = null;

	public boolean ignoreForeignKeyConstraints = true;

	public AbstractSqlRepository() {

		loadSyncDetails();
	}

	public String getConnectionName() {
		return connectionName;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	public String getConnectionUrl() {
		return connectionUrl;
	}

	public void setConnectionUrl(String connectionUrl) {
		this.connectionUrl = connectionUrl;
	}

	public Connection getConnection() throws SQLException {

		if (StringUtil.isNotEmpty(connectionUrl)) {
			return JdbcUtil.createConnectionFromUrl(FacesContext.getCurrentInstance(), connectionUrl);
		}

		if (StringUtil.isNotEmpty(connectionName)) {
			return JdbcUtil.createNamedConnection(FacesContext.getCurrentInstance(), connectionName);
		}

		throw new NullPointerException("Connection Name / Url has not been set");

	}

	abstract public void loadSyncDetails();

	public void reloadSyncDetails() {
		syncList.clear();
		views.clear();
		loadSyncDetails();
	}

	protected void addSync(SqlTableSync sync) {
		this.syncList.add(sync);
	}

	protected void addView(SqlView view) {
		this.views.add(view);
	}

	protected void addProc(SqlProc proc) {
		this.procs.add(proc);
	}

	protected void addViewFromSql(String viewName, String sql) {
		addView(SqlView.fromSql(viewName, sql));
	}

	protected void addViewFromSqlFile(String viewName) {
		addView(SqlView.fromSqlFile(viewName));
	}

	protected void addViewFromPlugin(String viewName, String pluginId, String basePath) {
		addView(SqlView.fromClasspath(viewName, pluginId, basePath));
	}

	protected void addProcFromPlugin(String procName, String pluginId, String basePath) {
		addProc(SqlProc.fromClasspath(procName, pluginId, basePath));
	}

	public boolean isInitialised() {
		return initialised;
	}

	public void setInitialised(boolean initialised) {
		this.initialised = initialised;
	}

	public Object tableCount(Connection con, String table) {

		try {

			Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as \"count\" FROM \"" + table + "\"");

			if (rs.next()) {
				return rs.getLong("count");
			}

			return null;

		} catch (SQLException e) {

			return null;
		}

	}

	public Object queryCount(Connection con, String query) {

		try {

			Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			ResultSet rs = stmt.executeQuery(query);

			if (rs.next()) {
				return rs.getLong("count");
			}

			return null;

		} catch (SQLException e) {
			return null;
		}

	}

	private boolean viewExists(Connection con, String viewName) throws SQLException {

		String qry = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.VIEWS WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME = '%s'";

		qry = String.format(qry, viewName);

		Statement s = con.createStatement();
		ResultSet rs = s.executeQuery(qry);

		if (rs.next()) {
			return true;
		}

		return false;

	}

	private boolean procExists(Connection con, String procName) throws SQLException {

		String qry = "SELECT ROUTINE_NAME FROM INFORMATION_SCHEMA.ROUTINES WHERE ROUTINE_SCHEMA = 'PUBLIC' AND ROUTINE_NAME = '%s'";

		qry = String.format(qry, procName);

		Statement s = con.createStatement();
		ResultSet rs = s.executeQuery(qry);

		if (rs.next()) {
			return true;
		}

		return false;

	}

	protected void createView(Connection con, SqlView sqlView) {

		if (sqlView.isViewExists())
			return;

		try {

			String viewName = sqlView.getViewName();

			if (!viewExists(con, viewName)) {

				String sql = getCreateViewSQL(sqlView);

				con.createStatement().executeUpdate(sql);

			}

			sqlView.setViewExists(true);

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	protected void createViews(Connection con) {

		for (SqlView sqlView : views) {
			createView(con, sqlView);
		}

	}

	protected void createProc(Connection con, SqlProc sqlProc) {

		if (sqlProc.isProcExists())
			return;

		try {

			String procName = sqlProc.getProcName();

			if (!procExists(con, procName)) {

				String sql = getCreateProcSQL(sqlProc);

				con.createStatement().executeUpdate(sql);

			}
			sqlProc.setProcExists(true);

		} catch (SQLException e) {
			// logger.error(sqlProc.getProcName() + ": " + e.getMessage());
		}

	}

	protected void createProcs(Connection con) {

		for (SqlProc sqlProc : procs) {
			createProc(con, sqlProc);
		}

	}

	public void truncatePublicSchema() throws SQLException {

		getConnection().createStatement().executeUpdate("TRUNCATE SCHEMA public AND COMMIT");

		clearLastRefreshedDates();

	}

	private void clearLastRefreshedDates() {

		for (SqlSync sync : syncList) {
			sync.clearLastRefreshed();
		}

	}

	private void resetExistenceChecks() {
		for (SqlTableSync sqlTableSync : syncList) {
			sqlTableSync.clearExistenceChecks();
		}

		for (SqlView view : views) {
			view.clearExistenceChecks();
		}
	}

	public void dropPublicSchema() throws SQLException {

		getConnection().createStatement().executeUpdate("DROP SCHEMA PUBLIC CASCADE");

		clearLastRefreshedDates();
		resetExistenceChecks();

	}

	public void setReferentialIntegrity(Connection con, boolean setting) {

		try {
			if (setting) {
				String qry = "SET DATABASE REFERENTIAL INTEGRITY TRUE;";
				con.createStatement().execute(qry);

			} else {

				String qry = "SET DATABASE REFERENTIAL INTEGRITY FALSE;";
				con.createStatement().execute(qry);

			}
		} catch (SQLException e) {
			// logger.error(e.getMessage());
		}

	}

	public void fullSync() {
		clearLastRefreshedDates();
		sync();
	}

	public void sync() {

		Connection con = null;

		try {
			con = getConnection();
		} catch (SQLException e) {

			if (e.getCause() instanceof NameNotFoundException) {
				// logger.error(e.getCause().getMessage());
				FacesUtil.addErrorMessage("JDBC Connection not found with Name " + e.getCause().getMessage(), null);
			} else {

				if (e.getMessage() != null) {
					//logger.error(StringUtil.getNonNullString(e.getMessage()));
					FacesUtil.addErrorMessage(e.getMessage(), null);
				}
			}
			return;
		}

		for (SqlTableSync sync : syncList) {

			sync.createTableAuto(con);
			sync.createIndexes(con);

		}

		createViews(con);

		createProcs(con);

		// Sync

		if (ignoreForeignKeyConstraints) {
			setReferentialIntegrity(con, false);
		}

		for (SqlTableSync sync : syncList) {
			sync.sync(con);
		}

		if (delSync != null) {
			// Sync Deletions
			delSync.sync(con);
		}

		if (ignoreForeignKeyConstraints) {
			setReferentialIntegrity(con, true);
		}

		postSync(con);

	}

	protected void postSync(Connection con) {

	}

	public void refreshRepo() {

		Connection con = null;

		try {
			con = getConnection();
		} catch (SQLException e) {
			// logger.error(e.getMessage());
			FacesUtil.addErrorMessage(e.getMessage(), "");
			return;
		}

		for (SqlTableSync sync : syncList) {
			sync.createTable(con);
		}

		createViews(con);

		// Sync
		for (SqlTableSync sync : syncList) {
			sync.sync(con);
		}

		if (delSync != null) {
			// Sync Deletions
			delSync.sync(con);
		}

	}

	public void auditRepo() {

	}

	public String getCreateViewSQL(SqlView sqlView) {

		if (StringUtil.isNotEmpty(sqlView.getSqlQuery())) {
			return sqlView.getSqlQuery();
		}

		return JdbcUtil.readSqlFile(sqlView.getEffectiveFileName());

	}

	public String getCreateProcSQL(SqlProc sqlProc) {

		if (StringUtil.isNotEmpty(sqlProc.getSqlQuery())) {
			return sqlProc.getSqlQuery();
		}

		return JdbcUtil.readSqlFile(sqlProc.getEffectiveFileName());

	}

	protected String loadSQLFromFileResources(String fileName) {

		String sql = null;

		try {

			InputStream is = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/" + fileName);

			sql = getStringFromInputStream(is);
			is.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return sql;

	}

	public void refreshGlobalJdbcConnections() {

		// try {
		// JdbcDataSourceProvider.resetGlobalProvider();
		// } catch (ResourceFactoriesException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }

	}

	public static String getStringFromInputStream(InputStream is) {

		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();

		String line;
		try {

			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return sb.toString();

	}

	public static Date getDateInOneHour() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.HOUR, 1);
		return c.getTime();
	}

}