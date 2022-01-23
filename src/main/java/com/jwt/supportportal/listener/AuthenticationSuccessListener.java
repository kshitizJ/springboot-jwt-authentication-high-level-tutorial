package com.jwt.supportportal.listener;

import com.jwt.supportportal.domain.UserPrincipal;
import com.jwt.supportportal.service.LoginAttemptService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSuccessListener {

    @Autowired
    private LoginAttemptService loginAttemptService;

    /**
     * 
     * Here we know that the user has successfully logged in or is authenticated so
     * we remove the user from the cache.
     * 
     * @param event
     */
    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {

        // here we know that once the user is authenticated, getPrincipal() method will
        // always return the authenticated Object which will be of type UserPrincipal.
        // So we use Object here to check if the returned Object is of type
        // UserPrincipal for security purpose.
        Object principal = event.getAuthentication().getPrincipal();

        if (principal instanceof UserPrincipal) {

            UserPrincipal user = (UserPrincipal) event.getAuthentication().getPrincipal();

            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());

        }

    }

}
