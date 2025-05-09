package com.example.salesforecast;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@RestController
@RequestMapping("/api")
public class ForecastController {

    @PostMapping("/forecast")
    public ResponseEntity<String> runForecast(@RequestBody ForecastRequest request) {
        try {
            String scriptPath = "src/main/resources/scripts/forecast.py";

            String startDate = request.getStartDate();
            String endDate = request.getEndDate();
            String quarters = String.valueOf(request.getQuarters());

            ProcessBuilder pb = new ProcessBuilder("python3", scriptPath, startDate, endDate, quarters);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                return ResponseEntity.ok(output.toString());
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Ошибка при выполнении скрипта.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Исключение: " + e.getMessage());
        }
    }
}
