package com.jwt.supportportal.filter;

import static com.jwt.supportportal.constant.SecurityConstant.FORBIDDEN_MESSAGE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwt.supportportal.domain.HttpResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

/**
 * JwtAuthenticationEntryPoint
 * <p>
 * Whenever user fails to provide authentication and tries to access the
 * application then Http403ForbiddenEntryPoint class gets fired.
 * </p>
 * We will extend that class and provide a nice response if the user is not
 * authenticated
 * 
 */

@Component
public class JwtAuthenticationEntryPoint extends Http403ForbiddenEntryPoint {

    /**
     * This method overrides the default error reponse.
     * <p>
     * We will send our own error response with the help of this method.
     * </p>
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        
        HttpResponse httpResponse = new HttpResponse(FORBIDDEN.value(), FORBIDDEN,
                FORBIDDEN.getReasonPhrase().toUpperCase(), FORBIDDEN_MESSAGE);
        
        response.setContentType(APPLICATION_JSON_VALUE);
        
        response.setStatus(FORBIDDEN.value());
        
        OutputStream outputStream = response.getOutputStream();
        
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.writeValue(outputStream, httpResponse);
        
        outputStream.flush();
    }

}