package com.gregorbyte.xsp.sqlsync;

import java.io.Serializable;

public class SqlSyncColumn implements Serializable {

	private static final long serialVersionUID = 1L;

	private static final Integer DEFAULT_TEXT_LENGTH = 255;
	private static final Integer MAX_TEXT_LENGTH = 255;

	// SQL Table
	private String columnName;
	// Type
	private SqlSyncDataType type;
	// Length for String type
	private Integer length = null;

	public SqlSyncColumn(String columnName, SqlSyncDataType type) {
		this.columnName = columnName;
		this.type = type;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public SqlSyncDataType getType() {
		return type;
	}

	public void setType(SqlSyncDataType type) {
		this.type = type;
	}

	public Integer getLength() {

		if (length == null) {
			return DEFAULT_TEXT_LENGTH;
		}

		return length;
	}

	public void setLength(Integer length) {

		if (length > MAX_TEXT_LENGTH) {
			this.length = MAX_TEXT_LENGTH;
		}

		this.length = length;
	}

}
