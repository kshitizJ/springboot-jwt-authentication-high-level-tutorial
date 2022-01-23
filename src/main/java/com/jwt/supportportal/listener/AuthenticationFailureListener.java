package com.jwt.supportportal.listener;

import java.util.concurrent.ExecutionException;

import com.jwt.supportportal.service.LoginAttemptService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

/**
 * 
 * This class will act as a event listener and check the user's credentials.
 * 
 */
@Component
public class AuthenticationFailureListener {

    @Autowired
    private LoginAttemptService loginAttemptService;

    /**
     * 
     * AuthenticationFailureBadCredentialsEvent class is the event that is fired
     * when the user tries to log in but they didn't provide right credentials.
     * 
     * Here we annotate the method by @EventListener because we are listening to
     * that event when ever it occurs. Then we grab the principal and check if the
     * Object is an instance of String becuase our username is of type String class.
     * 
     * 
     * @param event
     * @throws ExecutionException
     */
    @EventListener
    public void onAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) throws ExecutionException {

        Object principal = event.getAuthentication().getPrincipal();

        if (principal instanceof String) {

            String username = (String) event.getAuthentication().getPrincipal();
            
            loginAttemptService.addUserToLoginAttemptCache(username);
        
        }

    }

}
