package com.jwt.supportportal.filter;

import static com.jwt.supportportal.constant.SecurityConstant.ACCESS_DENIED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jwt.supportportal.domain.HttpResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException exception) throws IOException, ServletException {

        HttpResponse httpResponse = new HttpResponse(UNAUTHORIZED.value(),
                UNAUTHORIZED,
                UNAUTHORIZED.getReasonPhrase().toUpperCase(), ACCESS_DENIED);
        
        response.setContentType(APPLICATION_JSON_VALUE);
        
        response.setStatus(UNAUTHORIZED.value());
        
        OutputStream outputStream = response.getOutputStream();
        
        ObjectMapper mapper = new ObjectMapper();
        
        mapper.writeValue(outputStream, httpResponse);
        
        outputStream.flush();

    }

}
