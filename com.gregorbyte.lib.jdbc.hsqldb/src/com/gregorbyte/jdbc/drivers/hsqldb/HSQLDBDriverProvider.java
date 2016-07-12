package com.gregorbyte.jdbc.drivers.hsqldb;


import java.sql.Driver;
import java.sql.SQLException;

import com.ibm.commons.jdbc.drivers.IJDBCDriverAlias;
import com.ibm.commons.jdbc.drivers.JDBCProvider;

public class HSQLDBDriverProvider implements JDBCProvider {

	public HSQLDBDriverProvider() {

	}

	@Override
	public Driver loadDriver(String className) throws SQLException {

		if (className.equals(org.hsqldb.jdbcDriver.class.getName())) {
			return new org.hsqldb.jdbcDriver();
		}

		if (className.equals(org.hsqldb.jdbc.JDBCDriver.class.getName())) {
			return new org.hsqldb.jdbc.JDBCDriver();
		}

		return null;
	}

	@Override
	public IJDBCDriverAlias[] getDriverAliases() {
		return null;
	}

}
