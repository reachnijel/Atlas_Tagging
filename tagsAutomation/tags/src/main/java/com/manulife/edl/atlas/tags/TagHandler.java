package com.manulife.edl.atlas.tags;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.manulife.edl.atlas.tags.tagObject.HiveColumn;
import com.manulife.edl.atlas.tags.tagObject.HiveSchema;
import com.manulife.edl.atlas.tags.tagObject.HiveTable;
import com.manulife.edl.atlas.tags.tagObject.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TagHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final int DEFAULT_LIMIT = 10000;

    public TagHandler() {

    }

    public HashMap<String, Tag> readTags(String tagsJson) throws JSONException {
        HashMap<String, Tag> tags = new HashMap<String, Tag>();

        JSONObject o = new JSONObject(tagsJson);
        if (o.has("classificationDefs")) {
            JSONArray classificationDefs = o.getJSONArray("classificationDefs");
            for (int n = 0; n < classificationDefs.length(); n++) {
                Tag tag = new Tag();

                JSONObject classification = classificationDefs.getJSONObject(n);
                tag.setTagName(classification.getString("name"));
                LOGGER.info(tag.getTagName());
                tag.setGuid(classification.getString("guid"));
                LOGGER.info(tag.getGuid());
                tag.setTagDesc(classification.getString("description"));
                LOGGER.info(tag.getTagDesc());

                tags.put(tag.getTagName(), tag);
            }
        }

        return tags;
    }

    public HashMap<String, HiveSchema> getAllTagsSchemaWise(RestfulHandler atlasApi, Vector<HiveColumn> columns) throws JSONException, URISyntaxException {
        HashMap<String, HiveSchema> schemas = new HashMap<String, HiveSchema>();
        // get the unique schemas  from DD .
        Vector<String> uniqueSchemas = getUniqueSchemas(columns);
        // get all the  Tags  associated with the columns in that schema
        for (String schema : uniqueSchemas) {
            int offset = 0;
            int entitiesCount = 1; // initialization
            int iterationcount = 0;
            while (entitiesCount >= 1) {

                JSONObject o = new JSONObject(atlasApi.getTags4ColumnSchema(schema, DEFAULT_LIMIT, offset));
                if (o.has("entities")) {
                    JSONArray entities = o.getJSONArray("entities");
                    entitiesCount = entities.length();
                    offset = offset + entitiesCount;
                    LOGGER.info("No. of entities: " + entitiesCount);
                    for (int i = 0; i < entities.length(); i++) {
                        JSONObject object = entities.getJSONObject(i);

                        if (object.has("typeName")) {
                            //logger.info("Type Name: " + object.getString("typeName"));
                            if (object.getString("typeName").equals("hive_column")) {
                                String status = object.getString("status");
                                if (!status.equalsIgnoreCase("ACTIVE")) {
                                    continue;
                                }
                                JSONObject atrObj = object.getJSONObject("attributes");
                                String columnName = null;
                                if (!atrObj.isNull("name")) {
                                    columnName = atrObj.getString("name");
                                }

                                LOGGER.debug("Column Name: " + columnName);
                                String guid = object.getString("guid");
                                LOGGER.debug("Guid: " + guid);
                                String qualifiedName = object.getJSONObject("attributes").getString("qualifiedName");
                                LOGGER.debug("Qualified Name: " + qualifiedName);
                                String schemaName = qualifiedName.split("\\.", 3)[0];
                                LOGGER.debug("Schema: " + schemaName);
                                String tableName = qualifiedName.split("\\.", 3)[1];
                                LOGGER.debug("Table: " + tableName);

                                Vector<String> strTags = new Vector<String>();
                                if (object.has("classificationNames")) {
                                    JSONArray classificationNames = object.getJSONArray("classificationNames");
                                    for (int j = 0; j < classificationNames.length(); j++) {
                                        strTags.add(classificationNames.getString(j));
                                        LOGGER.info(classificationNames.getString(j));
                                    }
                                }

                                if (!Strings.isNullOrEmpty(guid)) {
                                    boolean tableExist = false;
                                    boolean columnExist = false;
                                    HiveSchema tempSchema = new HiveSchema();
                                    HashMap<String, HiveTable> tempTables = new HashMap<String, HiveTable>();
                                    Vector<String> tempTablesList = new Vector<String>();
                                    HiveTable tempTable = new HiveTable();
                                    HashMap<String, HiveColumn> tempColumns = new HashMap<String, HiveColumn>();
                                    Vector<String> tempColumnsList = new Vector<String>();
                                    HiveColumn tempColumn = new HiveColumn();
                                    Vector<String> tempTags = new Vector<String>();
                                    if (schemas.containsKey(schemaName)) {
                                        tempSchema = schemas.get(schemaName);
                                        tempTables = tempSchema.getTables();
                                        tempTablesList = tempSchema.getTablesList();
                                        if (tempTablesList.contains(tableName)) {
                                            tempTable = tempTables.get(tableName);
                                            tempColumns = tempTable.getColumns();
                                            tempColumnsList = tempTable.getColumnsList();
                                            if (tempColumnsList.contains(columnName)) {
                                                tempColumn = tempColumns.get(columnName);
                                                tempTags = tempColumn.getTags();
                                                columnExist = true;
                                            } else {
                                                tempColumn.setColumnName(columnName);
                                                tempColumn.setGuid(guid);
                                            }
                                            tableExist = true;
                                        } else {
                                            tempColumn.setColumnName(columnName);
                                            tempColumn.setGuid(guid);
                                            tempTable.setTableName(tableName);
                                        }
                                    } else {
                                        tempColumn.setColumnName(columnName);
                                        tempColumn.setGuid(guid);
                                        tempTable.setTableName(tableName);
                                        tempSchema.setSchemaName(schemaName);
                                    }
                                    for (String tempTag : strTags) {
                                        if (!tempTags.contains(tempTag)) {
                                            tempTags.add(tempTag);
                                        }
                                    }
                                    tempColumn.setTags(tempTags);
                                    if (columnExist) {
                                        tempColumns.replace(columnName, tempColumn);
                                    } else {
                                        tempColumns.put(columnName, tempColumn);
                                        tempColumnsList.add(columnName);
                                    }

                                    tempTable.setColumns(tempColumns);
                                    tempTable.setColumnsList(tempColumnsList);
                                    if (tableExist) {
                                        tempTables.replace(tableName, tempTable);
                                    } else {
                                        tempTables.put(tableName, tempTable);
                                        tempTablesList.add(tableName);
                                    }

                                    tempSchema.setTables(tempTables);
                                    tempSchema.setTablesList(tempTablesList);
                                    schemas.put(schemaName, tempSchema);
                                }

                            }
                        }
                    }
                } else {
                    break;
                }
            }
        }
        return schemas;

    }

    public Vector<String> getUniqueSchemas(Vector<HiveColumn> columns) {
        Iterator<HiveColumn> colIter = columns.iterator();
        Set<String> schemas = new LinkedHashSet<String>();

        while (colIter.hasNext()) {
            HiveColumn col = colIter.next();
            schemas.add(col.getSchemaName());
        }

        return new Vector<String>(schemas);
    }

    public HashMap<String, HiveSchema> getAllTaggedSchema(RestfulHandler atlasApi, HashMap<String, Tag> tags) throws JSONException, URISyntaxException {
        HashMap<String, HiveSchema> schemas = new HashMap<String, HiveSchema>();

        Iterator<String> tagIter = tags.keySet().iterator();
        while (tagIter.hasNext()) {
            Tag tag = tags.get(tagIter.next());
            int limit = 10000; //Max 10000
            int offset = 0;
            boolean callAtlas = true;
            while (callAtlas) {
                JSONObject o = new JSONObject(atlasApi.getHiveColumnByTag(Integer.toString(limit), tag.getTagName(), Integer.toString(offset)));

                if (o.has("entities")) {
                    JSONArray entities = o.getJSONArray("entities");
                    LOGGER.info("No. of entities: " + entities.length());
                    if (entities.length() >= limit) {
                        offset += limit;
                    } else {
                        callAtlas = false;
                    }

                    for (int i = 0; i < entities.length(); i++) {
                        JSONObject object = entities.getJSONObject(i);
                        if (object.has("typeName")) {
                            LOGGER.info("Type Name: " + object.getString("typeName"));
                            if (object.getString("typeName").equals("hive_column")) {
                                if (object.has("status")) {
                                    LOGGER.info("Status: " + object.getString("status"));
                                    if (object.getString("status").equals("ACTIVE")) {
                                        String columnName = object.getJSONObject("attributes").getString("name");
                                        LOGGER.info("Column Name: " + columnName);
                                        String guid = object.getString("guid");
                                        LOGGER.info("Guid: " + guid);
                                        String qualifiedName = object.getJSONObject("attributes").getString("qualifiedName");
                                        LOGGER.info("Qualified Name: " + qualifiedName);
                                        String schemaName = qualifiedName.split("\\.", 3)[0];
                                        LOGGER.info("Schema: " + schemaName);
                                        String tableName = qualifiedName.split("\\.", 3)[1];
                                        LOGGER.info("Table: " + tableName);

                                        Vector<String> strTags = new Vector<String>();
                                        if (object.has("classificationNames")) {
                                            JSONArray classificationNames = object.getJSONArray("classificationNames");
                                            for (int j = 0; j < classificationNames.length(); j++) {
                                                strTags.add(classificationNames.getString(j));
                                                LOGGER.info(classificationNames.getString(j));
                                            }
                                        }

                                        if (!"".equals(guid)) {
                                            boolean tableExist = false;
                                            boolean columnExist = false;
                                            HiveSchema tempSchema = new HiveSchema();
                                            HashMap<String, HiveTable> tempTables = new HashMap<String, HiveTable>();
                                            Vector<String> tempTablesList = new Vector<String>();
                                            HiveTable tempTable = new HiveTable();
                                            HashMap<String, HiveColumn> tempColumns = new HashMap<String, HiveColumn>();
                                            Vector<String> tempColumnsList = new Vector<String>();
                                            HiveColumn tempColumn = new HiveColumn();
                                            Vector<String> tempTags = new Vector<String>();
                                            if (schemas.containsKey(schemaName)) {
                                                tempSchema = schemas.get(schemaName);
                                                tempTables = tempSchema.getTables();
                                                tempTablesList = tempSchema.getTablesList();
                                                if (tempTablesList.contains(tableName)) {
                                                    tempTable = tempTables.get(tableName);
                                                    tempColumns = tempTable.getColumns();
                                                    tempColumnsList = tempTable.getColumnsList();
                                                    if (tempColumnsList.contains(columnName)) {
                                                        tempColumn = tempColumns.get(columnName);
                                                        tempTags = tempColumn.getTags();
                                                        columnExist = true;
                                                    } else {
                                                        tempColumn.setColumnName(columnName);
                                                        tempColumn.setGuid(guid);
                                                    }
                                                    tableExist = true;
                                                } else {
                                                    tempColumn.setColumnName(columnName);
                                                    tempColumn.setGuid(guid);
                                                    tempTable.setTableName(tableName);
                                                }
                                            } else {
                                                tempColumn.setColumnName(columnName);
                                                tempColumn.setGuid(guid);
                                                tempTable.setTableName(tableName);
                                                tempSchema.setSchemaName(schemaName);
                                            }
                                            for (String tempTag : strTags) {
                                                if (!tempTags.contains(tempTag)) {
                                                    tempTags.add(tempTag);
                                                }
                                            }
                                            tempColumn.setTags(tempTags);
                                            if (columnExist) {
                                                tempColumns.replace(columnName, tempColumn);
                                            } else {
                                                tempColumns.put(columnName, tempColumn);
                                                tempColumnsList.add(columnName);
                                            }

                                            tempTable.setColumns(tempColumns);

                                            tempTable.setColumnsList(tempColumnsList);
                                            if (tableExist) {
                                                tempTables.replace(tableName, tempTable);
                                            } else {
                                                tempTables.put(tableName, tempTable);
                                                tempTablesList.add(tableName);
                                            }

                                            tempSchema.setTables(tempTables);
                                            tempSchema.setTablesList(tempTablesList);
                                            schemas.put(schemaName, tempSchema);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    callAtlas = false;
                    LOGGER.info("entities is empty!!!");
                }
            }
        }

        return schemas;
    }

    public Vector<HiveColumn> getColumGuidByTagName(RestfulHandler atlasApi, String tag) throws JSONException, URISyntaxException {
        Vector<HiveColumn> columns = new Vector<HiveColumn>();

        int limit = 10000; //Max 10000
        int offset = 0;
        boolean callAtlas = true;
        while (callAtlas) {
            JSONObject o = new JSONObject(atlasApi.getHiveColumnByTag(Integer.toString(limit), tag, Integer.toString(offset)));

            if (o.has("entities")) {
                JSONArray entities = o.getJSONArray("entities");
                LOGGER.info("No. of entities: " + entities.length());
                if (entities.length() >= limit) {
                    offset += limit;
                } else {
                    callAtlas = false;
                }
                for (int i = 0; i < entities.length(); i++) {
                    HiveColumn col = new HiveColumn();
                    JSONObject object = entities.getJSONObject(i);
                    String colname = object.getJSONObject("attributes").getString("name");
                    String guid = object.getString("guid");
                    LOGGER.info(object.getString("guid"));
                    LOGGER.info(object.getJSONObject("attributes").getString("qualifiedName"));
                    col.setGuid(guid);
                    col.setColumnName(colname);
                    col.setTableName(object.getJSONObject("attributes").getString("qualifiedName").split("\\.", 3)[1]);
                    col.setSchemaName(object.getJSONObject("attributes").getString("qualifiedName").split("\\.", 3)[0]);
                    col.setTags(new Vector<String>());
                    columns.add(col);
                }
            } else {
                callAtlas = false;
                LOGGER.info("entities is empty!!!");
            }
        }

        return columns;
    }

    public Vector<HiveColumn> getAllColumGuid(RestfulHandler atlasApi, Vector<HiveColumn> columns) throws JSONException, URISyntaxException {
        //Hashtable<String,String> tables = new Hashtable<String,String>();
        Hashtable<String, String> columnHashTbl = new Hashtable<String, String>();
        ConcurrentHashMap<String, HiveColumn> tmpArray = new ConcurrentHashMap<String, HiveColumn>();

        int i = 0;
        // Control the total API calls hitting Atlas
        // Create a pool mechanism for the threads
        // Executor service maintains 10 threads and rest is maintained internally in a queue
        //ExecutorService es = Executors.newFixedThreadPool(10);
        boolean finished = false;
        for (HiveColumn column : columns) {
            System.out.println("Get guid for: " + column.getColumnName());
            i = i + 1;
            JSONObject o = null;
            //String table = tables.get(column.getTableName());
            JSONArray entities = null;
            //if ( table == null )
            //{
            //tables.put(column.getTableName(),column.getTableName());
            String table = column.getTableName();
            String key = table + "." + column.getColumnName();
            System.out.println("Get guid request:" + key);
            try {
                tmpArray.put(key, column);
                GetAllColumnGuidParallel R1 = new GetAllColumnGuidParallel(key, tmpArray, atlasApi);
                tmpArray = R1.run();
//					   System.out.println("Current keys are:" + tmpArray.keySet());
                //es.submit(R1);
                //es.execute(new DummyThread("Thread-"+ i));
                //System.out.println("Time limit hit. No new threads will be accepted");
                //Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
                ;
            }
        }
        finished = true;
        //es.shutdown();
		/*
		boolean finished = false;
		 try {
			finished = es.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException ex) {
			logger.catching(ex);
		}
		 */
        //Over write all entries in columns with the new HiveColumn objects
        if (finished) {
            columns.clear();
            for (HiveColumn column : tmpArray.values()) {
                System.out.println("Print the tags now: " + column.getTableName() + "." + column.getColumnName() + column.getTags().toString());
                columns.add(column);
            }
        } else {
            LOGGER.info("Thread execution is not complete");
        }
        return columns;
    }

    public HiveColumn getColumGuid(RestfulHandler atlasApi, HiveColumn column) throws JSONException, URISyntaxException {
        LOGGER.info("Get Column Guid for : " + column.getSchemaName() + "." + column.getTableName() + "." + column.getColumnName());
        JSONObject o = new JSONObject(atlasApi.getColumGuid(column));

        if (o.has("entities")) {
            JSONArray entities = o.getJSONArray("entities");
            for (int i = 0; i < entities.length(); i++) {
                JSONObject object = entities.getJSONObject(i);
                String guid = object.getString("guid");
                String status = object.getString("status");
                LOGGER.info(object.getString("guid"));
                LOGGER.info(object.getString("status"));
                if (status != null && status.equals("ACTIVE"))
                    column.setGuid(guid);
            }
        } else {
            LOGGER.info("entities is empty!!!");
        }
        return column;
    }

    public void compareAndTag(RestfulHandler atlasApi, HashMap<String, HiveSchema> schemas, Vector<HiveColumn> excelColumns) throws JSONException, URISyntaxException {
        /*
         * columnsToTag and columnsToRemoveTag are array of column to tag and remove the tags respectively
         * The setTags of columnsToTag  method are used to store the tags to be added . The setTags of columnsToRemoveTag are used to store the tags to be removed
         * I know this is not a good design. But no harm, am just avoiding over engineering.
         */
        Vector<HiveColumn> columnsToTag = new Vector<HiveColumn>();
        Vector<HiveColumn> columnsToRemoveTag = new Vector<HiveColumn>();


        for (HiveColumn col : excelColumns) {
            HiveColumn set_column = null;
            HiveColumn remove_column = null;
            if (schemas.containsKey(col.getSchemaName())) {
                HiveSchema tempSchema = schemas.get(col.getSchemaName());
                HashMap<String, HiveTable> tempTables = tempSchema.getTables();

                if (tempTables.containsKey(col.getTableName())) {
                    HiveTable tempTable = tempTables.get(col.getTableName());
                    HashMap<String, HiveColumn> tempColumns = tempTable.getColumns();

                    if (tempColumns.containsKey(col.getColumnName())) {
                        HiveColumn tempColumn = tempColumns.get(col.getColumnName());
                        Vector<String> tempTags = tempColumn.getTags();
                        Vector<String> tags = col.getTags();
                        Vector<String> setTags = new Vector<String>();
                        Vector<String> removeTags = new Vector<String>();
                        //find any tags to be added
                        for (String tag : tags) {

                            if (tempTags.contains(tag)) {
                                LOGGER.info("Column : " + col.getSchemaName() + "." + col.getTableName() + "." + col.getColumnName()
                                        + " already exists for tag[" + tag + "] - Not to add ");
                            } else {
                                setTags.add(tag);
                            }
                        }

                        // find  any tags to be removed
                        for (String tag : tempTags) {
                            if (tags.contains(tag)) {
                                LOGGER.info("Column : " + col.getSchemaName() + "." + col.getTableName() + "." + col.getColumnName()
                                        + " already exists for tag[" + tag + "] Not to remove");
                            } else {
                                removeTags.add(tag);
                            }
                        }

                        if (!setTags.isEmpty()) {
                            set_column = col.clone();
                            set_column.setTags(setTags);
                            setColumGuid(schemas, set_column);
                        }
                        if (!removeTags.isEmpty()) {
                            remove_column = col.clone();
                            remove_column.setTags(removeTags);
                            setColumGuid(schemas, remove_column);
                        }
                    }
//					else
//					{
//						set_column = getColumGuid(atlasApi, col);
//					}
                }
//				else
//				{
//					set_column = getColumGuid(atlasApi, col);
//				}
            }
//			else
//			{
//				set_column = getColumGuid(atlasApi, col);
//			}

            if (null != set_column && set_column.getGuid() != null) {
                columnsToTag.add(set_column);
            }
            if (null != remove_column && remove_column.getGuid() != null) {
                columnsToRemoveTag.add(remove_column);
            }
        }

        if (!columnsToTag.isEmpty()) {
            LOGGER.info("Start Tagging");
            setColumnsTag(atlasApi, columnsToTag);
            //CSVHandler.outPutCSV("./tagging_add_byDD.csv", columnsToTag);
            LOGGER.info("Tagging is completed!");
        } else {
            LOGGER.info("No tags to be added");
        }

        if (!columnsToRemoveTag.isEmpty()) {
            LOGGER.info("Remove Tagging");
            removeColumnsTag(atlasApi, columnsToRemoveTag);
            //CSVHandler.outPutCSV("./tagging_remove_byDD.csv", columnsToRemoveTag);
            //logger.info("Tagging is completed!");
        } else {
            LOGGER.info("No tags to be removed");
        }

    }

    /**
     * Skip the tagging process if the column have already got some tags.
     * <p>
     * Cautious, this method is only for Curation Layer tagging, of which curated columns should inherit
     * tags from its ascendants and should not be tagged if so.
     * Only those columns which are produced from scratch in curation can be tagged.
     * It's combined with updatecuratetag option.
     *
     * @param atlasApi
     * @param schemas
     * @param excelColumns
     * @return
     * @author Tao
     * @date
     */
    public void compareAndTagOnlyNecessary(RestfulHandler atlasApi, HashMap<String, HiveSchema> schemas, Vector<HiveColumn> excelColumns) throws URISyntaxException {
        /*
         * columnsToTag and columnsToRemoveTag are array of column to tag and remove the tags respectively
         * The setTags of columnsToTag  method are used to store the tags to be added . The setTags of columnsToRemoveTag are used to store the tags to be removed
         * I know this is not a good design. But no harm, am just avoiding over engineering.
         */
        Vector<HiveColumn> columnsToTag = new Vector<HiveColumn>();
        Vector<HiveColumn> columnsToRemoveTag = new Vector<HiveColumn>();
        boolean skipFlag = false;

        for (HiveColumn col : excelColumns) {
            HiveColumn set_column = null;
            HiveColumn remove_column = null;
            if (schemas.containsKey(col.getSchemaName())) {
                HiveSchema tempSchema = schemas.get(col.getSchemaName());
                HashMap<String, HiveTable> tempTables = tempSchema.getTables();

                if (tempTables.containsKey(col.getTableName())) {
                    HiveTable tempTable = tempTables.get(col.getTableName());
                    HashMap<String, HiveColumn> tempColumns = tempTable.getColumns();

                    if (tempColumns.containsKey(col.getColumnName())) {
                        HiveColumn tempColumn = tempColumns.get(col.getColumnName());
                        Vector<String> tempTags = tempColumn.getTags();
                        // Skip the rest if the column have already got tags
                        if (!tempTags.isEmpty()) {
                            LOGGER.info(tempSchema.getSchemaName() + "." + tempTable.getTableName() + "." +
                                    tempColumn.getColumnName() + " has already been tagged： " + Joiner.on(", ").join(tempTags));
                            continue;
                        }
                        Vector<String> tags = col.getTags();
                        Vector<String> setTags = new Vector<String>();
                        Vector<String> removeTags = new Vector<String>();
                        //find any tags to be added
                        for (String tag : tags) {

                            if (tempTags.contains(tag)) {
                                LOGGER.info("Column : " + col.getSchemaName() + "." + col.getTableName() + "." + col.getColumnName()
                                        + " already exists for tag[" + tag + "] - Not to add ");
                            } else {
                                setTags.add(tag);
                            }
                        }

                        // find  any tags to be removed
                        for (String tag : tempTags) {
                            if (tags.contains(tag)) {
                                LOGGER.info("Column : " + col.getSchemaName() + "." + col.getTableName() + "." + col.getColumnName()
                                        + " already exists for tag[" + tag + "] Not to remove");
                            } else {
                                removeTags.add(tag);
                            }
                        }

                        if (!setTags.isEmpty()) {
                            set_column = col.clone();
                            set_column.setTags(setTags);
                            setColumGuid(schemas, set_column);
                        }
                        if (!removeTags.isEmpty()) {
                            remove_column = col.clone();
                            remove_column.setTags(removeTags);
                            setColumGuid(schemas, remove_column);
                        }
                    }
                }
            }

            if (null != set_column && set_column.getGuid() != null) {
                columnsToTag.add(set_column);
            }
            if (null != remove_column && remove_column.getGuid() != null) {
                columnsToRemoveTag.add(remove_column);
            }
        }

        if (!columnsToTag.isEmpty()) {
            LOGGER.info("Start Tagging");
            setColumnsTag(atlasApi, columnsToTag);
            //CSVHandler.outPutCSV("./tagging_add_byDD.csv", columnsToTag);
            LOGGER.info("Tagging is completed!");
        } else {
            LOGGER.info("No tags to be added");
        }

        if (!columnsToRemoveTag.isEmpty()) {
            LOGGER.info("Remove Tagging");
            removeColumnsTag(atlasApi, columnsToRemoveTag);
            //CSVHandler.outPutCSV("./tagging_remove_byDD.csv", columnsToRemoveTag);
            //logger.info("Tagging is completed!");
        } else {
            LOGGER.info("No tags to be removed");
        }

    }

    private void setColumGuid(HashMap<String, HiveSchema> schemas, HiveColumn col) {
        HiveSchema tempSchema = schemas.get(col.getSchemaName());
        HiveTable tempTable = tempSchema.getTables().get(col.getTableName());
        HiveColumn tempCol = tempTable.getColumns().get(col.getColumnName());
        col.setGuid(tempCol.getGuid());
    }

    public void setColumnsTag(RestfulHandler atlasApi, Vector<HiveColumn> columns) throws URISyntaxException {
        HashMap<String, Vector<HiveColumn>> tagCollection = convertToTagColMap(columns);
        Iterator<String> itr = tagCollection.keySet().iterator();
        while (itr.hasNext()) {
            String tag = itr.next();
            atlasApi.setTagByColumnMultipleGuid(tagCollection.get(tag), tag);
        }
        //CSVHandler.outPutCSV("./tagging_output.csv", columns);
    }

    private HashMap<String, Vector<HiveColumn>> convertToTagColMap(Vector<HiveColumn> columns) {
        HashMap<String, Vector<HiveColumn>> tagCollection = new HashMap<String, Vector<HiveColumn>>();
        for (HiveColumn col : columns) {
            for (String tag : col.getTags()) {
                System.out.println("Set Tag[" + tag + "] for Column : " + col.getSchemaName() + "." + col.getTableName() + "." + col.getColumnName() + " ,guid : " + col.getGuid());
//				atlasApi.setTagByColumnMultipleGuid(col.getGuid(), tag);
                if (tagCollection.containsKey(tag)) {
                    if (!tag.isEmpty())
                        tagCollection.get(tag).addElement(col);
                } else {
                    if (!tag.isEmpty()) {
                        Vector<HiveColumn> v = new Vector<HiveColumn>();
                        v.add(col);
                        tagCollection.put(tag, v);
                    }
                }
            }
        }
        return tagCollection;
    }

    public void removeColumnsTag(RestfulHandler atlasApi, Vector<HiveColumn> columns) throws URISyntaxException {
        for (HiveColumn col : columns) {
            for (String tag : col.getTags()) {
                LOGGER.info("Remove  Tag[" + tag + "] for Column : " + col.getSchemaName() + "." + col.getTableName() + "." + col.getColumnName() + " ,guid : " + col.getGuid());
                atlasApi.delTagByColumnGuid(col.getGuid(), tag);
            }
        }

        //CSVHandler.outPutCSV("./tagging_output.csv", columns);
    }

    public void delColumnsTagBySchemaTag(RestfulHandler atlasApi, Vector<HiveColumn> columns, String tag, String schema) throws URISyntaxException {
        Vector<HiveColumn> del_columns = new Vector<HiveColumn>();

        for (HiveColumn col : columns) {
            if (col.getSchemaName().equals(schema)) {
                atlasApi.delTagByColumnGuid(col.getGuid(), tag);
                del_columns.add(col);
            }
        }

        if (del_columns != null && !del_columns.isEmpty()) {
            CSVHandler.outPutCSV("./tagging_delete_" + tag + "_" + schema + ".csv", del_columns);
        }
    }

    public void cleanupDeletedColumGuidByTags(RestfulHandler atlasApi, HashMap<String, Tag> tags) throws JSONException, URISyntaxException {
        Vector<HiveColumn> deleted_columns = new Vector<HiveColumn>();

        Iterator<String> tagIter = tags.keySet().iterator();
        while (tagIter.hasNext()) {
            Tag tag = tags.get(tagIter.next());
            int limit = 10000; //Max 10000
            int offset = 0;
            boolean callAtlas = true;
            while (callAtlas) {
                JSONObject o = new JSONObject(atlasApi.getHiveColumnByTag(Integer.toString(limit), tag.getTagName(), Integer.toString(offset)));

                if (o.has("entities")) {
                    JSONArray entities = o.getJSONArray("entities");
                    LOGGER.info("No. of entities: " + entities.length());
                    if (entities.length() >= limit) {
                        offset += limit;
                    } else {
                        callAtlas = false;
                    }

                    for (int i = 0; i < entities.length(); i++) {
                        HiveColumn col = new HiveColumn();
                        JSONObject object = entities.getJSONObject(i);

                        if (object.has("typeName")) {
                            LOGGER.info("Type Name: " + object.getString("typeName"));
                            if (object.getString("typeName").equals("hive_column")) {
                                if (object.has("status")) {
                                    LOGGER.info("Status: " + object.getString("status"));
                                    if (object.getString("status").equals("DELETED")) {
                                        String columnName = object.getJSONObject("attributes").getString("name");
                                        col.setColumnName(columnName);
                                        LOGGER.info("Column Name: " + columnName);
                                        String guid = object.getString("guid");
                                        col.setGuid(guid);
                                        LOGGER.info("Guid: " + guid);
                                        String qualifiedName = object.getJSONObject("attributes").getString("qualifiedName");
                                        LOGGER.info("Qualified Name: " + qualifiedName);
                                        String schemaName = qualifiedName.split("\\.", 3)[0];
                                        col.setSchemaName(schemaName);
                                        LOGGER.info("Schema: " + schemaName);
                                        String tableName = qualifiedName.split("\\.", 3)[1];
                                        col.setTableName(tableName);
                                        LOGGER.info("Table: " + tableName);

                                        Vector<String> strTags = new Vector<String>();
                                        if (object.has("classificationNames")) {
                                            JSONArray classificationNames = object.getJSONArray("classificationNames");
                                            for (int j = 0; j < classificationNames.length(); j++) {
                                                String tagName = classificationNames.getString(j);
                                                strTags.add(tagName);
                                                LOGGER.info(tagName);
                                                atlasApi.delTagByColumnGuid(col.getGuid(), tagName);
                                                LOGGER.info(columnName + "tag[" + tagName + "] is deleted");
                                            }
                                        }
                                        col.setTags(strTags);
                                        deleted_columns.add(col);
                                    }
                                }
                            }
                        }
                    }
                } else {
                    callAtlas = false;
                    LOGGER.info("entities is empty!!!");
                }
            }
        }


        if (deleted_columns != null && !deleted_columns.isEmpty()) {
            CSVHandler.outPutCSV("./tagging_cleanup.csv", deleted_columns);
        }
    }


}
