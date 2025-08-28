package com.example.JasperDemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDateTime;

@SpringBootApplication
public class JasperReportsApplication implements CommandLineRunner {

	private static final Logger logger = LoggerFactory.getLogger(JasperReportsApplication.class);

    @Autowired
    private ReportGeneratingService reportGeneratingService;

    public static void main(String[] args) {
        logger.info("STARTING_JasperReportsApplication");
        logger.info("START_TIME: {}", LocalDateTime.now());
        logger.info("VERSION: {}", System.getProperty("java.version"));

        SpringApplication.run(JasperReportsApplication.class, args);
	}

    @Override
    public void run(String... args) throws Exception {
        logger.info("APPLICATION_STARTED_SUCCESSFULLY");
        logger.info("SERVICE_IS_READY_TO_GENERATE_REPORTS");

        try {
            logger.info("GENERATING_REPORTS");
            String outPath = reportGeneratingService.generateTestReport();
            logger.info("TEST_REPORTS_GENERATED SUCCESSFULLY ");
        } catch (Exception e) {
            logger.error("FAILED_GENERATING_TEST_REPORTS", e);
        }
        logger.info("APPLICATION_READY");
    }
}
