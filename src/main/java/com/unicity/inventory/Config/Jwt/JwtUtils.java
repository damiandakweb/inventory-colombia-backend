package com.unicity.inventory.Config.Jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration.ms}")
    private int jwtExpirationMs;

    private Key key;

    // Este método se ejecuta una vez que las propiedades (@Value) han sido inyectadas.
    @PostConstruct
    public void init() {
        // 👇 CAMBIO CLAVE: Creamos una llave segura a partir del texto secreto.
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Método para generar el token
    public String generateJwtToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtExpirationMs))
                // 👇 Usamos la llave segura generada
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // Método para obtener el email/username del token
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    // Método para validar el token
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken);
            return true;
        } catch (Exception e) {
            // Aquí puedes loguear el error si quieres: e.printStackTrace();
        }
        return false;
    }
}