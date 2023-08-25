package com.backend.taskmanager.jwt;

import com.backend.taskmanager.service.UserDetailsImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtCore {
    @Value("${task-manager.jwt.duration}")
    private int lifetime;

    @Value("${task-manager.jwt.secret}")
    private String secret;
    private Key key;
    @PostConstruct
    public void init() {
        byte[] decodedKey = Base64.getDecoder().decode(secret.getBytes(StandardCharsets.UTF_8));
        key = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");
    }



    public String generateToken(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) (authentication.getPrincipal());


        return Jwts.builder()
                .setExpiration(new Date(new Date().getTime() + lifetime))
                .setSubject((userDetails.getUsername()))
                .setIssuedAt(new Date())
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }


    public String getNameFromJwt(String jwt) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(jwt)
                .getBody()
                .getSubject();
    }
}
