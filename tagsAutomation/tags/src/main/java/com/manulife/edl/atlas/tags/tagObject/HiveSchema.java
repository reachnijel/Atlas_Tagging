package com.manulife.edl.atlas.tags.tagObject;

import java.util.HashMap;
import java.util.Vector;

public class HiveSchema {
    private String schemaName;
    //	private String guid;
    private HashMap<String, HiveTable> tables;
    private Vector<String> tablesList;

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    //	public String getGuid() {
//		return guid;
//	}
//	public void setGuid(String guid) {
//		this.guid = guid;
//	}
    public HashMap<String, HiveTable> getTables() {
        return tables;
    }

    public void setTables(HashMap<String, HiveTable> tables) {
        this.tables = tables;
    }

    public Vector<String> getTablesList() {
        return tablesList;
    }

    public void setTablesList(Vector<String> tablesList) {
        this.tablesList = tablesList;
    }
}
