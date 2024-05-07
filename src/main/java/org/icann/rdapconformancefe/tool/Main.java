package org.icann.rdapconformancefe.tool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

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

                Executors.newSingleThreadExecutor().submit(() -> {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
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
    }
}