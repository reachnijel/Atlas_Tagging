package com.manulife.edl.atlas.tags;

import com.google.common.base.Strings;
import com.manulife.edl.atlas.tags.tagObject.HiveColumn;
import com.manulife.edl.atlas.tags.tagObject.HiveSchema;
import com.manulife.edl.atlas.tags.tagObject.HiveTable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

public class ExcelHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyyMMddHHmmss");

    private Properties LOOKUPPROP;
    private Properties INPUTFILECONFIGPROP;
    private XSSFWorkbook myWorkBook;
    private String countryTag;
    // the 3 fields below are for tagging automation functionality, set them up to avoid editing properties files
    private int dataElementSheetSeq = -1;
    private String hiveSchema;
    private String hKCategory;

    {
        this.LOOKUPPROP = new Properties();
        this.INPUTFILECONFIGPROP = new Properties();

        // Read properties file
        InputStream input = null;
        try {
            // Schema / tag lookup
            String path = System.getProperty("user.dir");
            System.out.println("CWD :" + path);
            input = new FileInputStream("lookup.properties");
            this.LOOKUPPROP.load(input);
            // Input file config
            input = new FileInputStream("inputFileConfig.properties");
            this.INPUTFILECONFIGPROP.load(input);
        } catch (IOException ex) {
            LOGGER.catching(ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    LOGGER.catching(e);
                }
            }
        }
    }

    public ExcelHandler(String countryTag) {
        this.countryTag = countryTag;
    }

    public ExcelHandler(String countryTag, String hiveSchema) {
        this.countryTag = countryTag;
        this.hiveSchema = hiveSchema.toLowerCase();
    }

    public ExcelHandler(String countryTag, int dataElementSheetSeq, String hiveSchema) {
        this.countryTag = countryTag;
        this.dataElementSheetSeq = dataElementSheetSeq;
        this.hiveSchema = hiveSchema.toLowerCase();
    }

    public ExcelHandler(String countryTag, String hiveSchema, String hKCategory) {
        this.countryTag = countryTag;
        this.hiveSchema = hiveSchema.toLowerCase();
        this.hKCategory = hKCategory;
    }

    public ExcelHandler(String countryTag, int dataElementSheetSeq, String hiveSchema, String hKCategory) {
        this.countryTag = countryTag;
        this.dataElementSheetSeq = dataElementSheetSeq;
        this.hiveSchema = hiveSchema.toLowerCase();
        this.hKCategory = hKCategory;
    }

    // read the excel for finding the columns mentioned
    public Vector<HiveColumn> readExcelForColumns(String fileName) throws Exception {
        Vector<HiveColumn> columns = new Vector<HiveColumn>();
        System.out.println("Data Dictionary being read -" + fileName);

        int rowCnt = 0;
        if (myWorkBook == null)
            myWorkBook = readExcel(fileName);
        XSSFSheet mySheet = null;
        try {
            if (dataElementSheetSeq != -1) {
                mySheet = myWorkBook.getSheetAt(dataElementSheetSeq);
            } else {
                dataElementSheetSeq = getInputFileConfig("data_elements_sheet");
                mySheet = myWorkBook.getSheetAt(dataElementSheetSeq); // Get iterator to all the rows in current sheet
            }
        } catch (Exception e) {
            LOGGER.error("Unable to read the excel sheet no " + dataElementSheetSeq + " please verify the property data_elements_sheet in inputFileConfig.properties");
            System.exit(0);
        }

        Iterator<Row> rowIterator = mySheet.iterator(); // Traversing over each row of XLSX file
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next(); // For each row, iterate through each columns

            if (rowCnt > 2) {
                HiveColumn col = new HiveColumn();
                Vector<String> tags = new Vector<String>();
                if (!Strings.isNullOrEmpty(hiveSchema)) {
                    col.setSchemaName(hiveSchema);
                } else {
                    String schemaName = schemaLookup(row.getCell(getInputFileConfig("source_system_col")).toString());
                    col.setSchemaName(schemaName);
                }

                LOGGER.debug("Schema:" + col.getSchemaName());
                // this point is to skip the empty rows after the real data content
                Cell tableName = row.getCell(getInputFileConfig("source_table_col"));
                // if table name is null or empty, it breaks here
                if (null == tableName) break;
                col.setTableName(tableName.toString().toLowerCase().trim());
                if (!Strings.isNullOrEmpty(col.getTableName())) {
                    col.setColumnName(colLookup(row.getCell(getInputFileConfig("element_name_col")).toString().toLowerCase().trim()));
                    try {
                        String category = row.getCell(getInputFileConfig("data_catgory_col")).getStringCellValue().trim();
                        String classification = row.getCell(getInputFileConfig("data_class_col")).getStringCellValue().trim();

                        // capitalized all tag mapping in the lookup.property to avoid tag look-up issue
                        String tagValue = (category + "-" + classification).toUpperCase();
                        String mappedtag = tagMapLookup(tagValue);
                        String colTag = null;
                        if (Strings.isNullOrEmpty(mappedtag))
                            LOGGER.error("----- Tag Value : " + tagValue + "could not be identified ");

                        if (!Strings.isNullOrEmpty(mappedtag) && !mappedtag.equalsIgnoreCase("[CC]")) {
                            if (Strings.isNullOrEmpty(hKCategory)) {
                                colTag = countryTag + "_" + mappedtag;
                            } else {
                                colTag = countryTag + "_" + hKCategory + "_" + mappedtag;
                            }
                        } else if (!Strings.isNullOrEmpty(mappedtag) && mappedtag.equalsIgnoreCase("[CC]")) {
                            colTag = mappedtag.replace("[CC]", countryTag);
                        } else {
                            colTag = countryTag;
                        }

                        tags.addElement(colTag);
                        LOGGER.debug("Current row: " + rowCnt + " > " + row.getCell(getInputFileConfig("source_system_col")) + "  Category > " + category + "  Classification > " + classification + " | ColumnTag:" + colTag);
                    } catch (Exception e) {
                        LOGGER.info(col.getTableName() + "." + col.getColumnName());
                        LOGGER.catching(e);
                        throw e;
                    }

                    col.setTags(tags);
                    System.out.println("Tags are: " + tags.toString());

                    if (col.getTableName().trim().length() > 0) {
                        LOGGER.info(col.getTableName() + "." + col.getColumnName());
                        columns.add(col);
                    }
                } else {
                    LOGGER.info("Table is null!");
                    LOGGER.info("Check Data Dictionary...Halting the Program after line: " + rowCnt + "!!");
                    LOGGER.info("If the line count is too small then the Sequence of Data Element Sheet passed in may be wrong!!");
                    break;
                }
            }

            rowCnt++;
        }

        return columns;
    }

    public XSSFWorkbook readExcel(String fileName) {
        File myFile = new File(fileName);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(myFile);
            LOGGER.info("Data Dictionary read");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        // Finds the workbook instance for XLSX file
        XSSFWorkbook myWorkBook = null;
        try {
            myWorkBook = new XSSFWorkbook(fis);
        } catch (IOException e) {
            e.printStackTrace();
        } // Return first sheet from the XLSX workbook
        LOGGER.info("Data Dictionary reading completed");
        return myWorkBook;
    }

    public Vector<HiveTable> readExcelForTables(String fileName) throws Exception {
        Vector<HiveTable> tables = new Vector<HiveTable>(100);

        int rowCnt = 0;
        if (myWorkBook == null)
            myWorkBook = readExcel(fileName);
        XSSFSheet mySheet = myWorkBook.getSheetAt(4); // Get iterator to all the rows in current sheet
        Iterator<Row> rowIterator = mySheet.iterator(); // Traversing over each row of XLSX file
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next(); // For each row, iterate through each columns

            if (rowCnt >= 3) {
                Vector<String> tags = new Vector<String>();
                HiveTable tbl = new HiveTable();

                tbl.setSchemaName(schemaLookup(row.getCell(0).toString()));
                tbl.setTableName(row.getCell(2).toString().toLowerCase().trim());
                tags.addElement(tagLookup(countryTag));
                tags.addElement(tagLookup(row.getCell(0).toString()));
                tags.addElement(tagLookup(row.getCell(5).getStringCellValue()));
                tags.addElement(tagLookup(row.getCell(6).getStringCellValue()));
                tbl.setTags(tags);
                if (tbl.getTableName().trim().length() > 0) {
                    LOGGER.info(tbl.getTableName());
                    tables.add(tbl);
                }
            }

            rowCnt++;
            //curl -v -u holger_gov:holger_gov 'http://sandbox.hortonworks.com:21000/api/atlas/v2/search/dsl?limit=25&query=where+qualifiedName+%3D%22default.employee%40Sandbox%22+%2C+columns&typeName=hive_table' -H 'Accept: application/json
        }
        return tables;
    }

    // Print Excel
    public void printTagReportBySchema(HashMap<String, HiveSchema> schemas) {
        try {
            XSSFWorkbook myWorkBook = new XSSFWorkbook();

            Iterator<String> schemaIter = schemas.keySet().iterator();
            while (schemaIter.hasNext()) {
                HiveSchema schema = schemas.get(schemaIter.next());
                String schemaName = schema.getSchemaName();
                XSSFSheet mySheet = generateSchemaSheet(myWorkBook, schemaName);
                int rownum = mySheet.getLastRowNum() + 1;
                LOGGER.info("Print schema: " + schemaName);
                HashMap<String, HiveTable> tables = schema.getTables();
                Vector<String> tableList = schema.getTablesList();
                for (String tableName : tableList) {
                    HiveTable table = tables.get(tableName);
                    LOGGER.info("Print table: " + tableName);
                    HashMap<String, HiveColumn> columns = table.getColumns();
                    Vector<String> columnList = table.getColumnsList();
                    for (String columnName : columnList) {
                        HiveColumn column = columns.get(columnName);
                        String columnGuid = column.getGuid();
                        Vector<String> tags = column.getTags();
                        if (!tags.isEmpty()) {
                            for (String columnTag : tags) {
                                rownum = printRow(mySheet, tableName, columnName, columnGuid, columnTag, rownum);
                            }
                        } else {
                            rownum = printRow(mySheet, tableName, columnName, columnGuid, "", rownum);
                        }
                    }
                }
            }
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            File myFile = new File("tagged_" + SDF.format(timestamp) + ".xlsx");
            LOGGER.info("Strat write Excel - " + "tagged_" + SDF.format(timestamp) + ".xlsx");
            FileOutputStream os = new FileOutputStream(myFile);

            myWorkBook.write(os);
            myWorkBook.close();
            os.flush();
            os.close();
            LOGGER.info("End write Excel");
        } catch (Exception e) {
            LOGGER.catching(e);
        }
    }

    private XSSFSheet generateSchemaSheet(XSSFWorkbook myWorkBook, String schemaName) {
        XSSFSheet mySheet = myWorkBook.createSheet(schemaName);
        // Generate 1st row header
        int rownum = 0;
        Row headerRow = mySheet.createRow(rownum++);
        // From 1st row header
        int headerCellnum = 0;
        Cell headerCell = headerRow.createCell(headerCellnum++);
        headerCell.setCellValue("Table");
        headerCell = headerRow.createCell(headerCellnum++);
        headerCell.setCellValue("Column");
        headerCell = headerRow.createCell(headerCellnum++);
        headerCell.setCellValue("Column Guid");
        headerCell = headerRow.createCell(headerCellnum++);
        headerCell.setCellValue("Column Tag");
        return mySheet;
    }

    private int printRow(XSSFSheet mySheet, String tableName, String columnName, String columnGuid, String columnTag, int rownum) {
        Row row = mySheet.createRow(rownum++);
        LOGGER.info("Print row: " + row.getRowNum());
        int cellnum = 0;
        Cell cell = row.createCell(cellnum++);
        cell.setCellValue(tableName);  //Table
        cell = row.createCell(cellnum++);
        cell.setCellValue(columnName); //Column
        LOGGER.info("Print column: " + columnName);
        cell = row.createCell(cellnum++);
        cell.setCellValue(columnGuid); //Column Guid
        LOGGER.info("Print column guid: " + columnGuid);
        cell = row.createCell(cellnum++);
        cell.setCellValue(columnTag); //Column Tag
        LOGGER.info("Print column tag: " + columnTag);

        return rownum;
    }

    /////////////////////////////////////////////////
    // Get Config and Lookup
    /////////////////////////////////////////////////
    private int getInputFileConfig(String key) {
        return Integer.parseInt(INPUTFILECONFIGPROP.getProperty(key));
    }

    private String schemaLookup(String schema) {
        return LOOKUPPROP.getProperty("schema." + countryTag + "." + schema);
    }

    private String tagLookup(String tag) throws Exception {
        String result = "";

        if (tag != null && tag.trim().length() == 0) {
            LOGGER.info("Tag is Blank.");
        } else {
            result = LOOKUPPROP.getProperty("tag." + tag, tag);
            LOGGER.info("tag:" + tag + " result:" + result);
        }

        return result;
    }

    private String tagMapLookup(String mapkey) throws Exception {
        String result = "";

        if (mapkey != null && mapkey.trim().length() == 0) {
            LOGGER.info("TagMap is Blank.");
        } else {
            result = LOOKUPPROP.getProperty("tagmap." + mapkey);
            LOGGER.info("tag:" + mapkey + " result:" + result);
        }

        return result;
    }

    private String colLookup(String column) throws Exception {
        String result = "";

        if (column != null && column.trim().length() == 0) {
            LOGGER.info("Column is Blank.");
        } else {
            result = LOOKUPPROP.getProperty("column." + column, column);

            LOGGER.info("column:" + column + " result:" + result);
        }

        return result;
    }

}
	
