package com.manulife.edl.atlas.tags;

import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
// Review comments
// change key to schema.table.colum to avoid duplication

import com.manulife.edl.atlas.tags.tagObject.HiveColumn;

public class GetAllColumnGuidParallel {
    private Thread t;
    private String colName;
    private ConcurrentHashMap<String, HiveColumn> arrayName;
    private RestfulHandler atlasApi;
    private static final Logger LOGGER = LogManager.getLogger();

    GetAllColumnGuidParallel(String name, ConcurrentHashMap<String, HiveColumn> tmpArray, RestfulHandler atlasApi) {
        colName = name;
        arrayName = tmpArray;
        this.atlasApi = atlasApi;
        LOGGER.info("Creating " + colName);
    }

    public ConcurrentHashMap<String, HiveColumn> run() {
        System.out.println("Running " + colName);
        JSONObject o = null;
        JSONArray entities = null;
        HiveColumn column = arrayName.get(colName);
        String table = column.getTableName();
        Hashtable<String, String> columnHashTbl = new Hashtable<String, String>();
        try {
            HiveColumn val = arrayName.get(colName);
            o = new JSONObject(atlasApi.getColumGuid(column));

            if (o.has("entities")) {
                entities = o.getJSONArray("entities");
                for (int i =
                     0; i < entities.length(); i++) {
                    JSONObject object = entities.getJSONObject(i);
                    JSONObject colObj = object.getJSONObject("attributes");
                    String colname = null;
                    if (!colObj.isNull("name")) {
                        colname = colObj.getString("name");
                    }

                    String guid = object.getString("guid");
                    String status = object.getString("status");
                    LOGGER.debug(object.getString("guid"));
                    LOGGER.debug(object.getJSONObject("attributes"));
                    LOGGER.debug(object.getString("status"));
                    if (status != null && status.equals("ACTIVE"))
                        columnHashTbl.put(table + "." + colname, guid);

                    LOGGER.info("GUID extracted for: " + colName + ", " + arrayName.get(colName));
                    // Removing the key to make space for new API calls
                    //arrayName.remove(colName);
                }
                // Let the thread sleep for a while.
                //Thread.sleep(5);
            } else {
                LOGGER.info("entities is empty!!!");
            }
        } catch (JSONException e) {
            LOGGER.catching(e);
        } catch (URISyntaxException e) {
            LOGGER.catching(e);
        }
        column.setGuid(columnHashTbl.get(column.getTableName() + "." + column.getColumnName()));
        arrayName.put(colName, column);
        System.out.println("Thread " + colName + " exiting.");
        return arrayName;
    }
	   
	   /*
	   public void start () {
	      System.out.println("Starting " +  colName );
	      if (t == null) {
	         t = new Thread (this, colName);
	         t.start ();
	      }
	   }
	   */
}

