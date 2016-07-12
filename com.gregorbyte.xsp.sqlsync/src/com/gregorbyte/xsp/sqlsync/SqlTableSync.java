package com.gregorbyte.xsp.sqlsync;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;


import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.model.domino.DominoUtils;

import lotus.domino.Database;
import lotus.domino.DateRange;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

public abstract class SqlTableSync implements SqlSync, Serializable {

	private static final long serialVersionUID = -8904373386121259526L;

//	private static final Logger logger = LoggerFactory
//			.getLogger(SqlTableSync.class);

	private static final String DEFAULT_LASTMODFIELDNAME = "lastModifiedDate";

	private Date lastRefreshed = null;
	private String viewName = null;
	private String tableName = null;
	private String replicaId = null;

	private Boolean tableExists = false;
	private Boolean indexesExist = false;

	private List<SqlSyncForeignKey> foreignKeys = new ArrayList<SqlSyncForeignKey>();

	private List<SqlSyncColumn> identityColumns = new ArrayList<SqlSyncColumn>();
	private List<SqlSyncColumn> valueColumns = new ArrayList<SqlSyncColumn>();
	/*
	 * Extra Columns are not included in the Sync, but will be created int the
	 * sql table They are used when you might want to update some information to
	 * 'normalise' some data within the sql system for increased performance.
	 * For example, if you have a complicated 'status' calculation, you can
	 * perform it and save the status in this extra column
	 */
	private List<SqlSyncColumn> extraColumns = new ArrayList<SqlSyncColumn>();
	// private List<String> identityColumns = new ArrayList<String>();
	// private List<String> valueColumns = new ArrayList<String>();
	private String lastModifiedFieldName = DEFAULT_LASTMODFIELDNAME;
	private SqlSyncColumn lastModifiedColumn = new SqlSyncColumn(
			DEFAULT_LASTMODFIELDNAME, SqlSyncDataType.LASTMODIFIED);

	private List<List<String>> unique = new ArrayList<List<String>>();

	private Map<String, List<String>> indexes = new HashMap<String, List<String>>();

	private String mergeQuery = null;

	public SqlTableSync(String viewName, String tableName, String replicaId) {
		this.viewName = viewName;
		this.tableName = tableName;
		this.replicaId = replicaId;

		addIdentityField("replicaId", -1, SqlSyncDataType.REPLICAID);
		addIdentityField("unid", -1, SqlSyncDataType.UNID);

	}

	public void clearLastRefreshed() {
		this.lastRefreshed = null;
	}

	public void clearExistenceChecks() {
		this.tableExists = false;
		this.indexesExist = false;
	}

	public String getViewName() {
		return viewName;
	}

	public String getTableName() {
		return tableName;
	}

	public String getReplicaId() {
		return replicaId;
	}

	private String getCreateTableSql() {
		return getSQL(getTableName() + "_create.sql");
	}

	private String getColumnDefinition(SqlSyncColumn col) {

		StringBuilder sb = new StringBuilder();

		String name = quote(col.getColumnName());

		sb.append(name);

		switch (col.getType()) {
		case REPLICAID:
			sb.append(" char(16) NOT NULL");
			break;
		case UNID:
			sb.append(" char(32) NOT NULL");
			break;
		case LASTMODIFIED:
			sb.append(" datetime NOT NULL");
			break;
		case STRING:
			sb.append(" varchar(");
			sb.append(col.getLength());
			sb.append(") NULL");
			break;
		case FIXEDLENGTHSTRING:
			sb.append(" char(");
			sb.append(col.getLength());
			sb.append(") NULL");
			break;
		case DATE:
			sb.append(" date NULL");
			break;
		case DATETIME:
			sb.append(" datetime NULL");
			break;
		case BOOLEAN:
			sb.append(" boolean NULL");
			break;
		case INTEGER:
			sb.append(" smallint NULL");
			break;

		default:
			break;
		}

		return sb.toString();

	}

	private String getForeignKeyClause(SqlSyncForeignKey key) {

		StringBuilder sb = new StringBuilder("FOREIGN KEY (");
		sb.append(joinColumnNames(key.getThisColumns(), true));
		sb.append(") ");
		sb.append(" REFERENCES ");
		sb.append(quote(key.getForeignTable()));
		sb.append("(");
		sb.append(joinColumnNames(key.getThatColumns(), true));
		sb.append(") ");

		return sb.toString();

	}

	private String getCreateTableSqlAuto() {

		StringBuilder sb = new StringBuilder();

		String tableName = quote(getTableName());

		sb.append("CREATE TABLE IF NOT EXISTS ");
		sb.append(tableName);
		sb.append(" ( ");

		boolean first = true;

		for (SqlSyncColumn col : identityColumns) {

			if (!first) {
				sb.append(", ");
			}

			sb.append(getColumnDefinition(col));

			first = false;

		}

		for (SqlSyncColumn col : valueColumns) {
			if (!first) {
				sb.append(", ");
			}

			sb.append(getColumnDefinition(col));
		}

		sb.append(", ");
		sb.append(getColumnDefinition(lastModifiedColumn));

		for (SqlSyncColumn col : extraColumns) {
			if (!first) {
				sb.append(", ");
			}
			sb.append(getColumnDefinition(col));
		}

		sb.append(", ");
		sb.append("PRIMARY KEY ( ");

		first = true;

		// Add Composite Primary Key
		for (SqlSyncColumn col : identityColumns) {

			if (!first) {
				sb.append(", ");
			}
			sb.append(quote(col.getColumnName()));
			first = false;
		}
		sb.append(" ) ");

		// Add Composite Primary Key
		for (List<String> unCol : unique) {

			sb.append(", UNIQUE(");
			sb.append(joinColumnNames(unCol, true));
			sb.append(")");

		}

		// Add Composite Primary Key
		for (SqlSyncForeignKey fk : foreignKeys) {

			sb.append(", ");
			sb.append(getForeignKeyClause(fk));

		}

		// END CREATE TABLE
		sb.append(" ) ");

		return sb.toString();

	}

	protected void populateMergeParams(ViewEntry ve, Vector<?> vals,
			PreparedStatement ps) {

		try {

			// Retrieve Values
			Timestamp lastMod = getTimestamp(vals, 0);
			String universalId = ve.getUniversalID();
			String replicaId = getReplicaId();

			int paramNo = 1;

			ps.setString(paramNo++, replicaId);
			ps.setString(paramNo++, universalId);

			int colNumber = 1;

			for (SqlSyncColumn col : valueColumns) {

				switch (col.getType()) {

				case FIXEDLENGTHSTRING:
					String fsval = getString(vals, colNumber);
					ps.setString(paramNo++, fsval);
					break;
				case STRING:
					String val = getString(vals, colNumber);
					ps.setString(paramNo++, val);
					break;
				case DATE:
					java.sql.Date dateval = getDate(vals, colNumber);
					ps.setDate(paramNo++, dateval);
					break;
				case DATETIME:
					Timestamp ts = getTimestamp(vals, colNumber);
					ps.setTimestamp(paramNo++, ts);
					break;
				case BOOLEAN:
					Boolean b = getBoolean(vals, colNumber);
					ps.setBoolean(paramNo++, b);
					break;
				case INTEGER:
					Integer i = getInteger(vals, colNumber);
					ps.setInt(paramNo++, i);
					break;

				default:
					break;
				}

				colNumber++;

			}

			ps.setTimestamp(paramNo++, lastMod);

		} catch (Exception e) {

		}

	}

	protected void addIdentityField(String columnName, int columnNo,
			SqlSyncDataType type) {

		SqlSyncColumn col = new SqlSyncColumn(columnName, type);
		this.identityColumns.add(col);

	}

	protected void addStringColumn(String columnName) {
		addValueField(columnName, SqlSyncDataType.STRING);
	}

	protected void addStringColumn(String columnName, Integer length) {
		SqlSyncColumn col = new SqlSyncColumn(columnName,
				SqlSyncDataType.STRING);
		col.setLength(length);
		this.valueColumns.add(col);
	}

	protected void addFixedLengthStringColumn(String columnName, Integer length) {
		SqlSyncColumn col = new SqlSyncColumn(columnName,
				SqlSyncDataType.FIXEDLENGTHSTRING);
		col.setLength(length);
		this.valueColumns.add(col);
	}

	protected void addBooleanColumn(String columnName) {
		addValueField(columnName, SqlSyncDataType.BOOLEAN);
	}

	protected void addIntegerColumn(String columnName) {
		addValueField(columnName, SqlSyncDataType.INTEGER);
	}

	protected void addDateColumn(String columnName) {
		addValueField(columnName, SqlSyncDataType.DATE);
	}

	protected void addDateTimeColumn(String columnName) {
		addValueField(columnName, SqlSyncDataType.DATETIME);
	}

	protected void addValueField(String columnName, SqlSyncDataType type) {

		SqlSyncColumn col = new SqlSyncColumn(columnName, type);
		this.valueColumns.add(col);
	}

	protected void addExtraField(String columnName, SqlSyncDataType type) {
		SqlSyncColumn col = new SqlSyncColumn(columnName, type);
		this.extraColumns.add(col);
	}

	protected void addExtraField(String columnName, SqlSyncDataType type,
			Integer length) {
		SqlSyncColumn col = new SqlSyncColumn(columnName, type);
		col.setLength(length);
		this.extraColumns.add(col);
	}

	protected void setLastModifiedFieldName(String fieldName) {
		this.lastModifiedFieldName = fieldName;
	}

	protected void addForeignKey(SqlSyncForeignKey key) {
		this.foreignKeys.add(key);
	}

	protected void addUnique(String... columnNames) {

		List<String> cols = new ArrayList<String>();

		for (String string : columnNames) {
			cols.add(string);
		}

		this.unique.add(cols);
	}

	protected void addIndex(String indexName, String... columns) {

		List<String> cols = new ArrayList<String>();

		for (String string : columns) {
			cols.add(string);
		}

		if (cols.isEmpty())
			return;

		indexes.put(indexName.toUpperCase(), cols);

	}

	private int mergeRecord(ViewEntry ve, Vector<?> vals, PreparedStatement ps) {

		try {

			ps.clearParameters();
			// populateInsertParams(ve, vals, ps);
			populateMergeParams(ve, vals, ps);
			return ps.executeUpdate();

		} catch (SQLIntegrityConstraintViolationException e) {
			return 0;
		} catch (SQLException e) {

			e.printStackTrace();
			// logger.info(e.getMessage());

			return 0;
		}

	}

	public void sync(Connection con) {

		String mergeQuery = getMergeQuery();

		try {

			PreparedStatement ps = con.prepareStatement(mergeQuery);

			Database db = DominoUtils.getCurrentDatabase();
			View view = db.getView(getViewName());

			if (view == null) {
//				logger.error("Sync error: View {} Not found in DB {}",
//						getViewName(), db.getFilePath());
				return;
			}

			ViewEntryCollection vec = null;

			if (lastRefreshed != null) {

				Date tomorrow = AbstractSqlRepository.getDateInOneHour();
				Date fromDate = lastRefreshed;

				DateRange dr = view.getParent().getParent()
						.createDateRange(fromDate, tomorrow);

				vec = view.getAllEntriesByKey(dr, true);
			} else {
				vec = view.getAllEntries();
			}

			ViewEntry ve = vec.getFirstEntry();

			int count = 0;

			while (ve != null) {

				ViewEntry next = vec.getNextEntry(ve);

				Vector<?> vals = ve.getColumnValues();

				int j = mergeRecord(ve, vals, ps);
				count = count + j;

				ve.recycle(vals);
				ve = next;

			}

			lastRefreshed = new Date();

		} catch (SQLException e) {
			//logger.error("SQL Error During Sync", e);
			e.printStackTrace();
		} catch (NotesException e) {
			//logger.error("Notes Error During Sync", e);
		} catch (Exception e) {
			//logger.error("Error During Sync", e);
		}

	}

	public void deleteRecords(Connection con) {

		String sql = "DELETE FROM " + getTableName() + " WHERE replicaId = '"
				+ replicaId + "'";

		try {
			con.createStatement().executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void createTableAuto(Connection con) {

		String sql = getCreateTableSqlAuto();

		try {
			con.createStatement().executeUpdate(sql);
		} catch (SQLException e) {
			//logger.error(e.getMessage());
		}

	}

	public void createTable(Connection con) {

		if (!tableExists) {

			String sql = getCreateTableSql();

			try {

				con.createStatement().executeUpdate(sql);

				tableExists = true;

			} catch (SQLException e) {
				//logger.error(e.getMessage());
			}
		}

	}

	private boolean indexExists(Connection con, String index)
			throws SQLException {

		String qry = "SELECT INDEX_NAME FROM INFORMATION_SCHEMA.SYSTEM_INDEXINFO "
				+ "WHERE INDEX_NAME = '%s' AND TABLE_SCHEM = 'PUBLIC'";

		qry = String.format(qry, index);

		Statement s = con.createStatement();
		ResultSet rs = s.executeQuery(qry);

		if (rs.next()) {
			return true;
		}

		return false;

	}

	private String getCreateIndexQuery(String indexName, List<String> columns) {

		StringBuilder sb = new StringBuilder();

		String cols = joinColumnNames(columns, true);

		sb.append("CREATE INDEX ");
		sb.append(indexName);
		sb.append(" ON ");
		sb.append(quote(getTableName()));
		sb.append(" (");
		sb.append(cols);
		sb.append("); ");

		return sb.toString();

	}

	public void createIndexes(Connection con) {

		if (indexesExist)
			return;

		boolean somethingbad = false;

		for (Entry<String, List<String>> index : indexes.entrySet()) {

			String indexName = index.getKey();

			try {

				if (!indexExists(con, indexName)) {

					String sql = getCreateIndexQuery(indexName,
							index.getValue());
					con.createStatement().executeUpdate(sql);

					//logger.info("Index Created : {}", indexName);

				} else {
					//logger.debug("Index Existed : {}", indexName);
				}

			} catch (SQLException e) {
				//logger.error(e.getMessage());
				somethingbad = true;
			}

		}

		if (!somethingbad) {
			indexesExist = true;
		}

	}

	private String getQuestionMarks() {

		int size = getAllFields().size();
		if (size == 0)
			return "";
		if (size == 1)
			return "?";
		return StringUtil.repeat("?,", size - 1) + "?";
	}

	private String joinColumnNames(List<String> cols, boolean quote) {

		StringBuilder sb = new StringBuilder();

		boolean first = true;

		for (String col : cols) {

			if (!first) {
				sb.append(", ");
			}

			if (quote) {
				sb.append(quote(col));
			} else {
				sb.append(col);
			}

			first = false;

		}

		return sb.toString();
	}

	private String join(List<SqlSyncColumn> cols, boolean quote) {

		StringBuilder sb = new StringBuilder();

		boolean first = true;

		for (SqlSyncColumn col : cols) {

			if (!first) {
				sb.append(", ");
			}

			if (quote) {
				sb.append(quote(col.getColumnName()));
			} else {
				sb.append(col.getColumnName());
			}

			first = false;

		}

		return sb.toString();
	}

	private String join(List<SqlSyncColumn> cols, String tableName,
			boolean quote) {

		StringBuilder sb = new StringBuilder();

		boolean first = true;

		for (SqlSyncColumn col : cols) {

			if (!first) {
				sb.append(", ");
			}

			sb.append(tableName);
			sb.append(".");

			if (quote) {
				sb.append(quote(col.getColumnName()));
			} else {
				sb.append(col.getColumnName());
			}

			first = false;

		}

		return sb.toString();

	}

	protected List<SqlSyncColumn> getAllFields() {

		List<SqlSyncColumn> fields = new ArrayList<SqlSyncColumn>();
		fields.addAll(identityColumns);
		fields.addAll(valueColumns);
		fields.add(lastModifiedColumn);
		return fields;

	}

	protected String getUpdateFieldsClause() {

		List<SqlSyncColumn> fields = new ArrayList<SqlSyncColumn>();

		fields.addAll(valueColumns);
		fields.add(lastModifiedColumn);

		String tableName = quote(getTableName());

		StringBuilder sb = new StringBuilder();

		boolean first = true;

		for (SqlSyncColumn column : fields) {

			if (!first) {
				sb.append(", ");
			}

			sb.append(quoteColumn(column.getColumnName(), tableName));
			sb.append(" = ");
			sb.append(quoteColumn(column.getColumnName(), "I"));

			first = false;

		}

		return sb.toString();

	}

	protected String getIdentityCondition() {

		boolean isfirstcol = true;

		StringBuilder sb = new StringBuilder();

		String tableName = quote(getTableName());

		for (SqlSyncColumn column : identityColumns) {

			if (!isfirstcol) {
				sb.append(" AND ");
			}

			sb.append(quoteColumn(column.getColumnName(), tableName));
			sb.append(" = ");
			sb.append(quoteColumn(column.getColumnName(), "I"));

			isfirstcol = false;

		}

		return sb.toString();

	}

	protected String getLastModifiedDiffClause() {

		StringBuilder sb = new StringBuilder();
		String table = quote(getTableName());
		sb.append(quoteColumn(lastModifiedFieldName, table));
		sb.append(" != ");
		sb.append(quoteColumn(lastModifiedFieldName, "I"));

		return sb.toString();

	}

	public String getMergeQuery() {

		if (StringUtil.isEmpty(mergeQuery) || true) {

			StringBuilder sb = new StringBuilder();

			String tableName = quote(getTableName());
			String questionMarks = getQuestionMarks();
			String allFields = join(getAllFields(), true);
			String identityCond = getIdentityCondition();
			@SuppressWarnings("unused")
			String lastModDiff = getLastModifiedDiffClause();
			String updateFields = getUpdateFieldsClause();
			String insertFieldNames = join(getAllFields(), true);
			String insertFieldValueNames = join(getAllFields(), "I", true);

			sb.append("MERGE INTO %s USING (VALUES %s) I ( %s ) ");
			// Seem to have a problem with the LastMod matching clause
			// sb.append("ON (%s) WHEN MATCHED AND %s THEN UPDATE SET %s ");
			sb.append("ON (%s) WHEN MATCHED THEN UPDATE SET %s ");
			sb.append("WHEN NOT MATCHED THEN INSERT (%s) VALUES (%s)");

			mergeQuery = String.format(sb.toString(), tableName, questionMarks,
					allFields, identityCond, updateFields, insertFieldNames,
					insertFieldValueNames);

			try {
				PrintWriter pw = new PrintWriter(
						"C:\\Users\\cgregor\\Desktop\\HSQLTEST\\merge_"
								+ getTableName() + ".txt");
				pw.print(mergeQuery);
				pw.close();

			} catch (FileNotFoundException e) {

				e.printStackTrace();
			}

		}

		return mergeQuery;

	}

	public String getMergeQueryMssql() {
		// MERGE INTO jobhub
		// USING (VALUES ('replica123456789', 'G1e000'))
		// I (replicaId, JobNo)
		// ON (jobhub.replicaId = I.replicaId)
		// WHEN MATCHED
		// THEN UPDATE SET
		// jobhub.JobNo = I.JobNo
		// WHEN NOT MATCHED
		// THEN INSERT (replicaId, JobNo) VALUES (I.replicaId, I.JobNo);
		return null;
	}

	public static String quote(String value) {
		return "\"" + value + "\"";
	}

	public static String quoteColumn(String column, String table) {
		return table + "." + quote(column);
	}

	public String getSQL(String fileName) {

		String sql = null;

		try {

			InputStream is = getClass().getResourceAsStream(fileName);

			// InputStream is = FacesContext.getCurrentInstance()
			// .getExternalContext().getResourceAsStream("/" + fileName);

			sql = AbstractSqlRepository.getStringFromInputStream(is);
			is.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return sql;

	}

	protected String getString(Vector<?> vals, int colNumber) {
		return (String) vals.elementAt(colNumber);
	}

	protected Boolean getBoolean(Vector<?> vals, int colNumber) {
		// Valid
		String validString = vals.elementAt(colNumber).toString();
		if (StringUtil.equals(validString, "1")
				|| StringUtil.equals(validString, "1.0")) {
			return true;
		} else {
			return false;
		}
	}

	protected Integer getInteger(Vector<?> vals, int colNumber) {

		Object o = vals.elementAt(colNumber);

		if (o instanceof String) {
			try {
				return Integer.parseInt((String) o);
			} catch (NumberFormatException e) {
				return null;
			}
		}

		if (o instanceof Number) {
			return ((Number) o).intValue();
		}
		return null;

	}

	protected java.sql.Date getDate(Vector<?> vals, int colNumber) {

		Date lastExpeditedDate = null;
		Object lastExpeditedDateObject = vals.elementAt(colNumber++);

		if (lastExpeditedDateObject instanceof lotus.domino.DateTime) {
			try {
				lastExpeditedDate = ((lotus.domino.DateTime) lastExpeditedDateObject)
						.toJavaDate();
				return new java.sql.Date(lastExpeditedDate.getTime());
			} catch (NotesException e) {
			}
		}

		return null;

	}

	protected java.sql.Timestamp getTimestamp(Vector<?> vals, int colNumber) {

		Object dateObject = vals.elementAt(colNumber++);

		if (dateObject instanceof lotus.domino.DateTime) {

			try {
				Date lastModifiedDate = ((lotus.domino.DateTime) dateObject)
						.toJavaDate();
				return new java.sql.Timestamp(lastModifiedDate.getTime());
			} catch (NotesException e) {
				return null;
			}
		}
		return null;
	}
}
