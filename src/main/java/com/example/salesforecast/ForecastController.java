package com.example.salesforecast;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") 
public class ForecastController {

    @PostMapping("/forecast")
    public ResponseEntity<String> runForecast(
            @RequestParam("file") MultipartFile file,
            @RequestParam("trainStart") int trainStart,
            @RequestParam("trainEnd") int trainEnd,
            @RequestParam("forecastSteps") int forecastSteps) {

        try {
            File tempFile = File.createTempFile("uploaded-", ".csv");
            file.transferTo(tempFile);

            String scriptPath = "/app/scripts/forecast.py";
            List<String> command = Arrays.asList(
                "/opt/venv/bin/python", scriptPath,
                "--csv", tempFile.getAbsolutePath(),
                "--train-start", String.valueOf(trainStart),
                "--train-end", String.valueOf(trainEnd),
                "--forecast-steps", String.valueOf(forecastSteps)
            );

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return ResponseEntity.ok(output.toString());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Ошибка при выполнении скрипта:\n" + output.toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Исключение: " + e.toString());
        }
    }
}
