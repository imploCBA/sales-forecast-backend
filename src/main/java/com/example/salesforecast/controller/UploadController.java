package com.example.salesforecast.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@CrossOrigin // разрешаем доступ с фронтенда
public class UploadController {

    @PostMapping("/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file) throws IOException {
        // TODO: вызвать здесь Python-нейросеть
        System.out.println("Файл получен: " + file.getOriginalFilename());
        return ResponseEntity.ok("Файл успешно получен: " + file.getOriginalFilename());
    }
}