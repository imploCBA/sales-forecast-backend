package com.example.salesforecast.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // Разрешаем CORS для фронтенда
public class UploadController {

    @PostMapping("/upload")
    public ResponseEntity<List<List<String>>> handleFileUpload(@RequestParam("file") MultipartFile file) {
        List<List<String>> result = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            int count = 0;
            while ((line = reader.readLine()) != null && count < 2) {
                String[] values = line.split(",");
                result.add(Arrays.asList(values));
                count++;
            }

            return ResponseEntity.ok(result);

        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}