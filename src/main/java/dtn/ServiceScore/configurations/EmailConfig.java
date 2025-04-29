package dtn.ServiceScore.configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");  // Địa chỉ máy chủ SMTP
        mailSender.setPort(587);  // Cổng SMTP (thường là 587 cho TLS, 465 cho SSL)
        mailSender.setUsername("trankimhieu1008@gmail.com");  // Tên đăng nhập email
        mailSender.setPassword("lldq uzsm agwe vqwi");  // Mật khẩu email

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");  // Kích hoạt STARTTLS
        props.put("mail.debug", "true");  // Bật debug để xem chi tiết về quá trình gửi email

        return mailSender;
    }
}
