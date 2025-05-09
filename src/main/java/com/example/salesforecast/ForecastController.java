package com.example.salesforecast;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") 
public class ForecastController {

    @PostMapping("/forecast")
    public ResponseEntity<String> runForecast(@RequestParam("file") MultipartFile file) {
        try {
            // Сохраняем файл во временную директорию
            File tempFile = File.createTempFile("uploaded-", ".csv");
            file.transferTo(tempFile);

            // Путь к Python-скрипту
            String scriptPath = "/app/scripts/forecast.py";

	    System.out.println("Script path: " + scriptPath);
	    System.out.println("Temp CSV path: " + tempFile.getAbsolutePath());
	    String pathToAnswer = "Script path: " + scriptPath + "\n" + "Temp CSV path: " + tempFile.getAbsolutePath();

            // Запускаем скрипт, передаём путь к файлу
            ProcessBuilder pb = new ProcessBuilder(
		    "/opt/venv/bin/python",
		    scriptPath,
		    "--csv", tempFile.getAbsolutePath(),
		    "--train-start", "2015",
		    "--train-end", "2023",
		    "--forecast-steps", "4"
		);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Читаем вывод скрипта
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
    		System.err.println("Возникла следующая ошибка при выполнении скрипта:\n" + output);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Ошибка при выполнении скрипта."+pathToAnswer+"\n" + output);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Исключение: " + e.getMessage());
        }
    }
}
