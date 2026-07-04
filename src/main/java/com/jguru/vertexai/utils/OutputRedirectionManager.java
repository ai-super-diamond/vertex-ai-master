package com.jguru.vertexai.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OutputRedirectionManager {

  private static final Logger logger = LoggerFactory.getLogger(OutputRedirectionManager.class);

  private PrintStream originalOut;
  private PrintStream originalErr;
  private PrintStream fileOut;
  private String outputFile;

  public void setupOutputRedirection(String outputFile, boolean captureOutput) throws Exception {
    this.outputFile = outputFile;

    if (this.outputFile == null && captureOutput) {
      Path resultsDirPath = Paths.get("results");
      try {
        Files.createDirectories(resultsDirPath);
      } catch (Exception e) {
        logger.error("Failed to create results directory: {}", resultsDirPath, e);
        throw new RuntimeException("Failed to create results directory: " + resultsDirPath, e);
      }

      LocalDateTime now = LocalDateTime.now();
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd·MM·yyyy_HH꞉mm꞉ss");
      String timestamp = now.format(formatter);
      this.outputFile = String.format("results/runtime-results-%s.txt", timestamp);
    }

    if (this.outputFile != null) {
      originalOut = System.out;
      originalErr = System.err;
      Path filePath = Paths.get(this.outputFile);
      Path parentDirPath = filePath.getParent();

      if (parentDirPath != null) {
        try {
          Files.createDirectories(parentDirPath);
        } catch (Exception e) {
          logger.error("Failed to create parent directory for output file: {}", parentDirPath, e);
          throw new RuntimeException("Failed to create parent directory for output file: " + parentDirPath, e);
        }
      }

      fileOut = new PrintStream(new FileOutputStream(filePath.toFile()), true, "UTF-8");
      System.setOut(fileOut);
      System.setErr(fileOut);
      originalOut.println("Writing output to: " + filePath.toAbsolutePath());
    }
  }

  public void closeOutputRedirection() {
    if (fileOut != null) {
      fileOut.flush();
      fileOut.close();
      System.setOut(originalOut);
      System.setErr(originalErr);
      if (originalOut != null) {
        originalOut.println("Output written to: " + outputFile);
      }
    }
  }
}
