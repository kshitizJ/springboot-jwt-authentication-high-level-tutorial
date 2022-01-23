package com.jwt.supportportal.resource;

import static com.jwt.supportportal.constant.FileConstant.*;
import static com.jwt.supportportal.constant.SecurityConstant.JWT_TOKEN_HEADER;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import javax.mail.MessagingException;

import com.jwt.supportportal.domain.HttpResponse;
import com.jwt.supportportal.domain.User;
import com.jwt.supportportal.domain.UserPrincipal;
import com.jwt.supportportal.exception.ExceptionHandling;
import com.jwt.supportportal.exception.domain.EmailExistException;
import com.jwt.supportportal.exception.domain.EmailNotFoundException;
import com.jwt.supportportal.exception.domain.UserNotFoundException;
import com.jwt.supportportal.exception.domain.UsernameExistException;
import com.jwt.supportportal.service.UserService;
import com.jwt.supportportal.utility.JWTTokenProvider;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * If anytime there occurs an exception here, so this class will try to find the
 * handler for that exception in the ExceptionHandling class
 * 
 */

@RestController
@RequestMapping(path = { "/", "/user" })
@Slf4j
public class UserResource extends ExceptionHandling {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTTokenProvider jwtTokenProvider;

    @GetMapping("/home")
    public String showUser() {
        return "good";
    }

    /**
     * 
     * Here first we pass the username and password which is coming from client side
     * to authenticate.
     * 
     * <p>
     * If the user has provided right credentials then we get the user's data by
     * username.
     * </p>
     * 
     * <p>
     * Here we pass the user to UserPrincipal class to check all the roles,
     * authorities and if the user is active or not, etc.
     * </p>
     * 
     * <p>
     * Then we generate a our headers which will contain the JWT token for our user.
     * </p>
     * 
     * @param user
     * @return Response of User and JWT token, if he/she successfully logs in with
     *         right credentials.
     */
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody User user) {

        log.info("Authenticating the user.");
        authenticate(user.getUsername(), user.getPassword());

        log.info("Authentication of the user is successfull.");
        User loggedInUser = userService.findUserByUsername(user.getUsername());

        log.info("Setting the UserPrincipal for our logged in user.");
        UserPrincipal userPrincipal = new UserPrincipal(loggedInUser);

        log.info("Getting the headers for JWT Token.");
        HttpHeaders jwtHeaders = getJwtHeader(userPrincipal);

        log.info("Passing the user and header as a json response.");
        return new ResponseEntity<>(loggedInUser, jwtHeaders, OK);

    }

    /**
     * 
     * When first time user visits our page, we register is in our database.
     * 
     * @param user
     * @return Complete details of the user.
     * @throws UserNotFoundException
     * @throws UsernameExistException
     * @throws EmailExistException
     * @throws MessagingException
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody User user)
            throws UserNotFoundException, UsernameExistException, EmailExistException, MessagingException {

        log.info("Registering the new user.");
        return new ResponseEntity<>(
                userService.register(user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail(),
                        user.getPassword()),
                OK);

    }

    @PostMapping("/add")
    public ResponseEntity<User> addNewUser(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("role") String role,
            @RequestParam("isActive") String isActive,
            @RequestParam("isNonLocked") String isNonLocked,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage)
            throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {

        User user = userService.addNewUser(firstName, lastName, username, email, role, Boolean.parseBoolean(
                isNonLocked), Boolean.parseBoolean(isActive), profileImage);

        return new ResponseEntity<>(user, OK);

    }

    @PostMapping("/update")
    public ResponseEntity<User> updateUser(
            @RequestParam("currentUsername") String currentUsername,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("role") String role,
            @RequestParam("isActive") String isActive,
            @RequestParam("isNonLocked") String isNonLocked,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage)
            throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {

        User updateUser = userService.updateUser(
                currentUsername, firstName, lastName, username, email, role, Boolean.parseBoolean(
                        isNonLocked),
                Boolean.parseBoolean(isActive), profileImage);

        return new ResponseEntity<>(updateUser, OK);

    }

    @GetMapping("/find/{username}")
    public ResponseEntity<User> getUser(@PathVariable("username") String username) {

        User user = userService.findUserByUsername(username);

        return new ResponseEntity<>(user, OK);

    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> getAllUsers() {

        List<User> users = userService.getUsers();

        return new ResponseEntity<>(users, OK);

    }

    @GetMapping("/resetPassword/{email}")
    public ResponseEntity<HttpResponse> resetPassword(@PathVariable("email") String email)
            throws EmailNotFoundException, MessagingException {

        userService.resetPassword(email);

        return response(OK, "Email sent to: " + email);

    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity<HttpResponse> deleteUser(@PathVariable("id") String userId) {

        userService.deleteUser(userId);

        return response(OK, "User deleted successfully.");

    }

    @PostMapping("/updateProfileImage")
    public ResponseEntity<User> updateProfileImage(@RequestParam("username") String username,
            @RequestParam(value = "profileImage") MultipartFile profileImage)
            throws UserNotFoundException, UsernameExistException, EmailExistException, IOException {

        User user = userService.updateProfileImage(username, profileImage);

        return new ResponseEntity<>(user, OK);

    }

    @GetMapping(path = "/image/{username}/{fileName}", produces = IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable("username") String username, @PathVariable("fileName") String fileName)
            throws IOException {

        return Files.readAllBytes(Paths.get(USER_FOLDER + username + FORWARD_SLASH + fileName));

    }

    @GetMapping(path = "/image/profile/{username}", produces = IMAGE_JPEG_VALUE)
    public byte[] getTempProfileImage(@PathVariable("username") String username) throws IOException {

        URL url = new URL(TEMP_PROFILE_IMAGE_BASE_URL + username);

        // Creating a byte[] array Output Stream so that it can store all the data
        // coming from the url. With the help of ByteArrayOutputStream class we will be
        // able to capture the stream from the url and then covert it into byte array.
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (InputStream inputStream = url.openStream()) {

            // Here we are saying that from inputStream that we open using url, read 1024
            // bytes at a time.
            int bytesRead;
            byte[] chunk = new byte[1024];

            // in this while loop we are saying that 'give me that many bytes eveyrtime we
            // loop through this until there is no more'.
            while ((bytesRead = inputStream.read(chunk)) > 0) {

                // I am gonna read chunk which has length of 1024 and we start from 0 till the
                // bytesRead.
                // Here bytesRead is the length of everything that we are reading.
                byteArrayOutputStream.write(chunk, 0, bytesRead);

            }
        }

        // Then we return the our byteArray,
        return byteArrayOutputStream.toByteArray();

    }

    /**
     * 
     * This method takes the valid UserPrincipal
     * 
     * <p>
     * Then we initiailize HttpHeaders instance to generate a response of jwt token.
     * </p>
     * 
     * <p>
     * Then we pass our UserPrincipal which contains the user credentials to
     * JwtTokenProvider class which will in reponse return a JWT token for that
     * user.
     * </p>
     * 
     * <p>
     * Then we add our header's name and token in the header instance and return the
     * header.
     * </p>
     * 
     * @param userPrincipal
     * @return
     */
    private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {

        HttpHeaders headers = new HttpHeaders();

        headers.add(JWT_TOKEN_HEADER, jwtTokenProvider.generateJwtToken(userPrincipal));

        return headers;
    }

    /**
     * 
     * Here we use our authentication manager to authenticate the username and
     * password.
     * 
     * <p>
     * If the credentials are incorrect then this method will throw and exception
     * and user won't be able to login in with wrong credentials.
     * </p>
     * 
     * @param username
     * @param password
     */
    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }

    private ResponseEntity<HttpResponse> response(HttpStatus httpStatus, String message) {

        return new ResponseEntity<>(
                new HttpResponse(httpStatus.value(), httpStatus, httpStatus.getReasonPhrase(), message), httpStatus);

    }

}
