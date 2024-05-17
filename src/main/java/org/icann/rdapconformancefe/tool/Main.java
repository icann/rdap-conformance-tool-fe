package org.icann.rdapconformancefe.tool;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.icann.rdapconformance.tool.RdapConformanceTool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import picocli.CommandLine;

@SpringBootApplication
public class Main {

  private static Logger LOGGER = Logger.getLogger(Main.class.getName());

  public static void main(String[] args) {
    LOGGER.info("Starting the application..");
    // LOGGER.setLevel(Level.DEBUG);
    SpringApplication.run(Main.class, args);
  }

  @RestController
  public class SearchController {

    public String findValidationResult(List<String> output) {
      for (String line : output) {
        if (line.contains("RDAPValidationResult")) {
          System.out.println("XXX Found the line: " + line);
          return line;
        }
      }
      return null; // or some default value
    }

    @PostMapping("/check")
    @ResponseBody
    public Map<String, String> check(
        @RequestParam String url,
        @RequestParam(required = false) String gltdRegistrar,
        @RequestParam(required = false) String gltdRegistry,
        @RequestParam(required = false) String thin) {
      // LOGGER.info("Received URL: " + url);
      // LOGGER.info("GltdRegistrar: " + gltdRegistrar);
      // LOGGER.info("GltdRegistry: " + gltdRegistry);
      // LOGGER.info("Thin: " + thin);
      System.out.println("Received URL: " + url);
      System.out.println("GltdRegistrar: " + gltdRegistrar);
      System.out.println("GltdRegistry: " + gltdRegistry);
      System.out.println("Thin: " + thin);

      // Get the RDPT directory from the environment
      String rdpt = System.getenv("RDPT");

      // Construct the arguments
      String[] args =
          new String[] {
            "--use-local-datasets",
            "-v",
            "--print-results-path",
            "-c",
            rdpt + "/rdapct-config.json",
            url
          };

      // Create a new CommandLine instance
      // CommandLine cmd = new CommandLine(new RdapConformanceTool());
      // Execute the RdapConformanceTool with the arguments
      // int exitCode = cmd.execute(args);
      // We shouldn't need a Callable that will execute the command
      String resultsFile = null;
      try {
        // Create a new ByteArrayOutputStream to capture the output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);

        // Save the old System.out and System.err
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;

        // Redirect System.out and System.err to the PrintStream
        System.setOut(ps);
        System.setErr(ps);

        // Create a new CommandLine instance
        CommandLine cmd = new CommandLine(new RdapConformanceTool());

        // Execute the RdapConformanceTool with the arguments
        int exitCode = cmd.execute(args);

        // Restore the old System.out and System.err
        System.setOut(oldOut);
        System.setErr(oldErr);

        // Get the output as a list of strings
        List<String> output = Arrays.asList(baos.toString().split("\\n"));
        output.forEach(System.out::println);

        for (String line : output) {
          System.out.println("<OUTPUT> " + line);
        }
        for (String line : output) {
          System.out.println(line);
          if (line.startsWith("[RdapConformaceTool] ==> Results path is: ")) {
            resultsFile = line.substring("[RdapConformaceTool] ==> Results path is: ".length());
          }
        }
      } catch (Exception e) {
        // Handle exception
        List<String> output = Collections.singletonList("Error: " + e.getMessage());
      }
      // After restoring System.out and System.err
      // LOGGER = Logger.getLogger(Main.class.getName());
      System.out.println("Run is finished, setting up the data to return it.");
      // Print the output
      // output.forEach(System.out::println);
      // Create a map to store the result
      Map<String, String> resultMap = new HashMap<>();

      if (resultsFile != null) {
        try {
          String fileContent = new String(Files.readAllBytes(Paths.get(resultsFile)));
          resultMap.put("data", fileContent);
          System.out.println("Results file content: " + fileContent);
        } catch (IOException e) {
          // LOGGER.error("Error reading file: " + resultsFile, e);
          System.out.println("Results file error: " + e.getMessage());
          resultMap.put("data", "error");
        }
      } else {
        resultMap.put("data", "ok");
      }

      System.out.println("Processed is finished, all set to return..");
      return resultMap;
    } // end of post

    @PostMapping("/old")
    @ResponseBody
    public Map<String, String> old(@RequestParam String url) {
      LOGGER.info("OLD Received URL: " + url);

      // Get the RDPT directory from the environment
      String rdpt = System.getenv("RDPT");
      String rdapct = System.getenv("RDAPCT");

      // Construct the command
      List<String> command =
          Arrays.asList(
              "java",
              "-jar",
              rdapct + "/tool/target/rdapct-1.0.2.jar",
              "--use-local-datasets",
              // "-v",
              "--print-results-path",
              "-c",
              // rdpt + "/tool/bin/rdapct-config.json",
              rdpt + "/rdapct-config.json",
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

        // Extract the code
        if (result.contains("RDAPValidationResult{")) {
          result = result.substring(result.indexOf("RDAPValidationResult{"));
        }

        // Create a map to store the result
        Map<String, String> resultMap = new HashMap<>();
        resultMap.put("data", result);
        LOGGER.info("OLD Processed is finished, all set to return..");
        return resultMap;

      } catch (IOException e) {
        LOGGER.severe("Failed to run command: " + e.getMessage());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // restore interrupted status
        LOGGER.severe("Thread was interrupted: " + e.getMessage());
      }
      // Create a map to store the error message
      Map<String, String> errorMap = new HashMap<>();
      errorMap.put("data", "bad");

      LOGGER.info("OLD Processed is finished, bad error!");

      return errorMap;
    } // end of post
  } // end of class
} // end of main
