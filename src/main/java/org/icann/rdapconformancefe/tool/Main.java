package org.icann.rdapconformancefe.tool;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.*;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.LocalFileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.http.RDAPHttpValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

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

    @PostMapping("/check")
    @ResponseBody
    public Map<String, String> check(
        @RequestParam String url,
        @RequestParam(required = false) String gltdRegistrar,
        @RequestParam(required = false) String gltdRegistry,
        @RequestParam(required = false) String thin) {

      Map<String, String> resultMap = new HashMap<>();

      // Check the rules
      if ("1".equals(gltdRegistrar) && ("1".equals(gltdRegistry) || "1".equals(thin))) {
        resultMap.put("data", "bad arguments");
        return resultMap;
      }

      if ("1".equals(gltdRegistrar) && "1".equals(thin)) {
        resultMap.put("data", "bad arguments");
        return resultMap;
      }

      // debug stuff Sysout b/c the redirects of streams messes with Springs logger
      System.out.println("Received URL: " + url);
      System.out.println("GltdRegistrar: " + gltdRegistrar);
      System.out.println("GltdRegistry: " + gltdRegistry);
      System.out.println("Thin: " + thin);

      // Get the RDPT directory from the environment
      String rdpt = System.getenv("RDPT");
      Pattern pattern = Pattern.compile(".*  : (.*)$");

      // We shouldn't need a Callable, Spring runs in a separate thread
      String resultsFile = null;
      try {
        FileSystem fileSystem = new LocalFileSystem();
        WebRDAPConfiguration configuration = new WebRDAPConfiguration();
        configuration.setUseLocalDatasets(true);
        configuration.setUri(new URI(url));
        configuration.setConfigurationFile(rdpt + "/rdapct-config.json");
        configuration.setMaxRedirects(3);
        configuration.setTimeout(20);
        configuration.setVerbose(true);

        if ("1".equals(gltdRegistrar)) {
          configuration.setGtldRegistrar(true);
        }

        if ("1".equals(gltdRegistry)) {
          configuration.setGtldRegistry(true);
        }

        if ("1".equals(thin)) {
          configuration.setThin(true);
        }

        System.out.println("Configuration is set up.");
        RDAPHttpValidator validator = new RDAPHttpValidator(configuration, fileSystem);
        System.out.println("Validator is set up, run it");
        Integer vret = validator.validate();
        resultsFile = validator.getResultsPath();
        System.out.println("Validator is finished, get the results");
        System.out.println("Results file: " + resultsFile);
        System.out.println("Results file is set.");
      } catch (Exception e) {
        // Handle exception
        List<String> output = Collections.singletonList("Error: " + e.getMessage());
        output.forEach(System.out::println);
      }
      System.out.println("Run is finished, setting up the data to return it.");

      if (resultsFile != null) {
        try {
          String fileContent = new String(Files.readAllBytes(Paths.get(resultsFile)));
          resultMap.put("data", fileContent);
          System.out.println("Got the file contents.");
          // System.out.println("Results file content: " + fileContent);
        } catch (IOException e) {
          System.out.println("Results file error: " + e.getMessage());
          resultMap.put("data", "error");
        }
      } else {
        System.out.println("No results file.");
        resultMap.put("data", "ok");
      }

      System.out.println("Processed is finished, all set to return..");
      return resultMap;
    } // end of post
  } // end of class
} // end of main
