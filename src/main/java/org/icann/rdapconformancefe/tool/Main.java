package org.icann.rdapconformancefe.tool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class Main {

  private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) {
    SpringApplication.run(Main.class, args);
  }

  @RestController
  public class SearchController {

    @GetMapping("/search")
    public String search() {
      ProcessBuilder processBuilder = new ProcessBuilder("ls", "-laR", "/tmp/*");
      processBuilder.redirectErrorStream(true);

      StringBuilder output = new StringBuilder();

      try {
        Process process = processBuilder.start();

        Executors.newSingleThreadExecutor()
            .submit(
                () -> {
                  try (BufferedReader reader =
                      new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                      output.append(line).append("\n");
                    }
                  } catch (Exception e) {
                    LOGGER.severe("Error reading process output: " + e.getMessage());
                    e.printStackTrace();
                  }
                });

        // Wait for the process to finish
        int exitCode = process.waitFor();
        LOGGER.info("Process finished with exit code: " + exitCode);

      } catch (Exception e) {
        LOGGER.severe("Error starting process: " + e.getMessage());
        e.printStackTrace();
      }

      return output.toString();
    }

    @PostMapping("/check")
    public String check(@RequestParam String url) {
      LOGGER.info("Received URL: " + url);

      // Get the RDPT directory from the environment
      String rdpt = System.getenv("RDPT");

      // Construct the command
      List<String> command =
          Arrays.asList(
              "java",
              "-jar",
              rdpt + "/tool/bin/rdapct-1.0.jar",
              "--use-local-datasets",
              "-v",
              "-c",
              rdpt + "/tool/bin/rdapct-config.json",
              url);
      try {
        // Run the command
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true); // Redirect error stream to output stream
        Process process = processBuilder.start();

        // Create a reader for the output stream
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        // Create a list to store lines
        List<String> lines = new ArrayList<>();

        // Read the output and error streams
        String line;
        while ((line = reader.readLine()) != null) {
          System.out.println(line); // Write to console
          lines.add(line); // Add line to list
        }

        // Wait for the process to finish
        int exitCode = process.waitFor();

        // Process the lines
        String result =
            lines
                .stream()
                .filter(l -> l.contains("RDAPValidationResult{") && l.contains("}"))
                .findFirst()
                .orElse("unknown");
        return "data: " + result;
      } catch (IOException e) {
        LOGGER.severe("Failed to run command: " + e.getMessage());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // restore interrupted status
        LOGGER.severe("Thread was interrupted: " + e.getMessage());
      }

      return "data: bad";
    } // end of post
  } // end of class
} // end of main
