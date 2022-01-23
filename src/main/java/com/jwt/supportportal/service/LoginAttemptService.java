package com.jwt.supportportal.service;

import static java.util.concurrent.TimeUnit.MINUTES;

import java.util.concurrent.ExecutionException;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.springframework.stereotype.Service;

/**
 * 
 * In this class we are going to check how many times user attempts to login.
 * 
 * <p>
 * Here we use google's guava library to cache user's attemps.
 * </p>
 * 
 */
@Service
public class LoginAttemptService {

    private static final int MAXIMUM_NUMBER_OF_ATTEMPTS = 5;

    private static final int ATTEMPT_INCREMENT = 1;

    private LoadingCache<String, Integer> loginAttemptCache;

    /**
     * 
     * LoginAttempService() constructor will assign loginAttempCache a new Cache
     * with different features.
     * 
     * Here we set the expiration of the cache as 15 minutes.
     * 
     * Maximum size of the cache is to take 100 users at a time.
     * Eg: ("kshitizJ", 1), ("omiChougule". 4), ("duttaJyoti",2)
     * 
     * Then we build the cache using CacheLoader class and add the unimplemented
     * method.
     * 
     */
    public LoginAttemptService() {
        super();
        loginAttemptCache = CacheBuilder.newBuilder().expireAfterWrite(15, MINUTES).maximumSize(100)
                .build(new CacheLoader<String, Integer>() {

                    @Override
                    public Integer load(String key) throws Exception {

                        return 0;

                    }

                });
    }

    /**
     * 
     * In this method we are removing the user from the cache.
     * 
     * <p> Say if the user has provided right credentials then we remove the user from
     * the cache.
     * 
     * @param username
     */
    public void evictUserFromLoginAttemptCache(String username) {

        loginAttemptCache.invalidate(username);

    }

    /**
     * 
     * Adding new User to Cache and intializing his/her attempts.
     * 
     * @param username
     * @throws ExecutionException
     */
    public void addUserToLoginAttemptCache(String username) throws ExecutionException {

        int attempts = 0;

        attempts = ATTEMPT_INCREMENT + loginAttemptCache.get(username);

        loginAttemptCache.put(username, attempts);

    }

    /**
     * 
     * Checking if the user's login attempt has exceeded the maximum limit.
     * 
     * @param username
     * @return
     * @throws ExecutionException
     */
    public boolean hasExceededMaxAttempt(String username) throws ExecutionException {

        return loginAttemptCache.get(username) >= MAXIMUM_NUMBER_OF_ATTEMPTS;

    }

}
