package dtn.ServiceScore.controllers;

import dtn.ServiceScore.components.ExcelHelper;
import dtn.ServiceScore.dtos.ChangePasswordDTO;
import dtn.ServiceScore.dtos.UserDTO;
import dtn.ServiceScore.dtos.UserLoginDTO;
import dtn.ServiceScore.dtos.UserUpdateDTO;
import dtn.ServiceScore.model.User;
import dtn.ServiceScore.responses.LoginRespone;
import dtn.ServiceScore.responses.MessageResponse;
import dtn.ServiceScore.responses.UserResponse;
import dtn.ServiceScore.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ExcelHelper excelHelper;
    @PostMapping("/register")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO, BindingResult result) {
        try {
            if (result.hasErrors()) {
                List<String> errors = result.getFieldErrors()
                        .stream()
                        .map(FieldError::getDefaultMessage)
                        .toList();
                return ResponseEntity.badRequest().body(errors);
            }

            if (!userDTO.getPassword().equals(userDTO.getRetypePassword())) {
                return ResponseEntity.badRequest().body(new MessageResponse("mat khau khong trung khop"));
            }
            userService.createUser(userDTO);
            return ResponseEntity.ok(new MessageResponse("dang ki thanh cong"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginDTO userLoginDTO) {
        try {
            LoginRespone loginRespone = userService.login(userLoginDTO.getUsername(), userLoginDTO.getPassword());

            return ResponseEntity.ok(loginRespone);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/info")
    public UserResponse getCurrentUser() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .fullname(user.getFullname())
                .phoneNumber(user.getPhoneNumber())
                .studentId(user.getStudentId() != null ? user.getStudentId() : null)
                .address(user.getAddress())
                .isActive(user.isActive())
                .dateOfBirth(user.getDateOfBirth())
                .email(user.getEmail())
                .username(user.getUsername())
                .clazz(user.getClazz() != null ? user.getClazz().getName() : null)
                .Department(user.getClazz() != null && user.getClazz().getDepartment() != null
                        ? user.getClazz().getDepartment().getName()
                        : null)
                .build();
        return userResponse;

    }

    @GetMapping("/by_class/{classId}")
    public List<?> getUsersByClass(@PathVariable Long classId) {
        List<UserResponse> userResponses = userService.findUsersByClassId(classId)
                .stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .address(user.getAddress())
                        .phoneNumber(user.getPhoneNumber())
                        .fullname(user.getFullname())
                        .studentId(user.getStudentId())
                        .build()
                ).collect(Collectors.toList());

        // Đảo ngược danh sách
        Collections.reverse(userResponses);
        return userResponses;
    }

    // thay đổi mật khẩu
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDTO request) {
        userService.changePassword(request);
        return ResponseEntity.ok(new MessageResponse("Đổi mật khẩu thành công!"));
    }



    @GetMapping("/students")
    public ResponseEntity<?> getStudents(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<User> studentPage = userService.getUsersByRole("SV", pageable);

        Page<UserResponse> responsePage = studentPage.map(user -> UserResponse.builder()
                .id(user.getId())
                .fullname(user.getFullname())
                .phoneNumber(user.getPhoneNumber())
                .studentId(user.getStudentId())
                .address(user.getAddress())
                .isActive(user.isActive())
                .dateOfBirth(user.getDateOfBirth())
                .email(user.getEmail())
                .username(user.getUsername())
                .clazz(user.getClazz() != null ? user.getClazz().getName() : null)
                .Department(user.getClazz() != null && user.getClazz().getDepartment() != null
                        ? user.getClazz().getDepartment().getName() : null)
                .build());

        return ResponseEntity.ok(responsePage);
    }
    @GetMapping("/lcdlist")
    public ResponseEntity<?> getLcds(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<User> studentPage = userService.getUsersByRole("LCD", pageable);

        Page<UserResponse> responsePage = studentPage.map(user -> UserResponse.builder()
                .id(user.getId())
                .fullname(user.getFullname())
                .phoneNumber(user.getPhoneNumber())
                .studentId(user.getStudentId())
                .address(user.getAddress())
                .isActive(user.isActive())
                .dateOfBirth(user.getDateOfBirth())
                .email(user.getEmail())
                .username(user.getUsername())
                .clazz(user.getClazz() != null ? user.getClazz().getName() : null)
                .Department(user.getClazz() != null && user.getClazz().getDepartment() != null
                        ? user.getClazz().getDepartment().getName() : null)
                .build());

        return ResponseEntity.ok(responsePage);
    }
    @GetMapping("/exclude-sv-lcd")
    public ResponseEntity<?> getUsersExcludeSVAndLCD() {
        List<User> users = userService.getUsersExcludingRoles();

        List<UserResponse> userResponses = users.stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .fullname(user.getFullname())
                        .phoneNumber(user.getPhoneNumber())
                        .studentId(user.getStudentId())
                        .address(user.getAddress())
                        .isActive(user.isActive())
                        .dateOfBirth(user.getDateOfBirth())
                        .email(user.getEmail())
                        .username(user.getUsername())
                        .role(user.getRole().getName())
                        .clazz(user.getClazz() != null ? user.getClazz().getName() : null)
                        .Department(user.getClazz() != null && user.getClazz().getDepartment() != null
                                ? user.getClazz().getDepartment().getName() : null)
                        .build())
                .collect(Collectors.toList());

        return ResponseEntity.ok(userResponses);
    }
    @PostMapping("/add-student-exel")
    public ResponseEntity<?> registerMultipleUsers(@RequestParam("file") MultipartFile file) {
        try {
            List<UserDTO> userList = excelHelper.excelToUsers(file);

            List<String> result = new ArrayList<>();
            for (UserDTO userDTO : userList) {
                try {
                    userService.createUser(userDTO);
                    result.add("Thành công");
                } catch (Exception e) {
                    result.add("Lỗi với " + userDTO.getStudentId() + ": " + e.getMessage());
                }
            }

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Không thể đọc file Excel: " + e.getMessage());
        }
    }

    @GetMapping("/search/student")
    public ResponseEntity<?> searchUsersPaginated(@RequestParam String search,
                                                   @RequestParam int page,
                                                   @RequestParam int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> userPage = userService.searchUsersPaginated(search, pageable);

        Page<UserResponse> responsePage = userPage.map(user -> UserResponse.builder()
                .id(user.getId())
                .fullname(user.getFullname())
                .phoneNumber(user.getPhoneNumber())
                .studentId(user.getStudentId())
                .address(user.getAddress())
                .isActive(user.isActive())
                .dateOfBirth(user.getDateOfBirth())
                .email(user.getEmail())
                .username(user.getUsername())
                .clazz(user.getClazz() != null ? user.getClazz().getName() : null)
                .Department(user.getClazz() != null && user.getClazz().getDepartment() != null
                        ? user.getClazz().getDepartment().getName() : null)
                .role(user.getRole().getName()) // thêm role nếu cần thiết
                .build());

        return ResponseEntity.ok(responsePage);
    }

    @PutMapping("/profile/{id}")
    public  ResponseEntity<?> updateProfile(@PathVariable Long id,
                                @RequestBody UserUpdateDTO dto) {
        System.out.println("Updating profile for user with ID: " + id);
        userService.updateUserProfileById(id, dto);
        return ResponseEntity.ok( new MessageResponse("cập nhật thành công"));
    }

    @PutMapping("/deactivate/{id}")
    public ResponseEntity<?> deactivateUser(@PathVariable("id") long id) {
        User updatedUser = userService.deactivateUserById(id);
        if (updatedUser != null) {
            return  ResponseEntity.ok( new MessageResponse("cập nhật thành công")); // Trả về người dùng đã được cập nhật
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id); // Trả về lỗi 404 nếu không tìm thấy người dùng
        }
    }


}
