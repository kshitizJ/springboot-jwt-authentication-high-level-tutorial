package com.jwt.supportportal.utility;

import static com.auth0.jwt.algorithms.Algorithm.HMAC256;
import static com.jwt.supportportal.constant.SecurityConstant.AUTHORITIES;
import static com.jwt.supportportal.constant.SecurityConstant.EXPIRATION_TIME;
import static com.jwt.supportportal.constant.SecurityConstant.GET_ARRAYS_ADMINISTRATION;
import static com.jwt.supportportal.constant.SecurityConstant.GET_ARRAYS_LLC;
import static com.jwt.supportportal.constant.SecurityConstant.TOKEN_CANNOT_BE_VERIFIED;
import static java.util.Arrays.stream;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.jwt.supportportal.domain.UserPrincipal;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

@Component
public class JWTTokenProvider {

    // value is coming from application.yml file
    @Value("${jwt.secret}")
    private String secret;

    public String generateJwtToken(UserPrincipal principal) {

        String[] claims = getClaimsFromUser(principal);

        return JWT.create()
                .withIssuer(GET_ARRAYS_LLC)
                .withAudience(GET_ARRAYS_ADMINISTRATION)
                .withIssuedAt(new Date())
                .withSubject(principal.getUsername())
                .withArrayClaim(AUTHORITIES, claims)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(HMAC256(secret.getBytes()));

    }

    public List<GrantedAuthority> getAuthorities(String token) {

        String[] claims = getClaimsFromToken(token);

        return stream(claims).map(SimpleGrantedAuthority::new).collect(Collectors.toList());

    }

    public Authentication getAuthentication(String username, List<GrantedAuthority> authorities,
            HttpServletRequest request) {

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                username, null, authorities);

        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        return usernamePasswordAuthenticationToken;

    }

    public boolean isTokenValid(String username, String token) {

        JWTVerifier verifier = getJWTVerifier();

        return StringUtils.isNotEmpty(username) && !isTokenExpired(verifier, token);

    }

    public String getSubject(String token) {

        JWTVerifier verifier = getJWTVerifier();

        return verifier.verify(token).getSubject();

    }

    private boolean isTokenExpired(JWTVerifier verifier, String token) {

        Date expiration = verifier.verify(token).getExpiresAt();

        return expiration.before(new Date());

    }

    private String[] getClaimsFromToken(String token) {

        JWTVerifier verifier = getJWTVerifier();

        return verifier.verify(token).getClaim(AUTHORITIES).asArray(String.class);

    }

    private JWTVerifier getJWTVerifier() {

        JWTVerifier verifier;

        try {

            Algorithm algorithm = HMAC256(secret);

            verifier = JWT.require(algorithm).withIssuer(GET_ARRAYS_LLC).build();

        } catch (JWTVerificationException exception) {

            throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED);

        }

        return verifier;

    }

    private String[] getClaimsFromUser(UserPrincipal principal) {

        List<String> authorities = new ArrayList<>();

        for (GrantedAuthority authority : principal.getAuthorities())
            authorities.add(authority.getAuthority());

        return authorities.toArray(new String[0]);

    }

}
