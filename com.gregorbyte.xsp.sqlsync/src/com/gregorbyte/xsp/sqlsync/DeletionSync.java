package com.gregorbyte.xsp.sqlsync;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.model.domino.DominoUtils;

import lotus.domino.Database;
import lotus.domino.DateRange;
import lotus.domino.NotesException;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;

public class DeletionSync implements SqlSync, Serializable {

	private static final long serialVersionUID = 1L;

//	private static final Logger logger = LoggerFactory
//			.getLogger(SqlTableSync.class);

	//private DateTime lastRefreshed = null;
	private Date lastRefreshed = null;
	private String viewName = null;
	private String replicaId = null;

	private Map<String, String> deleteQueries = new HashMap<String, String>();

	public DeletionSync(String viewName, String replicaId) {
		this.viewName = viewName;
		this.replicaId = replicaId;
	}

	public void clearLastRefreshed() {
		this.lastRefreshed = null;
	}

	public void addFormMapping(String formName, String tableName) {

		String sql = String.format(
				"DELETE FROM \"%s\" WHERE \"replicaId\" = ? AND \"unid\" = ?",
				tableName);
		deleteQueries.put(formName.toLowerCase(), sql);

	}

	public String getViewName() {
		return viewName;
	}

	private String getSql(String formName) {
		return deleteQueries.get(formName.toLowerCase());
	}

	private PreparedStatement getPreparedStatement(
			Map<String, PreparedStatement> cache, Connection conn,
			String formName) throws SQLException {

		String key = formName.toLowerCase();

		if (cache.containsKey(key)) {
			return cache.get(key);
		}

		String sql = getSql(formName);

		if (StringUtil.isEmpty(sql))
			return null;

		PreparedStatement ps = conn.prepareStatement(sql);

		ps.setString(1, replicaId);

		cache.put(key, ps);

		return ps;

	}

	public String getReplicaId() {
		return replicaId;
	}

	public void sync(Connection con) {

		try {

			Database db = DominoUtils.getCurrentDatabase();
			View view = db.getView(getViewName());

			if (view == null) {
//				logger.error(
//						"Sync error: Deletions View {} Not found in DB {}",
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

			Map<String, PreparedStatement> cache = new HashMap<String, PreparedStatement>();

			while (ve != null) {

				ViewEntry next = vec.getNextEntry(ve);

				Vector<?> vals = ve.getColumnValues();

				String form = getString(vals, 1);
				String unid = getString(vals, 2);

				PreparedStatement ps = getPreparedStatement(cache, con, form);

				int j = 0;

				if (ps != null) {
					ps.setString(2, unid);
					j = ps.executeUpdate();
				}

				count = count + j;

				ve.recycle(vals);
				ve = next;

			}

			lastRefreshed = new Date();

		} catch (SQLException e) {
			//logger.error("SQL Error During Sync", e);
		} catch (NotesException e) {
			//logger.error("Notes Error During Sync", e);
		} catch (Exception e) {
			//logger.error("Error During Sync", e);
		}

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
