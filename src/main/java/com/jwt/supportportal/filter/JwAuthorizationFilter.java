package com.jwt.supportportal.filter;

import static com.jwt.supportportal.constant.SecurityConstant.OPTIONS_HTTP_METHOD;
import static com.jwt.supportportal.constant.SecurityConstant.TOKEN_PREFIX;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.jwt.supportportal.utility.JWTTokenProvider;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.AllArgsConstructor;

/**
 * JwAuthorizationFilter
 */

@Component
@AllArgsConstructor
public class JwAuthorizationFilter extends OncePerRequestFilter {

    private JWTTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (request.getMethod().equalsIgnoreCase(OPTIONS_HTTP_METHOD)) {
            
            response.setStatus(OK.value());

        } else {

            String authorizationHeader = request.getHeader(AUTHORIZATION);
            
            if (authorizationHeader == null || !authorizationHeader.startsWith(TOKEN_PREFIX)) {

                filterChain.doFilter(request, response);
                
                return;

            }

            String token = authorizationHeader.substring(TOKEN_PREFIX.length());
            
            String username = this.jwtTokenProvider.getSubject(token);

            if (this.jwtTokenProvider.isTokenValid(username, token)) {

                List<GrantedAuthority> authorities = this.jwtTokenProvider.getAuthorities(token);
                
                Authentication authentication = this.jwtTokenProvider.getAuthentication(username, authorities, request);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } else {

                // anything fails will clear the context
                SecurityContextHolder.clearContext();

            }
        }
        filterChain.doFilter(request, response);
    }

}