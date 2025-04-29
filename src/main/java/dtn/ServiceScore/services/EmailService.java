package dtn.ServiceScore.services;
import jakarta.mail.MessagingException; // Đảm bảo dòng này đã được import

public interface EmailService {
    void sendResetPasswordEmail(String to, String name, String resetLink) throws MessagingException ;
}
