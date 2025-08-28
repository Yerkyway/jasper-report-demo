package com.example.JasperDemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportGeneratingController {

    private static final Logger logger = LoggerFactory.getLogger(ReportGeneratingController.class);

    @Autowired
    private ReportGeneratingService reportGeneratingService;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateReport(
            @RequestParam(defaultValue = "test_report.jrxml") String template,
            @RequestParam(defaultValue = "test_data.json") String data,
            @RequestParam(required = false) String output) {

        logger.info("REST API request - template: {}, data: {}, output: {}", template, data, output);

        try {
            if (output == null) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
                output = "report_" + timestamp + ".pdf";
            }

            String pdfPath = reportGeneratingService.generateReport(template, data, output);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Report generated successfully");
            response.put("outputPath", pdfPath);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            response.put("downloadUrl", "/api/reports/download/" + output);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("REST API error during report generation", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/generate/test")
    public ResponseEntity<Map<String, Object>> generateTestReport() {
        logger.info("REST API request - generating test report");

        try {
            String pdfPath = reportGeneratingService.generateTestReport();

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Test report generated successfully");
            response.put("outputPath", pdfPath);
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // Extract filename for download URL
            String fileName = pdfPath.substring(pdfPath.lastIndexOf("/") + 1);
            response.put("downloadUrl", "/api/reports/download/" + fileName);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("REST API error during test report generation", e);

            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadReport(@PathVariable String filename) {
        try {
            String filePath = "out/" + filename;
            File file = new File(filePath);

            if (!file.exists()) {
                logger.warn("Download requested for non-existent file: {}", filePath);
                return ResponseEntity.notFound().build();
            }

            logger.info("Downloading file: {}", filePath);

            Resource resource = new FileSystemResource(file);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(file.length())
                    .body(resource);

        } catch (Exception e) {
            logger.error("Error during file download: {}", filename, e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listReports() {
        try {
            File outDir = new File("out");
            Map<String, Object> response = new HashMap<>();

            if (!outDir.exists()) {
                response.put("files", new String[0]);
                response.put("count", 0);
                response.put("message", "Output directory does not exist");
                return ResponseEntity.ok(response);
            }

            File[] files = outDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".pdf"));

            if (files != null) {
                String[] fileNames = new String[files.length];
                for (int i = 0; i < files.length; i++) {
                    fileNames[i] = files[i].getName();
                }
                response.put("files", fileNames);
                response.put("count", files.length);
            } else {
                response.put("files", new String[0]);
                response.put("count", 0);
            }

            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error listing reports", e);
            Map<String, Object> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}