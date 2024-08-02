package com.register.controller;

import com.register.storage.StorageFileNotFoundException;
import com.register.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/file")
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/list")
    public ResponseEntity<List<String>> listUploadedFiles(Model model) throws IOException {
        List<String> files = storageService.loadAll().map(
                        path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                                "serveFile", path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList());

        return ResponseEntity.ok(files);
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        Resource file = storageService.loadAsResource(filename);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }

        String contentType = determineContentType(file);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }

    private String determineContentType(Resource file) {
        String contentType = "application/octet-stream";

        try {
            contentType = Files.probeContentType(file.getFile().toPath());
        } catch (IOException e) {
            // Log error
        }

        return contentType;
    }

    @PostMapping("/create")
    public ResponseEntity<String> handleFileUpload(@RequestParam("file") MultipartFile file,
                                                   RedirectAttributes redirectAttributes) {
        String filename = storageService.store(file);
        return ResponseEntity.ok("You successfully uploaded " + filename + "!");
    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }
}