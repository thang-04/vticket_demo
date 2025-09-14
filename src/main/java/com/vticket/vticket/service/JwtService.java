package com.vticket.vticket.service;

import com.vticket.vticket.config.Config;
import com.vticket.vticket.domain.mongodb.entity.User;
import io.jsonwebtoken.*;
import io.micrometer.common.util.StringUtils;
import lombok.experimental.NonFinal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private static final Logger logger = LogManager.getLogger(JwtService.class);

    @NonFinal
    @Value("${jwt.signerKey}")
    protected String SIGNER_KEY;


//    public String generateToken(User user, Date expireDate, String clientKey) {
//
//        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
//        String username = "";
//        if (StringUtils.isNotEmpty(user.getUsername())) {
//            username += user.getUsername();
//        }
//        String access_token = "";
//        if (StringUtils.isNotEmpty(user.getAccess_token())) {
//            access_token += user.getAccess_token();
//        }
//        String id = "" + user.getId();
//        String uuid = UUID.randomUUID().toString();
//        long created = System.currentTimeMillis();
//
//        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
//                .subject(user.getUsername())
//                .issuer("thangnd.com")
//                .issueTime(new Date())
//                .expirationTime(expireDate)

    /// /                    .claim("scope", buildScope(user))
//                .claim("username", username)
//                .claim("uuid", uuid)
//                .claim("access_token", access_token)
//                .claim("created", created)
//                .claim("clientKey", clientKey)
//                .build();
//
//        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
//
//        JWSObject jwsObject = new JWSObject(header, payload);
//
//        try {
//            jwsObject.sign(new MACSigner(SIGNER_KEY.getBytes()));
//            return jwsObject.serialize();
//        } catch (JOSEException e) {
//            throw new RuntimeException(e);
//        }
//    }
    public String generateToken(User user, Date expireDate) {
        logger.debug("Generating JWT token for user: {}", user.getUsername());
        try {
            //The JWT signature algorithm we will be using to sign the token
            SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
            //We will sign our JWT with our ApiKey secret
            byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(SIGNER_KEY);
            Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
            //Let's set the JWT Claims
            String username = "";
            if (StringUtils.isNotEmpty(user.getUsername())) {
                username += user.getUsername();
            }
            String access_token = "";
            if (StringUtils.isNotEmpty(user.getAccess_token())) {
                access_token += user.getAccess_token();
            }
            String id = "" + user.getId();
            String uuid = UUID.randomUUID().toString();
            long created = System.currentTimeMillis();

            JwtBuilder builder = Jwts.builder().setId(id)
                    .setIssuedAt(new Date())
                    .setSubject("Viettel Media Game platform")
                    .setIssuer("Viettel")
                    .claim("username", username)
                    .claim("uuid", uuid)
                    .claim("access_token", access_token)
                    .claim("created", created)
                    .claim("clientKey", SIGNER_KEY)
                    .setHeaderParam("typ", "JWS")
                    .signWith(signatureAlgorithm, signingKey);

            builder.setExpiration(expireDate);
            String token = builder.compact();
            logger.info("Successfully generated JWT token for user: {} with expiration: {}", username, expireDate);
            return token;
        } catch (Exception ex) {
            logger.error("Error generating JWT token for user: {} - {}", user.getUsername(), ex.getMessage(), ex);
            return "";
        }
    }

    public User verifyAcessToken(String jwt) {
        logger.debug("Verifying access token");
        User user = new User();
        try {
            if (StringUtils.isEmpty(jwt)) {
                logger.warn("Empty JWT token provided");
                user = null;
            } else {
                Claims claims = Jwts.parser()
                        .setSigningKey(DatatypeConverter.parseBase64Binary(SIGNER_KEY))
                        .parseClaimsJws(jwt).getBody();
                String username = claims.get("username", String.class);
                String access_token = claims.get("access_token", String.class);
                String id = claims.getId();
                user.setAccess_token(access_token);
                user.setUsername(username);
                user.setId(id);
                logger.info("Successfully verified access token for user: {}", username);
            }
        } catch (ExpiredJwtException ex) {
            logger.warn("JWT token has expired: {}", ex.getMessage());
            if (user != null) {
                user.setId(String.valueOf(Config.CODE.ERROR_CODE_103));
            }
        } catch (Exception ex) {
            logger.error("Error verifying access token: {}", ex.getMessage(), ex);
            user = null;
        }
        return user;
    }

    public User verifyRefreshToken(String jwt) {
        logger.debug("Verifying refresh token");
        User user = new User();
        try {
            if (StringUtils.isEmpty(jwt)) {
                logger.warn("Empty refresh token provided");
                user = null;
            } else {
                Claims claims = Jwts.parser()
                        .setSigningKey(DatatypeConverter.parseBase64Binary(SIGNER_KEY))
                        .parseClaimsJws(jwt).getBody();
                String username = claims.get("username", String.class);
                String access_token = claims.get("access_token", String.class);
                long created = claims.get("created", Long.class);
                long dateNow = System.currentTimeMillis();
                if (dateNow - created < 10000) {
                    //khong cho phep thuc hien lien tuc trong 10s
                    logger.warn("Refresh token used too frequently for user: {}", username);
                    user = null;
                } else {
                    String id = claims.getId();
                    user.setAccess_token(access_token);
                    user.setUsername(username);
                    user.setId(id);
                    logger.info("Successfully verified refresh token for user: {}", username);
                }
            }
        } catch (ExpiredJwtException ex) {
            logger.warn("Refresh token has expired: {}", ex.getMessage());
            if (user != null) {
                user.setId(String.valueOf(Config.CODE.ERROR_CODE_103));
            }
        } catch (Exception ex) {
            logger.error("Error verifying refresh token: {}", ex.getMessage(), ex);
            user = null;
        }
        return user;
    }



}
