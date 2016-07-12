package com.gregorbyte.xsp.sqlsync;

import java.sql.Connection;
import java.sql.SQLException;

import javax.faces.context.FacesContext;

import lotus.domino.NotesException;

import com.gregorbyte.xsp.sqlsync.AbstractSqlRepository;
import com.ibm.xsp.extlib.relational.util.JdbcUtil;
import com.ibm.xsp.model.domino.DominoUtils;

public class ExampleSqlRepository extends AbstractSqlRepository {

	private static final long serialVersionUID = 4506117317844049212L;

	public static final String BEAN_NAME = "exampleSqlRepo";
	public static final String CONN_NAME = "examplehsql";

	@Override
	public void loadSyncDetails() {

		try {

			String replicaId = DominoUtils.getCurrentDatabase().getReplicaID();

			addSync(new ExampleSqlSync(replicaId));

		} catch (NotesException e) {

		}

	}

	protected Connection getConnectionByName(String connectionName)
			throws SQLException {
		return JdbcUtil.createNamedConnection(
				FacesContext.getCurrentInstance(), connectionName);
	}

	@Override
	public Connection getConnection() throws SQLException {
		return getConnectionByName(CONN_NAME);
	}

}
