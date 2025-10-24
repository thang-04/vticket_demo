package com.vticket.vticket.config;

import java.util.Date;
import java.util.Map;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger logger = LogManager.getLogger(CustomJwtDecoder.class);

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
                logger.info("Jwt token expired");
            }

            return createJwt(token, claims);

        } catch (Exception e) {
           logger.warn("Failed to decode JWT token: " + e.getMessage(), e);
              throw new JwtException("Invalid JWT token", e);
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
