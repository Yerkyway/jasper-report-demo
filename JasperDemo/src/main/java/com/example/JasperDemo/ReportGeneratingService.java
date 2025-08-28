package com.example.JasperDemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportGeneratingService {
    private static final Logger logger = LoggerFactory.getLogger(ReportGeneratingService.class);
    private ObjectMapper objectMapper = new ObjectMapper();

    public String generateReport(String templateName,
                                 String dataFileName,
                                 String outputFileName) {

        long startTime = System.currentTimeMillis();
        logger.info("TEMPLATE={} DATA={} OUTPUT={}", templateName, dataFileName, outputFileName);
        try {
            logger.info("TEMPLATE_FILE {}", templateName);
            JasperReport jasperReport = loadTemplate(templateName);
            logger.info("TEMPLATE_FILE_LOADED_SUCCESSFULLY");

            logger.info("DATA_FILE {}", dataFileName);
            Map<String, Object> parameters = new HashMap<String, Object>();
            JRDataSource dataSource = loadData(dataFileName, parameters);
            logger.info("RECORDS {}", parameters);
            logger.info("DATA_FILE_LOADED_SUCCESSFULLY");

            logger.info("STARTING GENERATING REPORT");
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            String outPath = "out/ "+outputFileName;
            JasperExportManager.exportReportToPdfFile(jasperPrint, outPath);

            File outputFile = new File(outPath);
            long duration = System.currentTimeMillis() - startTime;

            if (outputFile.exists()) {
                long fileSize = outputFile.length()/1024;
                logger.info("OUTPUT_FILE {} SIZE {}kb DURATION {}ms", outputFileName, fileSize, duration);
                return outputFileName;
            } else {
                logger.info("ERROR = OUTPUT_FILE_WAS_NOT_CREATED");
                throw new RuntimeException("Pdf was not created {}"+outputFileName);
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            logger.error("ERROR {} DUARTION {}ms",
                    e.getMessage(), duration, e);
            throw new RuntimeException("Report generation failed" + e.getMessage(), e);
        }
    }

    public String generateTestReport() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        return generateReport("test_report.jrxml", "test_data.json", "test_report_" + timestamp + ".pdf");
    }

    private int getDataSourceSize(JRDataSource dataSource) {
        if (dataSource instanceof JRBeanCollectionDataSource) {
            return ((JRBeanCollectionDataSource) dataSource).getData().size();
        }
        return -1;
    }

    private JasperReport loadTemplate(String templateName) {
        try {
            String templatePath = "templates/" + templateName;
            File templateFile = new File(templatePath);

            if (!templateFile.exists()) {
                throw new FileNotFoundException("Template file not found: " + templatePath);
            }
            logger.info("LOADING_TEMPLATE_FILE_FROM {}", templateFile.getAbsolutePath());

            if (templateName.endsWith(".jrxml")) {
                return JasperCompileManager.compileReport(templateFile.getAbsolutePath());
            } else if(templateName.endsWith(".jasper")) {
                try (InputStream inputStream = new FileInputStream(templateFile)) {
                    return (JasperReport) JRLoader.loadObject(inputStream);
                }
            } else {
                throw new RuntimeException("Use .jrxml or .jasper template file");
            }
        } catch (Exception e) {
            logger.error("FAILED_LOADING_TEMPLATE_FILE {}", templateName, e);
            throw new RuntimeException("Error loading template file: " + e.getMessage(), e);
        }
    }

    private JRDataSource loadData(String dataFileName, Map<String, Object> parameters) {
        try {
            String dataFilePath = "data/" + dataFileName;
            File dataFile = new File(dataFilePath);

            if (!dataFile.exists()) {
                logger.warn("DATA_FILE_NOT_FOUND: {} USING_EMPTY_DATA", dataFilePath);
                return new JRBeanCollectionDataSource(Collections.emptyList());
            }

            logger.info("LOADING_DATA_FILE_FROM {}", dataFile.getAbsolutePath());
            String jsonContent = FileUtils.readFileToString(dataFile, "UTF-8");

            Map<String, Object> jsonData = objectMapper.readValue(jsonContent, Map.class);

            jsonData.forEach((key, value) -> {
                if (!"item".equals(key)) {
                    parameters.put(key, value);
                    logger.debug("ADDED_PARAMETER: {} = {}", key, value);
                }
            });

            Object itemsObject = jsonData.get("items");
            if (itemsObject instanceof List) {
                List<?> items = (List<?>) itemsObject;
                logger.info("LOADED {} DATA_RECORDS", items.size());
                return new JRBeanCollectionDataSource(items);
            } else {
                logger.info("NO ITEMS COLLECTION FOUND, USING SINGLE RECORD");
                return new JRBeanCollectionDataSource(Collections.singletonList(jsonData));
            }
        } catch (Exception e) {
            logger.error("FAILED_TO_LOAD_DATA: {}", dataFileName, e);
            logger.warn("USING_EMPTY_DATA_SOURCES_DUE_TO_DATA_LOADING_ERROR");
            return new JRBeanCollectionDataSource(Collections.emptyList());
        }
    }
}
