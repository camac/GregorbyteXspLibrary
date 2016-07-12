package com.gregorbyte.xsp.sqlsync;

import com.gregorbyte.xsp.sqlsync.SqlTableSync;

public class ExampleSqlSync extends SqlTableSync {

	private static final long serialVersionUID = 1L;

	public ExampleSqlSync(String replicaId) {
		
		super("SqlSyncExamples", "examples", replicaId);

		addStringColumn("id");
		addStringColumn("name");
		addStringColumn("description");
		
		addIndex("indexname", "name", "description");
		
	}

}
