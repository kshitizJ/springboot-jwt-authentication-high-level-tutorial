package com.jwt.supportportal.service.implementation;

import static com.jwt.supportportal.constant.FileConstant.*;
import static com.jwt.supportportal.enumeration.Role.ROLE_USER;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.apache.commons.lang3.StringUtils.EMPTY;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.mail.MessagingException;
import javax.transaction.Transactional;

import com.jwt.supportportal.domain.User;
import com.jwt.supportportal.domain.UserPrincipal;
import com.jwt.supportportal.enumeration.Role;
import com.jwt.supportportal.exception.domain.EmailExistException;
import com.jwt.supportportal.exception.domain.EmailNotFoundException;
import com.jwt.supportportal.exception.domain.UserNotFoundException;
import com.jwt.supportportal.exception.domain.UsernameExistException;
import com.jwt.supportportal.repository.UserRepository;
import com.jwt.supportportal.service.EmailService;
import com.jwt.supportportal.service.LoginAttemptService;
import com.jwt.supportportal.service.UserService;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Qualifier("userDetailsService")
@Slf4j
public class UserServiceImplementation implements UserService, UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Autowired
    private EmailService emailService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("Finding username: {}", username);
        User user = userRepository.findUserByUsername(username);

        if (user == null) {

            log.error("Username {} not found in the database.", username);
            throw new UsernameNotFoundException("Invalid username: " + username);

        } else {

            //
            try {

                validateLoginAttempt(user);

            } catch (ExecutionException e) {

                e.printStackTrace();

            }

            log.info("Found username {} in the database", username);
            user.setLastLoginDateDisplay(user.getLastLoginDate());

            user.setLastLoginDate(new Date());

            userRepository.save(user);

            UserPrincipal userPrincipal = new UserPrincipal(user);

            log.info("Returning found user.");
            return userPrincipal;

        }
    }

    private void validateLoginAttempt(User user) throws ExecutionException {

        log.info("Checking if the user is locked or not.");
        if (user.getIsNotLocked()) {

            log.info("User is not locked. Checking if the user has exceeded the number of attempts to login or not.");
            if (loginAttemptService.hasExceededMaxAttempt(user.getUsername())) {

                log.info("Locking the user account because he/she has exceeded the number of tries to log in.");
                user.setIsNotLocked(false);

            } else {

                log.info("User has not yet exceeded the number of tries to log in.");
                user.setIsNotLocked(true);

            }
        } else {

            log.info("User's account is already locked so removing the user from the cache.");
            loginAttemptService.evictUserFromLoginAttemptCache(user.getUsername());

        }
    }

    /**
     * 
     * For registering the user we are passing empty String of currentUsername in
     * validateNewUsernameAndEmail() function.
     * 
     * @throws EmailExistException
     * @throws UsernameExistException
     * @throws UserNotFoundException
     * @throws MessagingException
     * 
     */
    @Override
    public User register(String firstName, String lastName, String username, String email, String password)
            throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException {

        log.info("Validating the username and email.");
        validateNewUsernameAndEmail(EMPTY, username, email);

        log.info("Initializing new User");
        User user = new User();

        log.info("Setting user_id of the user.");
        user.setUserId(generateUserId());

        log.info("Setting encoded password.");
        user.setPassword(passwordEncoder.encode(password));

        log.info("Setting first name.");
        user.setFirstName(firstName);

        log.info("Setting last name.");
        user.setLastName(lastName);

        log.info("Setting email id");
        user.setEmail(email);

        log.info("Setting username.");
        user.setUsername(username);

        log.info("Setting joining date.");
        user.setJoinedDate(new Date());

        log.info("Setting user's account as active.");
        user.setIsActive(true);

        log.info("Setting user's account as not locked.");
        user.setIsNotLocked(true);

        log.info("Giving Role to user.");
        user.setRole(ROLE_USER.name());

        log.info("Giving authorities to the user.");
        user.setAuthorities(ROLE_USER.getAuthorities());

        log.info("Setting profile image url of the user.");
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));

        emailService.sendSuccessfullyRegisterMessage(user.getFirstName(), user.getEmail());

        return userRepository.save(user);

    }

    @Override
    public User addNewUser(String firstName, String lastName, String username, String email, String role,
            boolean isNonLocked, boolean isActive, MultipartFile profileImage)
            throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {

        log.info("Validating the username and email.");
        validateNewUsernameAndEmail(EMPTY, username, email);

        log.info("Initializing new User");
        User user = new User();

        log.info("Setting user_id of the user.");
        user.setUserId(generateUserId());

        log.info("Setting encoded password.");
        user.setPassword(passwordEncoder.encode("randomPassword"));

        log.info("Setting first name.");
        user.setFirstName(firstName);

        log.info("Setting last name.");
        user.setLastName(lastName);

        log.info("Setting email id");
        user.setEmail(email);

        log.info("Setting username.");
        user.setUsername(username);

        log.info("Setting joining date.");
        user.setJoinedDate(new Date());

        log.info("Setting user's account as active.");
        user.setIsActive(true);

        log.info("Setting user's account as not locked.");
        user.setIsNotLocked(true);

        log.info("Giving Role to user.");
        user.setRole(getRoleEnumName(role).name());

        log.info("Giving authorities to the user.");
        user.setAuthorities(getRoleEnumName(role).getAuthorities());

        log.info("Setting profile image url of the user.");
        user.setProfileImageUrl(getTemporaryProfileImageUrl(username));

        // Saving the profile image in the folder.
        saveProfileImage(user, profileImage);

        return userRepository.save(user);
    }

    @Override
    public User updateUser(String currentUsername, String newFirstName, String newLastName, String newUsername,
            String newEmail, String newRole, boolean isNonLocked, boolean isActive, MultipartFile newProfileImage)
            throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {

        log.info("Validating the username and email.");
        User user = validateNewUsernameAndEmail(currentUsername, newUsername, newEmail);

        log.info("Setting first name.");
        user.setFirstName(newFirstName);

        log.info("Setting last name.");
        user.setLastName(newLastName);

        log.info("Setting email id");
        user.setEmail(newEmail);

        log.info("Setting username.");
        user.setUsername(newUsername);

        log.info("Setting user's account as active.");
        user.setIsActive(isActive);

        log.info("Setting user's account as not locked.");
        user.setIsNotLocked(isNonLocked);

        log.info("Giving Role to user.");
        user.setRole(getRoleEnumName(newRole).name());

        log.info("Giving authorities to the user.");
        user.setAuthorities(getRoleEnumName(newRole).getAuthorities());

        // Saving the profile image in the folder.
        saveProfileImage(user, newProfileImage);

        return userRepository.save(user);
    }

    @Override
    public User updateProfileImage(String username, MultipartFile profileImage)
            throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {

        User user = validateNewUsernameAndEmail(username, null, null);

        saveProfileImage(user, profileImage);

        return userRepository.save(user);
    }

    @Override
    public List<User> getUsers() {

        return userRepository.findAll();

    }

    @Override
    public User findUserByUsername(String username) {

        return userRepository.findUserByUsername(username);

    }

    @Override
    public User findUserByEmail(String email) {

        return userRepository.findUserByEmail(email);

    }

    @Override
    public void deleteUser(String userId) {

        userRepository.deleteByUserId(userId);

    }

    @Override
    public void resetPassword(String email) throws EmailNotFoundException, MessagingException {

        User user = findUserByEmail(email);

        if (user == null)
            throw new EmailNotFoundException("No user found by email: " + email);

        String password = "randomPassword";

        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);

        emailService.sendSuccessfullyRegisterMessage(user.getFirstName(), user.getEmail());

    }

    private String getTemporaryProfileImageUrl(String username) {

        /**
         * 
         * this is going to give us the base like
         * 'http://localhost:8081//user/image/profile/temp'
         * 
         * <p
         * So ServletUriComponentsBuilder.fromCurrentContextPath() will return what ever
         * the base url is, i.e. 'http://localhost:8081/'.
         * </p>
         * 
         * <p>
         * And .path() function will add the path to the base url.
         * </p>
         * 
         * 
         */
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(DEFAULT_USER_IMAGE_PATH + FORWARD_SLASH + username).toUriString();

    }

    private String generateUserId() {

        return RandomStringUtils.randomNumeric(10);

    }

    /**
     * 
     * If user is registering his/her credentials then we will pass currentUsername
     * as empty string and 'else' statement will get executed. It will check for the
     * username and email if they exist or not.
     * 
     * <p>
     * If user wants to update his username or email then we will check for his
     * currentUsername, newUsername and email. If currentUsername is not blank that
     * means user wants to update his profile in this case 'if' statement will get
     * executed. Then we will check if the currentUsername is null, we throw an
     * exception that the currentUsername is not there in database. If the
     * currentUsername exist in the database then we check for newUsername, if there
     * is someone with that username who already exist with that username or not. If
     * not then we check for the email. if the there is someone who already exist
     * with that email id then we throw an exception.
     * </p>
     * 
     * 
     * @param currentUsername
     * @param newUsername
     * @param email
     * @return User if there exist a currentUser else null
     * @throws UserNotFoundException
     * @throws UsernameExistException
     * @throws EmailExistException
     */
    private User validateNewUsernameAndEmail(String currentUsername, String newUsername, String email)
            throws UserNotFoundException, UsernameExistException, EmailExistException {

        User userByNewUsername = findUserByUsername(newUsername);

        User userByNewEmail = findUserByEmail(email);

        log.info("Checking if the current username is blank or not.");
        if (StringUtils.isNotBlank(currentUsername)) {

            log.info("Checking if the current username exist in the database or not.");
            User currentUser = findUserByUsername(currentUsername);

            if (currentUser == null)
                throw new UserNotFoundException("No user found by username: " + currentUsername);

            log.info("Checking if the new username already exist in the database or not.");
            if (userByNewUsername != null && !currentUser.getId().equals(userByNewUsername.getId()))
                throw new UsernameExistException("Username already exist.");

            log.info("Checking if the new email id exist in the database or not.");
            if (userByNewEmail != null && !currentUser.getId().equals(userByNewEmail.getId()))
                throw new EmailExistException("Email already exist.");

            log.info("Everything is fine and we can update the user profile.");
            return currentUser;

        } else {

            log.info("Checking if the new username already exist in the database or not.");
            if (userByNewUsername != null)
                throw new UsernameExistException("Username already exist.");

            log.info("Checking if the new email id exist in the database or not.");
            if (userByNewEmail != null)
                throw new EmailExistException("Email already exist.");

            return null;

        }

    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role.toUpperCase());
    }

    private void saveProfileImage(User user, MultipartFile profileImage) throws IOException {

        // Checking if the profile image sent is not null.
        if (profileImage != null) {

            // This Path will be like this:
            // kshitiz/home/Kshitiz/java/Springboot-tutorial/Springboot-security-courses/get-arrays/support-portal/src/main/resources/static/images/user/
            Path userFolder = Paths.get(USER_FOLDER + user.getUsername()).toAbsolutePath().normalize();

            // Checking that the folder exist or else it will not save the image. So if the
            // folder doesnot exist then we create the folder.
            if (!Files.exists(userFolder)) {

                Files.createDirectories(userFolder);
                log.info(DIRECTORY_CREATED + userFolder);

            }

            // If the file exist then we delete the existed file
            Files.deleteIfExists(Paths.get(userFolder + user.getUsername() + DOT + JPG_EXTENSION));

            // Saving the actual file inside the folder. REPLACE_EXISTING = will replace the
            // existing picture.
            Files.copy(profileImage.getInputStream(), userFolder.resolve(user.getUsername() + DOT + JPG_EXTENSION),
                    REPLACE_EXISTING);

            // Changing the user's profile url.
            user.setProfileImageUrl(setProfileImageUrl(user.getUsername()));
            log.info(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());

        }

    }

    private String setProfileImageUrl(String username) {

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(USER_IMAGE_PATH + FORWARD_SLASH + username + FORWARD_SLASH + username + DOT + JPG_EXTENSION)
                .toUriString();

    }

}
