package com.manulife.edl.atlas.tags.tagObject;

import java.util.Vector;

public class HiveColumn {
    private String columnName;
    private String guid;
    private Vector<String> tags;

    private String schemaName;
    private String tableName;

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Vector<String> getTags() {
        return tags;
    }

    public void setTags(Vector<String> tags) {
        this.tags = tags;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public HiveColumn clone() {
        HiveColumn hiveCol = new HiveColumn();
        hiveCol.setColumnName(columnName);
        hiveCol.setGuid(guid);
        hiveCol.setSchemaName(schemaName);
        hiveCol.setTableName(tableName);
        hiveCol.setTags(tags);
        return hiveCol;
    }
}
