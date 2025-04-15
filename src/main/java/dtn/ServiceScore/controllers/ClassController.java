package dtn.ServiceScore.controllers;

import dtn.ServiceScore.components.ExcelHelper;
import dtn.ServiceScore.dtos.ClassDTO;
import dtn.ServiceScore.dtos.ClassSearchRequest;
import dtn.ServiceScore.services.ClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("api/v1/class")
@RequiredArgsConstructor
public class ClassController {
    private final ClassService classService;
    private final ExcelHelper excelHelper;
    @GetMapping("/all")
    public ResponseEntity<?> getAllFiveGood() {
        try {
            return ResponseEntity.ok(classService.getAllClass());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/search")
    public ResponseEntity<?> searchClasses(@RequestBody ClassSearchRequest request) {
        List<dtn.ServiceScore.model.Class> classes = classService.getClasses(request);
        return ResponseEntity.ok(classes);
    }
    @PostMapping("/create")
    public ResponseEntity<?> createClass(@RequestBody ClassDTO classDTO) {
        try {
            classService.createClass(classDTO);
            return ResponseEntity.ok("Thêm lớp thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi: " + e.getMessage());
        }
    }
    @PostMapping("/import")
    public ResponseEntity<?> importClassesFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            List<ClassDTO> classes = excelHelper.excelToClasses(file);
            for (ClassDTO classDTO : classes) {
                classService.createClass(classDTO); // Giả sử createClass trong service đã được chỉnh sửa để thêm nhiều lớp
            }
            return ResponseEntity.ok("Import lớp thành công!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi khi đọc file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Lỗi khi thêm lớp: " + e.getMessage());
        }
    }
}
