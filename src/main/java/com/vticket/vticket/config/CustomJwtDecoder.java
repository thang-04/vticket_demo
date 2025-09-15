package com.vticket.vticket.config;


import java.util.Date;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

@Component
public class CustomJwtDecoder implements JwtDecoder {

    @Value("${jwt.signerKey}")
    private String signerKey;

    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            // Parse và validate JWT token
            byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(signerKey);
            SecretKeySpec signingKey = new SecretKeySpec(apiKeySecretBytes, SignatureAlgorithm.HS256.getJcaName());

            JwtParser parser = Jwts.parser().setSigningKey(signingKey);
            Claims claims = parser.parseClaimsJws(token).getBody();

            // check expiration token
            if (claims.getExpiration().before(new Date())) {
                throw new JwtException("Token expired");
            }

            return createJwt(token, claims);

        } catch (Exception e) {
            throw new JwtException("Invalid token: " + e.getMessage());
        }
    }

    private Jwt createJwt(String tokenValue, Claims claims) {
        return new Jwt(tokenValue,
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant(),
                Map.of("typ", "JWS", "alg", "HS256"), // headers
                claims); // claims
    }
}
