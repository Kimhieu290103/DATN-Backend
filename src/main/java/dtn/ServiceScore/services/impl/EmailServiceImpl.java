package dtn.ServiceScore.services.impl;

import dtn.ServiceScore.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    private final JavaMailSender mailSender;

    @Override
    public void sendResetPasswordEmail(String to, String name, String resetToken) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Yêu cầu đặt lại mật khẩu");
        helper.setText(
                "<p>Chào " + name + ",</p>" +
                        "<p>Bạn đã yêu cầu đặt lại mật khẩu.</p>" +
                        "<p>Mã token của bạn là: <strong>" + resetToken + "</strong></p>" +
                        "<p>Mã này sẽ hết hạn trong 15 phút.</p>" +
                        "<p>Nếu bạn không yêu cầu điều này, hãy bỏ qua email này.</p>",
                true
        );

        mailSender.send(message);
    }
}
