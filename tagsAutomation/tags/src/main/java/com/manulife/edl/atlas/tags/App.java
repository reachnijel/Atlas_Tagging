package com.manulife.edl.atlas.tags;

import com.google.common.base.Strings;
import com.manulife.edl.atlas.tags.tagObject.HiveColumn;
import com.manulife.edl.atlas.tags.tagObject.HiveSchema;
import com.manulife.edl.atlas.tags.tagObject.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Vector;

public class App {
    private static final Logger LOGGER = LogManager.getLogger();
    private static String[] HK_CATEGORIES = {"LFE", "WLT", "CMB"};

    public static void main(String[] args) {
        if (args.length < 8) {
            LOGGER.error("Usage : tagging {report} {cluster} {knoxHost} {knoxPort} {atlasHost} {atlasPort} {user} {password} ");
            LOGGER.error("Example : tagging report p01eaedl azalvedlmgtdp01.p01eaedl.manulife.com 8443 azalvedlmstdp07.p01eaedl.manulife.com 21443 EDL_User 123456 ");
            LOGGER.error("or");
            LOGGER.error("Usage : tagging {updatetag} {cluster} {knoxHost} {knoxPort} {atlasHost} {atlasPort} {user} {password} {country} {DD URL} " +
                    "{dataElementSheetSeq} {Hive Schema}");
            LOGGER.error("Example : tagging updatetag p01eaedl azalvedlmgtdp01.p01eaedl.manulife.com 8443 azalvedlmstdp07.p01eaedl.manulife.com 21443 EDL_User " +
                    "123456 VN EDL_DataDict_VN_AMS.xlsx 3 vn_published_ams_db");
            LOGGER.error("or");
            LOGGER.error("Usage (For HK Only): tagging {updatetag} {cluster} {knoxHost} {knoxPort} {atlasHost} {atlasPort} {user} {password} {country} {DD URL} " +
                    "{dataElementSheetSeq} {Hive Schema} {HK_Category (LFE, WLT or CMB)}");
            LOGGER.error("Example (For HK Only): tagging updatetag p01eaedl azalvedlmgtdp01.p01eaedl.manulife.com 8443 azalvedlmstdp07.p01eaedl.manulife.com 21443 EDL_User " +
                    "123456 HK EDL_DataDict_HK_AMS.xlsx 3 vn_published_ams_db CMB");
            LOGGER.error("or");
//            logger.error("Usage : tagging {add} {cluster} {knoxHost} {knoxPort} {atlasHost} {atlasPort} {user} {password} {country} {DD URL} ");
//            logger.error("Example : tagging add p01eaedl azalvedlmgtdp01.p01eaedl.manulife.com 8443 azalvedlmstdp07.p01eaedl.manulife.com 21443 EDL_User 123456 VN VM_Master.xlsx ");
//            logger.error("or");
//            logger.error("Usage : tagging {addDD} {cluster} {knoxHost} {knoxPort} {atlasHost} {atlasPort} {user} {password} {country} {DD URL} ");
//            logger.error("Example : tagging addDD p01eaedl azalvedlmgtdp01.p01eaedl.manulife.com 8443 azalvedlmstdp07.p01eaedl.manulife.com 21443 EDL_User 123456 VN EDL_DataDict_VN_AMS.xlsx ");
//            logger.error("or");
            LOGGER.error("Usage : tagging {deltag} {cluster} {knoxHost} {knoxPort} {atlasHost} {atlasPort} {user} {password} {tag name} {schema} ");
            LOGGER.error("Example : tagging deltag p01eaedl azalvedlmgtdp01.p01eaedl.manulife.com 8443 azalvedlmstdp07.p01eaedl.manulife.com 21443 EDL_User 123456 CAS vn_published_cas_db ");
            LOGGER.error("or");
            LOGGER.error("Usage : tagging {cleanup} {cluster} {knoxHost} {knoxPort} {atlasHost} {atlasPort} {user} {password} ");
            LOGGER.error("Example : tagging cleanup p01eaedl azalvedlmgtdp01.p01eaedl.manulife.com 8443 azalvedlmstdp07.p01eaedl.manulife.com 21443 EDL_User 123456 ");
            LOGGER.error("or");
            LOGGER.error("Usage : tagging {curateupdatetag} {cluster} {knoxHost} {knoxPort} {atlasHost} {atlasPort} {user} {password} {country} {DD URL}");
            LOGGER.error("Example : tagging curateupdatetag p01eaedl azalvedlmgtdp01.p01eaedl.manulife.com 8443 azalvedlmstdp07.p01eaedl.manulife.com 21443 EDL_User 123456 VN https://git.ap.manulife.com/projects/BDP/repos/vn/raw/dd/CAS/EDL_DataDict_VN_CAS.xlsx?at=refs%2Fheads%2Fmaster ");
//            LOGGER.error("or");
//            logger.error("Usage : tagging {syncup} {cluster} {knoxHost} {knoxPort} {atlasHost} {atlasPort} {user} {password} {country} {DD URL}");
//            logger.error("Example : tagging syncup p01eaedl azalvedlmgtdp01.p01eaedl.manulife.com 8443 azalvedlmstdp07.p01eaedl.manulife.com 21443 EDL_User 123456 VN https://git.ap.manulife.com/projects/BDP/repos/vn/raw/dd/CAS/EDL_DataDict_VN_CAS.xlsx?at=refs%2Fheads%2Fmaster ");
            System.exit(1);
        }

        String option = args[0];
        if (Strings.isNullOrEmpty(option) || (!option.equals("report")
//                && !option.equals("addtag")
                && !option.equals("updatetag")
                && !option.equals("deltag")
                && !option.equals("cleanup")
                && !option.equals("curateupdatetag")
        )) {
//            logger.error("Usage : The 1st arguemnet should be \"report\" or \"addtag\" or \"updatetag\" or \"deltag\" or \"cleanup\" or \"curateupdatetag\"");
            LOGGER.error("Usage : The 1st arguemnet should be \"report\" or \"updatetag\" or \"deltag\" or \"cleanup\" or \"curateupdatetag\"");
            System.exit(1);
        }

        String cluster = args[1];
        String knoxHost = args[2];
        String strKnoxPort = args[3];
        String atlasHost = args[4];
        String strAtlasPort = args[5];
        int knoxPort = 0;
        int atlasPort = 0;
        try {
            knoxPort = Integer.parseInt(strKnoxPort);
            atlasPort = Integer.parseInt(strAtlasPort);
        } catch (Exception e) {
            LOGGER.error("Usage : Port must be integer");
            System.exit(1);
        }
        String encodedCred = args[6];
        byte[] decodedArray = Base64.getDecoder().decode(encodedCred);
        String[] credArr = new String(decodedArray).split(":");
        String user = credArr[0];
        String pass = credArr[1];

        try {
            Vector<HiveColumn> columns = new Vector<HiveColumn>();
//            RestfulHandler atlasApi = new RestfulHandler(cluster, atlasHost, atlasPort, user, pass);
            RestfulHandler atlasApi = new RestfulHandler(cluster, knoxHost, knoxPort, atlasHost, atlasPort, user, pass);
            //Fetch the 'hadoop-jwt' cookie or token first in order to complete the process
            atlasApi.setupKnoxCookie();

            if (option.equals("report")) {
                TagHandler tagHandler = new TagHandler();
                ExcelHandler excel = new ExcelHandler("ALL");

                String tagsJson = atlasApi.getAllTag();
                HashMap<String, Tag> tags = tagHandler.readTags(tagsJson);
                HashMap<String, HiveSchema> schemas = tagHandler.getAllTaggedSchema(atlasApi, tags);
                excel.printTagReportBySchema(schemas);

                LOGGER.info("Report is completed");
            }
            // Use this method to manually tag a particular table
//	    	else if ( options.equals("addtag") )
//	    	{
//	        	String country = args[8].toUpperCase();
//	        	String excelFile = args[9];
//
//		    	TagHandler tagHandler = new TagHandler();
//		    	logger.info("Country -->" + country);
//
//	        	ExcelHandler excel = new ExcelHandler(country);
//	        	// gets the list of all columns from country data dictionary
//	        	columns = excel.readExcelForColumns(excelFile);
//	        	logger.info(columns.toString());
//	        	for (HiveColumn col : columns) {
//	        		logger.info("ColumnName: " +col.getColumnName());
//	        		Thread.sleep(100);
//	        	}
//	        	logger.info("HiveColumn object creation completed");
//	        	CSVHandler.outPutCSV("./tagging_excel_output.csv", columns);
//
//	        	// The next step will get the existing details about the column from Atlas
//		    	columns = tagHandler.getAllColumGuid(atlasApi, columns);
//
//		    	tagHandler.setColumnsTag(atlasApi, columns);
//	    	}
            else if (option.equals("updatetag")) {
                String country = args[7];
                String ddUrl = args[8];

                DdDownloader downloader = new DdDownloader();
                String dd = downloader.bearerDownload(ddUrl);

                if (Strings.isNullOrEmpty(dd)) {
                    LOGGER.error("The DD download failed, please check it out in the log");
                    System.exit(1);
                }

                // lines below is used for tag automation
                int dataElementSheetSeq = -1;
                // skipping the data element sheet sequence assigning
                try {
                    dataElementSheetSeq = Integer.parseInt(args[9]);
                } catch (NumberFormatException e) {
                    LOGGER.warn("Usage : Data Element sheet sequence must be integer");
                }

                String schema;
                String hKCategory;
                ExcelHandler excel;
                if (-1 == dataElementSheetSeq) {
                    schema = args[9];

                    if (country.equalsIgnoreCase("HK")) {
                        hKCategory = args[10];
                        // have a check on the HK category key word, currently it should be LEF, WLT or CMB
                        if (!Arrays.asList(HK_CATEGORIES).contains(hKCategory)) {
                            LOGGER.error("HK Data Category should be LFE, WLT or CMB");
                            System.exit(1);
                        }
                        excel = new ExcelHandler(country, schema, hKCategory);
                    } else {
                        excel = new ExcelHandler(country, schema);
                    }
                } else {
                    schema = args[10];

                    if (country.equalsIgnoreCase("HK")) {
                        hKCategory = args[11];
                        // have a check on the HK category key word, currently it should be LEF, WLT or CMB
                        if (!Arrays.asList(HK_CATEGORIES).contains(hKCategory)) {
                            LOGGER.error("HK Data Category should be LFE, WLT or CMB");
                            System.exit(1);
                        }
                        excel = new ExcelHandler(country, dataElementSheetSeq, schema, hKCategory);
                    } else {
                        excel = new ExcelHandler(country, dataElementSheetSeq, schema);
                    }
                }

                TagHandler tagHandler = new TagHandler();
                // Read all columns from Excel
                Vector<HiveColumn> excelColumns = excel.readExcelForColumns(dd);
//	        	CSVHandler.outPutCSV("./tagging_excel_output.csv", excelColumns);
                // Read all tagged from Atlas
                HashMap<String, HiveSchema> schemas = tagHandler.getAllTagsSchemaWise(atlasApi, excelColumns);
                // Compare the tags in DD and Atlas and apply only the difference in tagging from DD
                tagHandler.compareAndTag(atlasApi, schemas, excelColumns);
            } else if (option.equals("deltag")) {
                String tag = args[8];
                String schema = args[9];
                TagHandler tagHandler = new TagHandler();
                columns = tagHandler.getColumGuidByTagName(atlasApi, tag);
                tagHandler.delColumnsTagBySchemaTag(atlasApi, columns, tag, schema);
                LOGGER.info("Delete Tag " + tag + " at " + schema + " is completed");
            } else if (option.equals("cleanup")) {
                TagHandler tagHandler = new TagHandler();
                String tagsJson = atlasApi.getAllTag();
                HashMap<String, Tag> tags = tagHandler.readTags(tagsJson);
                tagHandler.cleanupDeletedColumGuidByTags(atlasApi, tags);
                LOGGER.info("Clean up deleted Tag is completed");
            } else if (option.equals("curateupdatetag")) {
                String country = args[7];
                String ddUrl = args[8];

                DdDownloader downloader = new DdDownloader();
                String dd = downloader.bearerDownload(ddUrl);

                if (Strings.isNullOrEmpty(dd)) {
                    LOGGER.error("The DD download failed, please check it out in the log");
                    System.exit(1);
                }

                // lines below is used for tag automation
                int dataElementSheetSeq = -1;
                // skipping the data element sheet sequence assigning
                try {
                    dataElementSheetSeq = Integer.parseInt(args[9]);
                } catch (NumberFormatException e) {
                    LOGGER.warn("Usage : Data Element sheet sequence must be integer");
                }

                String schema;
                String hKCategory;
                ExcelHandler excel;
                if (-1 == dataElementSheetSeq) {
                    schema = args[9];

                    if (country.equalsIgnoreCase("HK")) {
                        hKCategory = args[10];
                        // have a check on the HK category key word, currently it should be LEF, WLT or CMB
                        if (!Arrays.asList(HK_CATEGORIES).contains(hKCategory)) {
                            LOGGER.error("HK Data Category should be LFE, WLT or CMB");
                            System.exit(1);
                        }
                        excel = new ExcelHandler(country, schema, hKCategory);
                    } else {
                        excel = new ExcelHandler(country, schema);
                    }
                } else {
                    schema = args[10];

                    if (country.equalsIgnoreCase("HK")) {
                        hKCategory = args[11];
                        // have a check on the HK category key word, currently it should be LEF, WLT or CMB
                        if (!Arrays.asList(HK_CATEGORIES).contains(hKCategory)) {
                            LOGGER.error("HK Data Category should be LFE, WLT or CMB");
                            System.exit(1);
                        }
                        excel = new ExcelHandler(country, dataElementSheetSeq, schema, hKCategory);
                    } else {
                        excel = new ExcelHandler(country, dataElementSheetSeq, schema);
                    }
                }

                TagHandler tagHandler = new TagHandler();
                // Read all columns from Excel
                Vector<HiveColumn> excelColumns = excel.readExcelForColumns(dd);
//	        	CSVHandler.outPutCSV("./tagging_excel_output.csv", excelColumns);
                // Read all tagged from Atlas
                HashMap<String, HiveSchema> schemas = tagHandler.getAllTagsSchemaWise(atlasApi, excelColumns);
                // Compare the tags in DD and Atlas and apply only the difference in tagging from DD
                tagHandler.compareAndTagOnlyNecessary(atlasApi, schemas, excelColumns);
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            LOGGER.error("Please have a check on the parameters, the sequence or number of them should be wrong!");
        } catch (Exception e) {
            LOGGER.catching(e);
        }
        LOGGER.info("Tagging Completed !!!");
    }
}
