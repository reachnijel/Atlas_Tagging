package com.manulife.edl.atlas.tags;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Vector;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.manulife.edl.atlas.tags.tagObject.HiveColumn;


public class CSVHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void outPutCSV(String filename, Vector<HiveColumn> columns) {
        FileOutputStream fos = null;
        OutputStreamWriter osw = null;
        try {
            fos = new FileOutputStream(filename);
            //StringBuffer
            osw = new OutputStreamWriter(fos);
            StringBuffer strbuf = null;
            for (HiveColumn column : columns) {
                strbuf = new StringBuffer(512);
                strbuf.append(column.getSchemaName()).append(",");
                strbuf.append(column.getTableName()).append(",");
                strbuf.append(column.getColumnName()).append(",");
                strbuf.append(column.getGuid()).append(",");
                for (String tag : column.getTags()) {
                    strbuf.append(tag).append(",");
                }

                strbuf.append("END\n");
                osw.write(strbuf.toString());
            }
        } catch (Exception e) {
            LOGGER.catching(e);
        } finally {
            try {
                osw.flush();
                osw.close();
                fos.flush();
                fos.close();
            } catch (Exception ex) {
                LOGGER.catching(ex);
            }
        }
        return;
    }
}
