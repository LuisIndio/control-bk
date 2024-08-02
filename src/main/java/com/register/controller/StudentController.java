package com.register.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.register.model.Student;
import com.register.repository.StudentRepository;
import com.register.storage.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/student")
public class StudentController {
    private final StorageService storageService;

    @Autowired
    private StudentRepository studentRepository;

    public StudentController(StorageService storageService) {
        this.storageService = storageService;
    }

    private Student getStudentOrThrow(Long id) {
        return studentRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found")
        );
    }

    @GetMapping("/list")
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @PostMapping("/create")
    public ResponseEntity<Student> createStudent(@RequestBody Student student) {
        if (student == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Student savedStudent = studentRepository.save(student);
        return new ResponseEntity<>(savedStudent, HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable Long id, @RequestBody Student studentDetails) {
        Student student = getStudentOrThrow(id);

        student.setStudentName(studentDetails.getStudentName());
        student.setLastName(studentDetails.getLastName());
        student.setBirthDate(studentDetails.getBirthDate());
        student.setRegistrationDate(studentDetails.getRegistrationDate());
        student.setRegistrationEndDate(studentDetails.getRegistrationEndDate());

        Student updatedStudent = studentRepository.save(student);
        return new ResponseEntity<>(updatedStudent, HttpStatus.OK);
    }

    @GetMapping("/list/{id}")
    public Student getStudentById(@PathVariable Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    @PostMapping("/students/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file, @RequestParam("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        Student student = getStudentOrThrow(id);

        if (!file.isEmpty()) {
            String fileName;
            try {
                fileName = storageService.store(file);
            } catch (Exception e) {
                response.put("message", "Error to upload image");
                response.put("error", e.getMessage().concat(": ").concat(e.getMessage()));
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }

            String namePhotoBeforeDelete = student.getImagePath();
            if (namePhotoBeforeDelete != null && !namePhotoBeforeDelete.equals("default.jpg")) {
                storageService.delete(namePhotoBeforeDelete);
            }

            student.setImagePath(fileName);
            Student savedStudent = studentRepository.save(student);

            response.put("message", "Image uploaded successfully");
            response.put("student", savedStudent);
        }

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        Student student = getStudentOrThrow(id);

        String imageName = student.getImagePath();
        if (imageName != null && !imageName.equals("default.jpg")) {
            boolean isDeleted = storageService.delete(imageName);
            if (!isDeleted) {
                response.put("message", "Failed to delete image");
                return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }

        studentRepository.deleteById(id);
        response.put("message", "Student and image deleted successfully");

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}