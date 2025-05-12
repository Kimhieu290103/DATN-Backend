package dtn.ServiceScore.components;

import dtn.ServiceScore.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {
    @Value("${jwt.expiration}")
    private int expiration; // luu vao bien moi truong
    @Value("${jwt.secretKey}")
    private String serectKey;

    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        // thêm sau, nếu xóa thì xóa cái này
        String subject;
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            claims.put("username", user.getUsername());
            subject = user.getUsername();
        } else {
            claims.put("studentId", user.getStudentId());
            subject = user.getStudentId();
        }
        // ô viền dưới để xóa
        claims.put("userName", user.getUsername());
        try {
            return Jwts.builder()
                    .claims(claims)
//                    .subject(user.getUsername()) // cái này là dùng username
                    .subject(subject)
                    .expiration(new Date(System.currentTimeMillis() + expiration * 1000L))
                    .signWith(getSignInKey()) // Không cần SignatureAlgorithm
                    .compact();
        } catch (Exception e) {
            System.err.println("Can't create JWT Token: " + e.getMessage());
            return null;
        }
    }
    public String generateResetToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("purpose", "RESET_PASSWORD"); // để phân biệt rõ
        // dùng thêm srudent id
        String subject = (user.getUsername() != null && !user.getUsername().isEmpty())
                ? user.getUsername()
                : user.getStudentId();
        // mốc dưới để xóa phía trên
        return Jwts.builder()
                .claims(claims)
           //     .subject(user.getUsername()) đây là dùng username
                .subject(subject)
                .expiration(new Date(System.currentTimeMillis() + 15 * 60 * 1000)) // 15 phút
                .signWith(getSignInKey())
                .compact();
    }

    private SecretKey getSignInKey() {
        byte[] bytes = Decoders.BASE64.decode(serectKey);
        return Keys.hmacShaKeyFor(bytes);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = this.extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        // cái cũ dùng username
//        String userName = extractUserName(token);
//        return (userName.equals(userDetails.getUsername())) && !isTokenExpired(token);

        // cái mới
        String subject = extractUserName(token);
        return subject.equals(userDetails.getUsername()) && !isTokenExpired(token);

    }

}
