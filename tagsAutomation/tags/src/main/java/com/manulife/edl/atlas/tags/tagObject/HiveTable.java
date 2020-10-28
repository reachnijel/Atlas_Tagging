package com.manulife.edl.atlas.tags.tagObject;

import java.util.HashMap;
import java.util.Vector;

public class HiveTable {
    private String tableName;
    private String guid;
    private HashMap<String, HiveColumn> columns;
    private Vector<String> columnsList;

    private String schemaName;
    private Vector<String> tags;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public HashMap<String, HiveColumn> getColumns() {
        return columns;
    }

    public void setColumns(HashMap<String, HiveColumn> columns) {
        this.columns = columns;
    }

    public Vector<String> getColumnsList() {
        return columnsList;
    }

    public void setColumnsList(Vector<String> columnsList) {
        this.columnsList = columnsList;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public Vector<String> getTags() {
        return tags;
    }

    public void setTags(Vector<String> tags) {
        this.tags = tags;
    }

}
