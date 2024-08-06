package org.icann.rdapconformancefe.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import org.icann.rdapconformance.validator.workflow.FileSystem;
import org.icann.rdapconformance.validator.workflow.LocalFileSystem;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPValidationStatus;
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
                resultMap.put("error", "bad arguments");
                return resultMap;
            }

            if ("1".equals(gltdRegistrar) && "1".equals(thin)) {
                resultMap.put("error", "bad arguments");
                return resultMap;
            }

            // clean the url
            url = url.trim();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                // even though the front-end checks for this already, play defense and be nice
                url = "http://" + url;
            }

            LOGGER.info("Received URL: " + url);
            LOGGER.info("GltdRegistrar: " + gltdRegistrar);
            LOGGER.info("GltdRegistry: " + gltdRegistry);
            LOGGER.info("Thin: " + thin);

            // Get the RDAPCT directory from the environment
            String rdpt = System.getenv("RDAPCT");
            // if it's not set return error
            if (rdpt == null) {
                LOGGER.info("RDAPCT environment variable not set");
                resultMap.put("error", "RDAPCT environment variable not set");
                return resultMap;
            }

            String resultsFile = null;

            try {
                FileSystem fileSystem = new LocalFileSystem();
                WebRDAPConfiguration configuration = new WebRDAPConfiguration();
                configuration.setUseLocalDatasets(true);
                configuration.setUri(new URI(url));

                URL configFile = this.getClass().getClassLoader().getResource("/sql");
                LOGGER.info("Configuration file: " + configFile.toString());
                configuration.setConfigurationFile(configFile.toString());
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

                LOGGER.info("Configuration is set up, run it");
                RDAPHttpValidator validator = new RDAPHttpValidator(configuration, fileSystem);
                Integer vret = validator.validate();
                LOGGER.info("Validator returned: " + Integer.toString(vret));
                // if the return code isn't 0 something went wrong, look it up and then tell the front-end
                if (vret != 0) {
                    RDAPValidationStatus status = findStatusByValue(vret);
                    if (status != null) {
                        String explanation = "Error code: " + vret + " - " + status.getDescription();
                        resultMap.put("error", explanation);
                        LOGGER.info(explanation);
                    } else {
                        String explanation = "Unknown error code: " + vret;
                        resultMap.put("error", explanation);
                        LOGGER.info(explanation);
                    }
                    return resultMap;
                }

                resultsFile = validator.getResultsPath();
                LOGGER.info("Results file: " + resultsFile);
            } catch (Exception e) {
                LOGGER.info("Error: " + e.getMessage());
                resultMap.put("error", e.getMessage());
            }

            LOGGER.info("Run is finished, setting up the data to return it.");

            if (resultsFile != null) {
                try {
                    String fileContent = new String(Files.readAllBytes(Paths.get(resultsFile)));
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode jsonObject = (ObjectNode) mapper.readTree(fileContent);

                    // Get the values of the keys to be moved
                    Object testedURI = jsonObject.get("testedURI");
                    Object receivedHttpStatusCode = jsonObject.get("receivedHttpStatusCode");
                    Object definitionIdentifier = jsonObject.get("definitionIdentifier");

                    // Remove the keys from the original object
                    jsonObject.remove("testedURI");
                    jsonObject.remove("receivedHttpStatusCode");
                    jsonObject.remove("definitionIdentifier");

                    // Create a new LinkedHashMap and put the keys back in at the top
                    LinkedHashMap<String, Object> newJsonObject = new LinkedHashMap<>();
                    newJsonObject.put("testedURI", testedURI);
                    newJsonObject.put("receivedHttpStatusCode", receivedHttpStatusCode);
                    newJsonObject.put("definitionIdentifier", definitionIdentifier);

                    // Put the rest of the keys back in
                    jsonObject
                        .fields()
                        .forEachRemaining(entry -> newJsonObject.put(entry.getKey(), entry.getValue()));
                    // put it all in the result map for the client
                    resultMap.put("data", mapper.writeValueAsString(newJsonObject));
                    LOGGER.info("Got the file contents.");
                } catch (IOException e) {
                    LOGGER.info("Results file error: " + e.getMessage());
                    resultMap.put("error", "Unable to read results");
                }
            } else {
                LOGGER.info("No results file.");
                resultMap.put("error", "Unable to read results");
            }
            LOGGER.info("Processed is finished, all set to return..");
            return resultMap;
        } // end of post

        // Lookup the error codes so we can tell them what happened
        private RDAPValidationStatus findStatusByValue(int value) {
            for (RDAPValidationStatus status : RDAPValidationStatus.values()) {
                if (status.getValue() == value) {
                    return status;
                }
            }
            return null; // not found
        }
    } // end of class
} // end of main
