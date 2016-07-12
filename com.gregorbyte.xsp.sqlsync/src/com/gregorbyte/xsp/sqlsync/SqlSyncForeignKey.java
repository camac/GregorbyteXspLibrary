package com.gregorbyte.xsp.sqlsync;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SqlSyncForeignKey implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<String> thisColumns = new ArrayList<String>();
	private String foreignTable = null;
	private List<String> thatColumns = new ArrayList<String>();

	public SqlSyncForeignKey(String table) {
		this.foreignTable = table;
	}

	public SqlSyncForeignKey addColumns(String thisColumn, String thatColumn) {

		thisColumns.add(thisColumn);
		thatColumns.add(thatColumn);

		return this;

	}

	public List<String> getThisColumns() {
		return thisColumns;
	}

	public void setThisColumns(List<String> thisColumns) {
		this.thisColumns = thisColumns;
	}

	public String getForeignTable() {
		return foreignTable;
	}

	public void setForeignTable(String foreignTable) {
		this.foreignTable = foreignTable;
	}

	public List<String> getThatColumns() {
		return thatColumns;
	}

	public void setThatColumns(List<String> thatColumns) {
		this.thatColumns = thatColumns;
	}

}
