package dtn.ServiceScore.controllers;

import dtn.ServiceScore.components.JwtTokenUtil;
import dtn.ServiceScore.dtos.ResetPasswordDTO;
import dtn.ServiceScore.model.User;
import dtn.ServiceScore.repositories.UserRepository;
import dtn.ServiceScore.services.EmailService;
import dtn.ServiceScore.services.UserService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/forgotpass")
@RequiredArgsConstructor
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final JwtTokenUtil jwtTokenUtil;
    private final EmailService emailService;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        // Tìm người dùng theo email
        Optional<User> optionalUser = userRepository.findByEmail(email);

        // Nếu không tìm thấy người dùng với email này
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Email không tồn tại trong hệ thống");
        }

        User user = optionalUser.get();

        // Tạo mã reset password token
        String resetToken = jwtTokenUtil.generateResetToken(user);

        // Gửi email chứa mã token
        try {
            emailService.sendResetPasswordEmail(user.getEmail(), user.getFullname(), resetToken);
            return ResponseEntity.ok("Đã gửi mã đặt lại mật khẩu vào email");
        } catch (MessagingException e) {
            return ResponseEntity.internalServerError().body("Không thể gửi email: " + e.getMessage());
        }
    }
    @PostMapping("/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDTO resetDTO) {
        String token = resetDTO.getToken();
        String newPassword = resetDTO.getNewPassword();

        // Kiểm tra token
        if (jwtTokenUtil.isTokenExpired(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token đã hết hạn");
        }

        // Lấy username từ token
        String username = jwtTokenUtil.extractUserName(token);
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Người dùng không tồn tại");
        }

        User user = optionalUser.get();

        // Cập nhật mật khẩu mới
        user.setPassword(passwordEncoder.encode(newPassword));
// dùng hàm encode nếu có
        userRepository.save(user);

        return ResponseEntity.ok("Đặt lại mật khẩu thành công");
    }


}
