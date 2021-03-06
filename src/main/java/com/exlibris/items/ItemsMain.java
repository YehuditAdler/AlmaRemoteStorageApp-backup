package com.exlibris.items;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.VariableField;

import com.exlibris.configuration.ConfigurationHandler;
import com.exlibris.util.FTPUtil;
import com.exlibris.util.XmlUtil;

public class ItemsMain {

    static String mainLocalFolder = "files//items//";

    final static Logger logger = Logger.getLogger(ItemsMain.class);

    public static synchronized void mergeItemsWithSCF(String institution) {
        try {
            JSONObject props = ConfigurationHandler.getInstance().getConfiguration();
            String ftpFolder = props.getJSONObject("ftp_server").getString("main_folder");

            logger.info("Starting Merge Items With SCF For Institution: " + institution);
            logger.debug("empty the local folder");
            File tarGzFolder = new File(mainLocalFolder + "targz//");
            if (tarGzFolder.isDirectory()) {
                FileUtils.cleanDirectory(tarGzFolder);
            } else {
                tarGzFolder.mkdirs();
            }
            logger.debug("get files from ftp");

            FTPUtil.getFiles("/" + ftpFolder + "/" + institution + "/items/", mainLocalFolder + "targz//");

            logger.debug("loop over tar gz files and exact them to xml folder");
            XmlUtil.unTarGzFolder(tarGzFolder, mainLocalFolder + "xml//");
            logger.debug("loop over xml files and convert them to records");
            File xmlFolder = new File(mainLocalFolder + "xml//");
            File[] xmlFiles = xmlFolder.listFiles();
            int totalRecords = 0;
            for (File xmlFile : xmlFiles) {
                String methodName = null;
                if (xmlFile.getName().endsWith("_delete.xml")) {
                    methodName = "itemDeleted";
                } else {
                    methodName = "itemUpdated";
                }
                logger.debug("convert xml file to marc4j");
                List<Record> records = XmlUtil.xmlFileToMarc4jRecords(xmlFile);
                logger.debug("loop over records and merge with SCF");
                for (Record record : records) {
                    logger.debug("get network system number");
                    String NZMmsId = getNetworkNumber(record.getVariableFields("035"));
                    List<VariableField> variableFields = record.getVariableFields("ITM");
                    for (VariableField variableField : variableFields) {
                        ItemData itemData = ItemData.dataFieldToItemData((DataField) variableField, institution,
                                NZMmsId);
                        if (itemData.getBarcode() == null) {
                            logger.warn("Synchronize Item Failed. Barcode is null Item Data: " + variableField);
                        }
                        Method method = Class.forName("com.exlibris.items.ItemsHandler").getMethod(methodName,
                                ItemData.class);
                        method.invoke(null, itemData);
                    }
                }
                totalRecords += records.size();
            }
            logger.info("Total Records from FTP: " + totalRecords);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static String getNetworkNumber(List<VariableField> variableFields) {
        for (VariableField variableField : variableFields) {
            String subfield = ((DataField) variableField).getSubfieldsAsString("a");
            if (subfield != null && subfield.contains("EXLNZ")) {
                return subfield.replaceAll("^\\(EXLNZ(.*)\\)", "");
            }
        }
        return null;
    }

}
