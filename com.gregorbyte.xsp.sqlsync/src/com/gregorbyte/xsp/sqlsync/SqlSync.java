package com.gregorbyte.xsp.sqlsync;

import java.sql.Connection;

public interface SqlSync {

	public void clearLastRefreshed();

	public void sync(Connection conn);

}
