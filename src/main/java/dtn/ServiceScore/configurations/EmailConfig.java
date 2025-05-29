package dtn.ServiceScore.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {
    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private int port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;
    @Bean
    public JavaMailSender mailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);  // Địa chỉ máy chủ SMTP
        mailSender.setPort(port);  // Cổng SMTP (thường là 587 cho TLS, 465 cho SSL)
        mailSender.setUsername(username);  // Tên đăng nhập email
        mailSender.setPassword(password);  // Mật khẩu email

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");  // Kích hoạt STARTTLS
        props.put("mail.debug", "true");  // Bật debug để xem chi tiết về quá trình gửi email

        return mailSender;
    }
}
