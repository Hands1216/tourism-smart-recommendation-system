import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

public class GenToken {
    public static void main(String[] args) {
        String SECRET = "tourism-smart-recommendation-system-jwt-secret-key-2025";
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        
        long EXPIRATION = 7 * 24 * 60 * 60 * 1000;
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION);
        
        String token = Jwts.builder()
            .subject("1")
            .claims(Map.of("role", "user"))
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact();
        
        System.out.println(token);
    }
}
